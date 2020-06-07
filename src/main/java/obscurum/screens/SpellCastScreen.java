package obscurum.screens;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.util.Util;
import obscurum.creatures.Player;
import obscurum.creatures.abilities.Spell;
import obscurum.display.Display;
import obscurum.display.ListEntry;
import obscurum.display.ScrollList;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.Level;

/**
 * This models a subscreen from which the player can select a known spell to
 * cast.
 * @author Alex Ghita
 */
public class SpellCastScreen extends SubScreen {
  private ScrollList scrollList;

  public SpellCastScreen(Level[] world, Player player) {
    super(world, player);

    computeWidth();
    computeHeight();
    makeScrollList();
    computeTopLeft();
  }

  @Override
  protected void computeWidth() {
    width = 30;
    for (int i = 0; i < player.getSpells().size(); i++) {
      Spell spell = player.getSpells().get(i);
      int spellLength = spell.getName().length() +
          Util.getNumberOfDigits(spell.getLevel()) +
          Util.getNumberOfDigits(spell.getManaCost()) +
          Util.getNumberOfDigits(spell.getCurrentCooldown()) +
          Util.getNumberOfDigits(spell.getCooldown()) + 15;

      width = Math.max(width, spellLength);
    }
  }

  @Override
  protected void computeHeight() {
    // The height will be constant.
    height = 11;
  }

  private void makeScrollList() {
    ArrayList<ListEntry> list = new ArrayList<ListEntry>();

    for (int i = 0; i < player.getSpells().size(); i++) {
      Spell spell = player.getSpells().get(i);
      list.add(new ListEntry(spell.getName() + " " + spell.getLevel(), i));
    }

    scrollList = new ScrollList(list, 3, width);
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    terminal.clear(' ', topLeft.x + 1, topLeft.y + 1, width - 1, height - 1);
    drawBorders(terminal, topLeft, width, height);
    writeCentre(terminal, "Cast a Spell", 1);
    writeHorizontalLine(terminal, 2);

    ArrayList<ListEntry> entries = scrollList.getEntries();
    int selected = scrollList.getSelected();
    int selectedHeight = scrollList.getSelectedHeight();
    if (entries.size() > 0) {
      int linesPrinted = 0;
      for (int i = 0; i < scrollList.getHeight(); i++) {
        int index = selected - selectedHeight + i;
        if (index >= entries.size()) {
          break;
        }
        Spell spell = player.getSpells().get(index);
        int xOffset = entries.get(index).getText().length();
        String display;
        Color displayColour;

        displayColour = spell.getSpellType() == Spell.REST_SPELL &&
                player.getCombatCooldown() > 0 ? Display.RED : Display.WHITE;
        terminal.write(entries.get(index).getText(), topLeft.x + 2,
            topLeft.y + i + 3, displayColour);
        terminal.write(" (", topLeft.x + 2 + xOffset, topLeft.y + i + 3);
        xOffset += 2;
        displayColour = spell.getManaCost() > player.getMana() ?
            Display.BRIGHT_RED : Display.WHITE;
        display = "MC:" + spell.getManaCost();
        terminal.write(display, topLeft.x + 2 + xOffset, topLeft.y + i + 3,
            displayColour);
        xOffset += display.length();
        display = ", ";
        terminal.write(display, topLeft.x + 2 + xOffset, topLeft.y + i + 3);
        xOffset += display.length();
        displayColour = spell.getCurrentCooldown() == 0 ? Display.WHITE :
            Display.BRIGHT_RED;
        display = "CD:" + spell.getCurrentCooldown() + "/" +
            spell.getCooldown();
        terminal.write(display, topLeft.x + 2 + xOffset, topLeft.y + i + 3,
            displayColour);
        xOffset += display.length();
        terminal.write(")", topLeft.x + 2 + xOffset, topLeft.y + i + 3);
        linesPrinted++;
      }
      // for (int i = linesPrinted + 1; i < scrollList.getHeight(); i++) {
      //   terminal.write(emptyLine, topLeft.x, topLeft.y + i);
      // }
      terminal.write(">", topLeft.x + 1, topLeft.y + selectedHeight + 3);
    }

    if (player.getSpells().size() == 0) {
      terminal.write("  You don't know any spells.", topLeft.x + 1,
          topLeft.y + height - 7);
    }

    writeHorizontalLine(terminal, height - 5);
    terminal.write("MC = Mana Cost, CD = Cooldown", topLeft.x + 1,
        topLeft.y + height - 4);
    writeHorizontalLine(terminal, height - 3);
    writeCentre(terminal, "Press [e] to use spell.", height - 2);
    writeCentre(terminal, "Press [esc] to return.", height - 1);
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
        if (scrollList.getEntries().isEmpty() ||
            !player.canCastSpell(scrollList.getSelectedIndex())) {
          return this;
        }
        Spell selectedSpell =
            player.getSpells().get(scrollList.getSelectedIndex());
        player.setSubScreen(new SpellAimScreen(world, player,
            selectedSpell.getMaxRange(), scrollList.getSelectedIndex()));
        return new PlayScreen(world, player);
      default:
        return this;
    }
  }
}
