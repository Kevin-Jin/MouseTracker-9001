import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BaseFrame extends JFrame {
	private enum EventType { MOUSE_POSITION, LEFT_MOUSE_BUTTON, RIGHT_MOUSE_BUTTON, KEY_PRESS, KEY_RELEASE }

	private static final long serialVersionUID = 1L;

	private final InnerPanel innerPanel;
	private volatile long startTime;
	private BufferedImage bufferImage;

	private final List<WithTime<Point>> mousePositions = new LinkedList<WithTime<Point>>();
	private final List<WithTime<Boolean>> leftMouseButtonState = new LinkedList<WithTime<Boolean>>();
	private final List<WithTime<Boolean>> rightMouseButtonState = new LinkedList<WithTime<Boolean>>();
	private final List<WithTime<Integer>> keysPressed = new LinkedList<WithTime<Integer>>();
	private final List<WithTime<Integer>> keysReleased = new LinkedList<WithTime<Integer>>();

	public BaseFrame() {
		super("Y-Hack 2013");
		setUndecorated(true);
		setLocation(0, 0);
		setBackground(new Color(0, 0, 0, 0));
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		innerPanel = new InnerPanel();
		getContentPane().add(innerPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		bufferImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

		startTime = System.currentTimeMillis();
	}

	public void addPoint(final Point p) {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mousePositions.add(new WithTime<Point>(p, time));
			}
		});
	}

	public void startPlayback() {
		startTime = System.currentTimeMillis();
		new Thread(new Runnable() {
			private long getEarliest() {
				long earliest = Long.MAX_VALUE;
				if (!mousePositions.isEmpty())
					if (mousePositions.get(0).timestamp < earliest)
						earliest = mousePositions.get(0).timestamp;
				if (!leftMouseButtonState.isEmpty())
					if (leftMouseButtonState.get(0).timestamp < earliest)
						earliest = leftMouseButtonState.get(0).timestamp;
				if (!rightMouseButtonState.isEmpty())
					if (rightMouseButtonState.get(0).timestamp < earliest)
						earliest = rightMouseButtonState.get(0).timestamp;
				if (!keysPressed.isEmpty())
					if (keysPressed.get(0).timestamp < earliest)
						earliest = keysPressed.get(0).timestamp;
				if (!keysReleased.isEmpty())
					if (keysReleased.get(0).timestamp < earliest)
						earliest = keysReleased.get(0).timestamp;
				if (earliest != Long.MAX_VALUE)
					return earliest;
				return -1;
			}

			private EventType getEventType(long time) {
				if (!mousePositions.isEmpty())
					if (mousePositions.get(0).timestamp == time)
						return EventType.MOUSE_POSITION;
				if (!leftMouseButtonState.isEmpty())
					if (leftMouseButtonState.get(0).timestamp == time)
						return EventType.LEFT_MOUSE_BUTTON;
				if (!rightMouseButtonState.isEmpty())
					if (rightMouseButtonState.get(0).timestamp == time)
						return EventType.RIGHT_MOUSE_BUTTON;
				if (!keysPressed.isEmpty())
					if (keysPressed.get(0).timestamp == time)
						return EventType.KEY_PRESS;
				if (!keysReleased.isEmpty())
					if (keysReleased.get(0).timestamp == time)
						return EventType.KEY_RELEASE;
				return null;
			}

			@Override
			public void run() {
				long earliest;
				while ((earliest = getEarliest()) != -1) {
					long elapsed = System.currentTimeMillis() - startTime;
					long sleepTime = earliest - elapsed;
					try {
						Thread.sleep(Math.max(0, sleepTime));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					switch (getEventType(earliest)) {
						case MOUSE_POSITION:
							if (mousePositions.size() > 1) {
								Graphics bufG = bufferImage.getGraphics();
								Point prev = mousePositions.get(0).value, cur = mousePositions.get(1).value;
								bufG.drawLine(prev.x, prev.y, cur.x, cur.y);
								repaint();
								bufG.dispose();
							}
							mousePositions.remove(0);
							break;
						case LEFT_MOUSE_BUTTON:
							break;
						case RIGHT_MOUSE_BUTTON:
							break;
						case KEY_PRESS:
							break;
						case KEY_RELEASE:
							break;
					}
				}
			}
		}).start();
	}

	public void clearAll() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mousePositions.clear();
				leftMouseButtonState.clear();
				rightMouseButtonState.clear();
				keysPressed.clear();
				keysReleased.clear();
				startTime = System.currentTimeMillis();
				bufferImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			}
		});
	}

	private final class InnerPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public InnerPanel() {
			setBackground(new Color(255, 255, 255));
		}

		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(0, 0, 0, 255 / 2));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(bufferImage, 0, 0, null);
			g.dispose();
		}
	}
}
