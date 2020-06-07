package obscurum.screens;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.Math;
import java.util.ArrayList;
import obscurum.GameMain;
import obscurum.environment.background.BackgroundTile;
import obscurum.display.Display;
import obscurum.display.ListEntry;
import obscurum.display.ScrollList;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.foreground.CustomForegroundTile;

/**
 * This models a subscreen where players can add new tiles through the game
 * editor.
 * @author Alex Ghita
 */
public class EditorNewItemScreen extends SubScreen {
  private GameEditorScreen mainScreen;
  private ScrollList scrollList;
  private int objectType; // 0 = foreground, 1 = background
  private String name;
  private int fgIndex;
  private int bgIndex;
  private char glyph;
  private boolean capsLock;

  public EditorNewItemScreen(GameEditorScreen mainScreen, int objectType) {
    super();
    this.mainScreen = mainScreen;
    this.objectType = objectType;

    name = objectType == 0 ? "New Foreground Tile" : "New Background Tile";
    fgIndex = bgIndex = 0;
    glyph = '#';
    capsLock = false;
    makeScrollList();

    computeWidth();
    computeHeight();
    computeTopLeft();
  }

  private void makeScrollList() {
    ArrayList<ListEntry> list = new ArrayList<ListEntry>();

    list.add(new ListEntry("Name: " + name, 0));
    list.add(new ListEntry("Glyph: " + Character.toString(glyph), 1));
    list.add(new ListEntry("Foreground Colour: " +
        Display.COLOUR_NAMES[fgIndex], 2));
    if (objectType == 1) {
      list.add(new ListEntry("Background Colour: " +
          Display.COLOUR_NAMES[bgIndex], 3));
    }

    scrollList = new ScrollList(list, 5, width - 2);
  }

  @Override
  protected void computeWidth() {
    width = 60;
  }

  @Override
  protected void computeHeight() {
    // The height will be constant.
    height = 15;
  }

  @Override
  protected void computeTopLeft() {
    topLeft = new Point(GameMain.SCREEN_WIDTH / 2 - width / 2,
        GameMain.SCREEN_HEIGHT / 2 - height / 2);
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    terminal.clear(' ', topLeft.x + 1, topLeft.y + 1, width - 1, height - 1);
    drawBorders(terminal, topLeft, width, height);
    writeCentre(terminal, "Create a new object", 1);
    writeHorizontalLine(terminal, 2);

    scrollList.printList(terminal, topLeft.x + 1, topLeft.y + 3);
    writeHorizontalLine(terminal, height - 8);
    terminal.write("In-game Display: ", topLeft.x + 1, topLeft.y + height - 7);
    if (objectType == 0) {
      terminal.write(Character.toString(glyph), topLeft.x + 18,
          topLeft.y + height - 7, Display.COLOURS[fgIndex]);
    } else {
      terminal.write(Character.toString(glyph), topLeft.x + 18,
          topLeft.y + height - 7, Display.COLOURS[fgIndex],
          Display.COLOURS[bgIndex]);
    }

    writeHorizontalLine(terminal, height - 6);
    writeCentre(terminal, "Use letters & numbers for names, [backspace] " +
        "to delete.", height - 5);
    writeCentre(terminal, "Use [shift] to switch between uppercase/" +
        "lowercase letters.", height - 4);
    writeCentre(terminal, "Press " + (char)27 + "/" + (char)26 +
        " to change the selected field's value.", height - 3);
    writeCentre(terminal, "Press [enter] to save the current object.",
        height - 2);
    writeCentre(terminal, "Press [esc] to go back without saving.",
        height - 1);

    terminal.repaint();
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    if (scrollList.getSelectedIndex() == 0 && name.length() < 30 &&
        (key.getKeyCode() >= KeyEvent.VK_A &&
        key.getKeyCode() <= KeyEvent.VK_Z ||
        key.getKeyCode() == KeyEvent.VK_SPACE ||
        key.getKeyCode() >= KeyEvent.VK_0 &&
        key.getKeyCode() <= KeyEvent.VK_9)) {
      if (key.getKeyCode() >= KeyEvent.VK_A &&
          key.getKeyCode() <= KeyEvent.VK_Z) {
        if (capsLock) {
          name += (char)key.getKeyCode();
        } else {
          name += (char)(key.getKeyCode() + 32);
        }
      } else {
        name += (char)(key.getKeyCode());
      }
      scrollList.updateSelectedEntry("Name: " + name);
      return this;
    }
    switch (key.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        mainScreen.setInSubScreen();
        return mainScreen;
      case KeyEvent.VK_BACK_SPACE:
        if (scrollList.getSelectedIndex() == 0 && name.length() > 0) {
          name = name.substring(0, name.length() - 1);
          scrollList.updateSelectedEntry("Name: " + name);
        }
        return this;
      case KeyEvent.VK_SHIFT:
        capsLock = !capsLock;
        return this;
      case KeyEvent.VK_UP:
        scrollList.scrollUp();
        return this;
      case KeyEvent.VK_DOWN:
        scrollList.scrollDown();
        return this;
      case KeyEvent.VK_ENTER:
        if (objectType == 0) {
          mainScreen.addForegroundTile(new CustomForegroundTile(name, glyph,
              Display.COLOURS[fgIndex]));
        } else {
          mainScreen.addBackgroundTile(new BackgroundTile(name, glyph,
              Display.COLOURS[fgIndex], Display.COLOURS[bgIndex]));
        }
        mainScreen.setInSubScreen();
        return mainScreen;
      case KeyEvent.VK_LEFT:
        if (scrollList.getSelectedIndex() == 1) {
          glyph = (char)Math.max(0, glyph - 1);
          scrollList.updateSelectedEntry("Glyph: " + glyph);
        } else if (scrollList.getSelectedIndex() == 2) {
          fgIndex = Math.max(0, fgIndex - 1);
          scrollList.updateSelectedEntry("Foreground Colour: " +
              Display.COLOUR_NAMES[fgIndex]);
        } else if (scrollList.getSelectedIndex() == 3) {
          bgIndex = Math.max(0, bgIndex - 1);
          scrollList.updateSelectedEntry("Background Colour: " +
              Display.COLOUR_NAMES[bgIndex]);
        }
        return this;
      case KeyEvent.VK_RIGHT:
        if (scrollList.getSelectedIndex() == 1) {
          glyph = (char)Math.min(255, glyph + 1);
          scrollList.updateSelectedEntry("Glyph: " + glyph);
        } else if (scrollList.getSelectedIndex() == 2) {
          fgIndex = Math.min(Display.NUM_OF_COLOURS - 1, fgIndex + 1);
          scrollList.updateSelectedEntry("Foreground Colour: " +
              Display.COLOUR_NAMES[fgIndex]);
        } else if (scrollList.getSelectedIndex() == 3) {
          bgIndex = Math.min(Display.NUM_OF_COLOURS - 1, bgIndex + 1);
          scrollList.updateSelectedEntry("Background Colour: " +
              Display.COLOUR_NAMES[bgIndex]);
        }
        return this;
      default:
        return this;
    }
  }
}
