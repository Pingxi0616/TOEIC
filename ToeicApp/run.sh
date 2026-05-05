#!/bin/bash
# ── TOEIC App 執行腳本 ────────────────────────────────────────
LIB="lib/gson-2.10.1.jar"

echo "▶ 啟動 TOEIC 練習系統..."
java -cp "out:$LIB" main.Main
