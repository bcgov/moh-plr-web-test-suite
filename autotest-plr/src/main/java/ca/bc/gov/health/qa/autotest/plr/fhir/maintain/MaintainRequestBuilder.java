package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import org.json.JSONObject;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;

/**
 * Common contract for all maintain request builders.
 * Implementations provide the FHIR resource type and the JSON payload.
 */
public interface MaintainRequestBuilder {

    /*
     * TODO (AZ) - doc
     */
    JSONObject build();

    /*
     * TODO (AZ) - doc
     */
    PlrFhirResourceType resourceType();

    /*
     * TODO (AZ) - doc
     */
    //void verifyParameters();
}
