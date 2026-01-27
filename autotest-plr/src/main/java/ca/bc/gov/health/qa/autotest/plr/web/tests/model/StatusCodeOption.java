package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different status codes.
 */
public enum StatusCodeOption {
	CANCELLED("CANCELLED - Cancelled"), 
	ACTIVE("ACTIVE - Active"), 
	TERMINATED("TERMINATED - Terminated"), 
	INACTIVE("INACTIVE - Inactive"), 
	SUSPENDED("SUSPENDED - Suspended"), 
	NULLIFIED("NULLIFIED - Nullified"),
	PENDING("PENDING - Pending"),
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
