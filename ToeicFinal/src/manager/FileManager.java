package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Vocabulary;
import model.VocabCollection;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {
    private static final String DATA_PATH       = "data/vocabulary.json";
    private static final String COLLECTION_PATH = "data/collections.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Vocabulary ────────────────────────────────────────────
    public static List<Vocabulary> loadVocabulary() {
        return loadFromJson(DATA_PATH, new TypeToken<List<Vocabulary>>(){}.getType());
    }
    public static void saveVocabulary(List<Vocabulary> list) {
        saveToJson(list, DATA_PATH);
    }

    // ── Collections ───────────────────────────────────────────
    public static List<VocabCollection> loadCollections() {
        return loadFromJson(COLLECTION_PATH, new TypeToken<List<VocabCollection>>(){}.getType());
    }
    public static void saveCollections(List<VocabCollection> list) {
        saveToJson(list, COLLECTION_PATH);
    }

    // ── 匯入 CSV（支援 CP950/Big5/UTF-8） ─────────────────────
    public static List<Vocabulary> importFromCsv(String csvPath) {
        List<Vocabulary> list = new ArrayList<>();
        Pattern posP = Pattern.compile("^([a-zA-Z]+\\.[/a-zA-Z.]*)\\s*(.+)");
        String today = LocalDate.now().toString();
        String[] encs = {"CP950","UTF-8","Big5","ISO-8859-1"};
        BufferedReader br = null;
        for (String enc : encs) {
            try { br = new BufferedReader(new InputStreamReader(new FileInputStream(csvPath), enc)); break; }
            catch (Exception ignored) {}
        }
        if (br == null) return list;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().replace("\r","");
                if (line.isEmpty()) continue;
                int c = line.indexOf(','); if (c < 0) continue;
                String word = line.substring(0, c).trim();
                String rest = line.substring(c + 1).trim();
                if (word.isEmpty() || rest.isEmpty()) continue;
                String pos = "", meaning = rest;
                Matcher m = posP.matcher(rest);
                if (m.matches()) { pos = m.group(1).trim().replaceAll("/$",""); meaning = m.group(2).trim(); }
                meaning = meaning.replaceAll("^[a-zA-Z]+\\.\\s*", "");
                Vocabulary v = new Vocabulary();
                v.setWord(word); v.setMeaning(meaning.isEmpty() ? rest : meaning); v.setPos(pos);
                v.setFamiliarity(1); v.setLastReviewDate(today); v.setNextReviewDate(today);
                list.add(v);
            }
            br.close();
        } catch (IOException e) { System.err.println("匯入錯誤: " + e.getMessage()); }
        return list;
    }

    // ── 通用 JSON I/O ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private static <T> T loadFromJson(String path, Type type) {
        File f = new File(path);
        if (!f.exists()) return (T) new ArrayList<>();
        try (Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8")) {
            Object result = GSON.fromJson(r, type);
            return result != null ? (T) result : (T) new ArrayList<>();
        } catch (IOException e) { System.err.println("載入失敗 " + path + ": " + e.getMessage()); return (T) new ArrayList<>(); }
    }
    private static void saveToJson(Object obj, String path) {
        new File(path).getParentFile().mkdirs();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(path), "UTF-8")) {
            GSON.toJson(obj, w);
        } catch (IOException e) { System.err.println("儲存失敗 " + path + ": " + e.getMessage()); }
    }
}
