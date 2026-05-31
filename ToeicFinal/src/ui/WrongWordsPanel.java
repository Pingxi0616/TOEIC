package ui;

import controller.DashboardController;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class WrongWordsPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable onBack;
    private JPanel listPanel;

    public WrongWordsPanel(DashboardController ctrl, Runnable onBack) {
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

        JLabel title = new JLabel("錯誤單字區");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(new Color(0xF9A825));

        JLabel hint = new JLabel("依答錯次數排序");
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
        JButton b = new JButton("返回主頁 →");
        b.setFont(AppColors.FONT_SMALL);
        b.setForeground(AppColors.TEXT_SECONDARY);
        b.setBackground(AppColors.BG_MAIN);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> onBack.run());
        return b;
    }

    public void refresh() {
        listPanel.removeAll();
        List<Vocabulary> wrongs = ctrl.getWrongWords();

        if (wrongs.isEmpty()) {
            JLabel none = new JLabel("尚無答錯紀錄，繼續加油！");
            none.setFont(AppColors.FONT_BODY);
            none.setForeground(AppColors.TEXT_GREEN);
            none.setBorder(new EmptyBorder(20, 8, 0, 0));
            listPanel.add(none);
        } else {
            for (Vocabulary v : wrongs) {
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
                WordCardPopup.show(WrongWordsPanel.this, v);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(0xF0E8D8));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(AppColors.BG_CARD);
            }
        });

        JLabel wrongLbl = new JLabel("答錯 " + v.getWrongCount() + " 次");
        wrongLbl.setFont(AppColors.FONT_SMALL);
        wrongLbl.setForeground(AppColors.TEXT_RED);
        wrongLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(info,     BorderLayout.CENTER);
        row.add(wrongLbl, BorderLayout.EAST);
        return row;
    }
}
