package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

/**
 * Enumerates the type of services a clinic provides.
 * Values mirror the UI labels exposed via {@link #getText()}.
 */
public enum ClinicServices {
    /** Episodic care services. */
    EPISODIC("Episodic"),
    /** Longitudinal care services. */
    LONGITUDINAL("Longitudinal"),
    /** Mixed model services. */
    MIXED("Mixed");

    private final String text;
    ClinicServices(String text) { this.text = text; }
    /**
     * Returns the exact label used in UI/forms for this enum value.
     * @return user-facing label
     */
    public String getText() { return text; }
}
