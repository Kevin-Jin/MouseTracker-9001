import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BaseFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	List<Point> points = new ArrayList<Point>();

	public BaseFrame() {
		super("Y-Hack 2013");
		setUndecorated(true);
		setLocation(0, 0);
		setBackground(new Color(0, 0, 0, 0));

		getContentPane().add(new InnerPannel());

		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void addPoint(Point p) {
		points.add(p);
	}
	
	private final class InnerPannel extends JPanel {
		private static final long serialVersionUID = 1L;

		public InnerPannel() {
			setBackground(new Color(255, 255, 255));
			setSize(500, 500);
			add(new JButton("Hello"));
		}

		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(0, 0, 0, 255 / 2));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(new Color(0, 255, 255));
			g.fillOval(50, 50, 100, 100);
			for (int i = 1; i < points.size(); ++i) {
				Point prev = points.get(i-1), cur = points.get(i);
				g.drawLine(prev.x, prev.y, cur.x, cur.y);
			}
			g.dispose();
		}
	}
}
