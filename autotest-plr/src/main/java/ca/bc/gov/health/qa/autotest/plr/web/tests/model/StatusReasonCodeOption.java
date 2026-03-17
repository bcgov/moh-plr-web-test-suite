package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different status reason codes.
 */
public enum StatusReasonCodeOption {
	/** Retired */
	RET("RET - Retired"),
	/** Practising */
	PRAC("PRAC - Practising"),
	/** Voluntary Withdrawal */
	VW("VW - Voluntary Withdrawal"),
	/** Non-resident */
	NR("NR - Non-resident"),
	/** Temporary Permit */
	TEMPPER("VW - Temporary Permit"),
	/** Organization Provider */
	ORG("ORG - Organization Provider"),
	/** Initial Non Practicing */
	INNONPRAC("INNONPRAC - Initial Non Practicing"),
	/** Left the Province */
	LTP("LTP - Left the Province"),
	/** Special Registry */
	SPE("SPE - Special Registry"),
	/** Associate */
	ASSOC("ASSOC - Associate"),
	/** Erased by Resolution */
	ERSRES("ERSRES - Erased by Resolution"),
	/** Unknown */
	UNK("UNK - Unknown"),
	/** Deceased */
	DEC("DEC - Deceased"),
	/** Honorary */
	HON("HON - Honorary"),
	/** Transfer */
	TSF("TSF - Transfer"),
	/** Licensed Denied */
	DEN("DEN - Licensed Denied"),
	/** Good Standing */
	GS("GS - Good Standing"),
	/** Non Payment of Fee */
	NONPAY("NONPAY - Non Payment of Fee"),
	/** Suspended */
	SUS("SUS - Suspended"),
	/** Out of Province */
	OOP("OOP - Out of Province"),
	/** Address Unknown */
	AU("AU - Address Unknown"),
	/** Non Practicing */
	NONPRAC("NONPRAC - Non Practicing"),
	/** Temporary Inactive */
	TI("TI - Temporary Inactive"),
	/** Missionary */
	MIS("MIS - Missionary"),
	/** License Lapsed on Request */
	LAP("LAP - License Lapsed on Request"),
	/** Resigned - disciplinary action */
	RESDISC("RESDISC - Resigned - disciplinary action"),
	/** Medical Student */
	MEDSTUD("MEDSTUD - Medical Student");

	private String text;

	StatusReasonCodeOption(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the StatusReasonCodeOption.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding StatusReasonCodeOption enum value.
	 * @param text  the string representation of the StatusReasonCodeOption
	 * @return 		the corresponding StatusReasonCodeOption enum value, or null if not found
	 */
	public static StatusReasonCodeOption fromString(String text) {
		for (StatusReasonCodeOption b : StatusReasonCodeOption.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
