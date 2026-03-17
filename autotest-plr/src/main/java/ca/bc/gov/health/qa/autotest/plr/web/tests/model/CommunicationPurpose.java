package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing communication purpose codes used in PLR for addresses and telecommunications.
 */
public enum CommunicationPurpose {
	/** Business Contact */
	BC("BC - Business Contact"),
	/** College Contact */
	CC("CC - College Contact"),
	/** Doctor's Contact */
	DC("DC - Doctor's Contact"),
	/** Facility Contact */
	FC("FC - Facility Contact"),
	/** Home Contact */
	HC("HC - Home Contact"),
	/** Ministry Contact */
	MC("MC - Ministry Contact"),
	/** Other Contact */
	OC("OC - Other Contact");

	private final String text;

	CommunicationPurpose(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the communication purpose.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the code part of the communication purpose (e.g., "BC", "HC").
	 *
	 * @return the code as a string
	 */
	public String getCode() {
		return this.text.split(" ")[0];
	}

	/**
	 * Gets the description part of the communication purpose (e.g., "Business Contact").
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
	public static CommunicationPurpose fromString(String text) {
		for (CommunicationPurpose cp : CommunicationPurpose.values()) {
			if (cp.text.equalsIgnoreCase(text) || cp.getCode().equalsIgnoreCase(text)) {
				return cp;
			}
		}
		return null;
	}

	/**
	 * Checks if the given option string contains this communication purpose code.
	 *
	 * @param option the option string to check
	 * @return true if the option contains this code
	 */
	public boolean matchesOption(String option) {
		return option != null && option.contains(this.getCode());
	}
}
