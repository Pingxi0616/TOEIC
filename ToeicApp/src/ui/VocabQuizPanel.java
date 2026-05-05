package ui;

import controller.DashboardController;
import manager.QuizManager;
import manager.QuizManager.Mode;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class VocabQuizPanel extends JPanel {

    private final DashboardController ctrl;
    private Mode currentMode = Mode.EN_TO_CN;

    // 測驗狀態
    private List<Vocabulary> quizList;
    private int currentIndex = 0;
    private int correctCount = 0;

    // UI 元件
    private JLabel modeLabel, progressLabel, scoreLabel;
    private JLabel questionLabel;
    private JLabel chipReview, chipFam, chipNext;
    private JButton[] optBtns = new JButton[4];
    private JLabel feedbackLabel;
    private JButton nextBtn, pronounceBtn;
    private JPanel chipPanel;

    public VocabQuizPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(),      BorderLayout.NORTH);
        add(buildCenter(),      BorderLayout.CENTER);
    }

    // ── 頂部：標題 + 模式切換 ─────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("單字片語測驗");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        modeRow.setOpaque(false);
        String[] modes = {"英翻中", "中翻英", "片語"};
        Mode[]   modeVals = {Mode.EN_TO_CN, Mode.CN_TO_EN, Mode.PHRASE};
        for (int i = 0; i < modes.length; i++) {
            final Mode m = modeVals[i];
            JButton b = new JButton(modes[i]);
            b.setFont(AppColors.FONT_BTN);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> { currentMode = m; startQuiz(false); });
            styleModeBtn(b, m == currentMode);
            modeRow.add(b);
        }
        JButton endBtn = styledBtn("結束測驗", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        endBtn.addActionListener(e -> showResult());
        modeRow.add(endBtn);

        p.add(title,   BorderLayout.WEST);
        p.add(modeRow, BorderLayout.EAST);
        return p;
    }

    // ── 中央測驗區 ─────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(22, 28, 22, 28)
        ));

        card.add(buildProgressRow(), BorderLayout.NORTH);
        card.add(buildQuestionArea(), BorderLayout.CENTER);
        card.add(buildBottomRow(),   BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildProgressRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        progressLabel = new JLabel("第 1 / 20 題");
        progressLabel.setFont(AppColors.FONT_SMALL);
        progressLabel.setForeground(AppColors.TEXT_SECONDARY);
        scoreLabel = new JLabel("答對 0 · 答錯 0");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        p.add(progressLabel, BorderLayout.WEST);
        p.add(scoreLabel,    BorderLayout.EAST);
        return p;
    }

    private JPanel buildQuestionArea() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        // Chip 標籤列
        chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chipPanel.setOpaque(false);
        chipReview = makeChip("今日待複習", new Color(0xFAEEDA), new Color(0x633806));
        chipFam    = makeChip("熟悉度 ★☆☆☆☆", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        chipNext   = makeChip("答對 +1天", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        chipPanel.add(chipReview);
        chipPanel.add(chipFam);
        chipPanel.add(chipNext);

        // 題目
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(AppColors.FONT_WORD);
        questionLabel.setForeground(AppColors.TEXT_PRIMARY);

        JLabel hint = new JLabel("選出正確答案", SwingConstants.CENTER);
        hint.setFont(AppColors.FONT_SMALL);
        hint.setForeground(AppColors.TEXT_SECONDARY);

        JPanel qCenter = new JPanel(new GridLayout(3, 1, 0, 6));
        qCenter.setOpaque(false);
        qCenter.add(chipPanel);
        qCenter.add(questionLabel);
        qCenter.add(hint);

        // 四個選項
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
            optBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int idx = i;
            optBtns[i].addActionListener(e -> onOptionClick(idx));
            optGrid.add(optBtns[i]);
        }

        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(AppColors.FONT_BODY);

        p.add(qCenter,      BorderLayout.NORTH);
        p.add(optGrid,      BorderLayout.CENTER);
        p.add(feedbackLabel,BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildBottomRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel srInfo = new JLabel("<html>答對 → 間隔延長<br>答錯 → 明天再複習</html>");
        srInfo.setFont(AppColors.FONT_SMALL);
        srInfo.setForeground(AppColors.TEXT_SECONDARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        pronounceBtn = styledBtn("🔊 發音", AppColors.BG_CARD, AppColors.TEXT_PRIMARY);
        pronounceBtn.addActionListener(e -> pronounce());

        nextBtn = styledBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> nextQuestion());

        right.add(pronounceBtn);
        right.add(nextBtn);
        p.add(srInfo, BorderLayout.WEST);
        p.add(right,  BorderLayout.EAST);
        return p;
    }

    // ── 測驗邏輯 ──────────────────────────────────────────────
    public void startQuiz(boolean prioritizeWrong) {
        quizList     = ctrl.getQuizManager().generateQuizList(20, prioritizeWrong);
        currentIndex = 0;
        correctCount = 0;
        loadQuestion();
    }

    private void loadQuestion() {
        if (quizList == null || quizList.isEmpty()) {
            questionLabel.setText("題庫為空，請先新增單字！");
            return;
        }
        if (currentIndex >= quizList.size()) { showResult(); return; }

        Vocabulary v = quizList.get(currentIndex);
        QuizManager qm = ctrl.getQuizManager();

        // 進度
        progressLabel.setText("第 " + (currentIndex + 1) + " / " + quizList.size() + " 題");
        int wrong = quizList.size() - correctCount - currentIndex + correctCount;
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex - correctCount));

        // Chip
        chipReview.setVisible(v.isDueToday());
        chipFam.setText("熟悉度 " + v.getFamiliarityStars());
        chipNext.setText("答對 +" + (v.getFamiliarity() + 1) + "天");

        // 題目
        questionLabel.setText(qm.getQuestion(v, currentMode));
        feedbackLabel.setText("");
        nextBtn.setVisible(false);

        // 選項
        List<String> opts = qm.generateOptions(v, currentMode);
        for (int i = 0; i < 4; i++) {
            String text = i < opts.size() ? opts.get(i) : "";
            optBtns[i].setText(text);
            optBtns[i].setEnabled(true);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
        }
    }

    private void onOptionClick(int idx) {
        Vocabulary v  = quizList.get(currentIndex);
        String selected = optBtns[idx].getText();
        String answer   = ctrl.getQuizManager().getAnswer(v, currentMode);
        boolean correct = selected.equals(answer);

        ctrl.submitAnswer(v, correct);

        for (JButton b : optBtns) b.setEnabled(false);

        if (correct) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(new Color(0x1B5E20));
            feedbackLabel.setText("✓ 正確！" + v.getWord() + "：" + v.getMeaning());
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(new Color(0xB71C1C));
            feedbackLabel.setText("✗ 錯誤，正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            // 標示正確選項
            for (JButton b : optBtns) {
                if (b.getText().equals(answer)) {
                    b.setBackground(new Color(0xC8E6C9));
                    b.setForeground(new Color(0x1B5E20));
                }
            }
        }
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex + 1 - correctCount));
    }

    private void nextQuestion() {
        currentIndex++;
        loadQuestion();
    }

    private void showResult() {
        int total = quizList == null ? 0 : quizList.size();
        int wrong  = total - correctCount;
        String msg = String.format(
            "測驗結束！\n答對：%d 題\n答錯：%d 題\n正確率：%.0f%%",
            correctCount, wrong, total > 0 ? (correctCount * 100.0 / total) : 0
        );
        JOptionPane.showMessageDialog(this, msg, "測驗結果", JOptionPane.INFORMATION_MESSAGE);
        // 重置
        startQuiz(false);
    }

    private void pronounce() {
        if (quizList == null || currentIndex >= quizList.size()) return;
        String word = quizList.get(currentIndex).getWord();
        // 使用系統 TTS（macOS/Linux）或 FreeTTS
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac"))
                Runtime.getRuntime().exec(new String[]{"say", word});
            else if (os.contains("nix") || os.contains("nux"))
                Runtime.getRuntime().exec(new String[]{"espeak", word});
            // Windows 可透過 PowerShell TTS
            else
                Runtime.getRuntime().exec(new String[]{"powershell", "-Command",
                    "Add-Type -AssemblyName System.Speech;" +
                    "$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;" +
                    "$s.Speak('" + word + "')"});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "發音功能需要系統支援", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── 樣式工具 ──────────────────────────────────────────────
    private void styleModeBtn(JButton b, boolean active) {
        b.setFont(AppColors.FONT_BTN);
        b.setBorderPainted(true);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(4, 12, 4, 12)
        ));
        b.setBackground(active ? AppColors.BTN_PRIMARY : AppColors.BG_CARD);
        b.setForeground(active ? Color.WHITE : AppColors.TEXT_SECONDARY);
    }

    private JButton styledBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(AppColors.FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER_SOFT, 1, true),
            new EmptyBorder(5, 14, 5, 14)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel makeChip(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_SMALL);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(2, 8, 2, 8));
        return l;
    }
}
