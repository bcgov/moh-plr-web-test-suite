package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model;

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
    DEN("DEN"),
    /** Medical Doctor */
    MD("MD"),
    /** Registered Nurse */
    RN("RN"),
    /** Registered Nurse Practitioner */
    RNP("RNP"),
    /** Optometrist */
    OPT("OPT"),
    /** Registered Psychiatric Nurse */
    RPN("RPN"),
    /** Licensed Practical Nurse */
    LPN("LPN"),
    /** Registered Midwife */
    RM("RM"),
    /** Pharmacist */
    PHARM("PHARM"),
    /** Podiatrist */
    PO("PO"),
    /** Health Authority */
    HA("HA"),
    
    // Out-of-Province (OOP) practitioner role types
    /** OOP Medical Doctor */
    OOP_MD("OOP-MD"),
    /** OOP Dentist */
    OOP_DEN("OOP-DEN"),
    /** OOP Registered Nurse */
    OOP_RN("OOP-RN"),
    /** OOP Registered Nurse Practitioner */
    OOP_RNP("OOP-RNP"),
    /** OOP Pharmacist */
    OOP_PHARM("OOP-PHARM"),
    /** OOP Optometrist */
    OOP_OPT("OOP-OPT"),
    /** OOP Registered Midwife */
    OOP_RM("OOP-RM"),
    /** OOP Naturopathic Doctor */
    OOP_ND("OOP-ND"),
    /** OOP Social Worker */
    OOP_SW("OOP-SW"),
    /** OOP Recreation Therapist */
    OOP_RECT("OOP-RECT"),
    /** OOP Audiologist */
    OOP_AUD("OOP-AUD"),
    /** OOP Respiratory Therapist */
    OOP_RT("OOP-RT"),
    /** OOP Occupational Therapist */
    OOP_OT("OOP-OT"),
    /** OOP Registered Dietician */
    OOP_RD("OOP-RD"),
    /** OOP Speech Language Pathologist */
    OOP_SLP("OOP-SLP"),
    /** OOP Registered Clinical Counsellor */
    OOP_CC("OOP-CC"),
    /** OOP Podiatrist */
    OOP_PO("OOP-PO"),
    /** OOP Physical Therapist */
    OOP_PT("OOP-PT"),
    /** OOP Psychologist */
    OOP_PSYCH("OOP-PSYCH"),
    /** OOP Chiropractor */
    OOP_CHIRO("OOP-CHIRO"),
    /** OOP Vocational Counsellor */
    OOP_VC("OOP-VC");

    private final String roleType_;

    IndividualRoleType(String roleType)
    {
        roleType_ = roleType;
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
