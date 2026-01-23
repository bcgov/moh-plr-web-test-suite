package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;

/**
 * Enumeration of PLR Individual (Practitioner) role type codes used when composing FHIR Practitioner
 * resources in maintain requests. The enum constant name matches the conceptual role while
 * the associated code value (returned by {@link #getRoleType()}) is the literal stored in
 * the outbound JSON.
 * 
 * <p>Role types are organized into two categories:
 * <ul>
 *   <li>BC-registered practitioners (MD, RN, DEN, etc.)</li>
 *   <li>Out-of-Province (OOP) practitioners with OOP- prefix</li>
 * </ul>
 */
public enum IndividualRoleType {
    // BC-registered practitioner role types
    /** Dentist */
    DEN("DEN", IdentifierType.DENID),
    /** Medical Doctor */
    MD("MD", IdentifierType.MPID),
    /** Registered Nurse */
    RN("RN", IdentifierType.RNID),
    /** Registered Nurse Practitioner */
    RNP("RNP", IdentifierType.RNID),
    /** Optometrist */
    OPT("OPT", IdentifierType.OPTID),
    /** Registered Psychiatric Nurse */
    RPN("RPN", IdentifierType.RNID),
    /** Licensed Practical Nurse */
    LPN("LPN", IdentifierType.RNID),
    /** Registered Midwife */
    RM("RM", IdentifierType.RMID),
    /** Pharmacist */
    PHARM("PHARM", IdentifierType.PHID),
    /** Podiatrist */
    PO("PO", IdentifierType.POID),
    /** Health Authority */
    HA("HA", IdentifierType.HAID),
    
    // Out-of-Province (OOP) practitioner role types
    /** OOP Medical Doctor */
    OOP_MD("OOP-MD", IdentifierType.OOPID),
    /** OOP Dentist */
    OOP_DEN("OOP-DEN", IdentifierType.OOPID),
    /** OOP Registered Nurse */
    OOP_RN("OOP-RN", IdentifierType.OOPID),
    /** OOP Registered Nurse Practitioner */
    OOP_RNP("OOP-RNP", IdentifierType.OOPID),
    /** OOP Pharmacist */
    OOP_PHARM("OOP-PHARM", IdentifierType.OOPID),
    /** OOP Optometrist */
    OOP_OPT("OOP-OPT", IdentifierType.OOPID),
    /** OOP Registered Midwife */
    OOP_RM("OOP-RM", IdentifierType.OOPID),
    /** OOP Naturopathic Doctor */
    OOP_ND("OOP-ND", IdentifierType.OOPID),
    /** OOP Social Worker */
    OOP_SW("OOP-SW", IdentifierType.OOPID),
    /** OOP Recreation Therapist */
    OOP_RECT("OOP-RECT", IdentifierType.OOPID),
    /** OOP Audiologist */
    OOP_AUD("OOP-AUD", IdentifierType.OOPID),
    /** OOP Respiratory Therapist */
    OOP_RT("OOP-RT", IdentifierType.OOPID),
    /** OOP Occupational Therapist */
    OOP_OT("OOP-OT", IdentifierType.OOPID),
    /** OOP Registered Dietician */
    OOP_RD("OOP-RD", IdentifierType.OOPID),
    /** OOP Speech Language Pathologist */
    OOP_SLP("OOP-SLP", IdentifierType.OOPID),
    /** OOP Registered Clinical Counsellor */
    OOP_CC("OOP-CC", IdentifierType.OOPID),
    /** OOP Podiatrist */
    OOP_PO("OOP-PO", IdentifierType.OOPID),
    /** OOP Physical Therapist */
    OOP_PT("OOP-PT", IdentifierType.OOPID),
    /** OOP Psychologist */
    OOP_PSYCH("OOP-PSYCH", IdentifierType.OOPID),
    /** OOP Chiropractor */
    OOP_CHIRO("OOP-CHIRO", IdentifierType.OOPID),
    /** OOP Vocational Counsellor */
    OOP_VC("OOP-VC", IdentifierType.OOPID);

    private final String roleType_;
    private final IdentifierType identifierType_;

    IndividualRoleType(String roleType, IdentifierType identifierType)
    {
        roleType_ = roleType;
        identifierType_ = identifierType;
    }

    /**
     * Returns the code value to emit in FHIR PractitionerRole.code coding for this role.
     * @return role type code literal
     */
    public String getRoleType()
    {
        return roleType_;
    }

    /**
     * Returns the identifier type associated with this role type.
     * @return identifier type for this role
     */
    public IdentifierType getIdentifierType()
    {
        return identifierType_;
    }

    /**
     * Resolves an individual role type from a code string.
     *
     * @param code the role code
     * @return the matching {@code IndividualRoleType}, or null if not found
     */
    public static IndividualRoleType resolveRoleType(String code) {
        if (code == null) return null;
        for (IndividualRoleType r : IndividualRoleType.values()) {
            if (r.getRoleType().equalsIgnoreCase(code)) return r;
        }
        return null;
    }
}
