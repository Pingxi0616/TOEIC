package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class VocabManagerPanel extends JPanel {

    private final DashboardController ctrl;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalLabel, wrongLabel;
    private JTextField searchField;

    // 左側資料夾面板
    private JPanel folderBtnArea;
    private static final String FOLDER_ALL   = "__ALL__";    // 全部單字
    private static final String FOLDER_TOEIC = "__TOEIC__";  // TOEIC 多益單字
    private String selectedFolder = FOLDER_ALL;

    public VocabManagerPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    // ── 頂部標題列 ────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("單字庫管理");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setFont(AppColors.FONT_BODY);
        searchField.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "搜尋單字…");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(searchField.getText()); }
        });

        JButton btnAdd     = accentBtn("＋ 新增單字");
        JButton btnWrong   = sBtn("查看錯題本");
        JButton btnRefresh = sBtn("重新整理");
        btnRefresh.setToolTipText("清除搜尋並重新從資料庫載入（可復原未儲存的操作錯誤）");

        btnAdd.addActionListener(e -> showAddWordDialog());
        btnWrong.addActionListener(e -> showWrongWords());
        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            selectedFolder = FOLDER_ALL;
            ctrl.reload();   // 從 JSON 重新載入
            refresh();
        });

        right.add(searchField);
        right.add(btnAdd);
        right.add(btnWrong);
        right.add(btnRefresh);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── 主內容（左欄資料夾 + 右側表格） ──────────────────────
    private JPanel buildContent() {
        JPanel container = new JPanel(new BorderLayout(12, 0));
        container.setOpaque(false);
        container.add(buildFolderPanel(), BorderLayout.WEST);
        container.add(buildTableCard(),   BorderLayout.CENTER);
        return container;
    }

    // ── 左側資料夾面板 ────────────────────────────────────────
    private JPanel buildFolderPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setPreferredSize(new Dimension(190, 0));
        outer.setBackground(AppColors.BG_CARD);
        outer.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(14, 10, 10, 10)
        ));

        JLabel lbl = new JLabel("資料夾");
        lbl.setFont(AppColors.FONT_HEAD);
        lbl.setForeground(AppColors.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 2, 6, 0));

        folderBtnArea = new JPanel();
        folderBtnArea.setLayout(new BoxLayout(folderBtnArea, BoxLayout.Y_AXIS));
        folderBtnArea.setOpaque(false);

        JScrollPane scroll = new JScrollPane(folderBtnArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JButton addFolderBtn = new JButton("＋ 新增資料夾");
        addFolderBtn.setFont(AppColors.FONT_SMALL);
        addFolderBtn.setBackground(AppColors.BG_SIDEBAR);
        addFolderBtn.setForeground(AppColors.TEXT_PRIMARY);
        addFolderBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        addFolderBtn.setFocusPainted(false);
        addFolderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addFolderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFolderBtn.addActionListener(e -> showAddCollectionDialog());

        outer.add(lbl,          BorderLayout.NORTH);
        outer.add(scroll,       BorderLayout.CENTER);
        outer.add(addFolderBtn, BorderLayout.SOUTH);
        return outer;
    }

    // 重繪資料夾按鈕清單
    private void refreshFolders() {
        folderBtnArea.removeAll();

        // 固定資料夾：全部單字 & TOEIC 多益單字
        folderBtnArea.add(folderBtn(
            "全部單字 (" + ctrl.getTotalCount() + ")", FOLDER_ALL));
        folderBtnArea.add(Box.createVerticalStrut(4));
        folderBtnArea.add(folderBtn(
            "TOEIC 多益單字 (" + ctrl.getToeicCount() + ")", FOLDER_TOEIC));
        folderBtnArea.add(Box.createVerticalStrut(4));

        // 使用者自訂資料夾
        for (VocabCollection col : ctrl.getCollections()) {
            int cnt = ctrl.getCollectionWords(col).size();
            folderBtnArea.add(folderBtn(col.getName() + " (" + cnt + ")", col.getName()));
            folderBtnArea.add(Box.createVerticalStrut(4));
        }

        folderBtnArea.revalidate();
        folderBtnArea.repaint();
    }

    private JButton folderBtn(String text, String colName) {
        boolean selected = colName != null && colName.equals(selectedFolder);
        boolean isUserFolder = colName != null
            && !FOLDER_ALL.equals(colName) && !FOLDER_TOEIC.equals(colName);

        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_SMALL);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(selected ? AppColors.BG_SELECTED : AppColors.BG_CARD);
        b.setForeground(AppColors.TEXT_PRIMARY);
        b.setBorder(new CompoundBorder(
            new LineBorder(selected ? new Color(0x7ABAAA) : new Color(0, 0, 0, 0), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        if (isUserFolder) {
            b.setToolTipText("右鍵可重新命名或刪除");
        }
        b.addActionListener(e -> {
            selectedFolder = colName;
            filterByFolder();
            refreshFolders();
        });

        // 右鍵選單：僅使用者自訂資料夾
        if (isUserFolder) {
            b.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed (MouseEvent e) { maybeShowFolderPopup(e, colName); }
                @Override public void mouseReleased(MouseEvent e) { maybeShowFolderPopup(e, colName); }
                private void maybeShowFolderPopup(MouseEvent e, String name) {
                    if (!e.isPopupTrigger()) return;
                    JPopupMenu menu = new JPopupMenu();
                    menu.setBackground(AppColors.BG_CARD);
                    menu.setBorder(new CompoundBorder(
                        new LineBorder(AppColors.BORDER_SOFT, 1, true),
                        new EmptyBorder(2, 0, 2, 0)
                    ));

                    JMenuItem renameItem = new JMenuItem("重新命名");
                    renameItem.setFont(AppColors.FONT_BODY);
                    renameItem.setForeground(AppColors.TEXT_PRIMARY);
                    renameItem.setBackground(AppColors.BG_CARD);
                    renameItem.setBorder(new EmptyBorder(6, 16, 6, 16));
                    renameItem.setOpaque(true);
                    renameItem.addActionListener(ev -> renameCollection(name));

                    JMenuItem deleteItem = new JMenuItem("刪除資料夾");
                    deleteItem.setFont(AppColors.FONT_BODY);
                    deleteItem.setForeground(AppColors.TEXT_RED);
                    deleteItem.setBackground(AppColors.BG_CARD);
                    deleteItem.setBorder(new EmptyBorder(6, 16, 6, 16));
                    deleteItem.setOpaque(true);
                    deleteItem.addActionListener(ev -> deleteCollection(name));

                    JSeparator sep = new JSeparator();
                    sep.setForeground(AppColors.BORDER_SOFT);
                    sep.setBackground(AppColors.BG_CARD);
                    menu.add(renameItem);
                    menu.add(sep);
                    menu.add(deleteItem);
                    menu.show(b, e.getX(), e.getY());
                }
            });
        }
        return b;
    }

    // ── 重新命名資料夾 ────────────────────────────────────────
    private void renameCollection(String oldName) {
        String input = UIUtils.showInput(this, "請輸入新的資料夾名稱：", "重新命名「" + oldName + "」");
        if (input == null || input.trim().isEmpty()) return;
        String newName = input.trim();
        if (newName.equals(oldName)) return;

        boolean dup = ctrl.getCollections().stream()
            .anyMatch(c -> c.getName().equals(newName));
        if (dup) {
            UIUtils.showMessage(this, "「" + newName + "」資料夾已存在！", "重複名稱");
            return;
        }

        ctrl.getCollections().stream()
            .filter(c -> c.getName().equals(oldName))
            .findFirst()
            .ifPresent(col -> col.setName(newName));

        ctrl.saveCollections();
        if (oldName.equals(selectedFolder)) selectedFolder = newName;
        refresh();
    }

    // ── 刪除資料夾 ────────────────────────────────────────────
    private void deleteCollection(String colName) {
        if (!UIUtils.showConfirm(this,
                "確定要刪除「" + colName + "」資料夾嗎？\n（資料夾內的單字不會被刪除，只是移除資料夾）",
                "確認刪除")) return;

        ctrl.getCollections().stream()
            .filter(c -> c.getName().equals(colName))
            .findFirst()
            .ifPresent(ctrl::removeCollection);

        if (colName.equals(selectedFolder)) selectedFolder = FOLDER_ALL;
        refresh();
    }

    // ── 右側表格卡片 ──────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel statRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        statRow.setOpaque(false);
        totalLabel = new JLabel();
        wrongLabel = new JLabel();
        totalLabel.setFont(AppColors.FONT_BODY);
        wrongLabel.setFont(AppColors.FONT_BODY);
        wrongLabel.setForeground(AppColors.TEXT_RED);
        statRow.add(totalLabel);
        statRow.add(new JLabel(" | "));
        statRow.add(wrongLabel);

        // 右鍵提示
        JLabel hint = new JLabel("右鍵單字列可編輯／刪除");
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        statRow.add(Box.createHorizontalStrut(20));
        statRow.add(hint);

        String[] cols = {"單字","詞性","詞義","熟悉度","答對","答錯","下次複習"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(AppColors.FONT_BODY);
        table.setRowHeight(26);
        table.setGridColor(new Color(0xE8E8E8));
        table.setSelectionBackground(new Color(0xD8EED8));
        table.getTableHeader().setFont(AppColors.FONT_BODY);
        table.getTableHeader().setBackground(new Color(0xF0E8D8));
        table.setAutoCreateRowSorter(true);

        int[] widths = {110, 60, 150, 90, 50, 50, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ── 右鍵選單（編輯 / 刪除） ──
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) { maybeShowPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
            private void maybeShowPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                table.setRowSelectionInterval(row, row);
                int modelRow = table.convertRowIndexToModel(row);
                String word = (String) tableModel.getValueAt(modelRow, 0);
                Vocabulary v = ctrl.getVocabList().stream()
                    .filter(x -> x.getWord().equals(word)).findFirst().orElse(null);
                if (v == null) return;
                showRowPopup(e, v);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(AppColors.BORDER_SOFT));

        card.add(statRow, BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        return card;
    }

    // ── 右鍵彈出選單 ──────────────────────────────────────────
    private void showRowPopup(MouseEvent e, Vocabulary v) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("編輯單字");
        editItem.setFont(AppColors.FONT_BODY);
        editItem.addActionListener(ev -> showEditWordDialog(v));

        JMenuItem deleteItem = new JMenuItem("刪除單字");
        deleteItem.setFont(AppColors.FONT_BODY);
        deleteItem.setForeground(AppColors.TEXT_RED);
        deleteItem.addActionListener(ev -> {
            if (!v.isCustom()) {
                UIUtils.showMessage(this,
                    "「" + v.getWord() + "」是 TOEIC 原始單字，無法刪除。\n只有自訂新增的單字才可以刪除。",
                    "無法刪除");
                return;
            }
            if (!UIUtils.showConfirm(this,
                    "確定要從單字庫刪除「" + v.getWord() + "」嗎？\n（同時會從所有資料夾中移除）",
                    "確認刪除")) return;
            ctrl.getVocabList().remove(v);
            for (VocabCollection col : ctrl.getCollections())
                col.removeWord(v.getWord());
            ctrl.save();
            ctrl.saveCollections();
            refresh();
        });

        menu.add(editItem);
        menu.addSeparator();
        menu.add(deleteItem);
        menu.show(table, e.getX(), e.getY());
    }

    // ── 刷新 ──────────────────────────────────────────────────
    public void refresh() {
        refreshFolders();
        filterByFolder();
    }

    // ── 依資料夾篩選表格 ──────────────────────────────────────
    private void filterByFolder() {
        if (FOLDER_ALL.equals(selectedFolder)) {
            loadTableData(ctrl.getVocabList());
        } else if (FOLDER_TOEIC.equals(selectedFolder)) {
            loadTableData(ctrl.getToeicWords());
        } else {
            VocabCollection col = ctrl.getCollections().stream()
                .filter(c -> c.getName().equals(selectedFolder))
                .findFirst().orElse(null);
            if (col != null) loadTableData(ctrl.getCollectionWords(col));
            else             { selectedFolder = FOLDER_ALL; loadTableData(ctrl.getVocabList()); }
        }
    }

    // ── 搜尋篩選 ──────────────────────────────────────────────
    private void filterTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) { filterByFolder(); return; }
        String kw = keyword.trim().toLowerCase();
        List<Vocabulary> base = currentBaseList();
        List<Vocabulary> filtered = base.stream()
            .filter(v -> v.getWord().toLowerCase().contains(kw)
                      || (v.getMeaning() != null && v.getMeaning().contains(kw)))
            .collect(Collectors.toList());
        loadTableData(filtered);
    }

    private List<Vocabulary> currentBaseList() {
        if (FOLDER_ALL.equals(selectedFolder))   return ctrl.getVocabList();
        if (FOLDER_TOEIC.equals(selectedFolder)) return ctrl.getToeicWords();
        VocabCollection col = ctrl.getCollections().stream()
            .filter(c -> c.getName().equals(selectedFolder))
            .findFirst().orElse(null);
        return col != null ? ctrl.getCollectionWords(col) : ctrl.getVocabList();
    }

    private void loadTableData(List<Vocabulary> list) {
        if (list == null) list = ctrl.getVocabList();
        tableModel.setRowCount(0);
        for (Vocabulary v : list) {
            tableModel.addRow(new Object[]{
                v.getWord(),
                v.getPos() != null ? v.getPos() : "",
                v.getMeaning(),
                v.getFamiliarityStars(),
                v.getCorrectCount(),
                v.getWrongCount(),
                v.getNextReviewDate() != null ? v.getNextReviewDate() : "—"
            });
        }
        totalLabel.setText("顯示 " + list.size() + " / " + ctrl.getTotalCount() + " 筆單字");
        long wc = ctrl.getVocabList().stream().filter(v -> v.getWrongCount() > 0).count();
        wrongLabel.setText("有錯誤記錄：" + wc + " 個");
    }

    // ── 新增單字對話框 ────────────────────────────────────────
    private void showAddWordDialog() {
        JDialog dlg = newDialog("新增單字", 420, 310);

        JTextField wordField      = styledTF();
        JTextField meaningField   = styledTF();
        JTextField phraseField    = styledTF();
        JTextField phraseMeanField = styledTF();

        String[] posOpts = {"n.","v.","adj.","adv.","prep.","conj.",
                            "pron.","interj.","n./v.","adj./n.","prep./adv."};
        JComboBox<String> posCombo = posCombo(posOpts);

        JPanel form = buildFormPanel(5);
        form.add(formLabel("英文單字 *"));    form.add(wordField);
        form.add(formLabel("詞性"));          form.add(posCombo);
        form.add(formLabel("中文詞義 *"));    form.add(meaningField);
        form.add(formLabel("相關片語"));      form.add(phraseField);
        form.add(formLabel("片語中文意思"));  form.add(phraseMeanField);

        // 顯示目前選中的資料夾（如果有的話）
        JLabel folderHint = new JLabel(selectedFolder != null
            ? "  新增後將自動加入「" + selectedFolder + "」資料夾" : "");
        folderHint.setFont(AppColors.FONT_SMALL);
        folderHint.setForeground(new Color(0x2E7D6E));

        JButton addBtn = accentBtn("確認新增");
        JButton cancelBtn = sBtn("取消");
        cancelBtn.addActionListener(e -> dlg.dispose());

        addBtn.addActionListener(e -> {
            String word    = wordField.getText().trim();
            String meaning = meaningField.getText().trim();
            String pos     = posCombo.getSelectedItem() != null
                             ? posCombo.getSelectedItem().toString().trim() : "";
            if (word.isEmpty() || meaning.isEmpty()) {
                UIUtils.showMessage(dlg, "英文單字和中文詞義不能為空！", "欄位不完整");
                return;
            }
            boolean exists = ctrl.getVocabList().stream()
                .anyMatch(v -> v.getWord().equalsIgnoreCase(word));
            if (exists) {
                UIUtils.showMessage(dlg, "「" + word + "」已存在於單字庫中！", "重複單字");
                return;
            }
            Vocabulary v = new Vocabulary(word, meaning, pos, "");
            v.setCustom(true);   // 使用者自訂新增的單字
            String phrase  = phraseField.getText().trim();
            String phraseM = phraseMeanField.getText().trim();
            if (!phrase.isEmpty())  v.setPhrase(phrase);
            if (!phraseM.isEmpty()) v.setPhraseMeaning(phraseM);

            ctrl.getVocabList().add(v);

            // 自動加入目前選中的資料夾（只有在 user collection 時才加）
            if (selectedFolder != null && !FOLDER_ALL.equals(selectedFolder) && !FOLDER_TOEIC.equals(selectedFolder)) {
                ctrl.getCollections().stream()
                    .filter(c -> c.getName().equals(selectedFolder))
                    .findFirst()
                    .ifPresent(col -> {
                        col.addWord(word);
                        ctrl.saveCollections();
                    });
            }
            ctrl.save();
            dlg.dispose();
            refresh();
            UIUtils.showMessage(VocabManagerPanel.this,
                "已新增「" + word + "」！" + (selectedFolder != null
                    && !FOLDER_ALL.equals(selectedFolder) && !FOLDER_TOEIC.equals(selectedFolder)
                    ? "\n同時加入「" + selectedFolder + "」資料夾。" : ""),
                "新增成功");
        });

        layoutDialog(dlg, form, folderHint, cancelBtn, addBtn);
    }

    // ── 編輯單字對話框 ────────────────────────────────────────
    private void showEditWordDialog(Vocabulary v) {
        JDialog dlg = newDialog("編輯單字", 420, 310);

        JTextField wordField      = styledTF();
        JTextField meaningField   = styledTF();
        JTextField phraseField    = styledTF();
        JTextField phraseMeanField = styledTF();

        String[] posOpts = {"n.","v.","adj.","adv.","prep.","conj.",
                            "pron.","interj.","n./v.","adj./n.","prep./adv."};
        JComboBox<String> posCombo = posCombo(posOpts);

        // 預填現有資料
        wordField.setText(v.getWord() != null      ? v.getWord()         : "");
        meaningField.setText(v.getMeaning() != null ? v.getMeaning()      : "");
        phraseField.setText(v.getPhrase() != null   ? v.getPhrase()       : "");
        phraseMeanField.setText(v.getPhraseMeaning() != null
                                                    ? v.getPhraseMeaning() : "");
        posCombo.setSelectedItem(v.getPos() != null ? v.getPos() : "");

        JPanel form = buildFormPanel(5);
        form.add(formLabel("英文單字 *"));    form.add(wordField);
        form.add(formLabel("詞性"));          form.add(posCombo);
        form.add(formLabel("中文詞義 *"));    form.add(meaningField);
        form.add(formLabel("相關片語"));      form.add(phraseField);
        form.add(formLabel("片語中文意思"));  form.add(phraseMeanField);

        JButton saveBtn   = accentBtn("儲存修改");
        JButton cancelBtn = sBtn("取消");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String newWord    = wordField.getText().trim();
            String newMeaning = meaningField.getText().trim();
            String newPos     = posCombo.getSelectedItem() != null
                                ? posCombo.getSelectedItem().toString().trim() : "";
            if (newWord.isEmpty() || newMeaning.isEmpty()) {
                UIUtils.showMessage(dlg, "英文單字和中文詞義不能為空！", "欄位不完整");
                return;
            }
            // 如果單字名稱改變，同步更新所有 Collection 中的紀錄
            String oldWord = v.getWord();
            if (!newWord.equals(oldWord)) {
                boolean dup = ctrl.getVocabList().stream()
                    .anyMatch(x -> x != v && x.getWord().equalsIgnoreCase(newWord));
                if (dup) {
                    UIUtils.showMessage(dlg, "「" + newWord + "」已存在！", "重複");
                    return;
                }
                for (VocabCollection col : ctrl.getCollections()) {
                    if (col.containsWord(oldWord)) {
                        col.removeWord(oldWord);
                        col.addWord(newWord);
                    }
                }
                ctrl.saveCollections();
            }
            v.setWord(newWord);
            v.setMeaning(newMeaning);
            v.setPos(newPos);
            v.setPhrase(phraseField.getText().trim());
            v.setPhraseMeaning(phraseMeanField.getText().trim());
            ctrl.save();
            dlg.dispose();
            refresh();
        });

        layoutDialog(dlg, form, new JLabel(""), cancelBtn, saveBtn);
    }

    // ── 新增資料夾 ────────────────────────────────────────────
    private void showAddCollectionDialog() {
        String input = UIUtils.showInput(this, "請輸入資料夾名稱：", "新增資料夾");
        if (input == null || input.trim().isEmpty()) return;
        final String colName = input.trim();
        boolean exists = ctrl.getCollections().stream()
            .anyMatch(c -> c.getName().equals(colName));
        if (exists) {
            UIUtils.showMessage(this, "「" + colName + "」資料夾已存在！", "重複");
            return;
        }
        ctrl.addCollection(new VocabCollection(colName));
        selectedFolder = colName;   // 自動切換到新資料夾
        refresh();
    }

    // ── 錯題本彈窗（主題化） ──────────────────────────────────
    private void showWrongWords() {
        List<Vocabulary> wrongs = ctrl.getWrongWords();
        if (wrongs.isEmpty()) {
            UIUtils.showMessage(this, "目前沒有錯題記錄！", "錯題本");
            return;
        }
        // 按錯誤次數降序排列
        wrongs = wrongs.stream()
            .sorted((a, b) -> b.getWrongCount() - a.getWrongCount())
            .collect(Collectors.toList());

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "錯題本", true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(AppColors.BG_MAIN);
        root.setBorder(new EmptyBorder(22, 26, 18, 26));

        // ── 頭部 ──
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel titleLbl = new JLabel("錯題本");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(AppColors.TEXT_RED);
        JLabel subLbl = new JLabel("共 " + wrongs.size() + " 個單字需要加強，依錯誤次數排序");
        subLbl.setFont(AppColors.FONT_SMALL);
        subLbl.setForeground(AppColors.TEXT_SECONDARY);
        head.add(titleLbl, BorderLayout.WEST);
        head.add(subLbl,   BorderLayout.SOUTH);

        // ── 表格 ──
        String[] cols = {"單字", "詞義", "錯誤次數", "熟悉度"};
        Object[][] data = new Object[wrongs.size()][4];
        for (int i = 0; i < wrongs.size(); i++) {
            Vocabulary vv = wrongs.get(i);
            data[i][0] = vv.getWord();
            data[i][1] = vv.getMeaning() != null ? vv.getMeaning() : "";
            data[i][2] = vv.getWrongCount();
            data[i][3] = vv.getFamiliarityStars();
        }
        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 2 ? Integer.class : String.class;
            }
        };
        JTable tbl = new JTable(tm);
        tbl.setFont(AppColors.FONT_BODY);
        tbl.setRowHeight(28);
        tbl.setBackground(AppColors.BG_CARD);
        tbl.setGridColor(new Color(0xE8E8E8));
        tbl.setSelectionBackground(new Color(0xFFE4D0));
        tbl.getTableHeader().setFont(AppColors.FONT_BODY);
        tbl.getTableHeader().setBackground(new Color(0xF0E8D8));
        tbl.getTableHeader().setForeground(AppColors.TEXT_PRIMARY);
        tbl.setAutoCreateRowSorter(true);

        int[] widths = {120, 180, 80, 110};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // 單字欄（紅色）
        tbl.getColumnModel().getColumn(0).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                { setForeground(AppColors.TEXT_RED); }
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setForeground(sel ? Color.WHITE : AppColors.TEXT_RED);
                    setFont(new Font("Serif", Font.BOLD, 13));
                    return this;
                }
            });
        // 錯誤次數欄（紅色居中）
        tbl.getColumnModel().getColumn(2).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setHorizontalAlignment(CENTER);
                    setForeground(sel ? Color.WHITE : AppColors.TEXT_RED);
                    setFont(AppColors.FONT_BTN);
                    setText(v != null ? v + " 次" : "");
                    return this;
                }
            });
        // 熟悉度欄（居中）
        tbl.getColumnModel().getColumn(3).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setHorizontalAlignment(CENTER);
                    setForeground(sel ? Color.WHITE : AppColors.TEXT_SECONDARY);
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(AppColors.BORDER, 2, true));
        scroll.getViewport().setBackground(AppColors.BG_CARD);

        // ── 關閉按鈕 ──
        JButton okBtn = new JButton("關閉");
        okBtn.setFont(AppColors.FONT_BTN);
        okBtn.setBackground(AppColors.BTN_PRIMARY);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(8, 36, 8, 36)
        ));
        okBtn.setFocusPainted(false);
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        btnRow.setOpaque(false);
        btnRow.add(okBtn);

        root.add(head,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setSize(580, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Dialog 工具方法 ───────────────────────────────────────
    private JDialog newDialog(String title, int w, int h) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(AppColors.BG_CARD);
        return dlg;
    }

    private JPanel buildFormPanel(int rows) {
        JPanel form = new JPanel(new GridLayout(rows, 2, 8, 10));
        form.setBackground(AppColors.BG_CARD);
        form.setBorder(new EmptyBorder(20, 24, 6, 24));
        return form;
    }

    private void layoutDialog(JDialog dlg, JPanel form, JLabel hint,
                              JButton cancelBtn, JButton confirmBtn) {
        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        hintRow.setBackground(AppColors.BG_CARD);
        hintRow.add(hint);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnRow.setBackground(AppColors.BG_CARD);
        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(AppColors.BG_CARD);
        south.add(hintRow, BorderLayout.CENTER);
        south.add(btnRow,  BorderLayout.EAST);

        dlg.add(form,  BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── UI 小工具 ─────────────────────────────────────────────
    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_BODY);
        l.setForeground(AppColors.TEXT_PRIMARY);
        return l;
    }
    private JTextField styledTF() {
        JTextField f = new JTextField();
        f.setFont(AppColors.FONT_BODY);
        f.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return f;
    }
    private JComboBox<String> posCombo(String[] opts) {
        JComboBox<String> cb = new JComboBox<>(opts);
        cb.setEditable(true);
        cb.setFont(AppColors.FONT_BODY);
        return cb;
    }
    private JButton accentBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(AppColors.BTN_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(5, 14, 5, 14)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JButton sBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(AppColors.BG_CARD);
        b.setForeground(AppColors.TEXT_PRIMARY);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
