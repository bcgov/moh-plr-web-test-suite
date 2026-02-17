package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderRoleTypeConsumerOptions {

	DEN("DEN - Dentist"), 
	RN("RN - Registered Nurse"), 
	RNP("RNP - Registered Nurse Practitioner"), 
	OOPMD("OOP-MD - OOP Medical Doctor"), 
	OOPDEN("OOP-DEN - OOP Dentist"), 
	OOPRN("OOP-RN - OOP Registered Nurse"), 
	OOPRNP("OOP-RNP - OOP Registered Nurse Practitioner"), 
	OOPPHARM("OOP-PHARM - OOP Pharmacist"), 
	OOPOPT("OOP-OPT - OOP Optometrist"), 
	OOPRM("OOP-RM - OOP Registered Midwife"), 
	PHARM("PHARM - Pharmacist"), 
	PO("PO - Podiatrist"), 
	HA("HA - Health Authority"), 
	OOPND("OOP-ND - OOP Naturopathic Doctor"), 
	PCY("PCY - Pharmacy"), OOPRECT("OOP-RECT - OOP Recreation Therapist"), 
	OOPSW("OOP-SW - OOP Social Worker"), 
	OOPAUD("OOP-AUD - OOP Audiologist"), 
	OOPRT("OOP-RT - OOP Respiratory Therapist"), 
	OOPRD("OOP-RD - OOP Registered Dietician"), 
	OOPOT("OOP-OT - OOP Occupational Therapist"), 
	OOPCC("OOP-CC - OOP Registered Clinical Counsellor"), 
	OOPSLP("OOP-SLP - OOP Speech Language Pathologist"),
	OOPPO("OOP-PO - OOP Podiatrist"),
	ND("ND - Naturopathic Doctor"),
	OOPCHIRO("OOP-CHIRO - OOP Chiropractor"),
	OOPPT("OOP-PT - OOP Physical Therapist"),
	OOPVC("OOP-VC - OOP Vocational Counsellor"),
	OOPPSYCH("OOP-PSYCH - OOP Psychologist"),
	RAC("RAC - Registered Acupuncturist"),
	EMR("EMR - Emergency Medical Responder"),
	ACP("ACP - Advanced Care Paramedic"),
	RTR("RTR - Radiation Technologist in Radiology"),
	MOA("MOA - Medical Office Assistant"),
	CHIRO("CHIRO - Chiropractor"),
	RET("RET - Registered Electroencephalography Technologist");


	private String text;

	ProviderRoleTypeConsumerOptions(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the ProviderRoleType.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding ProviderRoleType enum value.
	 * @param text  the string representation of the ProviderRoleType
	 * @return 		the corresponding ProviderRoleType enum value, or null if not found
	 */
	public static ProviderRoleTypeConsumerOptions fromString(String text) {
		for (ProviderRoleTypeConsumerOptions b : ProviderRoleTypeConsumerOptions.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
