package ca.bc.gov.health.qa.autotest.plr.util;

public enum TelecommunicationType {
	PHONE("T - Telephone"), 
	MOBILE("MB - Mobile"),
	PAGER("PG - Pager"),
	FAX("FAX - Fax"),
	MODEM("M - Modem");

	private final String text;

	TelecommunicationType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getStartText() { return this.text.split(" ")[0]; }

	public static TelecommunicationType fromString(String text) {
		for (TelecommunicationType b : TelecommunicationType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
