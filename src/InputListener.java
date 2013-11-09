import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener {
	private final JFrame frame;
	private boolean isRecording;

	public InputListener() {
		frame = new JFrame("Y-Hack 2013");

		frame.setUndecorated(true);
		frame.setLocation(0, 0);
		frame.setBackground(new Color(0, 0, 0, 0));

		frame.getContentPane().add(new JPanel() {
			private static final long serialVersionUID = 1L;

			{
				JPanel panel = new JPanel();
				panel.setBackground(new Color(255, 255, 255));
				panel.setSize(500, 500);
				panel.add(new JButton("Hello"));
				add(panel);
			}

			@Override
			public void paintComponent(Graphics g) {
				g.setColor(new Color(0, 0, 0, 255 / 2));
				g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				g.setColor(new Color(0, 255, 255));
				g.fillOval(50, 50, 100, 100);
				g.dispose();
			}
		});

		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
			GlobalScreen.unregisterNativeHook();
			frame.dispose();
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_F9) {
			isRecording = !isRecording;
			System.out.println((isRecording ? "START" : "STOP") + " RECORDING");
		}

		System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {

	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		System.out.println("Mouse Clicked: " + e.getClickCount());
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		System.out.println("Mouse Pressed: " + e.getButton());
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		System.out.println("Mouse Released: " + e.getButton());
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		System.out.println("Mouse Moved: " + e.getX() + ", " + e.getY());
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		System.out.println("Mouse Dragged: " + e.getX() + ", " + e.getY());
	}

	public static void main(String[] args) {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		// Construct the example object and initialze native hook.
		InputListener listener = new InputListener();
		GlobalScreen.getInstance().addNativeKeyListener(listener);
		GlobalScreen.getInstance().addNativeMouseListener(listener);
		GlobalScreen.getInstance().addNativeMouseMotionListener(listener);
		listener.frame.setVisible(true);
	}
}
