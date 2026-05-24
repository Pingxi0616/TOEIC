package model;

import java.util.ArrayList;
import java.util.List;

public class VocabCollection {
    private String name;
    private List<String> words; // stores word strings as keys

    public VocabCollection() { this.words = new ArrayList<>(); }

    public VocabCollection(String name) {
        this.name  = name;
        this.words = new ArrayList<>();
    }

    public String getName()           { return name; }
    public void   setName(String n)   { this.name = n; }
    public List<String> getWords()    { return words; }
    public void setWords(List<String> w) { this.words = w; }

    public void addWord(String word)    { if (!words.contains(word)) words.add(word); }
    public void removeWord(String word) { words.remove(word); }
    public boolean containsWord(String word) { return words.contains(word); }

    @Override
    public String toString() { return name + " (" + words.size() + " 個單字)"; }
}
