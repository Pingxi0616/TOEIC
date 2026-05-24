#!/bin/bash
mkdir -p out
SRC=$(find src -name "*.java" | tr '\n' ' ')
echo "▶ 編譯中..."
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar" -d out $SRC
echo "✓ 完成"
