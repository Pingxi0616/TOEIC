package ui;

import controller.DashboardController;
import manager.QuizManager;
import manager.QuizManager.Mode;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class FillBlankPanel extends JPanel {

    private final DashboardController ctrl;
    private List<Vocabulary> quizList;
    private int currentIndex = 0;
    private int correctCount = 0;

    private JLabel progressLabel, scoreLabel;
    private JTextArea sentenceArea;
    private JButton[] optBtns = new JButton[4];
    private JLabel feedbackLabel;
    private JTextArea analysisArea;
    private JButton nextBtn;
    private JToggleButton vocabModeBtn, grammarModeBtn;

    public FillBlankPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        startQuiz();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("句子填空測驗");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        vocabModeBtn   = makeToggleBtn("單字導向");
        grammarModeBtn = makeToggleBtn("文法導向");
        vocabModeBtn.setSelected(true);
        bg.add(vocabModeBtn);
        bg.add(grammarModeBtn);
        vocabModeBtn.addActionListener(e -> startQuiz());
        grammarModeBtn.addActionListener(e -> startQuiz());
        right.add(vocabModeBtn);
        right.add(grammarModeBtn);
        JButton end = new JButton("結束");
        end.setFont(AppColors.FONT_BTN);
        end.addActionListener(e -> showResult());
        right.add(end);

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

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
        progressLabel = new JLabel("第 1 / 20 題");
        progressLabel.setFont(AppColors.FONT_SMALL);
        progressLabel.setForeground(AppColors.TEXT_SECONDARY);
        scoreLabel = new JLabel("");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        progRow.add(progressLabel, BorderLayout.WEST);
        progRow.add(scoreLabel,    BorderLayout.EAST);

        // 例句區（含挖空）
        sentenceArea = new JTextArea();
        sentenceArea.setFont(new Font("Serif", Font.PLAIN, 16));
        sentenceArea.setEditable(false);
        sentenceArea.setLineWrap(true);
        sentenceArea.setWrapStyleWord(true);
        sentenceArea.setBackground(new Color(0xF0EAD8));
        sentenceArea.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        // 選項
        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            optBtns[i] = new JButton();
            optBtns[i].setFont(AppColors.FONT_BODY);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER_SOFT, 1, true),
                new EmptyBorder(10, 10, 10, 10)
            ));
            optBtns[i].setFocusPainted(false);
            optBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int idx = i;
            optBtns[i].addActionListener(e -> onOptionClick(idx));
            optGrid.add(optBtns[i]);
        }

        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(AppColors.FONT_BODY);

        // 解析區
        analysisArea = new JTextArea();
        analysisArea.setFont(AppColors.FONT_BODY);
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setBackground(new Color(0xE8F4FD));
        analysisArea.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, new Color(0x2196F3)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        analysisArea.setVisible(false);

        nextBtn = new JButton("下一題 →");
        nextBtn.setFont(AppColors.FONT_BTN);
        nextBtn.setBackground(AppColors.BTN_PRIMARY);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> nextQuestion());

        JPanel southPanel = new JPanel(new BorderLayout(0, 8));
        southPanel.setOpaque(false);
        southPanel.add(feedbackLabel, BorderLayout.NORTH);
        southPanel.add(analysisArea,  BorderLayout.CENTER);
        JPanel nextRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextRow.setOpaque(false);
        nextRow.add(nextBtn);
        southPanel.add(nextRow, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(sentenceArea, BorderLayout.NORTH);
        center.add(optGrid,      BorderLayout.CENTER);
        center.add(southPanel,   BorderLayout.SOUTH);

        card.add(progRow,  BorderLayout.NORTH);
        card.add(center,   BorderLayout.CENTER);
        return card;
    }

    public void startQuiz() {
        List<Vocabulary> pool = ctrl.getQuizManager().getWordsWithExample();
        if (pool.isEmpty()) pool = ctrl.getVocabList(); // fallback
        java.util.Collections.shuffle(pool);
        quizList     = pool.subList(0, Math.min(20, pool.size()));
        currentIndex = 0;
        correctCount = 0;
        loadQuestion();
    }

    private void loadQuestion() {
        if (quizList.isEmpty() || currentIndex >= quizList.size()) { showResult(); return; }
        Vocabulary v = quizList.get(currentIndex);
        QuizManager qm = ctrl.getQuizManager();

        progressLabel.setText("第 " + (currentIndex + 1) + " / " + quizList.size() + " 題");
        scoreLabel.setText("答對 " + correctCount);
        sentenceArea.setText(qm.getQuestion(v, Mode.FILL_BLANK));
        feedbackLabel.setText("");
        analysisArea.setVisible(false);
        nextBtn.setVisible(false);

        List<String> opts = qm.generateOptions(v, Mode.FILL_BLANK);
        for (int i = 0; i < 4; i++) {
            optBtns[i].setText(i < opts.size() ? opts.get(i) : "");
            optBtns[i].setEnabled(true);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
        }
    }

    private void onOptionClick(int idx) {
        Vocabulary v      = quizList.get(currentIndex);
        String selected   = optBtns[idx].getText();
        String answer     = ctrl.getQuizManager().getAnswer(v, Mode.FILL_BLANK);
        boolean correct   = selected.equals(answer);

        ctrl.submitAnswer(v, correct);
        for (JButton b : optBtns) b.setEnabled(false);

        if (correct) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(new Color(0x1B5E20));
            feedbackLabel.setText("✓ 正確！");
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(new Color(0xB71C1C));
            feedbackLabel.setText("✗ 正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : optBtns)
                if (b.getText().equals(answer)) b.setBackground(new Color(0xC8E6C9));
        }

        // 顯示解析
        String analysis = v.getWord() + "（" + v.getPos() + "）" + v.getMeaning();
        if (v.getExample() != null && !v.getExample().isEmpty())
            analysis += "\n例句：" + v.getExample();
        analysisArea.setText(analysis);
        analysisArea.setVisible(true);
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex + 1 - correctCount));
    }

    private void nextQuestion() { currentIndex++; loadQuestion(); }

    private void showResult() {
        int total = quizList == null ? 0 : Math.min(currentIndex, quizList.size());
        JOptionPane.showMessageDialog(this,
            String.format("測驗結束！\n答對：%d  答錯：%d\n正確率：%.0f%%",
                correctCount, total - correctCount,
                total > 0 ? (correctCount * 100.0 / total) : 0),
            "結果", JOptionPane.INFORMATION_MESSAGE);
        startQuiz();
    }

    private JToggleButton makeToggleBtn(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(4, 12, 4, 12)
        ));
        b.setBackground(AppColors.BG_CARD);
        b.setForeground(AppColors.TEXT_SECONDARY);
        b.addChangeListener(e -> {
            b.setBackground(b.isSelected() ? AppColors.BTN_PRIMARY : AppColors.BG_CARD);
            b.setForeground(b.isSelected() ? Color.WHITE : AppColors.TEXT_SECONDARY);
        });
        return b;
    }
}
