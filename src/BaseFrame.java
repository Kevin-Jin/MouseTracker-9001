import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BaseFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final List<WithTime<Point>> mousePositions = new ArrayList<WithTime<Point>>();
	private final InnerPanel innerPanel;
	private BufferedImage bufferImage;

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
	}

	public void addPoint(final Point p) {
		final long now = System.currentTimeMillis();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mousePositions.add(new WithTime<Point>(p, now));
				Graphics bufG = bufferImage.getGraphics();
				for (int i = 1; i < mousePositions.size(); i++) {
					Point prev = mousePositions.get(i-1).value, cur = mousePositions.get(i).value;
					bufG.drawLine(prev.x, prev.y, cur.x, cur.y);
				}
				bufG.dispose();
			}
		});
	}

	public void clearAllAndMakeVisible() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mousePositions.clear();
				bufferImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				innerPanel.paintComponent(innerPanel.getGraphics());
				setVisible(true);
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
			g.setColor(new Color(0, 255, 255));
			g.fillOval(50, 50, 100, 100);
			g.drawImage(bufferImage, 0, 0, null);
			g.dispose();
		}
	}
}
