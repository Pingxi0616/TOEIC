import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import manager.QuizManager;
import manager.QuizManager.Mode;
import manager.QuizManager.QuizItem;
import model.Vocabulary;
import java.util.*;

class QuizManagerTest {

    // ── Helper ────────────────────────────────────────────────

    private List<Vocabulary> makeList(int count) {
        String[] words    = {"abandon","ability","abide","absent","accept","access","achieve","active"};
        String[] meanings = {"to leave","skill","follow","missing","receive","enter","reach","energetic"};
        String[] poses    = {"v.","n.","v.","adj.","v.","n.","v.","adj."};
        List<Vocabulary> list = new ArrayList<>();
        for (int i = 0; i < count && i < words.length; i++) {
            Vocabulary v = new Vocabulary(words[i], meanings[i], poses[i],
                    "The manager decided to " + words[i] + " the proposal.");
            v.setPhrase(words[i] + " on");
            v.setPhraseMeaning("phrase meaning for " + words[i]);
            list.add(v);
        }
        return list;
    }

    // ── generateQuizList ──────────────────────────────────────

    @Test
    void generateQuizList_countRespected() {
        QuizManager qm = new QuizManager(makeList(8));
        assertEquals(5, qm.generateQuizList(5, false).size());
    }

    @Test
    void generateQuizList_poolSmallerThanCount() {
        QuizManager qm = new QuizManager(makeList(3));
        assertEquals(3, qm.generateQuizList(10, false).size());
    }

    @Test
    void generateQuizList_emptyPool_returnsEmpty() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        assertEquals(0, qm.generateQuizList(10, false).size());
    }

    @Test
    void generateQuizList_prioritizeWrong_mostWrongWordsLeadTheList() {
        List<Vocabulary> list = makeList(8);
        list.get(0).setWrongCount(5);  // highest
        list.get(1).setWrongCount(3);  // second
        QuizManager qm = new QuizManager(list);
        // count=4 → wrongSlots = 4/2 = 2, so first 2 must be the wrong words in order
        List<Vocabulary> quiz = qm.generateQuizList(4, true);
        assertEquals(4, quiz.size());
        assertEquals(5, quiz.get(0).getWrongCount());
        assertEquals(3, quiz.get(1).getWrongCount());
    }

    @Test
    void generateQuizList_noWrongWords_stillFillsCount() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        List<Vocabulary> quiz = qm.generateQuizList(6, true);
        assertEquals(6, quiz.size());
    }

    // ── getAnswer ─────────────────────────────────────────────

    @Test
    void getAnswer_enToCn_returnsMeaning() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("to leave", qm.getAnswer(v, Mode.EN_TO_CN));
    }

    @Test
    void getAnswer_cnToEn_returnsWord() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("abandon", qm.getAnswer(v, Mode.CN_TO_EN));
    }

    @Test
    void getAnswer_phrase_returnsPhrase() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        v.setPhrase("abandon ship");
        assertEquals("abandon ship", qm.getAnswer(v, Mode.PHRASE));
    }

    @Test
    void getAnswer_phrase_noPhrase_fallsBackToWord() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("abandon", qm.getAnswer(v, Mode.PHRASE));
    }

    @Test
    void getAnswer_fillBlank_returnsWord() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("abandon", qm.getAnswer(v, Mode.FILL_BLANK));
    }

    @Test
    void getAnswer_nullWord_returnsEmpty() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary();
        assertEquals("", qm.getAnswer(v, Mode.CN_TO_EN));
    }

    @Test
    void getAnswer_nullMeaning_returnsEmpty() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary();
        assertEquals("", qm.getAnswer(v, Mode.EN_TO_CN));
    }

    // ── getQuestion ───────────────────────────────────────────

    @Test
    void getQuestion_enToCn_returnsWord() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("abandon", qm.getQuestion(v, Mode.EN_TO_CN));
    }

    @Test
    void getQuestion_cnToEn_returnsMeaning() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        assertEquals("to leave", qm.getQuestion(v, Mode.CN_TO_EN));
    }

    @Test
    void getQuestion_phrase_returnsPhraseMeaning() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        v.setPhraseMeaning("give up a ship");
        assertEquals("give up a ship", qm.getQuestion(v, Mode.PHRASE));
    }

    @Test
    void getQuestion_fillBlank_withExample_replacesWord() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.",
                "The company had to abandon the project.");
        String q = qm.getQuestion(v, Mode.FILL_BLANK);
        assertTrue(q.contains("_____"), "Should contain blank marker");
        assertFalse(q.toLowerCase().contains("abandon"), "Word should be hidden");
    }

    @Test
    void getQuestion_fillBlank_noExample_usesTemplate() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", null);
        String q = qm.getQuestion(v, Mode.FILL_BLANK);
        assertTrue(q.contains("_____"), "Template must contain blank marker");
    }

    @Test
    void getQuestion_fillBlank_templateByPos_noun() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("ability", "skill", "n.", null);
        String q = qm.getQuestion(v, Mode.FILL_BLANK);
        assertTrue(q.contains("_____"));
    }

    @Test
    void getQuestion_fillBlank_templateByPos_adj() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        Vocabulary v = new Vocabulary("active", "energetic", "adj.", null);
        String q = qm.getQuestion(v, Mode.FILL_BLANK);
        assertTrue(q.contains("_____"));
    }

    // ── generateOptions ───────────────────────────────────────

    @Test
    void generateOptions_returns4Options() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        List<String> opts = qm.generateOptions(list.get(0), Mode.EN_TO_CN);
        assertEquals(4, opts.size());
    }

    @Test
    void generateOptions_alwaysContainsCorrectAnswer() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        Vocabulary target = list.get(0);
        String answer = qm.getAnswer(target, Mode.EN_TO_CN);
        List<String> opts = qm.generateOptions(target, Mode.EN_TO_CN);
        assertTrue(opts.contains(answer), "Options must include the correct answer");
    }

    @Test
    void generateOptions_noNullOrEmptyOption() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        List<String> opts = qm.generateOptions(list.get(0), Mode.EN_TO_CN);
        for (String o : opts) {
            assertNotNull(o);
            assertFalse(o.isEmpty(), "Option must not be empty");
        }
    }

    @Test
    void generateOptions_noDuplicates() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        List<String> opts = qm.generateOptions(list.get(0), Mode.CN_TO_EN);
        long distinct = opts.stream().distinct().count();
        assertEquals(opts.size(), distinct, "Options should be unique");
    }

    // ── modeLabel ─────────────────────────────────────────────

    @Test
    void modeLabel_allFourModes() {
        assertEquals("英翻中", QuizManager.modeLabel(Mode.EN_TO_CN));
        assertEquals("中翻英", QuizManager.modeLabel(Mode.CN_TO_EN));
        assertEquals("片語",   QuizManager.modeLabel(Mode.PHRASE));
        assertEquals("填空",   QuizManager.modeLabel(Mode.FILL_BLANK));
    }

    // ── generateMixedQuiz ─────────────────────────────────────

    @Test
    void generateMixedQuiz_totalCountCorrect() {
        List<Vocabulary> list = makeList(8);
        list.get(0).setWrongCount(2);
        list.get(1).setWrongCount(1);
        QuizManager qm = new QuizManager(list);
        List<QuizItem> items = qm.generateMixedQuiz(3, 2, 2, false);
        assertEquals(7, items.size());
    }

    @Test
    void generateMixedQuiz_fillBlankItemsHaveCorrectMode() {
        List<Vocabulary> list = makeList(8);
        QuizManager qm = new QuizManager(list);
        List<QuizItem> items = qm.generateMixedQuiz(0, 3, 0, false);
        long fillCount = items.stream().filter(i -> i.mode == Mode.FILL_BLANK).count();
        assertEquals(3, fillCount);
    }

    @Test
    void generateMixedQuiz_wrongCountCappedByPool() {
        List<Vocabulary> list = makeList(8);
        // no words have wrongCount > 0, so wrong pool starts empty and gets filled from lowFam
        QuizManager qm = new QuizManager(list);
        List<QuizItem> items = qm.generateMixedQuiz(0, 0, 3, false);
        assertEquals(3, items.size());
    }

    // ── getWordsWithExample ───────────────────────────────────

    @Test
    void getWordsWithExample_filtersCorrectly() {
        List<Vocabulary> list = new ArrayList<>();
        list.add(new Vocabulary("word1", "meaning1", "n.", "Has an example."));
        list.add(new Vocabulary("word2", "meaning2", "v.", null));
        list.add(new Vocabulary("word3", "meaning3", "adj.", ""));
        QuizManager qm = new QuizManager(list);
        List<Vocabulary> result = qm.getWordsWithExample();
        assertEquals(1, result.size());
        assertEquals("word1", result.get(0).getWord());
    }

    @Test
    void getWordsWithExample_emptyPool_returnsEmpty() {
        QuizManager qm = new QuizManager(new ArrayList<>());
        assertTrue(qm.getWordsWithExample().isEmpty());
    }
}
