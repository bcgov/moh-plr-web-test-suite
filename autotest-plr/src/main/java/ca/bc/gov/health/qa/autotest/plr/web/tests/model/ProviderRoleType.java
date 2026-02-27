package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing different types of Provider Roles.
 */
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

	private static final Set<ProviderRoleType> BC_TYPE_SET =
			Collections.unmodifiableSet(EnumSet.of(DEN, MD, RN, RNP, OPT, RPN, LPN, RM, PHARM, PO, HA));

	private static final Set<ProviderRoleType> OOP_TYPE_SET =
			Collections.unmodifiableSet(EnumSet.of(OOPMD, OOPDEN, OOPRN, OOPRNP, OOPPHARM, OOPOPT, OOPRM, OOPND, OOPAUD,
					OOPSW, OOPRECT, OOPRT, OOPRD, OOPOT, OOPCC, OOPSLP, OOPPO, OOPCHIRO, OOPPT, OOPVC, OOPPSYCH));

	/**
	 * Gets the set of ProviderRoleType values associated with the given ProviderType.
	 * @param providerType the ProviderType for which to retrieve the associated ProviderRoleType values
	 * @return a set of ProviderRoleType values associated with the given ProviderType
	 */
	public static Set<ProviderRoleType> getProviderRoleTypeSet(ProviderType providerType)
	{
		if (providerType.equals(ProviderType.BC_PRACTITIONER)) return BC_TYPE_SET;
		else return OOP_TYPE_SET;
	}
}
