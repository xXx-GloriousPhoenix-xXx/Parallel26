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

    public BallCanvas() {
        initializeHoles();
    }

    private void initializeHoles() {
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (holes.isEmpty() && getWidth() > 0 && getHeight() > 0) {
            createHoles();
        }
    }

    private void createHoles() {
        holes.clear();
        int margin = 5;

        holes.add(new Hole(margin, margin));
        holes.add(new Hole(getWidth() - Hole.SIZE - margin, margin));
        holes.add(new Hole(margin, getHeight() - Hole.SIZE - margin));
        holes.add(new Hole(getWidth() - Hole.SIZE - margin, getHeight() - Hole.SIZE - margin));

        holes.add(new Hole(getWidth() / 2.0 - Hole.SIZE / 2.0, margin));
        holes.add(new Hole(getWidth() / 2.0 - Hole.SIZE / 2.0, getHeight() - Hole.SIZE - margin));
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

        for (Hole hole : holes) {
            hole.draw(g2);
        }

        for (Ball ball : balls) {
            ball.draw(g2);
        }
    }
}