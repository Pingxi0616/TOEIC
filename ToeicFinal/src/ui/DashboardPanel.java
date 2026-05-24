package ui;

import controller.DashboardController;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable onFavorite, onWrong, onCollection, onHistory;

    // 閃卡
    private List<Vocabulary> cardWords;
    private int     cardIndex   = 0;
    private boolean showMeaning = false;
    private JLabel  cardWordLabel, cardPosLabel, cardMeaningLabel, cardIndexLabel;
    private JButton heartBtn, camBtn;

    // 計數
    private JLabel favCountLabel, wrongCountLabel, colCountLabel, historyCountLabel;

    // 日期
    private JLabel dateLabel;

    private static final Color WRONG_YELLOW = new Color(0xF9A825);
    private static final Color COL_TEAL     = new Color(0x2E7D6E);
    private static final Color HIST_INDIGO  = new Color(0x5C6BC0);

    public DashboardPanel(DashboardController ctrl,
                          Runnable onFavorite, Runnable onWrong,
                          Runnable onCollection, Runnable onHistory) {
        this.ctrl = ctrl;
        this.onFavorite   = onFavorite;
        this.onWrong      = onWrong;
        this.onCollection = onCollection;
        this.onHistory    = onHistory;

        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildDateBar(),  BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        refresh();
        fetchDateAsync();
    }

    // ── 日期列 ────────────────────────────────────────────────
    private JPanel buildDateBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));
        dateLabel = new JLabel(formatDate(LocalDate.now()));
        dateLabel.setFont(AppColors.FONT_BODY);
        dateLabel.setForeground(AppColors.TEXT_SECONDARY);
        dateLabel.setToolTipText("日期已透過網路同步");
        p.add(dateLabel, BorderLayout.EAST);
        return p;
    }

    // ── 主體 ──────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.add(buildNavCards(),  BorderLayout.NORTH);
        p.add(buildFlashCard(), BorderLayout.CENTER);
        return p;
    }

    // ── 4 個導航卡（Favorite / Wrong / Collection / History） ─
    private JPanel buildNavCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 110));

        favCountLabel     = countLabel("0");
        wrongCountLabel   = countLabel("0");
        colCountLabel     = countLabel("0");
        historyCountLabel = countLabel("0");

        row.add(buildNavCard("♥","#C62828","Favorite 單字",  favCountLabel,    "已收藏",  AppColors.TEXT_RED,  onFavorite));
        row.add(buildNavCard("✗","#E65100","錯誤單字區",     wrongCountLabel,  "已答錯",  new Color(0xE65100), onWrong));
        row.add(buildNavCard("☰","#2E7D6E","Collection 群組", colCountLabel,   "個群組",  COL_TEAL,            onCollection));
        row.add(buildNavCard("◷","#5C6BC0","History 已學",   historyCountLabel,"已練習",  HIST_INDIGO,         onHistory));
        return row;
    }

    private JPanel buildNavCard(String icon, String iconHex, String title,
                                JLabel cntLabel, String unit, Color cntColor, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon + "  " + title);
        iconLbl.setFont(AppColors.FONT_HEAD);
        iconLbl.setForeground(Color.decode(iconHex));

        cntLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 26));
        cntLabel.setForeground(cntColor);
        cntLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel unitLbl = new JLabel(unit, SwingConstants.CENTER);
        unitLbl.setFont(AppColors.FONT_SMALL);
        unitLbl.setForeground(AppColors.TEXT_SECONDARY);

        JPanel mid = new JPanel(new GridLayout(2, 1, 0, 2));
        mid.setOpaque(false);
        mid.add(cntLabel);
        mid.add(unitLbl);

        card.add(iconLbl, BorderLayout.NORTH);
        card.add(mid,     BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(new Color(0xF0E8D8)); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { card.setBackground(AppColors.BG_CARD); }
        });
        return card;
    }

    // ── 閃卡區域 ──────────────────────────────────────────────
    private JPanel buildFlashCard() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);

        // 卡片本體
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(30, 36, 24, 36)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cardWordLabel = new JLabel("", SwingConstants.CENTER);
        cardWordLabel.setFont(new Font("Serif", Font.BOLD, 44));
        cardWordLabel.setForeground(new Color(0x1565C0));

        cardPosLabel = new JLabel("", SwingConstants.CENTER);
        cardPosLabel.setFont(new Font("Microsoft JhengHei", Font.ITALIC, 15));
        cardPosLabel.setForeground(AppColors.TEXT_SECONDARY);

        cardMeaningLabel = new JLabel("點擊顯示中文", SwingConstants.CENTER);
        cardMeaningLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 22));
        cardMeaningLabel.setForeground(AppColors.TEXT_SECONDARY);

        cardIndexLabel = new JLabel("", SwingConstants.CENTER);
        cardIndexLabel.setFont(AppColors.FONT_SMALL);
        cardIndexLabel.setForeground(AppColors.TEXT_SECONDARY);

        JPanel centerBlock = new JPanel(new GridLayout(4, 1, 0, 8));
        centerBlock.setOpaque(false);
        centerBlock.add(cardPosLabel);
        centerBlock.add(cardWordLabel);
        centerBlock.add(cardMeaningLabel);
        centerBlock.add(cardIndexLabel);

        card.add(centerBlock, BorderLayout.CENTER);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { toggleMeaning(); }
        });

        // 控制列
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        JButton prevBtn = navBtn("←");
        JButton nextBtn = navBtn("→");
        heartBtn = iconBtn("♡");
        camBtn   = iconBtn("🔍");
        JButton pronounceBtn = iconBtn("🔊");

        prevBtn.addActionListener(e -> { if (cardWords != null && !cardWords.isEmpty()) { cardIndex = (cardIndex - 1 + cardWords.size()) % cardWords.size(); loadCard(); } });
        nextBtn.addActionListener(e -> { if (cardWords != null && !cardWords.isEmpty()) { cardIndex = (cardIndex + 1) % cardWords.size(); loadCard(); } });
        heartBtn.addActionListener(e -> toggleHeart());
        camBtn.addActionListener(e -> {
            if (cardWords != null && cardIndex < cardWords.size())
                WordCardPopup.openCambridge(this, cardWords.get(cardIndex).getWord());
        });
        pronounceBtn.addActionListener(e -> pronounce());

        btnRow.add(prevBtn);
        btnRow.add(heartBtn);
        btnRow.add(pronounceBtn);
        btnRow.add(camBtn);
        btnRow.add(nextBtn);

        outer.add(card,   BorderLayout.CENTER);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    // ── 閃卡邏輯 ─────────────────────────────────────────────
    private void loadCard() {
        if (cardWords == null || cardWords.isEmpty()) {
            cardWordLabel.setText("尚無單字");
            cardMeaningLabel.setText("");
            cardIndexLabel.setText("");
            return;
        }
        Vocabulary v = cardWords.get(cardIndex);
        showMeaning = false;
        cardPosLabel.setText(v.getPos());
        cardWordLabel.setText(v.getWord());
        cardMeaningLabel.setText("點擊顯示中文");
        cardMeaningLabel.setForeground(AppColors.TEXT_SECONDARY);
        cardIndexLabel.setText((cardIndex + 1) + " / " + cardWords.size()
            + "   " + v.getFamiliarityStars());
        heartBtn.setText(v.isFavorite() ? "♥" : "♡");
        heartBtn.setForeground(v.isFavorite() ? AppColors.TEXT_RED : AppColors.TEXT_SECONDARY);
    }

    private void toggleMeaning() {
        if (cardWords == null || cardWords.isEmpty()) return;
        Vocabulary v = cardWords.get(cardIndex);
        showMeaning = !showMeaning;
        if (showMeaning) {
            cardMeaningLabel.setText(v.getMeaning());
            cardMeaningLabel.setForeground(AppColors.TEXT_PRIMARY);
        } else {
            cardMeaningLabel.setText("點擊顯示中文");
            cardMeaningLabel.setForeground(AppColors.TEXT_SECONDARY);
        }
    }

    private void toggleHeart() {
        if (cardWords == null || cardWords.isEmpty()) return;
        Vocabulary v = cardWords.get(cardIndex);
        ctrl.toggleFavorite(v, !v.isFavorite());
        heartBtn.setText(v.isFavorite() ? "♥" : "♡");
        heartBtn.setForeground(v.isFavorite() ? AppColors.TEXT_RED : AppColors.TEXT_SECONDARY);
        favCountLabel.setText(String.valueOf(ctrl.getFavoriteWords().size()));
    }

    private void pronounce() {
        if (cardWords == null || cardIndex >= cardWords.size()) return;
        String word = cardWords.get(cardIndex).getWord();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"say", word});
            else if (os.contains("nix") || os.contains("nux")) Runtime.getRuntime().exec(new String[]{"espeak", word});
            else Runtime.getRuntime().exec(new String[]{"powershell","-Command",
                "Add-Type -AssemblyName System.Speech;$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;$s.Speak('"+word.replace("'","")+"')"});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"發音需要系統支援","提示",JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── 刷新 ─────────────────────────────────────────────────
    public void refresh() {
        favCountLabel.setText(String.valueOf(ctrl.getFavoriteWords().size()));
        wrongCountLabel.setText(String.valueOf(ctrl.getWrongWords().size()));
        colCountLabel.setText(String.valueOf(ctrl.getCollections().size()));
        historyCountLabel.setText(String.valueOf(ctrl.getHistoryWords().size()));
        // 閃卡用今日待複習；若無則用全部
        List<Vocabulary> today = ctrl.getTodayWords();
        cardWords = today.isEmpty() ? ctrl.getVocabList() : today;
        if (cardIndex >= cardWords.size()) cardIndex = 0;
        loadCard();
    }

    // ── worldtimeapi 日期同步 ─────────────────────────────────
    private void fetchDateAsync() {
        new Thread(() -> {
            try {
                URL url = new URL("https://worldtimeapi.org/api/timezone/Asia/Taipei");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000); conn.setReadTimeout(3000);
                if (conn.getResponseCode() == 200) {
                    String body = new String(conn.getInputStream().readAllBytes(), "UTF-8");
                    java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("\"datetime\":\"(\\d{4}-\\d{2}-\\d{2})").matcher(body);
                    if (m.find()) {
                        LocalDate d = LocalDate.parse(m.group(1));
                        SwingUtilities.invokeLater(() -> {
                            dateLabel.setText(formatDate(d) + " ✓");
                            dateLabel.setToolTipText("已透過 worldtimeapi.org 同步");
                        });
                    }
                }
            } catch (Exception ignored) {}
        }, "date-sync").start();
    }

    private String formatDate(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("yyyy年M月d日，EEEE", Locale.TRADITIONAL_CHINESE));
    }

    // ── 工具 ─────────────────────────────────────────────────
    private JLabel countLabel(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Microsoft JhengHei", Font.BOLD, 26));
        return l;
    }
    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 20));
        b.setBackground(AppColors.BG_CARD);
        b.setForeground(AppColors.TEXT_PRIMARY);
        b.setBorder(new CompoundBorder(new LineBorder(AppColors.BORDER,1,true), new EmptyBorder(6,18,6,18)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton iconBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Serif", Font.PLAIN, 18));
        b.setBackground(AppColors.BG_CARD);
        b.setForeground(AppColors.TEXT_SECONDARY);
        b.setBorder(new CompoundBorder(new LineBorder(AppColors.BORDER_SOFT,1,true), new EmptyBorder(6,12,6,12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
