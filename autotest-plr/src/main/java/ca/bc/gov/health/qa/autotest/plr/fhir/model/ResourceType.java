package ca.bc.gov.health.qa.autotest.plr.fhir.model;

/**
 * Supported FHIR resource types for simplified query/maintain helpers.
 */
public enum ResourceType {
    PRACTITIONER("Practitioner"),
    ORGANIZATION("Organization"),
    FACILITY("Location");

    private final String wireName;
    ResourceType(String wireName){ this.wireName = wireName; }
    public String wire(){ return wireName; }
}
