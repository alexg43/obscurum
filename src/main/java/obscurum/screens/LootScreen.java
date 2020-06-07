package obscurum.screens;

import java.awt.event.KeyEvent;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.util.Util;
import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.display.ListEntry;
import obscurum.display.ScrollList;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;

/**
 * This models a subscreen where a player can see what items a corpse or a
 * treasure chest has, and can take them.
 * @author Alex Ghita
 */
public class LootScreen extends SubScreen {
  private Creature corpse;
  private ScrollList scrollList;

  public LootScreen(Level[] world, Player player, Creature corpse) {
    super(world, player);

    this.corpse = corpse;
    computeWidth();
    computeHeight();
    makeScrollList();
    computeTopLeft();
  }

  @Override
  protected void computeWidth() {
    /**
     * Determines the width of the loot screen. Standard width is 29, but it
     * will increase as much as necessary in order to fit each item's name on a
     * single line.
     */
    width = 30;
    for (int i = 0; i < corpse.getInventory().getSize(); i++) {
      if (corpse.getInventory().getAmount(i) > 0) {
        /**
         * This ensures that there is enough room for filler characters, such
         * as a space between the name and each vertical screen margin, a space
         * between the name and the amount, two spaces for brackets, and at
         * least one more space for the amount.
         */
        int fillerRoom = 5 +
            Util.getNumberOfDigits(corpse.getInventory().getAmount(i));

        width = Math.max(width,
            corpse.getInventory().getItem(i).getName().length() + fillerRoom);
      }
    }
  }

  @Override
  protected void computeHeight() {
    height = corpse.isAlive() ? 9 : 10;
  }

  private void makeScrollList() {
    ArrayList<ListEntry> list = new ArrayList<ListEntry>();

    for (int i = 0; i < corpse.getInventory().getSize(); i++) {
      if (corpse.getInventory().getSlot(i).isEmpty()) {
        continue;
      }
      list.add(new ListEntry(corpse.getInventory().getItem(i).getName() + " (" +
          corpse.getInventory().getAmount(i) + ")", i));
    }

    scrollList = new ScrollList(list, 3, width);
  }

  private void updateScrollList() {
    if (corpse.getInventory().getSlot(scrollList.getSelectedIndex())
        .isEmpty()) {
      scrollList.removeSelectedEntry();
    } else {
      scrollList.updateSelectedEntry(corpse.getInventory().getItem(
          scrollList.getSelectedIndex()).getName() + " (" +
          corpse.getInventory().getAmount(scrollList.getSelectedIndex()) + ")");
    }
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    String title = corpse.isAlive() ? corpse.getName() :
        corpse.getName() + " Corpse";
    terminal.clear(' ', topLeft.x + 1, topLeft.y + 1, width - 1, height - 1);
    drawBorders(terminal, topLeft, width, height);
    writeCentre(terminal, title, 1);
    writeHorizontalLine(terminal, 2);

    scrollList.printList(terminal, topLeft.x + 1, topLeft.y + 3);

    int helpHeight = corpse.isAlive() ? height - 3 : height - 4;

    writeHorizontalLine(terminal, helpHeight);
    if (!corpse.isAlive()) {
      writeCentre(terminal, "Press [x] to destroy corpse.", helpHeight + 1);
      helpHeight++;
    }
    writeCentre(terminal, "Press [e] to loot.", helpHeight + 1);
    writeCentre(terminal, "Press [esc] to return.", helpHeight + 2);
    terminal.repaint();
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    switch (key.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        player.setInSubScreen(false);
        return new PlayScreen(world, player);
      case KeyEvent.VK_UP:
        scrollList.scrollUp();
        return this;
      case KeyEvent.VK_DOWN:
        scrollList.scrollDown();
        return this;
      case KeyEvent.VK_E:
        if (corpse.getInventory().countFilledSlots() > 0) {
          player.loot(
              corpse.getInventory().getSlot(scrollList.getSelectedIndex()));
          updateScrollList();
        }
        return this;
      case KeyEvent.VK_X:
        if (corpse.isAlive()) {
          return this;
        }
        corpse.getLevel().remove(corpse);
        player.setInSubScreen(false);
        return new PlayScreen(world, player);
      default:
        return this;
    }
  }
}
