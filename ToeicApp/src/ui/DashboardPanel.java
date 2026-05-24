package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final DashboardController ctrl;

    private List<Vocabulary> cardWords;
    private int cardIndex = 0;
    private boolean showMeaning = false;

    private JLabel cardWordLabel;
    private JLabel cardPosLabel;
    private JLabel cardMeaningLabel;
    private JLabel cardIndexLabel;
    private JButton heartBtn;
    private JButton colBtn;

    private JLabel favCountLabel;
    private JLabel wrongCountLabel;
    private JLabel colCountLabel;
    private JLabel historyCountLabel;

    private final Runnable onFavorite;
    private final Runnable onWrong;
    private final Runnable onCollection;
    private final Runnable onHistory;

    // colors used in multiple places
    private static final Color WRONG_YELLOW = new Color(0xF9A825);
    private static final Color COL_TEAL     = new Color(0x2E7D6E);
    private static final Color HIST_INDIGO  = new Color(0x5C6BC0);

    public DashboardPanel(DashboardController ctrl,
                          Runnable onFavorite,
                          Runnable onWrong,
                          Runnable onCollection,
                          Runnable onHistory) {
        this.ctrl         = ctrl;
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
    }

    private JPanel buildDateBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel date = new JLabel(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日")));
        date.setFont(AppColors.FONT_BODY);
        date.setForeground(AppColors.TEXT_SECONDARY);

        p.add(date, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.add(buildNavCards(),  BorderLayout.NORTH);
        p.add(buildFlashCard(), BorderLayout.CENTER);
        return p;
    }

    // ── 4 nav cards ──────────────────────────────────────────────
    private JPanel buildNavCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 110));

        favCountLabel     = new JLabel("0", SwingConstants.CENTER);
        wrongCountLabel   = new JLabel("0", SwingConstants.CENTER);
        colCountLabel     = new JLabel("0", SwingConstants.CENTER);
        historyCountLabel = new JLabel("0", SwingConstants.CENTER);

        row.add(buildNavCard("♥", "#C62828", "Favorite 單字區", favCountLabel,
                "已收藏的單字",       AppColors.TEXT_RED,       onFavorite));
        row.add(buildNavCard("✗", "#F9A825", "錯誤單字區",    wrongCountLabel,
                "答錯次數最多的單字", WRONG_YELLOW,              onWrong));
        row.add(buildNavCard("☰", "#2E7D6E", "Collection",   colCountLabel,
                "自訂單字收藏群組",   COL_TEAL,                  onCollection));
        row.add(buildNavCard("◷", "#1565C0", "History",      historyCountLabel,
                "已練習過的單字",     new Color(0x1565C0),       onHistory));
        return row;
    }

    private JPanel buildNavCard(String icon, String iconColor, String text,
                                JLabel countLabel, String subtitle,
                                Color accent, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(
            "<html><font color='" + iconColor + "'>" + icon + "</font> " + text + "</html>");
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);

        countLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        countLabel.setForeground(accent);
        countLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(AppColors.FONT_SMALL);
        subLbl.setForeground(AppColors.TEXT_SECONDARY);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(countLabel, BorderLayout.WEST);
        bottom.add(subLbl,     BorderLayout.EAST);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bottom,   BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(0xF0E8D8));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(AppColors.BG_CARD);
            }
        });
        return card;
    }

    // ── flash card ───────────────────────────────────────────────
    private JPanel buildFlashCard() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(AppColors.BG_CARD);
        wrapper.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(20, 24, 20, 24)
        ));

        // Header
        JLabel sectionTitle = new JLabel("學習單字卡");
        sectionTitle.setFont(AppColors.FONT_HEAD);
        sectionTitle.setForeground(AppColors.TEXT_PRIMARY);

        cardIndexLabel = new JLabel("0 / 0", SwingConstants.RIGHT);
        cardIndexLabel.setFont(AppColors.FONT_SMALL);
        cardIndexLabel.setForeground(AppColors.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.add(sectionTitle,  BorderLayout.WEST);
        header.add(cardIndexLabel, BorderLayout.EAST);

        // Card center — GridBagLayout keeps positions fixed
        JPanel cardCenter = new JPanel(new GridBagLayout());
        cardCenter.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Word label — always visible, blue, clickable → Cambridge
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        cardWordLabel = new JLabel("", SwingConstants.CENTER);
        cardWordLabel.setFont(new Font("Serif", Font.BOLD, 38));
        cardWordLabel.setForeground(new Color(0x1565C0));
        cardWordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardWordLabel.setToolTipText("點擊開啟 Cambridge Dictionary");
        cardWordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openCambridge(); }
        });
        cardCenter.add(cardWordLabel, gbc);

        // Pos label — always rendered (space preserves row height)
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPosLabel = new JLabel(" ", SwingConstants.CENTER);
        cardPosLabel.setFont(new Font("Microsoft JhengHei", Font.ITALIC, 14));
        cardPosLabel.setForeground(AppColors.TEXT_SECONDARY);
        cardCenter.add(cardPosLabel, gbc);

        // Meaning label — always rendered
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardMeaningLabel = new JLabel(" ", SwingConstants.CENTER);
        cardMeaningLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        cardMeaningLabel.setForeground(AppColors.TEXT_PRIMARY);
        cardCenter.add(cardMeaningLabel, gbc);

        // Action row — all 5 buttons in one FlowLayout row
        heartBtn = buildActionBtn("♥  Favorite", AppColors.TEXT_RED);
        heartBtn.addActionListener(e -> toggleFavorite());
        lockBtnSize(heartBtn, "✓  Favorite");

        colBtn = buildActionBtn("☰  Collection", COL_TEAL);
        colBtn.addActionListener(e -> showCollectionDialog());
        lockBtnSize(colBtn, "✓  Collection");

        JButton prevBtn = buildNavBtn("◀ 上一個", e -> moveCard(-1));
        JButton flipBtn = buildNavBtn("翻面",     e -> flipCard());
        JButton nextBtn = buildNavBtn("下一個 ▶", e -> moveCard(1));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(16, 0, 0, 0));
        actionRow.add(prevBtn);
        actionRow.add(flipBtn);
        actionRow.add(nextBtn);
        actionRow.add(heartBtn);
        actionRow.add(colBtn);

        wrapper.add(header,     BorderLayout.NORTH);
        wrapper.add(cardCenter, BorderLayout.CENTER);
        wrapper.add(actionRow,  BorderLayout.SOUTH);

        return wrapper;
    }

    private JButton buildNavBtn(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setForeground(AppColors.TEXT_PRIMARY);
        b.setBackground(new Color(0xF0E8D8));
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(6, 16, 6, 16)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        return b;
    }

    private static void lockBtnSize(JButton btn, String altText) {
        String orig = btn.getText();
        Dimension d1 = btn.getPreferredSize();
        btn.setText(altText);
        Dimension d2 = btn.getPreferredSize();
        btn.setText(orig);
        Dimension fixed = new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        btn.setPreferredSize(fixed);
        btn.setMinimumSize(fixed);
    }

    private JButton buildActionBtn(String text, Color accent) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setForeground(accent);
        b.setBackground(AppColors.BG_CARD);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new CompoundBorder(
            new LineBorder(accent, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── card logic ───────────────────────────────────────────────
    private void moveCard(int delta) {
        if (cardWords == null || cardWords.isEmpty()) return;
        cardIndex = (cardIndex + delta + cardWords.size()) % cardWords.size();
        showMeaning = false;
        updateCardDisplay();
    }

    private void flipCard() {
        showMeaning = !showMeaning;
        updateCardDisplay();
    }

    private Vocabulary getCurrentVocab() {
        if (cardWords == null || cardWords.isEmpty()) return null;
        return cardWords.get(cardIndex);
    }

    private void toggleFavorite() {
        Vocabulary v = getCurrentVocab();
        if (v == null) return;
        ctrl.toggleFavorite(v, !v.isFavorite());
        updateIconBtns(v);
        favCountLabel.setText(String.valueOf(ctrl.getFavoriteWords().size()));
    }

    private void openCambridge() {
        Vocabulary v = getCurrentVocab();
        if (v == null) return;
        try {
            Desktop.getDesktop().browse(
                new URI("https://dictionary.cambridge.org/dictionary/english/" + v.getWord()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "無法開啟瀏覽器，請手動前往：\nhttps://dictionary.cambridge.org/dictionary/english/" + v.getWord(),
                "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateCardDisplay() {
        Vocabulary v = getCurrentVocab();
        if (v == null) {
            cardWordLabel.setText("（無單字）");
            cardPosLabel.setText(" ");
            cardMeaningLabel.setText(" ");
            cardIndexLabel.setText("0 / 0");
            return;
        }
        cardWordLabel.setText(v.getWord());
        cardIndexLabel.setText((cardIndex + 1) + " / " + cardWords.size());

        if (showMeaning) {
            cardPosLabel.setText("(" + v.getPos() + ")");
            cardMeaningLabel.setText(v.getMeaning());
        } else {
            cardPosLabel.setText("點擊「翻面」查看解釋");
            cardMeaningLabel.setText(" ");
        }
        updateIconBtns(v);
    }

    private void updateIconBtns(Vocabulary v) {
        if (heartBtn == null || colBtn == null) return;

        if (v.isFavorite()) {
            heartBtn.setText("✓  Favorite");
            heartBtn.setForeground(Color.WHITE);
            heartBtn.setBackground(AppColors.TEXT_RED);
        } else {
            heartBtn.setText("♥  Favorite");
            heartBtn.setForeground(AppColors.TEXT_RED);
            heartBtn.setBackground(AppColors.BG_CARD);
        }

        boolean inCol = ctrl.getCollections().stream()
                .anyMatch(c -> c.containsWord(v.getWord()));
        if (inCol) {
            colBtn.setText("✓  Collection");
            colBtn.setForeground(Color.WHITE);
            colBtn.setBackground(COL_TEAL);
        } else {
            colBtn.setText("☰  Collection");
            colBtn.setForeground(COL_TEAL);
            colBtn.setBackground(AppColors.BG_CARD);
        }
    }

    // ── collection chooser dialog ─────────────────────────────────
    private void showCollectionDialog() {
        Vocabulary v = getCurrentVocab();
        if (v == null) return;

        Window w = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(400, 360);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(24, 28, 20, 28)
        ));

        JLabel titleLbl = new JLabel("加入 Collection 群組");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Existing collections with checkboxes (multi-select)
        List<VocabCollection> cols = ctrl.getCollections();
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setOpaque(false);
        checkPanel.setBorder(new EmptyBorder(4, 6, 4, 4));

        final JCheckBox[] checks = new JCheckBox[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            boolean alreadyIn = cols.get(i).containsWord(v.getWord());
            checks[i] = new JCheckBox(
                "  " + cols.get(i).getName() + "  (" + cols.get(i).getWords().size() + " 個單字)");
            checks[i].setFont(AppColors.FONT_BODY);
            checks[i].setOpaque(false);
            checks[i].setFocusPainted(false);
            checks[i].setSelected(alreadyIn);
            checkPanel.add(checks[i]);
        }

        JScrollPane radioScroll = UIUtils.styledScroll(checkPanel);
        radioScroll.setBorder(new LineBorder(AppColors.BORDER_SOFT, 1));
        radioScroll.getViewport().setBackground(AppColors.BG_CARD);

        // New group text field (styled)
        JLabel newLbl = new JLabel("新增群組並加入：");
        newLbl.setFont(AppColors.FONT_BODY);
        newLbl.setForeground(AppColors.TEXT_SECONDARY);

        JTextField newField = new JTextField();
        newField.setFont(AppColors.FONT_BODY);
        newField.setBackground(AppColors.BG_CARD);
        newField.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel newRow = new JPanel(new BorderLayout(0, 6));
        newRow.setOpaque(false);
        newRow.setBorder(new EmptyBorder(12, 0, 10, 0));
        newRow.add(newLbl,    BorderLayout.NORTH);
        newRow.add(newField,  BorderLayout.CENTER);

        // Buttons
        JButton removeBtn = dlgBtn("取消群組", AppColors.TEXT_RED, AppColors.BG_MAIN, AppColors.TEXT_RED);
        removeBtn.addActionListener(e -> {
            ctrl.getCollections().forEach(c -> c.removeWord(v.getWord()));
            ctrl.saveCollections();
            updateIconBtns(v);
            colCountLabel.setText(String.valueOf(ctrl.getCollections().size()));
            dlg.dispose();
        });

        JButton okBtn = dlgBtn("確認加入", Color.WHITE, AppColors.BORDER, AppColors.BORDER);
        okBtn.setOpaque(true);
        okBtn.addActionListener(e -> {
            String newName = newField.getText().trim();
            if (!newName.isEmpty()) {
                VocabCollection col = new VocabCollection(newName);
                col.addWord(v.getWord());
                ctrl.addCollection(col);
            }
            for (int i = 0; i < cols.size(); i++) {
                if (checks[i].isSelected()) {
                    cols.get(i).addWord(v.getWord());
                } else {
                    cols.get(i).removeWord(v.getWord());
                }
            }
            ctrl.saveCollections();
            updateIconBtns(v);
            colCountLabel.setText(String.valueOf(ctrl.getCollections().size()));
            dlg.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(removeBtn);
        btnRow.add(okBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(newRow,  BorderLayout.NORTH);
        south.add(btnRow,  BorderLayout.SOUTH);

        content.add(titleLbl,    BorderLayout.NORTH);
        content.add(radioScroll,  BorderLayout.CENTER);
        content.add(south,        BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private static JButton dlgBtn(String text, Color fg, Color bg, Color border) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(5, 14, 5, 14)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void refresh() {
        cardWords = ctrl.getVocabList();
        if (cardIndex >= cardWords.size()) cardIndex = 0;
        showMeaning = false;
        updateCardDisplay();

        favCountLabel.setText(String.valueOf(ctrl.getFavoriteWords().size()));
        wrongCountLabel.setText(String.valueOf(ctrl.getWrongWords().size()));
        colCountLabel.setText(String.valueOf(ctrl.getCollections().size()));
        historyCountLabel.setText(String.valueOf(ctrl.getHistoryWords().size()));
    }
}
