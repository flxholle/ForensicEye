#!/bin/bash
echo "
███████████                                                 ███           ██████████
░░███░░░░░░█                                                ░░░           ░░███░░░░░█
 ░███   █ ░   ██████  ████████   ██████  ████████    █████  ████   ██████  ░███  █ ░  █████ ████  ██████
 ░███████    ███░░███░░███░░███ ███░░███░░███░░███  ███░░  ░░███  ███░░███ ░██████   ░░███ ░███  ███░░███
 ░███░░░█   ░███ ░███ ░███ ░░░ ░███████  ░███ ░███ ░░█████  ░███ ░███ ░░░  ░███░░█    ░███ ░███ ░███████
 ░███  ░    ░███ ░███ ░███     ░███░░░   ░███ ░███  ░░░░███ ░███ ░███  ███ ░███ ░   █ ░███ ░███ ░███░░░
 █████      ░░██████  █████    ░░██████  ████ █████ ██████  █████░░██████  ██████████ ░░███████ ░░██████
░░░░░        ░░░░░░  ░░░░░      ░░░░░░  ░░░░ ░░░░░ ░░░░░░  ░░░░░  ░░░░░░  ░░░░░░░░░░   ░░░░░███  ░░░░░░
                                                                                       ███ ░███
                                                                                      ░░██████
                                                                                       ░░░░░░
"

echo "Starting Forensic Eye Auto Run..."
echo "Make sure that USB Debugging is enabled on your Android device."


if ! adb shell pm list packages | grep -q "com.flxholle.forensiceye"; then
    echo "Installing Forensic Eye APK..."
    adb install -g forensiceye.apk
else
    echo "Forensic Eye app is already installed. Skipping installation."
fi

echo "Enabling Bluetooth..."
adb shell cmd bluetooth_manager enable

echo "Launching Forensic Eye Auto Run..."
adb shell am start -n com.flxholle.forensiceye/.AutoRunActivity

while true; do
    if adb shell "[ -f /storage/emulated/0/Android/data/com.flxholle.forensiceye/files/finished_auto_run.txt ]"; then
        break
    fi
    sleep 1
done

echo "Auto Run finished. Pulling results to local machine..."
adb pull /storage/emulated/0/Android/data/com.flxholle.forensiceye/files

echo "Done. Results are saved in the 'files' directory."