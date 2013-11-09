import java.awt.Point;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener {
	private boolean isRecording;
	public final BaseFrame frame;

	public InputListener() {
		frame = new BaseFrame();
		frame.setVisible(true);
		isRecording = true;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
			frame.dispose();
			GlobalScreen.unregisterNativeHook();
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_F9) {
			isRecording = !isRecording;
			if (isRecording)
				frame.clearAllAndMakeVisible();
			else
				frame.setVisible(false);

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
		int x = MathHelper.clampInt(e.getX(), 0, frame.getWidth() - 1);
		int y = MathHelper.clampInt(e.getY(), 0, frame.getHeight() - 1);
		if (isRecording) {
			frame.addPoint(new Point(x, y));
			frame.repaint();
		}
		System.out.println("Mouse Moved: " + e.getX() + ", " + e.getY());
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		System.out.println("Mouse Dragged: " + e.getX() + ", " + e.getY());
	}
}
