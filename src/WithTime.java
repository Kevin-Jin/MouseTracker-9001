public class WithTime<T> {
	public final T value;
	public final long timestamp;

	public WithTime(T value, long timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}
}
