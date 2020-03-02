import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ExitListener implements KeyListener {

    public ExitListener() {

    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
            System.out.println("pressed");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
