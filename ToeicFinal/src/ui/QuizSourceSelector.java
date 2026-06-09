package ui;

import controller.DashboardController;
import manager.QuizManager;
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
import java.util.stream.Collectors;

/**
 * 測驗前的單字庫來源選擇器（條列式）
 */
public class QuizSourceSelector extends JPanel {

    public enum SourceType { ALL, UNLEARNED, HISTORY, WRONG, TOEIC, FAVORITE, TODAY, COLLECTION }

    public static class Source {
        public final SourceType      type;
        public final String          label, detail;
        public final Color           color;
        public final VocabCollection collection;
        Source(SourceType t, String l, Color c, String d, VocabCollection col) {
            type = t; label = l; color = c; detail = d; collection = col;
        }
    }

    private final DashboardController        ctrl;
    private final String                     titleText;
    private final String                     titleImagePath;
    private final Consumer<List<Vocabulary>> onSelect;
    private final boolean                    showModeSelector;

    private Source              selectedSource = null;
    private QuizManager.Mode    selectedMode   = QuizManager.Mode.EN_TO_CN;
    private final List<Source>    sources = new ArrayList<>();

    private JPanel listPanel;
    private JButton[] modeBtns = new JButton[2];

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              Consumer<List<Vocabulary>> onSelect) {
        this(ctrl, titleText, null, onSelect, false);
    }

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              String imagePath, Consumer<List<Vocabulary>> onSelect) {
        this(ctrl, titleText, imagePath, onSelect, false);
    }

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              Consumer<List<Vocabulary>> onSelect, boolean showModeSelector) {
        this(ctrl, titleText, null, onSelect, showModeSelector);
    }

    public QuizSourceSelector(DashboardController ctrl, String titleText,
                              String imagePath, Consumer<List<Vocabulary>> onSelect, boolean showModeSelector) {
        this.ctrl              = ctrl;
        this.titleText         = titleText;
        this.titleImagePath    = imagePath;
        this.onSelect          = onSelect;
        this.showModeSelector  = showModeSelector;
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
        if (titleImagePath != null) west.add(loadScaledIcon(titleImagePath, 80));

        p.add(west, BorderLayout.WEST);
        return p;
    }

    // ── 條列主體 ──────────────────────────────────────────────
    private JPanel buildBody() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scroll = UIUtils.styledScroll(listPanel);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // 如果需要顯示模式選擇，添加在頂部
        if (showModeSelector) {
            JPanel modePanel = new JPanel(new BorderLayout());
            modePanel.setOpaque(false);
            modePanel.setBorder(new EmptyBorder(0, 0, 12, 0));

            JPanel modeContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            modeContent.setOpaque(false);

            JLabel modeLabel = new JLabel("選擇模式：");
            modeLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));  // 與「群組」同大
            modeLabel.setForeground(AppColors.TEXT_PRIMARY);
            modeContent.add(modeLabel);

            String[] modeNames = {"英翻中", "中翻英"};
            QuizManager.Mode[] modeVals = {QuizManager.Mode.EN_TO_CN, QuizManager.Mode.CN_TO_EN};
            for (int i = 0; i < 2; i++) {
                final int fi = i;
                modeBtns[i] = new JButton(modeNames[i]);
                modeBtns[i].setFont(AppColors.FONT_BTN);
                modeBtns[i].setFocusPainted(false);
                modeBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                modeBtns[i].addActionListener(e -> {
                    selectedMode = modeVals[fi];
                    refreshModeBtns();
                });
                modeContent.add(modeBtns[i]);
            }
            refreshModeBtns();
            modePanel.add(modeContent, BorderLayout.WEST);
            wrapper.add(modePanel, BorderLayout.NORTH);
        }

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── 刷新來源清單 ──────────────────────────────────────────
    public QuizManager.Mode getSelectedMode() {
        return selectedMode;
    }

    public void refreshSources() {
        sources.clear();
        selectedSource = null;

        long unlearnedCnt = ctrl.getVocabList().stream()
            .filter(v -> v.getCorrectCount() == 0 && v.getWrongCount() == 0)
            .count();

        // ── 主要來源（前 5 項） ────────────────────────────────────────────
        sources.add(new Source(SourceType.ALL,
            "全部單字", new Color(0x4E342E),  // 深褐色
            ctrl.getTotalCount() + " 個（隨機 20 題）", null));
        sources.add(new Source(SourceType.UNLEARNED,
            "尚學單字", new Color(0x4E342E),
            unlearnedCnt + " 個（尚未練習）", null));
        sources.add(new Source(SourceType.HISTORY,
            "已學單字", new Color(0x4E342E),
            ctrl.getHistoryWords().size() + " 個（練習過的）", null));
        sources.add(new Source(SourceType.WRONG,
            "錯誤單字", new Color(0x4E342E),
            ctrl.getWrongWords().size() + " 個（依錯誤次數）", null));
        sources.add(new Source(SourceType.TOEIC,
            "TOEIC 多益單字", new Color(0x4E342E),
            ctrl.getToeicCount() + " 個（隨機 20 題）", null));

        // ── 群組來源 ────────────────────────────────────────────
        for (VocabCollection col : ctrl.getCollections()) {
            sources.add(new Source(SourceType.COLLECTION,
                col.getName(), new Color(0x1565C0),  // 藍色
                col.getWords().size() + " 個單字", col));
        }

        rebuildList();
    }

    private void rebuildList() {
        listPanel.removeAll();

        // ── 主要來源（前 5 項）─────────────────────────────────
        int mainCount = 5;
        for (int i = 0; i < Math.min(mainCount, sources.size()); i++) {
            listPanel.add(buildRow(sources.get(i)));
            listPanel.add(Box.createVerticalStrut(8));
        }

        // ── 群組項目 ────────────────────────────────────────────
        if (sources.size() > mainCount) {
            listPanel.add(Box.createVerticalStrut(16));
            for (int i = mainCount; i < sources.size(); i++) {
                listPanel.add(buildRow(sources.get(i)));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }


    // ── 建立條列行 ────────────────────────────────────────────
    private JPanel buildRow(Source src) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(AppColors.BG_CARD);
        row.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        row.setMinimumSize(new Dimension(0, 70));
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 左側預留小空隙
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 6, 0, 0));
        wrapper.setMinimumSize(new Dimension(0, 70));
        wrapper.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        wrapper.add(row, BorderLayout.CENTER);

        // 左側：標題 + 說明（移除圓點）
        JLabel titleLbl = new JLabel(src.label);
        titleLbl.setFont(AppColors.FONT_HEAD);
        // 四個主要來源用深灰色，群組用深藍色
        if (src.type == SourceType.COLLECTION) {
            titleLbl.setForeground(new Color(0x0D47A1));  // 深藍色
        } else {
            titleLbl.setForeground(new Color(0x424242));  // 深灰色
        }

        JLabel detailLbl = new JLabel(src.detail);
        detailLbl.setFont(AppColors.FONT_SMALL);
        detailLbl.setForeground(AppColors.TEXT_SECONDARY);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(titleLbl);

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);
        info.add(titleRow,  BorderLayout.NORTH);
        info.add(detailLbl, BorderLayout.SOUTH);

        // 右側：開始按鈕（rgb(124, 98, 80)）
        JButton startBtn = new JButton("開始練習 →");
        startBtn.setFont(AppColors.FONT_BTN);
        startBtn.setBackground(new Color(124, 98, 80));  // rgb(124, 98, 80)
        startBtn.setForeground(Color.WHITE);
        startBtn.setOpaque(true);
        startBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> handleSelect(src));

        // 右側垂直置中
        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(startBtn);

        row.add(info,       BorderLayout.CENTER);
        row.add(btnWrapper, BorderLayout.EAST);

        // 只有按鈕可以互動，hover 時變淺棕色
        MouseAdapter btnHover = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                startBtn.setBackground(new Color(160, 134, 120));  // 更淺的棕色
            }
            @Override public void mouseExited(MouseEvent e) {
                startBtn.setBackground(new Color(124, 98, 80));
            }
        };
        startBtn.addMouseListener(btnHover);
        return wrapper;
    }

    private void selectOnly(Source src) {
        selectedSource = src;
    }

    private void handleSelect(Source src) {
        selectedSource = src;
        List<Vocabulary> words = resolveWords(src);
        if (words.isEmpty()) {
            UIUtils.showMessage(this,
                "「" + src.label + "」目前沒有單字，請先新增或完成測驗！",
                "無單字");
            return;
        }
        onSelect.accept(words);
    }

    private void refreshModeBtns() {
        QuizManager.Mode[] vals = {QuizManager.Mode.EN_TO_CN, QuizManager.Mode.CN_TO_EN};
        for (int i = 0; i < modeBtns.length; i++) {
            boolean on = vals[i] == selectedMode;
            modeBtns[i].setBackground(on ? new Color(124, 98, 80) : AppColors.BG_CARD);
            modeBtns[i].setForeground(on ? Color.WHITE : AppColors.TEXT_SECONDARY);
            modeBtns[i].setBorder(new CompoundBorder(
                new LineBorder(on ? AppColors.BORDER : AppColors.BORDER_SOFT, 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        }
    }

    private List<Vocabulary> resolveWords(Source src) {
        switch (src.type) {
            case ALL -> {
                // 全部單字：固定 20 題
                List<Vocabulary> all = new ArrayList<>(ctrl.getVocabList());
                Collections.shuffle(all);
                return new ArrayList<>(all.subList(0, Math.min(20, all.size())));
            }
            case UNLEARNED -> {
                // 尚學單字：固定 20 題
                List<Vocabulary> ul = ctrl.getVocabList().stream()
                    .filter(v -> v.getCorrectCount() == 0 && v.getWrongCount() == 0)
                    .collect(Collectors.toList());
                Collections.shuffle(ul);
                return new ArrayList<>(ul.subList(0, Math.min(20, ul.size())));
            }
            case HISTORY -> {
                // 已學單字：固定 20 題
                List<Vocabulary> hist = new ArrayList<>(ctrl.getHistoryWords());
                Collections.shuffle(hist);
                return new ArrayList<>(hist.subList(0, Math.min(20, hist.size())));
            }
            case WRONG -> {
                // 錯誤單字：固定 20 題
                List<Vocabulary> wrong = new ArrayList<>(ctrl.getWrongWords());
                Collections.shuffle(wrong);
                return new ArrayList<>(wrong.subList(0, Math.min(20, wrong.size())));
            }
            case TOEIC -> {
                // TOEIC：如果超過 20 題就出 20 題，不足就出全部
                List<Vocabulary> toeic = new ArrayList<>(ctrl.getToeicWords());
                Collections.shuffle(toeic);
                return new ArrayList<>(toeic.subList(0, Math.min(20, toeic.size())));
            }
            case COLLECTION -> {
                // 群組：如果超過 20 題就出 20 題，不足就出全部
                List<Vocabulary> col = new ArrayList<>(ctrl.getCollectionWords(src.collection));
                Collections.shuffle(col);
                return new ArrayList<>(col.subList(0, Math.min(20, col.size())));
            }
            default -> { return new ArrayList<>(); }
        }
    }


    private JLabel loadScaledIcon(String resourcePath, int size) {
        JLabel lbl = new JLabel();
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                BufferedImage src = javax.imageio.ImageIO.read(url);
                int srcW = src.getWidth(), srcH = src.getHeight();
                int targetW = (int) Math.round((double) srcW / srcH * size);
                lbl.setPreferredSize(new Dimension(targetW, size));
                lbl.setIcon(new ImageIcon(scaleHighQuality(src, targetW, size)));
            }
        } catch (Exception ignored) {}
        return lbl;
    }

    private static BufferedImage scaleHighQuality(BufferedImage src, int targetW, int targetH) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage cur = src;
        while (w > targetW * 2 || h > targetH * 2) {
            w = Math.max(w / 2, targetW); h = Math.max(h / 2, targetH);
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(cur, 0, 0, w, h, null); g2.dispose();
            cur = tmp;
        }
        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(cur, 0, 0, targetW, targetH, null); g2.dispose();
        return result;
    }
}
