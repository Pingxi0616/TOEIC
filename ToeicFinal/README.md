# TOEIC 單字練習系統

以 Java Swing 開發的桌面應用程式，提供單字測驗、句子填空、客製化出題、單字庫管理等功能，並整合間隔複習（Spaced Repetition）機制追蹤學習進度。

---

## 功能列表

- **主頁（Dashboard）**：顯示學習統計、今日複習單字數、熟悉度平均
- **單字片語測驗**：英翻中、中翻英、片語三種模式
- **句子填空測驗**：將例句中的單字替換為空格作答
- **客製化出題**：自訂題型比例（單字題 + 填空題 + 錯題複習）
- **單字庫管理**：新增、編輯、刪除單字，支援 CSV 匯入
- **收藏單字 / 錯題複習 / 學習歷史**
- **單字集（Collection）**：建立自訂主題單字集

---

## 環境需求

- Java 11 或以上版本（建議 17 LTS）
- [Gson 2.10.1](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)（JSON 序列化函式庫）

---

## ⚠️ 首次使用前必讀

### 安裝 Gson 函式庫

本專案使用 [Gson](https://github.com/google/gson) 處理單字資料的 JSON 讀寫，**未隨 Git 提供**，需手動下載：

1. 下載 [gson-2.10.1.jar](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)
2. 在 `ToeicFinal/` 底下建立 `lib` 資料夾（若尚未存在）
3. 將下載的 jar 放入 `lib/` 資料夾

```
ToeicFinal/
└── lib/
    └── gson-2.10.1.jar   ← 放在這裡（手動加入）
```

> 若缺少此檔案，編譯會直接失敗（javac 找不到 com.google.gson 套件）。

---

## 編譯與執行

**Windows（使用提供的 .bat 腳本）：**
```bat
compile_windows.bat
run_windows.bat
```

> 若雙擊 `.bat` 後視窗一閃即逝看不到錯誤訊息（常見原因：找不到 Java 或 Gson），請改用下方「手動編譯」方式，在終端機中執行可保留錯誤訊息以便除錯。

**Windows（VSCode 終端機 / PowerShell 手動編譯）：**
```powershell
# 1. 切到專案資料夾
cd ToeicFinal

# 2. 建立 out 資料夾（存放編譯後的 .class 檔）
mkdir out -ErrorAction SilentlyContinue

# 3. 編譯
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out src\main\Main.java src\model\*.java src\manager\*.java src\controller\*.java src\tools\*.java src\ui\*.java

# 4. 執行
java -cp "out;lib\gson-2.10.1.jar" main.Main
```

**macOS / Linux：**
```bash
bash compile.sh
bash run.sh
```

---

## 專案結構

```
ToeicFinal/
├── src/
│   ├── main/         # 程式進入點
│   ├── controller/   # 業務邏輯控制
│   ├── manager/      # 核心管理（QuizManager, FileManager, ReviewManager）
│   ├── model/        # 資料模型（Vocabulary, VocabCollection）
│   ├── ui/           # 畫面元件
│   └── tools/        # 工具（例句生成器、CSV 轉換）
├── lib/              # 依賴函式庫（Gson，需手動下載，見上方說明）
├── data/             # 資料檔（vocabulary.json、collections.json，隨 repo 提供）
└── test/             # 單元測試
```
