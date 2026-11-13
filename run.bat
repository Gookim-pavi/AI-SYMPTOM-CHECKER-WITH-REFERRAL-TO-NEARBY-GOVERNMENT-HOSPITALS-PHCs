@echo off
setlocal
echo =========================================================================
echo    SSS MEDICAL ASSIST - SMART HEALTHCARE TRIAGE & GOVT PHC REFERRAL
echo =========================================================================

set JAVA_CMD="C:\Users\sagec\AppData\Roaming\squidservers\java\jdk-25\bin\java.exe"
if not exist %JAVA_CMD% set JAVA_CMD=java

if not exist "bin\com\sanjeevani\App.class" (
    echo [INFO] Binaries not found. Building project first...
    call build.bat
)

echo Starting SSS Medical Assist Backend Server on Port 8080...
%JAVA_CMD% -cp bin com.sanjeevani.App 8080

pause
