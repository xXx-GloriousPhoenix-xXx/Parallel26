import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

class Ball implements Runnable {
    private final BallCanvas canvas;
    public static final int XSIZE = 20;
    public static final int YSIZE = 20;
    private double x;
    private double y;
    private double dx;
    private double dy;
    private final Color color;
    public final BALL_PRIORITY priority;
    public volatile boolean isScored = false;
    private volatile boolean isRunning;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Ball (BallCanvas c, BALL_PRIORITY priority, boolean isRunning) {
        this.canvas = c;
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        this.isRunning = isRunning;

        Random random = new Random();

        this.x = (width / 2) * random.nextDouble() + (width / 4);
        this.y = (height / 2) * random.nextDouble() + (height / 4);

        double angle = 2 * Math.PI * random.nextDouble();
        this.dx = 2 * Math.cos(angle);
        this.dy = 2 * Math.sin(angle);

        this.priority = priority;

        this.color = switch (priority) {
            case BALL_PRIORITY.ANY -> new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            );
            case BALL_PRIORITY.LOW -> new Color(49, 49, 255);
            case BALL_PRIORITY.HIGH -> new Color(255, 49, 49);
        };
    }

    public void draw(Graphics2D g2) {
        if (isScored) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color lightColor = color.brighter();
        Color darkColor = color.darker();

        GradientPaint gradient = new GradientPaint(
                (float) x, (float) y, lightColor,
                (float) (x + XSIZE), (float) (y + YSIZE), darkColor,
                true
        );

        g2.setPaint(gradient);
        g2.fill(new Ellipse2D.Double(x, y, XSIZE, YSIZE));

        g2.setColor(darkColor);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new Ellipse2D.Double(x, y, XSIZE, YSIZE));

        g2.setColor(new Color(255, 255, 255, 150));
        g2.fillOval((int) x + 5, (int) y + 5, 6, 6);
    }

    public void move() {
        if (!isRunning || isScored) return;

        x += dx;
        y += dy;

        if (x < 0) {
            x = 0;
            dx = -dx;
        }

        if(x + XSIZE >= this.canvas.getWidth()) {
            x = this.canvas.getWidth() - XSIZE;
            dx = -dx;
        }

        if (y < 0) {
            y = 0;
            dy = -dy;
        }

        if(y + YSIZE >= this.canvas.getHeight()) {
            y = this.canvas.getHeight() - YSIZE;
            dy = -dy;
        }

        this.canvas.repaint();
    }

    public void score() {
        isScored = true;
    }

    @Override
    public void run() {
        try {
            while (!isScored && !Thread.currentThread().isInterrupted()) {
                move();
                canvas.checkHoleCollisions(this);
                Thread.sleep(5);
            }
        }
        catch (InterruptedException _) {
        }
    }

    public void resume() {
        isRunning = true;
    }

    public void pause() {
        isRunning = false;
    }
}