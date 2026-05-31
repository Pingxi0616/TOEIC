package ui;

import controller.DashboardController;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicSliderUI;

public class CustomQuizPanel extends JPanel {

    private final DashboardController ctrl;

    private JSlider vocabSlider, fillSlider, wrongSlider;
    private JLabel  vocabVal, fillVal, wrongVal, totalLabel;
    private JToggleButton weakToggle, recentToggle;

    // 出題來源
    private String quizSource = "全部單字";
    private JPanel sourceButtonArea;

    // 熟悉度篩選（index 0=★ … 4=★★★★★）
    private boolean[] famFilter = {true, true, true, true, true};
    private JToggleButton[] famBtns;

    public CustomQuizPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── 頂部標題 ──────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel t = new JLabel("客製化出題");
        t.setFont(AppColors.FONT_TITLE);
        t.setForeground(AppColors.TEXT_PRIMARY);
        p.add(t, BorderLayout.WEST);
        return p;
    }

    // ── 主內容 ────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 14, 0));
        topRow.setOpaque(false);
        topRow.add(buildRatioCard());
        topRow.add(buildPreferenceCard());

        JPanel botRow = new JPanel(new GridLayout(1, 2, 14, 0));
        botRow.setOpaque(false);
        botRow.add(buildSourceCard());
        botRow.add(buildFamiliarityCard());

        p.add(topRow,          BorderLayout.NORTH);
        p.add(botRow,          BorderLayout.CENTER);
        p.add(buildStartBtn(), BorderLayout.SOUTH);
        return p;
    }

    // ── 出題比例卡（排球拉桿） ─────────────────────────────────
    private JPanel buildRatioCard() {
        JPanel card = cardPanel("出題比例（共 20 題）");

        vocabSlider = makeSlider(10);
        fillSlider  = makeSlider(7);
        wrongSlider = makeSlider(3);
        vocabVal    = valLabel("10");
        fillVal     = valLabel("7");
        wrongVal    = valLabel("3");

        totalLabel = new JLabel("總計：10 + 7 + 3 = 20 題", SwingConstants.CENTER);
        totalLabel.setFont(AppColors.FONT_SMALL);
        totalLabel.setForeground(AppColors.TEXT_GREEN);
        totalLabel.setBackground(new Color(0xFEF9E7));
        totalLabel.setOpaque(true);
        totalLabel.setBorder(new EmptyBorder(6, 10, 6, 10));

        javax.swing.event.ChangeListener cl = e -> updateTotal();
        vocabSlider.addChangeListener(cl);
        fillSlider .addChangeListener(cl);
        wrongSlider.addChangeListener(cl);

        JPanel body = new JPanel(new GridLayout(4, 1, 0, 10));
        body.setOpaque(false);
        body.add(buildSliderRow("單字片語", vocabSlider, vocabVal));
        body.add(buildSliderRow("句子填空", fillSlider,  fillVal));
        body.add(buildSliderRow("錯題複習", wrongSlider, wrongVal));
        body.add(totalLabel);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSliderRow(String label, JSlider slider, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppColors.FONT_BODY);
        lbl.setPreferredSize(new Dimension(66, 20));
        row.add(lbl,    BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    // ── 偏好設定卡 ────────────────────────────────────────────
    private JPanel buildPreferenceCard() {
        JPanel card = cardPanel("出題偏好設定");

        weakToggle   = makeToggle("弱點優先模式", "自動提高熟悉度低的題目比例");
        recentToggle = makeToggle("僅考近期錯題", "限最近 3 次測驗答錯的單字");
        weakToggle.setSelected(true);

        JLabel rTitle = new JLabel("錯題重出時機");
        rTitle.setFont(AppColors.FONT_SMALL);
        rTitle.setForeground(AppColors.TEXT_SECONDARY);
        rTitle.setBorder(new EmptyBorder(4, 0, 4, 0));

        ButtonGroup bg = new ButtonGroup();
        JRadioButton r1 = styledRadio("同回合立刻重出", true);
        JRadioButton r2 = styledRadio("下次測驗才重出", false);
        r1.setToolTipText("答錯後立即將該題插回題目佇列，本回合結束前會再出現");
        r2.setToolTipText("答錯後記入下次測驗，本回合不重複，維持流暢節奏");
        bg.add(r1); bg.add(r2);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(weakToggle);
        body.add(Box.createVerticalStrut(8));
        body.add(styledSeparator());
        body.add(Box.createVerticalStrut(8));
        body.add(recentToggle);
        body.add(Box.createVerticalStrut(8));
        body.add(styledSeparator());
        body.add(Box.createVerticalStrut(8));
        body.add(rTitle);
        body.add(r1);
        body.add(r2);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ── 出題來源卡 ────────────────────────────────────────────
    // 固定來源的顏色對應
    private static final java.util.Map<String, Color> SOURCE_COLORS = new java.util.LinkedHashMap<>();
    static {
        SOURCE_COLORS.put("全部單字",      new Color(0x5C6BC0)); // 靛藍
        SOURCE_COLORS.put("TOEIC 多益單字", new Color(0x2E7D6E)); // 青綠
        SOURCE_COLORS.put("Favorite 收藏", new Color(0xC62828)); // 紅
        SOURCE_COLORS.put("錯誤單字",       new Color(0xE65100)); // 橘
    }
    private static final Color COL_COLOR = new Color(0x6D4C41); // 使用者群組：棕

    private JPanel buildSourceCard() {
        JPanel card = cardPanel("出題來源");

        sourceButtonArea = new JPanel();
        sourceButtonArea.setLayout(new BoxLayout(sourceButtonArea, BoxLayout.Y_AXIS));
        sourceButtonArea.setOpaque(false);

        refreshSourceButtons();

        JScrollPane scroll = UIUtils.styledScroll(sourceButtonArea);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void refreshSourceButtons() {
        if (sourceButtonArea == null) return;
        sourceButtonArea.removeAll();

        ButtonGroup bg = new ButtonGroup();
        for (String src : SOURCE_COLORS.keySet()) {
            sourceButtonArea.add(sourceBtn(src, SOURCE_COLORS.get(src), bg));
            sourceButtonArea.add(Box.createVerticalStrut(5));
        }
        if (!ctrl.getCollections().isEmpty()) {
            // 群組分隔線
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setForeground(AppColors.BORDER_SOFT);
            sourceButtonArea.add(Box.createVerticalStrut(3));
            sourceButtonArea.add(sep);
            sourceButtonArea.add(Box.createVerticalStrut(5));
            for (model.VocabCollection col : ctrl.getCollections()) {
                sourceButtonArea.add(sourceBtn(col.getName(), COL_COLOR, bg));
                sourceButtonArea.add(Box.createVerticalStrut(5));
            }
        }
        sourceButtonArea.revalidate();
        sourceButtonArea.repaint();
    }

    private JToggleButton sourceBtn(String name, Color accent, ButtonGroup bg) {
        boolean selected = name.equals(quizSource);
        Color bgColor  = selected ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30) : AppColors.BG_CARD;

        JToggleButton b = new JToggleButton(name, selected) {
            @Override protected void paintComponent(Graphics g) {
                // 先手動填背景，避免 Windows L&F 覆蓋藍色
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                if (isSelected()) {
                    // 左側 3px 彩色指示條
                    g.setColor(accent);
                    g.fillRect(0, 0, 3, getHeight());
                }
            }
        };
        b.setContentAreaFilled(false);
        b.setFont(selected
            ? new Font("Microsoft JhengHei", Font.BOLD, 13)
            : AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBackground(bgColor);
        b.setForeground(selected ? accent : AppColors.TEXT_PRIMARY);
        b.setBorder(new CompoundBorder(
            new LineBorder(selected ? accent : new Color(0, 0, 0, 0), 1, true),
            new EmptyBorder(7, 14, 7, 10)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!b.isSelected()) b.setBackground(AppColors.BG_MAIN);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!b.isSelected()) b.setBackground(AppColors.BG_CARD);
            }
        });
        b.addActionListener(e -> { quizSource = name; refreshSourceButtons(); });
        bg.add(b);
        return b;
    }

    // ── 熟悉度篩選卡 ──────────────────────────────────────────
    // 每個等級的顏色：淺色(未選) / 深色(已選)
    private static final Color[] FAM_LIGHT = {
        new Color(0xF9EDED), new Color(0xFAF0E4),
        new Color(0xFAF5E0), new Color(0xEFF5E8), new Color(0xE8F4F0)
    };
    private static final Color[] FAM_DEEP = {
        new Color(0xEDD8D0), new Color(0xEDE0C8),
        new Color(0xEDE8C0), new Color(0xD8E8C8), new Color(0xC8E0D8)
    };

    private JPanel buildFamiliarityCard() {
        JPanel card = cardPanel("熟悉度篩選");

        JLabel hint = new JLabel("選擇要複習的熟悉度等級（可多選）");
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        hint.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel starRow = new JPanel(new GridLayout(1, 5, 8, 0));
        starRow.setOpaque(false);
        famBtns = new JToggleButton[5];

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            final int level = i + 1;
            // 用 HTML 讓星星大一點
            String starHtml = "<html><center>"
                + "<font size='4'>" + "★".repeat(level) + "<font color='#CCCCCC'>" + "★".repeat(5 - level) + "</font></font>"
                + "<br><font size='2' color='#888888'>" + level + " 星</font>"
                + "</center></html>";

            JToggleButton btn = new JToggleButton(starHtml, famFilter[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    super.paintComponent(g);
                }
            };
            btn.setContentAreaFilled(false);
            btn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setBackground(famFilter[i] ? FAM_DEEP[i] : FAM_LIGHT[i]);
            btn.setForeground(AppColors.TEXT_PRIMARY);
            btn.setBorder(new CompoundBorder(
                new LineBorder(famFilter[i] ? new Color(0x9A8A70) : AppColors.BORDER_SOFT, 2, true),
                new EmptyBorder(10, 4, 10, 4)
            ));
            btn.addActionListener(e -> {
                famFilter[idx] = btn.isSelected();
                btn.setBackground(btn.isSelected() ? FAM_DEEP[idx] : FAM_LIGHT[idx]);
                btn.setBorder(new CompoundBorder(
                    new LineBorder(btn.isSelected() ? new Color(0x9A8A70) : AppColors.BORDER_SOFT, 2, true),
                    new EmptyBorder(10, 4, 10, 4)
                ));
            });
            famBtns[i] = btn;
            starRow.add(btn);
        }

        // 快速選擇按鈕
        JButton allBtn  = quickBtn("全選");
        JButton noneBtn = quickBtn("全不選");
        JButton lowBtn  = quickBtn("只選未熟悉");
        allBtn.addActionListener(e  -> setFamFilter(true, true, true, true, true));
        noneBtn.addActionListener(e -> setFamFilter(false, false, false, false, false));
        lowBtn.addActionListener(e  -> setFamFilter(true, true, false, false, false));

        JPanel quickRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickRow.setOpaque(false);
        quickRow.add(allBtn); quickRow.add(noneBtn); quickRow.add(lowBtn);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(hint,     BorderLayout.NORTH);
        body.add(starRow,  BorderLayout.CENTER);
        body.add(quickRow, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void setFamFilter(boolean... vals) {
        for (int i = 0; i < 5; i++) {
            famFilter[i] = vals[i];
            famBtns[i].setSelected(vals[i]);
            famBtns[i].setBackground(vals[i] ? FAM_DEEP[i] : FAM_LIGHT[i]);
            famBtns[i].setBorder(new CompoundBorder(
                new LineBorder(vals[i] ? new Color(0x9A8A70) : AppColors.BORDER_SOFT, 2, true),
                new EmptyBorder(10, 4, 10, 4)
            ));
        }
    }

    private JButton quickBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_SMALL);
        b.setForeground(AppColors.TEXT_SECONDARY);
        b.setBackground(AppColors.BG_MAIN);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(3, 8, 3, 8)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JSeparator styledSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.BORDER_SOFT);
        sep.setBackground(AppColors.BG_CARD);
        return sep;
    }

    // ── 開始按鈕 ──────────────────────────────────────────────
    private JPanel buildStartBtn() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        p.setOpaque(false);
        JButton btn = new JButton("開始測驗 →");
        btn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btn.setBackground(AppColors.BTN_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(10, 28, 10, 28)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onStart());
        p.add(btn);
        return p;
    }

    // ── 互動邏輯 ──────────────────────────────────────────────
    private void updateTotal() {
        int v = vocabSlider.getValue();
        int f = fillSlider.getValue();
        int w = wrongSlider.getValue();
        vocabVal.setText(String.valueOf(v));
        fillVal .setText(String.valueOf(f));
        wrongVal.setText(String.valueOf(w));

        int total = v + f + w;
        totalLabel.setText("總計：" + v + " + " + f + " + " + w + " = " + total + " 題");
        totalLabel.setForeground(total == 20 ? AppColors.TEXT_GREEN : AppColors.TEXT_RED);

    }

    private void onStart() {
        int v = vocabSlider.getValue();
        int f = fillSlider.getValue();
        int w = wrongSlider.getValue();
        boolean prioritize = weakToggle.isSelected();
        boolean recentOnly = recentToggle.isSelected();

        String modeDesc = prioritize ? "弱點優先模式" : "隨機模式";
        String msg = String.format(
            "本次出題：單字片語 %d 題、句子填空 %d 題、錯題複習 %d 題\n出題來源：%s\n出題偏好：%s\n\n確認開始綜合測驗？",
            v, f, w, quizSource, modeDesc);
        if (UIUtils.showConfirm(this, msg, "客製化出題")) {
            int[] settings = {v, f, w, prioritize ? 1 : 0, recentOnly ? 1 : 0};
            firePropertyChange("startCustomQuiz", (Object) null, settings);
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────
    private JPanel cardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppColors.BG_CARD);
        p.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(AppColors.FONT_HEAD);
        lbl.setForeground(AppColors.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JSlider makeSlider(int init) {
        JSlider s = new JSlider(0, 20, init);
        s.setOpaque(false);
        s.setFocusable(false);
        s.setUI(new VolleyballSliderUI(s));
        return s;
    }

    private JLabel valLabel(String init) {
        JLabel l = new JLabel(init);
        l.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        l.setPreferredSize(new Dimension(24, 20));
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JToggleButton makeToggle(String title, String sub) {
        JToggleButton b = new JToggleButton(
            "<html><b>" + title + "</b><br>"
            + "<font color='gray'><small>" + sub + "</small></font></html>"
        ) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setFont(AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setOpaque(false);
        b.setBackground(AppColors.BG_CARD);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        b.setFocusPainted(false);
        b.addChangeListener(e ->
            b.setBackground(b.isSelected() ? new Color(0xEDE5D4) : AppColors.BG_CARD)
        );
        return b;
    }

    private JRadioButton styledRadio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setFont(AppColors.FONT_BODY);
        r.setForeground(AppColors.TEXT_PRIMARY);
        r.setOpaque(false);
        r.setFocusPainted(false);

        // 自訂圖示（取代系統藍點，改用暖棕色）
        Icon unsel = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.BG_CARD);
                g2.fillOval(x, y, 14, 14);
                g2.setColor(AppColors.BORDER_SOFT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, 14, 14);
                g2.dispose();
            }
            public int getIconWidth()  { return 16; }
            public int getIconHeight() { return 16; }
        };
        Icon sel = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppColors.BG_CARD);
                g2.fillOval(x, y, 14, 14);
                g2.setColor(new Color(0x9A8A70));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, 14, 14);
                g2.setColor(new Color(0x6A5A40)); // 暖棕色實心點
                g2.fillOval(x + 4, y + 4, 6, 6);
                g2.dispose();
            }
            public int getIconWidth()  { return 16; }
            public int getIconHeight() { return 16; }
        };
        r.setIcon(unsel);
        r.setSelectedIcon(sel);
        return r;
    }

    // ══════════════════════════════════════════════════════════
    // 排球滑桿 UI
    // ══════════════════════════════════════════════════════════
    private static class VolleyballSliderUI extends BasicSliderUI {

        private static BufferedImage volleyballImg;

        static {
            try {
                java.net.URL url = VolleyballSliderUI.class
                    .getResource("/resources/volleyball.png");
                if (url != null) {
                    BufferedImage raw = javax.imageio.ImageIO.read(url);
                    volleyballImg = scaleHighQuality(raw, 64, 44);
                }
            } catch (Exception e) {
                volleyballImg = null;
            }
        }

        private static BufferedImage scaleHighQuality(BufferedImage src, int tw, int th) {
            int w = src.getWidth(), h = src.getHeight();
            BufferedImage cur = src;
            while (w > tw * 2 || h > th * 2) {
                w = Math.max(w / 2, tw);
                h = Math.max(h / 2, th);
                BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(cur, 0, 0, w, h, null);
                g2.dispose();
                cur = tmp;
            }
            BufferedImage result = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = result.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(cur, 0, 0, tw, th, null);
            g2.dispose();
            return result;
        }

        VolleyballSliderUI(JSlider b) { super(b); }

        @Override
        protected Dimension getThumbSize() { return new Dimension(54, 40); }

        @Override public void paintFocus(Graphics g) {}

        // ── 軌道：藍色填充進度條 ──────────────────────────────
        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cy  = trackRect.y + trackRect.height / 2;
            int th  = 6;
            int ty  = cy - th / 2;
            int arc = th;

            g2.setColor(new Color(0xE8E8E8));
            g2.fillRoundRect(trackRect.x, ty, trackRect.width, th, arc, arc);

            int thumbCx = thumbRect.x + thumbRect.width / 2;
            int filled  = thumbCx - trackRect.x;
            if (filled > 0) {
                g2.setColor(new Color(0xF5C840));
                g2.fillRoundRect(trackRect.x, ty,
                    Math.min(filled, trackRect.width), th, arc, arc);
            }
            g2.dispose();
        }

        // ── 拇指：排球圖片（去背，無外框）────────────────────────
        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

            int d  = Math.min(thumbRect.width, thumbRect.height);
            int tx = thumbRect.x + (thumbRect.width  - d) / 2;
            int ty = thumbRect.y + (thumbRect.height - d) / 2;

            if (volleyballImg != null) {
                g2.drawImage(volleyballImg, tx, ty, d, d, null);
            } else {
                g2.setColor(new Color(0xF5C840));
                g2.fillOval(tx, ty, d, d);
            }
            g2.dispose();
        }
    }
}
