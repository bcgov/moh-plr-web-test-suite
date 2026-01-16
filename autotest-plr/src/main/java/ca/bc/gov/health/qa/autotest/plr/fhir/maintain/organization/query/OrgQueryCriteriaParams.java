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

    /**
     * Gets the organization name filter.
     * @return organization name or null if not set
     */
    public String getName() { return name; }
    
    /**
     * Sets the organization name filter for the query.
     * @param name organization name to search for
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setName(String name) { this.name = name; return this; }

    /**
     * Gets the organization description filter.
     * @return organization description or null if not set
     */
    public String getDescription() { return description; }
    
    /**
     * Sets the organization description filter for the query.
     * @param description organization description to search for
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setDescription(String description) { this.description = description;  return this; }

    /**
     * Gets the organization role type code.
     * @return role type code string or null if not set
     */
    public String getRoleType() { return type.getRoleType(); }
    
    /**
     * Sets the organization role type filter for the query.
     * @param roleType organization role type enum value
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setRoleType(OrgRoleType roleType) { this.type = roleType; return this; }

    /**
     * Gets the address city filter.
     * @return city name or null if not set
     */
    public String getAddressCity() { return addressCity; }
    
    /**
     * Sets the address city filter for the query.
     * @param addressCity city name to search for in organization addresses
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setAddressCity(String addressCity) { this.addressCity = addressCity; return this; }

    /**
     * Gets the address line 1 filter.
     * @return address line 1 or null if not set
     */
    public String getAddressLine1() { return addressLine1; }
    
    /**
     * Sets the address line 1 filter for the query.
     * @param addressLine1 first line of address to search for
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }

    /**
     * Checks if historical records should be included in the query results.
     * @return true if history is requested, false otherwise
     */
    public boolean isWithHistory() { return withHistory; }
    
    /**
     * Sets whether to include historical records in the query results.
     * @param withHistory true to include historical data, false to exclude
     * @return this instance for method chaining
     */
    public OrgQueryCriteriaParams setWithHistory(boolean withHistory) { this.withHistory = withHistory; return this; }

}
