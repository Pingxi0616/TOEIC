package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

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

    private UIUtils() {}
}
