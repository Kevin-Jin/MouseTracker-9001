import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BaseFrame extends JFrame {
	private enum EventType { MOUSE_POSITION, LEFT_MOUSE_BUTTON, RIGHT_MOUSE_BUTTON, KEY_PRESS, KEY_RELEASE, NONE }

	private static final long serialVersionUID = 1L;

	private final InnerPanel innerPanel;
	private volatile long startTime;
	private BufferedImage mouseMovement;

	private final List<WithTime<Point>> mousePositions = new LinkedList<WithTime<Point>>();
	private final List<WithTime<Boolean>> leftMouseButtonState = new LinkedList<WithTime<Boolean>>();
	private final List<WithTime<Boolean>> rightMouseButtonState = new LinkedList<WithTime<Boolean>>();
	private final List<WithTime<Integer>> keysPressed = new LinkedList<WithTime<Integer>>();
	private final List<WithTime<Integer>> keysReleased = new LinkedList<WithTime<Integer>>();

	private Point lastMousePosition;
	private int mouseCursorSize;
	private boolean leftMouseButtonHeld, rightMouseButtonHeld;
	private Set<Integer> lastKeysPressed = new HashSet<Integer>();

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

		mouseCursorSize = 0;
		leftMouseButtonHeld = rightMouseButtonHeld = false;

		mouseMovement = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

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

	public void addLeftMouseButtonPress() {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				leftMouseButtonState.add(new WithTime<Boolean>(Boolean.TRUE, time));
			}
		});
	}

	public void addLeftMouseButtonRelease() {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				leftMouseButtonState.add(new WithTime<Boolean>(Boolean.FALSE, time));
			}
		});
	}

	public void addRightMouseButtonPress() {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rightMouseButtonState.add(new WithTime<Boolean>(Boolean.TRUE, time));
			}
		});
	}

	public void addRightMouseButtonRelease() {
		final long time = System.currentTimeMillis() - startTime;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rightMouseButtonState.add(new WithTime<Boolean>(Boolean.FALSE, time));
			}
		});
	}

	public void startPlayback() {
		startTime = System.currentTimeMillis();
		new Thread(new Runnable() {
			private long getEarliest() {
				long earliest = Long.MAX_VALUE;
				if (leftMouseButtonHeld || rightMouseButtonHeld)
					earliest = System.currentTimeMillis() - startTime + 16;
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
				return EventType.NONE;
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
							if (lastMousePosition != null) {
								Graphics bufG = mouseMovement.getGraphics();
								Point cur = mousePositions.get(0).value;
								bufG.drawLine(lastMousePosition.x, lastMousePosition.y, cur.x, cur.y);
								repaint();
								bufG.dispose();
							}
							lastMousePosition = mousePositions.remove(0).value;
							break;
						case LEFT_MOUSE_BUTTON:
							leftMouseButtonHeld = leftMouseButtonState.remove(0).value.booleanValue();
							break;
						case RIGHT_MOUSE_BUTTON:
							rightMouseButtonHeld = rightMouseButtonState.remove(0).value.booleanValue();
							break;
						case KEY_PRESS:
							break;
						case KEY_RELEASE:
							break;
						case NONE:
							break;
					}
					if (leftMouseButtonHeld) {
						mouseCursorSize = Math.min(mouseCursorSize + Math.max(17, (int) sleepTime), 1000);
						repaint();
					}
					if (rightMouseButtonHeld) {
						mouseCursorSize = Math.max(mouseCursorSize - Math.max(17, (int) sleepTime), 0);
						repaint();
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
				leftMouseButtonHeld = rightMouseButtonHeld = false;
				mouseCursorSize = 0;

				//clear the playback canvas
				Graphics2D g2 = (Graphics2D) mouseMovement.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

				startTime = System.currentTimeMillis();
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

			g.drawImage(mouseMovement, 0, 0, null);
			if (mouseCursorSize != 0) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.black);
				if (lastMousePosition != null)
					g2.fillRect(lastMousePosition.x - mouseCursorSize / 50 / 2, lastMousePosition.y - mouseCursorSize / 50 / 2, mouseCursorSize / 50, mouseCursorSize / 50);
				else
					g2.fillRect(getWidth() - mouseCursorSize / 50 / 2, getHeight() - mouseCursorSize / 50 / 2, mouseCursorSize / 50, mouseCursorSize / 50);
			}

			g.dispose();
		}
	}
}
