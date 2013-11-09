public class MathHelper {
	public static int clampInt(int val, int min, int max) {
		return Math.min(Math.max(val, min), max);
	}
}
