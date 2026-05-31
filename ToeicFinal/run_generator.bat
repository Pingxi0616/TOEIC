@echo off
chcp 65001
echo === Compiling... ===
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out src\model\*.java src\manager\*.java src\tools\ExampleGenerator.java
if errorlevel 1 (
    echo Compile failed!
    pause
    exit /b 1
)
echo === Running ExampleGenerator ===
java -cp "out;lib\gson-2.10.1.jar" tools.ExampleGenerator
pause
