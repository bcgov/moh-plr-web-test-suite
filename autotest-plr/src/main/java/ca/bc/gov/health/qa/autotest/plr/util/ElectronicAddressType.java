package ca.bc.gov.health.qa.autotest.plr.util;

public enum ElectronicAddressType {
	EMAIL("E - Email"), 
	FTP("F - FTP"),
	HTTP("H - HTTP");

	private final String text;

	ElectronicAddressType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getStartText() { return this.text.split(" ")[0]; }

	public String getEndText() { return this.text.split(" ")[2]; }

	public String getDataField() { return this.getEndText() + " (" + this.getStartText() + ")"; }

	public static ElectronicAddressType fromString(String text) {
		for (ElectronicAddressType b : ElectronicAddressType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
