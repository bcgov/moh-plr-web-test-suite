package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum GenderCode {
	 U("U - Unknown"), 
	 F("F - Female"),
	 M("M - Male");

	private String text;

	GenderCode(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static GenderCode fromString(String text) {
		for (GenderCode b : GenderCode.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
