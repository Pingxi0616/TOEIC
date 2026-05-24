package ui;

import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;

public class WordCardPopup {

    public static void show(Component parent, Vocabulary v) {
        Window w = SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setMinimumSize(new Dimension(480, 0));
        dlg.setLocationRelativeTo(parent);

        // Root panel: compound border gives the "floating card" look
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppColors.BG_MAIN);
        content.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2),
            new EmptyBorder(40, 36, 24, 36)
        ));

        // ── Word (top, blue, clickable) ───────────────────────
        JLabel wordLbl = new JLabel(v.getWord(), SwingConstants.CENTER);
        wordLbl.setFont(new Font("Serif", Font.BOLD, 44));
        wordLbl.setForeground(new Color(0x1565C0));
        wordLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wordLbl.setToolTipText("點擊開啟 Cambridge Dictionary");
        wordLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                openCambridge(dlg, v.getWord());
            }
        });
        wordLbl.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ── Middle info (pos → meaning → stars → example) ────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel posLbl = new JLabel("(" + (v.getPos() != null ? v.getPos() : "") + ")",
                SwingConstants.CENTER);
        posLbl.setFont(new Font("Microsoft JhengHei", Font.ITALIC, 15));
        posLbl.setForeground(AppColors.TEXT_SECONDARY);
        posLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel meaningLbl = new JLabel(v.getMeaning() != null ? v.getMeaning() : "",
                SwingConstants.CENTER);
        meaningLbl.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        meaningLbl.setForeground(AppColors.TEXT_PRIMARY);
        meaningLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stars — positioned between meaning and example, bigger
        JLabel starLbl = new JLabel(v.getFamiliarityStars(), SwingConstants.CENTER);
        starLbl.setFont(new Font("Serif", Font.PLAIN, 28));
        starLbl.setForeground(new Color(0xF9A825));
        starLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(posLbl);
        info.add(Box.createVerticalStrut(8));
        info.add(meaningLbl);
        info.add(Box.createVerticalStrut(20));
        info.add(starLbl);

        String ex = v.getExample();
        if (ex != null && !ex.isBlank()) {
            JLabel exLbl = new JLabel("<html><center><i>" + ex + "</i></center></html>",
                    SwingConstants.CENTER);
            exLbl.setFont(new Font("Serif", Font.ITALIC, 17));
            exLbl.setForeground(AppColors.TEXT_SECONDARY);
            exLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.add(Box.createVerticalStrut(16));
            info.add(exLbl);
        }

        // ── Close button (bottom) ──────────────────────────────
        JButton closeBtn = new JButton("關閉");
        closeBtn.setFont(AppColors.FONT_BTN);
        closeBtn.setForeground(AppColors.TEXT_SECONDARY);
        closeBtn.setBackground(AppColors.BG_MAIN);
        closeBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(6, 28, 6, 28)
        ));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnRow.add(closeBtn);

        content.add(wordLbl, BorderLayout.NORTH);
        content.add(info,    BorderLayout.CENTER);
        content.add(btnRow,  BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    static void openCambridge(Component parent, String word) {
        try {
            Desktop.getDesktop().browse(
                new URI("https://dictionary.cambridge.org/dictionary/english/" + word));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                "無法開啟瀏覽器，請手動前往：\nhttps://dictionary.cambridge.org/dictionary/english/" + word,
                "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
