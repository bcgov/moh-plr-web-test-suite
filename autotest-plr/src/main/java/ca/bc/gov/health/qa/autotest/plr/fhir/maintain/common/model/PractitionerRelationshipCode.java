package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model;

/**
 * Enumeration of practitioner relationship type codes used in OrganizationAffiliation
 * resources to describe relationships between organizations, practitioners, and facilities.
 * These codes are used in the 'code' element of OrganizationAffiliation resources.
 */
public enum PractitionerRelationshipCode {
    /** Locum relationship */
    LOC,
    /** Employer relationship */
    ER,
    /** Employee relationship */
    EE,
    /** Pharmacist relationship */
    PHCST,
    /** Pharmacy Manager relationship */
    PHMGR,
    /** Staff Pharmacist relationship */
    PHSTF,
    /** Other relationship type */
    OTHER,
    /** Owner Provider / Sponsor relationship */
    SPONSOR,
    /** Pharmacy relationship */
    PHARMACY,
    /** Operates the target */
    OPERATES,
    /** Operated by the target */
    OPERATED,
    /** Directs the target, e.g. Medical Director */
    DIRECTS,
    /** Directed by the target */
    DIRECTED,
    /** Works at relationship */
    WORKSAT,
    /** Work location relationship */
    WORKLOCATION,
    /** Manages the target */
    MANAGES,
    /** Managed by the target */
    MANAGEDBY;

    /**
     * Returns the code value as a string (the enum constant name).
     * @return code string
     */
    public String getCode() {
        return this.name();
    }

    /**
     * Resolves a relationship code enum from a code string.
     *
     * @param code the relationship code string
     * @return the matching {@code PractitionerRelationshipCode}, or null if not found
     */
    public static PractitionerRelationshipCode resolveCode(String code) {
        if (code == null) return null;
        for (PractitionerRelationshipCode c : PractitionerRelationshipCode.values()) {
            if (c.name().equalsIgnoreCase(code)) return c; // case-insensitive match
        }
        return null;
    }
}
