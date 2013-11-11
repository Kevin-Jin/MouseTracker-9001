import java.awt.Point;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener {
	private final BaseFrame frame;

	/**
	 * This variable can be accessed only by the JNativeHook thread.
	 */
	private boolean isRecording;
	/**
	 * This variable can be accessed only by the JNativeHook thread.
	 */
	private boolean isPlayingBack;

	public InputListener() {
		frame = new BaseFrame();
		isRecording = false;
		isPlayingBack = false;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
			frame.endPlaybackAndReset();
			frame.dispose();
			GlobalScreen.unregisterNativeHook();
		} else if (e.getKeyCode() != NativeKeyEvent.VK_F9 && e.getKeyCode() != NativeKeyEvent.VK_F8 && isRecording) {
			frame.keyEvent(e.getKeyCode(), true);
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VK_F9) {
			isPlayingBack = false;
			isRecording = !isRecording;
			if (isRecording)
				frame.endPlaybackAndReset();

			System.out.println((isRecording ? "START" : "STOP") + " RECORDING");
		} else if (e.getKeyCode() == NativeKeyEvent.VK_F8) {
			isRecording = false;
			isPlayingBack = !isPlayingBack;
			if (isPlayingBack)
				frame.startPlayback();
			else
				frame.pausePlayback();

			System.out.println((isPlayingBack ? "START" : "STOP") + " PLAYING");
		} else if (isRecording && e.getKeyCode() != NativeKeyEvent.VK_ESCAPE) {
			frame.keyEvent(e.getKeyCode(), false);
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		frame.keyEvent(e.getKeyChar());
	}

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
			int x = MathHelper.clampInt(e.getX(), 0, BaseFrame.FULL_SCREEN_CANVAS.width - 1);
			int y = MathHelper.clampInt(e.getY(), 0, BaseFrame.FULL_SCREEN_CANVAS.height - 1);
			frame.mouseMoved(new Point(x, y));
		}
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		nativeMouseMoved(e);
	}
}
