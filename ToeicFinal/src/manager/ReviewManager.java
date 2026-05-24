package manager;

import model.Vocabulary;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 間隔複習（Spaced Repetition）管理
 * 熟悉度 1~5：答對 → 熟悉度+1，間隔天數加倍；答錯 → 熟悉度-1，明天再複習
 */
public class ReviewManager {

    // 各熟悉度對應的複習間隔天數
    private static final int[] INTERVALS = {0, 1, 2, 4, 7, 14};

    private final List<Vocabulary> vocabList;

    public ReviewManager(List<Vocabulary> vocabList) {
        this.vocabList = vocabList;
    }

    /** 今日待複習單字（nextReviewDate <= 今天） */
    public List<Vocabulary> getTodayReviewWords() {
        return vocabList.stream()
                .filter(Vocabulary::isDueToday)
                .collect(Collectors.toList());
    }

    /** 所有答錯過的單字 */
    public List<Vocabulary> getWrongWords() {
        return vocabList.stream()
                .filter(v -> v.getWrongCount() > 0)
                .sorted(Comparator.comparingInt(Vocabulary::getWrongCount).reversed())
                .collect(Collectors.toList());
    }

    /** 根據答題結果更新單字狀態 */
    public void updateAfterAnswer(Vocabulary vocab, boolean correct) {
        String today = LocalDate.now().toString();
        vocab.setLastReviewDate(today);

        if (correct) {
            vocab.setCorrectCount(vocab.getCorrectCount() + 1);
            int newFam = Math.min(5, vocab.getFamiliarity() + 1);
            vocab.setFamiliarity(newFam);
            int days = INTERVALS[newFam];
            vocab.setNextReviewDate(LocalDate.now().plusDays(days).toString());
        } else {
            vocab.setWrongCount(vocab.getWrongCount() + 1);
            int newFam = Math.max(1, vocab.getFamiliarity() - 1);
            vocab.setFamiliarity(newFam);
            vocab.setNextReviewDate(LocalDate.now().plusDays(1).toString());
        }
    }

    /** 統計平均熟悉度（0~100%） */
    public int getAverageFamiliarityPercent() {
        if (vocabList.isEmpty()) return 0;
        double avg = vocabList.stream()
                .mapToInt(Vocabulary::getFamiliarity)
                .average().orElse(0);
        return (int) Math.round((avg / 5.0) * 100);
    }

    /** 已學單字數（correctCount > 0 or familiarity > 1） */
    public long getLearnedCount() {
        return vocabList.stream()
                .filter(v -> v.getCorrectCount() > 0 || v.getFamiliarity() > 1)
                .count();
    }
}
