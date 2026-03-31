package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing address type codes used in PLR for provider addresses.
 */
public enum AddressType {
	/** Physical Location */
	P("P - Physical location"),
	/** Mailing Address */
	M("M - Mailing address");

	private final String text;

	AddressType(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the address type.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the code part of the address type (e.g., "P", "M").
	 *
	 * @return the code as a string
	 */
	public String getCode() {
		return this.text.split(" ")[0];
	}

	/**
	 * Gets the description part of the address type (e.g., "Physical Location").
	 *
	 * @return the description as a string
	 */
	public String getDescription() {
		return this.text.substring(this.text.indexOf("-") + 2);
	}

	/**
	 * Gets the enum from a string value.
	 *
	 * @param text the string value to match
	 * @return the corresponding enum, or null if not found
	 */
	public static AddressType fromString(String text) {
		for (AddressType at : AddressType.values()) {
			if (at.text.equalsIgnoreCase(text) || at.getCode().equalsIgnoreCase(text)) {
				return at;
			}
		}
		return null;
	}

	/**
	 * Checks if the given option string contains this address type code.
	 *
	 * @param option the option string to check
	 * @return true if the option contains this code
	 */
	public boolean matchesOption(String option) {
		return option != null && option.contains(this.getCode());
	}
}
