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

```
compile_windows.bat
run_windows.bat
```

> 若雙擊 `.bat` 後視窗一閃即逝看不到錯誤訊息（常見原因：找不到 Java 或 Gson），請改用下方「手動編譯」方式，在終端機中執行可保留錯誤訊息以便除錯。

**Windows（VSCode 終端機 / PowerShell 手動編譯）：**

```
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

```
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

---

## ⚠️ data/vocabulary.json 多人協作問題

### 目前狀況

`ToeicFinal/data/` 目前只有兩個檔案，**都隨 Git 追蹤、會一起 push 上 repo**：

```
ToeicFinal/data/
├── collections.json
└── vocabulary.json
```

`vocabulary.json` 同時存放了兩種性質不同的資料：

| 類型 | 欄位範例 | 是否該共享 |
|---|---|---|
| 單字本身 | word、meaning、pos、example | 應該共享 |
| 個人學習紀錄 | wrongCount、correctCount、familiarity、lastReviewDate、nextReviewDate | 不該共享 |

因為兩者混在同一個檔案，目前只要任何人在本機**練習測驗**（不是新增單字），`vocabulary.json` 的內容就會被改動。一旦這個改動被 commit / push 上去，會出現以下狀況：

- 組員 `git pull` 時，自己的學習紀錄被別人覆蓋
- 雙方都改過時直接 merge 衝突（例如先前遇到的 `CONFLICT (modify/delete)` 或 `Unable to pull when changes are present on your branch` 錯誤）
- 即使只是新增了幾個單字，也會連帶把自己的答題紀錄一起推給所有人

### 建議解決方案：拆分檔案（尚未實作）

> 以下為**建議的修改方向**，目前程式碼與 repo 中尚未實際拆分，仍是單一 `vocabulary.json`。

將 `vocabulary.json` 拆成兩個檔案，各自負責不同職責：

```
ToeicFinal/data/
├── words.json       # 單字基本資料 → 加入 Git 版本控制，供所有人共用
├── progress.json    # 個人學習紀錄 → 加入 .gitignore，僅留在本機
└── collections.json
```

- **`words.json`**：只存放單字本身（word / meaning / pos / phrase / example）。任何人新增單字後，正常 `git add` → `commit` → `push`，組員 `pull` 下來就能拿到新單字。
- **`progress.json`**：只存放個人學習紀錄（wrongCount / correctCount / familiarity / lastReviewDate / nextReviewDate / favorite）。加入 `.gitignore`，每個人各自保存在本機，答題不會互相覆蓋。

程式啟動時，由 `FileManager` 依照 `word` 欄位把兩份資料合併成記憶體中的 `Vocabulary` 清單；存檔時則拆分寫回兩個檔案。

**需要調整的部分：**

1. `FileManager` 新增 `loadWords()`、`loadProgress()`、`mergeData()`、`saveWords()`、`saveProgress()`
2. `.gitignore` 加入一行：`data/progress.json`
3. 新增單字流程只呼叫 `saveWords()`，可直接 commit 分享
4. 答題流程只呼叫 `saveProgress()`，不會異動 `words.json`，因此不會產生衝突

**效益：** 新增單字可正常透過 Git 共用，個人學習紀錄完全留在本機、互不干擾，不再出現 `vocabulary.json` 的 merge 衝突。
