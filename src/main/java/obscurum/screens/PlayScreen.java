package obscurum.screens;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.GameMain;
import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.creatures.ai.CorpseAI;
import obscurum.creatures.ai.CreatureAI;
import obscurum.creatures.ai.PlayerAI;
import obscurum.creatures.util.Line;
import obscurum.display.Display;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;
import obscurum.environment.background.*;
import obscurum.environment.builders.*;
import obscurum.environment.foreground.*;
import obscurum.factories.*;
import obscurum.items.Equipment;
import obscurum.items.weapons.Weapon;
import obscurum.placeholders.NullCreature;
import obscurum.placeholders.NullEquipment;
import obscurum.placeholders.NullScreen;
import obscurum.util.Util;

/**
 * This models the main game screen, which generates new levels, populates and
 * updates them, and is responsible for displaying the game level in which the
 * player currently is.
 * @author Alex Ghita
 */
public class PlayScreen extends Screen {
  public static final int GAME_TL_X = 0;
  public static final int GAME_TL_Y = 0;
  public static final int GAME_WIDTH = 82;
  public static final int GAME_HEIGHT = 23;
  public static final int STATS_TL_X = GAME_WIDTH - 1;
  public static final int STATS_TL_Y = 0;
  public static final int STATS_WIDTH =
      GameMain.SCREEN_WIDTH - GAME_WIDTH + 1;
  public static final int STATS_HEIGHT = GAME_HEIGHT;
  public static final int LOG_TL_X = 0;
  public static final int LOG_TL_Y = GAME_HEIGHT - 1;
  public static final int LOG_WIDTH = GameMain.SCREEN_WIDTH;
  public static final int LOG_HEIGHT = GameMain.SCREEN_HEIGHT -
      GAME_HEIGHT + 1;
  public Screen subScreen;
  private CreatureFactory cf;
  private FurnitureFactory ff;

  public PlayScreen(Level[] world, Player player) {
    super(world, player);
    subScreen = new NullScreen();
  }

  public PlayScreen() {
    createWorld(3);
    subScreen = new NullScreen();
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    if (player.isInSubScreen()) {
      subScreen = player.getSubScreen();
    }

    drawBorders(terminal, new Point(GAME_TL_X, GAME_TL_Y), GAME_WIDTH,
        GAME_HEIGHT, false, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    displayTiles(terminal, getScrollLocation());
    drawBorders(terminal, new Point(STATS_TL_X, STATS_TL_Y), STATS_WIDTH,
        STATS_HEIGHT, false, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    drawBorders(terminal, new Point(LOG_TL_X, LOG_TL_Y), LOG_WIDTH, LOG_HEIGHT,
        false, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DB_INTERSECT, STATS_TL_X, STATS_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DV_DR_INTERSECT, LOG_TL_X, LOG_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DV_DL_INTERSECT, GameMain.SCREEN_WIDTH - 1,
        LOG_TL_Y, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, GAME_WIDTH - 1,
        GAME_HEIGHT - 1, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write("Press [F1] for help.", GAME_WIDTH + 4, GAME_HEIGHT - 2);

    // Display combat log.
    for (int i = player.getCombatLog().size() - 1; i >= 0; i--) {
      terminal.write(player.getCombatLog().get(i), 1, GAME_HEIGHT + i);
    }

    // Display player information.
    terminal.write("Player Stats", STATS_TL_X + (STATS_WIDTH / 2) - 6,
        STATS_TL_Y + 2);
    writeHorizontalLine(terminal, STATS_TL_X, STATS_WIDTH - 1, STATS_TL_Y + 3);
    if (player.getHealth() < player.getMaxHealth() * 0.5) {
      int offset = 8;
      terminal.write("Health:", STATS_TL_X + 1, STATS_TL_Y + 4);
      terminal.write(Integer.toString(player.getHealth()), STATS_TL_X + offset,
          STATS_TL_Y + 4, Display.RED);
      offset += Util.getNumberOfDigits(player.getHealth());
      terminal.write("/" + player.getMaxHealth() + ".", STATS_TL_X + offset,
          STATS_TL_Y + 4);
    } else {
      terminal.write("Health:" + player.getHealth() + "/" +
          player.getMaxHealth() + ".", STATS_TL_X + 1, STATS_TL_Y + 4);
    }
    if (player.getMana() < player.getMaxMana() * 0.25) {
      int offset = 6;
      terminal.write("Mana:", STATS_TL_X + 1, STATS_TL_Y + 5);
      terminal.write(Integer.toString(player.getMana()), STATS_TL_X + offset,
          STATS_TL_Y + 5, Display.RED);
      offset += Util.getNumberOfDigits(player.getMana());
      terminal.write("/" + player.getMaxMana() + ".", STATS_TL_X + offset,
          STATS_TL_Y + 5);
    } else {
      terminal.write("Mana:" + player.getMana() + "/" +
          player.getMaxMana() + ".", STATS_TL_X + 1, STATS_TL_Y + 5);
    }
    terminal.write("Power Level:" +
        player.getAttributes()[Creature.POWER_LEVEL] + ".", STATS_TL_X + 1,
        STATS_TL_Y + 6);
    terminal.write("Experience:" + player.getExperience() + "/" +
        player.getExperienceToLevel() + ".", STATS_TL_X + 1, STATS_TL_Y + 7);
    if (player.getCombatCooldown() > 0) {
      terminal.write("In combat.", STATS_TL_X + 1, STATS_TL_Y + 8,
          Display.RED);
    } else {
      terminal.write("Out of combat.", STATS_TL_X + 1, STATS_TL_Y + 8,
          Display.GREEN);
    }
    writeHorizontalLine(terminal, STATS_TL_X, STATS_WIDTH - 1, STATS_TL_Y + 9);

    // Display target information.
    Creature target = player.getTarget();
    if (!target.isOfType(new NullCreature()) &&
        (target.isAlive() || ((CorpseAI)target.getAI()).getTurnsLeft() > 0)) {
      terminal.write("Target Stats", STATS_TL_X + (STATS_WIDTH / 2) - 6,
          STATS_TL_Y + 11);
      writeHorizontalLine(terminal, STATS_TL_X, STATS_WIDTH - 1,
          STATS_TL_Y + 12);

      String targetName = "Name:" + target.getName();
      if (!target.isAlive()) {
        targetName += " Corpse";
      }
      targetName += ".";
      terminal.write(targetName, STATS_TL_X + 1, STATS_TL_Y + 13);
      terminal.write("Health:" + target.getHealth() + "/" +
          target.getMaxHealth() + ".", STATS_TL_X + 1, STATS_TL_Y + 14);
      terminal.write("Mana:" + target.getMana() + "/" +
          target.getMaxMana() + ".", STATS_TL_X + 1, STATS_TL_Y + 15);
      terminal.write("Power Level:" +
          target.getAttributes()[Creature.POWER_LEVEL] + ".", STATS_TL_X + 1,
          STATS_TL_Y + 16);
      if (target.getCombatCooldown() > 0) {
        terminal.write("In combat.", STATS_TL_X + 1, STATS_TL_Y + 17,
            Display.RED);
      } else {
        terminal.write("Out of combat.", STATS_TL_X + 1, STATS_TL_Y + 17,
            Display.GREEN);
      }
    }

    if (player.isInSubScreen()) {
      subScreen.displayOutput(terminal);
    }
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    if (!player.isAlive()) {
      return new EndScreen(player, false);
    } else if (player.hasWon()) {
      return new EndScreen(player, true);
    }
    if (player.isInSubScreen()) {
      subScreen = subScreen.respondToUserInput(key);
      return this;
    } else {
      switch (key.getKeyCode()) {
        case KeyEvent.VK_F1:
          return new HelpScreen(world, player, HelpScreen.PLAY_HELP);
        case KeyEvent.VK_ESCAPE:
          return new ConfirmExitScreen(world, player);
        case KeyEvent.VK_LEFT: player.moveBy(-1, 0); return this;
        case KeyEvent.VK_RIGHT: player.moveBy(1, 0); return this;
        case KeyEvent.VK_UP: player.moveBy(0, -1); return this;
        case KeyEvent.VK_DOWN: player.moveBy(0, 1); return this;
        case KeyEvent.VK_A:
          Equipment weapon =
              player.getEquipment().getEquipment(Equipment.WEAPON);
          if (!weapon.isOfType(new NullEquipment()) &&
              ((Weapon)weapon).getWeaponType() == Weapon.RANGED) {
            ((PlayerAI)player.getAI()).aim(Player.MAX_RANGE);
          }
          return this;
        case KeyEvent.VK_B:
          return new SpellbookScreen(world, player);
        case KeyEvent.VK_C:
          return new CharacterScreen(world, player);
        // Uncomment to be able to become invulnerable (used for debugging).
        // case KeyEvent.VK_G:
        //   player.setToGodMode();
        //   return this;
        case KeyEvent.VK_I:
          return new InventoryScreen(world, player);
        case KeyEvent.VK_L:
          player.setSubScreen(new LevelUpScreen(world, player));
          player.setInSubScreen(true);
          return this;
        case KeyEvent.VK_S:
          player.setSubScreen(new SpellCastScreen(world, player));
          player.setInSubScreen(true);
          return this;
        // Uncomment to be able to see everything (used for debugging).
        // case KeyEvent.VK_Q:
        //   int kt = (player.getAI().getKnowledgeType() + 1) % 3;
        //   player.getAI().setKnowledgeType(kt);
        //   return this;
        default:
          return this;
      }
    }
  }

  public Point getScrollLocation() {
    int scrollX = Math.max(0,
        Math.min(player.getLocation().x - (GAME_WIDTH - 2) / 2,
        player.getLevel().getWidth() - (GAME_WIDTH - 2)));
    int scrollY = Math.max(0,
        Math.min(player.getLocation().y - (GAME_HEIGHT - 2) / 2,
        player.getLevel().getHeight() - (GAME_HEIGHT - 2)));
    Point scrollLocation = new Point(scrollX, scrollY);

    player.setScreenLocation(new Point(
        player.getLocation().x - scrollLocation.x,
        player.getLocation().y - scrollLocation.y));
    return scrollLocation;
  }

  public void createWorld(int levels) {
    world = new Level[levels];
    ArrayList<ForegroundTile> foregroundOptions =
        new ArrayList<ForegroundTile>();
    ArrayList<BackgroundTile> backgroundOptions =
        new ArrayList<BackgroundTile>();

    foregroundOptions.add(new Wall());
    foregroundOptions.add(new Stone());
    backgroundOptions.add(new Floor());
    backgroundOptions.add(new Grass());

    ArrayList<String[]> input = readCSV("custom_tiles.csv");
    for (String[] line : input) {
      if (line.length == 5) {
        foregroundOptions.add(new CustomForegroundTile(line[0],
            (char)Integer.parseInt(line[1]), new Color(
            Integer.parseInt(line[2]), Integer.parseInt(line[3]),
            Integer.parseInt(line[4]))));
      } else {
        backgroundOptions.add(new CustomBackgroundTile(line[0],
            (char)Integer.parseInt(line[1]),
            new Color(Integer.parseInt(line[2]), Integer.parseInt(line[3]),
            Integer.parseInt(line[4])), new Color(Integer.parseInt(line[5]),
            Integer.parseInt(line[6]), Integer.parseInt(line[7]))));
      }
    }

    for (int i = 0; i < levels; i++) {
      ForegroundTile f = Util.pickRandomElement(foregroundOptions);
      BackgroundTile b = Util.pickRandomElement(backgroundOptions);
      boolean next = i == levels - 1 ? false : true;
      boolean prev = i == 0 ? false : true;
      world[i] = Math.random() < 0.5 ?
          new DungeonBuilder(91, 31, f, b, next, prev).build(5000, 5, 13, 0.1,
          -1) :
          new CaveBuilder(91, 31, f, b, next, prev).build();
      if (prev) {
        world[i].setPrevious(world[i - 1]);
        world[i - 1].setNext(world[i]);
      }
    }

    for (int l = 0; l < levels; l++) {
      cf = new CreatureFactory(world[l]);
      ff = new FurnitureFactory(world[l]);

      if (l == 0) {
        player = cf.newPlayer();
      }
      if (l == levels - 1) {
        ff.newTreasureChest();
      }
      for (int i = 0; i < 10; i++) {
        cf.newGoblin(Math.max(l + 1, (int)(Math.random() * (l + 3))));
        cf.newZombie(Math.max(l + 1, (int)(Math.random() * (l + 3))));
      }
      for (int i = 0; i < 20; i++) {
        ff.newTorch();
        ff.newTreasureChest(l + 1);
        ff.newSpikeTrap();
      }
      for (int i = 0; i < 10; i++) {
        ff.newSpawner(new String[]{"Zombie", "Goblin"},
            Math.max(l + 1, (int)(Math.random() * (l + 3))), 5, cf);
      }
    }
    player.setWorld(world);
  }

  public void displayTiles(AsciiPanel terminal, Point topLeft) {
    ArrayList<Point> attackTrajectories = new ArrayList<Point>();

    if (!player.isInSubScreen()) {
      player.getLevel().updateCreatures();
      player.getLevel().updateSpawners();

      if (player.hasAmulet() && !player.hasSpawnedExit()) {
        Point location;
        do {
          location = world[0].getRandomEmptyLocation();
        } while (world[0].countSurroundingForegroundTiles(location,
            new EmptyTile()) < 8);
        world[0].setBackgroundTile(location, new ExitPortal(
            world[0].getBackgroundTile(location)));
        for (int i = 0; i < world.length; i++) {
          world[i].powerUpCreatures();
        }
        player.setSpawnedExit(true);
      }
    }
    if (player.getAI().getKnowledgeType() == CreatureAI.OMNISCIENT) {
      for (int x = 0; x < GAME_WIDTH - 2; x++) {
        for (int y = 0; y < GAME_HEIGHT - 2; y++) {
          int wx = x + topLeft.x;
          int wy = y + topLeft.y;

          ForegroundTile tile = player.getLevel().getForegroundTile(wx, wy);
          if (tile instanceof Creature && !tile.getName().equals("Player") &&
              ((Creature)tile).isAlive()) {
            Line attackTrajectory = ((Creature)tile).getAttackTrajectory();
            if (attackTrajectory != null) {
              attackTrajectories.addAll(attackTrajectory.getPoints());
            }
          }

          // The + 1 for x and y accounts for the game border.
          terminal.write(player.getLevel().getDisplayGlyph(wx, wy), x + 1,
              y + 1, player.getLevel().getDisplayForegroundColour(wx, wy),
              player.getLevel().getDisplayBackgroundColour(wx, wy));
        }
      }
    } else {
      Level exploration = player.getExploration();

      for (int x = 0; x < GAME_WIDTH - 2; x++) {
        for (int y = 0; y < GAME_HEIGHT - 2; y++) {
          int wx = x + topLeft.x;
          int wy = y + topLeft.y;

          if (player.getAI().canSee(new Point(wx, wy)) ||
              player.getLevel().isIlluminated(new Point(wx, wy)) &&
              player.getAI().canSee(new Point(wx, wy), -1)) {
            ForegroundTile tile = player.getLevel().getForegroundTile(wx, wy);
            if (tile instanceof Creature && !tile.getName().equals("Player") &&
                ((Creature)tile).isAlive()) {
              Line attackTrajectory = ((Creature)tile).getAttackTrajectory();
              if (attackTrajectory != null) {
                attackTrajectories.addAll(attackTrajectory.getPoints());
              }
            }
            terminal.write(player.getLevel().getDisplayGlyph(wx, wy), x + 1,
                y + 1, player.getLevel().getDisplayForegroundColour(wx, wy),
                player.getLevel().getDisplayBackgroundColour(wx, wy));
          }
          else if (player.getAI().getKnowledgeType() == CreatureAI.LEARNING
              && exploration.getForegroundTile(wx, wy) != null) {
            terminal.write(exploration.getDisplayGlyph(wx, wy), x + 1, y + 1,
                exploration.getDisplayForegroundColour(wx,
                    wy).darker().darker().darker(),
                exploration.getDisplayBackgroundColour(wx,
                    wy).darker().darker().darker());
          }
        }
      }
    }

    for (int x = 0; x < GAME_WIDTH - 2; x++) {
      for (int y = 0; y < GAME_HEIGHT - 2; y++) {
        int wx = x + topLeft.x;
        int wy = y + topLeft.y;

        if (attackTrajectories.contains(new Point(wx, wy)) &&
            (player.getAI().getKnowledgeType() == CreatureAI.OMNISCIENT ||
            player.getAI().canSee(new Point(wx, wy)))) {
          terminal.write(player.getLevel().getDisplayGlyph(wx, wy), x + 1,
              y + 1, player.getLevel().getDisplayForegroundColour(wx, wy),
              Display.RED);
        }
      }
    }
  }
}
