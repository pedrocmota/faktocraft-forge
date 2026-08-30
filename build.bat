@echo off
rem Builds the distributable mod jar (reobfuscated) into build\libs.
rem Usage: build.bat          -> full build (compile + tests hooks + jar)
rem        build.bat quick    -> jar only, skips checks (faster iteration)
rem        build.bat clean    -> clean + full build
setlocal
cd /d "%~dp0"
set "GRADLEW=%~dp0gradlew.bat"

set MODE=%1
if "%MODE%"=="clean" (
  call "%GRADLEW%" clean build --console=plain
) else if "%MODE%"=="quick" (
  call "%GRADLEW%" build -x test -x check --console=plain
) else (
  call "%GRADLEW%" build --console=plain
)
if errorlevel 1 (
  echo.
  echo BUILD FAILED
  exit /b 1
)

echo.
echo Jar generated in build\libs:
dir /b "%~dp0build\libs\*.jar"
