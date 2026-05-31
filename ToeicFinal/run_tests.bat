@echo off
setlocal
chcp 65001 > nul 2>&1

echo.
echo  ============================================================
echo    TOEIC - JUnit 5 Tests + JaCoCo Coverage
echo  ============================================================
echo.

set JUNIT_JAR=lib\junit-platform-console-standalone-1.10.2.jar
set JACOCO_AGENT=lib\jacocoagent.jar
set JACOCO_CLI=lib\jacococli.jar

if not exist "%JUNIT_JAR%"   goto :missing
if not exist "%JACOCO_AGENT%" goto :missing

if not exist out         mkdir out
if not exist test-out    mkdir test-out
if not exist coverage    mkdir coverage
if not exist coverage\html mkdir coverage\html

:: Step 1: compile core sources (--release 21 for JaCoCo compat)
echo  [1/4] Compiling model / manager / controller ...
javac --release 21 -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out ^
  src\model\Vocabulary.java src\model\VocabCollection.java ^
  src\manager\FileManager.java src\manager\QuizManager.java src\manager\ReviewManager.java ^
  src\controller\DashboardController.java
if errorlevel 1 ( echo  [FAIL] Main compilation failed & goto :end )
echo  [OK]  Core sources compiled  (Java 21 bytecode)
echo.

:: Step 2: compile tests
echo  [2/4] Compiling tests ...
javac --release 21 -encoding UTF-8 -cp "out;lib\gson-2.10.1.jar;%JUNIT_JAR%" -d test-out ^
  test\VocabularyTest.java ^
  test\QuizManagerTest.java ^
  test\FileManagerTest.java ^
  test\ReviewManagerTest.java
if errorlevel 1 ( echo  [FAIL] Test compilation failed & goto :end )
echo  [OK]  Tests compiled
echo.

:: Step 3: run tests with JaCoCo agent (output saved to log)
echo  [3/4] Running tests ...
java "-javaagent:%JACOCO_AGENT%=destfile=coverage\jacoco.exec,includes=manager.*:model.*:controller.*" ^
  -cp "out;lib\gson-2.10.1.jar;%JUNIT_JAR%;test-out" ^
  org.junit.platform.console.ConsoleLauncher ^
  --scan-class-path=test-out ^
  --details=tree ^
  --disable-ansi-colors > coverage\test-results.txt 2>&1

:: Show only the summary lines from the log
echo.
echo  ============================================================
echo    Test Results
echo  ============================================================
findstr /C:"tests found" /C:"tests skipped" /C:"tests started" /C:"tests successful" /C:"tests failed" /C:"tests aborted" coverage\test-results.txt
echo  ============================================================
echo  [LOG] Full output: coverage\test-results.txt
echo.

:: Step 4: generate coverage report
echo  [4/4] Generating coverage report ...
java -jar "%JACOCO_CLI%" report coverage\jacoco.exec ^
  --classfiles out\model --classfiles out\manager --classfiles out\controller ^
  --sourcefiles src\model --sourcefiles src\manager --sourcefiles src\controller ^
  --html coverage\html ^
  --csv coverage\coverage.csv
if errorlevel 1 ( echo  [WARN] Report generation failed & goto :end )

echo  [OK]  HTML: coverage\html\index.html
echo  [OK]  CSV : coverage\coverage.csv
echo.
start "" "coverage\html\index.html"
goto :end

:missing
echo  [ERROR] Test libraries not found.
echo          Run:  powershell -ExecutionPolicy Bypass -File download_test_libs.ps1

:end
echo.
echo  ============================================================
pause
