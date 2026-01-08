package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enumerates relationship types between facilities and locations.
 */

public enum RelationshipType {
	
	LOCATION("LOCATION - Location of"), 
	LOCATED("LOCATED - Located at");
	 

	private String text;

	RelationshipType(String text) {
		this.text = text;
	}

	/**
	 * Gets the display text associated with this relationship type.
	 * @return display text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Resolves an enum constant by case-insensitive matching of its display text.
	 * @param text display text to match
	 * @return matching {@link RelationshipType} or null if not found
	 */
	public static RelationshipType fromString(String text) {
		for (RelationshipType b : RelationshipType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
