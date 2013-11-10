import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class BaseFrame extends JFrame {
	private final InnerPanel innerPanel;
	private volatile long startTime;
	private volatile boolean playing;

	private final Set<Integer> pressedKeys = new HashSet<Integer>();
	private final ConcurrentNavigableMap<Long, Event> events = new ConcurrentSkipListMap<Long, Event>();

	public BaseFrame() {
		super("Y-Hack 2013");
		setUndecorated(true);
		setLocation(0, 0);
		setBackground(new Color(0, 0, 0, 0));
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		innerPanel = new InnerPanel();
		getContentPane().add(innerPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		startTime = System.currentTimeMillis();
	}

	public void mouseMoved(final Point p) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				events.put(Long.valueOf(time), new Event.MouseMoved(p));
			}
		});
	}

	public void keyEvent(final int keycode, final boolean pressed) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (pressed)
					pressedKeys.add(Integer.valueOf(keycode));
				else
					pressedKeys.remove(Integer.valueOf(keycode));
				events.put(Long.valueOf(time), new Event.KeyChange(keycode, pressed, pressedKeys));				
			}
		});
	}

	public void mouseEvent(final int x, final int y, final boolean left, final boolean pressed) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (pressed)
					events.put(Long.valueOf(time), new Event.MousePressed(new Point(x, y), left));
				else
					events.put(Long.valueOf(time), new Event.MouseReleased(new Point(x, y), left));
			}
		});
	}

	public void startPlayback() {
		new Thread(new Runnable() {
			public void run() {
				startTime = System.currentTimeMillis();
				playing = true;
				while (!events.isEmpty()) {
					repaint();
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				playing = false;
			}
		}).start();
	}

	public void clearAll() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				//clear the playback canvas
				Graphics2D g2 = (Graphics2D) innerPanel.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

				events.clear();
				startTime = System.currentTimeMillis();
			}
		});
	}

	private final class InnerPanel extends JPanel {
		public InnerPanel() {
			setBackground(new Color(255, 255, 255));
		}

		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(0, 0, 0, 255 / 2));
			g.fillRect(0, 0, getWidth(), getHeight());
			long now = System.currentTimeMillis();
			Point p = null;
			if(!playing)
				return;
			for (Iterator<Map.Entry<Long, Event>> iter = events.headMap(Long.valueOf(System.currentTimeMillis() - startTime), true).entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Long, Event> entry = iter.next();
				if (!entry.getValue().draw((Graphics2D) g, Math.max(0, now - startTime - entry.getKey()), p))
					iter.remove();
				if (entry.getValue() instanceof Event.MouseEvent)
					p = ((Event.MouseEvent) entry.getValue()).p;
			}
			g.dispose();
		}
	}
}
