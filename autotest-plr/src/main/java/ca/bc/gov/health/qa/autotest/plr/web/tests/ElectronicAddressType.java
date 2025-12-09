package ca.bc.gov.health.qa.autotest.plr.web.tests;

public enum ElectronicAddressType {
	EMAIL("E - Email"), 
	FTP("F - FTP"),
	HTTP("H - HTTP");

	private String text;

	ElectronicAddressType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static ElectronicAddressType fromString(String text) {
		for (ElectronicAddressType b : ElectronicAddressType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
