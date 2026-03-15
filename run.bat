@echo off
set "BASE_DIR=%~dp0"
set "JAR_PATH=%BASE_DIR%target\HomeBudgetTracker.jar"
set "CLASSES_PATH=%BASE_DIR%target\classes"

echo Starting Home Budget Tracker (with fixes)...
java --enable-native-access=ALL-UNNAMED -cp "%CLASSES_PATH%;%JAR_PATH%" com.mybudg.Main
pause
