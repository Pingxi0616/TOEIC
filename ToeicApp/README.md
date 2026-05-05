# TOEIC 練習系統

## 專案結構
```
ToeicApp/
├── src/
│   ├── main/Main.java              # 程式入口
│   ├── model/Vocabulary.java       # 單字資料模型
│   ├── manager/
│   │   ├── FileManager.java        # JSON 讀寫（Gson）
│   │   ├── QuizManager.java        # 出題邏輯
│   │   └── ReviewManager.java      # 間隔複習 / 錯題
│   ├── controller/
│   │   └── DashboardController.java # 統一資料控制
│   └── ui/
│       ├── AppColors.java          # 色彩與字型常數
│       ├── ToeicApp.java           # 主視窗（JFrame）
│       ├── DashboardPanel.java     # 總覽頁
│       ├── VocabQuizPanel.java     # 單字片語測驗
│       ├── FillBlankPanel.java     # 句子填空測驗
│       ├── CustomQuizPanel.java    # 客製化出題
│       └── VocabManagerPanel.java  # 單字庫管理
├── data/
│   └── vocabulary.json             # 本地單字題庫
├── lib/
│   └── gson-2.10.1.jar             # Gson（手動放入）
├── compile.sh                      # 編譯腳本
├── run.sh                          # 執行腳本
└── README.md
```

## 如何執行

### 1. 下載依賴
- Gson: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
- 放入 `lib/` 資料夾

### 2. 編譯
```bash
chmod +x compile.sh run.sh
./compile.sh
```

### 3. 執行
```bash
./run.sh
```

### Windows 手動指令
```bat
:: 編譯
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out ^
  src\main\Main.java ^
  src\model\Vocabulary.java ^
  src\manager\FileManager.java ^
  src\manager\QuizManager.java ^
  src\manager\ReviewManager.java ^
  src\controller\DashboardController.java ^
  src\ui\AppColors.java ^
  src\ui\ToeicApp.java ^
  src\ui\DashboardPanel.java ^
  src\ui\VocabQuizPanel.java ^
  src\ui\FillBlankPanel.java ^
  src\ui\CustomQuizPanel.java ^
  src\ui\VocabManagerPanel.java

:: 執行
java -cp "out;lib\gson-2.10.1.jar" main.Main
```

## 功能說明

| 功能 | 說明 |
|------|------|
| 總覽 | 統計卡片、今日待複習清單 |
| 單字片語測驗 | 英翻中 / 中翻英 / 片語，四選一，間隔複習 |
| 句子填空 | 挖空例句，附解析，單字/文法導向切換 |
| 客製化出題 | 調整比例、弱點優先、錯題重出設定 |
| 單字庫管理 | 全單字列表、排序、錯題本查看 |

## JSON 單字格式
```json
{
  "word": "purchase",
  "meaning": "購買",
  "pos": "verb",
  "phrase": "purchase order",
  "phraseMeaning": "採購訂單",
  "example": "She purchased a laptop.",
  "wrongCount": 0,
  "correctCount": 0,
  "familiarity": 1,
  "lastReviewDate": "2026-05-03",
  "nextReviewDate": "2026-05-03"
}
```

## 間隔複習演算法
| 熟悉度 | 複習間隔 |
|--------|---------|
| ★☆☆☆☆ (1) | 1 天後 |
| ★★☆☆☆ (2) | 2 天後 |
| ★★★☆☆ (3) | 4 天後 |
| ★★★★☆ (4) | 7 天後 |
| ★★★★★ (5) | 14 天後 |

答對 → 熟悉度+1  
答錯 → 熟悉度-1，明天重考
