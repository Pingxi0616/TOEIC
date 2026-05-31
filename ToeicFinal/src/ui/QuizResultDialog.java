package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public final class QuizResultDialog {

    private QuizResultDialog() {}

    public static void show(Component parent, int correct, int total,
                            String quizName, Runnable onClose) {

        Window owner = parent instanceof Window
                ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setResizable(false);

        // ── 根面板 ──────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AppColors.BG_MAIN);
        root.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1),
            new EmptyBorder(26, 34, 20, 34)
        ));

        // ── 標題 ────────────────────────────────────────────────
        JLabel titleLbl = new JLabel(quizName + " 完成！");
        titleLbl.setFont(AppColors.FONT_TITLE);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        // ── 成績一行 ────────────────────────────────────────────
        double pct = total > 0 ? correct * 100.0 / total : 0;
        JLabel scoreLbl = new JLabel(
            String.format("答對 %d / %d 題　正確率 %.0f%%", correct, total, pct),
            SwingConstants.LEFT
        );
        scoreLbl.setFont(AppColors.FONT_BODY);
        scoreLbl.setForeground(AppColors.TEXT_SECONDARY);

        // ── 完成按鈕 ────────────────────────────────────────────
        JButton okBtn = new JButton("完成");
        okBtn.setFont(AppColors.FONT_BTN);
        okBtn.setBackground(AppColors.BTN_PRIMARY);
        okBtn.setForeground(Color.WHITE);
        okBtn.setOpaque(true);
        okBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(7, 36, 7, 36)
        ));
        okBtn.setFocusPainted(false);
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(okBtn);

        root.add(titleLbl, BorderLayout.NORTH);
        root.add(scoreLbl, BorderLayout.CENTER);
        root.add(btnRow,   BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(340, dlg.getHeight()));
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);

        if (onClose != null) onClose.run();
    }
}
