package ca.bc.gov.health.qa.autotest.plr.util;

public enum RelatedProviderIdentifierType {
	PHYID("PHYID - Pharmacy ID Number"), 
	IPC("IPC - Internal Provider Code"), 
	HFI("HFI - MSP Facility Number"),
	CPN("CPN - Common Party Number"), 
	HLBCID("HLBCID - Healthlinks ID"), 
	ORGID("ORGID - Organization");

	private String text;

	RelatedProviderIdentifierType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static RelatedProviderIdentifierType fromString(String text) {
		for (RelatedProviderIdentifierType b : RelatedProviderIdentifierType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
