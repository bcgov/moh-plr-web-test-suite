package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderIdentifierTypeOptions {
	CPN("CPN - Common Party Number"), 
	IPC("IPC - Internal Provider Code"), 
	DENID("DENID - Dentist ID Number"), 
	RNID("RNID - Registered Nurse ID Number"), 
	CPSID("CPSID - Physician ID Number"), 
	ORGID("ORGID - Organization"), 
	OOPID("OOPID - Out of Province Provider"), 
	MPID("MPID - Ministry Practitioner ID (MSP ID)"), 
	OPTID("OPTID - Optometrist ID Number"), 
	RMID("RMID - Registered Midwife ID Number"), 
	PHID("PHID - Pharmacist ID Number"), 
	POID("POID - Podiatrist ID"), 
	HFI("HFI - MSP Facility Number"), 
	HAID("HAID - Health Authority ID"), 
	PHYID("PHYID - Pharmacy ID Number"), 
	HLBCID("HLBCID - Healthlinks ID"), 
	SRID("SRID - Special Register General"), 
	AOMDID("AOMDID - Ambulance Operator Medical Director ID"); 
	private String text;

	ProviderIdentifierTypeOptions(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the ProviderIdentifierTypeOptions.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding ProviderIdentifierTypeOptions enum value.
	 * @param text  the string representation of the ProviderIdentifierTypeOptions
	 * @return 		the corresponding ProviderIdentifierTypeOptions enum value, or null if not found
	 */
	public static ProviderIdentifierTypeOptions fromString(String text) {
		for (ProviderIdentifierTypeOptions b : ProviderIdentifierTypeOptions.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
	
}
