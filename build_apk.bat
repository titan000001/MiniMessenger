@echo off
echo Check for JAVA_HOME...
if "%JAVA_HOME%" == "" (
    echo ERROR: JAVA_HOME is not set. Please install Java JDK and set the JAVA_HOME environment variable.
    pause
    exit /b 1
)

echo JAVA_HOME is set to %JAVA_HOME%
echo.
echo Building APK...
call gradle assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build Successful!
echo APK location: app\build\outputs\apk\debug\app-debug.apk
pause
