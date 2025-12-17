package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;

/**
 * Maps a facility query JSON response into a {@link MaintainFacilityBuilder}.
 *
 * Error and missed segments will be skipped. Validation of required fields will be
 * caught by MaintainFacilityBuilder.verifyParameters() on build().
 */
public final class FacilityQueryResponseMapper {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private FacilityQueryResponseMapper() {}

    /** Canonical extension URL for facility note wrapper. */
    private static final String NOTE_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-note-extension";

    /** Canonical extension URL that wraps the physical address (valueAddress). */
    private static final String PHYS_ADDRESS_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-physical-address-extension";

    /** Canonical extension URL that wraps all health service area values. */
    private static final String HEALTH_SERVICE_AREA_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-health-service-area-extension";

    /** Canonical extension URL that wraps the community health service area (name -> valueString) */
    private static final String COMMUNITY_HEALTH_AREA_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-community-health-area-extension";

    /** Canonical extension URL that wraps the primary care network (name -> valueString) */
    private static final String PRIMARY_CARE_NETWORK_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-primary-care-network-extension";

    /** Canonical extension URL that wraps the health service delivery area (valueString) */
    private static final String HEALTH_SERVICE_DELIVERY_AREA_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-health-service-delivery-area-extension";

    /** Canonical extension URL that wraps the local health area (value string) */
    private static final String LOCAL_HEALTH_AREA_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-local-health-area-extension";

    /** Canonical extension URL that wraps the health authority (value string) */
    private static final String HEALTH_AUTHORITY_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-facility-health-authority-extension";

    /** OrganizationAffiliation resourceType constant. */
    private static final String ORG_AFFILIATION_TYPE = "OrganizationAffiliation";
    
    /** Location resourceType constant. */
    private static final String LOCATION_TYPE = "Location";

    /**
     * Convert a facility query bundle into a MaintainFacilityBuilder.
     * @param facQueryBundle Complete JSON bundle returned by facility query
     * @return populated MaintainFacilityBuilder
     * @throws IllegalArgumentException if no Location resource found
     */
    public static MaintainFacilityBuilder fromQueryBundle(JSONObject facQueryBundle) {
        // The first entry with search.mode=="match" contains resource.entry[] collection bundle.
        JSONArray topEntries = facQueryBundle.optJSONArray("entry");

        JSONObject collectionBundle = null;

        //Find the entry that contains the facility values
        for (int i = 0; i < topEntries.length(); i++) {
            JSONObject entry = topEntries.optJSONObject(i);
            if (entry == null) continue;
            JSONObject search = entry.optJSONObject("search");
            if (search != null && "match".equals(search.optString("mode"))) {
                collectionBundle = entry.optJSONObject("resource");
                break;
            }
        }

        //error if query has not returned any matching facilities
        if (collectionBundle == null) {
            throw new IllegalArgumentException("No collection bundle (search.mode=match) found in facility query response");
        }

        JSONArray innerEntries = collectionBundle.optJSONArray("entry");
        if (innerEntries == null) {
            throw new IllegalArgumentException("Inner collection bundle missing entry array");
        }

        JSONObject facilityResource = null;
        // Collect affiliation resources for relationships and the facility resource.
        JSONArray affiliationArray = new JSONArray();
        for (int i = 0; i < innerEntries.length(); i++) {
            JSONObject inner = innerEntries.optJSONObject(i);
            if (inner == null) continue;
            JSONObject resource = inner.optJSONObject("resource");
            if (resource == null) continue;
            String type = resource.optString("resourceType", "");
            if (LOCATION_TYPE.equals(type) && facilityResource == null) {
                facilityResource = resource; // first location only
            } else if (ORG_AFFILIATION_TYPE.equals(type)) {
                affiliationArray.put(resource);
            }
        }

        if (facilityResource == null) {
            throw new IllegalArgumentException("No Location resource present in facility query bundle");
        }

        MaintainFacilityBuilder builder = new MaintainFacilityBuilder();
        mapIdentifier(facilityResource, builder);
        mapNameAndDesc(facilityResource, builder);
        mapAddress(facilityResource, builder);
        mapTelecom(facilityResource, builder);
        mapNotes(facilityResource, builder);
        mapOrganizationRelationships(affiliationArray, builder);
        return builder;
    }

    /*
     * Maps the identifier from the Location resource to the Facility builder.
     * @param location Facility resource JSON
     * @param b MaintainFacilityBuilder to populate
     */
    private static void mapIdentifier(JSONObject location, MaintainFacilityBuilder b) {
        JSONArray identifiers = location.optJSONArray("identifier");
        if (identifiers != null && !identifiers.isEmpty()) {
            JSONObject id = identifiers.optJSONObject(0);
            if (id != null) {
                String value = id.optString("value", null);
                if (value != null && !value.isEmpty()) {
                    b.identifier(value);
                }
            }
        }
    }

    /*
     * Maps the name and description from the Location resource to the Facility builder.
     * @param location Facility resource JSON
     * @param b MaintainFacilityBuilder to populate
     */
    private static void mapNameAndDesc(JSONObject location, MaintainFacilityBuilder b) {
        String name = location.optString("name", null);
        if (name != null && !name.isEmpty()) {
            b.name(name);
        }

        JSONArray alias = location.optJSONArray("alias");
        if (alias != null && alias.length() > 0) {
            String desc = alias.optString(0, null);
            if (desc != null && !desc.isEmpty()) {
                b.description(desc);
            }
        }
    }

    /*
     * Maps the address from the Location resource to the Facility builder.
     * @param location Facility resource JSON
     * @param b MaintainFacilityBuilder to populate
     */
    private static void mapAddress(JSONObject location, MaintainFacilityBuilder b) {
    
        JSONArray extensions = location.optJSONArray("extension");
        JSONObject valueAddress = null;
        JSONArray hsdaInfo = null;
        if (extensions != null) {
            for (int i = 0; i < extensions.length(); i++) {
                JSONObject ext = extensions.optJSONObject(i);
                if (ext == null) continue;
                if (PHYS_ADDRESS_EXTENSION_URL.equals(ext.optString("url"))) {
                    valueAddress = ext.optJSONObject("valueAddress");
                }
                if (HEALTH_SERVICE_AREA_URL.equals(ext.optString("url"))) {
                    hsdaInfo = ext.optJSONArray("extension");
                }
            }

        }

        if (valueAddress != null) {
            JSONArray lines = valueAddress.optJSONArray("line");
            String line1 = lines != null && lines.length() > 0 ? lines.optString(0, null) : valueAddress.optString("text", null);
            String city = valueAddress.optString("city", null);
            String postal = valueAddress.optString("postalCode", null);
            if (line1 != null && city != null) {
                b.addAddress(line1, city, postal);
            }
        }

        // parse HSDA values to add to builder
        String chsaString = "";
        String pcnString = "";
        String hsdaString = "";
        String lhaString = "";
        String haString = "";
        if (hsdaInfo != null) {
            for (int i = 0; i < hsdaInfo.length(); i++) {
                JSONObject ext = hsdaInfo.optJSONObject(i);
                if (ext == null) continue;
                if (COMMUNITY_HEALTH_AREA_URL.equals(ext.optString("url"))) {
                    for (int j = 0; j < ext.optJSONArray("extension").length(); j++) {
                        if (ext.optJSONArray("extension").optJSONObject(j).get("url").equals("name")) {
                            chsaString = ext.optJSONArray("extension").optJSONObject(j).optString("valueString", "");
                        }
                    }
                }
                if (PRIMARY_CARE_NETWORK_URL.equals(ext.optString("url"))) {
                    for (int j = 0; j < ext.optJSONArray("extension").length(); j++) {
                        if (ext.optJSONArray("extension").optJSONObject(j).get("url").equals("name")) {
                            pcnString = ext.optJSONArray("extension").optJSONObject(j).optString("valueString", "");
                        }
                    }
                }
                if (HEALTH_SERVICE_DELIVERY_AREA_URL.equals(ext.optString("url"))) { hsdaString = ext.optString("valueString", ""); }
                if (LOCAL_HEALTH_AREA_URL.equals(ext.optString("url"))) { lhaString = ext.optString("valueString", ""); }
                if (HEALTH_AUTHORITY_URL.equals(ext.optString("url"))) { haString = ext.optString("valueString", ""); }
            }
            b.addHSDA(chsaString, pcnString, hsdaString, lhaString, haString);
        }
    }

    /*
     * Maps the telecom values from the Location resource to the Facility builder.
     * @param location Facility resource JSON
     * @param b MaintainFacilityBuilder to populate
     */
    private static void mapTelecom(JSONObject location, MaintainFacilityBuilder b) {
        JSONArray telecom = location.optJSONArray("telecom");
        if (telecom == null) return;
        for (int i = 0; i < telecom.length(); i++) {
            JSONObject t = telecom.optJSONObject(i);
            if (t == null) continue;
            String system = t.optString("system", null); // builder expects 'type'
            String value  = t.optString("value", null);
            if (system != null && value != null && !system.isEmpty() && !value.isEmpty()) {
                b.addTelecom(system, value);
            }
        }
    }

    /*
     * Maps the notes from the Location resource to the Facility builder.
     * @param location Facility resource JSON
     * @param b MaintainFacilityBuilder to populate
     */
    private static void mapNotes(JSONObject location, MaintainFacilityBuilder b) {
        JSONArray extensions = location.optJSONArray("extension");
        if (extensions == null) return;
        for (int i = 0; i < extensions.length(); i++) {
            JSONObject ext = extensions.optJSONObject(i);
            if (ext == null) continue;
            if (NOTE_EXTENSION_URL.equals(ext.optString("url"))) {
                JSONArray nested = ext.optJSONArray("extension");
                if (nested == null) continue;
                for (int j = 0; j < nested.length(); j++) {
                    JSONObject inner = nested.optJSONObject(j);
                    if (inner == null) continue;
                    if ("text".equals(inner.optString("url"))) {
                        String noteText = inner.optString("valueString", null);
                        if (noteText != null && !noteText.isEmpty()) {
                            b.addNote(noteText);
                        }
                    }
                }
            }
        }
    }

    /*
     * Maps the OrganizationRelationships from the Location resource to the Facility builder.
     * Does not include Org Name in the map currently, use queryOrgByIdentifier to find based on identifier if needed
     *
     * @param affiliationArray Array containing OrganizationAffiliation resources
     * @param b MaintainFacilityBuilder to populate
     *
     */
    private static void mapOrganizationRelationships(JSONArray affiliationArray, MaintainFacilityBuilder b) {
        if (affiliationArray == null) return;
        for (int i = 0; i < affiliationArray.length(); i++) {
            JSONObject aff = affiliationArray.optJSONObject(i);

            JSONObject org = aff.optJSONObject("organization");
            JSONObject identifier = org.optJSONObject("identifier");

            String system    = identifier.optString("system", null);
            String idValue   = identifier.optString("value", null);
            if (system == null || idValue == null || idValue.isEmpty()) continue;
            IdentifierType idType = IdentifierType.resolveIdentifierType(system);
            if (idType != null) {
                b.addOrganizationRelationship(idType, idValue, null);
            }
        }
    }
}
