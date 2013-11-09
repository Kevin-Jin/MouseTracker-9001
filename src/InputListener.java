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
		} else {
			//frame.addKeyRelease(e.getKeyCode());
		}
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
				frame.startPlayback();
				frame.setVisible(true);
			} else {
				frame.setVisible(false);
			}

			System.out.println((isPlayingBack ? "START" : "STOP") + " PLAYING");
		} else {
			//frame.addKeyPress(e.getKeyCode());
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		if (e.getKeyChar() != NativeKeyEvent.CHAR_UNDEFINED) {
			frame.addKeyPress(e.getKeyChar());
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		switch (e.getButton()) {
			case 1:
				frame.addLeftMouseButtonPress();
				break;
			case 2:
				frame.addRightMouseButtonPress();
				break;
		}
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		switch (e.getButton()) {
			case 1:
				frame.addLeftMouseButtonRelease();
				break;
			case 2:
				frame.addRightMouseButtonRelease();
				break;
		}
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		int x = MathHelper.clampInt(e.getX(), 0, frame.getWidth() - 1);
		int y = MathHelper.clampInt(e.getY(), 0, frame.getHeight() - 1);
		if (isRecording) {
			frame.addPoint(new Point(x, y));
			frame.repaint();
		}
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		nativeMouseMoved(e);
	}
}
