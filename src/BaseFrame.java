import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BaseFrame extends JFrame {
	private static final boolean ALWAYS_SHOW_IN_TASKBAR = true;

	public static final Dimension FULL_SCREEN_CANVAS = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Dimension HIDDEN_CANVAS = new Dimension(0, 0);

	private final EventPainter innerPanel;
	/**
	 * This Collection can be accessed only by the JNativeHook thread.
	 */
	private final Map<Integer, Event.KeyChange> pressedKeyEvents;
	private final ConcurrentNavigableMap<Long, Event> events;
	private final Set<Character> currentlyPressedKeys;

	private volatile long startTime;
	private volatile boolean playing;
	/**
	 * This variable can be accessed only by the JNativeHook thread.
	 */
	private Event.KeyChange lastPressedKeyEvent;

	public BaseFrame() {
		super("Y-Hack 2013");
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 255 / 2));
		setLocation(0, 0);
		if (ALWAYS_SHOW_IN_TASKBAR)
			setVisible(true);
		else
			setSize(FULL_SCREEN_CANVAS);
		hideCanvas();
		innerPanel = new EventPainter();
		getContentPane().add(innerPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pressedKeyEvents = new HashMap<Integer, Event.KeyChange>();
		events = new ConcurrentSkipListMap<Long, Event>();
		currentlyPressedKeys = Collections.synchronizedSet(new LinkedHashSet<Character>());

		startTime = System.currentTimeMillis();
		playing = false;
	}

	public void mouseMoved(Point p) {
		long time = System.currentTimeMillis() - startTime;
		events.put(Long.valueOf(time), new Event.MouseMoved(p));
	}

	public void keyEvent(int keycode, boolean pressed) {
		long time = System.currentTimeMillis() - startTime;
		Event.KeyChange ev;
		if (pressed) {
			ev = new Event.KeyChange(keycode, true, time, currentlyPressedKeys, null);
			pressedKeyEvents.put(Integer.valueOf(keycode), ev);
			lastPressedKeyEvent = ev;
		} else {
			Event.KeyChange pressedEvent = pressedKeyEvents.get(Integer.valueOf(keycode));
			ev = new Event.KeyChange(keycode, false, time, currentlyPressedKeys, pressedEvent);
			if (pressedEvent != null)
				pressedEvent.setReleasedEvent(ev);
		}
		events.put(Long.valueOf(time), ev);
	}

	public void keyEvent(char keyChar) {
		if (lastPressedKeyEvent != null)
			lastPressedKeyEvent.setKeyChar(keyChar);
	}

	public void mouseEvent(int x, int y, boolean left, boolean pressed) {
		long time = System.currentTimeMillis() - startTime;
		if (pressed)
			events.put(Long.valueOf(time), new Event.MousePressed(new Point(x, y), left));
		else
			events.put(Long.valueOf(time), new Event.MouseReleased(new Point(x, y), left));
	}

	public void startPlayback() {
		new Thread(new Runnable() {
			public void run() {
				startTime = System.currentTimeMillis();
				playing = true;
				showCanvas();
				while (playing) {
					repaint();
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				//paint an empty canvas
				repaint();
			}
		}).start();
	}

	public void pausePlayback() {
		playing = false;
	}

	public void endPlaybackAndReset() {
		currentlyPressedKeys.clear();
		events.clear();
		playing = false;

		startTime = System.currentTimeMillis();
	}

	public void hideCanvas() {
		if (ALWAYS_SHOW_IN_TASKBAR) {
			setSize(HIDDEN_CANVAS);
			setState(ICONIFIED);
		} else {
			setVisible(false);
		}
	}

	public void showCanvas() {
		if (ALWAYS_SHOW_IN_TASKBAR)
			setSize(FULL_SCREEN_CANVAS);
		else
			setVisible(true);
		//restore and focus the window
		setState(ICONIFIED);
		setState(NORMAL);
	}

	private class EventPainter extends JPanel {
		/**
		 * This variable can be accessed only by the Swing EDT.
		 */
		private boolean hide;

		public EventPainter() {
			setBackground(Color.white);
			hide = false;
		}

		@Override
		public void paintComponent(Graphics g) {
			/*Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			g2.setColor(new Color(0, 0, 0, 255 / 2));
			g2.fillRect(0, 0, getWidth(), getHeight());*/

			if (!playing) {
				if (hide) {
					hide = false;
					hideCanvas();
				} else {
					//go through another repaint before we call hideCanvas(). for some reason, this draw only takes affect after another repaint,
					//but when HIDDEN_CANVAS = (0, 0) or setVisible(false), repaint doesn't take effect until after the canvas is restored,
					//resulting in the restored canvas first displaying the final frame of the previous playback if hideCanvas is not called this way.
					hide = true;
					repaint();
				}
				return;
			}
			hide = false;

			long now = System.currentTimeMillis();
			Point p = null;
			for (Iterator<Map.Entry<Long, Event>> iter = events.headMap(Long.valueOf(System.currentTimeMillis() - startTime), true).entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Long, Event> entry = iter.next();
				if (!entry.getValue().draw((Graphics2D) g, Math.max(0, now - startTime - entry.getKey()), p))
					iter.remove();
				if (entry.getValue() instanceof Event.MouseEvent)
					p = ((Event.MouseEvent) entry.getValue()).p;
			}
		}
	}
}
