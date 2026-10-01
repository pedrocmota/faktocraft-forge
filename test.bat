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
rem        test.bat -fixture build   -> build the legacy save fixture (world +
rem                                   manifest) into docs\fixtures\legacy-1201
rem        test.bat -fixture verify  -> seed the test world from that fixture
rem                                   and run the save parity checks
setlocal enabledelayedexpansion
cd /d "%~dp0"
set "GRADLEW=%~dp0gradlew.bat"
set "LOG=%~dp0build\gametest.log"

rem The single quotes ride along into the powershell command below, where a
rem bare comma would otherwise split the argument.
set "FILTER="
set "PROFILE=default"
set "FIXTURE="
set "FIXDIR=%~dp0docs\fixtures\legacy-1201"
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
if /i "%~1"=="-fixture" (
  set "FIXTURE=%~2"
  set "FILTER='-Ptests=legacy'"
  shift
  shift
  goto options
)
set "PROFILE_ARG=-Pprofile=%PROFILE%"
echo [Gametest] Mod profile: %PROFILE%
set "FIXTURE_ARG="
if not "%FIXTURE%"=="" (
  set "FIXTURE_ARG=-PlegacyFixture=%FIXTURE%"
  echo [Gametest] Legacy fixture mode: %FIXTURE%
)
if /i "%FIXTURE%"=="verify" if not exist "%FIXDIR%\world\level.dat" (
  echo No fixture world at %FIXDIR%\world - run "test.bat -fixture build" on the 1.20.1 branch first.
  exit /b 1
)

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
  if exist "%GTDIR%\legacy-fixture" rmdir /s /q "%GTDIR%\legacy-fixture"
  if /i "%FIXTURE%"=="verify" (
    echo [Gametest] Seeding the test world from %FIXDIR%
    xcopy /e /i /q "%FIXDIR%\world" "%GTDIR%\world" >nul
    xcopy /e /i /q "%FIXDIR%\legacy-fixture" "%GTDIR%\legacy-fixture" >nul
  )
  echo [Gametest] Booting the test server, this can take a minute...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$booting = $true; & '%GRADLEW%' runGameTestServer %AUDIT% %FILTER% %PROFILE_ARG% %FIXTURE_ARG% --console=plain 2>&1 | Tee-Object -FilePath '%LOG%' | Select-String -SimpleMatch -Pattern '[Gametest]','LogTestReporter','required tests','energyAudit','Running test batch','LegacyFixture','LegacySave' | ForEach-Object { if ($booting) { $booting = $false; Write-Host '[Gametest] test server ready - running the suite' }; $_.Line -replace '^\[[\d:]+\] \[[^\]]+\] \[[^\]]+\]: ', '' }; exit $LASTEXITCODE"
  if not "!ERRORLEVEL!"=="0" goto failed
  if /i "%FIXTURE%"=="build" (
    echo [Gametest] Saving the fixture to %FIXDIR%
    if exist "%FIXDIR%" rmdir /s /q "%FIXDIR%"
    mkdir "%FIXDIR%"
    xcopy /e /i /q "%GTDIR%\world" "%FIXDIR%\world" >nul
    xcopy /e /i /q "%GTDIR%\legacy-fixture" "%FIXDIR%\legacy-fixture" >nul
    if exist "%FIXDIR%\world\session.lock" del /q "%FIXDIR%\world\session.lock"
    if exist "%FIXDIR%\world\level.dat_old" del /q "%FIXDIR%\world\level.dat_old"
  )
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
