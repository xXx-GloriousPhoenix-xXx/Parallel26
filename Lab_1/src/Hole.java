import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Hole {
    private final double x;
    private final double y;
    public static final int SIZE = 40;
    private static final int RADIUS = SIZE / 2;

    public Hole(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 100));
        g2.fill(new Ellipse2D.Double(x - 2, y - 2, SIZE + 4, SIZE + 4));

        GradientPaint gradient = new GradientPaint(
                (float) x, (float) y, new Color(20, 20, 20),
                (float) (x + SIZE), (float) (y + SIZE), new Color(5, 5, 5),
                true
        );
        g2.setPaint(gradient);
        g2.fill(new Ellipse2D.Double(x, y, SIZE, SIZE));

        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new Ellipse2D.Double(x, y, SIZE, SIZE));
    }

    public boolean checkCollision(Ball ball) {
        double holeCenterX = x + RADIUS;
        double holeCenterY = y + RADIUS;
        double ballCenterX = ball.getX() + Ball.XSIZE / 2.0;
        double ballCenterY = ball.getY() + Ball.YSIZE / 2.0;

        double distance = Math.sqrt(
                Math.pow(holeCenterX - ballCenterX, 2) +
                        Math.pow(holeCenterY - ballCenterY, 2)
        );

        return distance < RADIUS;
    }
}