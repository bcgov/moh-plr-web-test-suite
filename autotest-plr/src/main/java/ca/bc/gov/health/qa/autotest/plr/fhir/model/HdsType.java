package ca.bc.gov.health.qa.autotest.plr.fhir.model;

/**
 * Enumeration of Health Delivery Site (HDS) subtype classifications used only when
 * an organization's role type is {@link OrgRoleType#HDS}. The enum constant name is
 * serialized directly as the coding.code value in the specialized _type extension block.
 */
public enum HdsType {
    /**
     * TODO (AZ) - doc
     */
    CLINIC,
    /**
     * TODO (AZ) - doc
     */
    PHARMACY,
    /**
     * TODO (AZ) - doc
     */
    HOSPITAL,
    /**
     * TODO (AZ) - doc
     */
    EMERGENCY,
    /**
     * TODO (AZ) - doc
     */
    LAB,
    /**
     * TODO (AZ) - doc
     */
    GENERAL_CARE,
    /**
     * TODO (AZ) - doc
     */
    INPATIENT,
    /**
     * TODO (AZ) - doc
     */
    HOUSING,
    /**
     * TODO (AZ) - doc
     */
    OUTPATIENT;
}
