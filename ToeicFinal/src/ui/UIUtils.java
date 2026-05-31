package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;

public class UIUtils {

    public static JScrollPane styledScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setBackground(AppColors.BG_MAIN);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.setViewportBorder(null);
        styleBar(sp.getVerticalScrollBar());
        return sp;
    }

    private static void styleBar(JScrollBar bar) {
        // 14px total: 6px left gap + 8px thumb area
        bar.setPreferredSize(new Dimension(14, 0));
        bar.setOpaque(false);
        bar.setUI(new BasicScrollBarUI() {
            private static final int GAP = 6;
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0xB8A888);
                trackColor = new Color(0xF5EFE0);
            }
            @Override
            protected JButton createDecreaseButton(int o) { return zero(); }
            @Override
            protected JButton createIncreaseButton(int o) { return zero(); }
            private JButton zero() {
                JButton b = new JButton();
                Dimension d = new Dimension(0, 0);
                b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d);
                return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + GAP, r.y + 2, r.width - GAP - 1, r.height - 4, 6, 6);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(trackColor);
                g.fillRect(r.x + GAP, r.y, r.width - GAP, r.height);
            }
        });
    }

    // ── 風格一致的提示對話框 ──────────────────────────────────

    /** 顯示訊息對話框（僅 OK） */
    public static void showMessage(Component parent, String message, String title) {
        JDialog dlg = buildDlg(parent, title);
        JPanel content = buildContent(title, message);

        JButton ok = styledDialogBtn("OK", AppColors.BTN_PRIMARY, Color.WHITE);
        ok.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        content.add(btnRow, BorderLayout.SOUTH);
        finishDlg(dlg, content, parent);
    }

    /** 顯示輸入對話框，回傳輸入文字，取消回傳 null */
    public static String showInput(Component parent, String message, String title) {
        String[] result = {null};
        JDialog dlg = buildDlg(parent, title);
        JPanel content = buildContent(title, message);

        JTextField field = new JTextField();
        field.setFont(AppColors.FONT_BODY);
        field.setBackground(AppColors.BG_CARD);
        field.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));

        JButton cancel = styledDialogBtn("取消", AppColors.TEXT_SECONDARY, AppColors.BG_MAIN);
        JButton ok     = styledDialogBtn("確定", AppColors.BTN_PRIMARY, Color.WHITE);
        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> { result[0] = field.getText(); dlg.dispose(); });
        field.addActionListener(e -> { result[0] = field.getText(); dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel); btnRow.add(ok);

        content.add(field,  BorderLayout.CENTER);
        content.add(btnRow, BorderLayout.SOUTH);
        finishDlg(dlg, content, parent);
        return result[0];
    }

    /** 顯示確認對話框（YES / NO），回傳 true = YES */
    public static boolean showConfirm(Component parent, String message, String title) {
        boolean[] result = {false};
        JDialog dlg = buildDlg(parent, title);
        JPanel content = buildContent(title, message);

        JButton no  = styledDialogBtn("取消", AppColors.TEXT_SECONDARY, AppColors.BG_MAIN);
        JButton yes = styledDialogBtn("確定", AppColors.BTN_PRIMARY, Color.WHITE);
        no .addActionListener(e -> dlg.dispose());
        yes.addActionListener(e -> { result[0] = true; dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(no);
        btnRow.add(yes);

        content.add(btnRow, BorderLayout.SOUTH);
        finishDlg(dlg, content, parent);
        return result[0];
    }

    private static JDialog buildDlg(Component parent, String title) {
        Window w = parent instanceof Window
                 ? (Window) parent
                 : SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        return dlg;
    }

    private static JPanel buildContent(String title, String message) {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(AppColors.BG_MAIN);
        p.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(22, 26, 18, 26)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        titleLbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        // 多行訊息支援（\n 換行）
        String html = "<html>" + message.replace("\n", "<br>") + "</html>";
        JLabel msgLbl = new JLabel(html);
        msgLbl.setFont(AppColors.FONT_BODY);
        msgLbl.setForeground(AppColors.TEXT_SECONDARY);

        p.add(titleLbl, BorderLayout.NORTH);
        p.add(msgLbl,   BorderLayout.CENTER);
        return p;
    }

    private static void finishDlg(JDialog dlg, JPanel content, Component parent) {
        dlg.setContentPane(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(300, dlg.getHeight()));
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static JButton styledDialogBtn(String text, Color bg, Color fg) {
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

    private UIUtils() {}
}
