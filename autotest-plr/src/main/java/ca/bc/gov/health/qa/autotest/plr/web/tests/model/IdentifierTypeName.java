package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enumerates identifier type names used within the facility context.
 */

public enum IdentifierTypeName {
	IFC("IFC - Internal Facility Code");

	private String text;

	IdentifierTypeName(String text) {
		this.text = text;
	}

	/**
	 * Gets the display text for this identifier type name.
	 * @return display text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Resolves an enum constant by case-insensitive matching of its display text.
	 * @param text display text to match
	 * @return matching {@link IdentifierTypeName} or null if not found
	 */
	public static IdentifierTypeName fromString(String text) {
		for (IdentifierTypeName b : IdentifierTypeName.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
