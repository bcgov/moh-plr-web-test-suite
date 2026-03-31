package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum for Electronic Address Purposes
 */
public enum ElectronicAddressPurpose {
	/** Ministry Contact */
	MINISTRY_CONTACT("MC - Ministry Contact"),
	/** Other Contact */
	OTHER_CONTACT("OC - Other Contact"),
	/** Business Contact */
	BUSINESS_CONTACT("BC - Business Contact"),
	/** Home Contact */
	HOME_CONTACT("HC - Home Contact"),
	/** College Contact */
	COLLEGE_CONTACT("CC - College Contact"),
	/** Doctor's Contact */
	DOCTORS_CONTACT("DC - Doctor's Contact"),
	/** Facility Contact */
	FACILITY_CONTACT("FC - Facility Contact");

	private final String text;

	ElectronicAddressPurpose(String text) {
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
	 * Get the starting text before the hyphen (code)
	 *
	 * @return the starting text as a string
	 */
	public String getStartText() { return this.text.split(" ")[0]; }

	/**
	 * Get the ending text after the hyphen (description)
	 *
	 * @return the ending text as a string
	 */
	public String getEndText() { 
		String[] parts = this.text.split(" - ");
		return parts.length > 1 ? parts[1] : this.text; 
	}

	/**
	 * Get the data field format "EndText (StartText)"
	 *
	 * @return the data field as a string
	 */
	public String getDataField() { return this.getEndText() + " (" + this.getStartText() + ")"; }
}
