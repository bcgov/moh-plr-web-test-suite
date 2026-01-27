package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different types of Health Delivery Services (HDS).
 */
public enum HdsType {
	CLINIC("CLINIC - Clinic"), 
	PHARMACY("PHARMACY - Pharmacy"),
	HOSPITAL("HOSPITAL - Hospital"),
	EMERGENCY("EMERGENCY - Emergency"),
	LAB("LAB - Laboratory"),
	GENERAL_CARE("GENERAL_CARE - General Care"),
	INPATIENT("INPATIENT - Inpatient"),
	HOUSING("HOUSING - Housing"),
	OUTPATIENT("OUTPATIENT - Outpatient");
	
	private String text;

	HdsType(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the HdsType.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding HdsType enum value.
	 * @param text  the string representation of the HdsType
	 * @return 		the corresponding HdsType enum value, or null if not found
	 */
	public static HdsType fromString(String text) {
		for (HdsType b : HdsType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
