@echo off
echo ========================================
echo Building Game Application Installer (MSI)
echo ========================================

REM Configuration
SET JAVAFX_SDK=C:\Program Files\Java\javafx-sdk-23.0.2\lib
SET JAVAFX_JMODS=C:\Program Files\Java\javafx-jmods-21.0.8
SET OUTPUT_DIR=build
SET JAR_NAME=GameApplication.jar
SET APP_NAME=Game Application
SET APP_VERSION=1.0.0
SET VENDOR=Your Name

REM Clean previous build
echo.
echo [1/5] Cleaning previous build...
if exist %OUTPUT_DIR% rmdir /s /q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%\classes
mkdir %OUTPUT_DIR%\installer

REM Compile
echo.
echo [2/5] Compiling Java source files...
javac --module-path "%JAVAFX_SDK%" ^
      --add-modules javafx.controls,javafx.media ^
      -d %OUTPUT_DIR%\classes ^
      src/*.java

if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

REM Copy resources
echo.
echo [3/5] Copying resources...
if exist src\images xcopy /E /I /Q src\images %OUTPUT_DIR%\classes\images
if exist src\sounds xcopy /E /I /Q src\sounds %OUTPUT_DIR%\classes\sounds
if exist src\videos xcopy /E /I /Q src\videos %OUTPUT_DIR%\classes\videos
if exist src\*.csv copy /Y src\*.csv %OUTPUT_DIR%\classes\

REM Create JAR
echo.
echo [4/5] Creating JAR file...
jar --create ^
    --file %OUTPUT_DIR%\%JAR_NAME% ^
    --main-class GameApplication ^
    -C %OUTPUT_DIR%\classes .

if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

REM Create MSI installer (no WiX needed)
echo.
echo [5/5] Creating MSI installer...
echo This may take a few minutes...
echo Using jmods from: %JAVAFX_JMODS%

jpackage --input %OUTPUT_DIR% ^
         --name "%APP_NAME%" ^
         --main-jar %JAR_NAME% ^
         --main-class GameApplication ^
         --type msi ^
         --win-shortcut ^
         --win-menu ^
         --win-dir-chooser ^
         --module-path "%JAVAFX_JMODS%" ^
         --add-modules javafx.controls,javafx.media,javafx.graphics,javafx.base ^
         --dest %OUTPUT_DIR%\installer ^
         --app-version %APP_VERSION% ^
         --vendor "%VENDOR%" ^
         --description "An educational game application" ^
         --copyright "Copyright 2025" ^
         --java-options "--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED" ^
         --verbose

if errorlevel 1 (
    echo.
    echo ERROR: Installer creation failed!
    echo.
    echo The error details should be shown above.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS!
echo ========================================
echo.
echo Installer created at: %OUTPUT_DIR%\installer\
echo.
dir %OUTPUT_DIR%\installer\
echo.
pause