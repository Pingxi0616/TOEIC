import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.Vocabulary;
import java.time.LocalDate;

class VocabularyTest {

    // ── Constructor ───────────────────────────────────────────

    @Test
    void defaultConstructor_fieldsAreNull() {
        Vocabulary v = new Vocabulary();
        assertNull(v.getWord());
        assertNull(v.getMeaning());
    }

    @Test
    void paramConstructor_setsAllFields() {
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "He abandoned the plan.");
        assertEquals("abandon", v.getWord());
        assertEquals("to leave", v.getMeaning());
        assertEquals("v.", v.getPos());
        assertEquals("He abandoned the plan.", v.getExample());
        assertEquals(1, v.getFamiliarity());
        assertNotNull(v.getLastReviewDate());
        assertNotNull(v.getNextReviewDate());
    }

    // ── Familiarity clamping ──────────────────────────────────

    @Test
    void setFamiliarity_clampsBelowMin() {
        Vocabulary v = new Vocabulary();
        v.setFamiliarity(0);
        assertEquals(1, v.getFamiliarity());
    }

    @Test
    void setFamiliarity_clampsAboveMax() {
        Vocabulary v = new Vocabulary();
        v.setFamiliarity(10);
        assertEquals(5, v.getFamiliarity());
    }

    @Test
    void setFamiliarity_acceptsValidRange() {
        Vocabulary v = new Vocabulary();
        for (int i = 1; i <= 5; i++) {
            v.setFamiliarity(i);
            assertEquals(i, v.getFamiliarity());
        }
    }

    @Test
    void getFamiliarity_defaultConstructor_returnsOne() {
        // raw field is 0 when unset; getter clamps it to 1
        Vocabulary v = new Vocabulary();
        assertEquals(1, v.getFamiliarity());
    }

    // ── getFamiliarityStars ───────────────────────────────────

    @Test
    void getFamiliarityStars_level1() {
        Vocabulary v = new Vocabulary();
        v.setFamiliarity(1);
        assertEquals("★☆☆☆☆", v.getFamiliarityStars());
    }

    @Test
    void getFamiliarityStars_level3() {
        Vocabulary v = new Vocabulary();
        v.setFamiliarity(3);
        assertEquals("★★★☆☆", v.getFamiliarityStars());
    }

    @Test
    void getFamiliarityStars_level5() {
        Vocabulary v = new Vocabulary();
        v.setFamiliarity(5);
        assertEquals("★★★★★", v.getFamiliarityStars());
    }

    // ── isDueToday ────────────────────────────────────────────

    @Test
    void isDueToday_nullDate_returnsTrue() {
        Vocabulary v = new Vocabulary();
        assertTrue(v.isDueToday());
    }

    @Test
    void isDueToday_pastDate_returnsTrue() {
        Vocabulary v = new Vocabulary();
        v.setNextReviewDate("2020-01-01");
        assertTrue(v.isDueToday());
    }

    @Test
    void isDueToday_today_returnsTrue() {
        Vocabulary v = new Vocabulary();
        v.setNextReviewDate(LocalDate.now().toString());
        assertTrue(v.isDueToday());
    }

    @Test
    void isDueToday_futureDate_returnsFalse() {
        Vocabulary v = new Vocabulary();
        v.setNextReviewDate("2099-12-31");
        assertFalse(v.isDueToday());
    }

    // ── Null safety ───────────────────────────────────────────

    @Test
    void getPos_null_returnsEmptyString() {
        Vocabulary v = new Vocabulary();
        assertEquals("", v.getPos());
    }

    @Test
    void getExample_null_returnsEmptyString() {
        Vocabulary v = new Vocabulary();
        assertEquals("", v.getExample());
    }

    // ── Counters & favorite ───────────────────────────────────

    @Test
    void wrongAndCorrectCounts_defaultZero() {
        Vocabulary v = new Vocabulary();
        assertEquals(0, v.getWrongCount());
        assertEquals(0, v.getCorrectCount());
    }

    @Test
    void setWrongAndCorrectCount() {
        Vocabulary v = new Vocabulary();
        v.setWrongCount(3);
        v.setCorrectCount(7);
        assertEquals(3, v.getWrongCount());
        assertEquals(7, v.getCorrectCount());
    }

    @Test
    void toggleFavorite() {
        Vocabulary v = new Vocabulary();
        assertFalse(v.isFavorite());
        v.setFavorite(true);
        assertTrue(v.isFavorite());
        v.setFavorite(false);
        assertFalse(v.isFavorite());
    }

    @Test
    void isCustom_defaultFalse() {
        Vocabulary v = new Vocabulary();
        assertFalse(v.isCustom());
        v.setCustom(true);
        assertTrue(v.isCustom());
    }

    // ── toString ─────────────────────────────────────────────

    @Test
    void toString_containsWordAndMeaning() {
        Vocabulary v = new Vocabulary("abandon", "to leave", "v.", "");
        String s = v.toString();
        assertTrue(s.contains("abandon"));
        assertTrue(s.contains("to leave"));
    }
}
