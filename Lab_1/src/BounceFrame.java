import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {
    private final BallCanvas canvas;
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    private static final int PADDING = 20;

    private final JButton resume;
    private final JButton pause;
    private final JLabel scoreLabel;

    private static boolean isRunning;

    public BounceFrame() {
        Color green = new Color(30, 109, 82);
        Color brown = new Color(119, 56, 38);

        setSize(WIDTH, HEIGHT);
        setTitle("Більярд");
        setLayout(new BorderLayout());

        // ===== Canvas =====
        canvas = new BallCanvas();
        canvas.setBackground(green);

        var canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.setBackground(brown);
        canvasPanel.setBorder(BorderFactory.createEmptyBorder( PADDING, PADDING, 0, PADDING ));
        canvasPanel.add(canvas, BorderLayout.CENTER);
        add(canvasPanel, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(brown);

        Styler styler = new Styler();

        JButton addOne = styler.StyleButton(new JButton("+1"));
        addOne.addActionListener(_ -> handleAddSingle());

        JButton addMultiple = styler.StyleButton(new JButton("+100"));
        addMultiple.addActionListener(_ -> handleAddMultiple());

        resume = styler.StyleButton(new JButton("Почати"));
        resume.addActionListener(_ -> handleResume());

        pause = styler.StyleButton(new JButton("Зупинити"));
        pause.addActionListener(_ -> handlePause());

        JButton clear = styler.StyleButton(new JButton("Очистити"));
        clear.addActionListener(_ -> handleClear());

        JButton priority = styler.StyleButton(new JButton("Пріоритет-тест"));
        priority.addActionListener(_ -> handlePriorityTest());

        scoreLabel = styler.StyleLabel(new JLabel("0"));

        buttonPanel.add(addOne);
        buttonPanel.add(addMultiple);
        buttonPanel.add(pause);
        buttonPanel.add(resume);
        buttonPanel.add(clear);
        buttonPanel.add(priority);
        buttonPanel.add(scoreLabel);

        add(buttonPanel, BorderLayout.SOUTH);

        handlePause();

        startScoreUpdater();
    }

    private void startScoreUpdater() {
        Timer timer = new Timer(100, _ ->
                scoreLabel.setText(canvas.getScoredBalls() + "")
        );
        timer.start();
    }

    public void handleAddSingle() {
        Ball b = new Ball(canvas, BALL_PRIORITY.ANY, isRunning);
        canvas.add(b);
    }

    public void handleAddMultiple() {
        for (int i = 0; i < 100; i++) {
            handleAddSingle();
        }
    }

    public void handleResume() {
        isRunning = true;
        resume.setEnabled(false);
        pause.setEnabled(true);
        canvas.resume();
    }

    public void handlePause() {
        isRunning = false;
        resume.setEnabled(true);
        pause.setEnabled(false);
        canvas.pause();
    }

    public void handleClear() {
        canvas.clear();
        isRunning = false;
        resume.setEnabled(true);
        pause.setEnabled(false);
    }

    public void handlePriorityTest() {
        handleClear();

        var redBall = new Ball(canvas, BALL_PRIORITY.HIGH, isRunning);
        canvas.add(redBall);

        for (int i = 0; i < 1000; i++) {
            var blueBall = new Ball(canvas, BALL_PRIORITY.LOW, isRunning);
            canvas.add(blueBall);
        }

        handleResume();
    }
}