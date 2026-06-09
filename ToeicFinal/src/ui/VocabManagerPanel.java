package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Dialog;
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

        JButton btnAdd      = accentBtn("＋ 新增單字");
        JButton btnWrong    = sBtn("查看錯題本");
        JButton btnResetErr = sBtn("重置錯題本");
        btnResetErr.setToolTipText("清空所有單字的錯誤記錄，重新開始計算");

        btnAdd.addActionListener(e -> showAddWordDialog());
        btnWrong.addActionListener(e -> showWrongWords());
        btnResetErr.addActionListener(e -> {
            if (!UIUtils.showConfirm(this, "確定要清空所有錯誤記錄嗎？\n此操作無法復原。", "重置錯題本")) return;
            ctrl.getVocabList().forEach(v -> v.setWrongCount(0));
            ctrl.save();
            refresh();
            UIUtils.showMessage(this, "已清空所有錯誤記錄！", "重置完成");
        });

        right.add(searchField);
        right.add(btnAdd);
        right.add(btnWrong);
        right.add(btnResetErr);

        SwingUtilities.invokeLater(() -> {
            int h = Math.max(
                btnAdd.getPreferredSize().height,
                searchField.getPreferredSize().height
            );
            Dimension sf = searchField.getPreferredSize();
            searchField.setPreferredSize(new Dimension(sf.width, h));
            searchField.setMinimumSize(new Dimension(sf.width, h));
            searchField.setMaximumSize(new Dimension(sf.width, h));
            for (JButton b : new JButton[]{btnAdd, btnWrong, btnResetErr}) {
                Dimension pd = b.getPreferredSize();
                b.setPreferredSize(new Dimension(pd.width, h));
                b.setMinimumSize(new Dimension(pd.width, h));
                b.setMaximumSize(new Dimension(pd.width, h));
            }
            right.revalidate();
        });

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);
        west.add(title);
        p.add(west,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── 主內容（左欄資料夾 + 右側表格） ──────────────────────
    private JPanel buildContent() {
        JPanel container = new JPanel(new BorderLayout(12, 0));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 6, 0, 0));
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
        addFolderBtn.setFont(AppColors.FONT_BODY);
        addFolderBtn.setBackground(AppColors.BG_SIDEBAR);
        addFolderBtn.setForeground(AppColors.TEXT_PRIMARY);
        addFolderBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        addFolderBtn.setFocusPainted(false);
        addFolderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addFolderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFolderBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { addFolderBtn.setBackground(AppColors.BG_MAIN); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { addFolderBtn.setBackground(AppColors.BG_SIDEBAR); }
        });
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
        b.setFont(AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
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

                    menu.add(renameItem);
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

        JPanel statRow = new JPanel(new BorderLayout());
        statRow.setOpaque(false);

        totalLabel = new JLabel();
        wrongLabel = new JLabel(); // 保留欄位但不顯示
        totalLabel.setFont(AppColors.FONT_BODY);

        JLabel hint = new JLabel("右鍵單字列可編輯／刪除");
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        hint.setHorizontalAlignment(SwingConstants.RIGHT);

        statRow.add(totalLabel, BorderLayout.WEST);
        statRow.add(hint,       BorderLayout.EAST);

        String[] cols = {"單字","詞性","詞義","答對","答錯","熟悉度"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(AppColors.FONT_BODY);
        table.setRowHeight(26);
        table.setGridColor(new Color(0xE8E8E8));
        table.setSelectionBackground(new Color(168, 200, 192, 128));
        table.setSelectionForeground(AppColors.TEXT_PRIMARY);
        table.getTableHeader().setFont(AppColors.FONT_BODY);
        table.getTableHeader().setBackground(new Color(0xF0E8D8));
        table.getTableHeader().setReorderingAllowed(false);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
            new javax.swing.table.TableRowSorter<>(tableModel);
        // 只有「單字」(0) 和「熟悉度」(5) 可排序
        for (int i = 0; i < cols.length; i++)
            sorter.setSortable(i, i == 0 || i == 5);
        table.setRowSorter(sorter);

        int[] widths = {110, 60, 180, 50, 50, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // 所有欄位加左 padding
        javax.swing.table.DefaultTableCellRenderer paddedLeft =
            new javax.swing.table.DefaultTableCellRenderer();
        paddedLeft.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        // 單字、詞性、詞義 欄：左對齊小距離
        for (int i = 0; i < 3; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(paddedLeft);
        // 答對、答錯、熟悉度 欄：置中
        javax.swing.table.DefaultTableCellRenderer centerRenderer =
            new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 3; i < cols.length; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

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
        menu.setBackground(AppColors.BG_CARD);
        menu.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(2, 0, 2, 0)
        ));

        JMenuItem editItem = new JMenuItem("編輯單字");
        editItem.setFont(AppColors.FONT_BODY);
        editItem.setForeground(AppColors.TEXT_PRIMARY);
        editItem.setBackground(AppColors.BG_CARD);
        editItem.setBorder(new EmptyBorder(6, 16, 6, 16));
        editItem.setOpaque(true);
        editItem.addActionListener(ev -> showEditWordDialog(v));

        JMenuItem deleteItem = new JMenuItem("刪除單字");
        deleteItem.setFont(AppColors.FONT_BODY);
        deleteItem.setForeground(AppColors.TEXT_RED);
        deleteItem.setBackground(AppColors.BG_CARD);
        deleteItem.setBorder(new EmptyBorder(6, 16, 6, 16));
        deleteItem.setOpaque(true);
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
                v.getCorrectCount(),
                v.getWrongCount(),
                v.getFamiliarityStars()
            });
        }
        totalLabel.setText("顯示 " + list.size() + " / " + ctrl.getTotalCount() + " 筆單字");
        long wc = ctrl.getVocabList().stream().filter(v -> v.getWrongCount() > 0).count();
        wrongLabel.setText("有錯誤記錄：" + wc + " 個");
    }

    // ── 新增單字對話框 ────────────────────────────────────────
    private void showAddWordDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JTextField wordField    = styledTF();
        JTextField meaningField = styledTF();
        JTextField exampleField = styledTF();

        String[] posOpts = {"n.","v.","adj.","adv.","prep.","conj.",
                            "pron.","interj.","n./v.","adj./n.","prep./adv."};
        JComboBox<String> posCombo = posCombo(posOpts);

        // 標題
        JLabel titleLbl = new JLabel("新增單字");
        titleLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        // 表單
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 8, 0));
        form.add(formLabelRequired("英文單字"));  form.add(wordField);
        form.add(formLabelRequired("詞性"));     form.add(posCombo);
        form.add(formLabelRequired("中文詞義")); form.add(meaningField);
        form.add(formLabelRequired("例句"));     form.add(exampleField);

        // 提示：顯示目標資料夾名稱
        String displayFolder = FOLDER_ALL.equals(selectedFolder)   ? "全部單字"
                             : FOLDER_TOEIC.equals(selectedFolder) ? "TOEIC 多益單字"
                             : selectedFolder != null              ? selectedFolder : "全部單字";
        JLabel folderHint = new JLabel("新增後將自動加入「" + displayFolder + "」資料夾");
        folderHint.setFont(AppColors.FONT_SMALL);
        folderHint.setForeground(new Color(0x2E7D6E));

        // 按鈕
        JButton addBtn    = UIUtils.styledDialogBtn("確認新增", AppColors.BTN_PRIMARY, Color.WHITE);
        JButton cancelBtn = UIUtils.styledDialogBtn("取消", new Color(0x9E8272), Color.WHITE);
        cancelBtn.addActionListener(e -> dlg.dispose());

        addBtn.addActionListener(e -> {
            String word    = wordField.getText().trim();
            String meaning = meaningField.getText().trim();
            String example = exampleField.getText().trim();
            String pos     = posCombo.getSelectedItem() != null
                             ? posCombo.getSelectedItem().toString().trim() : "";
            if (word.isEmpty() || meaning.isEmpty() || example.isEmpty()) {
                UIUtils.showMessage(dlg, "英文單字、中文詞義和例句均不能為空！", "欄位不完整");
                return;
            }
            boolean exists = ctrl.getVocabList().stream()
                .anyMatch(v -> v.getWord().equalsIgnoreCase(word));
            if (exists) {
                UIUtils.showMessage(dlg, "「" + word + "」已存在於單字庫中！", "重複單字");
                return;
            }
            Vocabulary v = new Vocabulary(word, meaning, pos, example);
            v.setCustom(true);
            ctrl.getVocabList().add(v);
            if (selectedFolder != null && !FOLDER_ALL.equals(selectedFolder) && !FOLDER_TOEIC.equals(selectedFolder)) {
                ctrl.getCollections().stream()
                    .filter(c -> c.getName().equals(selectedFolder))
                    .findFirst()
                    .ifPresent(col -> { col.addWord(word); ctrl.saveCollections(); });
            }
            ctrl.save();
            dlg.dispose();
            refresh();
            UIUtils.showMessage(VocabManagerPanel.this,
                "已新增「" + word + "」！" + (selectedFolder != null
                    && !FOLDER_ALL.equals(selectedFolder) && !FOLDER_TOEIC.equals(selectedFolder)
                    ? "\n同時加入「" + selectedFolder + "」資料夾。" : ""), "新增成功");
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(Box.createHorizontalStrut(8));
        btnRow.add(addBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(folderHint, BorderLayout.WEST);
        south.add(btnRow,     BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));
        content.add(titleLbl, BorderLayout.NORTH);
        content.add(form,     BorderLayout.CENTER);
        content.add(south,    BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(420, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── 編輯單字對話框 ────────────────────────────────────────
    private void showEditWordDialog(Vocabulary v) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        JTextField wordField    = styledTF();
        JTextField meaningField = styledTF();

        String[] posOpts = {"n.","v.","adj.","adv.","prep.","conj.",
                            "pron.","interj.","n./v.","adj./n.","prep./adv."};
        JComboBox<String> posCombo = posCombo(posOpts);

        wordField.setText(v.getWord()    != null ? v.getWord()    : "");
        meaningField.setText(v.getMeaning() != null ? v.getMeaning() : "");
        posCombo.setSelectedItem(v.getPos() != null ? v.getPos() : "");

        JLabel titleLbl = new JLabel("編輯單字");
        titleLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 8, 0));
        form.add(formLabelRequired("英文單字")); form.add(wordField);
        form.add(formLabelRequired("詞性"));     form.add(posCombo);
        form.add(formLabelRequired("中文詞義")); form.add(meaningField);

        JButton saveBtn   = UIUtils.styledDialogBtn("儲存修改", AppColors.BTN_PRIMARY, Color.WHITE);
        JButton cancelBtn = UIUtils.styledDialogBtn("取消", new Color(0x9E8272), Color.WHITE);
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
            String oldWord = v.getWord();
            if (!newWord.equals(oldWord)) {
                boolean dup = ctrl.getVocabList().stream()
                    .anyMatch(x -> x != v && x.getWord().equalsIgnoreCase(newWord));
                if (dup) {
                    UIUtils.showMessage(dlg, "「" + newWord + "」已存在！", "重複");
                    return;
                }
                for (VocabCollection col : ctrl.getCollections()) {
                    if (col.containsWord(oldWord)) { col.removeWord(oldWord); col.addWord(newWord); }
                }
                ctrl.saveCollections();
            }
            v.setWord(newWord);
            v.setMeaning(newMeaning);
            v.setPos(newPos);
            ctrl.save();
            dlg.dispose();
            refresh();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelBtn);
        btnRow.add(Box.createHorizontalStrut(8));
        btnRow.add(saveBtn);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));
        content.add(titleLbl, BorderLayout.NORTH);
        content.add(form,     BorderLayout.CENTER);
        content.add(btnRow,   BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(400, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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

    // ── 錯題本彈窗 ──────────────────────────────────────────────
    private void showWrongWords() {
        List<Vocabulary> wrongs = ctrl.getWrongWords();
        if (wrongs.isEmpty()) {
            UIUtils.showMessage(this, "目前沒有錯題記錄！", "錯題本");
            return;
        }
        wrongs = wrongs.stream()
            .sorted((a, b) -> b.getWrongCount() - a.getWrongCount())
            .collect(Collectors.toList());

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);

        // 標題
        JLabel titleLbl = new JLabel("錯題本");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel subLbl = new JLabel("錯誤單字：" + wrongs.size() + " 個");
        subLbl.setFont(AppColors.FONT_SMALL);
        subLbl.setForeground(AppColors.TEXT_RED);
        subLbl.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel head = new JPanel(new BorderLayout(0, 0));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 12, 0));
        head.add(titleLbl, BorderLayout.NORTH);
        head.add(subLbl,   BorderLayout.SOUTH);

        // 表格：單字、詞性、詞義、錯誤次數
        String[] cols = {"單字", "詞性", "詞義", "錯誤次數"};
        Object[][] data = new Object[wrongs.size()][4];
        for (int i = 0; i < wrongs.size(); i++) {
            Vocabulary vv = wrongs.get(i);
            data[i][0] = vv.getWord();
            data[i][1] = vv.getPos().isEmpty() ? "—" : vv.getPos();
            data[i][2] = vv.getMeaning() != null ? vv.getMeaning() : "";
            data[i][3] = vv.getWrongCount();
        }
        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : String.class;
            }
        };
        Color SEL_BG = new Color(168, 200, 192, 128);

        JTable tbl = new JTable(tm) {
            @Override public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(SEL_BG);
                } else {
                    c.setBackground(AppColors.BG_CARD);
                }
                return c;
            }
        };
        tbl.setFont(AppColors.FONT_BODY);
        tbl.setRowHeight(28);
        tbl.setBackground(AppColors.BG_CARD);
        tbl.setGridColor(new Color(0xE8E8E8));
        tbl.setSelectionBackground(SEL_BG);
        tbl.setSelectionForeground(AppColors.TEXT_PRIMARY);
        tbl.getTableHeader().setFont(AppColors.FONT_BODY);
        tbl.getTableHeader().setBackground(new Color(0xDDD0BC));
        tbl.getTableHeader().setForeground(AppColors.TEXT_PRIMARY);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.setAutoCreateRowSorter(true);

        int[] widths = {110, 70, 190, 80};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // 預設左 padding renderer
        javax.swing.table.DefaultTableCellRenderer paddedLeft =
            new javax.swing.table.DefaultTableCellRenderer();
        paddedLeft.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        for (int i = 0; i < 4; i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(paddedLeft);

        // 單字欄：黑色 + 左 padding
        javax.swing.table.DefaultTableCellRenderer blackRenderer =
            new javax.swing.table.DefaultTableCellRenderer();
        blackRenderer.setForeground(AppColors.TEXT_PRIMARY);
        blackRenderer.setFont(new Font("Serif", Font.BOLD, 13));
        blackRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        tbl.getColumnModel().getColumn(0).setCellRenderer(blackRenderer);

        // 錯誤次數欄：紅色居中
        tbl.getColumnModel().getColumn(3).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setHorizontalAlignment(CENTER);
                    setForeground(AppColors.TEXT_RED);
                    setFont(AppColors.FONT_BTN);
                    setText(v != null ? v + " 次" : "");
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(AppColors.BORDER_SOFT));
        scroll.getViewport().setBackground(AppColors.BG_CARD);

        // 關閉按鈕（靠右）
        JButton okBtn = UIUtils.styledDialogBtn("關閉", AppColors.BTN_PRIMARY, Color.WHITE);
        okBtn.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnRow.add(okBtn);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));
        content.add(head,   BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setSize(520, 440);
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
    private JMenuItem styledMenuItem(String text, Color fg) {
        JMenuItem item = new JMenuItem(text) {
            @Override protected void paintComponent(Graphics g) {
                if (isArmed()) {
                    g.setColor(new Color(168, 200, 192, 120));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.setColor(AppColors.BG_CARD);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        item.setFont(AppColors.FONT_BODY);
        item.setForeground(fg);
        item.setBackground(AppColors.BG_CARD);
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(7, 16, 7, 16));
        return item;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_BODY);
        l.setForeground(AppColors.TEXT_PRIMARY);
        return l;
    }
    private JLabel formLabelRequired(String text) {
        JLabel l = new JLabel("<html>" + text + " <font color='#E53935'>*</font></html>");
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
        int mask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        f.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, mask), javax.swing.text.DefaultEditorKit.copyAction);
        f.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, mask), javax.swing.text.DefaultEditorKit.pasteAction);
        f.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, mask), javax.swing.text.DefaultEditorKit.cutAction);
        f.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, mask), javax.swing.text.DefaultEditorKit.selectAllAction);
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
        UIUtils.addHover(b, AppColors.BTN_PRIMARY);
        return b;
    }
    private JButton sBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(AppColors.BG_CARD);
        b.setForeground(new Color(0x7C6250));
        b.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x7C6250), 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        UIUtils.addHover(b, AppColors.BG_CARD);
        return b;
    }
}
