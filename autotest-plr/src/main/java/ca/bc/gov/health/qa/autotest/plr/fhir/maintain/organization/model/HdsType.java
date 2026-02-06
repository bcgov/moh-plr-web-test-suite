package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

/**
 * Enumeration of Health Delivery Site (HDS) ype classifications used only when
 * an organization's role type is {@link OrgRoleType#HDS}. The enum constant name is
 * serialized directly as the coding.code value in the specialized _type extension block.
 */
public enum HdsType {
    /**
     * TODO (AZ) - doc
     */
    CLINIC,
    /**
     * TODO (AZ) - doc
     */
    PHARMACY,
    /**
     * TODO (AZ) - doc
     */
    HOSPITAL,
    /**
     * TODO (AZ) - doc
     */
    EMERGENCY,
    /**
     * TODO (AZ) - doc
     */
    LAB,
    /**
     * TODO (AZ) - doc
     */
    GENERAL_CARE,
    /**
     * TODO (AZ) - doc
     */
    INPATIENT,
    /**
     * TODO (AZ) - doc
     */
    HOUSING,
    /**
     * TODO (AZ) - doc
     */
    OUTPATIENT;

    /**
     * Resolves an HDS type enum from a code string.
     *
     * @param code the HDS code
     * @return the matching `HdsType`, or null if not found
     */
    public static HdsType resolveHdsType(String code) {
		if (code == null) return null;
		for (HdsType h : HdsType.values()) {
			if (h.name().equalsIgnoreCase(code)) return h; // case-insensitive match
		}
		return null;
	}
}
