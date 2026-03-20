package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderIdentifierTypeSecondaryOptions {
	CPN("CPN - Common Party Number"),
	IPC("IPC - Internal Provider Code"),
	DENID("DENID - Dentist ID Number"),
	RNID("RNID - Registered Nurse ID Number"),
	CPSID("CPSID - Physician ID Number"),
	ORGID("ORGID - Organization"),
	OOPID("OOPID - Out of Province Provider"),
	MPID("MPID - Ministry Practitioner ID (MSP ID)"),
	HFI("HFI - MSP Facility Number"),
	PHYID("PHYID - Pharmacy ID Number"),
	HLBCID("HLBCID - Healthlinks ID"),
	SRID("SRID - Special Register General"),
	AOMDID("AOMDID - Ambulance Operator Medical Director ID");
	private String text;

	ProviderIdentifierTypeSecondaryOptions(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the ProviderIdentifierTypeSecondaryOptions.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding ProviderIdentifierTypeSecondaryOptions enum value.
	 * @param text  the string representation of the ProviderIdentifierTypeSecondaryOptions
	 * @return 		the corresponding ProviderIdentifierTypeSecondaryOptions enum value, or null if not found
	 */
	public static ProviderIdentifierTypeSecondaryOptions fromString(String text) {
		for (ProviderIdentifierTypeSecondaryOptions b : ProviderIdentifierTypeSecondaryOptions.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
	
}
