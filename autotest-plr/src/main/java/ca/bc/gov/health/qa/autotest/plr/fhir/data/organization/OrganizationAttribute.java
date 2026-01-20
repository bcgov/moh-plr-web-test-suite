package ca.bc.gov.health.qa.autotest.plr.fhir.data.organization;

/**
 * Enumeration of Organization attributes used for maintain payload generation.
 * Optional fields are only included when explicitly requested.
 */
public enum OrganizationAttribute {
    /** Identifier (REQUIRED - always generated) */
    IDENTIFIER(true),
    /** Name (REQUIRED) */
    NAME(true),
    /** Role Type (REQUIRED) */
    ROLE_TYPE(true),
    /** Alias (OPTIONAL) */
    ALIAS(false),
    /** Confidentiality (OPTIONAL) */
    CONFIDENTIALITY(false),
    /** Address (REQUIRED) */
    ADDRESS(true),
    /** Telecom (OPTIONAL) */
    TELECOM(false),
    /** Status at least 1 is required*/
    STATUS(true),
    /** Note (OPTIONAL) */
    NOTE(false),

    // OrganizationProperties-derived optional attributes ---------------------------------------
    /** Clinic services model (OPTIONAL). */
    CLINIC_SERVICES(false),
    /** Clinic ownership/operation business type (OPTIONAL). */
    CLINIC_OWNER_BUSINESS_TYPE(false),
    /** Clinic/organization type classification (OPTIONAL). */
    CLINIC_TYPE(false),
    /** Legal business name of the clinic (OPTIONAL). */
    CLINIC_LEGAL_BUSINESS_NAME(false),
    /** Address unit values (list) (OPTIONAL). */
    ADDRESS_UNIT(false),
    /** Clinic hours of operation (list) (OPTIONAL). */
    CLINIC_HOURS_OF_OPERATION(false),
    /** Clinic owner names (list) (OPTIONAL). */
    CLINIC_OWNER_NAMES(false),
    /** Payee numbers (list) (OPTIONAL). */
    PAYEE_NUMBER(false),
    /** PCI flag indicator (OPTIONAL). */
    PCI_FLAG(false),
    /** Facility relationships (OPTIONAL). */
    FACILITY_RELATIONSHIPS(false);

    private final boolean required;

    /*
     * Constructor
     * @param required indicates if the attribute is required
     */
    OrganizationAttribute(boolean required) { this.required = required; }

    /**
     * Indicates whether the attribute is required for a minimally valid Organization maintain payload.
     * @return true if required, false if optional
     */
    public boolean isRequired() { return required; }
}