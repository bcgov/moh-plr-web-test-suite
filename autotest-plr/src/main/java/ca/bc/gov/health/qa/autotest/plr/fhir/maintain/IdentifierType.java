package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

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
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getSourceSystem()
    {
        return sourceSystem_;
    }
}
