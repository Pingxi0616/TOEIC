package model;

public class Vocabulary {
    private String word;
    private String meaning;
    private String pos;
    private String phrase;
    private String phraseMeaning;
    private String example;
    private int wrongCount;
    private int correctCount;
    private int familiarity;
    private String lastReviewDate;
    private String nextReviewDate;

    public Vocabulary() {}

    public Vocabulary(String word, String meaning, String pos, String example) {
        this.word = word;
        this.meaning = meaning;
        this.pos = pos;
        this.example = example;
        this.wrongCount = 0;
        this.correctCount = 0;
        this.familiarity = 1;
        this.lastReviewDate = java.time.LocalDate.now().toString();
        this.nextReviewDate = java.time.LocalDate.now().toString();
    }

    public String getWord()           { return word; }
    public String getMeaning()        { return meaning; }
    public String getPos()            { return pos; }
    public String getPhrase()         { return phrase; }
    public String getPhraseMeaning()  { return phraseMeaning; }
    public String getExample()        { return example; }
    public int    getWrongCount()     { return wrongCount; }
    public int    getCorrectCount()   { return correctCount; }
    public int    getFamiliarity()    { return familiarity; }
    public String getLastReviewDate() { return lastReviewDate; }
    public String getNextReviewDate() { return nextReviewDate; }

    public void setWord(String v)            { this.word = v; }
    public void setMeaning(String v)         { this.meaning = v; }
    public void setPos(String v)             { this.pos = v; }
    public void setPhrase(String v)          { this.phrase = v; }
    public void setPhraseMeaning(String v)   { this.phraseMeaning = v; }
    public void setExample(String v)         { this.example = v; }
    public void setWrongCount(int v)         { this.wrongCount = v; }
    public void setCorrectCount(int v)       { this.correctCount = v; }
    public void setFamiliarity(int v)        { this.familiarity = v; }
    public void setLastReviewDate(String v)  { this.lastReviewDate = v; }
    public void setNextReviewDate(String v)  { this.nextReviewDate = v; }

    public String getFamiliarityStars() {
        return "★".repeat(familiarity) + "☆".repeat(5 - familiarity);
    }

    public boolean isDueToday() {
        if (nextReviewDate == null) return true;
        String today = java.time.LocalDate.now().toString();
        return nextReviewDate.compareTo(today) <= 0;
    }

    @Override
    public String toString() { return word + "（" + meaning + "）"; }
}
