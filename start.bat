@echo off
rem Starts the Faktocraft dedicated server and dev client with a single command.
rem Usage: start.bat                     -> server + client
rem        start.bat client              -> client only (singleplayer already has a built-in server)
rem        start.bat server              -> dedicated server only
rem        start.bat client alternative  -> second argument picks the mod profile
rem                                         (default, alternative, production; see build.gradle)
setlocal
cd /d "%~dp0"
set "GRADLEW=%~dp0gradlew.bat"

set MODE=%1
if "%MODE%"=="" set MODE=both
set PROFILE=%2
if "%PROFILE%"=="" set PROFILE=default
set "PROFILE_ARG=-Pprofile=%PROFILE%"
echo Mod profile: %PROFILE%

if "%MODE%"=="client" (
  call "%GRADLEW%" runClient %PROFILE_ARG% --console=plain
  exit /b %ERRORLEVEL%
)
if "%MODE%"=="server" (
  call "%GRADLEW%" runServer %PROFILE_ARG% --console=plain
  exit /b %ERRORLEVEL%
)

echo [1/3] Building once (keeps both runs from fighting over the same tasks)...
call "%GRADLEW%" compileJava processResources %PROFILE_ARG% --console=plain
if errorlevel 1 exit /b 1

echo [2/3] Starting the dedicated server in a separate window...
if exist "%~dp0run\server-console.log" del "%~dp0run\server-console.log"
start "Faktocraft Server" cmd /c ""%GRADLEW%" runServer %PROFILE_ARG% --console=plain > "%~dp0run\server-console.log" 2>&1"

:wait
ping -n 3 127.0.0.1 >nul
findstr /c:"For help, type" "%~dp0run\server-console.log" >nul 2>&1 && goto ready
findstr /c:"BUILD FAILED" "%~dp0run\server-console.log" >nul 2>&1 && goto failed
findstr /c:"Failed to start the minecraft server" "%~dp0run\server-console.log" >nul 2>&1 && goto failed
goto wait

:failed
echo.
echo The server failed to start. Last lines of run\server-console.log:
powershell -NoProfile -Command "Get-Content '%~dp0run\server-console.log' -Tail 20"
exit /b 1

:ready
echo     Server ready: localhost:25565  (Multiplayer ^> Direct Connection)
echo [3/3] Opening the client...
call "%GRADLEW%" runClient %PROFILE_ARG% --console=plain

echo.
echo Client closed. The server is still running in the "Faktocraft Server" window.
echo To shut it down safely, type  stop  in that window.
