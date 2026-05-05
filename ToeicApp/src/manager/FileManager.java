package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Vocabulary;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String DATA_PATH = "data/vocabulary.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 從 JSON 載入所有單字 */
    public static List<Vocabulary> loadVocabulary() {
        File file = new File(DATA_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Vocabulary>>() {}.getType();
            List<Vocabulary> list = GSON.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("載入失敗: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 將所有單字存回 JSON */
    public static void saveVocabulary(List<Vocabulary> vocabList) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        try (Writer writer = new FileWriter(DATA_PATH)) {
            GSON.toJson(vocabList, writer);
        } catch (IOException e) {
            System.err.println("儲存失敗: " + e.getMessage());
        }
    }

    /** 匯入純文字 txt（每行格式：word,meaning,pos,example） */
    public static List<Vocabulary> importFromTxt(String txtPath) {
        List<Vocabulary> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(txtPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length >= 2) {
                    Vocabulary v = new Vocabulary(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts.length > 2 ? parts[2].trim() : "",
                        parts.length > 3 ? parts[3].trim() : ""
                    );
                    list.add(v);
                }
            }
        } catch (IOException e) {
            System.err.println("匯入失敗: " + e.getMessage());
        }
        return list;
    }
}
