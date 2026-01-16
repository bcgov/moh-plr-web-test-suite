package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Relationship Type (located at/location of)
 */
public enum RelationshipType {

	/**
	 * Facility is the location of the related provider
	 */
	LOCATION("LOCATION - Location of", "Location of (LOCATION)"),

	/**
	 * Facility is located at the related provider
	 */
	LOCATED("LOCATED - Located at", "Located at (LOCATED)");

	private String text;
	private String blockText;

	RelationshipType(String text, String blockText) {
		this.text = text; this.blockText = blockText;
	}

	/**
	 * Gets the text - the text used when selecting a Relationship Type in an Update Data Block menu
	 * @return	a string of the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the block text - the text within an Organization Relationship data block Relationship Type field
	 *
	 * @return	a string of the block text
	 */
	public String getBlockText() { return this.blockText; }

	/**
	 * Determines the relationship type enum based on the text of the type
	 *
	 * @param text		the text to determine the relationship type from
	 * @return			a RelationshipType enum reference that matches the text
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