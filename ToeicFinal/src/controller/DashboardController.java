package controller;

import manager.FileManager;
import manager.QuizManager;
import manager.ReviewManager;
import model.Vocabulary;
import model.VocabCollection;

import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {
    private List<Vocabulary>      vocabList;
    private QuizManager           quizManager;
    private ReviewManager         reviewManager;
    private List<VocabCollection> collections;

    public DashboardController() { reload(); }

    public void reload() {
        vocabList     = FileManager.loadVocabulary();
        quizManager   = new QuizManager(vocabList);
        reviewManager = new ReviewManager(vocabList);
        collections   = FileManager.loadCollections();
    }
    public void save()              { FileManager.saveVocabulary(vocabList); }
    public void saveCollections()   { FileManager.saveCollections(collections); }

    // ── 存取 ──────────────────────────────────────────────────
    public List<Vocabulary>      getVocabList()    { return vocabList; }
    public QuizManager           getQuizManager()  { return quizManager; }
    public ReviewManager         getReviewManager(){ return reviewManager; }
    public List<VocabCollection> getCollections()  { return collections; }

    // ── 統計 ──────────────────────────────────────────────────
    public int  getTotalCount()                { return vocabList.size(); }
    public long getLearnedCount()              { return reviewManager.getLearnedCount(); }
    public int  getTodayReviewCount()          { return reviewManager.getTodayReviewWords().size(); }
    public int  getAverageFamiliarityPercent() { return reviewManager.getAverageFamiliarityPercent(); }
    public int  getAverageFamiliarityPct()     { return getAverageFamiliarityPercent(); }
    public List<Vocabulary> getTodayWords()    { return reviewManager.getTodayReviewWords(); }
    public List<Vocabulary> getWrongWords()    { return reviewManager.getWrongWords(); }

    public List<Vocabulary> getHistoryWords() {
        return vocabList.stream()
            .filter(v -> v.getCorrectCount() > 0 || v.getWrongCount() > 0)
            .collect(Collectors.toList());
    }
    public List<Vocabulary> getFavoriteWords() {
        return vocabList.stream().filter(Vocabulary::isFavorite).collect(Collectors.toList());
    }

    /** 取得某個 Collection 的 Vocabulary 清單 */
    public List<Vocabulary> getCollectionWords(VocabCollection col) {
        return vocabList.stream()
            .filter(v -> col.containsWord(v.getWord()))
            .collect(Collectors.toList());
    }

    // ── 操作 ──────────────────────────────────────────────────
    public void toggleFavorite(Vocabulary v, boolean fav) { v.setFavorite(fav); save(); }
    public void addCollection(VocabCollection col)    { collections.add(col); saveCollections(); }
    public void removeCollection(VocabCollection col) { collections.remove(col); saveCollections(); }

    public void submitAnswer(Vocabulary vocab, boolean correct) {
        reviewManager.updateAfterAnswer(vocab, correct);
        save();
    }
}
