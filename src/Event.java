import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public abstract class Event {
	public final long timestamp;

	public Event(long timestamp) {
		this.timestamp = timestamp;
	}

	public abstract boolean draw(Graphics2D g, long dT, Object... args);

	public static abstract class MouseEvent extends Event {
		public final Point p;
		
		public MouseEvent(long time, Point p) {
			super(time);
			this.p = p;
		}
	}

	public static class MouseMoved extends MouseEvent {
		public MouseMoved(long time, Point p) {
			super(time, p);
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			if (args[0] != null) {
				Point p2 = (Point)args[0];
				float r = dT / 1000.0f;
				if (r > 1)
					r = 1;
				g.setColor(new Color(1-r, 0, r));
				g.drawLine(p.x, p.y, p2.x, p2.y);
			}
			return dT < 1000;
		}
	}

	public static class MousePressed extends MouseEvent {
		public final boolean left; 

		public MousePressed(long time, Point p, boolean left) {
			super(time, p);
			this.left = left;
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			int size = (int) Math.min(dT / 30, 30);
			size = 30 - size;
			g.drawOval(p.x - size/2, p.y - size/2, size, size);
			return size > 0;
		}
	}

	public static class MouseReleased extends MouseEvent {
		public final boolean left; 

		public MouseReleased(long time, Point p, boolean left) {
			super(time, p);
			this.left = left;
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			return false;
		}
	}

	public static class KeyChange extends Event {
		public final int key;
		public final boolean pressed; //or release

		public KeyChange(long time, int key, boolean pressed) {
			super(time);
			this.key = key;
			this.pressed = pressed;
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			return true;
		}
	}
}
