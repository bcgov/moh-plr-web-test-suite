package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum StatusReasonCodeOption {
	RET("RET - Retired"), 
	PRAC("PRAC - Practising"),
	VW("VW - Voluntary Withdrawal"),
	NR("NR - Non-resident"),
	TEMPPER("VW - Temporary Permit"),
	ORG("ORG - Organization Provider"),
	INNONPRAC("INNONPRAC - Initial Non Practicing"),
	LTP("LTP - Left the Province"),
	SPE("SPE - Special Registry"),
	ASSOC("ASSOC - Associate"),
	ERSRES("ERSRES - Erased by Resolution"),
	UNK("UNK - Unknown"),
	DEC("DEC - Deceased"),
	HON("HON - Honorary"),
	TSF("TSF - Transfer"),
	DEN("DEN - Licensed Denied"),
	GS("GS - Good Standing"),
	NONPAY("NONPAY - Non Payment of Fee"),
	SUS("SUS - Suspended"),
	OOP("OOP - Out of Province"),
	AU("AU - Address Unknown"),
	NONPRAC("NONPRAC - Non Practicing"),
	TI("TI - Temporary Inactive"),
	MIS("MIS - Missionary"),
	LAP("LAP - License Lapsed on Request"),
	RESDISC("RESDISC - Resigned - disciplinary action"),
	MEDSTUD("MEDSTUD - Medical Student");

	private String text;

	StatusReasonCodeOption(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static StatusReasonCodeOption fromString(String text) {
		for (StatusReasonCodeOption b : StatusReasonCodeOption.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
