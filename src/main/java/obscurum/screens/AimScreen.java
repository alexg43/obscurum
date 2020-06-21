package obscurum.screens;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.util.Line;
import obscurum.display.Display;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;
import obscurum.placeholders.NullCreature;

/**
 * This models an aim screen, which can be used to aim with ranged weapons or
 * spells.
 * @author Alex Ghita
 */
public abstract class AimScreen extends SubScreen {
  protected Point playerLocation;
  protected Point targetLocation;
  protected Point screenLocation;
  protected int maxRange;

  public AimScreen(Level[] world, Player player, int maxRange) {
    super(world, player);
    this.maxRange = maxRange;

    playerLocation = player.getLocation();
    targetLocation = player.getLocation();
    screenLocation = player.getScreenLocation();
    player.setTarget(player);
    computeWidth();
    computeHeight();
    computeTopLeft();
  }

  @Override
  protected void computeWidth() {
    width = PlayScreen.GAME_WIDTH - 2;
  }

  @Override
  protected void computeHeight() {
    height = PlayScreen.GAME_HEIGHT - 2;
  }

  @Override
  protected void computeTopLeft() {
    topLeft = new Point(PlayScreen.GAME_TL_X + 1, PlayScreen.GAME_TL_Y + 1);
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    Line trajectory = new Line(playerLocation, targetLocation);
    trajectory.plotLine();
    List<Point> points = trajectory.getPointsOnLine();

    for (Point p : points) {
      int xOffset = targetLocation.x - p.x;
      int yOffset = targetLocation.y - p.y;
      terminal.write(player.getLevel().getDisplayGlyph(p),
          screenLocation.x + 1 - xOffset, screenLocation.y + 1 - yOffset,
          player.getLevel().getDisplayForegroundColour(p), Display.MAGENTA);
    }
    terminal.repaint();
  }

  private void moveTarget(int xOffset, int yOffset) {
    Point newTargetLocation = new Point(targetLocation.x + xOffset,
        targetLocation.y + yOffset);

    if (!player.getLevel().isInBounds(newTargetLocation)) {
      return;
    }
    int maxRangeSquared = Math.min(maxRange,
        player.getAttributes()[Creature.LINE_OF_SIGHT]) *
        Math.min(maxRange, player.getAttributes()[Creature.LINE_OF_SIGHT]);
    if ((newTargetLocation.x - playerLocation.x) *
        (newTargetLocation.x - playerLocation.x) +
        (newTargetLocation.y - playerLocation.y) *
        (newTargetLocation.y - playerLocation.y) > maxRangeSquared) {
      return;
    }
    if (!player.getAI().canSee(newTargetLocation)) {
      return;
    }
    targetLocation = newTargetLocation;
    screenLocation.x += xOffset;
    screenLocation.y += yOffset;
    if (player.getLevel().getForegroundTile(targetLocation) instanceof
        Creature) {
      player.setTarget(
          (Creature)(player.getLevel().getForegroundTile(targetLocation)));
    } else {
      player.setTarget(new NullCreature());
    }
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    switch (key.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        player.setInSubScreen(false);
        return new PlayScreen(world, player);
      case KeyEvent.VK_UP:
        moveTarget(0, -1);
        return this;
      case KeyEvent.VK_RIGHT:
        moveTarget(1, 0);
        return this;
      case KeyEvent.VK_DOWN:
        moveTarget(0, 1);
        return this;
      case KeyEvent.VK_LEFT:
        moveTarget(-1, 0);
        return this;
      case KeyEvent.VK_A:
        performAction();
        player.setInSubScreen(false);
        return new PlayScreen(world, player);
      default:
        return this;
    }
  }

  protected abstract void performAction();
}
