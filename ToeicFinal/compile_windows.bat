@echo off
if not exist out mkdir out
for /r src %%f in (*.java) do set SRC=!SRC! %%f
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out ^
  src\main\Main.java src\model\*.java src\manager\*.java ^
  src\controller\*.java src\tools\*.java src\ui\*.java
echo Done!
