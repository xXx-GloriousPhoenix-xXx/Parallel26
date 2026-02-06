import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BallCanvas extends JPanel {
    private final ArrayList<Ball> balls = new ArrayList<>();
    private final ArrayList<Thread> threads = new ArrayList<>();
//    private final ArrayList<Hole> holes = new ArrayList<>();

    public void add(Ball b) {
        this.balls.add(b);
        Thread t = new Thread(b);
        t.start();
        threads.add(t);
        repaint();
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
        for (var t : threads) {
            t.interrupt();
        }
        threads.clear();
        balls.clear();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        for (Ball b : balls) {
            b.draw(g2);
        }
    }


}