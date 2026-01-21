package ca.bc.gov.health.qa.autotest.plr.fhir.data.individual;

/**
 * Attributes for Practitioner maintain requests. Each enum value can be marked
 * as required to enable central validation in the builder prior to request
 * materialization.
 */
public enum IndividualAttribute {
    /** At least one identifier value */
    IDENTIFIER(true),
    /** Family (last) name */
    FAMILY_NAME(true),
    /** Given names array [first, middle, third] determine if at least a name will be required*/
    NAMES(true),
    /** At least one postal/physical address */
    ADDRESS(true),
    /** At least one telecom entry */
    TELECOM(false),
    /** At least one status entry */
    STATUS(true),
    /** At least one free-form note */
    NOTE(false),
    /** Role type code */
    ROLE_TYPE(true),
    /** Demographics map (DOB, DOD, birth country/province, gender) */
    DEMOGRAPHICS(true),
    /** Expertise entries */
    EXPERTISE(false),
    /** Credential/qualification entries */
    CREDENTIAL(false),
    /** Disciplinary action entries */
    DISCIPLINARY_ACTION(false),
    /** Condition/restriction entries */
    CONDITION(false),
    /** Confidentiality flag */
    CONFIDENTIALITY(false),
    /** Organization relationships (individual-to-organization) */
    ORGANIZATION_RELATIONSHIPS(false),
    /** Individual relationships (individual-to-individual) */
    INDIVIDUAL_RELATIONSHIPS(false);

    private final boolean required;

    IndividualAttribute(boolean required) {
        this.required = required;
        }

    /**
     * Indicates if this attribute is required for a valid practitioner request.
     * @return true if required, false otherwise
     */
    public boolean isRequired() { return required; }
}
