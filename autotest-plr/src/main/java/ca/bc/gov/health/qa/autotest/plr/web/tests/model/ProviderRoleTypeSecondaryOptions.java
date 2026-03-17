package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderRoleTypeSecondaryOptions {

	DEN("DEN - Dentist"), 
	MD("MD - Medical Doctor"), 
	RN("RN - Registered Nurse"), 
	OOPDEN("OOP-DEN - OOP Dentist"); 
	
	private String text;

	ProviderRoleTypeSecondaryOptions(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the ProviderRoleTypeSecondaryOptions.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding ProviderRoleTypeSecondaryOptions enum value.
	 * @param text  the string representation of the ProviderRoleTypeSecondaryOptions
	 * @return 		the corresponding ProviderRoleTypeSecondaryOptions enum value, or null if not found
	 */
	public static ProviderRoleTypeSecondaryOptions fromString(String text) {
		for (ProviderRoleTypeSecondaryOptions b : ProviderRoleTypeSecondaryOptions.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
