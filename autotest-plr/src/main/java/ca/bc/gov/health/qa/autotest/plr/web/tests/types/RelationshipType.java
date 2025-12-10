package ca.bc.gov.health.qa.autotest.plr.web.tests.types;

public enum RelationshipType {
	
	LOCATION("LOCATION - Location of"), 
	LOCATED("LOCATED - Located at");
	 

	private String text;

	RelationshipType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static RelationshipType fromString(String text) {
		for (RelationshipType b : RelationshipType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
