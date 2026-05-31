package ui;

import controller.DashboardController;
import model.Vocabulary;
import model.VocabCollection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * 測驗前的單字庫來源選擇器（卡片格局，圓圈指示燈）
 */
public class QuizSourceSelector extends JPanel {

    public enum SourceType { ALL, TOEIC, FAVORITE, WRONG, HISTORY, TODAY, COLLECTION }

    public static class Source {
        public final SourceType      type;
        public final String          label, detail;
        public final Color           color;
        public final VocabCollection collection;
        Source(SourceType t, String l, Color c, String d, VocabCollection col) {
            type = t; label = l; color = c; detail = d; collection = col;
        }
    }

    private final DashboardController       ctrl;
    private final String                    titleText;
    private final String                    titleImagePath;
    private final Consumer<List<Vocabulary>> onSelect;

    /** 目前被點擊（綠燈）的來源 */
    private Source selectedSource = null;

    /** 圓圈元件對應清單，用來做全局重繪 */
    private final List<CircleDot> dots   = new ArrayList<>();
    private final List<Source>    sources = new ArrayList<>();

    private JScrollPane bodyScroll;
    private JPanel      sourceGrid;

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              Consumer<List<Vocabulary>> onSelect) {
        this(ctrl, titleText, null, onSelect);
    }

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              String imagePath, Consumer<List<Vocabulary>> onSelect) {
        this.ctrl           = ctrl;
        this.titleText      = titleText;
        this.titleImagePath = imagePath;
        this.onSelect       = onSelect;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        refreshSources();
    }

    // ── 頂部標題 ──────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel t = new JLabel(titleText);
        t.setFont(AppColors.FONT_TITLE);
        t.setForeground(AppColors.TEXT_PRIMARY);

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);
        west.add(t);
        if (titleImagePath != null) {
            west.add(loadScaledIcon(titleImagePath, 80));
        }

        JLabel hint = new JLabel("請選擇要練習的單字來源");
        hint.setFont(AppColors.FONT_BODY);
        hint.setForeground(AppColors.TEXT_SECONDARY);
        p.add(west, BorderLayout.WEST);
        p.add(hint, BorderLayout.EAST);
        return p;
    }

    private JLabel loadScaledIcon(String resourcePath, int size) {
        JLabel lbl = new JLabel();
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                BufferedImage src = javax.imageio.ImageIO.read(url);
                // 保持原始長寬比，以 size 為高度
                int srcW = src.getWidth(), srcH = src.getHeight();
                int targetH = size;
                int targetW = (int) Math.round((double) srcW / srcH * targetH);
                lbl.setPreferredSize(new Dimension(targetW, targetH));
                lbl.setIcon(new ImageIcon(scaleHighQuality(src, targetW, targetH)));
            }
        } catch (Exception ignored) {}
        return lbl;
    }

    /** 逐步降採樣（progressive scaling），比 getScaledInstance 清晰得多 */
    private static BufferedImage scaleHighQuality(BufferedImage src, int targetW, int targetH) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage cur = src;
        while (w > targetW * 2 || h > targetH * 2) {
            w = Math.max(w / 2, targetW);
            h = Math.max(h / 2, targetH);
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(cur, 0, 0, w, h, null);
            g2.dispose();
            cur = tmp;
        }
        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(cur, 0, 0, targetW, targetH, null);
        g2.dispose();
        return result;
    }

    // ── 卡片區域（3欄格局，可垂直捲動）──────────────────────
    // 每張卡片高 150px，列間距 14px → 2 列 = 150×2 + 14 = 314px
    private static final int CARD_H  = 200;
    private static final int CARD_GAP = 14;
    private static final int VISIBLE_ROWS = 3; // 一次顯示幾列

    private JPanel buildBody() {
        sourceGrid = new JPanel(new GridLayout(0, 3, CARD_GAP, CARD_GAP));
        sourceGrid.setOpaque(false);

        bodyScroll = UIUtils.styledScroll(sourceGrid);
        bodyScroll.setBorder(null);
        bodyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(CARD_H + CARD_GAP);
        // 固定可見高度：剛好顯示 VISIBLE_ROWS 列
        int visH = VISIBLE_ROWS * CARD_H + (VISIBLE_ROWS - 1) * CARD_GAP;
        bodyScroll.setPreferredSize(new Dimension(0, visH));
        bodyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, visH));

        // NORTH 讓 bodyScroll 用自己的 preferredHeight（不被拉伸），
        // 超過 3 排的內容由 bodyScroll 自己捲動
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bodyScroll, BorderLayout.NORTH);
        return wrapper;
    }

    // ── 刷新來源清單 ──────────────────────────────────────────
    public void refreshSources() {
        sources.clear();
        dots.clear();
        selectedSource = null;

        sources.add(new Source(SourceType.ALL,
            "全部單字",    AppColors.TEXT_PRIMARY,
            ctrl.getTotalCount() + " 個（隨機 20 題）", null));
        sources.add(new Source(SourceType.TOEIC,
            "TOEIC 多益單字", new Color(0x2E7D6E),
            ctrl.getToeicCount() + " 個（隨機 20 題）", null));
        sources.add(new Source(SourceType.FAVORITE,
            "Favorite 收藏", AppColors.TEXT_RED,
            ctrl.getFavoriteWords().size() + " 個單字", null));
        sources.add(new Source(SourceType.WRONG,
            "錯誤單字",    new Color(0xE65100),
            ctrl.getWrongWords().size() + " 個（依錯誤次數）", null));
        sources.add(new Source(SourceType.HISTORY,
            "已學單字",    new Color(0x5C6BC0),
            ctrl.getHistoryWords().size() + " 個（練習過的）", null));
        sources.add(new Source(SourceType.TODAY,
            "今日待複習",  new Color(0x2E7D6E),
            ctrl.getTodayReviewCount() + " 個（隨機 50 題）", null));

        for (VocabCollection col : ctrl.getCollections()) {
            sources.add(new Source(SourceType.COLLECTION,
                col.getName(), new Color(0x7B5EA7),
                col.getWords().size() + " 個單字", col));
        }

        sourceGrid.removeAll();
        for (Source src : sources) {
            sourceGrid.add(buildCard(src));
        }
        sourceGrid.revalidate();
        sourceGrid.repaint();
    }

    // ── 建立單張卡片 ──────────────────────────────────────────
    private JPanel buildCard(Source src) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(AppColors.BG_CARD);
        card.setPreferredSize(new Dimension(0, CARD_H)); // 固定高度，避免格子大小隨內容改變
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(22, 18, 22, 18)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── 標題列（圓圈 + 文字）
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        CircleDot dot = new CircleDot(src);
        dots.add(dot);

        JLabel titleLbl = new JLabel(src.label);
        titleLbl.setFont(AppColors.FONT_HEAD);
        titleLbl.setForeground(src.color);

        titleRow.add(dot);
        titleRow.add(titleLbl);

        // ── 數量說明
        JLabel detailLbl = new JLabel(src.detail);
        detailLbl.setFont(AppColors.FONT_SMALL);
        detailLbl.setForeground(AppColors.TEXT_SECONDARY);

        // ── 開始按鈕（點擊 → 選取並啟動測驗）
        JButton startBtn = new JButton("開始練習 →");
        startBtn.setFont(AppColors.FONT_BTN);
        startBtn.setBackground(AppColors.BTN_PRIMARY);
        startBtn.setForeground(Color.WHITE);
        startBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> handleSelect(src));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(detailLbl, BorderLayout.WEST);

        card.add(titleRow,  BorderLayout.NORTH);
        card.add(bottom,    BorderLayout.CENTER);
        card.add(startBtn,  BorderLayout.SOUTH);

        // ── 卡片懸浮效果 + 點擊只亮綠燈
        MouseAdapter cardMouse = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectOnly(src); }
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xF0E8D8));
                startBtn.setBackground(new Color(0x3A4A5E));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(AppColors.BG_CARD);
                startBtn.setBackground(AppColors.BTN_PRIMARY);
            }
        };
        card.addMouseListener(cardMouse);

        // ── 子元件只處理點擊（不加 hover 避免滑鼠移入子元件時閃爍）
        MouseAdapter childClick = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectOnly(src); }
        };
        titleRow.addMouseListener(childClick);
        detailLbl.addMouseListener(childClick);
        titleLbl.addMouseListener(childClick);
        dot.addMouseListener(childClick);

        return card;
    }

    // ── 只亮綠燈，不啟動測驗 ──────────────────────────────────
    private void selectOnly(Source src) {
        selectedSource = src;
        repaintAllDots();
    }

    // ── 亮綠燈 + 啟動測驗 ────────────────────────────────────
    private void handleSelect(Source src) {
        selectedSource = src;
        repaintAllDots();

        List<Vocabulary> words = resolveWords(src);
        if (words.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "「" + src.label + "」目前沒有單字，請先新增或完成測驗！",
                "無單字", JOptionPane.WARNING_MESSAGE);
            return;
        }
        onSelect.accept(words);
    }

    private List<Vocabulary> resolveWords(Source src) {
        switch (src.type) {
            case FAVORITE   -> { return ctrl.getFavoriteWords(); }
            case WRONG      -> { return ctrl.getWrongWords(); }
            case HISTORY    -> { return ctrl.getHistoryWords(); }
            case COLLECTION -> { return ctrl.getCollectionWords(src.collection); }
            case TODAY -> {
                List<Vocabulary> due = new ArrayList<>(ctrl.getTodayWords());
                Collections.shuffle(due);
                due.sort(Comparator.comparingInt(Vocabulary::getFamiliarity));
                return new ArrayList<>(due.subList(0, Math.min(50, due.size())));
            }
            case TOEIC -> {
                List<Vocabulary> toeic = new ArrayList<>(ctrl.getToeicWords());
                Collections.shuffle(toeic);
                return new ArrayList<>(toeic.subList(0, Math.min(20, toeic.size())));
            }
            default -> {  // ALL
                List<Vocabulary> all = new ArrayList<>(ctrl.getVocabList());
                Collections.shuffle(all);
                return new ArrayList<>(all.subList(0, Math.min(20, all.size())));
            }
        }
    }

    /** 全部圓圈重繪（只有 selectedSource 對應的是綠色） */
    private void repaintAllDots() {
        for (CircleDot d : dots) d.repaint();
    }

    // ── 自訂圓圈元件 ──────────────────────────────────────────
    private class CircleDot extends JPanel {
        private final Source src;
        CircleDot(Source src) {
            this.src = src;
            setOpaque(false);
            setPreferredSize(new Dimension(16, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean on = (selectedSource == src);
            // 從 (1,1) 開始留出 1px 邊距，避免 1.5px 描邊被裁切
            g2.setColor(on ? new Color(0x4CAF50) : new Color(0xBDBDBD));
            g2.fillOval(1, 1, 13, 13);
            g2.setColor(on ? new Color(0x2E7D32) : new Color(0x9E9E9E));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(1, 1, 13, 13);
            g2.dispose();
        }
    }
}
