@echo off
echo ====================================
echo  RoomCraft Designer 2.0
echo  PUSL3122 Group Project
echo ====================================
echo.

:: Clean bin
if exist bin rmdir /s /q bin
mkdir bin

:: Compile
echo [1/2] Compiling...
javac -encoding UTF-8 -d bin -sourcepath src src/RoomCraftApp.java
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed! See errors above.
    pause
    exit /b 1
)
echo [1/2] Compilation successful!
echo.

:: Run
echo [2/2] Launching RoomCraft Designer 2.0...
java -cp bin RoomCraftApp

pause
