import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import manager.FileManager;
import model.Vocabulary;
import java.io.*;
import java.nio.file.*;
import java.util.*;

class FileManagerTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("toeic_test");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }

    // ── importFromCsv ─────────────────────────────────────────

    @Test
    void importFromCsv_singleEntry_parsedCorrectly() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "abandon,v. to leave behind\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(1, result.size());
        assertEquals("abandon", result.get(0).getWord());
        assertTrue(result.get(0).getMeaning().contains("to leave behind"));
    }

    @Test
    void importFromCsv_multipleEntries() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv,
                "abandon,v. to leave\n" +
                "ability,n. a skill\n" +
                "active,adj. energetic\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(3, result.size());
    }

    @Test
    void importFromCsv_extractsPos() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "ability,n. a skill or capacity\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(1, result.size());
        assertEquals("n.", result.get(0).getPos());
        assertTrue(result.get(0).getMeaning().contains("a skill or capacity"));
    }

    @Test
    void importFromCsv_skipsEmptyLines() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "abandon,v. to leave\n\n\nability,n. a skill\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(2, result.size());
    }

    @Test
    void importFromCsv_skipsLineWithoutComma() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "no comma here\nability,n. a skill\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(1, result.size());
        assertEquals("ability", result.get(0).getWord());
    }

    @Test
    void importFromCsv_nonExistentFile_returnsEmptyList() {
        List<Vocabulary> result = FileManager.importFromCsv(
                tempDir.resolve("does_not_exist.csv").toString());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void importFromCsv_setsDefaultFamiliarityOne() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "abandon,v. to leave\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(1, result.get(0).getFamiliarity());
    }

    @Test
    void importFromCsv_setsLastAndNextReviewDate() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        Files.writeString(csv, "abandon,v. to leave\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        Vocabulary v = result.get(0);
        assertNotNull(v.getLastReviewDate(), "lastReviewDate must be set");
        assertNotNull(v.getNextReviewDate(), "nextReviewDate must be set");
        // Both should be today's date
        assertEquals(v.getLastReviewDate(), v.getNextReviewDate());
    }

    @Test
    void importFromCsv_entryWithoutPos_stillImported() throws IOException {
        Path csv = tempDir.resolve("vocab.csv");
        // No pos prefix (no "x." pattern), meaning is taken as-is
        Files.writeString(csv, "abandon,to leave behind\n");
        List<Vocabulary> result = FileManager.importFromCsv(csv.toString());
        assertEquals(1, result.size());
        assertEquals("abandon", result.get(0).getWord());
    }

    // ── loadVocabulary integration (smoke test) ───────────────

    @Test
    void loadVocabulary_returnsNonNullList() {
        List<Vocabulary> list = FileManager.loadVocabulary();
        assertNotNull(list, "loadVocabulary must never return null");
    }

    @Test
    void loadVocabulary_wordsHaveNonEmptyWordField() {
        List<Vocabulary> list = FileManager.loadVocabulary();
        if (list.isEmpty()) return; // skip if file not found
        for (Vocabulary v : list.subList(0, Math.min(10, list.size()))) {
            assertNotNull(v.getWord(), "word field must not be null");
            assertFalse(v.getWord().isBlank(), "word field must not be blank");
        }
    }
}
