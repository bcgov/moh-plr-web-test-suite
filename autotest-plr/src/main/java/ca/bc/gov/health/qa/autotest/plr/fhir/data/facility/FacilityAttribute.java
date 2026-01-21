package ca.bc.gov.health.qa.autotest.plr.fhir.data.facility;

/**
 * Enumeration of facility fields facility generation.
 * Optional fields are only included when explicitly requested.
 */
public enum FacilityAttribute {
    /** Facility name (REQUIRED - always generated) */
    NAME(true),
    
    /** Facility address (REQUIRED - always generated) */
    ADDRESS(true),
    
    /** Facility identifier (REQUIRED - always generated) */
    IDENTIFIER(true),
    
    /** Phone number telecom entry */
    PHONE(false),
    
    /** Mobile telecom entry */
    MOBILE(false),

    /** Pager telecom entry */
    PAGER(false),

    /** Modem telecom entry */
    MODEM(false),

    /** Fax telecom entry */
    FAX(false),

    /** Email telecom entry */
    EMAIL(false),
    
    /** Website URL telecom entry */
    WEBSITE(false),

    /** FTP telecom entry */
    FTP(false),

    /** Description/alias beyond the standard */
    DESCRIPTION(false),

    /** Note/annotation about the facility */
    NOTE(false),

    /** Organization relationship */
    ORG_RELATIONSHIP(false);

    
    
    private final boolean required;
    
    /**
     * @param required true if this field is always required and cannot be disabled
     */
    FacilityAttribute(boolean required) {
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