package obscurum.placeholders;

import java.awt.Point;
import obscurum.creatures.Creature;
import obscurum.display.Display;
import obscurum.placeholders.NullLevel;

/**
 * This represents a "null creature".
 * @author Alex Ghita
 */
public class NullCreature extends Creature {
  /**
   * Class constructor specifying placeholder values.
   */
  public NullCreature() {
    super("Null Creature", ' ', Display.BLACK, new NullLevel(), new Point(),
        1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0);
  }

  @Override
  public void powerUp() {}

  @Override
  protected void generateInventory() {}
}
