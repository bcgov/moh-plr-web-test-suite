package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model;

/**
 * TODO (AZ) - doc
 */
public enum IdentifierType
{
    /**
     * TODO (AZ) - doc
     */
    AOMDID("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-ambulance-director"),

    /**
     * TODO (AZ) - doc
     */
    CPN("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-plr-common-party-number"),

    /**
     * Physician ID Number (CPSID)
     */
    CPSID("https://fhir.infoway-inforoute.ca/NamingSystem/ca-bc-license-physician"),

    /**
     * Dentist ID Number (DENID)
     */
    DENID("https://fhir.infoway-inforoute.ca/NamingSystem/ca-bc-license-dentist"),

    /**
     * TODO (AZ) - doc
     */
    IFC("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-plr-ifc"),

    /**
     * TODO (AZ) - doc
     */
    IPC("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-plr-ipc"),

    /**
     * Out of Province Provider (OOPID)
     */
    OOPID("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-out-of-province-provider"),

    /**
     * TODO (AZ) - doc
     */
    ORGID("https://health.gov.bc.ca/fhir/NamingSystem/ca-bc-plr-org-id"),

    /**
     * TODO (AZ) - doc
     */
    RNID("https://fhir.infoway-inforoute.ca/NamingSystem/ca-bc-license-nurse");

    private final String sourceSystem_;

    private IdentifierType(String sourceSystem)
    {
        sourceSystem_ = sourceSystem;
    }

    /**
     * Returns the source system URI that identifies this type.
     *
     * @return source system URI string
     */
    public String getSourceSystem()
    {
        return sourceSystem_;
    }

    /**
     * Gets an {@link IdentifierType} enum constant matching the given system URI.
     *
     * @param system the system URI to match
     * @return matching {@link IdentifierType} or null if not found
     */
    public static IdentifierType resolveIdentifierType(String system) {
        for (IdentifierType t : IdentifierType.values()) {
            if (t.getSourceSystem().equals(system)) {
                return t;
            }
        }
        return null; // unknown system; skip
    }
}
