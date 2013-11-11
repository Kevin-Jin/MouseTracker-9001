import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Set;

import org.jnativehook.keyboard.NativeKeyEvent;

public abstract class Event {
	public abstract boolean draw(Graphics2D g, long dT, Object... args);

	public static abstract class MouseEvent extends Event {
		public final Point p;
		
		public MouseEvent(Point p) {
			this.p = p;
		}
	}

	public static class MouseMoved extends MouseEvent {
		public MouseMoved(Point p) {
			super(p);
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			if (args[0] != null) {
				Point p2 = (Point)args[0];
				float b = dT / 1000.0f;
				if (b > 1)
					b = 1;
				g.setColor(new Color(1 - b, 0, b));
				g.drawLine(p.x, p.y, p2.x, p2.y);
			}
			return dT < 1000;
		}
	}

	public static class MousePressed extends MouseEvent {
		public final boolean left;

		public MousePressed(Point p, boolean left) {
			super(p);
			this.left = left;
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			int size = 30 - (int)Math.min(dT / 30, 30);
			if (left)
				g.drawOval(p.x - size/2, p.y - size/2, size, size);
			else
				g.drawRect(p.x - size/2, p.y - size/2, size, size);
			return size > 0;
		}
	}

	public static class MouseReleased extends MouseEvent {
		public final boolean left; 

		public MouseReleased(Point p, boolean left) {
			super(p);
			this.left = left;
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			return false;
		}
	}

	public static class KeyChange extends Event {
		private static final Font font = new Font("Arial", Font.PLAIN, 150);

		public final int key;
		public final boolean pressed; //or release

		private final long timestamp;
		private final Set<Character> currentlyPressedKeys;

		private volatile KeyChange complement;
		private volatile char keyChar;

		public KeyChange(int key, boolean pressed, long timestamp, Set<Character> currentlyPressedKeys, KeyChange complement) {
			this.key = key;
			this.pressed = pressed;
			this.timestamp = timestamp;
			this.currentlyPressedKeys = currentlyPressedKeys;
			this.complement = complement;
			this.keyChar = NativeKeyEvent.CHAR_UNDEFINED;
		}

		public void setKeyChar(char keyChar) {
			this.keyChar = keyChar;
		}

		private void drawString(Graphics g) {
			StringBuilder sb = new StringBuilder(currentlyPressedKeys.size());
			for (Character c : currentlyPressedKeys)
				sb.append(c);
			String str = sb.toString();
			g.setFont(font);
			g.setColor(new Color(0, 0, 0, 255));
			g.drawString(str, (BaseFrame.FULL_SCREEN_CANVAS.width - g.getFontMetrics().stringWidth(str)) / 2, (BaseFrame.FULL_SCREEN_CANVAS.height) / 2 + g.getFontMetrics().getMaxDescent());
		}

		@Override
		public boolean draw(Graphics2D g, long dT, Object... args) {
			//if complement is null when pressed, then there was no corresponding release event, so keep drawing this character
			boolean draw = pressed && (complement == null || timestamp + dT < complement.timestamp);
			if (complement != null)
				complement.keyChar = keyChar;
			if (pressed && keyChar != NativeKeyEvent.CHAR_UNDEFINED) {
				currentlyPressedKeys.add(Character.valueOf(keyChar));
				drawString(g);
			} else {
				currentlyPressedKeys.remove(Character.valueOf(keyChar));
				drawString(g);
			}
			return draw;
		}

		public void setReleasedEvent(KeyChange releasedKeyEvent) {
			this.complement = releasedKeyEvent;
		}
	}
}
