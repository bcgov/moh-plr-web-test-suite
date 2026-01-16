package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ElectronicAddressType {
	EMAIL("E - Email"), 
	FTP("F - FTP"),
	HTTP("H - HTTP");

	private final String text;

	ElectronicAddressType(String text) {
		this.text = text;
	}

	/**
	 * Gets the text - the text used when selecting E-Address Type in update menu
	 * @return	a string of the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the start of the E-Address type text (the single letter)
	 * @return	a string of the starting text
	 */
	public String getStartText() { return this.text.split(" ")[0]; }

	/**
	 * Gets the end of the E-Address type text (the non-abbreviated version of the type)
	 * @return	a string of the ending text
	 */
	public String getEndText() { return this.text.split(" ")[2]; }

	/**
	 * Gets the data field as it is shown in the view facility data block
	 * @return	a string of the data field
	 */
	public String getDataField() { return this.getEndText() + " (" + this.getStartText() + ")"; }

	/**
	 * Determines an ElectronicAddressType enum based on a text string
	 *
	 * @param text	the text string to match with an ElectronicAddressType
	 * @return		an ElectronicAddressType enum that matches the text string
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