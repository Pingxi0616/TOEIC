package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.Dialog;
import java.awt.Window;
import java.util.List;

public class CollectionPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable onBack;
    private JPanel groupListPanel;
    private JPanel wordListPanel;
    private VocabCollection selectedCol;

    public CollectionPanel(DashboardController ctrl, Runnable onBack) {
        this.ctrl   = ctrl;
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JButton backBtn = buildBackBtn();

        JLabel iconLbl = new JLabel("★");
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        iconLbl.setForeground(new Color(0x2E7D6E));

        JLabel titleLbl = new JLabel(" Collection 收藏群組");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(new Color(0x2E7D6E));

        JPanel title = new JPanel();
        title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
        title.setOpaque(false);
        title.add(iconLbl);
        title.add(titleLbl);

        JButton addBtn = new JButton("＋ 新增群組");
        addBtn.setFont(AppColors.FONT_BTN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(AppColors.BTN_PRIMARY);
        addBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        UIUtils.addHover(addBtn, AppColors.BTN_PRIMARY);
        addBtn.addActionListener(e -> showAddGroupDialog());

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.add(addBtn);
        right.add(Box.createHorizontalStrut(8));
        right.add(backBtn);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JButton buildBackBtn() {
        JButton b = new JButton("返回主頁");
        b.setFont(AppColors.FONT_BTN);
        b.setForeground(AppColors.TEXT_SECONDARY);
        b.setBackground(AppColors.BG_MAIN);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        UIUtils.addHover(b, AppColors.BG_CARD);
        b.addActionListener(e -> onBack.run());
        return b;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setOpaque(false);

        // Left: group list
        JPanel leftCard = new JPanel(new BorderLayout(0, 8));
        leftCard.setBackground(AppColors.BG_CARD);
        leftCard.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        JLabel groupTitle = new JLabel("資料夾");
        groupTitle.setFont(AppColors.FONT_HEAD);
        groupTitle.setForeground(AppColors.TEXT_PRIMARY);
        groupTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        groupListPanel = new JPanel();
        groupListPanel.setLayout(new BoxLayout(groupListPanel, BoxLayout.Y_AXIS));
        groupListPanel.setOpaque(false);
        JScrollPane groupScroll = UIUtils.styledScroll(groupListPanel);
        groupScroll.setBorder(null);

        leftCard.add(groupTitle,  BorderLayout.NORTH);
        leftCard.add(groupScroll, BorderLayout.CENTER);

        // Right: word list in selected group
        JPanel rightCard = new JPanel(new BorderLayout(0, 8));
        rightCard.setBackground(AppColors.BG_CARD);
        rightCard.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel rightTop = new JPanel(new BorderLayout());
        rightTop.setOpaque(false);
        JLabel wordTitle = new JLabel("群組單字");
        wordTitle.setFont(AppColors.FONT_HEAD);
        wordTitle.setForeground(AppColors.TEXT_PRIMARY);

        rightTop.add(wordTitle, BorderLayout.WEST);

        wordListPanel = new JPanel();
        wordListPanel.setLayout(new BoxLayout(wordListPanel, BoxLayout.Y_AXIS));
        wordListPanel.setOpaque(false);
        JScrollPane wordScroll = UIUtils.styledScroll(wordListPanel);
        wordScroll.setBorder(null);

        rightCard.add(rightTop,   BorderLayout.NORTH);
        rightCard.add(wordScroll, BorderLayout.CENTER);

        body.add(leftCard);
        body.add(rightCard);
        return body;
    }

    public void refresh() {
        groupListPanel.removeAll();
        List<VocabCollection> cols = ctrl.getCollections();

        if (cols.isEmpty()) {
            JLabel none = new JLabel("尚無群組，點擊「新增群組」建立");
            none.setFont(AppColors.FONT_BODY);
            none.setForeground(AppColors.TEXT_SECONDARY);
            groupListPanel.add(none);
        } else {
            for (VocabCollection col : cols) {
                groupListPanel.add(buildGroupRow(col));
                groupListPanel.add(Box.createVerticalStrut(6));
            }
        }
        groupListPanel.revalidate();
        groupListPanel.repaint();

        refreshWordList();
    }

    private void refreshWordList() {
        wordListPanel.removeAll();
        if (selectedCol == null) {
            JLabel hint = new JLabel("← 點選左側群組查看單字");
            hint.setFont(AppColors.FONT_BODY);
            hint.setForeground(AppColors.TEXT_SECONDARY);
            wordListPanel.add(hint);
        } else {
            List<String> words = selectedCol.getWords();
            if (words.isEmpty()) {
                JLabel none = new JLabel("此群組尚無單字");
                none.setFont(AppColors.FONT_BODY);
                none.setForeground(AppColors.TEXT_SECONDARY);
                wordListPanel.add(none);
            } else {
                for (String word : words) {
                    wordListPanel.add(buildWordRow(word));
                    wordListPanel.add(Box.createVerticalStrut(6));
                }
            }
        }
        wordListPanel.revalidate();
        wordListPanel.repaint();
    }

    private JPanel buildGroupRow(VocabCollection col) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        boolean selected = col == selectedCol;
        row.setBackground(selected ? AppColors.BG_SELECTED : new Color(0xF0E8D8));
        row.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 8)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLbl = new JLabel(col.getName() + "  (" + col.getWords().size() + ")");
        nameLbl.setFont(AppColors.FONT_BODY);
        nameLbl.setForeground(AppColors.TEXT_PRIMARY);

        JButton delBtn = new JButton("刪除");
        delBtn.setFont(AppColors.FONT_SMALL);
        delBtn.setForeground(AppColors.TEXT_RED);
        delBtn.setBackground(row.getBackground());
        delBtn.setBorder(new EmptyBorder(2, 6, 2, 6));
        delBtn.setFocusPainted(false);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            if (showConfirmDialog("確定刪除群組「" + col.getName() + "」？")) {
                if (selectedCol == col) selectedCol = null;
                ctrl.removeCollection(col);
                refresh();
            }
        });

        row.add(nameLbl, BorderLayout.CENTER);
        row.add(delBtn,  BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedCol = col;
                refresh();
            }
        });

        return row;
    }

    private JPanel buildWordRow(String word) {
        Vocabulary vocab = ctrl.getVocabList().stream()
                .filter(v -> v.getWord().equals(word))
                .findFirst().orElse(null);

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(AppColors.BG_CARD);
        row.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        String wordLine = vocab != null
            ? word + "  " + vocab.getFamiliarityStars()
            : word;
        JLabel wordLbl = new JLabel(wordLine);
        wordLbl.setFont(AppColors.FONT_HEAD);
        wordLbl.setForeground(AppColors.TEXT_PRIMARY);

        String detailLine = vocab != null
            ? "(" + vocab.getPos() + ")  " + vocab.getMeaning()
            : "";
        JLabel detailLbl = new JLabel(detailLine);
        detailLbl.setFont(AppColors.FONT_BODY);
        detailLbl.setForeground(AppColors.TEXT_SECONDARY);

        info.add(wordLbl);
        info.add(detailLbl);

        if (vocab != null) {
            final Vocabulary finalVocab = vocab;
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    WordCardPopup.show(CollectionPanel.this, finalVocab);
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    row.setBackground(new Color(0xD3E2DA));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    row.setBackground(AppColors.BG_CARD);
                }
            });
        }

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnGroup.setOpaque(false);

        JButton moveBtn = new JButton("移動");
        moveBtn.setFont(AppColors.FONT_SMALL);
        moveBtn.setForeground(new Color(0x5C6BC0));
        moveBtn.setBackground(AppColors.BG_CARD);
        moveBtn.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x5C6BC0), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        moveBtn.setFocusPainted(false);
        moveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        moveBtn.addActionListener(e -> showMoveWordDialog(word));

        JButton rmBtn = new JButton("移除");
        rmBtn.setFont(AppColors.FONT_SMALL);
        rmBtn.setForeground(AppColors.TEXT_RED);
        rmBtn.setBackground(AppColors.BG_CARD);
        rmBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.TEXT_RED, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        rmBtn.setFocusPainted(false);
        rmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rmBtn.addActionListener(e -> {
            selectedCol.removeWord(word);
            ctrl.saveCollections();
            refreshWordList();
        });

        btnGroup.add(moveBtn);
        btnGroup.add(rmBtn);

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnGroup);

        row.add(info,       BorderLayout.CENTER);
        row.add(btnWrapper, BorderLayout.EAST);
        return row;
    }

    private void showMoveWordDialog(String word) {
        if (selectedCol == null) return;
        List<VocabCollection> others = ctrl.getCollections().stream()
                .filter(c -> c != selectedCol)
                .collect(java.util.stream.Collectors.toList());

        if (others.isEmpty()) {
            UIUtils.showMessage(this, "沒有其他群組可以移動，請先新增群組。", "提示");
            return;
        }

        String[] options = others.stream()
                .map(c -> c.getName() + " (" + c.getWords().size() + " 個單字)")
                .toArray(String[]::new);

        // ── 自訂主題 Dialog ──────────────────────────────────
        Window w = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));

        JLabel titleLbl = new JLabel("移動單字");
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel msgLbl = new JLabel("將「" + word + "」移動到：");
        msgLbl.setFont(AppColors.FONT_BODY);
        msgLbl.setForeground(AppColors.TEXT_SECONDARY);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setOpaque(false);
        north.add(titleLbl, BorderLayout.NORTH);
        north.add(msgLbl,   BorderLayout.SOUTH);

        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(AppColors.FONT_BODY);
        combo.setBackground(AppColors.BG_CARD);
        combo.setForeground(AppColors.TEXT_PRIMARY);
        combo.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(3, 6, 3, 6)
        ));

        VocabCollection[] chosen = {null};

        JButton cancelBtn = styledDlgBtn("取消", new Color(0x9E8272), Color.WHITE);
        JButton okBtn     = styledDlgBtn("確定", AppColors.BTN_PRIMARY, Color.WHITE);
        cancelBtn.addActionListener(e -> dlg.dispose());
        okBtn.addActionListener(e -> {
            int idx = combo.getSelectedIndex();
            if (idx >= 0) chosen[0] = others.get(idx);
            dlg.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        content.add(north,   BorderLayout.NORTH);
        content.add(combo,   BorderLayout.CENTER);
        content.add(btnRow,  BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(300, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (chosen[0] != null) {
            selectedCol.removeWord(word);
            chosen[0].addWord(word);
            ctrl.saveCollections();
            refreshWordList();
        }
    }

    /** 自訂樣式的確認對話框，回傳 true = 確定 */
    private boolean showConfirmDialog(String message) {
        Window w = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));

        JLabel titleLbl = new JLabel("刪除確認");
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(AppColors.FONT_BODY);
        msgLbl.setForeground(AppColors.TEXT_SECONDARY);

        boolean[] result = {false};

        JButton cancelBtn = styledDlgBtn("取消", new Color(0x9E8272), Color.WHITE);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton okBtn = styledDlgBtn("確定刪除", AppColors.BTN_PRIMARY, Color.WHITE);
        okBtn.addActionListener(e -> { result[0] = true; dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        content.add(titleLbl, BorderLayout.NORTH);
        content.add(msgLbl,   BorderLayout.CENTER);
        content.add(btnRow,   BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(360, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return result[0];
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
        UIUtils.addHover(b, bg);
        return b;
    }

    private void showAddGroupDialog() {
        Window w = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));

        JLabel titleLbl = new JLabel("新增群組");
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel nameLbl = new JLabel("群組名稱：");
        nameLbl.setFont(AppColors.FONT_BODY);
        nameLbl.setForeground(AppColors.TEXT_SECONDARY);

        JTextField nameField = new JTextField();
        nameField.setFont(AppColors.FONT_BODY);
        nameField.setBackground(AppColors.BG_CARD);
        nameField.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(nameLbl);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(nameField);

        JButton cancelBtn = styledDlgBtn("取消", new Color(0x9E8272), Color.WHITE);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton okBtn = styledDlgBtn("確認建立", AppColors.BTN_PRIMARY, Color.WHITE);
        okBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                VocabCollection col = new VocabCollection(name);
                ctrl.addCollection(col);
                selectedCol = col;
                refresh();
            }
            dlg.dispose();
        });

        // Enter 鍵直接確認
        nameField.addActionListener(e -> okBtn.doClick());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);

        content.add(titleLbl,    BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);
        content.add(btnRow,      BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setSize(300, 210);
        dlg.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
        dlg.setVisible(true);
    }

    private static JButton styledBtn(String text, Color fg, Color bg, Color border) {
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

}
