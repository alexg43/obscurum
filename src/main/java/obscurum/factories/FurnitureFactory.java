package obscurum.factories;

import java.awt.Point;
import obscurum.creatures.Spawner;
import obscurum.creatures.TreasureChest;
import obscurum.environment.Level;
import obscurum.environment.background.traps.SpikeTrap;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.Torch;

/**
 * This models a furniture factory, which has several methods to spawn torches,
 * treasure chests, traps, and spawners.
 * @author Alex Ghita
 */
public class FurnitureFactory {
  private Level level;

  public FurnitureFactory(Level level) {
    this.level = level;
  }

  public Torch newTorch() {
    Point location;
    do {
      location = level.getRandomEmptyLocation();
    } while (level.countSurroundingForegroundTiles(location,
        new EmptyTile()) < 8);
    Torch torch = new Torch(3 + (int)(Math.random() * 5), level, location);
    level.setForegroundTile(torch.getLocation(), torch);
    level.addTorch(torch);
    return torch;
  }

  public void newSpawner(String[] creatures, int creaturePowerLevel,
      int maxCreatureCount, CreatureFactory factory) {
    Point location;
    do {
      location = level.getRandomEmptyLocation();
    } while (level.countSurroundingForegroundTiles(location,
        new EmptyTile()) < 8);
    Spawner spawner = new Spawner(level, location, creatures,
        creaturePowerLevel, maxCreatureCount, factory);
    level.setForegroundTile(location, spawner);
    level.addSpawner(spawner);
  }

  // Special treasure chest.
  public TreasureChest newTreasureChest() {
    Point location;
    do {
      location = level.getRandomEmptyLocation();
    } while (level.countSurroundingForegroundTiles(location,
        new EmptyTile()) < 8);
    TreasureChest chest = new TreasureChest(level, location);
    level.setForegroundTile(chest.getLocation(), chest);

    return chest;
  }

  public TreasureChest newTreasureChest(int powerLevel) {
    Point location;
    do {
      location = level.getRandomEmptyLocation();
    } while (level.countSurroundingForegroundTiles(location,
        new EmptyTile()) < 8);
    TreasureChest chest = new TreasureChest(level, location, powerLevel);
    level.setForegroundTile(chest.getLocation(), chest);

    return chest;
  }

  public SpikeTrap newSpikeTrap() {
    Point location = level.getRandomEmptyLocation();
    SpikeTrap trap = new SpikeTrap(level.getBackgroundTile(location));
    level.setBackgroundTile(location, trap);

    return trap;
  }
}
