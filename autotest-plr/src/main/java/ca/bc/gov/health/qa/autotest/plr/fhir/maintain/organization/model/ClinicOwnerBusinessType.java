package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

/**
 * Business ownership/operation types for a clinic/organization.
 * Values match UI labels exposed via {@link #getText()}.
 */
public enum ClinicOwnerBusinessType {
    /** First Nation Health Authority owned/operated. */
    FIRST_NATION_HEALTH_AUTHORITY_OWNED_OPERATED("First Nation Health Authority Owned / Operated"),
    /** Health Authority owned/operated. */
    HEALTH_AUTHORITY_OWNED_OPERATED("Health Authority Owned / Operated"),
    /** Owned/operated by a non-physician corporation. */
    NON_PHYSICIAN_CORPORATION("Non-Physician Corporation"),
    /** Registered non-profit society. */
    NON_PROFIT_SOCIETY("Non-Profit Society"),
    /** Other business ownership type. */
    OTHER("Other"),
    /** Physician owned/operated. */
    PHYSICIAN_OWNED_OPERATED("Physician Owned / Operated");

    private final String text;
    ClinicOwnerBusinessType(String text) { this.text = text; }
    /**
     * Returns the exact label used in UI/forms for this enum value.
     * @return user-facing label
     */
    public String getText() { return text; }
}
