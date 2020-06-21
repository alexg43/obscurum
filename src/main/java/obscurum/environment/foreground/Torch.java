package obscurum.environment.foreground;

import java.awt.Point;
import java.util.List;

import obscurum.util.Line;
import obscurum.display.Display;
import obscurum.environment.Level;

public class Torch extends ForegroundTile {
    private final int lineOfSight;
    private final Level level;
    private final Point location;

    public Torch(int lineOfSight, Level level, Point location) {
        super("Torch", (char)15, Display.ORANGE, 1, 0, true, true);

        if (lineOfSight < 1) {
            throw new IllegalArgumentException(String.format("Line of sight %d must be at least 1.", lineOfSight));
        }

        this.lineOfSight = lineOfSight;
        this.level = level;
        this.location = location;
    }

    public boolean doesIlluminate(Point target) {
        int distance = (location.x - target.x) * (location.x - target.x) + (location.y - target.y) * (location.y - target.y);
        Line sightRay;
        List<Point> linePoints;

        if (distance > lineOfSight * lineOfSight) {
            return false;
        }

        sightRay = new Line(location, target);
        sightRay.plotLine();
        linePoints = sightRay.getPointsOnLine();
        for (Point p : linePoints) {
            if (level.getForegroundTile(p).isOpaque() && !p.equals(target) && !p.equals(location)) {
                return false;
            }
        }
        return true;
    }

    public Point getLocation() {
        return location;
    }
}
