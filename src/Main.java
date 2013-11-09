import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class Main {
	public static void main(String[] args) {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		// Construct the example object and initialize native hook.
		InputListener listener = new InputListener();
		GlobalScreen.getInstance().addNativeKeyListener(listener);
		GlobalScreen.getInstance().addNativeMouseListener(listener);
		GlobalScreen.getInstance().addNativeMouseMotionListener(listener);
	}
}
