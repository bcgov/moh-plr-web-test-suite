package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different types of Identifier Type Names.
 */
public enum IdentifierTypeName {
	/** Select One */
	SELECT_ONE("Select One"),
	/** Internal Facility Code */
	IFC("IFC - Internal Facility Code");

	private final String text;

	IdentifierTypeName(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the identifier type name.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to the corresponding IdentifierTypeName enum value.
	 *
	 * @param text  the string representation of the identifier type name
	 * @return 		the corresponding IdentifierTypeName enum value, or null if not found
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
