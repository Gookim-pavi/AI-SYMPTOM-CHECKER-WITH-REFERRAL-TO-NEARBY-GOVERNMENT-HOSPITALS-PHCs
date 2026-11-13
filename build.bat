@echo off
setlocal
echo =========================================================================
echo    SSS MEDICAL ASSIST - COMPILING JAVA 25 SOURCE CODE
echo =========================================================================

set JAVAC_CMD="C:\Users\sagec\AppData\Roaming\squidservers\java\jdk-25\bin\javac.exe"
if not exist %JAVAC_CMD% set JAVAC_CMD=javac

if not exist "bin" mkdir "bin"

echo Compiling all Java classes to bin/...
%JAVAC_CMD% -d bin -sourcepath src src\com\sanjeevani\models\*.java src\com\sanjeevani\engine\*.java src\com\sanjeevani\data\*.java src\com\sanjeevani\server\*.java src\com\sanjeevani\App.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed with error code %ERRORLEVEL%!
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Compilation complete! All .class files written to bin/
echo.
pause
