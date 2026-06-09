package ui;

import controller.DashboardController;
import manager.QuizManager.QuizItem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ToeicApp extends JFrame {

    private final DashboardController ctrl = new DashboardController();

    private DashboardPanel     dashPanel;
    private VocabQuizPanel     vocabPanel;
    private FillBlankPanel     fillPanel;
    private CustomQuizPanel    customPanel;
    private MixedQuizPanel     mixedPanel;   // 綜合測驗（客製化出題使用）
    private VocabManagerPanel  managerPanel;
    private FavoriteWordsPanel favoritePanel;
    private WrongWordsPanel    wrongPanel;
    private CollectionPanel    collectionPanel;
    private HistoryPanel       historyPanel;

    private JPanel     cardArea;
    private CardLayout cardLayout;
    private JButton[]  navBtns;

    private static final String[] PAGE_KEYS = {
        "dashboard","vocab","fill","custom","manager",
        "favorite","wrong","collection","history","mixed"
    };
    private static final String[] SIDEBAR_NAMES = {
        "主頁","單字片語測驗","句子填空測驗","客製化出題","單字庫管理"
    };
    private static final int SIDEBAR_COUNT = 5;

    public ToeicApp() {
        setTitle("TOEIC 練習系統");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.BG_MAIN);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildCardArea(), BorderLayout.CENTER);
        setContentPane(root);
        selectPage(0);
    }

    // ── 側欄 ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBackground(AppColors.BG_SIDEBAR);
        sb.setBorder(new MatteBorder(0,0,0,1, new Color(0xB8A888)));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 18));
        logo.setBackground(AppColors.BG_SIDEBAR);
        logo.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, new Color(0xB8A888)),
            new EmptyBorder(0, 18, 0, 0)
        ));
        JLabel sq = new JLabel("🏐");
        sq.setFont(new Font("Apple Color Emoji", Font.PLAIN, 28));
        sq.setPreferredSize(new Dimension(36, 36));
        JLabel appName = new JLabel("<html><b>TOEIC 練習</b><br>"
            + "<font color='gray' size='2'>多益單字學習系統</font></html>");
        appName.setFont(AppColors.FONT_HEAD);
        logo.add(sq); logo.add(appName);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(AppColors.BG_SIDEBAR);
        nav.setBorder(new EmptyBorder(10, 8, 10, 8));

        navBtns = new JButton[SIDEBAR_COUNT];
        for (int i = 0; i < SIDEBAR_COUNT; i++) {
            final int idx = i;
            navBtns[i] = navBtn(SIDEBAR_NAMES[i]);
            navBtns[i].addActionListener(e -> selectPage(idx));
            nav.add(navBtns[i]);
            nav.add(Box.createVerticalStrut(2));
            if (i == 0 || i == 4) nav.add(sep());
        }

        JLabel countLbl = new JLabel("題庫：" + ctrl.getTotalCount() + " 個單字");
        countLbl.setFont(AppColors.FONT_SMALL);
        countLbl.setForeground(new Color(0x8A9AB0));
        countLbl.setBorder(new EmptyBorder(6, 14, 0, 0));
        nav.add(countLbl);

        sb.add(logo, BorderLayout.NORTH);
        sb.add(nav,  BorderLayout.CENTER);
        return sb;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BODY);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,0),1,true), new EmptyBorder(8,14,8,14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(AppColors.BG_SIDEBAR);
        b.setForeground(AppColors.TEXT_SECONDARY);
        return b;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setForeground(new Color(0xB8A888));
        return s;
    }

    // ── 主區 ──────────────────────────────────────────────────
    private JPanel buildCardArea() {
        cardLayout = new CardLayout();
        cardArea   = new JPanel(cardLayout);
        cardArea.setBackground(AppColors.BG_MAIN);

        Runnable backHome  = () -> selectPage(0);
        Runnable backCustom = () -> selectPage(3);   // 綜合測驗結束後回客製化出題

        dashPanel       = new DashboardPanel(ctrl,
            () -> selectPage(5), () -> selectPage(6),
            () -> selectPage(7), () -> selectPage(8));
        vocabPanel      = new VocabQuizPanel(ctrl);
        fillPanel       = new FillBlankPanel(ctrl);
        customPanel     = new CustomQuizPanel(ctrl);
        mixedPanel      = new MixedQuizPanel(ctrl, backCustom);
        managerPanel    = new VocabManagerPanel(ctrl);
        favoritePanel   = new FavoriteWordsPanel(ctrl, backHome);
        wrongPanel      = new WrongWordsPanel(ctrl,    backHome);
        collectionPanel = new CollectionPanel(ctrl,    backHome);
        historyPanel    = new HistoryPanel(ctrl,       backHome);

        // ── 客製化出題 → 綜合測驗 ──
        customPanel.addPropertyChangeListener("startCustomQuiz", evt -> {
            int[] settings  = (int[]) evt.getNewValue();
            int vocabCount  = settings[0];
            int fillCount   = settings[1];
            int poolChoice  = settings[2]; // 0=已學 1=尚學 2=錯誤

            java.util.List<model.Vocabulary> pool = (poolChoice == 3)
                ? ctrl.getVocabList()
                : ctrl.buildCustomPool(
                    poolChoice == 2, // inclWrong
                    poolChoice == 0, // inclLearned
                    poolChoice == 1  // inclUnlearned
                  );
            List<QuizItem> items = ctrl.getQuizManager()
                .generateMixedQuiz(vocabCount, fillCount, pool);
            mixedPanel.startQuiz(items);
            showMixed();
        });

        cardArea.add(dashPanel,       PAGE_KEYS[0]);
        cardArea.add(vocabPanel,      PAGE_KEYS[1]);
        cardArea.add(fillPanel,       PAGE_KEYS[2]);
        cardArea.add(customPanel,     PAGE_KEYS[3]);
        cardArea.add(managerPanel,    PAGE_KEYS[4]);
        cardArea.add(favoritePanel,   PAGE_KEYS[5]);
        cardArea.add(wrongPanel,      PAGE_KEYS[6]);
        cardArea.add(collectionPanel, PAGE_KEYS[7]);
        cardArea.add(historyPanel,    PAGE_KEYS[8]);
        cardArea.add(mixedPanel,      PAGE_KEYS[9]);

        return cardArea;
    }

    /** 顯示綜合測驗（不改變側欄 highlight） */
    private void showMixed() {
        cardLayout.show(cardArea, PAGE_KEYS[9]);
        // 側欄維持「客製化出題」高亮
        for (int i = 0; i < SIDEBAR_COUNT; i++) {
            boolean on = (i == 3);
            navBtns[i].setBackground(on ? AppColors.BG_SELECTED : AppColors.BG_SIDEBAR);
            navBtns[i].setForeground(on ? AppColors.TEXT_PRIMARY : AppColors.TEXT_SECONDARY);
            navBtns[i].setBorder(on
                ? new CompoundBorder(new LineBorder(new Color(0x7ABAAA),1,true), new EmptyBorder(8,14,8,14))
                : new CompoundBorder(new LineBorder(new Color(0,0,0,0),1,true),  new EmptyBorder(8,14,8,14)));
        }
    }

    public void selectPage(int idx) {
        cardLayout.show(cardArea, PAGE_KEYS[idx]);
        for (int i = 0; i < SIDEBAR_COUNT; i++) {
            boolean on = (i == idx);
            navBtns[i].setBackground(on ? AppColors.BG_SELECTED : AppColors.BG_SIDEBAR);
            navBtns[i].setForeground(on ? AppColors.TEXT_PRIMARY : AppColors.TEXT_SECONDARY);
            navBtns[i].setBorder(on
                ? new CompoundBorder(new LineBorder(new Color(0x7ABAAA),1,true), new EmptyBorder(8,14,8,14))
                : new CompoundBorder(new LineBorder(new Color(0,0,0,0),1,true),  new EmptyBorder(8,14,8,14)));
        }
        switch (idx) {
            case 0 -> dashPanel.refresh();
            case 1 -> vocabPanel.showSelector();
            case 2 -> fillPanel.showSelector();
            case 4 -> managerPanel.refresh();
            case 5 -> favoritePanel.refresh();
            case 6 -> wrongPanel.refresh();
            case 7 -> collectionPanel.refresh();
            case 8 -> historyPanel.refresh();
        }
    }
}
