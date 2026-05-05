package ui;

import controller.DashboardController;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final DashboardController ctrl;

    // 動態標籤
    private JLabel learnedLabel, reviewLabel, famLabel, streakLabel;
    private JLabel reviewDeltaLabel, famDeltaLabel;
    private JPanel todayWordPanel;

    public DashboardPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);

        refresh();
    }

    // ── 頂部標題列 ────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("總覽");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel date = new JLabel(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日")));
        date.setFont(AppColors.FONT_BODY);
        date.setForeground(AppColors.TEXT_SECONDARY);

        p.add(title, BorderLayout.WEST);
        p.add(date,  BorderLayout.EAST);
        return p;
    }

    // ── 中央內容 ──────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.add(buildStatCards(), BorderLayout.NORTH);
        p.add(buildBottom(),    BorderLayout.CENTER);
        return p;
    }

    // ── 四張統計卡片 ─────────────────────────────────────────
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        learnedLabel     = new JLabel("0",  SwingConstants.CENTER);
        reviewLabel      = new JLabel("0",  SwingConstants.CENTER);
        famLabel         = new JLabel("0%", SwingConstants.CENTER);
        streakLabel      = new JLabel("0天",SwingConstants.CENTER);
        reviewDeltaLabel = new JLabel("", SwingConstants.CENTER);
        famDeltaLabel    = new JLabel("", SwingConstants.CENTER);

        row.add(buildStatCard(learnedLabel,     "已學單字",  null));
        row.add(buildStatCard(reviewLabel,       "待複習",    reviewDeltaLabel));
        row.add(buildStatCard(famLabel,          "熟悉度",    famDeltaLabel));
        row.add(buildStatCard(streakLabel,       "連續學習",  makeSmallLabel("持續中")));

        return row;
    }

    private JPanel buildStatCard(JLabel numLabel, String title, JLabel subLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 12, 16, 12)
        ));

        numLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 26));
        numLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(AppColors.FONT_BODY);
        titleLabel.setForeground(AppColors.TEXT_SECONDARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(numLabel, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.setOpaque(false);
        center.add(numLabel);
        center.add(titleLabel);

        card.add(center, BorderLayout.CENTER);
        if (subLabel != null) {
            subLabel.setFont(AppColors.FONT_SMALL);
            subLabel.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(subLabel, BorderLayout.SOUTH);
        }
        return card;
    }

    // ── 下半區（功能選單 + 今日待複習） ─────────────────────
    private JPanel buildBottom() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildMenuCard());
        p.add(buildTodayReviewCard());
        return p;
    }

    private JPanel buildMenuCard() {
        JPanel card = cardPanel("功能選單");
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 0, 0, 0));

        body.add(buildMenuRow("單字片語測驗 >", "中翻英 · 英翻中 · 片語"));
        body.add(buildMenuRow("句子填空測驗 >", "單字導向 · 文法導向"));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMenuRow(String title, String sub) {
        JPanel row = new JPanel(new BorderLayout(0, 2));
        row.setBackground(new Color(0xF0E8D8));
        row.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel t = new JLabel(title);
        t.setFont(AppColors.FONT_HEAD);
        t.setForeground(AppColors.TEXT_PRIMARY);
        JLabel s = new JLabel(sub);
        s.setFont(AppColors.FONT_SMALL);
        s.setForeground(AppColors.TEXT_SECONDARY);
        row.add(t, BorderLayout.NORTH);
        row.add(s, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildTodayReviewCard() {
        JPanel card = cardPanel("今日待複習單字");
        todayWordPanel = new JPanel();
        todayWordPanel.setLayout(new BoxLayout(todayWordPanel, BoxLayout.Y_AXIS));
        todayWordPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(todayWordPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── 工具方法 ─────────────────────────────────────────────
    private JPanel cardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppColors.BG_CARD);
        p.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(AppColors.FONT_HEAD);
        lbl.setForeground(AppColors.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JLabel makeSmallLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(AppColors.FONT_SMALL);
        l.setForeground(AppColors.TEXT_SECONDARY);
        return l;
    }

    /** 重新整理統計資訊 */
    public void refresh() {
        learnedLabel.setText(String.valueOf(ctrl.getLearnedCount()));
        reviewLabel.setText(String.valueOf(ctrl.getTodayReviewCount()));
        famLabel.setText(ctrl.getAverageFamiliarityPercent() + "%");
        streakLabel.setText("14天"); // TODO: 實作連續天數追蹤

        reviewDeltaLabel.setText("到期提醒");
        reviewDeltaLabel.setForeground(AppColors.TEXT_ORANGE);

        famDeltaLabel.setText("↑ 持續進步");
        famDeltaLabel.setForeground(AppColors.TEXT_GREEN);

        // 更新今日待複習清單
        todayWordPanel.removeAll();
        List<Vocabulary> today = ctrl.getTodayWords();
        if (today.isEmpty()) {
            JLabel none = new JLabel("  今日無待複習單字 🎉");
            none.setFont(AppColors.FONT_BODY);
            none.setForeground(AppColors.TEXT_GREEN);
            todayWordPanel.add(none);
        } else {
            for (Vocabulary v : today.subList(0, Math.min(today.size(), 8))) {
                JLabel lbl = new JLabel("  ● " + v.getWord()
                    + "   " + v.getFamiliarityStars());
                lbl.setFont(AppColors.FONT_BODY);
                lbl.setForeground(AppColors.TEXT_PRIMARY);
                lbl.setBorder(new EmptyBorder(3, 0, 3, 0));
                todayWordPanel.add(lbl);
            }
        }
        todayWordPanel.revalidate();
        todayWordPanel.repaint();
    }
}
