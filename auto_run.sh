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

echo "Checking for connected device..."
while ! adb get-state 1>/dev/null 2>&1; do
    echo "No device connected. Please connect your Android device and enable USB Debugging."
    sleep 2
done
echo "Device connected."


if ! adb shell pm list packages | grep -q "com.flxholle.forensiceye"; then
    echo "Downloading latest Forensic Eye APK from GitHub releases..."
    latest_url=$(curl -s https://api.github.com/repos/flxholle/ForensicEye/releases/latest | grep "browser_download_url.*apk" | cut -d '"' -f 4)
    curl -L -o forensiceye.apk "$latest_url"

    echo "Installing Forensic Eye APK..."
    adb install -g forensiceye.apk
else
    echo "Forensic Eye app is already installed. Skipping installation."
    if [[ $(adb shell ls /storage/emulated/0/Android/data/com.flxholle.forensiceye/files 2> /dev/null) ]]; then
        echo "Clearing data directory."
        adb shell rm -r /storage/emulated/0/Android/data/com.flxholle.forensiceye/files
    fi
fi

echo "Enabling Bluetooth..."
adb shell cmd bluetooth_manager enable

echo "Launching Forensic Eye Auto Run..."
adb shell am start -n com.flxholle.forensiceye/.AutoRunActivity

echo "Waiting for Auto Run to finish..."
while true; do
    if [[ $(adb shell ls /storage/emulated/0/Android/data/com.flxholle.forensiceye/files/finished_auto_run.txt 2> /dev/null) ]]; then
        break
    fi
    sleep 1
done

echo "Auto Run finished. Pulling results to local machine..."
adb pull /storage/emulated/0/Android/data/com.flxholle.forensiceye/files ./ForensicEyeData

echo "Done. Results are saved in the 'ForensicEyeData' directory."