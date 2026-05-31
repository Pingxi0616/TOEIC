package ui;

import controller.DashboardController;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import model.VocabCollection;
import model.Vocabulary;
import java.awt.Dialog;

public class DashboardPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable onFavorite, onWrong, onCollection, onHistory;

    // 閃卡
    private List<Vocabulary> cardWords;
    private int     cardIndex   = 0;
    private boolean showMeaning = false;
    private JLabel  cardWordLabel, cardPosLabel, cardMeaningLabel, cardIndexLabel;
    private JButton heartBtn, colBtn;

    // 計數
    private JLabel favCountLabel, wrongCountLabel, colCountLabel, historyCountLabel;

    // 日期 + 單字卡來源選擇器
    private JLabel dateLabel;
    private JComboBox<String> sourceCombo;
    private boolean updatingCombo = false;

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

        // 單字卡來源選擇器
        sourceCombo = new JComboBox<>();
        sourceCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        sourceCombo.setFont(AppColors.FONT_SMALL);
        sourceCombo.setBackground(AppColors.BG_MAIN);
        sourceCombo.setForeground(AppColors.TEXT_PRIMARY);
        sourceCombo.setBorder(new EmptyBorder(2, 6, 2, 6));
        sourceCombo.setFocusable(false);
        sourceCombo.setToolTipText("選擇單字卡顯示的單字庫");
        sourceCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(AppColors.FONT_SMALL);
                lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    lbl.setBackground(AppColors.BG_SIDEBAR);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                } else {
                    lbl.setBackground(AppColors.BG_MAIN);
                    lbl.setForeground(AppColors.TEXT_PRIMARY);
                }
                return lbl;
            }
        });
        sourceCombo.addActionListener(e -> { if (!updatingCombo) updateCardSource(); });

        JLabel srcLbl = new JLabel("單字卡：");
        srcLbl.setFont(AppColors.FONT_SMALL);
        srcLbl.setForeground(AppColors.TEXT_SECONDARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(srcLbl);
        right.add(sourceCombo);
        right.add(dateLabel);

        p.add(right, BorderLayout.EAST);
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
        row.setPreferredSize(new Dimension(0, 140));

        favCountLabel     = countLabel("0");
        wrongCountLabel   = countLabel("0");
        colCountLabel     = countLabel("0");
        historyCountLabel = countLabel("0");

        row.add(buildNavCard("♥", "Segoe UI Symbol", "Favorite 單字",   AppColors.TEXT_RED,  favCountLabel,    "已收藏", AppColors.TEXT_RED,  onFavorite));
        row.add(buildNavCard("✗", "Segoe UI Symbol", "錯誤單字區",      new Color(0xE65100), wrongCountLabel,  "已答錯", new Color(0xE65100), onWrong));
        row.add(buildNavCard("★", "Segoe UI Symbol", "Collection 群組", COL_TEAL,            colCountLabel,    "個群組", COL_TEAL,            onCollection));
        row.add(buildNavCard("✓", "Segoe UI Symbol", "History 已學",    HIST_INDIGO,         historyCountLabel,"已練習", HIST_INDIGO,         onHistory));
        return row;
    }

    private JPanel buildNavCard(String icon, String iconFont, String title, Color titleColor,
                                JLabel cntLabel, String unit, Color cntColor, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 圖示（各 icon 使用最適合的字型）
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font(iconFont, Font.PLAIN, 18));
        iconLbl.setForeground(titleColor);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
        titleLbl.setForeground(titleColor);

        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));
        titleRow.setOpaque(false);
        titleRow.add(iconLbl);
        titleRow.add(Box.createHorizontalStrut(5));
        titleRow.add(titleLbl);

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

        card.add(titleRow, BorderLayout.NORTH);
        card.add(mid,      BorderLayout.CENTER);

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
        cardWordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardWordLabel.setToolTipText("點擊查詢劍橋詞典");
        cardWordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (cardWords != null && !cardWords.isEmpty())
                    WordCardPopup.openCambridge(DashboardPanel.this, cardWords.get(cardIndex).getWord());
            }
        });

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
        colBtn   = iconBtn("＋");
        colBtn.setToolTipText("加入群組資料夾");
        JButton pronounceBtn = iconBtn("🔊");
        pronounceBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        prevBtn.addActionListener(e -> { if (cardWords != null && !cardWords.isEmpty()) { cardIndex = (cardIndex - 1 + cardWords.size()) % cardWords.size(); loadCard(); } });
        nextBtn.addActionListener(e -> { if (cardWords != null && !cardWords.isEmpty()) { cardIndex = (cardIndex + 1) % cardWords.size(); loadCard(); } });
        heartBtn.addActionListener(e -> toggleHeart());
        colBtn.addActionListener(e -> showAddToCollectionDialog());
        pronounceBtn.addActionListener(e -> pronounce());

        btnRow.add(prevBtn);
        btnRow.add(heartBtn);
        btnRow.add(pronounceBtn);
        btnRow.add(colBtn);
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

        // 更新單字卡來源選擇器（保留上次選擇）
        updatingCombo = true;
        String prevSel = sourceCombo.getSelectedItem() != null
                         ? sourceCombo.getSelectedItem().toString() : null;
        sourceCombo.removeAllItems();
        sourceCombo.addItem("今日待複習");
        sourceCombo.addItem("全部單字");
        sourceCombo.addItem("TOEIC 多益單字");
        sourceCombo.addItem("Favorite 收藏");
        sourceCombo.addItem("錯誤單字");
        for (VocabCollection col : ctrl.getCollections()) {
            sourceCombo.addItem(col.getName());
        }
        if (prevSel != null) sourceCombo.setSelectedItem(prevSel);
        updatingCombo = false;

        updateCardSource();
    }

    // ── 依選擇來源更新閃卡單字清單 ────────────────────────────
    private void updateCardSource() {
        if (sourceCombo == null) return;
        String sel = sourceCombo.getSelectedItem() != null
                     ? sourceCombo.getSelectedItem().toString() : "今日待複習";
        List<Vocabulary> words;
        if (sel.equals("全部單字")) {
            words = ctrl.getVocabList();
        } else if (sel.equals("TOEIC 多益單字")) {
            words = ctrl.getToeicWords();
        } else if (sel.equals("Favorite 收藏")) {
            words = ctrl.getFavoriteWords();
        } else if (sel.equals("錯誤單字")) {
            words = ctrl.getWrongWords();
        } else {
            // 先比對 collection 名稱
            VocabCollection col = ctrl.getCollections().stream()
                .filter(c -> c.getName().equals(sel)).findFirst().orElse(null);
            if (col != null) {
                words = ctrl.getCollectionWords(col);
            } else {
                // 今日待複習（預設）
                List<Vocabulary> today = ctrl.getTodayWords();
                words = today.isEmpty() ? ctrl.getVocabList() : today;
            }
        }
        cardWords = words.isEmpty() ? ctrl.getVocabList() : words;
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

    private void showAddToCollectionDialog() {
        if (cardWords == null || cardWords.isEmpty()) return;
        Vocabulary v = cardWords.get(cardIndex);
        List<VocabCollection> cols = ctrl.getCollections();
        if (cols.isEmpty()) {
            UIUtils.showMessage(this, "尚未建立任何群組，請先到「Collection 群組」建立群組", "加入群組");
            return;
        }

        // ── 自訂樣式對話框 ──────────────────────────────────────
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(AppColors.BG_MAIN);
        root.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));

        JLabel titleLbl = new JLabel("加入群組");
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel msgLbl = new JLabel("將「" + v.getWord() + "」加入群組：");
        msgLbl.setFont(AppColors.FONT_BODY);
        msgLbl.setForeground(AppColors.TEXT_SECONDARY);

        // 下拉選單
        String[] names = cols.stream()
            .map(c -> c.getName() + " (" + c.getWords().size() + " 個)")
            .toArray(String[]::new);
        JComboBox<String> combo = new JComboBox<>(names);
        combo.setFont(AppColors.FONT_BODY);
        combo.setBackground(AppColors.BG_CARD);
        combo.setForeground(AppColors.TEXT_PRIMARY);
        combo.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(msgLbl, BorderLayout.NORTH);
        centerPanel.add(combo,  BorderLayout.CENTER);

        // 按鈕
        int[] result = {-1};
        JButton cancel = styledDlgBtn("取消", AppColors.TEXT_SECONDARY, AppColors.BG_MAIN);
        JButton ok     = styledDlgBtn("確定", AppColors.BTN_PRIMARY, Color.WHITE);
        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> { result[0] = combo.getSelectedIndex(); dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(ok);

        root.add(titleLbl,    BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(btnRow,      BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(300, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (result[0] >= 0) {
            cols.get(result[0]).addWord(v.getWord());
            ctrl.saveCollections();
            UIUtils.showMessage(this,
                "已將「" + v.getWord() + "」加入「" + cols.get(result[0]).getName() + "」",
                "成功");
        }
    }

    private JButton styledDlgBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(5, 16, 5, 16)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
