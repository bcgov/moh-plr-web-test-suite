package ca.bc.gov.health.qa.autotest.plr.fhir.model;

/**
 * Enumeration of supported end reason codes used in lifecycle extensions for affiliation / relationship
 * resources.
 */
public enum EndReasonCode {
    /**
     * TODO (AZ) - doc
     */
    CHANGE("CHG"),
    /**
     * TODO (AZ) - doc
     */
    CEASE("CEASE"),
    /**
     * TODO (AZ) - doc
     */
    CORRECTION("CORR");

    private final String wire;
    EndReasonCode(String wire){ this.wire = wire; }
    /**
     * Returns the wire-format code string expected by the FHIR server.
     * @return wire code
     */
    public String wire(){ return wire; }
}
