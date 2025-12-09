package ca.bc.gov.health.qa.autotest.plr.fhir.model;

/**
 * Enumeration of PLR Organization role type codes used when composing FHIR Organization
 * resources in maintain requests. The enum constant name matches the conceptual role while
 * the associated code value (returned by {@link #getRoleType()}) is the literal stored in
 * the outbound JSON.
 */
public enum OrgRoleType {
    /** Business organization */
    BUSINESS("BUSINESS"),
    /** Org organization */
    ORG("ORG"),
    /** Clinical organization */
    CLINIC("CLINIC"),
    /** HDS organization */
    HDS("HDS");

    private final String roleType_;

    OrgRoleType(String roleType)
    {
        roleType_ = roleType;
    }

    /**
     * Returns the code value to emit in FHIR Organization.type coding for this role.
     * @return role type code literal
     */
    public String getRoleType()
    {
        return roleType_;
    }

    /**
     * Resolves an organization role type from a code string.
     *
     * @param code the role code
     * @return the matching `OrgRoleType`, or null if not found
     */
    public static OrgRoleType resolveRoleType(String code) {
		if (code == null) return null;
		for (OrgRoleType r : OrgRoleType.values()) {
			if (r.getRoleType().equalsIgnoreCase(code)) return r;
		}
		return null;
	}

}
