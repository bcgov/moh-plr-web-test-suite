package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

/**
 * Primary clinic/organization type classification.
 * Values mirror the UI labels via {@link #getText()}.
 */
public enum ClinicType {
    /** Community Health Centre. */
    COMMUNITY_HEALTH_CENTRE_CHC("Community Health Centre (CHC)"),
    /** First Nations Primary Health Care Clinic. */
    FIRST_NATIONS_PRIMARY_HEALTH_CARE_CLINIC_FNPCI("First Nations Primary Health Care Clinic (FNPCI)"),
    /** Health Care Practitioner Office (including NPPCC). */
    HEALTH_CARE_PRACTITIONER_OFFICE_INCL_NPPCC("Health Care Practitioner Office (incl. NPPCC)"),
    /** Hybrid primary care practice. */
    HYBRID_PRIMARY_CARE_PRACTICE("Hybrid Primary Care Practice"),
    /** Longitudinal primary care practice. */
    LONGITUDINAL_PRIMARY_CARE_PRACTICE("Longitudinal Primary Care Practice"),
    /** Urgent Primary Care Centre. */
    URGENT_PRIMARY_CARE_CENTRE_UPCC("Urgent Primary Care Centre (UPCC)"),
    /** Walk-in / episodic care clinic. */
    WALK_IN_EPISODIC_CARE_CLINIC("Walk-in/Episodic Care Clinic");

    private final String text;
    ClinicType(String text) { this.text = text; }
    /**
     * Returns the exact label used in UI/forms for this enum value.
     * @return user-facing label
     */
    public String getText() { return text; }
}
