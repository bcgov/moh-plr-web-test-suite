package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different status codes.
 */
public enum StatusCodeOption {
	/** Cancelled */
	CANCELLED("CANCELLED - Cancelled"),
	/** Active */
	ACTIVE("ACTIVE - Active"),
	/** Terminated */
	TERMINATED("TERMINATED - Terminated"),
	/** Inactive */
	INACTIVE("INACTIVE - Inactive"),
	/** Suspended */
	SUSPENDED("SUSPENDED - Suspended"),
	/** Nullified */
	NULLIFIED("NULLIFIED - Nullified"),
	/** Pending */
	PENDING("PENDING - Pending"),
	/** Unknown */
	UNKNOWN("UNKNOWN - Unknown");

	private String text;

	StatusCodeOption(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the StatusCodeOption.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding StatusCodeOption enum value.
	 * @param text  the string representation of the StatusCodeOption
	 * @return 		the corresponding StatusCodeOption enum value, or null if not found
	 */
	public static StatusCodeOption fromString(String text) {
		for (StatusCodeOption b : StatusCodeOption.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
