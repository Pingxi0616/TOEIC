package ui;

import controller.DashboardController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * 主視窗：左側欄 + 右側 CardLayout 切換畫面
 */
public class ToeicApp extends JFrame {

    private final DashboardController ctrl = new DashboardController();

    // 各功能面板
    private DashboardPanel   dashPanel;
    private VocabQuizPanel   vocabPanel;
    private FillBlankPanel   fillPanel;
    private CustomQuizPanel  customPanel;
    private VocabManagerPanel managerPanel;

    private JPanel cardArea;
    private CardLayout cardLayout;

    // 側欄按鈕
    private JButton[] navBtns;
    private static final String[] PAGE_KEYS  = {"dashboard","vocab","fill","custom","manager"};
    private static final String[] PAGE_NAMES = {"總覽","單字片語測驗","句子填空測驗","客製化出題","單字庫管理"};

    public ToeicApp() {
        setTitle("TOEIC 練習系統");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_MAIN);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildCardArea(), BorderLayout.CENTER);

        setContentPane(root);
        selectPage(0); // 預設顯示總覽
    }

    // ── 左側欄 ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBackground(AppColors.BG_SIDEBAR);
        sb.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0xB8A888)));

        // Logo 區
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        logo.setBackground(AppColors.BG_SIDEBAR);
        logo.setBorder(new MatteBorder(0, 0, 1, 0, new Color(0xB8A888)));
        JLabel square = new JLabel("  ");
        square.setBackground(AppColors.TEXT_PRIMARY);
        square.setOpaque(true);
        square.setPreferredSize(new Dimension(28, 28));
        JLabel appName = new JLabel("<html><b>TOEIC 練習</b><br>"
            + "<font color='gray' size='2'>多益學習系統</font></html>");
        appName.setFont(AppColors.FONT_HEAD);
        logo.add(square);
        logo.add(appName);

        // 導覽按鈕
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(AppColors.BG_SIDEBAR);
        nav.setBorder(new EmptyBorder(10, 8, 10, 8));

        navBtns = new JButton[PAGE_NAMES.length];
        for (int i = 0; i < PAGE_NAMES.length; i++) {
            final int idx = i;
            navBtns[i] = buildNavBtn(PAGE_NAMES[i]);
            navBtns[i].addActionListener(e -> selectPage(idx));
            nav.add(navBtns[i]);
            nav.add(Box.createVerticalStrut(2));
            if (i == 3) nav.add(buildSeparator());
        }

        sb.add(logo, BorderLayout.NORTH);
        sb.add(nav,  BorderLayout.CENTER);
        return sb;
    }

    private JButton buildNavBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBorder(new CompoundBorder(
            new LineBorder(new Color(0, 0, 0, 0), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(AppColors.BG_SIDEBAR);
        b.setForeground(AppColors.TEXT_SECONDARY);
        return b;
    }

    private JSeparator buildSeparator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xB8A888));
        return sep;
    }

    // ── 右側 CardLayout ───────────────────────────────────────
    private JPanel buildCardArea() {
        cardLayout = new CardLayout();
        cardArea   = new JPanel(cardLayout);
        cardArea.setBackground(AppColors.BG_MAIN);

        dashPanel    = new DashboardPanel(ctrl);
        vocabPanel   = new VocabQuizPanel(ctrl);
        fillPanel    = new FillBlankPanel(ctrl);
        customPanel  = new CustomQuizPanel(ctrl);
        managerPanel = new VocabManagerPanel(ctrl);

        cardArea.add(dashPanel,    PAGE_KEYS[0]);
        cardArea.add(vocabPanel,   PAGE_KEYS[1]);
        cardArea.add(fillPanel,    PAGE_KEYS[2]);
        cardArea.add(customPanel,  PAGE_KEYS[3]);
        cardArea.add(managerPanel, PAGE_KEYS[4]);

        // 客製化出題「開始」後切換到測驗頁
        customPanel.addPropertyChangeListener("startCustomQuiz", evt -> {
            boolean prioritize = (boolean) evt.getNewValue();
            vocabPanel.startQuiz(prioritize);
            selectPage(1);
        });

        return cardArea;
    }

    private void selectPage(int idx) {
        cardLayout.show(cardArea, PAGE_KEYS[idx]);
        for (int i = 0; i < navBtns.length; i++) {
            boolean on = (i == idx);
            navBtns[i].setBackground(on ? AppColors.BG_SELECTED : AppColors.BG_SIDEBAR);
            navBtns[i].setForeground(on ? AppColors.TEXT_PRIMARY : AppColors.TEXT_SECONDARY);
            navBtns[i].setBorder(on
                ? new CompoundBorder(new LineBorder(new Color(0x7ABAAA), 1, true), new EmptyBorder(8, 14, 8, 14))
                : new CompoundBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true), new EmptyBorder(8, 14, 8, 14))
            );
        }
        // 切回總覽時刷新資料
        if (idx == 0) dashPanel.refresh();
        if (idx == 4) managerPanel.refresh();
    }
}
