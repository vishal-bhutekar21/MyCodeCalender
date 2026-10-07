@echo off
setlocal EnableDelayedExpansion

title CodeCalendar - Android Runner (Device / Emulator)

echo ==============================================================================
echo                 CODECALENDAR - ANDROID RUNNER SCRIPT
echo ==============================================================================
echo.

:: -----------------------------------------------------------------------------
:: 1. Resolve Project Root Directory
:: -----------------------------------------------------------------------------
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%.."
set "PROJECT_ROOT=%CD%"

echo [*] Working Directory: %PROJECT_ROOT%

:: -----------------------------------------------------------------------------
:: 2. Locate Android SDK & ADB
:: -----------------------------------------------------------------------------
set "ADB_EXE=adb"
where adb >nul 2>nul
if %ERRORLEVEL% equ 0 goto found_adb

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB_EXE=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    goto found_adb
)
if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set "ADB_EXE=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    goto found_adb
)

echo [!] ERROR: ADB not found in PATH or standard Android SDK directories.
echo [!] Please make sure Android Studio SDK Platform-Tools are installed.
echo.
pause
exit /b 1

:found_adb
echo [*] Using ADB: "%ADB_EXE%"
"%ADB_EXE%" start-server >nul 2>nul

:: -----------------------------------------------------------------------------
:: 3. Detect Target: Physical Device First, Running Emulator Next
:: -----------------------------------------------------------------------------
echo [*] Scanning for connected Android devices and emulators...

set "PHYSICAL_DEVICE="
set "EMULATOR_DEVICE="

for /f "tokens=1,2" %%A in ('"%ADB_EXE%" devices') do (
    if "%%B"=="device" (
        set "DEV_ID=%%A"
        set "PREFIX=!DEV_ID:~0,9!"
        if "!PREFIX!"=="emulator-" (
            if not defined EMULATOR_DEVICE set "EMULATOR_DEVICE=%%A"
        ) else (
            if not defined PHYSICAL_DEVICE set "PHYSICAL_DEVICE=%%A"
        )
    )
)

set "TARGET_DEVICE="
set "TARGET_TYPE="

if defined PHYSICAL_DEVICE (
    set "TARGET_DEVICE=%PHYSICAL_DEVICE%"
    set "TARGET_TYPE=Physical Android Device (USB / Wi-Fi)"
    goto device_selected
)

if defined EMULATOR_DEVICE (
    set "TARGET_DEVICE=%EMULATOR_DEVICE%"
    set "TARGET_TYPE=Active Android Emulator"
    goto device_selected
)

:: -----------------------------------------------------------------------------
:: 4. If No Device Running, Attempt Launching Installed AVD
:: -----------------------------------------------------------------------------
echo [!] No active physical device or running emulator detected.
echo [*] Checking for available Android Virtual Devices (AVDs)...

set "EMULATOR_EXE="
if exist "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" (
    set "EMULATOR_EXE=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"
)
if not defined EMULATOR_EXE if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\emulator\emulator.exe" (
    set "EMULATOR_EXE=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\emulator\emulator.exe"
)

set "FIRST_AVD="
if defined EMULATOR_EXE (
    for /f "tokens=*" %%V in ('"%EMULATOR_EXE%" -list-avds 2^>nul') do (
        if not defined FIRST_AVD set "FIRST_AVD=%%V"
    )
)

if not defined FIRST_AVD goto no_target_found

echo [*] Launching Android Emulator: !FIRST_AVD!...
start "" "!EMULATOR_EXE!" -avd "!FIRST_AVD!"
echo [*] Waiting for emulator to boot up...
"%ADB_EXE%" wait-for-device

:wait_boot_loop
set "BOOT_STATUS=0"
for /f "tokens=*" %%S in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do (
    set "BOOT_STATUS=%%S"
)
if not "!BOOT_STATUS!"=="1" (
    timeout /t 2 /nobreak >nul
    goto wait_boot_loop
)

set "TARGET_DEVICE=emulator-5554"
set "TARGET_TYPE=Started Android Emulator [!FIRST_AVD!]"
echo [OK] Emulator booted successfully.
goto device_selected

:no_target_found
echo.
echo ==============================================================================
echo [!] NO DEVICE CONNECTED AND NO EMULATOR RUNNING
echo ==============================================================================
echo 1. Connect your Android phone via USB cable.
echo 2. Enable "Developer Options" and "USB Debugging" on your phone.
echo 3. Alternatively, open Android Studio and launch/create an Android Virtual Device.
echo ==============================================================================
echo.
pause
exit /b 1

:device_selected
echo.
echo ==============================================================================
echo [TARGET SELECTED]
echo Device Serial : %TARGET_DEVICE%
echo Target Type   : %TARGET_TYPE%
echo ==============================================================================
echo.

:: -----------------------------------------------------------------------------
:: 5. Build Debug APK with Gradle
:: -----------------------------------------------------------------------------
echo [*] Building CodeCalendar Debug APK...
cd /d "%PROJECT_ROOT%"

if not exist "gradlew.bat" (
    echo [!] ERROR: gradlew.bat not found in "%PROJECT_ROOT%".
    pause
    exit /b 1
)

call gradlew.bat :app:assembleDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo [!] Gradle build failed. Check the error log above.
    pause
    exit /b %ERRORLEVEL%
)

set "APK_PATH=%PROJECT_ROOT%\app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK_PATH%" (
    echo [!] ERROR: Built APK not found at "%APK_PATH%".
    pause
    exit /b 1
)

echo [OK] Build successful: "%APK_PATH%"
echo.

:: -----------------------------------------------------------------------------
:: 6. Install APK onto Target Device
:: -----------------------------------------------------------------------------
echo [*] Installing APK to %TARGET_DEVICE% [%TARGET_TYPE%]...
"%ADB_EXE%" -s %TARGET_DEVICE% install -r "%APK_PATH%"
if %ERRORLEVEL% neq 0 (
    echo [!] Installation failed. Trying uninstalling old version first...
    "%ADB_EXE%" -s %TARGET_DEVICE% uninstall com.vishal.mycodecalendar >nul 2>nul
    "%ADB_EXE%" -s %TARGET_DEVICE% install -r "%APK_PATH%"
    if %ERRORLEVEL% neq 0 (
        echo [!] Failed to install APK on device %TARGET_DEVICE%.
        pause
        exit /b %ERRORLEVEL%
    )
)
echo [OK] Installation complete!
echo.

:: -----------------------------------------------------------------------------
:: 7. Launch Main Activity
:: -----------------------------------------------------------------------------
echo [*] Launching CodeCalendar on %TARGET_DEVICE%...
"%ADB_EXE%" -s %TARGET_DEVICE% shell am start -n com.vishal.mycodecalendar/.MainActivity
if %ERRORLEVEL% neq 0 (
    echo [!] Failed to launch MainActivity.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ==============================================================================
echo [SUCCESS] CodeCalendar is now running on your %TARGET_TYPE%!
echo Target Device: %TARGET_DEVICE%
echo Package Name : com.vishal.mycodecalendar
echo ==============================================================================
echo.

:: -----------------------------------------------------------------------------
:: 8. Optional: Stream Logcat
:: -----------------------------------------------------------------------------
echo Press [L] to view live Android Logcat output.
echo Press any other key to exit.
set /p "CHOICE="
if /i "%CHOICE%"=="L" (
    echo.
    echo [*] Streaming Logcat for com.vishal.mycodecalendar (Ctrl+C to stop)...
    "%ADB_EXE%" -s %TARGET_DEVICE% logcat -v time | findstr /i "CodeCalendar Vishal MyCodeCalendar"
)

exit /b 0
