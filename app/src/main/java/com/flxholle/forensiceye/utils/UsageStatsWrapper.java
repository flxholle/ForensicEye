/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.flxholle.forensiceye.utils;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/**
 * Wrapper class for <code>queryEvents(…)</code> calls to the UsageStatsManager class
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class UsageStatsWrapper {
    protected final Context context;
    protected static UsageStatsManager usageStatsManager;

    public UsageStatsWrapper(Context context) {
        this.context = context;
        usageStatsManager = (UsageStatsManager) context
                .getSystemService(Context.USAGE_STATS_SERVICE);
    }


    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     *
     * @see <a href="https://stackoverflow.com/a/28921586">StackOverflow</a>
     */
    public boolean isPermissionGranted() {
        return isPermissionGranted(context);
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     */
    public static boolean isPermissionGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private final UnmatchedCloseEventGuardian guardian = new UnmatchedCloseEventGuardian(usageStatsManager);

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified period.
     *
     * Comments refer to the cases from
     * <a href="https://codeberg.org/fynngodau/usageDirect/wiki/Event-log-wrapper-scenarios">
     *     the documentation.</a>
     *
     * @param start First point in time to include in results
     * @param end   Last point in time to include in results
     * @return A list of foreground stats for the specified period
     */
    public List<UsageStatsEntry> getForegroundStatsByTimestamps(long start, long end) {

        /*
         * Because sometimes, open events do not have close events when they should, as a hack / workaround,
         * we query the apps currently in the foreground and match them against the apps that are currently
         * in the foreground if the query start date is very recent or in the future. Thus, we are using this
         * to tell apart True from Faulty unmatched open events.
         *
         * We query processes in the beginning of this method call in case querying the event log takes a
         * little longer.
         */
        List<String> foregroundProcesses = new ArrayList<>();
        if (end >= System.currentTimeMillis() - 1500) {

            // Get foreground tasks
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    foregroundProcesses.add(appProcess.processName);
                }
            }
        }


        // Assumption: events are ordered chronologically
        UsageEvents events = usageStatsManager.queryEvents(start, end);

        /* …except that sometimes, the events that are close to each other are swapped in a way that
         * breaks the assumption that all end times which do not have a matching start time have
         * started before start. We handle those as Duplicate close event and Duplicate open event.
         * Therefore, we keep null entries in our moveToForegroundMap instead of removing the entries
         * to prevent apps that had been opened previously in a period from being counted as "opened
         * before start" (as they are not a True unmatched close event).
         */

        // Map components to the last moveToForeground event
        Map<AppClass, Long> moveToForegroundMap = new HashMap<>();

        // Collect timespans during which components are in foreground
        ArrayList<UsageStatsEntry> usageStatEntries = new ArrayList<>();

        // Iterate over events
        UsageEvents.Event event = new UsageEvents.Event();
        AppClass appClass;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            appClass = new AppClass(event.getPackageName(), event.getClassName());

            switch (event.getEventType()) {
                /*
                 * "An event type denoting that an android.app.Activity moved to the foreground."
                 * (old definition: "An event type denoting that a component moved to the foreground.")
                 */
                case UsageEvents.Event.ACTIVITY_RESUMED:
                    /*
                     * public static final int android.app.usage.UsageEvents.Event.CONTINUE_PREVIOUS_DAY = 4;
                     * (annotated as @hide)
                     * "An event type denoting that a component was in the foreground the previous day.
                     * This is effectively treated as a MOVE_TO_FOREGROUND."
                     */
                case 4:
                    // Store open timestamp in map, overwriting earlier timestamps in case of Duplicate open event
                    moveToForegroundMap.put(appClass, event.getTimeStamp());

                    break;
                /*
                 * "An event type denoting that an android.app.Activity moved to the background."
                 * (old definition: "An event type denoting that a component moved to the background.")
                 */
                case UsageEvents.Event.ACTIVITY_PAUSED:
                    /*
                     * "An activity becomes invisible on the UI, corresponding to Activity.onStop()
                     * of the activity's lifecycle."
                     */
                case UsageEvents.Event.ACTIVITY_STOPPED:
                    /*
                     * public static final int android.app.usage.UsageEvents.Event.END_OF_DAY = 3;
                     * (annotated as @hide)
                     * "An event type denoting that a component was in the foreground when the stats
                     * rolled-over. This is effectively treated as a {@link #MOVE_TO_BACKGROUND}."
                     */
                case 3:
                    Long eventBeginTime = moveToForegroundMap.get(appClass);
                    if (eventBeginTime != null) {
                        // Open and close events in order
                        moveToForegroundMap.put(appClass, null);
                    } else if (
                        // App has not been in this query yet (test for Duplicate close event)
                            moveToForegroundMap.keySet().stream()
                                    .noneMatch(key -> event.getPackageName().equals(key.packageName)) &&
                                    /*
                                     * Test if this unmatched close event is True by asking the Guardian
                                     * to scan for it
                                     */
                                    guardian.test(event, start)
                    ) {
                        // Identified as True unmatched close event
                        // Take start as a starting timestamp
                        eventBeginTime = start;
                    } else break; // Ignore Faulty unmatched close event

                    // Check if another of the app's components have moved to the foreground in the meantime
                    OptionalLong endTime =
                            moveToForegroundMap.entrySet().stream()
                                    .filter(entry -> event.getPackageName().equals(entry.getKey().packageName))
                                    .filter(entry -> entry.getValue() != null)
                                    .mapToLong(entry -> entry.getValue())
                                    .min();

                    usageStatEntries.add(new UsageStatsEntry(
                            eventBeginTime,
                            endTime.orElse(event.getTimeStamp()),
                            event.getPackageName()
                    ));
                    break;
                /*
                 * "An event type denoting that the Android runtime underwent a shutdown process. A
                 * DEVICE_SHUTDOWN event should be treated as if all started activities and
                 * foreground services are now stopped and no explicit ACTIVITY_STOPPED and
                 * FOREGROUND_SERVICE_STOP events will be generated for them.
                 * [… A]ny open events without matching close events between DEVICE_SHUTDOWN and
                 * DEVICE_STARTUP should be ignored because the closing time is unknown."
                 */
                case UsageEvents.Event.DEVICE_SHUTDOWN:
                    // Per docs: iterate over remaining start events and treat them as closed
                    for (AppClass key : moveToForegroundMap.keySet()) {

                        if (moveToForegroundMap.get(key) == null) {
                            // Not a remaining start event
                            continue;
                        }

                        usageStatEntries.add(new UsageStatsEntry(
                                moveToForegroundMap.get(key),
                                event.getTimeStamp(),
                                key.packageName
                        ));

                        // Set entire app to closed
                        moveToForegroundMap.keySet().stream()
                                .filter(key1 -> key.packageName.equals(key1.packageName))
                                .forEach(samePackageKey -> moveToForegroundMap.put(samePackageKey, null));
                    }
                    break;
                /*
                 * "An event type denoting that the Android runtime started up. This could be after
                 * a shutdown or a runtime restart. Any open events without matching close events
                 * between DEVICE_SHUTDOWN and DEVICE_STARTUP should be ignored because the
                 * closing time is unknown."
                 */
                case UsageEvents.Event.DEVICE_STARTUP:
                    // Per docs: remove pending open events
                    for (AppClass key : moveToForegroundMap.keySet()) {
                        // Overwrite all times with null
                        moveToForegroundMap.put(key, null);
                    }

                    /* No package could be open longer than a reboot. Thus, we set the `start`
                     * timestamp to the boot event's timestamp in case we later assume that a
                     * package has been open "since the start of the period". It is not logical
                     * that this would happen but we can never know with this API.
                     */
                    start = event.getTimeStamp();
                    break;

            }
        }

        // Iterate over remaining start events
        for (AppClass key : moveToForegroundMap.keySet()) {

            if (moveToForegroundMap.get(key) == null) {
                // Not a remaining start event
                continue;
            }

            // Test if foreground app
            for (String foregroundProcess : foregroundProcesses) {
                if (foregroundProcess.contains(key.packageName)) {

                    // Is a foreground app (True unmatched open event)
                    usageStatEntries.add(new UsageStatsEntry(
                            moveToForegroundMap.get(key),
                            Math.min(System.currentTimeMillis(), end),
                            key.packageName
                    ));

                    break;
                }
            }

            // If app is not in foreground, drop event
            // Assume Faulty unmatched open event
        }

        /* If nothing happened during the timespan but there is an app in the foreground,
         * then this app was used the whole period time and there was No event for it.
         * Because the foreground applications API call is documented as not to be used
         * for purposes like this, we first query whether the process name is a valid
         * package name and if not, we drop it.
         */
        if (moveToForegroundMap.keySet().isEmpty()) {
            PackageManager packageManager = context.getPackageManager();
            for (String foregroundProcess : foregroundProcesses) {
                if (packageManager.getLaunchIntentForPackage(foregroundProcess) != null) {
                    usageStatEntries.add(
                            new UsageStatsEntry(
                                    start,
                                    Math.min(System.currentTimeMillis(), end),
                                    foregroundProcess
                            )
                    );
                    Log.d("EventLogWrapper", "Assuming that application " + foregroundProcess + " has been used " +
                            "the whole query time");
                }
            }
        }


        return usageStatEntries;
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified relative day.
     *
     * @param offset Day to query back in time relative to today
     */
    public List<UsageStatsEntry> getForegroundStatsByRelativeDay(int offset) {

        // Calculate timespan to query
        LocalDate queryDay = LocalDate.now()
                .minusDays(offset);

        long beginTime = queryDay
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long endTime = queryDay
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return getForegroundStatsByTimestamps(beginTime, endTime);
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics starting at <code>start</code> and ending at
     * the end of the day that contains <code>start</code>.
     *
     * @param start Starting time of query and point in time in day to query
     */
    public List<UsageStatsEntry> getForegroundStatsByPartialDay(long start) {
        ZoneId zone = ZoneId.systemDefault();
        long endTime = Instant.ofEpochMilli(start)
                .atZone(zone)
                .toLocalDate() // remove time (and zone) information
                .plusDays(1) // go one day ahead
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli();

        return getForegroundStatsByTimestamps(start, endTime);
    }


    /**
     * Collects <b>all</b> event information from system to calculate and aggregate precise
     * foreground time statistics for the provided day.
     *
     * @param day Day since epoch
     */
    public List<UsageStatsEntry> getForegroundStatsByDay(long day) {
        LocalDate date = LocalDate.ofEpochDay(day);
        long start = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long end = date.plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        return getForegroundStatsByTimestamps(start, end);
    }


    /**
     * Stores a class name and its corresponding package.
     */
    private static class AppClass {
        public @NonNull String packageName;
        public @NonNull String className;

        public AppClass(@NonNull String packageName, @NonNull String className) {
            this.packageName = packageName;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AppClass appClass = (AppClass) o;

            if (!packageName.equals(appClass.packageName)) return false;
            return className.equals(appClass.className);
        }

        @Override
        public int hashCode() {
            int result = packageName.hashCode();
            result = 31 * result + className.hashCode();
            return result;
        }
    }


    /**
     * “…a diminutive Guardian who traveled backward through time…”
     * <p>
     * Guards {@link UsageStatsWrapper} against Faulty unmatched close events (per
     * <a href="https://codeberg.org/fynngodau/usageDirect/wiki/Event-log-wrapper-scenarios">
     * docs</a>) by seeking backwards through time and scanning for the open event.
     */
    private static class UnmatchedCloseEventGuardian {

        private final UsageStatsManager usageStatsManager;

        private static final long SCAN_INTERVAL = 1000 * 60 * 60 * 24; // 24 hours

        UnmatchedCloseEventGuardian(UsageStatsManager usageStatsManager) {
            this.usageStatsManager = usageStatsManager;
        }

        /**
         * @param event      Event to validate
         * @param queryStart Timestamp at which original query started
         * @return True if the event is valid, false otherwise
         */
        public boolean test(UsageEvents.Event event, long queryStart) {

            UsageEvents events = usageStatsManager.queryEvents(queryStart - SCAN_INTERVAL, queryStart);

            // Iterate over events
            UsageEvents.Event e = new UsageEvents.Event();

            // Track whether the package is currently in foreground or background
            boolean open = false; // Not open until opened

            while (events.hasNextEvent()) {
                events.getNextEvent(e);

                if (e.getEventType() == UsageEvents.Event.DEVICE_STARTUP) {
                    // Consider all apps closed after startup according to docs
                    open = false;
                }

                // Only consider events concerning our package otherwise
                if (event.getPackageName().equals(e.getPackageName())) {
                    switch (e.getEventType()) {
                        // see EventLogWrapper
                        case UsageEvents.Event.ACTIVITY_RESUMED:
                        case 4:
                            open = true;
                            break;
                        case UsageEvents.Event.ACTIVITY_PAUSED:
                        case 3:
                            if (e.getTimeStamp() == event.getTimeStamp()) {
                                // Ignore original event
                                break;
                            }
                            open = false;
                    }
                }
            }

            Log.d("Guardian", "Scanned for package " + event.getPackageName() + " and determined event to be " +
                    (open ? "True" : "Faulty")
            );

            // Event is valid if it was previously opened (within SCAN_INTERVAL)
            return open;
        }
    }
}
