import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener {
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

		if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE)
			GlobalScreen.unregisterNativeHook();
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {

	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		System.out.println("Mosue Clicked: " + e.getClickCount());
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		System.out.println("Mosue Pressed: " + e.getButton());
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		System.out.println("Mosue Released: " + e.getButton());
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		System.out.println("Mosue Moved: " + e.getX() + ", " + e.getY());
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		System.out.println("Mosue Dragged: " + e.getX() + ", " + e.getY());
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
		InputListener penis = new InputListener();
		GlobalScreen.getInstance().addNativeKeyListener(penis);
		GlobalScreen.getInstance().addNativeMouseListener(penis);
		GlobalScreen.getInstance().addNativeMouseMotionListener(penis);
	}
}
