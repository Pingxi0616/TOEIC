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
 * 綜合測驗面板：統一處理 英翻中 / 中翻英 / 片語 / 填空 四種題型
 * 由 CustomQuizPanel 透過 ToeicApp 呼叫 startQuiz(List<QuizItem>)
 */
public class MixedQuizPanel extends JPanel {

    private final DashboardController ctrl;
    private final Runnable            onExit;

    private List<QuizItem> quizItems;
    private int currentIndex = 0;
    private int correctCount = 0;

    // ── UI ────────────────────────────────────────────────────
    private JLabel    progressLabel, scoreLabel, modeChip;
    private JTextArea questionArea;
    private JButton[] optBtns  = new JButton[4];
    private JLabel    feedbackLabel;
    private JButton   nextBtn;

    public MixedQuizPanel(DashboardController ctrl, Runnable onExit) {
        this.ctrl   = ctrl;
        this.onExit = onExit;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    /** 外部呼叫以啟動測驗 */
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

    // ── 頂部（標題 + 退出）────────────────────────────────────
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
        exitBtn.addActionListener(e -> {
            if (UIUtils.showConfirm(this, "確定要退出測驗嗎？進度將不會保留。", "退出確認"))
                onExit.run();
        });

        p.add(title,   BorderLayout.WEST);
        p.add(exitBtn, BorderLayout.EAST);
        return p;
    }

    // ── 測驗主體 ──────────────────────────────────────────────
    private JPanel buildCenter() {
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
        scoreLabel = new JLabel("答對 0");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        progRow.add(progressLabel, BorderLayout.WEST);
        progRow.add(scoreLabel,    BorderLayout.EAST);

        // 題型 Chip
        modeChip = new JLabel("英翻中", SwingConstants.CENTER);
        modeChip.setFont(AppColors.FONT_SMALL);
        modeChip.setOpaque(true);
        modeChip.setBorder(new EmptyBorder(3, 10, 3, 10));

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chipRow.setOpaque(false);
        chipRow.add(modeChip);

        // 題目（JTextArea 可換行，兼容填空的長句子）
        questionArea = new JTextArea();
        questionArea.setFont(AppColors.FONT_WORD);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setOpaque(false);
        questionArea.setBorder(new EmptyBorder(16, 0, 8, 0));
        questionArea.setForeground(AppColors.TEXT_PRIMARY);

        JPanel qBlock = new JPanel(new BorderLayout(0, 6));
        qBlock.setOpaque(false);
        qBlock.add(chipRow,      BorderLayout.NORTH);
        qBlock.add(questionArea, BorderLayout.CENTER);

        // 選項（2×2）
        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            optBtns[i] = new JButton();
            optBtns[i].setFont(AppColors.FONT_BODY);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
            optBtns[i].setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER_SOFT, 1, true),
                new EmptyBorder(12, 10, 12, 10)
            ));
            optBtns[i].setFocusPainted(false);
            optBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int fi = i;
            optBtns[i].addActionListener(e -> onOptionClick(fi));
            optGrid.add(optBtns[i]);
        }

        // 回饋 + 下一題
        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(AppColors.FONT_BODY);

        nextBtn = new JButton("下一題 →");
        nextBtn.setFont(AppColors.FONT_BTN);
        nextBtn.setBackground(AppColors.BTN_PRIMARY);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(6, 18, 6, 18)
        ));
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> { currentIndex++; loadQuestion(); });

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(feedbackLabel, BorderLayout.CENTER);
        JPanel nextWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nextWrap.setOpaque(false);
        nextWrap.add(nextBtn);
        bottomRow.add(nextWrap, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(qBlock,     BorderLayout.NORTH);
        center.add(optGrid,    BorderLayout.CENTER);
        center.add(bottomRow,  BorderLayout.SOUTH);

        card.add(progRow, BorderLayout.NORTH);
        card.add(center,  BorderLayout.CENTER);
        return card;
    }

    // ── 載入題目 ──────────────────────────────────────────────
    private void loadQuestion() {
        if (quizItems == null || currentIndex >= quizItems.size()) {
            showResult();
            return;
        }

        QuizItem item = quizItems.get(currentIndex);
        Vocabulary v  = item.vocab;
        Mode       m  = item.mode;
        QuizManager qm = ctrl.getQuizManager();

        progressLabel.setText("第 " + (currentIndex + 1) + " / " + quizItems.size() + " 題");
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex - correctCount));

        // 題型 chip 顏色與文字
        String modeText;
        Color  chipBg, chipFg;
        switch (m) {
            case EN_TO_CN -> { modeText="英翻中"; chipBg=new Color(0xE3F2FD); chipFg=new Color(0x1565C0); }
            case CN_TO_EN -> { modeText="中翻英"; chipBg=new Color(0xE8F5E9); chipFg=new Color(0x2E7D32); }
            case PHRASE   -> { modeText="片語";   chipBg=new Color(0xFFF3E0); chipFg=new Color(0xE65100); }
            default       -> { modeText="填空";   chipBg=new Color(0xF3E5F5); chipFg=new Color(0x7B1FA2); }
        }
        modeChip.setText(modeText);
        modeChip.setBackground(chipBg);
        modeChip.setForeground(chipFg);

        // 題目
        String q = qm.getQuestion(v, m);
        questionArea.setFont(q.length() > 30 ? AppColors.FONT_HEAD : AppColors.FONT_WORD);
        questionArea.setText(q);

        // 選項
        feedbackLabel.setText("");
        nextBtn.setVisible(false);
        java.util.List<String> opts = qm.generateOptions(v, m);
        for (int i = 0; i < 4; i++) {
            optBtns[i].setText(i < opts.size() ? opts.get(i) : "—");
            optBtns[i].setEnabled(true);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
        }
    }

    // ── 答題 ──────────────────────────────────────────────────
    private void onOptionClick(int idx) {
        if (quizItems == null || currentIndex >= quizItems.size()) return;
        QuizItem item   = quizItems.get(currentIndex);
        String   sel    = optBtns[idx].getText();
        String   answer = ctrl.getQuizManager().getAnswer(item.vocab, item.mode);
        boolean  ok     = sel.equals(answer);

        ctrl.submitAnswer(item.vocab, ok);
        for (JButton b : optBtns) b.setEnabled(false);

        if (ok) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(new Color(0x1B5E20));
            feedbackLabel.setText("正確！  " + item.vocab.getWord() + "：" + item.vocab.getMeaning());
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(new Color(0xB71C1C));
            feedbackLabel.setText("正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : optBtns)
                if (b.getText().equals(answer)) {
                    b.setBackground(new Color(0xC8E6C9));
                    b.setForeground(new Color(0x1B5E20));
                }
        }
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex + 1 - correctCount));
    }

    // ── 結果 ──────────────────────────────────────────────────
    private void showResult() {
        int total = quizItems == null ? 0 : quizItems.size();
        QuizResultDialog.show(this, correctCount, total, "綜合測驗", onExit);
    }
}
