package manager;

import model.Vocabulary;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuizManager {

    public enum Mode { EN_TO_CN, CN_TO_EN, PHRASE, FILL_BLANK }

    /** 混合出題時每一道題的包裝 */
    public static class QuizItem {
        public final Vocabulary vocab;
        public final Mode mode;
        public QuizItem(Vocabulary vocab, Mode mode) {
            this.vocab = vocab;
            this.mode  = mode;
        }
    }

    private final List<Vocabulary> vocabList;
    private final Random random = new Random();

    public QuizManager(List<Vocabulary> vocabList) {
        this.vocabList = vocabList;
    }

    // ── 單一模式出題清單 ─────────────────────────────────────
    public List<Vocabulary> generateQuizList(int count, boolean prioritizeWrong) {
        List<Vocabulary> pool = new ArrayList<>(vocabList);

        if (prioritizeWrong) {
            pool.sort((a, b) -> {
                int d = b.getWrongCount() - a.getWrongCount();
                return d != 0 ? d : a.getFamiliarity() - b.getFamiliarity();
            });
            int wrongSlots = Math.min(count / 2, pool.size());
            List<Vocabulary> wp = new ArrayList<>(pool.subList(0, wrongSlots));
            List<Vocabulary> rest = new ArrayList<>(pool.subList(wrongSlots, pool.size()));
            Collections.shuffle(rest, random);
            wp.addAll(rest);
            pool = wp;
        } else {
            Collections.shuffle(pool, random);
        }

        int n = Math.min(count, pool.size());
        return new ArrayList<>(pool.subList(0, n)); // 複製避免 subList view 問題
    }

    // ── 客製化混合出題清單 ────────────────────────────────────
    /**
     * @param vocabCount    單字題數量（英翻中/中翻英/片語 各取 1/3）
     * @param fillCount     填空題數量
     * @param wrongCount    錯題複習數量
     * @param prioritizeWeak 是否優先出弱點單字
     */
    public List<QuizItem> generateMixedQuiz(
            int vocabCount, int fillCount, int wrongCount, boolean prioritizeWeak) {

        List<QuizItem> items = new ArrayList<>();

        // ── 單字題 ──
        List<Vocabulary> vocabPool = new ArrayList<>(vocabList);
        if (prioritizeWeak) {
            vocabPool.sort(Comparator.comparingInt(Vocabulary::getFamiliarity)
                    .thenComparingInt(v -> -v.getWrongCount()));
        } else {
            Collections.shuffle(vocabPool, random);
        }
        // 每三題輪流：英翻中、中翻英、片語
        Mode[] vocabModes = {Mode.EN_TO_CN, Mode.CN_TO_EN, Mode.PHRASE};
        for (int i = 0; i < Math.min(vocabCount, vocabPool.size()); i++) {
            items.add(new QuizItem(vocabPool.get(i), vocabModes[i % 3]));
        }

        // ── 填空題（需要有例句）──
        List<Vocabulary> fillPool = vocabList.stream()
                .filter(v -> v.getExample() != null && !v.getExample().isEmpty())
                .collect(Collectors.toList());
        Collections.shuffle(fillPool, random);
        for (int i = 0; i < Math.min(fillCount, fillPool.size()); i++) {
            // 避免與單字題重複
            Vocabulary v = fillPool.get(i);
            items.add(new QuizItem(v, Mode.FILL_BLANK));
        }
        // 若無例句則補充一般英翻中
        if (fillPool.size() < fillCount) {
            List<Vocabulary> fallback = new ArrayList<>(vocabList);
            Collections.shuffle(fallback, random);
            int need = fillCount - fillPool.size();
            for (int i = 0; i < Math.min(need, fallback.size()); i++) {
                items.add(new QuizItem(fallback.get(i), Mode.EN_TO_CN));
            }
        }

        // ── 錯題複習 ──
        List<Vocabulary> wrongPool = vocabList.stream()
                .filter(v -> v.getWrongCount() > 0)
                .sorted(Comparator.comparingInt(Vocabulary::getWrongCount).reversed())
                .collect(Collectors.toList());
        // 錯題不足時補充熟悉度最低的
        if (wrongPool.size() < wrongCount) {
            List<Vocabulary> lowFam = vocabList.stream()
                    .filter(v -> v.getWrongCount() == 0)
                    .sorted(Comparator.comparingInt(Vocabulary::getFamiliarity))
                    .collect(Collectors.toList());
            for (Vocabulary v : lowFam) {
                if (wrongPool.size() >= wrongCount) break;
                if (!wrongPool.contains(v)) wrongPool.add(v);
            }
        }
        for (int i = 0; i < Math.min(wrongCount, wrongPool.size()); i++) {
            items.add(new QuizItem(wrongPool.get(i), Mode.EN_TO_CN));
        }

        // 最終打亂順序
        Collections.shuffle(items, random);
        return items;
    }

    // ── 選項生成 ────────────────────────────────────────────
    public List<String> generateOptions(Vocabulary correct, Mode mode) {
        Set<String> optionSet = new LinkedHashSet<>();
        String answer = getAnswer(correct, mode);
        if (answer.isEmpty()) answer = correct.getMeaning();
        optionSet.add(answer);

        List<Vocabulary> pool = new ArrayList<>(vocabList);
        pool.remove(correct);
        Collections.shuffle(pool, random);

        for (Vocabulary d : pool) {
            if (optionSet.size() >= 4) break;
            String opt = getAnswer(d, mode);
            if (!opt.isEmpty() && !opt.equals(answer)) optionSet.add(opt);
        }

        // 若選項不足 4 個，補充（同模式換 EN_TO_CN）
        if (optionSet.size() < 4) {
            for (Vocabulary d : pool) {
                if (optionSet.size() >= 4) break;
                String opt = d.getMeaning() != null ? d.getMeaning() : "";
                if (!opt.isEmpty() && !opt.equals(answer)) optionSet.add(opt);
            }
        }

        List<String> result = new ArrayList<>(optionSet);
        Collections.shuffle(result, random);
        return result;
    }

    public String getAnswer(Vocabulary v, Mode mode) {
        return switch (mode) {
            case EN_TO_CN   -> v.getMeaning() != null ? v.getMeaning() : "";
            case CN_TO_EN   -> v.getWord() != null ? v.getWord() : "";
            case PHRASE     -> (v.getPhrase() != null && !v.getPhrase().isEmpty())
                               ? v.getPhrase() : (v.getWord() != null ? v.getWord() : "");
            case FILL_BLANK -> v.getWord() != null ? v.getWord() : "";
        };
    }

    public String getQuestion(Vocabulary v, Mode mode) {
        return switch (mode) {
            case EN_TO_CN   -> v.getWord() != null ? v.getWord() : "";
            case CN_TO_EN   -> v.getMeaning() != null ? v.getMeaning() : "";
            case PHRASE     -> (v.getPhraseMeaning() != null && !v.getPhraseMeaning().isEmpty())
                               ? v.getPhraseMeaning() : (v.getMeaning() != null ? v.getMeaning() : "");
            case FILL_BLANK -> buildFillBlank(v);
        };
    }

    private String buildFillBlank(Vocabulary v) {
        if (v.getExample() == null || v.getExample().isEmpty()) {
            int len = v.getWord() != null ? v.getWord().length() : 6;
            return "_".repeat(len) + "  （提示：" + (v.getMeaning() != null ? v.getMeaning() : "") + "）";
        }
        String blank = "_".repeat(v.getWord().length());
        return v.getExample().replaceAll("(?i)\\b" + Pattern.quote(v.getWord()) + "\\b", blank);
    }

    public List<Vocabulary> getWordsWithExample() {
        return vocabList.stream()
                .filter(v -> v.getExample() != null && !v.getExample().isEmpty())
                .collect(Collectors.toList());
    }

    // 取得 mode 的顯示名稱
    public static String modeLabel(Mode mode) {
        return switch (mode) {
            case EN_TO_CN   -> "英翻中";
            case CN_TO_EN   -> "中翻英";
            case PHRASE     -> "片語";
            case FILL_BLANK -> "填空";
        };
    }
}
