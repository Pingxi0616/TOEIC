package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * 測驗前的單字庫來源選擇器
 * 顯示：全部單字 / 收藏 / 錯題 / 已學 / 各 Collection 群組
 * 使用者選擇後呼叫 onSelect(wordList) 回調
 */
public class QuizSourceSelector extends JPanel {

    public enum SourceType { ALL, FAVORITE, WRONG, HISTORY, COLLECTION }

    public static class Source {
        public final SourceType type;
        public final String     label;
        public final String     icon;
        public final Color      color;
        public final String     detail;
        public final VocabCollection collection; // 僅 COLLECTION 時使用

        Source(SourceType t, String i, String l, Color c, String d, VocabCollection col) {
            type = t; icon = i; label = l; color = c; detail = d; collection = col;
        }
    }

    private final DashboardController ctrl;
    private final String              title;
    private final Consumer<List<Vocabulary>> onSelect;
    private JPanel sourceGrid;

    public QuizSourceSelector(DashboardController ctrl, String title,
                              Consumer<List<Vocabulary>> onSelect) {
        this.ctrl     = ctrl;
        this.title    = title;
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildBody(),      BorderLayout.CENTER);
        refreshSources();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel t = new JLabel(title);
        t.setFont(AppColors.FONT_TITLE);
        t.setForeground(AppColors.TEXT_PRIMARY);
        JLabel hint = new JLabel("請選擇要練習的單字來源");
        hint.setFont(AppColors.FONT_BODY);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        p.add(t,    BorderLayout.WEST);
        p.add(hint, BorderLayout.EAST);
        return p;
    }

    private JPanel buildBody() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        sourceGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        sourceGrid.setOpaque(false);
        JScrollPane scroll = UIUtils.styledScroll(sourceGrid);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    public void refreshSources() {
        sourceGrid.removeAll();

        // ── 固定來源 ──
        addSourceCard(new Source(SourceType.ALL,      "📚","全部單字",      AppColors.TEXT_PRIMARY,
            ctrl.getTotalCount() + " 個單字", null));
        addSourceCard(new Source(SourceType.FAVORITE, "♥", "Favorite 收藏", AppColors.TEXT_RED,
            ctrl.getFavoriteWords().size() + " 個單字", null));
        addSourceCard(new Source(SourceType.WRONG,    "✗", "錯誤單字",      new Color(0xE65100),
            ctrl.getWrongWords().size() + " 個（依錯誤次數排序）", null));
        addSourceCard(new Source(SourceType.HISTORY,  "◷", "已學單字",      new Color(0x5C6BC0),
            ctrl.getHistoryWords().size() + " 個（練習過的單字）", null));

        // ── 今日待複習 ──
        int todayCnt = ctrl.getTodayReviewCount();
        addSourceCard(new Source(SourceType.ALL,      "🗓", "今日待複習",   new Color(0x2E7D6E),
            todayCnt + " 個單字需複習", null) {
            @Override public String toString() { return "today"; }
        });

        // ── Collection 群組 ──
        for (VocabCollection col : ctrl.getCollections()) {
            addSourceCard(new Source(SourceType.COLLECTION, "📁", col.getName(),
                new Color(0x7B5EA7),
                col.getWords().size() + " 個單字", col));
        }

        sourceGrid.revalidate();
        sourceGrid.repaint();
    }

    private void addSourceCard(Source src) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 圖示 + 標題
        JLabel titleLbl = new JLabel(src.icon + "  " + src.label);
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(src.color);

        // 數量說明
        JLabel detailLbl = new JLabel(src.detail);
        detailLbl.setFont(AppColors.FONT_SMALL);
        detailLbl.setForeground(AppColors.TEXT_SECONDARY);

        // 開始按鈕
        JButton startBtn = new JButton("開始練習 →");
        startBtn.setFont(AppColors.FONT_BTN);
        startBtn.setBackground(AppColors.BTN_PRIMARY);
        startBtn.setForeground(Color.WHITE);
        startBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        startBtn.addActionListener(e -> selectSource(src));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectSource(src); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(new Color(0xF0E8D8)); startBtn.setBackground(new Color(0x3A4A5E)); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { card.setBackground(AppColors.BG_CARD);    startBtn.setBackground(AppColors.BTN_PRIMARY); }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(detailLbl, BorderLayout.WEST);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bottom,   BorderLayout.CENTER);
        card.add(startBtn, BorderLayout.SOUTH);
        sourceGrid.add(card);
    }

    private void selectSource(Source src) {
        List<Vocabulary> words;
        switch (src.type) {
            case FAVORITE   -> words = ctrl.getFavoriteWords();
            case WRONG      -> words = ctrl.getWrongWords();
            case HISTORY    -> words = ctrl.getHistoryWords();
            case COLLECTION -> words = ctrl.getCollectionWords(src.collection);
            default -> {
                // ALL 且 label 含「今日」→ today review
                if (src.label.contains("今日")) words = ctrl.getTodayWords();
                else words = ctrl.getVocabList();
            }
        }
        if (words.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "「" + src.label + "」目前沒有單字，請先新增或完成測驗！",
                "無單字", JOptionPane.WARNING_MESSAGE);
            return;
        }
        onSelect.accept(words);
    }
}
