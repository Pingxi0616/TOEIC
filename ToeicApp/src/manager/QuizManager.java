package manager;

import model.Vocabulary;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 出題邏輯管理
 * 模式：EN_TO_CN（英翻中）、CN_TO_EN（中翻英）、PHRASE（片語）、FILL_BLANK（填空）
 */
public class QuizManager {

    public enum Mode { EN_TO_CN, CN_TO_EN, PHRASE, FILL_BLANK }

    private final List<Vocabulary> vocabList;
    private final Random random = new Random();

    public QuizManager(List<Vocabulary> vocabList) {
        this.vocabList = vocabList;
    }

    /**
     * 取得出題清單（共 count 題）
     * @param prioritizeWrong 是否優先出錯題
     */
    public List<Vocabulary> generateQuizList(int count, boolean prioritizeWrong) {
        List<Vocabulary> pool = new ArrayList<>(vocabList);

        if (prioritizeWrong) {
            // 依 wrongCount 排序，錯越多越先出
            pool.sort((a, b) -> {
                int wrongDiff = b.getWrongCount() - a.getWrongCount();
                if (wrongDiff != 0) return wrongDiff;
                return a.getFamiliarity() - b.getFamiliarity(); // 熟悉度低的優先
            });
            // 前 1/3 保留錯題，後面隨機
            int wrongSlots = Math.min(count / 2, pool.size());
            List<Vocabulary> wrongPart = new ArrayList<>(pool.subList(0, wrongSlots));
            List<Vocabulary> rest = new ArrayList<>(pool.subList(wrongSlots, pool.size()));
            Collections.shuffle(rest);
            wrongPart.addAll(rest);
            pool = wrongPart;
        } else {
            Collections.shuffle(pool);
        }

        return pool.subList(0, Math.min(count, pool.size()));
    }

    /**
     * 產生四個選項（1個正確 + 3個干擾項）
     * @param correct  正確答案單字
     * @param mode     出題模式
     * @return 打亂後的選項清單（String）
     */
    public List<String> generateOptions(Vocabulary correct, Mode mode) {
        Set<String> optionSet = new LinkedHashSet<>();
        String answer = getAnswer(correct, mode);
        optionSet.add(answer);

        List<Vocabulary> distractors = new ArrayList<>(vocabList);
        distractors.remove(correct);
        Collections.shuffle(distractors);

        for (Vocabulary d : distractors) {
            if (optionSet.size() >= 4) break;
            String opt = getAnswer(d, mode);
            if (!opt.equals(answer) && !opt.isEmpty()) {
                optionSet.add(opt);
            }
        }

        List<String> result = new ArrayList<>(optionSet);
        Collections.shuffle(result);
        return result;
    }

    /** 取得正確答案字串（依模式） */
    public String getAnswer(Vocabulary v, Mode mode) {
        return switch (mode) {
            case EN_TO_CN  -> v.getMeaning() != null ? v.getMeaning() : "";
            case CN_TO_EN  -> v.getWord() != null ? v.getWord() : "";
            case PHRASE    -> v.getPhrase() != null ? v.getPhrase() : v.getWord();
            case FILL_BLANK -> v.getWord() != null ? v.getWord() : "";
        };
    }

    /** 取得題目顯示文字（依模式） */
    public String getQuestion(Vocabulary v, Mode mode) {
        return switch (mode) {
            case EN_TO_CN  -> v.getWord();
            case CN_TO_EN  -> v.getMeaning();
            case PHRASE    -> v.getPhraseMeaning() != null ? v.getPhraseMeaning() : v.getMeaning();
            case FILL_BLANK -> buildFillBlank(v);
        };
    }

    /** 產生填空句子（將單字替換為底線） */
    private String buildFillBlank(Vocabulary v) {
        if (v.getExample() == null || v.getExample().isEmpty()) {
            return "________ ：" + v.getMeaning();
        }
        String blank = "_".repeat(v.getWord().length());
        return v.getExample().replaceAll("(?i)\\b" + v.getWord() + "\\b", blank);
    }

    /** 取得有例句的單字（供填空使用） */
    public List<Vocabulary> getWordsWithExample() {
        return vocabList.stream()
                .filter(v -> v.getExample() != null && !v.getExample().isEmpty())
                .collect(Collectors.toList());
    }
}
