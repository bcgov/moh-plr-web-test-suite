package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;

/**
 * Criteria container for Organization $entityQuery parameters. Only non-null/non-blank
 * values will be sent. The {@code withHistory} flag is included when true.
 * Convenience setter accepts {@link OrgRoleType} to populate the {@code type} filter.
 */
public class OrgQueryCriteriaParams {
    private String name = null;
    private String description = null;
    private OrgRoleType type = null;
    private String addressCity = null;
    private String addressLine1 = null;
    private boolean withHistory = false;

    public OrgQueryCriteriaParams() {}

    public String getName() { return name; }
    public OrgQueryCriteriaParams setName(String name) { this.name = name; return this; }

    public String getDescription() { return description; }
    public OrgQueryCriteriaParams setDescription(String description) { this.description = description;  return this; }

    public String getRoleType() { return type.getRoleType(); }
    public OrgQueryCriteriaParams setRoleType(OrgRoleType roleType) { this.type = roleType; return this; }

    public String getAddressCity() { return addressCity; }
    public OrgQueryCriteriaParams setAddressCity(String addressCity) { this.addressCity = addressCity; return this; }

    public String getAddressLine1() { return addressLine1; }
    public OrgQueryCriteriaParams setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }

    public boolean isWithHistory() { return withHistory; }
    public OrgQueryCriteriaParams setWithHistory(boolean withHistory) { this.withHistory = withHistory; return this; }

}
