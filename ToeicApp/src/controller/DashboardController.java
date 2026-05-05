package controller;

import manager.FileManager;
import manager.QuizManager;
import manager.ReviewManager;
import model.Vocabulary;

import java.util.List;

public class DashboardController {

    private List<Vocabulary> vocabList;
    private QuizManager quizManager;
    private ReviewManager reviewManager;

    public DashboardController() {
        reload();
    }

    public void reload() {
        vocabList     = FileManager.loadVocabulary();
        quizManager   = new QuizManager(vocabList);
        reviewManager = new ReviewManager(vocabList);
    }

    public void save() {
        FileManager.saveVocabulary(vocabList);
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

    /** 答題後更新並自動存檔 */
    public void submitAnswer(Vocabulary vocab, boolean correct) {
        reviewManager.updateAfterAnswer(vocab, correct);
        save();
    }
}
