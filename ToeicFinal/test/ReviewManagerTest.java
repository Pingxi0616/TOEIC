import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import manager.ReviewManager;
import model.Vocabulary;
import java.time.LocalDate;
import java.util.*;

class ReviewManagerTest {

    // ── updateAfterAnswer – correct ───────────────────────────

    @Test
    void correctAnswer_incrementsCorrectCountAndFamiliarity() {
        Vocabulary v = makeVocab(1, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, true);
        assertEquals(1, v.getCorrectCount());
        assertEquals(2, v.getFamiliarity());
    }

    @Test
    void correctAnswer_setsNextReviewDateByInterval() {
        // fam 1 → correct → newFam 2 → interval INTERVALS[2] = 2 days
        Vocabulary v = makeVocab(1, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, true);
        String expected = LocalDate.now().plusDays(2).toString();
        assertEquals(expected, v.getNextReviewDate());
    }

    @Test
    void correctAnswer_atMaxFamiliarity_staysAt5() {
        Vocabulary v = makeVocab(5, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, true);
        assertEquals(5, v.getFamiliarity());
    }

    @Test
    void correctAnswer_atMaxFamiliarity_intervalIs14Days() {
        // fam 5 → correct → stays at 5 → INTERVALS[5] = 14
        Vocabulary v = makeVocab(5, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, true);
        String expected = LocalDate.now().plusDays(14).toString();
        assertEquals(expected, v.getNextReviewDate());
    }

    @Test
    void correctAnswer_setsLastReviewDateToToday() {
        Vocabulary v = makeVocab(2, 0, 0);
        v.setLastReviewDate("2020-01-01");
        new ReviewManager(List.of(v)).updateAfterAnswer(v, true);
        assertEquals(LocalDate.now().toString(), v.getLastReviewDate());
    }

    // ── updateAfterAnswer – wrong ─────────────────────────────

    @Test
    void wrongAnswer_incrementsWrongCountAndDecreasesFamiliarity() {
        Vocabulary v = makeVocab(3, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, false);
        assertEquals(1, v.getWrongCount());
        assertEquals(2, v.getFamiliarity());
    }

    @Test
    void wrongAnswer_setsNextReviewToTomorrow() {
        Vocabulary v = makeVocab(3, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, false);
        String tomorrow = LocalDate.now().plusDays(1).toString();
        assertEquals(tomorrow, v.getNextReviewDate());
    }

    @Test
    void wrongAnswer_atMinFamiliarity_staysAt1() {
        Vocabulary v = makeVocab(1, 0, 0);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, false);
        assertEquals(1, v.getFamiliarity());
    }

    @Test
    void wrongAnswer_accumulatesWrongCount() {
        Vocabulary v = makeVocab(3, 0, 2);
        new ReviewManager(List.of(v)).updateAfterAnswer(v, false);
        assertEquals(3, v.getWrongCount());
    }

    // ── getTodayReviewWords ───────────────────────────────────

    @Test
    void getTodayReviewWords_returnsDueWordsOnly() {
        Vocabulary due    = makeVocab(1, 0, 0); due.setNextReviewDate("2020-01-01");
        Vocabulary notDue = makeVocab(1, 0, 0); notDue.setNextReviewDate("2099-12-31");
        ReviewManager rm  = new ReviewManager(List.of(due, notDue));
        List<Vocabulary> result = rm.getTodayReviewWords();
        assertEquals(1, result.size());
        assertSame(due, result.get(0));
    }

    @Test
    void getTodayReviewWords_emptyPool_returnsEmpty() {
        ReviewManager rm = new ReviewManager(new ArrayList<>());
        assertTrue(rm.getTodayReviewWords().isEmpty());
    }

    // ── getWrongWords ─────────────────────────────────────────

    @Test
    void getWrongWords_excludesZeroWrongCount() {
        Vocabulary ok   = makeVocab(1, 0, 0);
        Vocabulary bad  = makeVocab(1, 0, 3);
        ReviewManager rm = new ReviewManager(List.of(ok, bad));
        List<Vocabulary> result = rm.getWrongWords();
        assertEquals(1, result.size());
        assertSame(bad, result.get(0));
    }

    @Test
    void getWrongWords_sortedByWrongCountDescending() {
        Vocabulary v1 = makeVocab(1, 0, 1);
        Vocabulary v2 = makeVocab(1, 0, 5);
        Vocabulary v3 = makeVocab(1, 0, 3);
        ReviewManager rm = new ReviewManager(List.of(v1, v2, v3));
        List<Vocabulary> result = rm.getWrongWords();
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getWrongCount());
        assertEquals(3, result.get(1).getWrongCount());
        assertEquals(1, result.get(2).getWrongCount());
    }

    // ── getAverageFamiliarityPercent ──────────────────────────

    @Test
    void averageFamiliarityPercent_emptyList_returnsZero() {
        assertEquals(0, new ReviewManager(new ArrayList<>()).getAverageFamiliarityPercent());
    }

    @Test
    void averageFamiliarityPercent_allMaxFamiliarity_returns100() {
        Vocabulary v1 = makeVocab(5, 0, 0);
        Vocabulary v2 = makeVocab(5, 0, 0);
        ReviewManager rm = new ReviewManager(List.of(v1, v2));
        assertEquals(100, rm.getAverageFamiliarityPercent());
    }

    @Test
    void averageFamiliarityPercent_mixedValues() {
        // avg familiarity = (3+1)/2 = 2 → (2/5)*100 = 40
        Vocabulary v1 = makeVocab(3, 0, 0);
        Vocabulary v2 = makeVocab(1, 0, 0);
        ReviewManager rm = new ReviewManager(List.of(v1, v2));
        assertEquals(40, rm.getAverageFamiliarityPercent());
    }

    // ── getLearnedCount ───────────────────────────────────────

    @Test
    void getLearnedCount_countsWordsWithCorrectCountOrHighFamiliarity() {
        Vocabulary learned1 = makeVocab(1, 1, 0); // correctCount > 0
        Vocabulary learned2 = makeVocab(2, 0, 0); // familiarity > 1
        Vocabulary notYet   = makeVocab(1, 0, 0); // neither
        ReviewManager rm = new ReviewManager(List.of(learned1, learned2, notYet));
        assertEquals(2, rm.getLearnedCount());
    }

    @Test
    void getLearnedCount_emptyList_returnsZero() {
        assertEquals(0, new ReviewManager(new ArrayList<>()).getLearnedCount());
    }

    // ── Helper ────────────────────────────────────────────────

    private Vocabulary makeVocab(int familiarity, int correctCount, int wrongCount) {
        Vocabulary v = new Vocabulary("word", "meaning", "n.", "example");
        v.setFamiliarity(familiarity);
        v.setCorrectCount(correctCount);
        v.setWrongCount(wrongCount);
        return v;
    }
}
