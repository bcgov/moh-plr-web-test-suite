package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import org.json.JSONObject;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;

/**
 * Common contract for all maintain request builders.
 * Implementations provide the FHIR resource type and the JSON payload.
 */
public interface MaintainRequestBuilder {

    /**
     * Builds the maintain Bundle JSON payload representing the configured resource state.
     * Implementations typically perform required-field validation prior to returning.
     * @return immutable JSON object ready for submission
     */
    JSONObject build();

    /**
     * Returns the PLR FHIR resource type represented by this builder.
     * @return resource type enum
     */
    PlrFhirResourceType resourceType();

    /*
     * TODO (AZ) - doc
     */
    //void verifyParameters();
}
