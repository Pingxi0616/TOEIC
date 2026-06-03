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
    private boolean answered  = false;

    // UI
    private JLabel  progressLabel, scoreLabel;
    private JTextArea sentenceArea;
    private JButton[] optBtns = new JButton[4];
    private JLabel  feedbackLabel;
    private JTextPane analysisPane;
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
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { backBtn.setBackground(new Color(0xFFEBEB)); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { backBtn.setBackground(AppColors.BG_CARD); }
        });
        backBtn.addActionListener(e -> {
            if (UIUtils.showConfirm(this, "確定要退出測驗嗎？進度將不會保留。", "退出確認"))
                showSelector();
        });
        right.add(backBtn);
        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        west.setOpaque(false);
        west.add(title);
        topBar.add(west,  BorderLayout.WEST);
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
        sentenceArea.setFont(new Font("Serif", Font.BOLD, 20));
        sentenceArea.setEditable(false);
        sentenceArea.setLineWrap(true);
        sentenceArea.setWrapStyleWord(true);
        sentenceArea.setOpaque(false);
        sentenceArea.setBorder(new EmptyBorder(8, 0, 8, 0));

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
        feedbackLabel.setPreferredSize(new Dimension(0, 26));
        feedbackLabel.setMinimumSize(new Dimension(0, 26));

        analysisPane = new JTextPane();
        analysisPane.setEditable(false);
        analysisPane.setBackground(new Color(0xF5F6F8));
        analysisPane.setBorder(new CompoundBorder(
            new MatteBorder(0,3,0,0, new Color(0x4A5568)),
            new EmptyBorder(8,12,8,12)));
        analysisPane.setVisible(false);

        nextBtn = makeBtn("下一題 →", AppColors.BTN_PRIMARY, Color.WHITE);
        nextBtn.setVisible(false);
        nextBtn.addActionListener(e -> nextQuestion());

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        // 預先固定 south 高度，讓上方選項格不隨 analysisArea 顯示而縮小
        south.setPreferredSize(new Dimension(0, 130));
        south.setMinimumSize(new Dimension(0, 130));
        south.add(feedbackLabel, BorderLayout.NORTH);
        south.add(analysisPane,  BorderLayout.CENTER);
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
        analysisPane.setVisible(false);
        nextBtn.setVisible(false);
        answered = false;

        List<String> opts = qm.generateOptions(v, Mode.FILL_BLANK);
        for (int i = 0; i < 4; i++) {
            optBtns[i].setText(i < opts.size() ? opts.get(i) : "—");
            optBtns[i].setBackground(new Color(0xF0EAD8));
            optBtns[i].setForeground(AppColors.TEXT_PRIMARY);
        }
    }

    private void onOptionClick(int idx) {
        if (answered) return;
        answered = true;
        Vocabulary v   = quizList.get(currentIndex);
        String sel     = optBtns[idx].getText();
        String answer  = ctrl.getQuizManager().getAnswer(v, Mode.FILL_BLANK);
        boolean correct = sel.equals(answer);
        ctrl.submitAnswer(v, correct);
        if (correct) {
            correctCount++;
            optBtns[idx].setBackground(new Color(0xC8E6C9));
            optBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            feedbackLabel.setText("正確！");
            feedbackLabel.setForeground(AppColors.TEXT_GREEN);
        } else {
            optBtns[idx].setBackground(new Color(0xFFCDD2));
            optBtns[idx].setForeground(AppColors.TEXT_PRIMARY);
            feedbackLabel.setText("正確答案：" + answer);
            feedbackLabel.setForeground(AppColors.TEXT_RED);
            for (JButton b : optBtns) if (b.getText().equals(answer)) { b.setBackground(new Color(0xC8E6C9)); b.setForeground(AppColors.TEXT_PRIMARY); }
        }
        setAnalysisText(analysisPane, v, answer);
        analysisPane.setVisible(true);
        nextBtn.setVisible(true);
        scoreLabel.setText("答對 " + correctCount + " · 答錯 " + (currentIndex+1 - correctCount));
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
                // 在例句中找到答案單字並套用粗體藍色
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
        UIUtils.addHover(b, bg);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
