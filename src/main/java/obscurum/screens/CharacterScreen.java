package obscurum.screens;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;

import obscurum.GameMain;
import obscurum.creatures.Creature;
import obscurum.creatures.Player;
import obscurum.display.Display;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;
import obscurum.items.Equipment;
import obscurum.placeholders.NullEquipment;

/**
 * This models a screen that displays information about the player character,
 * such as the equipped items. It also lets players unequip items and view
 * their attributes.
 * @author Alex Ghita
 */
public class CharacterScreen extends Screen {
  private static final int EQUIPMENT_TL_X = 0;
  private static final int EQUIPMENT_TL_Y = 0;
  private static final int EQUIPMENT_WIDTH = 38;
  private static final int EQUIPMENT_HEIGHT = GameMain.SCREEN_HEIGHT;
  private static final int SELECTED_ITEM_TL_X = EQUIPMENT_WIDTH - 1;
  private static final int SELECTED_ITEM_TL_Y = 0;
  private static final int SELECTED_ITEM_WIDTH = 22;
  private static final int SELECTED_ITEM_HEIGHT = EQUIPMENT_HEIGHT;
  private static final int ATTRIBUTES_TL_X =
      EQUIPMENT_WIDTH + SELECTED_ITEM_WIDTH - 2;
  private static final int ATTRIBUTES_TL_Y = 0;
  private static final int ATTRIBUTES_WIDTH =
      GameMain.SCREEN_WIDTH - EQUIPMENT_WIDTH - SELECTED_ITEM_WIDTH + 2;
  private static final int ATTRIBUTES_HEIGHT = EQUIPMENT_HEIGHT;
  private static final int EQUIPMENT_SLOT_COUNT = Equipment.FEET + 1;
  private static final int EQUIPMENT_SLOT_WIDTH = 7;
  private static final int EQUIPMENT_SLOT_HEIGHT = 5;
  private static final String[] PLAYER_DEPICTION = {
    "   _   |",
    "  |@|  |",
    "  /|\\  |",
    " | | \\_#|",
    " \" |   |",
    "  / \\   ",
    " /  |   ",
    " |  |   ",
    " " + Display.SBR_CORNER + "  " + Display.SBL_CORNER + "   "
  };
  private int highlighted;

  public CharacterScreen(Level[] world, Player player) {
    super(world, player);
    highlighted = 0;
  }

  private void printEquipmentList(AsciiPanel terminal) {
    drawBorders(terminal, new Point(EQUIPMENT_TL_X, EQUIPMENT_TL_Y),
        EQUIPMENT_WIDTH, EQUIPMENT_HEIGHT, false, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    // Draw equipment slots.
    for (int i = 0; i < EQUIPMENT_SLOT_COUNT; i++) {
      int tlX = i < 3 ? EQUIPMENT_TL_X + 5 : EQUIPMENT_TL_X + 26;
      int tlY = 3 + i % 3 * 5;
      Color borderColour = highlighted == i ?
          Display.GREEN : Display.FG_WINDOW_FRAME;

      drawBorders(terminal, new Point(tlX, tlY), EQUIPMENT_SLOT_WIDTH,
          EQUIPMENT_SLOT_HEIGHT, false, borderColour, Display.BG_WINDOW_FRAME);

      if (!player.getEquipment().getEquipment(i).isOfType(
          new NullEquipment())) {
        Equipment displayItem = player.getEquipment().getEquipment(i);
        Color quality = Display.QUALITY_COLOURS[displayItem.getQuality()];

        terminal.write(displayItem.getGlyph(), tlX + 3, tlY + 2,
            displayItem.getColour());
        drawBorders(terminal, new Point(tlX + 1, tlY + 1),
            EQUIPMENT_SLOT_WIDTH - 2, EQUIPMENT_SLOT_HEIGHT - 2, true, quality,
            Display.BLACK);
      } else {
        terminal.clear(' ', tlX + 1, tlY + 1, 3, 3);
      }
    }
  }

  private void printPlayerDepiction(AsciiPanel terminal) {
    // Draw player.
    int playerTLX = EQUIPMENT_TL_X + 14;
    int playerTLY = EQUIPMENT_TL_Y + 6;

    for (int i = 0; i < PLAYER_DEPICTION.length; i++) {
      terminal.write(PLAYER_DEPICTION[i], playerTLX, playerTLY + i);
    }

    // Highlight a part of the player depiction based on what slot is selected.
    switch (highlighted) {
      case Equipment.HEAD:
        for (int i = 0; i < 2; i++) {
          terminal.write(PLAYER_DEPICTION[i].substring(0,
              PLAYER_DEPICTION[i].length() - 1), playerTLX, playerTLY + i,
              Display.GREEN);
        }
        break;
      case Equipment.CHEST:
        terminal.write(PLAYER_DEPICTION[2].substring(0,
          PLAYER_DEPICTION[2].length() - 1), playerTLX, playerTLY + 2,
          Display.GREEN);
        for (int i = 3; i < 5; i++) {
          terminal.write(PLAYER_DEPICTION[i].substring(2, 4), playerTLX + 2,
              playerTLY + i, Display.GREEN);
        }
        break;
      case Equipment.HANDS:
        for (int i = 3; i < 5; i++) {
          terminal.write(PLAYER_DEPICTION[i].substring(0, 2), playerTLX,
              playerTLY + i, Display.GREEN);
          terminal.write(PLAYER_DEPICTION[i].substring(5, 7), playerTLX + 5,
              playerTLY + i, Display.GREEN);
        }
        break;
      case Equipment.WEAPON:
        for (int i = 0; i < 5; i++) {
          terminal.write(PLAYER_DEPICTION[i].substring(
              PLAYER_DEPICTION[i].length() - 2, PLAYER_DEPICTION[i].length()),
              playerTLX + PLAYER_DEPICTION[i].length() - 2 , playerTLY + i,
              Display.GREEN);
        }
        break;
      case Equipment.LEGS:
        for (int i = 5; i < 8; i++) {
          terminal.write(PLAYER_DEPICTION[i], playerTLX, playerTLY + i,
              Display.GREEN);
        }
        break;
      default:
        terminal.write(PLAYER_DEPICTION[8], playerTLX, playerTLY + 8,
            Display.GREEN);
        break;
    }
  }

  private void printSelectedItemDescription(AsciiPanel terminal) {
    drawBorders(terminal, new Point(SELECTED_ITEM_TL_X, SELECTED_ITEM_TL_Y),
        SELECTED_ITEM_WIDTH, SELECTED_ITEM_HEIGHT, false,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    if (highlighted <= Equipment.FEET) {
      Equipment selectedItem = player.getEquipment().getEquipment(highlighted);
      if (!selectedItem.isOfType(new NullEquipment())) {
        String description;

        terminal.write(selectedItem.getName(), SELECTED_ITEM_TL_X + 1,
            SELECTED_ITEM_TL_Y + 1,
            Display.QUALITY_COLOURS[selectedItem.getQuality()]);
        description = selectedItem.getDescription();
        description += " \n " + selectedItem.getProperties() + ". \n " +
            "Item level " + selectedItem.getItemLevel() + ".";
        description += " \n " + "Requires level " +
            selectedItem.getRequiredLevel() + ".";
        terminal.writeMultiline(description, SELECTED_ITEM_TL_X + 1,
            SELECTED_ITEM_TL_Y + 2,
            SELECTED_ITEM_TL_X + SELECTED_ITEM_WIDTH + 1,
            SELECTED_ITEM_TL_Y + SELECTED_ITEM_HEIGHT);
      }
    }
    terminal.write("Press [F1] for help.", SELECTED_ITEM_TL_X + 1,
        SELECTED_ITEM_HEIGHT - 2);
  }

  private void printPlayerAttributes(AsciiPanel terminal) {
    drawBorders(terminal, new Point(ATTRIBUTES_TL_X, ATTRIBUTES_TL_Y),
        ATTRIBUTES_WIDTH, ATTRIBUTES_HEIGHT, false, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    int[] attributes = player.getAttributes();
    int[] baseAttributes = player.getBaseAttributes();

    terminal.write("Health:" + player.getCurrentHealth() + "/" +
        player.getMaxHealth() + ".", ATTRIBUTES_TL_X + 1,
        ATTRIBUTES_TL_Y + 1);
    terminal.write("Mana:" + player.getMana() + "/" +
        player.getMaxMana() + ".", ATTRIBUTES_TL_X + 1, ATTRIBUTES_TL_Y + 2);
    terminal.write("Defence:" + player.getArmour() + ".", ATTRIBUTES_TL_X + 1,
        ATTRIBUTES_TL_Y + 3);

    for (int i = 0; i < Creature.NUM_OF_ATTRIBUTES; i++) {
      String output;
      int outputLength;
      Color outputColour;

      switch (i) {
        case Creature.POWER_LEVEL:
          output = "Power Level";
          break;
        case Creature.LINE_OF_SIGHT:
          output = "Line of Sight";
          break;
        case Creature.INVENTORY_SIZE:
          output = "Inventory Size";
          break;
        case Creature.STRENGTH:
          output = "Strength";
          break;
        case Creature.AGILITY:
          output = "Agility";
          break;
        case Creature.STAMINA:
          output = "Stamina";
          break;
        case Creature.SPIRIT:
          output = "Spirit";
          break;
        case Creature.INTELLECT:
          output = "Intellect";
          break;
        case Creature.ATTACK_POWER:
          output = "Attack Power";
          break;
        case Creature.SPELL_POWER:
          output = "Spell Power";
          break;
        default:
          output = "Dodge Chance";
          break;
      }
      output += ":";
      outputLength = output.length();
      terminal.write(output, ATTRIBUTES_TL_X + 1, ATTRIBUTES_TL_Y + 4 + i);

      outputColour = attributes[i] > baseAttributes[i] ?
          Display.QUALITY_COLOURS[1] : Display.WHITE;
      output = i == Creature.DODGE_CHANCE ?
          Integer.toString(attributes[i]) + "%" :
          Integer.toString(attributes[i]);
      terminal.write(output, ATTRIBUTES_TL_X + 1 + outputLength,
          ATTRIBUTES_TL_Y + 4 + i, outputColour);
      outputLength = attributes[i] < 10 ? outputLength + 1 :
          attributes[i] < 100 ? outputLength + 2 : outputLength + 3;
      if (i == Creature.DODGE_CHANCE) {
        outputLength++;
      }
      if (attributes[i] > baseAttributes[i]) {
        terminal.write(" (" + baseAttributes[i] + "+",
            ATTRIBUTES_TL_X + 1 + outputLength, ATTRIBUTES_TL_Y + 4 + i);
        outputLength = baseAttributes[i] < 10 ? outputLength + 4 :
            baseAttributes[i] < 100 ? outputLength + 5 : outputLength + 6;
        terminal.write(Integer.toString(attributes[i] - baseAttributes[i]),
            ATTRIBUTES_TL_X + 1 + outputLength, ATTRIBUTES_TL_Y + 4 + i,
            Display.QUALITY_COLOURS[1]);
        outputLength = attributes[i] - baseAttributes[i] < 10 ?
            outputLength + 1 :
            attributes[i] - baseAttributes[i] < 100 ?
            outputLength + 2 : outputLength + 3;
        terminal.write(").", ATTRIBUTES_TL_X + 1 + outputLength,
            ATTRIBUTES_TL_Y + 4 + i);
        outputLength++;
      } else {
        terminal.write(".", ATTRIBUTES_TL_X + 1 + outputLength,
            ATTRIBUTES_TL_Y + 4 + i);
      }
    }
    terminal.write("Experience:" + player.getExperience() + "/" +
        player.getExperienceToLevel() + ".", ATTRIBUTES_TL_X + 1,
        ATTRIBUTES_TL_Y + 4 + Creature.NUM_OF_ATTRIBUTES);
  }

  private void connectFrames(AsciiPanel terminal) {
    terminal.write(Display.DH_DB_INTERSECT, ATTRIBUTES_TL_X, ATTRIBUTES_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DB_INTERSECT, SELECTED_ITEM_TL_X,
        SELECTED_ITEM_TL_Y, Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, ATTRIBUTES_TL_X,
        EQUIPMENT_HEIGHT - 1, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, SELECTED_ITEM_TL_X,
        EQUIPMENT_HEIGHT - 1,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    printEquipmentList(terminal);
    printPlayerDepiction(terminal);
    printSelectedItemDescription(terminal);
    printPlayerAttributes(terminal);
    connectFrames(terminal);
  }

  public Screen respondToUserInput(KeyEvent key) {
    switch (key.getKeyCode()) {
      case KeyEvent.VK_F1:
        return new HelpScreen(world, player, HelpScreen.CHARACTER_HELP);
      case KeyEvent.VK_ESCAPE:
        return new PlayScreen(world, player);
      case KeyEvent.VK_UP:
        highlighted = highlighted % (Equipment.NUM_OF_SLOTS / 2) == 0 ?
            highlighted : highlighted - 1;
        return this;
      case KeyEvent.VK_DOWN:
        highlighted = highlighted % (Equipment.NUM_OF_SLOTS / 2) ==
            Equipment.NUM_OF_SLOTS / 2 - 1 ? highlighted : highlighted + 1;
        return this;
      case KeyEvent.VK_LEFT:
        highlighted = highlighted < Equipment.NUM_OF_SLOTS / 2 ?
            highlighted : highlighted - Equipment.NUM_OF_SLOTS / 2;
        return this;
      case KeyEvent.VK_RIGHT:
        highlighted = highlighted < Equipment.NUM_OF_SLOTS / 2 ?
            highlighted + Equipment.NUM_OF_SLOTS / 2 : highlighted;
        return this;
      case KeyEvent.VK_E:
        Equipment selected = player.getEquipment().getEquipment(highlighted);
        if (!selected.isOfType(new NullEquipment())) {
          player.unequip(highlighted);
        }
        return this;
      default:
        return this;
    }
  }
}
