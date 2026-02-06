import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {
    private final BallCanvas canvas;
    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;
    private static final int PADDING = 20;

    private final JButton resume;
    private final JButton pause;

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

        buttonPanel.add(addOne);
        buttonPanel.add(addMultiple);
        buttonPanel.add(pause);
        buttonPanel.add(resume);
        buttonPanel.add(clear);

        add(buttonPanel, BorderLayout.SOUTH);

        handlePause();
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

}
