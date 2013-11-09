
public class MathHelper {
	public static int clampInt(int val, int min, int max) {
		if (val < min)
			return min;
		return (val > max) ? max : val;
	}
}
