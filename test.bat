@echo off
rem Runs the gametest suite, printing each test as it finishes instead of
rem going quiet until the verdict. The full gradle output goes to
rem build\gametest.log.
rem Usage: test.bat                -> one full run
rem        test.bat 5              -> up to 5 runs, stops at the first failure
rem                                   (a few tests only fail in certain
rem                                    layouts, so repeating catches them)
rem        test.bat 5 audit        -> same, with the energy network invariant
rem                                   audit on (-PenergyAudit=true)
rem        test.bat -only pump     -> only the tests whose name contains
rem                                   "pump" (several: -only "a,b" - the
rem                                   quotes matter, cmd splits on commas)
rem        test.bat -profile alternative -> run with another mod profile
rem                                   (default, alternative, production)
setlocal enabledelayedexpansion
cd /d "%~dp0"
set "GRADLEW=%~dp0gradlew.bat"
set "LOG=%~dp0build\gametest.log"

rem The single quotes ride along into the powershell command below, where a
rem bare comma would otherwise split the argument.
set "FILTER="
set "PROFILE=default"
:options
if /i "%~1"=="-only" (
  set "FILTER='-Ptests=%~2'"
  shift
  shift
  goto options
)
if /i "%~1"=="-profile" (
  set "PROFILE=%~2"
  shift
  shift
  goto options
)
set "PROFILE_ARG=-Pprofile=%PROFILE%"
echo [Gametest] Mod profile: %PROFILE%

set RUNS=%~1
if "%RUNS%"=="" set RUNS=1
rem Anything non-numeric here is a usage mistake (an unquoted comma filter
rem splits into extra args); run once rather than zero times.
echo %RUNS%| findstr /r /c:"^[0-9][0-9]*$" >nul || set RUNS=1

set "AUDIT="
if /i "%2"=="audit" set "AUDIT=-PenergyAudit=true"

if not exist "%~dp0build" mkdir "%~dp0build"

rem The test server gets its own folder with a flat world that is thrown away
rem before every run. Sharing run\world (a normal world, sea level above the
rem test platforms) let terrain water leak into structures depending on the
rem batch layout, which is where the "only fails in some layouts" tests came
rem from.
set "GTDIR=%~dp0run-gametest"
if not exist "%GTDIR%" mkdir "%GTDIR%"
if not exist "%GTDIR%\eula.txt" echo eula=true> "%GTDIR%\eula.txt"
if not exist "%GTDIR%\server.properties" (
  echo level-type=minecraft\:flat> "%GTDIR%\server.properties"
  echo online-mode=false>> "%GTDIR%\server.properties"
  echo spawn-protection=0>> "%GTDIR%\server.properties"
  echo sync-chunk-writes=false>> "%GTDIR%\server.properties"
)

for /L %%i in (1,1,%RUNS%) do (
  if not "%RUNS%"=="1" echo === Run %%i of %RUNS%
  rem Tee-Object keeps the whole log while the filter prints the interesting
  rem lines as they arrive; cmd alone cannot do both.
  if exist "%GTDIR%\world" rmdir /s /q "%GTDIR%\world"
  echo [Gametest] Booting the test server, this can take a minute...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$booting = $true; & '%GRADLEW%' runGameTestServer %AUDIT% %FILTER% %PROFILE_ARG% --console=plain 2>&1 | Tee-Object -FilePath '%LOG%' | Select-String -SimpleMatch -Pattern '[Gametest]','LogTestReporter','required tests','energyAudit','Running test batch' | ForEach-Object { if ($booting) { $booting = $false; Write-Host '[Gametest] test server ready - running the suite' }; $_.Line -replace '^\[[\d:]+\] \[[^\]]+\] \[[^\]]+\]: ', '' }; exit $LASTEXITCODE"
  if not "!ERRORLEVEL!"=="0" goto failed
)

echo.
echo TESTS PASSED
exit /b 0

:failed
rem Tee-Object writes the log as UTF-16, which findstr cannot read - ask
rem PowerShell whether any test ever reported in.
powershell -NoProfile -Command "if (Select-String -Path '%LOG%' -SimpleMatch 'required tests' -Quiet) { exit 0 } else { exit 1 }"
if errorlevel 1 (
  echo.
  echo No test ever ran - the build itself failed. Last lines of the log:
  powershell -NoProfile -Command "Get-Content '%LOG%' -Tail 25"
)
echo.
echo TESTS FAILED  ^(full log: build\gametest.log^)
exit /b 1
