package ui;

import controller.DashboardController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CustomQuizPanel extends JPanel {

    private final DashboardController ctrl;

    private JSlider vocabSlider, fillSlider, wrongSlider;
    private JLabel  vocabVal, fillVal, wrongVal, totalLabel;
    private JToggleButton weakToggle, recentToggle;

    // 預覽統計：用 JPanel 而非 JLabel
    private JPanel previewVocab, previewFill, previewWrong;

    public CustomQuizPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildContent(),  BorderLayout.CENTER);
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

        p.add(topRow,            BorderLayout.NORTH);
        p.add(buildPreviewCard(), BorderLayout.CENTER);
        p.add(buildStartBtn(),   BorderLayout.SOUTH);
        return p;
    }

    // ── 出題比例卡 ────────────────────────────────────────────
    private JPanel buildRatioCard() {
        JPanel card = cardPanel("出題比例（共 20 題）");

        vocabSlider = makeSlider(10);
        fillSlider  = makeSlider(7);
        wrongSlider = makeSlider(3);
        vocabVal    = new JLabel("10");
        fillVal     = new JLabel("7");
        wrongVal    = new JLabel("3");

        totalLabel = new JLabel("總計：10 + 7 + 3 = 20 題", SwingConstants.CENTER);
        totalLabel.setFont(AppColors.FONT_SMALL);
        totalLabel.setForeground(AppColors.TEXT_GREEN);
        totalLabel.setBackground(new Color(0xEEF0F2));
        totalLabel.setOpaque(true);
        totalLabel.setBorder(new EmptyBorder(6, 10, 6, 10));

        javax.swing.event.ChangeListener cl = e -> updateTotal();
        vocabSlider.addChangeListener(cl);
        fillSlider.addChangeListener(cl);
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

    private JPanel buildSliderRow(String label, JSlider slider, JLabel valLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppColors.FONT_BODY);
        lbl.setPreferredSize(new Dimension(66, 20));
        valLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        valLabel.setPreferredSize(new Dimension(24, 20));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lbl,      BorderLayout.WEST);
        row.add(slider,   BorderLayout.CENTER);
        row.add(valLabel, BorderLayout.EAST);
        return row;
    }

    // ── 偏好設定卡 ────────────────────────────────────────────
    private JPanel buildPreferenceCard() {
        JPanel card = cardPanel("出題偏好設定");

        weakToggle   = makeToggle("弱點優先模式", "自動提高熟悉度低的題目比例");
        recentToggle = makeToggle("僅考近期錯題", "限最近 3 次測驗的錯誤");
        weakToggle.setSelected(true);

        // 錯題重出時機
        JLabel rTitle = new JLabel("錯題重出時機");
        rTitle.setFont(AppColors.FONT_SMALL);
        rTitle.setForeground(AppColors.TEXT_SECONDARY);
        rTitle.setBorder(new EmptyBorder(4, 0, 4, 0));

        ButtonGroup bg = new ButtonGroup();
        JRadioButton r1 = styledRadio("同回合立刻重出", true);
        JRadioButton r2 = styledRadio("下次測驗才重出", false);
        bg.add(r1);
        bg.add(r2);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(weakToggle);
        body.add(Box.createVerticalStrut(8));
        body.add(new JSeparator());
        body.add(Box.createVerticalStrut(8));
        body.add(recentToggle);
        body.add(Box.createVerticalStrut(8));
        body.add(new JSeparator());
        body.add(Box.createVerticalStrut(8));
        body.add(rTitle);
        body.add(r1);
        body.add(r2);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ── 預覽卡 ────────────────────────────────────────────────
    private JPanel buildPreviewCard() {
        JPanel card = cardPanel("本次出題預覽");

        previewVocab = makePreviewStat("10", "單字片語題", "含 3 題弱點單字", AppColors.TEXT_GREEN);
        previewFill  = makePreviewStat("7",  "句子填空題", "單字 5 + 文法 2",  AppColors.TEXT_SECONDARY);
        previewWrong = makePreviewStat("3",  "錯題複習",   "近期 3 次錯誤",    new Color(0x9A6B3A));

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);
        grid.add(previewVocab);
        grid.add(previewFill);
        grid.add(previewWrong);

        card.add(grid, BorderLayout.CENTER);
        return card;
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
        fillVal.setText(String.valueOf(f));
        wrongVal.setText(String.valueOf(w));

        int total = v + f + w;
        totalLabel.setText("總計：" + v + " + " + f + " + " + w + " = " + total + " 題");
        totalLabel.setForeground(total == 20 ? AppColors.TEXT_GREEN : AppColors.TEXT_RED);
    }

    private void onStart() {
        boolean prioritize = weakToggle.isSelected();
        String msg = prioritize
            ? "已啟用弱點優先模式\n將優先抽出錯誤次數多的單字"
            : "隨機出題模式\n從全部題庫隨機選題";
        int choice = JOptionPane.showConfirmDialog(
            this, msg + "\n\n確認開始測驗？",
            "客製化出題", JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            firePropertyChange("startCustomQuiz", false, prioritize);
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
        return s;
    }

    private JToggleButton makeToggle(String title, String sub) {
        JToggleButton b = new JToggleButton(
            "<html><b>" + title + "</b><br>"
            + "<font color='gray'><small>" + sub + "</small></font></html>"
        );
        b.setFont(AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setBackground(AppColors.BG_CARD);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        b.setFocusPainted(false);
        b.addChangeListener(e ->
            b.setBackground(b.isSelected() ? new Color(0xDCEDC8) : AppColors.BG_CARD)
        );
        return b;
    }

    private JRadioButton styledRadio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setFont(AppColors.FONT_BODY);
        r.setOpaque(false);
        r.setFocusPainted(false);
        return r;
    }

    /** 預覽統計方塊，回傳 JPanel */
    private JPanel makePreviewStat(String num, String title, String sub, Color subColor) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(new Color(0xF5F6F8));
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel numLbl = new JLabel(num);
        numLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        numLbl.setForeground(AppColors.TEXT_PRIMARY);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppColors.FONT_SMALL);
        titleLbl.setForeground(AppColors.TEXT_SECONDARY);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(AppColors.FONT_SMALL);
        subLbl.setForeground(subColor);

        JPanel mid = new JPanel(new GridLayout(2, 1));
        mid.setOpaque(false);
        mid.add(titleLbl);
        mid.add(subLbl);

        p.add(numLbl, BorderLayout.NORTH);
        p.add(mid,    BorderLayout.CENTER);
        return p;
    }
}
