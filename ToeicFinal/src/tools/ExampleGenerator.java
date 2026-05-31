package tools;

import java.io.*;
import java.net.*;
import java.util.List;
import manager.FileManager;
import model.Vocabulary;

/**
 * 批次為 vocabulary.json 中沒有例句的單字生成 TOEIC 風格英文例句。
 * 使用 Groq 免費 API（https://console.groq.com）
 *
 * 使用方式：
 *   1. 到 https://console.groq.com 註冊帳號（免費，不需信用卡）
 *   2. 左側選 "API Keys" → "Create API Key"，複製 key
 *   3. 把下方 API_KEY 換成你的 Groq Key（格式：gsk_...）
 *   4. 雙擊 run_generator.bat 執行
 *   5. 程式每 50 個字存一次進度，中途可按 Ctrl+C 中斷，下次繼續
 */
public class ExampleGenerator {

    // ── 設定區（請修改這裡）────────────────────────────────────
    private static final String API_KEY    = "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"; // 填入你的 Groq API Key
    private static final String API_URL    = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL      = "llama-3.1-8b-instant"; // 免費、速度快
    private static final int    DELAY_MS   = 100;  // Groq 速度快，間隔可短一點
    private static final int    SAVE_EVERY = 50;   // 每幾個成功後自動存檔
    // ────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        if (API_KEY == null || API_KEY.isBlank() || API_KEY.contains("XXXX")) {
            System.err.println("❌ 請先在 ExampleGenerator.java 的 API_KEY 填入你的 OpenAI API Key！");
            return;
        }

        List<Vocabulary> list = FileManager.loadVocabulary();
        long noExample = list.stream()
            .filter(v -> v.getExample() == null || v.getExample().isBlank()).count();

        System.out.println("共 " + list.size() + " 個單字，其中 " + noExample + " 個需要生成例句。");
        System.out.println("開始生成...\n");

        int processed = 0, skipped = 0, failed = 0;

        for (int i = 0; i < list.size(); i++) {
            Vocabulary v = list.get(i);

            // 已有例句 → 跳過
            if (v.getExample() != null && !v.getExample().isBlank()) {
                skipped++;
                continue;
            }

            System.out.printf("[%d/%d] %-20s ", i + 1, list.size(), v.getWord());
            System.out.flush();

            String example = callOpenAI(v);
            if (example != null && !example.isBlank()) {
                v.setExample(example);
                processed++;
                System.out.println("→ " + example);
            } else {
                failed++;
                System.out.println("→ ✗ 失敗，跳過");
            }

            // 定期儲存進度
            if (processed % SAVE_EVERY == 0 && processed > 0) {
                FileManager.saveVocabulary(list);
                System.out.println("\n[已儲存進度：" + processed + " 個完成]\n");
            }

            Thread.sleep(DELAY_MS);
        }

        // 最終儲存
        FileManager.saveVocabulary(list);
        System.out.println("\n══════════════════════════════════");
        System.out.println("完成！成功生成：" + processed + " 個");
        System.out.println("已跳過（有例句）：" + skipped + " 個");
        System.out.println("失敗：" + failed + " 個");
        System.out.println("vocabulary.json 已更新。");
    }

    // ── 呼叫 OpenAI Chat Completion API ─────────────────────────
    private static String callOpenAI(Vocabulary v) {
        try {
            String prompt = buildPrompt(v);
            String body = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":[{\"role\":\"user\",\"content\":" + toJsonString(prompt) + "}],"
                + "\"max_tokens\":80,"
                + "\"temperature\":0.6"
                + "}";

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            if (code == 429) {
                // Rate limit：等久一點再重試
                System.out.print("[rate limit, 等待 5s] ");
                Thread.sleep(5000);
                return callOpenAI(v);
            }
            if (code != 200) {
                System.out.print("[HTTP " + code + "] ");
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            return extractContent(sb.toString());

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            System.out.print("[" + e.getMessage() + "] ");
            return null;
        }
    }

    // ── 組合 Prompt ──────────────────────────────────────────────
    private static String buildPrompt(Vocabulary v) {
        String posStr = (v.getPos() != null && !v.getPos().isBlank())
                        ? " (" + v.getPos() + ")" : "";
        String meaning = v.getMeaning() != null ? v.getMeaning() : "";
        return "Write one TOEIC-style English example sentence for the word \""
            + v.getWord() + "\"" + posStr + " (Chinese meaning: " + meaning + ").\n"
            + "Rules:\n"
            + "1. The word \"" + v.getWord() + "\" must appear exactly once in the sentence.\n"
            + "2. Use professional or business context (TOEIC style).\n"
            + "3. Sentence length: 10 to 20 words.\n"
            + "4. Return ONLY the sentence, no explanation, no quotes.";
    }

    // ── 從 OpenAI JSON 回應中提取 content 欄位 ──────────────────
    private static String extractContent(String json) {
        String marker = "\"content\":";
        int idx = json.indexOf(marker);
        if (idx < 0) return null;
        idx += marker.length();
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return null;
        idx++; // skip opening "

        StringBuilder sb = new StringBuilder();
        while (idx < json.length()) {
            char c = json.charAt(idx);
            if (c == '\\' && idx + 1 < json.length()) {
                char next = json.charAt(idx + 1);
                switch (next) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> {} // ignore \r
                    default   -> sb.append(next);
                }
                idx += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                idx++;
            }
        }

        String result = sb.toString().trim();
        // 去除 GPT 有時多加的外層引號
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 2) {
            result = result.substring(1, result.length() - 1).trim();
        }
        return result.isBlank() ? null : result;
    }

    // ── 將 Java 字串轉為合法 JSON 字串（含引號）───────────────────
    private static String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "")
                        .replace("\t", "\\t") + "\"";
    }
}
