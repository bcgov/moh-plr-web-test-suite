package ca.bc.gov.health.qa.autotest.plr.util;

public enum RelationshipType {
	
	LOCATION("LOCATION - Location of", "Location of (LOCATION)"),
	LOCATED("LOCATED - Located at", "Located at (LOCATED)");
	 

	private String text;
	private String blockText;

	RelationshipType(String text, String blockText) {
		this.text = text; this.blockText = blockText;
	}

	public String getText() {
		return this.text;
	}

	public String getBlockText() { return this.blockText; }

	public static RelationshipType fromString(String text) {
		for (RelationshipType b : RelationshipType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
