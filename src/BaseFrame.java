import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BaseFrame extends JFrame {
	private final InnerPanel innerPanel;
	private volatile long startTime;

	private final List<Event> showing = new ArrayList<Event>();
	private final List<Event> events = new ArrayList<Event>();

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
				events.add(new Event.MouseMoved(time, p));
			}
		});
	}

	public void keyEvent(final int keycode, final boolean pressed) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				events.add(new Event.KeyChange(time, keycode, pressed));
			}
		});
	}

	public void mouseEvent(final int x, final int y, final boolean left, final boolean pressed) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (pressed)
					events.add(new Event.MousePressed(time, new Point(x, y), left));
				else
					events.add(new Event.MouseReleased(time, new Point(x, y), left));
			}
		});
	}

	public void startPlayback() {
		startTime = System.currentTimeMillis();
		new Thread(new Runnable() {
			public void run() {
				while(!events.isEmpty()) {
					long cur = events.get(0).timestamp;
					long elapsed = System.currentTimeMillis() - startTime;
					try {
						Thread.sleep(Math.max(0, cur-elapsed));
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
					synchronized(showing) {
						showing.add(events.remove(0));
					}
					repaint();
				}
			}
		}).start();
	}

	public void clearAll() {
		startTime = System.currentTimeMillis();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized(showing) 
					events.clear();
					showing.clear();
				}
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
			synchronized(showing) {
				for (Iterator<Event> it = showing.iterator(); it.hasNext(); ) {
					Event event = it.next();
					if (!event.draw((Graphics2D) g, now - startTime - event.timestamp, p))
						it.remove();
					if (event instanceof Event.MouseEvent)
						p = ((Event.MouseEvent) event).p;
				}
			}
			g.dispose();
		}
	}
}
