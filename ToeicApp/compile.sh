#!/bin/bash
# ── TOEIC App 編譯腳本 ────────────────────────────────────────
# 使用前請確認：
#   1. lib/ 目錄已放入 gson-2.10.1.jar
#   2. Java 17+ 已安裝

set -e
mkdir -p out

LIB="lib/gson-2.10.1.jar"
SRC=$(find src -name "*.java" | tr '\n' ' ')

echo "▶ 編譯中..."
javac -encoding UTF-8 -cp "$LIB" -d out $SRC
echo "✓ 編譯完成"
