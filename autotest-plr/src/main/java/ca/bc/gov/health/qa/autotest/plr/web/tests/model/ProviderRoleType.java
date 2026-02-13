package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different types of Provider Roles.
 */
public enum ProviderRoleType {

	/** Dentist */
	DEN("DEN - Dentist"),
	/** Medical Doctor */
	MD("MD - Medical Doctor"),
	/** Registered Nurse */
	RN("RN - Registered Nurse"),
	/** Registered Nurse Practitioner */
	RNP("RNP - Registered Nurse Practitioner"),
	/** Out of Province Medical Doctor */
	OOPMD("OOP-MD - OOP Medical Doctor"),
	/** Out of Province Dentist */
	OOPDEN("OOP-DEN - OOP Dentist"),
	/** Out of Province Registered Nurse */
	OOPRN("OOP-RN - OOP Registered Nurse"),
	/** Out of Province Registered Nurse Practitioner */
	OOPRNP("OOP-RNP - OOP Registered Nurse Practitione"),
	/** Out of Province Pharmacist */
	OOPPHARM("OOP-PHARM - OOP Pharmacist"),
	/** Out of Province Optometrist */
	OOPOPT("OOP-OPT - OOP Optometris"),
	/** Optometrist */
	OPT("OPT - Optometrist"),
	/** Registered Psychiatric Nurse */
	RPN("RPN - Registered Psychiatric Nurse"),
	/** Out of Province Registered Midwife */
	OOPRM("OOP-RM - OOP Registered Midwife"),
	/** Licensed Practical Nurse */
	LPN("LPN - Licensed Practical Nurse"),
	/** Registered Midwife */
	RM("RM - Registered Midwife"),
	/** Pharmacist */
	PHARM("PHARM - Pharmacist"),
	/** Podiatrist */
	PO("PO - Podiatrist"),
	/** Health Authority */
	HA("HA - Health Authority"),
	/** Out of Province Naturopathic Doctor */
	OOPND("OOP-ND - OOP Naturopathic Doctor"),
	/** Out of Province Audiologist */
	OOPAUD("OOP-AUD - OOP Audiologist"),
	/** Out of Province Social Worker */
	OOPSW("OOP-SW - OOP Social Worker"),
	/** Out of Province Recreation Therapist */
	OOPRECT("OOP-RECT - OOP Recreation Therapist"),
	/** Out of Province Respiratory Therapist */
	OOPRT("OOP-RT - OOP Respiratory Therapist"),
	/** Out of Province Registered Dietician */
	OOPRD("OOP-RD - OOP Registered Dietician"),
	/** Out of Province Occupational Therapist */
	OOPOT("OOP-OT - OOP Occupational Therapist"),
	/** Out of Province Registered Clinical Counsellor */
	OOPCC("OOP-CC - OOP Registered Clinical Counsellor"),
	/** Out of Province Speech Language Pathologist */
	OOPSLP("OOP-SLP - OOP Speech Language Pathologist"),
	/** Out of Province Podiatrist */
	OOPPO("OOP-PO - OOP Podiatrist"),
	/** Out of Province Chiropractor */
	OOPCHIRO("OOP-CHIRO - OOP Chiropractor"),
	/** Out of Province Physical Therapist */
	OOPPT("OOP-PT - OOP Physical Therapist"),
	/** Out of Province Vocational Counsellor */
	OOPVC("OOP-VC - OOP Vocational Counsellor"),
	/** Out of Province Psychologist */
	OOPPSYCH("OOP-PSYCH - OOP Psychologist");
	

	private String text;

	ProviderRoleType(String text) {
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
	public static ProviderRoleType fromString(String text) {
		for (ProviderRoleType b : ProviderRoleType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
