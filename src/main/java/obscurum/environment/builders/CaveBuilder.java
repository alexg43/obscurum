package obscurum.environment.builders;

import java.awt.Point;
import java.lang.Math;
import obscurum.environment.Level;
import obscurum.environment.background.BackgroundTile;
import obscurum.environment.foreground.EmptyTile;
import obscurum.environment.foreground.ForegroundTile;

/**
 * This models a cave builder, which creates open levels with irregular rooms
 * and walls.
 * @author Alex Ghita
 */
public class CaveBuilder extends Builder {
  public CaveBuilder(int width, int height, ForegroundTile foregroundType,
      BackgroundTile backgroundType, boolean hasNext, boolean hasPrevious) {
    super(width, height, foregroundType, backgroundType, hasNext, hasPrevious);
  }

  public Level build() {
    return build(4, 3, 0.3, 20);
  }

  public Level build(int birthLimit, int deathLimit, double carveChance,
      int numberOfSteps) {
    boolean[][] board = new boolean[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        board[x][y] = Math.random() < carveChance ? true : false;
      }
    }

    for (int p = 0; p < numberOfSteps; p++) {
      boolean[][] newBoard = new boolean[width][height];

      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          int liveNeighbours = 0;

          for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
              int tx = x + i;
              int ty = y + j;

              if (i == 0 && j == 0) {
                continue;
              }
              else if (tx < 0 || ty < 0 || tx >= width || ty >= height ||
                  board[tx][ty]) {
                liveNeighbours++;
              }
            }
          }
          if (board[x][y]) {
            if (liveNeighbours < deathLimit) {
              newBoard[x][y] = false;
            } else {
              newBoard[x][y] = true;
            }
          } else {
            if (liveNeighbours > birthLimit) {
              newBoard[x][y] = true;
            } else {
              newBoard[x][y] = false;
            }
          }
        }
      }

      board = newBoard;
    }

    for (int x = 0; x < width; x++) {
      board[x][0] = board[x][height - 1] = true;
    }
    for (int y = 0; y < height; y++) {
      board[0][y] = board[width - 1][y] = true;
    }

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (board[x][y]) {
          level.setForegroundTile(x, y, foregroundType);
        } else {
          level.setForegroundTile(x, y, new EmptyTile());
        }
        level.setBackgroundTile(x, y, backgroundType);
      }
    }
    placeLadders();

    return level;
  }

  protected void placeLadders() {
    Point ladderLocation;
    if (hasNext) {
      ladderLocation = level.getRandomEmptyLocation();
      level.setBackgroundTile(ladderLocation, BackgroundTile.createDownwardDoorway(level.getBackgroundTile(ladderLocation)));
      level.setNextLocation(ladderLocation);
    }
    if (hasPrevious) {
      ladderLocation = level.getRandomEmptyLocation();
      level.setBackgroundTile(ladderLocation, BackgroundTile.createUpwardDoorway(level.getBackgroundTile(ladderLocation)));
      level.setPreviousLocation(ladderLocation);
    }
  }
}
