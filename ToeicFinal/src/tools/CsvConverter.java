package tools;

import manager.FileManager;
import model.Vocabulary;

import java.util.List;

/**
 * 一次性工具：將 7000vocs.csv 轉換成 vocabulary.json
 *
 * 編譯（從 ToeicApp/ 目錄執行）：
 *   Windows:
 *     javac -cp lib/gson-2.10.1.jar -d out src/model/Vocabulary.java src/manager/FileManager.java src/tools/CsvConverter.java
 *   Mac/Linux:
 *     javac -cp lib/gson-2.10.1.jar -d out src/model/Vocabulary.java src/manager/FileManager.java src/tools/CsvConverter.java
 *
 * 執行：
 *   Windows:
 *     java -cp "out;lib/gson-2.10.1.jar" tools.CsvConverter data/7000vocs.csv data/vocabulary.json
 *   Mac/Linux:
 *     java -cp "out:lib/gson-2.10.1.jar" tools.CsvConverter data/7000vocs.csv data/vocabulary.json
 */
public class CsvConverter {

    public static void main(String[] args) {
        String csvPath  = args.length > 0 ? args[0] : "data/7000vocs.csv";
        String jsonPath = args.length > 1 ? args[1] : "data/vocabulary.json";

        System.out.println("讀取 CSV：" + csvPath);
        List<Vocabulary> list = FileManager.importFromCsv(csvPath);

        if (list.isEmpty()) {
            System.err.println("錯誤：沒有讀到任何單字，請確認 CSV 路徑與格式。");
            System.exit(1);
        }

        System.out.printf("共讀取 %d 個單字，寫入 %s ...%n", list.size(), jsonPath);
        FileManager.saveVocabulary(list);
        System.out.println("完成！");
    }
}
