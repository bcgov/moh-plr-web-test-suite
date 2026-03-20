package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enumerates end reasons used for relationship/status termination.
 */
public enum EndReason {
	/** Cease */
	CEASE("CEASE - Cease"),
	/** Change */
	CHG("CHG - Change"),
	/** Correct */
	CORR("CORR - Correct");

	private String text;

	EndReason(String text) {
		this.text = text;
	}

	/**
	 * Gets the display text associated with this end reason.
	 * @return display text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Resolves an enum constant by case-insensitive matching of its display text.
	 * @param text display text to match
	 * @return matching {@link EndReason} or null if not found
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
