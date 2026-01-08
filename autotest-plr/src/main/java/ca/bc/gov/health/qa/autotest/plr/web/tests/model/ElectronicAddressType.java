package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enumerates electronic address types used by facility contact information.
 */

public enum ElectronicAddressType {
	EMAIL("E - Email"), 
	FTP("F - FTP"),
	HTTP("H - HTTP");

	private String text;

	ElectronicAddressType(String text) {
		this.text = text;
	}

	/**
	 * Gets the display text associated with this type.
	 * @return display text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Resolves an enum constant by case-insensitive matching of its display text.
	 * @param text display text to match
	 * @return matching {@link ElectronicAddressType} or null if not found
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
