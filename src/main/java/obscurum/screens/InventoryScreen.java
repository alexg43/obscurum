package obscurum.screens;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import obscurum.GameMain;
import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.display.Display;
import obscurum.display.DisplayTile;
import obscurum.environment.Level;
import obscurum.items.ConsumableItem;
import obscurum.items.Equipment;
import obscurum.items.Inventory;
import obscurum.items.InventorySlot;
import obscurum.items.Item;
import obscurum.placeholders.NullItem;

/**
 * This models an inventory screen, where the player can browse their items,
 * read their properties, use or equip them, and see depictions of equippable
 * items.
 * @author Alex Ghita
 */
public class InventoryScreen extends Screen {
  // Constants for inventory slot display.
  private static final int SLOT_COUNT = 54;
  private static final int SLOT_WIDTH = 6;
  private static final int SLOTS_PER_ROW = 6;
  // Constants for inventory section placement and size.
  private static final int INVENTORY_TL_X = 0;
  private static final int INVENTORY_TL_Y = 0;
  private static final int INVENTORY_WIDTH = 38;
  private static final int INVENTORY_HEIGHT = GameMain.SCREEN_HEIGHT;
  // Constants for the selected item's description placement and size.
  private static final int DESCRIPTION_TL_X = INVENTORY_WIDTH - 1;
  private static final int DESCRIPTION_TL_Y = 0;
  private static final int DESCRIPTION_WIDTH = 22;
  private static final int DESCRIPTION_HEIGHT = INVENTORY_HEIGHT;
  // Constants for the selected item's display, if it is a piece of equipment.
  private static final int DISPLAY_TL_X =
      DESCRIPTION_TL_X + DESCRIPTION_WIDTH - 1;
  private static final int DISPLAY_TL_Y = 0;
  public static final int DISPLAY_WIDTH = GameMain.SCREEN_WIDTH -
      INVENTORY_WIDTH - DESCRIPTION_WIDTH + 2;
  public static final int DISPLAY_HEIGHT = INVENTORY_HEIGHT;
  private int highlighted;

  /**
   * Class constructor specifying the game world and the player.
   * @param world
   * @param player
   */
  public InventoryScreen(Level[] world, Player player) {
    super(world, player);
    highlighted = 0;
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    Inventory inventory = player.getInventory();
    Item item = inventory.getSlot(highlighted).getItem();
    String description;

    // Print inventory slots.
    drawBorders(terminal, new Point(INVENTORY_TL_X, INVENTORY_TL_Y),
        INVENTORY_WIDTH, INVENTORY_HEIGHT, false, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    for (int i = 0; i < inventory.getSize(); i++) {
      // The highlighted slot's border has a different colour.
      if (i == highlighted) {
        printInventorySlot(terminal, i, 1);
      } else {
        printInventorySlot(terminal, i, 0);
      }
    }
    // Print the locked slots, which have a different border colour.
    for (int i = inventory.getSize(); i < SLOT_COUNT; i++) {
      printInventorySlot(terminal, i, 2);
    }

    // Print the selected item's description.
    drawBorders(terminal, new Point(DESCRIPTION_TL_X, DESCRIPTION_TL_Y),
        DESCRIPTION_WIDTH, DESCRIPTION_HEIGHT, false, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    // If the item is null, there is no description to print.
    if (!item.isOfType(new NullItem())) {
      terminal.writeMultiline(inventory.getSlot(highlighted).toString(),
          DESCRIPTION_TL_X + 1, DESCRIPTION_TL_Y + 1,
          DESCRIPTION_TL_X + DESCRIPTION_WIDTH,// + 1,
          DESCRIPTION_TL_Y + DESCRIPTION_HEIGHT,
          Display.QUALITY_COLOURS[item.getQuality()]);
      description = item.getDescription();
      // If the item is a piece of equipment, also add its attributes and slot.
      if (item instanceof Equipment) {
        description += " \n " + ((Equipment)item).getProperties() + ". \n " +
            "Item level " + ((Equipment)item).getItemLevel() + ".";
      }
      if (item instanceof ConsumableItem) {
        description = "[Usable] " + description;
      }
      terminal.writeMultiline(description, DESCRIPTION_TL_X + 1,
          terminal.getLastMultilineY() + 1,
          DESCRIPTION_TL_X + DESCRIPTION_WIDTH + 1,
          DESCRIPTION_TL_Y + DESCRIPTION_HEIGHT);

      if (item.getRequiredLevel() > 1) {
        Color displayColour;
        if (player.getAttributes()[Creature.POWER_LEVEL] >=
            item.getRequiredLevel()) {
          displayColour = Display.WHITE;
        } else {
          displayColour = Display.BRIGHT_RED;
        }
        terminal.write("Requires level " + item.getRequiredLevel() + ".",
            DESCRIPTION_TL_X + 1, terminal.getLastMultilineY() + 1,
            displayColour);
      }
    }
    terminal.write("Press [F1] for help.", DESCRIPTION_TL_X + 1,
        DESCRIPTION_HEIGHT - 2);

    // Print the selected item's display.
    drawBorders(terminal, new Point(DISPLAY_TL_X, DISPLAY_TL_Y), DISPLAY_WIDTH,
        DISPLAY_HEIGHT, false, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    if (item instanceof Equipment || item instanceof ConsumableItem) {
      DisplayTile[][] depiction = item instanceof Equipment ?
          ((Equipment)item).getDepiction() :
          ((ConsumableItem)item).getDepiction();
      // The depiction will be centred on the display panel.
      int topLeftX = DISPLAY_TL_X + (DISPLAY_WIDTH - depiction[0].length) / 2;
      int topLeftY = DISPLAY_TL_Y + (DISPLAY_HEIGHT - depiction.length) / 2;

      for (int i = 0; i < depiction.length; i++) {
        for (int j = 0; j < depiction[0].length; j++) {
          terminal.write(depiction[i][j].getGlyph(), topLeftX + j, topLeftY + i,
              depiction[i][j].getGlyphColour(),
              depiction[i][j].getBackgroundColour());
        }
      }
    }

    // Connect frames.
    terminal.write(Display.DH_DB_INTERSECT, DESCRIPTION_TL_X, DESCRIPTION_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DB_INTERSECT, DISPLAY_TL_X, DISPLAY_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, DESCRIPTION_TL_X,
        INVENTORY_HEIGHT - 1,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, DISPLAY_TL_X, INVENTORY_HEIGHT - 1,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
  }

  /**
   * Prints an inventory slot based on its index, i.e. how many slots have been
   * printed before it.
   * @param terminal
   * @param index
   * @param type if 0, the slot is usable, if 1, the slot is highlighted, i.e.
   *             currently selected by the player, if 2, the slot is locked and
   *             cannot be used yet
   */
  private void printInventorySlot(AsciiPanel terminal, int index, int type) {
    int x = (index % SLOT_WIDTH) * SLOTS_PER_ROW + 1;
    int y = 3 * (index / SLOT_WIDTH) + 1;
    Color colour = type == 0 ? Display.WHITE : type == 1 ?
        Display.GREEN : Display.BRIGHT_BLACK;
    InventorySlot slot = index >= player.getInventory().getSize() ?
        new InventorySlot() : player.getInventory().getSlot(index);

    // Print corners.
    terminal.write(Display.STL_CORNER, x, y, colour);
    terminal.write(Display.SBL_CORNER, x, y + 2, colour);
    terminal.write(Display.STR_CORNER, x + 5, y, colour);
    terminal.write(Display.SBR_CORNER, x + 5, y + 2, colour);
    // Print vertical lines.
    terminal.write(Display.SV_LINE, x, y + 1, colour);
    terminal.write(Display.SV_LINE, x + 2, y + 1, colour);
    terminal.write(Display.SV_LINE, x + 5, y + 1, colour);
    // Print horizontal lines.
    for (int i = 1; i <= 4; i++) {
      if (i == 2) {
        terminal.write(Display.SH_SB_INTERSECT, x + i, y, colour);
        terminal.write(Display.SH_ST_INTERSECT, x + i, y + 2, colour);
      } else {
        terminal.write(Display.SH_LINE, x + i, y, colour);
        terminal.write(Display.SH_LINE, x + i, y + 2, colour);
      }
    }
    // Print slot content.
    if (!slot.isEmpty()) {
      terminal.write(slot.getItem().getGlyph(), x + 1, y + 1,
          slot.getItem().getColour());
      if (slot.getAmount() < 10) {
        terminal.write(" " + slot.getAmount(), x + 3, y + 1);
      } else {
        terminal.write(Integer.toString(slot.getAmount()), x + 3, y + 1);
      }
    }
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    switch (key.getKeyCode()) {
      case KeyEvent.VK_F1:
        return new HelpScreen(world, player, HelpScreen.INVENTORY_HELP);
      case KeyEvent.VK_E:
        player.use(player.getInventory().getSlot(highlighted));
        return this;
      case KeyEvent.VK_X:
        if (!player.getInventory().getItem(highlighted).getName().equals(
            "Amulet of Power")) {
          player.getInventory().getSlot(highlighted).clear();
        }
        return this;
      case KeyEvent.VK_UP:
        highlighted = highlighted - 6 < 0 ? highlighted : highlighted - 6;
        return this;
      case KeyEvent.VK_DOWN:
        highlighted = highlighted + 6 >= player.getInventory().getSize() ?
            highlighted : highlighted + 6;
        return this;
      case KeyEvent.VK_LEFT:
        highlighted = highlighted % 6 == 0 ? highlighted : highlighted - 1;
        return this;
      case KeyEvent.VK_RIGHT:
        highlighted = highlighted % 6 == 5 ? highlighted :
            highlighted + 1 >= player.getInventory().getSize() ?
            highlighted : highlighted + 1;
        return this;
      case KeyEvent.VK_ESCAPE:
        return new PlayScreen(world, player);
      default:
        return this;
    }
  }
}
