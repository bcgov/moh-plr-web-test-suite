package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing US state codes used in PLR for provider addresses.
 */
public enum USState {
	/** Alabama */
	AL("AL - Alabama"),
	/** Alaska */
	AK("AK - Alaska"),
    /** American Samoa */
    AS("AS - American Samoa"),
	/** Arizona */
	AZ("AZ - Arizona"),
	/** Arkansas */
	AR("AR - Arkansas"),
	/** California */
	CA("CA - California"),
	/** Colorado */
	CO("CO - Colorado"),
	/** Connecticut */
	CT("CT - Connecticut"),
	/** Delaware */
	DE("DE - Delaware"),
	/** District of Columbia */
	DC("DC - District of Columbia"),
	/** Florida */
	FL("FL - Florida"),
	/** Georgia */
	GA("GA - Georgia"),
	/** Hawaii */
	HI("HI - Hawaii"),
	/** Idaho */
	ID("ID - Idaho"),
	/** Illinois */
	IL("IL - Illinois"),
	/** Indiana */
	IN("IN - Indiana"),
	/** Iowa */
	IA("IA - Iowa"),
	/** Kansas */
	KS("KS - Kansas"),
	/** Kentucky */
	KY("KY - Kentucky"),
	/** Louisiana */
	LA("LA - Louisiana"),
	/** Maine */
	ME("ME - Maine"),
	/** Maryland */
	MD("MD - Maryland"),
	/** Massachusetts */
	MA("MA - Massachusetts"),
	/** Michigan */
	MI("MI - Michigan"),
	/** Minnesota */
	MN("MN - Minnesota"),
	/** Mississippi */
	MS("MS - Mississippi"),
	/** Missouri */
	MO("MO - Missouri"),
	/** Montana */
	MT("MT - Montana"),
	/** Nebraska */
	NE("NE - Nebraska"),
	/** Nevada */
	NV("NV - Nevada"),
	/** New Jersey */
	NJ("NJ - New Jersey"),
	/** New Mexico */
	NM("NM - New Mexico"),
	/** New York */
	NY("NY - New York"),
	/** North Carolina */
	NC("NC - North Carolina"),
	/** North Dakota */
	ND("ND - North Dakota"),
    /** Northern Mariana Islands */
	MP("MP - Northern Mariana Islands"),
	/** Ohio */
	OH("OH - Ohio"),
	/** Oklahoma */
	OK("OK - Oklahoma"),
	/** Oregon */
	OR("OR - Oregon"),
	/** Pennsylvania */
	PA("PA - Pennsylvania"),
    /** Puerto Rico */
	PR("PR - Puerto Rico"),
	/** Rhode Island */
	RI("RI - Rhode Island"),
	/** South Carolina */
	SC("SC - South Carolina"),
	/** South Dakota */
	SD("SD - South Dakota"),
	/** Tennessee */
	TN("TN - Tennessee"),
	/** Texas */
	TX("TX - Texas"),
    /** U.S. Minor Outlying Islands */
	UM("UM - U.S. Minor Outlying Islands"),
	/** Utah */
	UT("UT - Utah"),
	/** Vermont */
	VT("VT - Vermont"),
    /** Virgin Islands */
    VI("VI - Virgin Islands of the U.S."),
	/** Virginia */
	VA("VA - Virginia"),
	/** Washington */
	WA("WA - Washington"),
	/** West Virginia */
	WV("WV - West Virginia"),
	/** Wisconsin */
	WI("WI - Wisconsin"),
	/** Wyoming */
	WY("WY - Wyoming");

	private final String text;

	USState(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the state.
	 *
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Gets the code part of the state (e.g., "AL", "CA").
	 *
	 * @return the code as a string
	 */
	public String getCode() {
		return this.text.split(" ")[0];
	}

	/**
	 * Gets the description part of the state (e.g., "Alabama", "California").
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
	public static USState fromString(String text) {
		for (USState state : USState.values()) {
			if (state.text.equalsIgnoreCase(text) || state.getCode().equalsIgnoreCase(text)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Checks if the given option string contains this state code.
	 *
	 * @param option the option string to check
	 * @return true if the option contains this code
	 */
	public boolean matchesOption(String option) {
		return option != null && option.contains(this.getCode());
	}
}
