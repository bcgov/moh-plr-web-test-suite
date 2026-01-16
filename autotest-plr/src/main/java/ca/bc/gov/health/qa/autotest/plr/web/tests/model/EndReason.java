package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * End Reasons
 */
public enum EndReason {

	/**
	 * Cease Data block
	 */
	 CEASE("CEASE - Cease"),

	/**
	 * Change Data block info
	 */
	 CHG("CHG - Change"),

	/**
	 * Data block correction
	 */
	 CORR("CORR - Correct");

	private String text;

	EndReason(String text) {
		this.text = text;
	}

	/**
	 * Gets the end reason text visible in the update facility menu or view facility data block field for End Reason
	 *
	 * @return	a string of the end reason text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Determines an EndReason enum based on a text string
	 *
	 * @param text	the text string to match with an EndReason
	 * @return		an EndReason enum that matches the text string
	 */
	public static EndReason fromString(String text) {
		for (EndReason b : EndReason.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
	

}