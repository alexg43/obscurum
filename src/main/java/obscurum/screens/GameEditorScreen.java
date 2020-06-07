package obscurum.screens;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import obscurum.GameMain;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.display.Display;
import obscurum.display.ListEntry;
import obscurum.display.ScrollList;
import obscurum.environment.Tile;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.background.CustomBackgroundTile;
import obscurum.environment.foreground.CustomForegroundTile;

/**
 * This models the game editor screen, where players can view, add, and delete
 * custom tiles.
 * @author Alex Ghita
 */
public class GameEditorScreen extends Screen {
  private static final int CURRENT_LIST_TL_X = 0;
  private static final int CURRENT_LIST_TL_Y = 0;
  private static final int CURRENT_LIST_WIDTH = 25;
  private static final int CURRENT_LIST_HEIGHT = GameMain.SCREEN_HEIGHT;
  private static final int OBJECT_DATA_TL_X = CURRENT_LIST_TL_X +
      CURRENT_LIST_WIDTH - 1;
  private static final int OBJECT_DATA_TL_Y = 0;
  private static final int OBJECT_DATA_WIDTH = GameMain.SCREEN_WIDTH -
      CURRENT_LIST_WIDTH + 1;
  private static final int OBJECT_DATA_HEIGHT = GameMain.SCREEN_HEIGHT;
  private static final String[] listNames = {"Foreground Tiles",
      "Background Tiles"};
  private ArrayList<CustomForegroundTile> foregroundTiles;
  private ArrayList<CustomBackgroundTile> backgroundTiles;
  private ArrayList<Integer> indicesToRemove;
  private ScrollList scrollList;
  private int selectedList; // 0 = foreground, 1 = background;
  private int previousList;
  private boolean inSubScreen;
  private Screen subScreen;

  public GameEditorScreen() {
    super();
    foregroundTiles = new ArrayList<CustomForegroundTile>();
    backgroundTiles = new ArrayList<CustomBackgroundTile>();
    selectedList = previousList = 0;

    ArrayList<String[]> input = readCSV("custom_tiles.csv");
    for (String[] line : input) {
      String name = line[0];
      char glyph = (char)Integer.parseInt(line[1]);
      Color fg = new Color(Integer.parseInt(line[2]),
          Integer.parseInt(line[3]), Integer.parseInt(line[4]));
      if (line.length == 5) {
        foregroundTiles.add(new CustomForegroundTile(name, glyph, fg));
      } else {
        Color bg = new Color(Integer.parseInt(line[5]),
            Integer.parseInt(line[6]), Integer.parseInt(line[7]));
        backgroundTiles.add(new CustomBackgroundTile(name, glyph, fg, bg));
      }
    }
    inSubScreen = false;
    subScreen = null;
    indicesToRemove = new ArrayList<Integer>();
    makeScrollList();
  }

  public void addForegroundTile(CustomForegroundTile f) {
    foregroundTiles.add(f);
    scrollList.getEntries().add(new ListEntry(f.getName(),
        scrollList.getEntries().size()));
  }

  public void addBackgroundTile(CustomBackgroundTile b) {
    backgroundTiles.add(b);
    scrollList.getEntries().add(new ListEntry(b.getName(),
        scrollList.getEntries().size()));
  }

  private void printCurrentList(AsciiPanel terminal) {
    int midX = (CURRENT_LIST_TL_X + CURRENT_LIST_WIDTH) / 2;
    String title = selectedList == 0 ? listNames[selectedList] + " >" :
        selectedList == listNames.length - 1 ? "< " + listNames[selectedList] :
        "< " + listNames[selectedList] + " >";
    String help = "Press [F1] for help.";

    drawBorders(terminal, new Point(CURRENT_LIST_TL_X, CURRENT_LIST_TL_Y),
        CURRENT_LIST_WIDTH, CURRENT_LIST_HEIGHT, false,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(title, midX - title.length() / 2, CURRENT_LIST_TL_Y + 1);
    writeHorizontalLine(terminal, CURRENT_LIST_TL_X, CURRENT_LIST_WIDTH - 1,
        CURRENT_LIST_TL_Y + 2);
    if (scrollList.getEntries().size() > 0) {
      scrollList.printList(terminal, CURRENT_LIST_TL_X + 1,
          CURRENT_LIST_TL_Y + 3, CURRENT_LIST_WIDTH - 2);
    }
    writeHorizontalLine(terminal, CURRENT_LIST_TL_X, CURRENT_LIST_WIDTH - 1,
        CURRENT_LIST_TL_Y + CURRENT_LIST_HEIGHT - 3);
    terminal.write(help, midX - help.length() / 2,
        CURRENT_LIST_TL_Y + CURRENT_LIST_HEIGHT - 2);
  }

  private void printObjectData(AsciiPanel terminal) {
    drawBorders(terminal, new Point(OBJECT_DATA_TL_X, OBJECT_DATA_TL_Y),
        OBJECT_DATA_WIDTH, OBJECT_DATA_HEIGHT, false,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    if (scrollList.getEntries().size() == 0) {
      return;
    }
    String name;
    String foregroundColour;
    String backgroundColour;
    String glyph;
    Tile tile = selectedList == 0 ?
        foregroundTiles.get(scrollList.getSelectedIndex()) :
        backgroundTiles.get(scrollList.getSelectedIndex());
    int currentPrintLine = OBJECT_DATA_TL_Y + 1;

    name = "Name: " + tile.getName() + ".";
    foregroundColour = "Foreground Colour: ";
    for (int i = 0; i < Display.NUM_OF_COLOURS; i++) {
      if (Display.COLOURS[i].equals(tile.getGlyphColour())) {
        foregroundColour += Display.COLOUR_NAMES[i] + ".";
        break;
      }
    }
    glyph = "Glyph: " + tile.getGlyph() + ".";

    terminal.write(name, OBJECT_DATA_TL_X + 1, currentPrintLine++);
    terminal.write(glyph, OBJECT_DATA_TL_X + 1, currentPrintLine++);
    terminal.write(foregroundColour, OBJECT_DATA_TL_X + 1, currentPrintLine++);
    if (selectedList == 1) {
      backgroundColour = "Background Colour: ";
      for (int i = 0; i < Display.NUM_OF_COLOURS; i++) {
        if (Display.COLOURS[i].equals(((BackgroundTile)tile).
            getBackgroundColour())) {
          backgroundColour += Display.COLOUR_NAMES[i] + ".";
          break;
        }
      }
      terminal.write(backgroundColour, OBJECT_DATA_TL_X + 1,
          currentPrintLine++);
    }
    terminal.write("In-game Display: ", OBJECT_DATA_TL_X + 1, currentPrintLine);
    if (selectedList == 0) {
      terminal.write(Character.toString(tile.getGlyph()), OBJECT_DATA_TL_X + 18,
          currentPrintLine, tile.getGlyphColour());
    } else {
      terminal.write(Character.toString(tile.getGlyph()), OBJECT_DATA_TL_X + 18,
          currentPrintLine, tile.getGlyphColour(),
          ((BackgroundTile)tile).getBackgroundColour());
    }
  }

  private void connectFrames(AsciiPanel terminal) {
    terminal.write(Display.DH_DB_INTERSECT, OBJECT_DATA_TL_X, OBJECT_DATA_TL_Y,
        Display.FG_WINDOW_FRAME, Display.BG_WINDOW_FRAME);
    terminal.write(Display.DH_DT_INTERSECT, OBJECT_DATA_TL_X,
        OBJECT_DATA_HEIGHT - 1, Display.FG_WINDOW_FRAME,
        Display.BG_WINDOW_FRAME);
  }

  public void setInSubScreen() {
    inSubScreen = !inSubScreen;
  }

  @Override
  public void displayOutput(AsciiPanel terminal) {
    printObjectData(terminal);
    printCurrentList(terminal);
    connectFrames(terminal);

    if (inSubScreen) {
      subScreen.displayOutput(terminal);
    }
  }

  private void removeSelectedEntry() {
    if (scrollList.getEntries().size() == 0) {
      return;
    }
    indicesToRemove.add(scrollList.getSelectedIndex());
    scrollList.removeSelectedEntry();
  }

  @Override
  public Screen respondToUserInput(KeyEvent key) {
    if (inSubScreen) {
      subScreen = subScreen.respondToUserInput(key);
      return this;
    }

    switch (key.getKeyCode()) {
      case KeyEvent.VK_F1:
        return new HelpScreen(HelpScreen.EDITOR_HELP);
      case KeyEvent.VK_ESCAPE:
        Collections.sort(indicesToRemove);
        for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
          if (selectedList == 0) {
            foregroundTiles.remove((int)indicesToRemove.get(i));
          } else {
            backgroundTiles.remove((int)indicesToRemove.get(i));
          }
        }
        indicesToRemove.clear();
        overwriteFiles();
        return new MainMenuScreen();
      case KeyEvent.VK_E:
        subScreen = new EditorNewItemScreen(this, selectedList);
        setInSubScreen();
        return this;
      case KeyEvent.VK_X:
        removeSelectedEntry();
        return this;
      case KeyEvent.VK_UP:
        scrollList.scrollUp();
        return this;
      case KeyEvent.VK_DOWN:
        scrollList.scrollDown();
        return this;
      case KeyEvent.VK_LEFT:
        if (selectedList > 0) {
          previousList = selectedList;
          selectedList = selectedList - 1;
          makeScrollList();
        }
        return this;
      case KeyEvent.VK_RIGHT:
        if (selectedList < listNames.length - 1) {
          previousList = selectedList;
          selectedList = selectedList + 1;
          makeScrollList();
        }
        return this;
      default:
        return this;
    }
  }

  private void overwriteFiles() {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(
          "src/main/resources/custom_tiles.csv"));
      ArrayList<Object> customObjects = new ArrayList<Object>();
      customObjects.addAll(foregroundTiles);
      customObjects.addAll(backgroundTiles);
      for (Object o : customObjects) {
        if (o instanceof CustomForegroundTile) {
          CustomForegroundTile tile = (CustomForegroundTile)o;
          Color fg = tile.getGlyphColour();
          writer.append(tile.getName() + "," + (int)tile.getGlyph() + "," +
              fg.getRed() + "," + fg.getGreen() + "," + fg.getBlue() + "\n");
        } else if (o instanceof CustomBackgroundTile) {
          CustomBackgroundTile tile = (CustomBackgroundTile)o;
          Color fg = tile.getGlyphColour();
          Color bg = tile.getBackgroundColour();
          writer.append(tile.getName() + "," + (int)tile.getGlyph() + "," +
              fg.getRed() + "," + fg.getGreen() + "," + fg.getBlue() + "," +
              bg.getRed() + "," + bg.getGreen() + "," + bg.getBlue() + "\n");
        }
      }
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void makeScrollList() {
    ArrayList<ListEntry> list = new ArrayList<ListEntry>();
    ArrayList<Tile> tiles = new ArrayList<Tile>();

    Collections.sort(indicesToRemove);
    for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
      if (previousList == 0) {
        foregroundTiles.remove((int)indicesToRemove.get(i));
      } else {
        backgroundTiles.remove((int)indicesToRemove.get(i));
      }
    }
    indicesToRemove.clear();

    if (selectedList == 0) {
      tiles.addAll(foregroundTiles);
    } else {
      tiles.addAll(backgroundTiles);
    }

    for (int i = 0; i < tiles.size(); i++) {
      list.add(new ListEntry(tiles.get(i).getName(), i));
    }

    scrollList = new ScrollList(list, CURRENT_LIST_HEIGHT - 6, 20);
  }
}
