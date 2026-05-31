package ui;

import controller.DashboardController;
import manager.QuizManager;
import manager.QuizManager.Mode;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 句子填空測驗（含來源選擇器）
 */
public class FillBlankPanel extends JPanel {

    private final DashboardController ctrl;
    private CardLayout inner;
    private JPanel     innerArea;
    private QuizSourceSelector selector;

    // 測驗狀態
    private List<Vocabulary> quizList;
    private int  currentIndex = 0;
    private int  correctCount = 0;

    // UI
    private JLabel  progressLabel, scoreLabel;
    private JTextArea sentenceArea;
    private JButton[] optBtns = new JButton[4];
    private JLabel  feedbackLabel;
    private JTextArea analysisArea;
    private JButton nextBtn;

    public FillBlankPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);

        inner = new CardLayout();
        innerArea = new JPanel(inner);
        innerArea.setOpaque(false);

        selector = new QuizSourceSelector(ctrl, "句子填空測驗", this::startFromSource);
        innerArea.add(selector,        "selector");
        innerArea.add(buildQuizPage(), "quiz");

        add(innerArea, BorderLayout.CENTER);
        showSelector();
    }

    public void showSelector() {
        selector.refreshSources();
        inner.show(innerArea, "selector");
    }

    private void startFromSource(List<Vocabulary> words) {
        // 所有單字均可出填空題（無例句時自動生成例句模板）
        List<Vocabulary> pool = new ArrayList<>(words);
        Collections.shuffle(pool);
        quizList     = new ArrayList<>(pool.subList(0, Math.min(20, pool.size())));
        currentIndex = 0;
        correctCount = 0;
        inner.show(innerArea, "quiz");
        loadQuestion();
    }

    private JPanel buildQuizPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));

        // TopBar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel title = new JLabel("句子填空測驗");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JButton backBtn = makeBtn("退出測驗", AppColors.BG_CARD, AppColors.TEXT_RED);
        backBtn.addActionListener(e -> showSelector());
        right.add(backBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        // 卡片
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(22, 28, 22, 28)
        ));

        JPanel progRow = new JPanel(new BorderLayout());
        progRow.setOpaque(false);
        progressLabel = new JLabel("第 1 / 20 題");
        progressLabel.setFont(AppColors.FONT_SMALL);
        progressLabel.setForeground(AppColors.TEXT_SECONDARY);
        scoreLabel = new JLabel("");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        progRow.add(progressLabel, BorderLayout.WEST);
        progRow.add(scoreLabel,    BorderLayout.EAST);

        sentenceArea = new JTextArea();
        sentenceArea.setFont(new Font("Serif", Font.PLAIN, 16));
        sentenceArea.setEditable(false);
        sentenceArea.setLineWrap(true);
        sentenceArea.setWrapStyleWord(true);
        sentenceArea.setBackground(new Color(0xF0EAD8));
        sentenceArea.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1), new EmptyBorder(12, 14, 12, 14)));

        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            optBtns[i] = new JButton();
            optBtns[i].setFont(AppColors.FONT_BODY);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
            optBtns[i].setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER_SOFT,1,true), new EmptyBorder(12,10,12,10)));
            optBtns[i].setFocusPainted(false);
            optBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int fi = i;
            optBtns[i].addActionListener(e -> onOptionClick(fi));
            optGrid.add(optBtns[i]);
        }

        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(AppColors.FONT_BODY);

        analysisArea = new JTextArea();
        analysisArea.setFont(AppColors.FONT_SMALL);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setBackground(new Color(0xF5F6F8));
        analysisArea.setBorder(new CompoundBorder(
            new MatteBorder(0,3,0,0, new Color(0x4A5568)),
            new EmptyBorder(8,12,8,12)));
        analysisArea.setVisible(false);

        nextBtn = makeBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> nextQuestion());

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(feedbackLabel, BorderLayout.NORTH);
        south.add(analysisArea,  BorderLayout.CENTER);
        JPanel nextRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextRow.setOpaque(false); nextRow.add(nextBtn);
        south.add(nextRow, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(sentenceArea, BorderLayout.NORTH);
        center.add(optGrid,      BorderLayout.CENTER);
        center.add(south,        BorderLayout.SOUTH);

        card.add(progRow, BorderLayout.NORTH);
        card.add(center,  BorderLayout.CENTER);

        page.add(topBar, BorderLayout.NORTH);
        page.add(card,   BorderLayout.CENTER);
        return page;
    }

    private void loadQuestion() {
        if (quizList == null || quizList.isEmpty() || currentIndex >= quizList.size()) {
            if (currentIndex >= (quizList == null ? 0 : quizList.size())) showResult();
            return;
        }
        Vocabulary v = quizList.get(currentIndex);
        QuizManager qm = ctrl.getQuizManager();

        progressLabel.setText("第 " + (currentIndex+1) + " / " + quizList.size() + " 題");
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex - correctCount));

        String q = qm.getQuestion(v, Mode.FILL_BLANK);
        sentenceArea.setText(q);
        feedbackLabel.setText("");
        analysisArea.setVisible(false);
        nextBtn.setVisible(false);

        List<String> opts = qm.generateOptions(v, Mode.FILL_BLANK);
        for (int i = 0; i < 4; i++) {
            optBtns[i].setText(i < opts.size() ? opts.get(i) : "—");
            optBtns[i].setEnabled(true);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
        }
    }

    private void onOptionClick(int idx) {
        Vocabulary v   = quizList.get(currentIndex);
        String sel     = optBtns[idx].getText();
        String answer  = ctrl.getQuizManager().getAnswer(v, Mode.FILL_BLANK);
        boolean correct = sel.equals(answer);
        ctrl.submitAnswer(v, correct);
        for (JButton b : optBtns) b.setEnabled(false);
        if (correct) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(new Color(0x1B5E20));
            feedbackLabel.setText("正確！");
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(new Color(0xB71C1C));
            feedbackLabel.setText("正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : optBtns) if (b.getText().equals(answer)) { b.setBackground(new Color(0xC8E6C9)); b.setForeground(new Color(0x1B5E20)); }
        }
        StringBuilder sb = new StringBuilder(v.getWord());
        if (!v.getPos().isEmpty()) sb.append("（").append(v.getPos()).append("）");
        sb.append("  ").append(v.getMeaning());
        if (!v.getExample().isEmpty()) sb.append("\n例：").append(v.getExample());
        analysisArea.setText(sb.toString());
        analysisArea.setVisible(true);
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex+1 - correctCount));
    }

    private void nextQuestion() { currentIndex++; loadQuestion(); }

    private void showResult() {
        int total = quizList == null ? 0 : quizList.size();
        QuizResultDialog.show(this, correctCount, total, "句子填空測驗", this::showSelector);
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(bg); b.setForeground(fg);
        b.setBorder(new CompoundBorder(new LineBorder(AppColors.BORDER_SOFT,1,true), new EmptyBorder(5,14,5,14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
