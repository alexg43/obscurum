package obscurum.environment;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import lombok.NonNull;
import obscurum.creatures.Creature;
import obscurum.creatures.Spawner;
import obscurum.creatures.ai.CorpseAI;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.background.traps.Trap;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;
import obscurum.environment.foreground.Torch;
import obscurum.placeholders.BackgroundLevelBound;
import obscurum.placeholders.ForegroundLevelBound;
import obscurum.placeholders.NullCreature;
import obscurum.placeholders.NullLevel;


public class Level {
    private final ForegroundTile[][] foreground;
    private final BackgroundTile[][] background;
    private final int width;
    private final int height;
    private Level next;
    private Level previous;
    private Point nextLocation;
    private Point previousLocation;
    private final ArrayList<Creature> creatures;
    private final ArrayList<Torch> torches;
    private final ArrayList<Spawner> spawners;

    public Level(@NonNull ForegroundTile[][] foreground, @NonNull BackgroundTile[][] background) {
        if (foreground.length != background.length) {
            throw new IllegalArgumentException("Foreground and background must have the same number of rows.");
        }

        for (int i = 0; i < foreground.length; i++) {
            if (background[i].length != background[0].length) {
                throw new IllegalArgumentException(String.format("Background row %d length %d should be %d.", i, background[i].length, background[0].length));
            } else if (foreground[i].length != foreground[0].length) {
                throw new IllegalArgumentException(String.format("Foreground row %d length %d should be %d.", i, foreground[i].length, foreground[0].length));
            } else if (foreground[i].length != background[i].length) {
                throw new IllegalArgumentException(String.format("Row %d length is not the same in the two boards.", i));
            }
        }

        this.foreground = foreground;
        this.background = background;
        this.width = background.length;
        this.height = background[0].length;

        if (width > 1) {
            next = new NullLevel();
            previous = new NullLevel();
            nextLocation = null;
            previousLocation = null;
        }
        creatures = new ArrayList<>();
        torches = new ArrayList<>();
        spawners = new ArrayList<>();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ForegroundTile getForegroundTile(int x, int y) {
        return getForegroundTile(new Point(x, y));
    }

    public ForegroundTile getForegroundTile(Point p) {
        if (!isInBounds(p)) {
            return new ForegroundLevelBound();
        }
        return foreground[p.x][p.y];
    }

    public BackgroundTile getBackgroundTile(Point p) {
        if (!isInBounds(p)) {
            return new BackgroundLevelBound();
        }
        return background[p.x][p.y];
    }

    public Level getPrevious() {
        return previous;
    }

    public Level getNext() {
        return next;
    }

    public Point getNextLocation() {
        return nextLocation;
    }

    public Point getPreviousLocation() {
        return previousLocation;
    }

    public char getDisplayGlyph(int x, int y) {
        return getDisplayGlyph(new Point(x, y));
    }

    public char getDisplayGlyph(Point p) {
        if (!isInBounds(p)) {
            return 0;
        }
        if (isForegroundOfType(p, "Empty Tile")) {
            return background[p.x][p.y].getGlyph();
        }
        return foreground[p.x][p.y].getGlyph();
    }

    public Color getDisplayForegroundColour(int x, int y) {
        return getDisplayForegroundColour(new Point(x, y));
    }

    public Color getDisplayForegroundColour(Point p) {
        checkForIllegalLocation(p);
        if (isForegroundOfType(p, "Empty Tile")) {
            return background[p.x][p.y].getGlyphColour();
        }
        return foreground[p.x][p.y].getGlyphColour();
    }

    public Color getDisplayBackgroundColour(int x, int y) {
        return getDisplayBackgroundColour(new Point(x, y));
    }

    public Color getDisplayBackgroundColour(Point p) {
        checkForIllegalLocation(p);
        return background[p.x][p.y].getBackgroundColour();
    }

    public void updateCreatures() {
        ArrayList<Creature> toRemove = new ArrayList<>();
        for (Creature c : creatures) {
            c.update();
            if (!c.isAlive() && ((CorpseAI)c.getAI()).getTurnsLeft() == 0) {
                toRemove.add(c);
            }
        }
        for (Creature c : toRemove) {
            remove(c);
        }
    }

    public void updateSpawners() {
        for (Spawner s : spawners) {
            s.update();
        }
    }

    public void powerUpCreatures() {
        for (Creature c : creatures) {
            c.powerUp();
        }
        for (Spawner s : spawners) {
            s.powerUp();
        }
    }

    public void add(Creature c) {
        if (c == null || c instanceof NullCreature) {
            throw new IllegalArgumentException("Creature cannot be null.");
        }
        creatures.add(c);
    }

    public void remove(Creature c) {
        for (int i = 0; i < creatures.size(); i++) {
            if (creatures.get(i).getLocation().equals(c.getLocation())) {
                setForegroundTile(creatures.get(i).getLocation(), new EmptyTile());
                creatures.remove(i);
                return;
            }
        }
        // The creature should have been found, so throw an error.
        throw new IllegalArgumentException("Creature to be removed not found.");
    }

    public void setForegroundTile(int x, int y, ForegroundTile f) {
        setForegroundTile(new Point(x, y), f);
    }

    public void setForegroundTile(Point p, ForegroundTile f) {
        checkForIllegalLocation(p);
        if (f == null || f instanceof ForegroundLevelBound) {
            throw new IllegalArgumentException("Foreground tile cannot be null.");
        }

        foreground[p.x][p.y] = f;
    }

    public void setBackgroundTile(int x, int y, BackgroundTile b) {
        setBackgroundTile(new Point(x, y), b);
    }

    public void setBackgroundTile(Point p, BackgroundTile b) {
        checkForIllegalLocation(p);
        if (b == null || b instanceof BackgroundLevelBound) {
            throw new IllegalArgumentException("Background tile cannot be null.");
        }

        background[p.x][p.y] = b;
    }

    public void setTile(Point p, ForegroundTile f, BackgroundTile b) {
        setForegroundTile(p, f);
        setBackgroundTile(p, b);
    }

    public void setNext(Level next) {
        if (next == null || next instanceof NullLevel) {
            throw new IllegalArgumentException("Next level cannot be null.");
        }

        this.next = next;
    }

    public void setPrevious(Level previous) {
        if (previous == null || previous instanceof NullLevel) {
            throw new IllegalArgumentException("Previous level cannot be null.");
        }

        this.previous = previous;
    }

    public void setNextLocation(Point nextLocation) {
        checkForIllegalLocation(nextLocation);

        this.nextLocation = nextLocation;
    }

    public void setPreviousLocation(Point previousLocation) {
        checkForIllegalLocation(previousLocation);

        this.previousLocation = previousLocation;
    }

    public Point getRandomEmptyLocation() {
        ArrayList<Point> emptyLocations = new ArrayList<>();

        // Get all empty locations.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isForegroundOfType(x, y, new EmptyTile()) &&
                        !isBackgroundOfType(x, y, "Downward Doorway") &&
                        !isBackgroundOfType(x, y, "Upward Doorway")) {
                    emptyLocations.add(new Point(x, y));
                }
            }
        }

        // If there are no empty locations, return null.
        if (emptyLocations.isEmpty()) {
            return null;
        }
        // Pick a random empty location and return it.
        return emptyLocations.get((int)(Math.random() * emptyLocations.size()));
    }

    public boolean isInBounds(int x, int y) {
        return isInBounds(new Point(x, y));
    }

    public boolean isInBounds(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    public boolean isForegroundOfType(int x, int y, ForegroundTile f) {
        return isForegroundOfType(new Point(x, y), f);
    }

    public boolean isForegroundOfType(int x, int y, String tileName) {
        return isForegroundOfType(new Point(x, y), tileName);
    }

    public boolean isForegroundOfType(Point p, ForegroundTile f) {
        checkForIllegalLocation(p);

        return foreground[p.x][p.y].getClass().equals(f.getClass());
    }

    public boolean isForegroundOfType(Point p, String tileName) {
        checkForIllegalLocation(p);

        return foreground[p.x][p.y].getName().equals(tileName);
    }

    public boolean isBackgroundOfType(int x, int y, BackgroundTile b) {
        return isBackgroundOfType(new Point(x, y), b);
    }

    public boolean isBackgroundOfType(int x, int y, String tileName) {
        return isBackgroundOfType(new Point(x, y), tileName);
    }

    public boolean isBackgroundOfType(Point p, BackgroundTile b) {
        checkForIllegalLocation(p);

        return background[p.x][p.y].getClass().equals(b.getClass());
    }

    public boolean isBackgroundOfType(Point p, String tileName) {
        checkForIllegalLocation(p);

        return background[p.x][p.y].getName().equals(tileName);
    }

    public boolean isCreature(Point p) {
        checkForIllegalLocation(p);

        return foreground[p.x][p.y] instanceof Creature;
    }

    public boolean isIlluminated(Point p) {
        for (Torch t : torches) {
            if (t.doesIlluminate(p)) {
                return true;
            }
        }
        return false;
    }

    public int countAdjacentForegroundTiles(int x, int y, ForegroundTile f) {
        return countAdjacentForegroundTiles(new Point(x, y), f);
    }

    public int countAdjacentForegroundTiles(Point p, ForegroundTile f) {
        return countNearbyTiles(p, f, new BackgroundLevelBound(), false);
    }

    public int countAdjacentBackgroundTiles(Point p, BackgroundTile b) {
        return countNearbyTiles(p, new ForegroundLevelBound(), b, false);
    }

    public int countSurroundingForegroundTiles(Point p, ForegroundTile f) {
        return countNearbyTiles(p, f, new BackgroundLevelBound(), true);
    }

    public int countSurroundingTiles(Point p, ForegroundTile f,
                                     BackgroundTile b) {
        return countNearbyTiles(p, f, b, true);
    }

    public void triggerTrap(Point p, Creature c) {
        ((Trap)(getBackgroundTile(p))).trigger(c);
        setBackgroundTile(p, ((Trap)(getBackgroundTile(p))).getReplacementTile());
    }

    private int countNearbyTiles(Point p, ForegroundTile f, BackgroundTile b, boolean corners) {
        checkForIllegalLocation(p);

        int count = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0 || !corners && i != 0 && j != 0 || !isInBounds(p.x + i, p.y + j)) {
                    continue;
                }
                if ((f instanceof ForegroundLevelBound || isForegroundOfType(p.x + i, p.y + j, f.name)) &&
                        (b instanceof BackgroundLevelBound || isBackgroundOfType(p.x + i, p.y + j,  b.name))) {
                    count++;
                }
            }
        }

        return count;
    }

    private void checkForIllegalLocation(Point p) {
        if (!isInBounds(p)) {
            throw new IllegalArgumentException(
                    "Location (" + p.x + ", " + p.y + ") must be between (0, 0) and (" +
                            width + ", " + height + ").");
        }
    }

    public void addTorch(Torch t) {
        torches.add(t);
    }

    public void addSpawner(Spawner s) {
        spawners.add(s);
    }
}
