package model;

import java.time.LocalDate;

public class Vocabulary {
    private String word;
    private String meaning;
    private String pos;
    private String phrase;
    private String phraseMeaning;
    private String example;
    private int    wrongCount;
    private int    correctCount;
    private int    familiarity;   // 1~5
    private String lastReviewDate;
    private String nextReviewDate;
    private boolean favorite;     // 收藏標記

    public Vocabulary() {}

    public Vocabulary(String word, String meaning, String pos, String example) {
        this.word = word; this.meaning = meaning;
        this.pos  = pos;  this.example = example;
        this.familiarity    = 1;
        this.lastReviewDate = LocalDate.now().toString();
        this.nextReviewDate = LocalDate.now().toString();
    }

    // ── Getters ───────────────────────────────────────────────
    public String  getWord()           { return word; }
    public String  getMeaning()        { return meaning; }
    public String  getPos()            { return pos != null ? pos : ""; }
    public String  getPhrase()         { return phrase; }
    public String  getPhraseMeaning()  { return phraseMeaning; }
    public String  getExample()        { return example != null ? example : ""; }
    public int     getWrongCount()     { return wrongCount; }
    public int     getCorrectCount()   { return correctCount; }
    public int     getFamiliarity()    { return familiarity < 1 ? 1 : Math.min(familiarity, 5); }
    public String  getLastReviewDate() { return lastReviewDate; }
    public String  getNextReviewDate() { return nextReviewDate; }
    public boolean isFavorite()        { return favorite; }

    // ── Setters ───────────────────────────────────────────────
    public void setWord(String v)           { this.word = v; }
    public void setMeaning(String v)        { this.meaning = v; }
    public void setPos(String v)            { this.pos = v; }
    public void setPhrase(String v)         { this.phrase = v; }
    public void setPhraseMeaning(String v)  { this.phraseMeaning = v; }
    public void setExample(String v)        { this.example = v; }
    public void setWrongCount(int v)        { this.wrongCount = v; }
    public void setCorrectCount(int v)      { this.correctCount = v; }
    public void setFamiliarity(int v)       { this.familiarity = Math.max(1, Math.min(5, v)); }
    public void setLastReviewDate(String v) { this.lastReviewDate = v; }
    public void setNextReviewDate(String v) { this.nextReviewDate = v; }
    public void setFavorite(boolean v)      { this.favorite = v; }

    public String getFamiliarityStars() {
        int f = getFamiliarity();
        return "★".repeat(f) + "☆".repeat(5 - f);
    }

    public boolean isDueToday() {
        if (nextReviewDate == null) return true;
        return nextReviewDate.compareTo(LocalDate.now().toString()) <= 0;
    }

    @Override public String toString() { return word + "（" + meaning + "）"; }
}
