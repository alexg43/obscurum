package obscurum.util;

import java.awt.Point;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class Line {
    private final Point start;
    private final Point end;
    private final List<Point> pointsOnLine;
    private final int dx;
    private final int dy;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.pointsOnLine = new ArrayList<>();
        this.dx = Math.abs(start.x - end.x);
        this.dy = Math.abs(start.y - end.y);
    }

    public void plotLine() {
        int stepX = start.x < end.x ? 1 : -1;
        int stepY = start.y < end.y ? 1 : -1;
        int x = start.x;
        int y = start.y;
        int error = dx - dy;
        int twiceError;

        while (true) {
            pointsOnLine.add(new Point(x, y));
            if (x == end.x && y == end.y) {
                break;
            }
            twiceError = 2 * error;
            if (twiceError > -dx) {
                error -= dy;
                x += stepX;
            }
            if (twiceError < dx) {
                error += dx;
                y += stepY;
            }
        }
    }

    public List<Point> getPointsOnLine() {
        return pointsOnLine;
    }
}
