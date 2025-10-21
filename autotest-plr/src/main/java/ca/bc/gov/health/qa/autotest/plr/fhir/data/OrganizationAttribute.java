package ca.bc.gov.health.qa.autotest.plr.fhir.data;

/**
 * Enumeration of facility fields facility generation.
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
    /** Status (OPTIONAL) */
    STATUS(false),
    /** Note (OPTIONAL) */
    NOTE(false);

    private final boolean required;

    OrganizationAttribute(boolean required) { this.required = required; }

    public boolean isRequired() { return required; }
}