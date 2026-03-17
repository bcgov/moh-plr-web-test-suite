package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderIdentifierTypeConsumerOptions {

	CPN("CPN - Common Party Number"),
	IPC("IPC - Internal Provider Code"),
	DENID("DENID - Dentist ID Number"),
	RNID("RNID - Registered Nurse ID Number"),
	ORGID("ORGID - Organization"),
	OOPID("OOPID - Out of Province Provider"),
	PHID("PHID - Pharmacist ID Number"),
	POID("POID - Podiatrist ID"),
	HFI("HFI - MSP Facility Number"),
	HAID("HAID - Health Authority ID"),
	PHYID("PHYID - Pharmacy ID Number"),
	HLBCID("HLBCID - Healthlinks ID"),
	NDID("NDID - Naturopathic Doctor ID"),
	PPID("PPID - Paramedic Practitioner ID"),
	MRTID("MRTID - Medical Radiation Technologist Identifier"),
	CHIROID("CHIROID - CHIROID"),
	ENPID("ENPID - Electroneurophysiology Technologist Identifier"),
	MOAID("MOAID - Medical Office Assistant ID"),
	RACID("RACID - Registered Acupuncturist ID");


	
	private String text;
	ProviderIdentifierTypeConsumerOptions(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the ProviderIdentifierTypeConsumerOptions.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding ProviderIdentifierTypeConsumerOptions enum value.
	 * @param text  the string representation of the ProviderIdentifierTypeConsumerOptions
	 * @return 		the corresponding ProviderIdentifierTypeConsumerOptions enum value, or null if not found
	 */
	public static ProviderIdentifierTypeConsumerOptions fromString(String text) {
		for (ProviderIdentifierTypeConsumerOptions b : ProviderIdentifierTypeConsumerOptions.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
	

}
