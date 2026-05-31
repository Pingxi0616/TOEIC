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

        // ── 填空題（所有單字均可，自動生成例句模板）──
        List<Vocabulary> fillPool = new ArrayList<>(vocabList);
        if (prioritizeWeak) {
            fillPool.sort(Comparator.comparingInt(Vocabulary::getFamiliarity)
                    .thenComparingInt(vv -> -vv.getWrongCount()));
        } else {
            Collections.shuffle(fillPool, random);
        }
        for (int i = 0; i < Math.min(fillCount, fillPool.size()); i++) {
            items.add(new QuizItem(fillPool.get(i), Mode.FILL_BLANK));
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

    // ── 填空例句模板（依詞性分組）───────────────────────────
    private static final String[] VERB_TEMPLATES = {
        "The manager decided to _____ the proposal without further delay.",
        "She was required to _____ the report before the board meeting.",
        "The team worked hard to _____ their quarterly targets on time.",
        "It is recommended to _____ the document carefully before submitting.",
        "The company chose to _____ its overseas operations next fiscal year.",
        "He needs to _____ the agreement terms with the legal department.",
        "They were instructed to _____ the process as quickly as possible.",
        "The director asked all staff members to _____ the new procedures.",
    };
    private static final String[] NOUN_TEMPLATES = {
        "The _____ of the project was presented at the annual conference.",
        "Strong _____ is the foundation of any successful organization.",
        "A clear _____ helps employees understand their responsibilities.",
        "The board approved additional budget for _____ next quarter.",
        "Without proper _____, productivity in the office will decline.",
        "The report analyzed the _____ of the new marketing strategy.",
        "Good _____ between departments improves overall performance.",
    };
    private static final String[] ADJ_TEMPLATES = {
        "The new proposal was _____ and addressed all major concerns.",
        "It is _____ for all staff members to attend the safety training.",
        "She delivered a _____ report that fully satisfied the committee.",
        "The client was _____ with the quality of service provided.",
        "A _____ understanding of the contract terms is necessary.",
        "The quarterly results were _____ and exceeded the original forecast.",
    };
    private static final String[] ADV_TEMPLATES = {
        "The team completed the project _____ before the deadline.",
        "The director _____ reviewed all submitted applications.",
        "She answered the interviewer's questions _____ and confidently.",
        "The office manager _____ updated all employee records.",
        "The proposal was _____ accepted by the board of directors.",
    };
    private static final String[] PREP_TEMPLATES = {
        "She walked _____ the park on her way to the office.",
        "The files were stored _____ the cabinet for easy access.",
        "He traveled _____ several cities to attend the annual summit.",
        "The package was shipped _____ the country within two days.",
        "Please place the signed documents _____ the desk before leaving.",
        "The branch office is located _____ the main building.",
        "The team gathered _____ the conference room for the briefing.",
    };
    private static final String[] CONJ_TEMPLATES = {
        "She studied hard, _____ she still found the final exam challenging.",
        "He prepared thoroughly, _____ the results were not as expected.",
        "The project was finished on time, _____ the budget was exceeded.",
        "The client approved the design, _____ requested several minor changes.",
    };

    private String buildFillBlank(Vocabulary v) {
        String word    = v.getWord() != null ? v.getWord() : "";
        String pos     = v.getPos() != null ? v.getPos().toLowerCase() : "";
        String example = v.getExample();

        // 若有真實例句且能找到單字，直接把單字替換為 _____
        if (example != null && !example.isBlank()) {
            String blanked = example.replaceAll(
                    "(?i)\\b" + Pattern.quote(word) + "\\b", "_____");
            if (!blanked.equals(example)) return blanked;
            blanked = example.replace(word, "_____");
            if (!blanked.equals(example)) return blanked;
        }

        // 依詞性選擇模板：用 contains 支援複合詞性（如 "prep./adv."、"n./v."）
        String[] pool;
        if      (pos.contains("prep"))                                          pool = PREP_TEMPLATES;
        else if (pos.contains("conj"))                                          pool = CONJ_TEMPLATES;
        else if (pos.contains("adv"))                                           pool = ADV_TEMPLATES;
        else if (pos.contains("adj"))                                           pool = ADJ_TEMPLATES;
        else if (pos.contains("v"))                                             pool = VERB_TEMPLATES;
        else if (pos.contains("n"))                                             pool = NOUN_TEMPLATES;
        else                                                                    pool = NOUN_TEMPLATES;

        return pool[Math.abs(word.hashCode()) % pool.length];
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
