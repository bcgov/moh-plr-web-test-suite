package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing Canadian province/territory codes used in PLR for provider addresses.
 */
public enum CanadianProvince {
	/** Alberta */
	AB("AB - Alberta"),
	/** British Columbia */
	BC("BC - British Columbia"),
	/** Manitoba */
	MB("MB - Manitoba"),
	/** New Brunswick */
	NB("NB - New Brunswick"),
	/** Newfoundland and Labrador */
	NL("NL - Newfoundland and Labrador"),
	/** Northwest Territories */
	NT("NT - Northwest Territories"),
	/** Nova Scotia */
	NS("NS - Nova Scotia"),
	/** Nunavut */
	NU("NU - Nunavut"),
	/** Ontario */
	ON("ON - Ontario"),
	/** Prince Edward Island */
	PE("PE - Prince Edward Island"),
	/** Quebec */
	QC("QC - Quebec"),
	/** Saskatchewan */
	SK("SK - Saskatchewan"),
	/** Yukon */
	YT("YT - Yukon Territories");

	private final String text;

	CanadianProvince(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the province/territory.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the code part of the province/territory (e.g., "AB", "BC").
	 *
	 * @return the code as a string
	 */
	public String getCode() {
		return this.text.split(" ")[0];
	}

	/**
	 * Gets the description part of the province/territory (e.g., "Alberta", "British Columbia").
	 *
	 * @return the description as a string
	 */
	public String getDescription() {
		return this.text.substring(this.text.indexOf("-") + 2);
	}

	/**
	 * Gets the enum from a string value.
	 *
	 * @param text the string value to match
	 * @return the corresponding enum, or null if not found
	 */
	public static CanadianProvince fromString(String text) {
		for (CanadianProvince cp : CanadianProvince.values()) {
			if (cp.text.equalsIgnoreCase(text) || cp.getCode().equalsIgnoreCase(text)) {
				return cp;
			}
		}
		return null;
	}

	/**
	 * Checks if the given option string contains this province/territory code.
	 *
	 * @param option the option string to check
	 * @return true if the option contains this code
	 */
	public boolean matchesOption(String option) {
		return option != null && option.contains(this.getCode());
	}
}
