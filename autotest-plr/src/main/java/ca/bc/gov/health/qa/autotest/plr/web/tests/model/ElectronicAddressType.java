package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum for Electronic Address Types
 */
public enum ElectronicAddressType {
	/** Email */
	EMAIL("E - Email"),
	/** FTP */
	FTP("F - FTP"),
	/** HTTP */
	HTTP("H - HTTP");

	private final String text;

	ElectronicAddressType(String text) {
		this.text = text;
	}

	/**
	 * Get the text value of the enum
	 * @return the text value as a string
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Get the starting text before the hyphen
	 *
	 * @return the starting text as a string
	 */
	public String getStartText() { return this.text.split(" ")[0]; }

	/**
	 * Get the ending text after the hyphen
	 *
	 * @return the ending text as a string
	 */
	public String getEndText() { return this.text.split(" ")[2]; }

	/**
	 * Get the data field format "EndText (StartText)"
	 *
	 * @return the data field as a string
	 */
	public String getDataField() { return this.getEndText() + " (" + this.getStartText() + ")"; }

	/**
	 * Get the enum from a string value
	 *
	 * @param text  the string value
	 * @return 		the corresponding enum, or null if not found
	 */
	public static ElectronicAddressType fromString(String text) {
		for (ElectronicAddressType b : ElectronicAddressType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
