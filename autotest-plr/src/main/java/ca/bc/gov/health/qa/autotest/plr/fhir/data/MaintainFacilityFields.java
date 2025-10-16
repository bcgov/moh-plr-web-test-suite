package ca.bc.gov.health.qa.autotest.plr.fhir.data;

/**
 * Enumeration of facility fields that can be included during facility generation.
 * Optional fields are only included when explicitly requested.
 */
public enum MaintainFacilityFields {
    /** Facility name (REQUIRED - always generated) */
    NAME(true),
    
    /** Facility address (REQUIRED - always generated) */
    ADDRESS(true),
    
    /** Facility identifier (REQUIRED - always generated) */
    IDENTIFIER(true),
    
    /** Phone number telecom entry */
    PHONE(false),
    
    /** Email telecom entry */
    EMAIL(false),
    
    /** Fax telecom entry */
    FAX(false),
    
    /** Website URL telecom entry */
    WEBSITE(false),
    
    /** Notes */
    NOTES(false),
    
    /** Description/alias beyond the standard */
    DESCRIPTION(false);
    
    private final boolean required;
    
    /**
     * @param required true if this field is always required and cannot be disabled
     */
    MaintainFacilityFields(boolean required) {
        this.required = required;
    }
    
    /**
     * Returns whether this field is required and cannot be disabled.
     * @return true if required, false if optional
     */
    public boolean isRequired() {
        return required;
    }
    
}