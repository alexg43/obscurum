package obscurum;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import obscurum.display.asciiPanel.AsciiPanel;
import obscurum.environment.foreground.*;
import obscurum.screens.MainMenuScreen;
import obscurum.screens.StartScreen;
import obscurum.screens.Screen;

public class GameMain extends JFrame implements KeyListener {
    public static final int SCREEN_WIDTH = 110;
    public static final int SCREEN_HEIGHT = 29;
    public static final int NUM_OF_GLYPHS = 256;
    public static List<ForegroundTile> foregroundTiles = Arrays.asList(new Stone(), new Wall());

    private final AsciiPanel terminal;
    private Screen screen;

    public GameMain() {
        super("Obscurum");
        terminal = new AsciiPanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        add(terminal);
        pack();
        setResizable(false);

        screen = new StartScreen();
        addKeyListener(this);
        repaint();
    }

    public void repaint() {
        terminal.clear();
        screen.displayOutput(terminal);
        super.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_Q && screen instanceof MainMenuScreen) {
            this.dispose();
        }
        screen = screen.respondToUserInput(e);
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        GameMain app = new GameMain();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }
}
