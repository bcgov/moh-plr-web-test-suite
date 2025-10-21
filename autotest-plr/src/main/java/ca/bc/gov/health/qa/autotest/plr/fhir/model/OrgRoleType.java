package ca.bc.gov.health.qa.autotest.plr.fhir.model;

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
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getRoleType()
    {
        return roleType_;
    }

}
