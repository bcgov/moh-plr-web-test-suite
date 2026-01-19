package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

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

	public String getText() {
		return this.text;
	}

	public static HdsType fromString(String text) {
		for (HdsType b : HdsType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
