package ui;

import controller.DashboardController;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class VocabManagerPanel extends JPanel {

    private final DashboardController ctrl;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, wrongLabel;

    public VocabManagerPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel title = new JLabel("單字庫管理");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnWrong  = styledBtn("查看錯題本");
        JButton btnRefresh = styledBtn("重新整理");
        btnWrong.addActionListener(e -> showWrongWords());
        btnRefresh.addActionListener(e -> refresh());
        right.add(btnWrong);
        right.add(btnRefresh);
        p.add(title, BorderLayout.WEST);
        p.add(right,  BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // 統計列
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

        // 表格
        String[] cols = {"單字", "詞義", "詞性", "熟悉度", "答對", "答錯", "下次複習"};
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

        // 欄寬
        int[] widths = {100, 120, 60, 80, 50, 50, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(AppColors.BORDER_SOFT));

        card.add(statRow, BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        List<Vocabulary> list = ctrl.getVocabList();
        tableModel.setRowCount(0);
        for (Vocabulary v : list) {
            tableModel.addRow(new Object[]{
                v.getWord(),
                v.getMeaning(),
                v.getPos() != null ? v.getPos() : "",
                v.getFamiliarityStars(),
                v.getCorrectCount(),
                v.getWrongCount(),
                v.getNextReviewDate() != null ? v.getNextReviewDate() : "—"
            });
        }
        totalLabel.setText("總單字數：" + list.size());
        long wrongCnt = list.stream().filter(v -> v.getWrongCount() > 0).count();
        wrongLabel.setText("有錯誤記錄：" + wrongCnt + " 個");
    }

    private void showWrongWords() {
        List<Vocabulary> wrongs = ctrl.getWrongWords();
        if (wrongs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有錯題記錄！",
                "錯題本", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("═══════ 錯題本 ═══════\n\n");
        for (Vocabulary v : wrongs) {
            sb.append(String.format("%-16s %s\n  錯誤 %d 次 / 答對 %d 次   熟悉度：%s\n\n",
                v.getWord(), v.getMeaning(),
                v.getWrongCount(), v.getCorrectCount(), v.getFamiliarityStars()));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setPreferredSize(new Dimension(400, 400));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
            "錯題本（共 " + wrongs.size() + " 個）", JOptionPane.PLAIN_MESSAGE);
    }

    private JButton styledBtn(String text) {
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
