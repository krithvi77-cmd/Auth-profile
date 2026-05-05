package test;

public class TestPreparationException extends Exception {

	private final boolean needsReconnect;

	public TestPreparationException(String message) {
		this(message, false);
	}

	public TestPreparationException(String message, boolean needsReconnect) {
		super(message);
		this.needsReconnect = needsReconnect;
	}

	public boolean isNeedsReconnect() {
		return needsReconnect;
	}
}
