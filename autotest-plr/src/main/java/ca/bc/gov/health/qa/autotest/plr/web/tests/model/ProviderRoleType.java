package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ProviderRoleType {
	
	DEN("DEN - Dentist"), 
	MD("MD - Medical Doctor"), 
	RN("RN - Registered Nurse"), 
	RNP("RNP - Registered Nurse Practitioner"), 
	OOPMD("OOP-MD - OOP Medical Doctor"), 
	OOPDEN("OOP-DEN - OOP Dentist"), 
	OOPRN("OOP-RN - OOP Registered Nurse"), 
	OOPRNP("OOP-RNP - OOP Registered Nurse Practitione"), 
	OOPPHARM("OOP-PHARM - OOP Pharmacist"), 
	OOPOPT("OOP-OPT - OOP Optometris"), 
	OPT("OPT - Optometrist"), 
	RPN("RPN - Registered Psychiatric Nurse"), 
	OOPRM("OOP-RM - OOP Registered Midwife"), 
	LPN("LPN - Licensed Practical Nurse"), 
	RM("RM - Registered Midwife"), 
	PHARM("PHARM - Pharmacist"), 
	PO("PO - Podiatrist"), 
	HA("HA - Health Authority"), 
	OOPND("OOP-ND - OOP Naturopathic Doctor"), 
	OOPAUD("OOP-AUD - OOP Audiologist"), 
	OOPSW("OOP-SW - OOP Social Worker"), 
	OOPRECT("OOP-RECT - OOP Recreation Therapist"), 
	OOPRT("OOP-RT - OOP Respiratory Therapist"), 
	OOPRD("OOP-RD - OOP Registered Dietician"), 
	OOPOT("OOP-OT - OOP Occupational Therapist"), 
	OOPCC("OOP-CC - OOP Registered Clinical Counsellor"), 
	OOPSLP("OOP-SLP - OOP Speech Language Pathologist"),
	OOPPO("OOP-PO - OOP Podiatrist"),
	OOPCHIRO("OOP-CHIRO - OOP Chiropractor"),
	OOPPT("OOP-PT - OOP Physical Therapist"),
	OOPVC("OOP-VC - OOP Vocational Counsellor"),
	OOPPSYCH("OOP-PSYCH - OOP Psychologist");
	

	private String text;

	ProviderRoleType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static ProviderRoleType fromString(String text) {
		for (ProviderRoleType b : ProviderRoleType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
