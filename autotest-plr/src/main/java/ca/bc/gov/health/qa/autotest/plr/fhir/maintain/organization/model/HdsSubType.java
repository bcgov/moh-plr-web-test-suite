package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

/**
 * Enumeration of Health Delivery Site (HDS) subtype classifications used only when
 * an organization's role type is {@link OrgRoleType#HDS}. The enum constant name is
 * serialized directly as the coding.code value in the specialized _type extension block.
 */
public enum HdsSubType {
    /** Adult Residential Care Homes. */
    BHACH("BHACH - Adult Residential Care Homes"),
    /** Assisted Living Facilities. */
    BHASL("BHASL - Assisted Living Facilities"),
    /** Community. */
    CMN("CMN - Community"),
    /** Community Telepharmacy. */
    CTPY("CTPY - Community Telepharmacy"),
    /** Education. */
    EDU("EDU - Education"),
    /** Hospital. */
    HOS("HOS - Hospital"),
    /** Intensive Care. */
    LDAIC("LDAIC - Intensive Care"),
    /** Emergency Medical Care. */
    LDEMC("LDEMC - Emergency Medical Care"),
    /** Emergency Room Care. */
    LDERC("LDERC - Emergency Room Care"),
    /** Neonatal Intensive Care. */
    LDNIC("LDNIC - Neonatal Intensive Care"),
    /** Pediatric Intensive Care. */
    LDPIC("LDPIC - Pediatric Intensive Care"),
    /** Trauma Care. */
    LDTRC("LDTRC - Trauma Care"),
    /** General Medical Care. */
    LEGMC("LEGMC - General Medical Care"),
    /** Inpatient Health Facilities. */
    LLIHF("LLIHF - Inpatient Health Facilities"),
    /** Nursing Facilities. */
    LLNRF("LLNRF - Nursing Facilities"),
    /** Community Health Centres. */
    LNCHC("LNCHC - Community Health Centres"),
    /** Indigenous Health Facilities. */
    LNIHF("LNIHF - Indigenous Health Facilities"),
    /** Mobile Health Care. */
    LNMHC("LNMHC - Mobile Health Care"),
    /** Outpatient Health Facilities. */
    LNOHF("LNOHF - Outpatient Health Facilities"),
    /** Urgent Care Centres. */
    LNUCC("LNUCC - Urgent Care Centres"),
    /** Women's Health Centres. */
    LNWHC("LNWHC - Women's Health Centres"),
    /** Walk In Medical Clinics. */
    LNWIC("LNWIC - Walk In Medical Clinics"),
    /** Satellite. */
    SATE("SATE - Satellite"),
    /** Telepharmacy. */
    TELE("TELE - Telepharmacy");

    private final String text;
    HdsSubType(String text) { this.text = text; }
    /**
     * Returns the exact label used in UI/forms for this enum value.
     * @return user-facing label
     */
    public String getText() { return text; }
}
