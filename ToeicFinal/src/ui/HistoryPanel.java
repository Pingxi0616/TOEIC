package ui;

import controller.DashboardController;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable onBack;
    private JPanel listPanel;

    public HistoryPanel(DashboardController ctrl, Runnable onBack) {
        this.ctrl   = ctrl;
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        add(UIUtils.styledScroll(listPanel), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JButton backBtn = buildBackBtn();

        JLabel iconLbl = new JLabel("✓");
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        iconLbl.setForeground(new Color(0x5C6BC0));

        JLabel titleLbl = new JLabel(" History 已學單字");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(new Color(0x5C6BC0));

        JPanel title = new JPanel();
        title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
        title.setOpaque(false);
        title.add(iconLbl);
        title.add(titleLbl);

        JLabel hint = new JLabel("已練習過的所有單字（含答對與答錯）");
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_SECONDARY);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.add(hint);
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

    public void refresh() {
        listPanel.removeAll();
        List<Vocabulary> history = ctrl.getHistoryWords();

        if (history.isEmpty()) {
            JLabel none = new JLabel("尚無學習紀錄，完成測驗後單字會出現在此");
            none.setFont(AppColors.FONT_BODY);
            none.setForeground(AppColors.TEXT_SECONDARY);
            none.setBorder(new EmptyBorder(20, 8, 0, 0));
            listPanel.add(none);
        } else {
            for (Vocabulary v : history) {
                listPanel.add(buildRow(v));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildRow(Vocabulary v) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(AppColors.BG_CARD);
        row.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel wordLbl = new JLabel(v.getWord() + "  " + v.getFamiliarityStars());
        wordLbl.setFont(AppColors.FONT_HEAD);
        wordLbl.setForeground(AppColors.TEXT_PRIMARY);

        JLabel detailLbl = new JLabel("(" + v.getPos() + ")  " + v.getMeaning());
        detailLbl.setFont(AppColors.FONT_BODY);
        detailLbl.setForeground(AppColors.TEXT_SECONDARY);

        info.add(wordLbl);
        info.add(detailLbl);

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                WordCardPopup.show(HistoryPanel.this, v);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(0xD3E2DA));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(AppColors.BG_CARD);
            }
        });

        JPanel stats = new JPanel(new GridLayout(2, 1, 0, 2));
        stats.setOpaque(false);

        JLabel correctLbl = new JLabel("答對 " + v.getCorrectCount() + " 次", SwingConstants.RIGHT);
        correctLbl.setFont(AppColors.FONT_SMALL);
        correctLbl.setForeground(AppColors.TEXT_GREEN);

        JLabel wrongLbl = new JLabel("答錯 " + v.getWrongCount() + " 次", SwingConstants.RIGHT);
        wrongLbl.setFont(AppColors.FONT_SMALL);
        wrongLbl.setForeground(v.getWrongCount() > 0 ? AppColors.TEXT_RED : AppColors.TEXT_SECONDARY);

        stats.add(correctLbl);
        stats.add(wrongLbl);

        row.add(info,  BorderLayout.CENTER);
        row.add(stats, BorderLayout.EAST);
        return row;
    }
}
