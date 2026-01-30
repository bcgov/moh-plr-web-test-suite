package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different types of telecommunication methods.
 */
public enum TelecommunicationType {
	/** Telephone */
	PHONE("T - Telephone"),
	/** Mobile */
	MOBILE("MB - Mobile"),
	/** Pager */
	PAGER("PG - Pager"),
	/** Fax */
	FAX("FAX - Fax"),
	/** Modem */
	MODEM("M - Modem");

	private final String text;

	TelecommunicationType(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the telecommunication type.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the starting part of the text representation.
	 *
	 * @return the starting part as a string
	 */
	public String getStartText() { return this.text.split(" ")[0]; }

	/**
	 * Gets the ending part of the text representation.
	 *
	 * @return the ending part as a string
	 */
	public String getEndText() { return this.text.split(" ")[2]; }

	/**
	 * Gets a formatted data field combining the end and start text (e.g., "Telephone (T)").
	 *
	 * @return the formatted data field
	 */
	public String getDataField() { return this.getEndText() + " (" + this.getStartText() + ")"; }

	/**
	 * Converts a string to the corresponding TelecommunicationType enum value.
	 *
	 * @param text  the string representation
	 * @return 		the corresponding TelecommunicationType, or null if not found
	 */
	public static TelecommunicationType fromString(String text) {
		for (TelecommunicationType b : TelecommunicationType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}