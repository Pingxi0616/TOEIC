package ui;

import controller.DashboardController;
import manager.QuizManager;
import manager.QuizManager.Mode;
import model.Vocabulary;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * 單字片語測驗
 * 流程：選擇器頁 → 測驗頁
 */
public class VocabQuizPanel extends JPanel {

    private final DashboardController ctrl;

    private CardLayout inner;
    private JPanel     innerArea;

    // 選擇器
    private QuizSourceSelector selector;

    // 測驗狀態
    private Mode             currentMode  = Mode.EN_TO_CN;
    private List<Vocabulary> quizList;
    private int  currentIndex = 0;
    private int  correctCount = 0;
    private boolean customMode = false;
    private Runnable onFinishCallback;

    // 測驗 UI
    private JLabel  progressLabel, scoreLabel;
    private JLabel  questionLabel, hintLabel;
    private JLabel  chipReview, chipFam, chipNext;
    private JButton[] optBtns = new JButton[4];
    private JLabel  feedbackLabel;
    private JButton nextBtn, pronounceBtn;
    private JPanel  modeRow;
    private JButton[] modeBtns = new JButton[3];

    public VocabQuizPanel(DashboardController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);

        inner = new CardLayout();
        innerArea = new JPanel(inner);
        innerArea.setOpaque(false);

        selector = new QuizSourceSelector(ctrl, "單字片語測驗", this::startFromSource);
        innerArea.add(selector,         "selector");
        innerArea.add(buildQuizPage(),  "quiz");

        add(innerArea, BorderLayout.CENTER);
        showSelector();
    }

    public void showSelector() {
        selector.refreshSources();
        inner.show(innerArea, "selector");
    }

    private void startFromSource(List<Vocabulary> words) {
        quizList     = words;
        currentIndex = 0;
        correctCount = 0;
        customMode   = false;
        modeRow.setVisible(true);
        refreshModeBtns();
        inner.show(innerArea, "quiz");
        loadQuestion();
    }

    public void startQuiz(boolean prioritizeWrong) {
        quizList     = ctrl.getQuizManager().generateQuizList(20, prioritizeWrong);
        currentIndex = 0;
        correctCount = 0;
        customMode   = false;
        modeRow.setVisible(true);
        refreshModeBtns();
        inner.show(innerArea, "quiz");
        loadQuestion();
    }

    public void startWithList(List<Vocabulary> list, Mode mode, Runnable onFinish) {
        customMode        = true;
        currentMode       = mode;
        onFinishCallback  = onFinish;
        quizList          = list;
        currentIndex      = 0;
        correctCount      = 0;
        modeRow.setVisible(false);
        refreshModeBtns();
        inner.show(innerArea, "quiz");
        loadQuestion();
    }

    // ── 測驗頁面建構 ──────────────────────────────────────────
    private JPanel buildQuizPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(24, 28, 24, 28));
        page.add(buildTopBar(),  BorderLayout.NORTH);
        page.add(buildCenter(),  BorderLayout.CENTER);
        return page;
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("單字片語測驗");
        title.setFont(AppColors.FONT_TITLE);
        title.setForeground(AppColors.TEXT_PRIMARY);

        modeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        modeRow.setOpaque(false);
        String[] modeNames = {"英翻中","中翻英","片語"};
        Mode[]   modeVals  = {Mode.EN_TO_CN, Mode.CN_TO_EN, Mode.PHRASE};
        for (int i = 0; i < 3; i++) {
            final int fi = i;
            modeBtns[i] = new JButton(modeNames[i]);
            modeBtns[i].setFont(AppColors.FONT_BTN);
            modeBtns[i].setFocusPainted(false);
            modeBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            modeBtns[i].addActionListener(e -> { currentMode = modeVals[fi]; refreshModeBtns(); loadQuestion(); });
            modeRow.add(modeBtns[i]);
        }
        JButton backBtn = makeBtn("← 選擇來源", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        backBtn.addActionListener(e -> showSelector());
        modeRow.add(backBtn);

        p.add(title,   BorderLayout.WEST);
        p.add(modeRow, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(AppColors.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(AppColors.BORDER, 2, true),
            new EmptyBorder(22, 28, 22, 28)
        ));
        // 進度
        JPanel progRow = new JPanel(new BorderLayout());
        progRow.setOpaque(false);
        progressLabel = new JLabel("第 1 / 20 題");
        progressLabel.setFont(AppColors.FONT_SMALL);
        progressLabel.setForeground(AppColors.TEXT_SECONDARY);
        scoreLabel = new JLabel("答對 0");
        scoreLabel.setFont(AppColors.FONT_SMALL);
        scoreLabel.setForeground(AppColors.TEXT_SECONDARY);
        progRow.add(progressLabel, BorderLayout.WEST);
        progRow.add(scoreLabel,    BorderLayout.EAST);

        // Chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chipReview = chip("今日待複習", new Color(0xFAEEDA), new Color(0x633806));
        chipFam    = chip("熟悉度 ★☆☆☆☆", AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        chipNext   = chip("答對 +1天",      AppColors.BG_CARD, AppColors.TEXT_SECONDARY);
        chips.add(chipReview); chips.add(chipFam); chips.add(chipNext);

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(AppColors.FONT_WORD);
        questionLabel.setForeground(AppColors.TEXT_PRIMARY);

        hintLabel = new JLabel("選出正確答案", SwingConstants.CENTER);
        hintLabel.setFont(AppColors.FONT_SMALL);
        hintLabel.setForeground(AppColors.TEXT_SECONDARY);

        JPanel qBlock = new JPanel(new GridLayout(3, 1, 0, 6));
        qBlock.setOpaque(false);
        qBlock.add(chips); qBlock.add(questionLabel); qBlock.add(hintLabel);

        // 選項
        JPanel optGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            optBtns[i] = new JButton();
            optBtns[i].setFont(AppColors.FONT_BODY);
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
            optBtns[i].setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER_SOFT, 1, true), new EmptyBorder(12, 10, 12, 10)));
            optBtns[i].setFocusPainted(false);
            optBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int fi = i;
            optBtns[i].addActionListener(e -> onOptionClick(fi));
            optGrid.add(optBtns[i]);
        }

        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(AppColors.FONT_BODY);

        // 底部
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel srInfo = new JLabel("<html>答對 → 間隔延長 &nbsp; 答錯 → 明天再複習</html>");
        srInfo.setFont(AppColors.FONT_SMALL);
        srInfo.setForeground(AppColors.TEXT_SECONDARY);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        pronounceBtn = makeBtn("🔊 發音", AppColors.BG_CARD, AppColors.TEXT_PRIMARY);
        pronounceBtn.addActionListener(e -> pronounce());
        nextBtn = makeBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> nextQuestion());
        right.add(pronounceBtn); right.add(nextBtn);
        bottom.add(srInfo, BorderLayout.WEST);
        bottom.add(right,  BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(qBlock,       BorderLayout.NORTH);
        center.add(optGrid,      BorderLayout.CENTER);
        center.add(feedbackLabel,BorderLayout.SOUTH);

        card.add(progRow,  BorderLayout.NORTH);
        card.add(center,   BorderLayout.CENTER);
        card.add(bottom,   BorderLayout.SOUTH);
        return card;
    }

    // ── 題目邏輯 ─────────────────────────────────────────────
    private void loadQuestion() {
        if (quizList == null || quizList.isEmpty()) { questionLabel.setText("此來源無單字"); return; }
        if (currentIndex >= quizList.size()) { showResult(); return; }

        Vocabulary v = quizList.get(currentIndex);
        QuizManager qm = ctrl.getQuizManager();

        progressLabel.setText("第 " + (currentIndex+1) + " / " + quizList.size() + " 題");
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex - correctCount));
        chipReview.setVisible(v.isDueToday());
        chipFam.setText("熟悉度 " + v.getFamiliarityStars());
        chipNext.setText("答對 +" + Math.min(v.getFamiliarity()+1, 5) + "天");

        String q = qm.getQuestion(v, currentMode);
        questionLabel.setText(q.length() > 40
            ? "<html><div style='text-align:center'>" + q + "</div></html>" : q);
        questionLabel.setFont(q.length() > 20 ? AppColors.FONT_HEAD : AppColors.FONT_WORD);
        hintLabel.setText(modeHint(currentMode));
        feedbackLabel.setText("");
        nextBtn.setVisible(false);

        List<String> opts = qm.generateOptions(v, currentMode);
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
        String answer  = ctrl.getQuizManager().getAnswer(v, currentMode);
        boolean correct = sel.equals(answer);
        ctrl.submitAnswer(v, correct);
        for (JButton b : optBtns) b.setEnabled(false);
        if (correct) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(new Color(0x1B5E20));
            feedbackLabel.setText("✓ 正確！  " + v.getWord() + "：" + v.getMeaning());
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(new Color(0xB71C1C));
            feedbackLabel.setText("✗ 錯誤，正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : optBtns) if (b.getText().equals(answer)) { b.setBackground(new Color(0xC8E6C9)); b.setForeground(new Color(0x1B5E20)); }
        }
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex+1 - correctCount));
    }

    private void nextQuestion() { currentIndex++; loadQuestion(); }

    private void showResult() {
        int total = quizList == null ? 0 : quizList.size();
        JOptionPane.showMessageDialog(this,
            String.format("測驗結束！\n答對：%d  答錯：%d\n正確率：%.0f%%",
                correctCount, total-correctCount,
                total>0 ? (correctCount*100.0/total) : 0),
            "測驗結果", JOptionPane.INFORMATION_MESSAGE);
        if (customMode && onFinishCallback != null) onFinishCallback.run();
        else showSelector();
    }

    private void pronounce() {
        if (quizList == null || currentIndex >= quizList.size()) return;
        String word = quizList.get(currentIndex).getWord();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) Runtime.getRuntime().exec(new String[]{"say", word});
            else if (os.contains("nix")||os.contains("nux")) Runtime.getRuntime().exec(new String[]{"espeak", word});
            else Runtime.getRuntime().exec(new String[]{"powershell","-Command",
                "Add-Type -AssemblyName System.Speech;$s=New-Object System.Speech.Synthesis.SpeechSynthesizer;$s.Speak('"+word.replace("'","")+"')"});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"發音需要系統支援","提示",JOptionPane.WARNING_MESSAGE); }
    }

    private void refreshModeBtns() {
        Mode[] vals = {Mode.EN_TO_CN, Mode.CN_TO_EN, Mode.PHRASE};
        for (int i = 0; i < modeBtns.length; i++) {
            boolean on = vals[i] == currentMode;
            modeBtns[i].setBackground(on ? AppColors.BTN_PRIMARY : AppColors.BG_CARD);
            modeBtns[i].setForeground(on ? Color.WHITE : AppColors.TEXT_SECONDARY);
            modeBtns[i].setBorder(new CompoundBorder(
                new LineBorder(on ? AppColors.BORDER : AppColors.BORDER_SOFT, 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        }
    }

    private String modeHint(Mode mode) {
        return switch (mode) {
            case EN_TO_CN -> "選出正確的中文意思";
            case CN_TO_EN -> "選出正確的英文單字";
            case PHRASE   -> "選出正確的片語";
            default       -> "選出填入空格的單字";
        };
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

    private JLabel chip(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(AppColors.FONT_SMALL);
        l.setBackground(bg); l.setForeground(fg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(2, 8, 2, 8));
        return l;
    }
}
