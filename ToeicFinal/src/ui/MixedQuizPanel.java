package ui;

import controller.DashboardController;
import manager.QuizManager;
import manager.QuizManager.Mode;
import manager.QuizManager.QuizItem;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * 綜合測驗：根據題型自動切換單字版型 / 填空版型
 */
public class MixedQuizPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable            onExit;

    private List<QuizItem> quizItems;
    private int currentIndex = 0;
    private int correctCount = 0;
    private boolean answered  = false;

    // ── 共用 ──────────────────────────────────────────────────
    private JLabel progressLabel, scoreLabel;

    // ── 單字版型 ──────────────────────────────────────────────
    private JLabel    vModeChip, vFamChip, vQuestionLabel, vHintLabel, vFeedbackLabel;
    private JButton[] vOptBtns = new JButton[4];
    private JButton   vNextBtn, vPronounceBtn;

    // ── 填空版型 ──────────────────────────────────────────────
    private JTextArea fSentenceArea;
    private JTextPane fAnalysisPane;
    private JLabel    fFeedbackLabel;
    private JButton[] fOptBtns = new JButton[4];
    private JButton   fNextBtn;

    // ── 切換 ──────────────────────────────────────────────────
    private CardLayout contentCard;
    private JPanel     contentArea;

    public MixedQuizPanel(DashboardController ctrl, Runnable onExit) {
        this.ctrl   = ctrl;
        this.onExit = onExit;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    public void startQuiz(List<QuizItem> items) {
        if (items == null || items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "沒有題目可以出題，請調整設定！",
                "客製化出題", JOptionPane.WARNING_MESSAGE);
            onExit.run();
            return;
        }
        this.quizItems    = items;
        this.currentIndex = 0;
        this.correctCount = 0;
        loadQuestion();
    }

    // ── 頂部 ──────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("綜合測驗");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JButton exitBtn = new JButton("退出測驗");
        exitBtn.setFont(AppColors.FONT_BTN);
        exitBtn.setBackground(AppColors.BG_CARD);
        exitBtn.setForeground(AppColors.TEXT_RED);
        exitBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(5, 14, 5, 14)
        ));
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { exitBtn.setBackground(new Color(0xFFEBEB)); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { exitBtn.setBackground(AppColors.BG_CARD); }
        });
        exitBtn.addActionListener(e -> {
            if (UIUtils.showConfirm(this, "確定要退出測驗嗎？進度將不會保留。", "退出確認"))
                onExit.run();
        });

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);
        west.add(title);
        p.add(west,    BorderLayout.WEST);
        p.add(exitBtn, BorderLayout.EAST);
        return p;
    }

    // ── 主體（進度列 + CardLayout 切換版型） ──────────────────
    private JPanel buildBody() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(22, 28, 22, 28)
        ));

        // 進度列
        JPanel progRow = new JPanel(new BorderLayout());
        progRow.setOpaque(false);
        progressLabel = new JLabel("第 1 / ? 題");
        progressLabel.setFont(AppColors.FONT_SMALL);
        progressLabel.setForeground(AppColors.TEXT_SECONDARY);
        scoreLabel = new JLabel("答對 0 · 答錯 0");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        progRow.add(progressLabel, BorderLayout.WEST);
        progRow.add(scoreLabel,    BorderLayout.EAST);

        // CardLayout 切換兩種版型
        contentCard = new CardLayout();
        contentArea = new JPanel(contentCard);
        contentArea.setOpaque(false);
        contentArea.add(buildVocabLayout(), "vocab");
        contentArea.add(buildFillLayout(),  "fill");

        card.add(progRow,     BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);
        return card;
    }

    // ── 單字版型（仿 VocabQuizPanel）─────────────────────────
    private JPanel buildVocabLayout() {
        // Chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.setBorder(new EmptyBorder(6, 0, 0, 0));
        vModeChip = chip("英翻中", new Color(0xE3F2FD), new Color(0x1565C0));
        vFamChip  = chip("熟悉度 ★☆☆☆☆", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        vModeChip.setFont(AppColors.FONT_BODY);
        vFamChip.setFont(AppColors.FONT_BODY);
        chips.add(vModeChip); chips.add(vFamChip);

        vQuestionLabel = new JLabel("", SwingConstants.CENTER);
        vQuestionLabel.setFont(AppColors.FONT_WORD);
        vQuestionLabel.setForeground(AppColors.TEXT_PRIMARY);

        vHintLabel = new JLabel("選出正確答案", SwingConstants.CENTER);
        vHintLabel.setFont(AppColors.FONT_SMALL);
        vHintLabel.setForeground(AppColors.TEXT_SECONDARY);

        JPanel qBlock = new JPanel(new GridLayout(3, 1, 0, 6));
        qBlock.setOpaque(false);
        qBlock.add(chips); qBlock.add(vQuestionLabel); qBlock.add(vHintLabel);

        // 選項
        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            vOptBtns[i] = makeOptBtn();
            final int fi = i;
            vOptBtns[i].addActionListener(e -> onVocabClick(fi));
            optGrid.add(vOptBtns[i]);
        }

        // Feedback
        vFeedbackLabel = new JLabel("", SwingConstants.CENTER);
        vFeedbackLabel.setFont(AppColors.FONT_BODY);
        vFeedbackLabel.setPreferredSize(new Dimension(0, 26));
        vFeedbackLabel.setMinimumSize(new Dimension(0, 26));

        // 底部按鈕
        vPronounceBtn = makeBtn("🔊", AppColors.BG_CARD, AppColors.TEXT_PRIMARY);
        vPronounceBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        vPronounceBtn.addActionListener(e -> pronounce());
        vNextBtn = makeBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        vNextBtn.setVisible(false);
        vNextBtn.addActionListener(e -> { currentIndex++; loadQuestion(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(vPronounceBtn); right.add(vNextBtn);

        SwingUtilities.invokeLater(() -> {
            int h = vNextBtn.getPreferredSize().height;
            Dimension pd = vPronounceBtn.getPreferredSize();
            vPronounceBtn.setPreferredSize(new Dimension(pd.width, h));
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(right, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(qBlock,        BorderLayout.NORTH);
        center.add(optGrid,       BorderLayout.CENTER);
        center.add(vFeedbackLabel,BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.add(center, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ── 填空版型（仿 FillBlankPanel）─────────────────────────
    private JLabel fModeChip;

    private JPanel buildFillLayout() {
        fModeChip = chip("填空", new Color(0xF3E5F5), new Color(0x7B1FA2));
        fModeChip.setFont(AppColors.FONT_BODY);
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setOpaque(false);
        chipRow.setBorder(new EmptyBorder(6, 0, 0, 0));
        chipRow.add(fModeChip);

        fSentenceArea = new JTextArea();
        fSentenceArea.setFont(new Font("Serif", Font.BOLD, 20));
        fSentenceArea.setEditable(false);
        fSentenceArea.setLineWrap(true);
        fSentenceArea.setWrapStyleWord(true);
        fSentenceArea.setOpaque(false);
        fSentenceArea.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            fOptBtns[i] = makeOptBtn();
            final int fi = i;
            fOptBtns[i].addActionListener(e -> onFillClick(fi));
            optGrid.add(fOptBtns[i]);
        }

        fFeedbackLabel = new JLabel("", SwingConstants.CENTER);
        fFeedbackLabel.setFont(AppColors.FONT_BODY);
        fFeedbackLabel.setPreferredSize(new Dimension(0, 26));
        fFeedbackLabel.setMinimumSize(new Dimension(0, 26));

        fAnalysisPane = new JTextPane();
        fAnalysisPane.setEditable(false);
        fAnalysisPane.setBackground(new Color(0xF5F6F8));
        fAnalysisPane.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, new Color(0x4A5568)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        fAnalysisPane.setVisible(false);

        fNextBtn = makeBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        fNextBtn.setVisible(false);
        fNextBtn.addActionListener(e -> { currentIndex++; loadQuestion(); });

        JPanel nextRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextRow.setOpaque(false);
        nextRow.add(fNextBtn);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.setPreferredSize(new Dimension(0, 130));
        south.setMinimumSize(new Dimension(0, 130));
        south.add(fFeedbackLabel, BorderLayout.NORTH);
        south.add(fAnalysisPane,  BorderLayout.CENTER);
        south.add(nextRow,        BorderLayout.SOUTH);

        JPanel qBlock = new JPanel(new BorderLayout(0, 8));
        qBlock.setOpaque(false);
        qBlock.add(chipRow,      BorderLayout.NORTH);
        qBlock.add(fSentenceArea,BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(qBlock,  BorderLayout.NORTH);
        center.add(optGrid, BorderLayout.CENTER);
        center.add(south,   BorderLayout.SOUTH);

        return center;
    }

    // ── 載入題目 ──────────────────────────────────────────────
    private void loadQuestion() {
        if (quizItems == null || currentIndex >= quizItems.size()) {
            showResult(); return;
        }
        QuizItem item = quizItems.get(currentIndex);
        Vocabulary v  = item.vocab;
        Mode       m  = item.mode;
        QuizManager qm = ctrl.getQuizManager();

        progressLabel.setText("第 " + (currentIndex + 1) + " / " + quizItems.size() + " 題");
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex - correctCount));

        java.util.List<String> opts = qm.generateOptions(v, m);

        if (m == Mode.FILL_BLANK) {
            contentCard.show(contentArea, "fill");

            fSentenceArea.setText(qm.getQuestion(v, m));
            fFeedbackLabel.setText("");
            fAnalysisPane.setVisible(false);
            fNextBtn.setVisible(false);
            answered = false;
            for (int i = 0; i < 4; i++) {
                fOptBtns[i].setText(i < opts.size() ? opts.get(i) : "—");
                fOptBtns[i].setBackground(new Color(0xF0EAD8));
                fOptBtns[i].setForeground(AppColors.TEXT_PRIMARY);
            }
        } else {
            contentCard.show(contentArea, "vocab");

            // chip
            String modeText; Color chipBg, chipFg;
            if (m == Mode.EN_TO_CN) {
                modeText="英翻中"; chipBg=new Color(0xE3F2FD); chipFg=new Color(0x1565C0);
            } else {
                modeText="中翻英"; chipBg=new Color(0xE8F5E9); chipFg=new Color(0x2E7D32);
            }
            vModeChip.setText(modeText);
            vModeChip.setBackground(chipBg);
            vModeChip.setForeground(chipFg);
            vFamChip.setText("熟悉度 " + v.getFamiliarityStars());

            String q = qm.getQuestion(v, m);
            vQuestionLabel.setText(q.length() > 40
                ? "<html><div style='text-align:center'>" + q + "</div></html>" : q);
            vQuestionLabel.setFont(q.length() > 20 ? AppColors.FONT_HEAD : AppColors.FONT_WORD);
            vHintLabel.setText(m == Mode.CN_TO_EN ? "選出正確的英文單字" :
                               m == Mode.PHRASE   ? "選出正確的片語"     : "選出正確的中文意思");

            vFeedbackLabel.setText("");
            vNextBtn.setVisible(false);
            answered = false;
            for (int i = 0; i < 4; i++) {
                vOptBtns[i].setText(i < opts.size() ? opts.get(i) : "—");
                vOptBtns[i].setBackground(new Color(0xF0EAD8));
                vOptBtns[i].setForeground(AppColors.TEXT_PRIMARY);
            }
        }
    }

    // ── 單字答題 ──────────────────────────────────────────────
    private void onVocabClick(int idx) {
        if (quizItems == null || currentIndex >= quizItems.size() || answered) return;
        answered = true;
        QuizItem item   = quizItems.get(currentIndex);
        String   sel    = vOptBtns[idx].getText();
        String   answer = ctrl.getQuizManager().getAnswer(item.vocab, item.mode);
        boolean  ok     = sel.equals(answer);

        ctrl.submitAnswer(item.vocab, ok);

        if (ok) {
            correctCount++;
            vOptBtns[idx].setBackground(new Color(0xC8E6C9));
            vOptBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            vFeedbackLabel.setText("正確！  " + item.vocab.getWord() + "：" + item.vocab.getMeaning());
            vFeedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            vOptBtns[idx].setBackground(new Color(0xFFCDD2));
            vOptBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            vFeedbackLabel.setText("正確答案：" + answer);
            vFeedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : vOptBtns)
                if (b.getText().equals(answer)) {
                    b.setBackground(new Color(0xC8E6C9));
                    b.setForeground(AppColors.TEXT_PRIMARY);
                }
        }
        vNextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex + 1 - correctCount));
    }

    // ── 填空答題 ──────────────────────────────────────────────
    private void onFillClick(int idx) {
        if (quizItems == null || currentIndex >= quizItems.size() || answered) return;
        answered = true;
        QuizItem item   = quizItems.get(currentIndex);
        String   sel    = fOptBtns[idx].getText();
        String   answer = ctrl.getQuizManager().getAnswer(item.vocab, item.mode);
        boolean  ok     = sel.equals(answer);

        ctrl.submitAnswer(item.vocab, ok);

        if (ok) {
            correctCount++;
            fOptBtns[idx].setBackground(new Color(0xC8E6C9));
            fOptBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            fFeedbackLabel.setText("正確答案：" + answer);
            fFeedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            fOptBtns[idx].setBackground(new Color(0xFFCDD2));
            fOptBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            fFeedbackLabel.setText("正確答案：" + answer);
            fFeedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : fOptBtns)
                if (b.getText().equals(answer)) {
                    b.setBackground(new Color(0xC8E6C9));
                    b.setForeground(AppColors.TEXT_PRIMARY);
                }
        }

        String pos = item.vocab.getPos().isEmpty() ? "" : "（" + item.vocab.getPos() + "）";
        setAnalysisText(fAnalysisPane, item.vocab, answer);
        fAnalysisPane.setVisible(true);
        fNextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex + 1 - correctCount));
    }

    private void setAnalysisText(JTextPane pane, model.Vocabulary v, String answer) {
        pane.setText("");
        javax.swing.text.StyledDocument doc = pane.getStyledDocument();
        javax.swing.text.SimpleAttributeSet normal = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setFontFamily(normal, "Microsoft JhengHei");
        javax.swing.text.StyleConstants.setFontSize(normal, 13);
        javax.swing.text.SimpleAttributeSet bold = new javax.swing.text.SimpleAttributeSet(normal);
        javax.swing.text.StyleConstants.setBold(bold, true);
        javax.swing.text.StyleConstants.setForeground(bold, Color.BLACK);
        try {
            doc.insertString(doc.getLength(), answer, normal);
            String pos = v.getPos().isEmpty() ? "" : "（" + v.getPos() + "）";
            doc.insertString(doc.getLength(), "  " + pos + "  " + v.getMeaning(), normal);
            if (!v.getExample().isEmpty()) {
                doc.insertString(doc.getLength(), "\n例：", normal);
                String ex = v.getExample();
                int idx = ex.toLowerCase().indexOf(answer.toLowerCase());
                if (idx >= 0) {
                    doc.insertString(doc.getLength(), ex.substring(0, idx), normal);
                    doc.insertString(doc.getLength(), ex.substring(idx, idx + answer.length()), bold);
                    doc.insertString(doc.getLength(), ex.substring(idx + answer.length()), normal);
                } else {
                    doc.insertString(doc.getLength(), ex, normal);
                }
            }
        } catch (Exception ignored) {}
    }

    private void pronounce() {
        if (quizItems == null || currentIndex >= quizItems.size()) return;
        String word = quizItems.get(currentIndex).vocab.getWord();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"say", word});
        } catch (Exception ignored) {}
    }

    private void showResult() {
        int total = quizItems == null ? 0 : quizItems.size();
        QuizResultDialog.show(this, correctCount, total, "綜合測驗", onExit);
    }

    // ── 工具 ──────────────────────────────────────────────────
    private JButton makeOptBtn() {
        JButton b = new JButton();
        b.setFont(AppColors.FONT_BODY);
        b.setBackground(new Color(0xF0EAD8));
        b.setForeground(AppColors.TEXT_PRIMARY);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 18, 6, 18)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        UIUtils.addHover(b, bg);
        return b;
    }

    private JLabel chip(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }
}
