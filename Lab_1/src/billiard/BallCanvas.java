package billiard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BallCanvas extends JPanel {
    private final List<Ball> balls = new CopyOnWriteArrayList<>();
    private final ArrayList<Thread> threads = new ArrayList<>();
    private final ArrayList<Hole> holes = new ArrayList<>();
    private int scoredBalls = 0;
    private int lastWidth = 0;
    private int lastHeight = 0;

    public BallCanvas() {
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (getWidth() > 0 && getHeight() > 0) {
            if (lastWidth != getWidth() || lastHeight != getHeight()) {
                createHoles();
                lastWidth = getWidth();
                lastHeight = getHeight();
            }
        }
    }

    private void createHoles() {
        synchronized (holes) {
            holes.clear();
            int margin = 5;

            holes.add(new Hole(margin, margin));
            holes.add(new Hole(getWidth() - Hole.SIZE - margin, margin));
            holes.add(new Hole(margin, getHeight() - Hole.SIZE - margin));
            holes.add(new Hole(getWidth() - Hole.SIZE - margin, getHeight() - Hole.SIZE - margin));

            holes.add(new Hole(getWidth() / 2.0 - Hole.SIZE / 2.0, margin));
            holes.add(new Hole(getWidth() / 2.0 - Hole.SIZE / 2.0, getHeight() - Hole.SIZE - margin));
        }
        repaint();
    }

    public void add(Ball b) {
        this.balls.add(b);
        Thread t = new Thread(b);
        t.setPriority(
                switch (b.priority) {
                    case ANY -> Thread.NORM_PRIORITY;
                    case LOW -> Thread.MIN_PRIORITY;
                    case HIGH -> Thread.MAX_PRIORITY;
                }
        );
        t.start();
        synchronized (threads) {
            threads.add(t);
        }
        repaint();
    }

    public void checkHoleCollisions(Ball ball) {
        if (ball.isScored) return;

        synchronized (holes) {
            for (Hole hole : holes) {
                if (hole.checkCollision(ball)) {
                    ball.score();
                    synchronized (this) {
                        scoredBalls++;
                    }
                    repaint();
                    break;
                }
            }
        }
    }

    public void resume() {
        if (balls.isEmpty()) return;
        for (var b : balls) {
            b.resume();
        }
    }

    public void pause() {
        if (balls.isEmpty()) return;
        for (var b : balls) {
            b.pause();
        }
    }

    public void clear() {
        synchronized (threads) {
            for (var t : threads) {
                t.interrupt();
            }
            threads.clear();
        }
        balls.clear();
        synchronized (this) {
            scoredBalls = 0;
        }
        repaint();
    }

    public synchronized int getScoredBalls() {
        return scoredBalls;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        synchronized (holes) {
            for (Hole hole : holes) {
                hole.draw(g2);
            }
        }

        for (Ball ball : balls) {
            ball.draw(g2);
        }
    }

    public void joinTest() {
        new Thread(() -> {
            var redBall = new Ball(this, BALL_PRIORITY.HIGH, true);
            var blueBall = new Ball(this, BALL_PRIORITY.LOW, true);

            var redThread = new Thread(redBall);
            var blueThread = new Thread(blueBall);

            balls.add(redBall);
            threads.add(redThread);
            redThread.start();

            try {
                redThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            balls.add(blueBall);
            threads.add(blueThread);
            blueThread.start();

            SwingUtilities.invokeLater(this::repaint);
        }).start();
    }
}