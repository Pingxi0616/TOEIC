package ui;

import controller.DashboardController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ToeicApp extends JFrame {

    private final DashboardController ctrl = new DashboardController();

    private DashboardPanel    dashPanel;
    private VocabQuizPanel    vocabPanel;
    private FillBlankPanel    fillPanel;
    private CustomQuizPanel   customPanel;
    private VocabManagerPanel managerPanel;
    private FavoriteWordsPanel favoritePanel;
    private WrongWordsPanel   wrongPanel;
    private CollectionPanel   collectionPanel;
    private HistoryPanel      historyPanel;

    private JPanel cardArea;
    private CardLayout cardLayout;

    private JButton[] navBtns;

    private static final int SIDEBAR_COUNT = 5;

    private static final String[] PAGE_KEYS = {
        "dashboard", "vocab", "fill", "custom", "manager",
        "favorite",  "wrong", "collection", "history"
    };
    private static final String[] SIDEBAR_NAMES = {
        "主頁", "單字片語測驗", "句子填空測驗", "客製化出題", "單字庫管理"
    };

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

        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildCardArea(), BorderLayout.CENTER);

        setContentPane(root);
        selectPage(0);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBackground(AppColors.BG_SIDEBAR);
        sb.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0xB8A888)));

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

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(AppColors.BG_SIDEBAR);
        nav.setBorder(new EmptyBorder(10, 8, 10, 8));

        navBtns = new JButton[SIDEBAR_COUNT];
        for (int i = 0; i < SIDEBAR_COUNT; i++) {
            final int idx = i;
            navBtns[i] = buildSidebarBtn(SIDEBAR_NAMES[i]);
            navBtns[i].addActionListener(e -> selectPage(idx));
            nav.add(navBtns[i]);
            nav.add(Box.createVerticalStrut(2));
            if (i == 0 || i == 4) nav.add(buildSeparator());
        }

        sb.add(logo, BorderLayout.NORTH);
        sb.add(nav,  BorderLayout.CENTER);
        return sb;
    }

    private JButton buildSidebarBtn(String text) {
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

    private JPanel buildCardArea() {
        cardLayout = new CardLayout();
        cardArea   = new JPanel(cardLayout);
        cardArea.setBackground(AppColors.BG_MAIN);

        dashPanel = new DashboardPanel(ctrl,
            () -> selectPage(5),   // onFavorite
            () -> selectPage(6),   // onWrong
            () -> selectPage(7),   // onCollection
            () -> selectPage(8)    // onHistory
        );

        vocabPanel      = new VocabQuizPanel(ctrl);
        fillPanel       = new FillBlankPanel(ctrl);
        customPanel     = new CustomQuizPanel(ctrl);
        managerPanel    = new VocabManagerPanel(ctrl);

        Runnable backToHome = () -> selectPage(0);
        favoritePanel   = new FavoriteWordsPanel(ctrl,  backToHome);
        wrongPanel      = new WrongWordsPanel(ctrl,     backToHome);
        collectionPanel = new CollectionPanel(ctrl,     backToHome);
        historyPanel    = new HistoryPanel(ctrl,        backToHome);

        cardArea.add(dashPanel,       PAGE_KEYS[0]);
        cardArea.add(vocabPanel,      PAGE_KEYS[1]);
        cardArea.add(fillPanel,       PAGE_KEYS[2]);
        cardArea.add(customPanel,     PAGE_KEYS[3]);
        cardArea.add(managerPanel,    PAGE_KEYS[4]);
        cardArea.add(favoritePanel,   PAGE_KEYS[5]);
        cardArea.add(wrongPanel,      PAGE_KEYS[6]);
        cardArea.add(collectionPanel, PAGE_KEYS[7]);
        cardArea.add(historyPanel,    PAGE_KEYS[8]);

        customPanel.addPropertyChangeListener("startCustomQuiz", evt -> {
            boolean prioritize = (boolean) evt.getNewValue();
            vocabPanel.startQuiz(prioritize);
            selectPage(1);
        });

        return cardArea;
    }

    private void selectPage(int idx) {
        cardLayout.show(cardArea, PAGE_KEYS[idx]);

        // Only update highlight for sidebar buttons (indices 0-4)
        for (int i = 0; i < SIDEBAR_COUNT; i++) {
            boolean on = (i == idx);
            navBtns[i].setBackground(on ? AppColors.BG_SELECTED : AppColors.BG_SIDEBAR);
            navBtns[i].setForeground(on ? AppColors.TEXT_PRIMARY : AppColors.TEXT_SECONDARY);
            navBtns[i].setBorder(on
                ? new CompoundBorder(new LineBorder(new Color(0x7ABAAA), 1, true), new EmptyBorder(8, 14, 8, 14))
                : new CompoundBorder(new LineBorder(new Color(0, 0, 0, 0), 1, true), new EmptyBorder(8, 14, 8, 14))
            );
        }

        switch (idx) {
            case 0: dashPanel.refresh();       break;
            case 4: managerPanel.refresh();    break;
            case 5: favoritePanel.refresh();   break;
            case 6: wrongPanel.refresh();      break;
            case 7: collectionPanel.refresh(); break;
            case 8: historyPanel.refresh();    break;
        }
    }
}
