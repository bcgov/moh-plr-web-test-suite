package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enumerates gender codes used in the system.
 */
public enum GenderCode {
	 U("U - Unknown"), 
	 F("F - Female"),
	 M("M - Male");

	private String text;

	GenderCode(String text) {
		this.text = text;
	}

	/**
	 * Gets the display text associated with this gender.
	 * @return display text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Resolves an enum constant by case-insensitive matching of its display text.
	 * @param text  display text to match
	 * @return 		matching {@link GenderCode} or null if not found
	 */
	public static GenderCode fromString(String text) {
		for (GenderCode b : GenderCode.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
