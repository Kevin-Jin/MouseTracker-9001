import java.awt.Point;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener {
	private boolean isRecording, isPlayingBack;
	public final BaseFrame frame;

	public InputListener() {
		frame = new BaseFrame();
		isRecording = false;
		isPlayingBack = false;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
			frame.dispose();
			GlobalScreen.unregisterNativeHook();
		}
		else if (isRecording) 
			frame.keyEvent(e.getKeyCode(), true);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_F9) {
			isPlayingBack = false;
			frame.setVisible(false);
			isRecording = !isRecording;
			if (isRecording)
				frame.clearAll();

			System.out.println((isRecording ? "START" : "STOP") + " RECORDING");
		} else if (e.getKeyCode() == NativeKeyEvent.VK_F8) {
			isRecording = false;
			isPlayingBack = !isPlayingBack;
			if (isPlayingBack) {
				frame.setVisible(true);
				frame.startPlayback();
			} else {
				frame.setVisible(false);
			}

			System.out.println((isPlayingBack ? "START" : "STOP") + " PLAYING");
		}
		else if (isRecording) 
			frame.keyEvent(e.getKeyCode(), false);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) { }

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) { }

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		if (isRecording)
			frame.mouseEvent(e.getX(), e.getY(), e.getButton() == NativeMouseEvent.BUTTON1, true);
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		if (isRecording)
			frame.mouseEvent(e.getX(), e.getY(), e.getButton() == NativeMouseEvent.BUTTON1, false);
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		if (isRecording) {
			int x = MathHelper.clampInt(e.getX(), 0, frame.getWidth() - 1);
			int y = MathHelper.clampInt(e.getY(), 0, frame.getHeight() - 1);
			frame.mouseMoved(new Point(x, y));
		}
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		nativeMouseMoved(e);
	}
}
