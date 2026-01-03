package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum IdentifierTypeName {
	IFC("IFC - Internal Facility Code");

	private String text;

	IdentifierTypeName(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static IdentifierTypeName fromString(String text) {
		for (IdentifierTypeName b : IdentifierTypeName.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
