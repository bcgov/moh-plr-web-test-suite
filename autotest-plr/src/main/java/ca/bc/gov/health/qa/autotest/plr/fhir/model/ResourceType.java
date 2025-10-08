package ca.bc.gov.health.qa.autotest.plr.fhir.model;

/**
 * Supported FHIR resource types for simplified query/maintain helpers.
 */
public enum ResourceType {
    /** Practitioner resource (FHIR Practitioner). */
    PRACTITIONER("Practitioner"),
    /** Organization resource (FHIR Organization). */
    ORGANIZATION("Organization"),
    /** Facility abstraction mapped to FHIR Location. */
    FACILITY("Location");

    private final String wireName;
    ResourceType(String wireName){ this.wireName = wireName; }
    /**
     * Returns the canonical FHIR resource type name used on the wire (JSON payload / REST path).
     * @return wire-format resource type name
     */
    public String wire(){ return wireName; }
}
