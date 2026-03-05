package ca.bc.gov.health.qa.autotest.plr.util;

/**
 * Enum for Related Provider Identifier Types
 */
public enum RelatedProviderIdentifierType {
	/** Pharmacy ID Number */
	PHYID("PHYID - Pharmacy ID Number"),
	/** Internal Provider Code */
	IPC("IPC - Internal Provider Code"),
	/** MSP Facility Number */
	HFI("HFI - MSP Facility Number"),
	/** Common Party Number */
	CPN("CPN - Common Party Number"),
	/** Healthlinks ID */
	HLBCID("HLBCID - Healthlinks ID"),
	/** Organization */
	ORGID("ORGID - Organization");

	private final String text;

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
