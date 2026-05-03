package ntou;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ToeicDashboard extends JFrame {

    // 配色定義
    private final Color BG_CREAM = new Color(242, 238, 226); 
    private final Color SIDEBAR_CREAM = new Color(232, 225, 205); 
    private final Color ACCENT_MINT = new Color(197, 226, 224); 
    private final Color BORDER_BLACK = new Color(45, 45, 45); 
    private final Color SOFT_LINE = new Color(180, 175, 160); 

    public ToeicDashboard() {
        super("TOEIC 練習系統");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_CREAM);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_CREAM);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, SOFT_LINE));
        
        // 使用 BoxLayout 垂直排列
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // 1. 標題區域
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        
        // 強制置頂與靠左
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        JPanel logoIcon = new JPanel();
        logoIcon.setPreferredSize(new Dimension(35, 35));
        logoIcon.setBackground(BORDER_BLACK);
        
        JLabel titleLabel = new JLabel("TOEIC 練習");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        
        titlePanel.add(logoIcon);
        titlePanel.add(titleLabel);
        
        sidebar.add(titlePanel);
        sidebar.add(Box.createVerticalStrut(10)); 

        // 2. 按鈕容器
        JPanel btnContainer = new JPanel();
        btnContainer.setOpaque(false);
        btnContainer.setLayout(new BoxLayout(btnContainer, BoxLayout.Y_AXIS));
        btnContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        
        // 強制容器置頂與靠左
        btnContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnContainer.setAlignmentY(Component.TOP_ALIGNMENT);

        String[] menus = {"總覽", "單字片語測驗", "句子填空測驗", "客製化出題", "單字庫管理"};
        for (String m : menus) {
            JButton btn = createNavButton(m, m.equals("總覽"));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnContainer.add(btn);
            btnContainer.add(Box.createVerticalStrut(12));
        }
        
        sidebar.add(btnContainer);
        
        // 3. 加入 VerticalGlue 將上方元件推向最頂端
        sidebar.add(Box.createVerticalGlue()); 

        return sidebar;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setOpaque(false);

        // 右欄頂部標題與淺色分割線
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);
        topArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, SOFT_LINE),
            BorderFactory.createEmptyBorder(30, 40, 15, 40)
        ));

        JLabel title = new JLabel("總覽");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        JLabel date = new JLabel("2026年4月12日");
        topArea.add(title, BorderLayout.WEST);
        topArea.add(date, BorderLayout.EAST);
        main.add(topArea, BorderLayout.NORTH);

        // 內容區排版
        JPanel contentGrid = new JPanel(new BorderLayout(0, 25));
        contentGrid.setOpaque(false);
        contentGrid.setBorder(BorderFactory.createEmptyBorder(25, 40, 30, 40));

        // 數據卡片列
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("142", "已學單字", "↑ 8 本週"));
        statsPanel.add(createStatCard("12", "待複習", "到期提醒"));
        statsPanel.add(createStatCard("78%", "熟悉度", "↑ 3% 上週"));
        statsPanel.add(createStatCard("14天", "連續學習", "持續中"));
        contentGrid.add(statsPanel, BorderLayout.NORTH);

        // 下方方框平行對齊
        JPanel bottomLayout = new JPanel(new GridLayout(1, 2, 30, 0));
        bottomLayout.setOpaque(false);

        // 功能選單大框
        JPanel funcLargeBox = createBrutalCard(); 
        funcLargeBox.setLayout(new BoxLayout(funcLargeBox, BoxLayout.Y_AXIS));
        funcLargeBox.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel funcTitle = new JLabel("功能選單");
        funcTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        funcTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        funcLargeBox.add(funcTitle);
        funcLargeBox.add(Box.createVerticalStrut(25));
        funcLargeBox.add(createStyledActionCard("單字片語測驗 >", "中翻英．英翻中．片語"));
        funcLargeBox.add(Box.createVerticalStrut(15));
        funcLargeBox.add(createStyledActionCard("句子填空測驗 >", "單字導向．文法導向"));
        
        bottomLayout.add(funcLargeBox);

        // 今日待複習單字
        JPanel listCard = createBrutalCard();
        listCard.setLayout(new BorderLayout());
        listCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel listTitle = new JLabel("今日待複習單字");
        listTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        listCard.add(listTitle, BorderLayout.NORTH);
        
        JTextArea words = new JTextArea("\n ● proficient\n ● implement\n ● negotiate\n ● facilitate");
        words.setEditable(false);
        words.setOpaque(false);
        words.setFont(new Font("SansSerif", Font.BOLD, 18));
        listCard.add(words, BorderLayout.CENTER);

        bottomLayout.add(listCard);
        contentGrid.add(bottomLayout, BorderLayout.CENTER);

        main.add(contentGrid, BorderLayout.CENTER);
        return main;
    }

    private JButton createNavButton(String text, boolean isSelected) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected || isHovered ? ACCENT_MINT : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-4, 20, 20);
                g2.setColor(BORDER_BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-5, 20, 20);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, 20, (getHeight() + fm.getAscent())/2 - 4);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(240, 50));
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createStatCard(String val, String title, String sub) {
        JPanel card = createBrutalCard();
        card.setLayout(new GridLayout(3, 1));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel v = new JLabel(val, JLabel.CENTER); v.setFont(new Font("Arial", Font.BOLD, 28));
        JLabel t = new JLabel(title, JLabel.CENTER);
        JLabel s = new JLabel(sub, JLabel.CENTER); s.setForeground(new Color(40, 120, 80));
        card.add(v); card.add(t); card.add(s);
        return card;
    }

    private JPanel createStyledActionCard(String title, String desc) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(BORDER_BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel t = new JLabel(title); t.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel d = new JLabel(desc); d.setForeground(Color.GRAY);
        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.SOUTH);
        card.setMaximumSize(new Dimension(1000, 70));
        return card;
    }

    private JPanel createBrutalCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_BLACK);
                g2.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 25, 25);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-6, 25, 25);
                g2.setColor(BORDER_BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-5, getHeight()-7, 25, 25);
                g2.dispose();
            }
        };
    }
}