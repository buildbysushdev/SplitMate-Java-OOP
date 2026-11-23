@echo off
REM Double-click or run in the SplitMate folder. Deletes old compiled files first, then compiles and starts SplitMate.
del /s /q *.class >nul 2>&1
javac Main.java model\*.java service\*.java ui\*.java
if errorlevel 1 (
    echo Compile failed - see the errors above.
    pause
    exit /b 1
)
java Main
