package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query;

/**
 * Criteria container for Individual (Practitioner) $entityQuery parameters. Only non-null/non-blank
 * values will be sent. The {@code withHistory} flag is included when true.
 */
public class IndividualQueryCriteriaParams {
    private String role = null;
    private String addressCity = null;
    private String family = null;
    private String expertise = null;
    private String communication = null;
    private String given = null;
    private String statusReason = null;
    private String status = null;
    private String gender = null;
    private boolean withHistory = false;

    public IndividualQueryCriteriaParams() {}

    /**
     * Gets the practitioner role type filter.
     * @return role type code or null if not set
     */
    public String getRole() { return role; }
    
    /**
     * Sets the practitioner role type filter for the query.
     * @param role practitioner role type code (e.g., MD, RN, DEN)
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setRole(String role) { this.role = role; return this; }

    /**
     * Gets the address city filter.
     * @return city name or null if not set
     */
    public String getAddressCity() { return addressCity; }
    
    /**
     * Sets the address city filter for the query.
     * @param addressCity city name to search for in practitioner addresses
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setAddressCity(String addressCity) { this.addressCity = addressCity; return this; }

    /**
     * Gets the family name (last name) filter.
     * @return family name or null if not set
     */
    public String getFamily() { return family; }
    
    /**
     * Sets the family name filter for the query.
     * @param family family (last) name to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setFamily(String family) { this.family = family; return this; }

    /**
     * Gets the expertise code filter.
     * @return expertise code or null if not set
     */
    public String getExpertise() { return expertise; }
    
    /**
     * Sets the expertise code filter for the query.
     * @param expertise expertise/specialty code to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setExpertise(String expertise) { this.expertise = expertise; return this; }

    /**
     * Gets the communication language code filter.
     * @return communication code or null if not set
     */
    public String getCommunication() { return communication; }
    
    /**
     * Sets the communication language code filter for the query.
     * @param communication language code to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setCommunication(String communication) { this.communication = communication; return this; }

    /**
     * Gets the given name (first name) filter.
     * @return given name or null if not set
     */
    public String getGiven() { return given; }
    
    /**
     * Sets the given name filter for the query.
     * @param given given (first) name to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setGiven(String given) { this.given = given; return this; }

    /**
     * Gets the status reason code filter.
     * @return status reason code or null if not set
     */
    public String getStatusReason() { return statusReason; }
    
    /**
     * Sets the status reason code filter for the query.
     * @param statusReason status reason code to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setStatusReason(String statusReason) { this.statusReason = statusReason; return this; }

    /**
     * Gets the status code filter.
     * @return status code or null if not set
     */
    public String getStatus() { return status; }
    
    /**
     * Sets the status code filter for the query.
     * @param status status code to search for
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setStatus(String status) { this.status = status; return this; }

    /**
     * Gets the gender filter.
     * @return gender code or null if not set
     */
    public String getGender() { return gender; }
    
    /**
     * Sets the gender filter for the query.
     * @param gender gender code to search for (male, female, unknown)
     * @return this instance for method chaining
     */
    public IndividualQueryCriteriaParams setGender(String gender) { this.gender = gender; return this; }

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
    public IndividualQueryCriteriaParams setWithHistory(boolean withHistory) { this.withHistory = withHistory; return this; }
}
