package controller;

import manager.FileManager;
import manager.QuizManager;
import manager.ReviewManager;
import model.Vocabulary;
import model.VocabCollection;

import java.util.List;

public class DashboardController {

    private List<Vocabulary>      vocabList;
    private QuizManager           quizManager;
    private ReviewManager         reviewManager;
    private List<VocabCollection> collections;

    public DashboardController() {
        reload();
    }

    public void reload() {
        vocabList     = FileManager.loadVocabulary();
        quizManager   = new QuizManager(vocabList);
        reviewManager = new ReviewManager(vocabList);
        collections   = FileManager.loadCollections();
    }

    public void save() {
        FileManager.saveVocabulary(vocabList);
    }

    public void saveCollections() {
        FileManager.saveCollections(collections);
    }

    // ── 資料存取 ──────────────────────────────────────────────
    public List<Vocabulary> getVocabList()  { return vocabList; }
    public QuizManager   getQuizManager()   { return quizManager; }
    public ReviewManager getReviewManager() { return reviewManager; }

    // ── Dashboard 統計 ────────────────────────────────────────
    public int  getTotalCount()                { return vocabList.size(); }
    public long getLearnedCount()              { return reviewManager.getLearnedCount(); }
    public int  getTodayReviewCount()          { return reviewManager.getTodayReviewWords().size(); }

    /** DashboardPanel 使用 */
    public int  getAverageFamiliarityPercent() { return reviewManager.getAverageFamiliarityPercent(); }
    /** 別名，保持相容 */
    public int  getAverageFamiliarityPct()     { return getAverageFamiliarityPercent(); }

    public List<Vocabulary> getTodayWords()    { return reviewManager.getTodayReviewWords(); }
    public List<Vocabulary> getWrongWords()    { return reviewManager.getWrongWords(); }

    public List<Vocabulary> getHistoryWords() {
        return vocabList.stream()
                .filter(v -> v.getCorrectCount() > 0 || v.getWrongCount() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Vocabulary> getFavoriteWords() {
        return vocabList.stream()
                .filter(Vocabulary::isFavorite)
                .collect(java.util.stream.Collectors.toList());
    }

    public void toggleFavorite(Vocabulary vocab, boolean favorite) {
        vocab.setFavorite(favorite);
        save();
    }

    // ── Collection 管理 ───────────────────────────────────────
    public List<VocabCollection> getCollections() { return collections; }

    public void addCollection(VocabCollection col) {
        collections.add(col);
        saveCollections();
    }

    public void removeCollection(VocabCollection col) {
        collections.remove(col);
        saveCollections();
    }

    /** 答題後更新並自動存檔 */
    public void submitAnswer(Vocabulary vocab, boolean correct) {
        reviewManager.updateAfterAnswer(vocab, correct);
        save();
    }
}
