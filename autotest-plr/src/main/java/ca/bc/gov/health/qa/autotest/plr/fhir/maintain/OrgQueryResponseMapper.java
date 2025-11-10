package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;

/**
 * Maps a organization query JSON response into a {@link MaintainOrgBuilder}.
 *
 * Error and missed segments will be skipped. Validation of required fields will be
 * caught by MaintainOrgBuilder.verifyParameters() on build().
 */
public final class OrgQueryResponseMapper {

	private OrgQueryResponseMapper() {}

	/** Canonical extension URL for organization note wrapper. */
	private static final String NOTE_EXTENSION_URL           = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-note-extension";
    /** Canonical extension URL for organization status wrapper */
    private static final String STATUS_EXTENSION_URL         = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-license-status-extension";
    /** Canonical extension URL for organization communication purpose wrapper */
    private static final String COMM_PURPOSE_URL             = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-communication-purpose-code-extension";

	/**
     * Convert a organization query bundle into a MaintainOrgBuilder.
     * @param orgQueryBundle Complete JSON bundle returned by organization query
     * @return populated MaintainOrgBuilder
     * @throws IllegalArgumentException if no Organization resource found
     */
	public static MaintainOrgBuilder fromQueryBundle(JSONObject orgQueryBundle) {
		JSONArray topEntries = orgQueryBundle.optJSONArray("entry");
		if (topEntries == null) {
			throw new IllegalArgumentException("No entries in organization query bundle");
		}
		JSONObject collectionBundle = null;
		for (int i = 0; i < topEntries.length(); i++) {
			JSONObject entry = topEntries.optJSONObject(i);
			if (entry == null) continue;
			JSONObject search = entry.optJSONObject("search");
			if (search != null && "match".equals(search.optString("mode"))) {
				collectionBundle = entry.optJSONObject("resource");
				break;
			}
		}

        //error if query has not returned any matching organizations
		if (collectionBundle == null) {
			throw new IllegalArgumentException("No collection bundle (search.mode=match) found in organization query response");
		}

		JSONArray innerEntries = collectionBundle.optJSONArray("entry");
		if (innerEntries == null) {
			throw new IllegalArgumentException("Inner collection bundle missing entry array");
		}

		JSONObject orgResource = null;
		for (int i = 0; i < innerEntries.length(); i++) {
			JSONObject inner = innerEntries.optJSONObject(i);
			if (inner == null) continue;
			JSONObject resource = inner.optJSONObject("resource");
			if (resource == null) continue;
			if ("Organization".equals(resource.optString("resourceType"))) {
				orgResource = resource;
				break;
			}
		}

		if (orgResource == null) {
			throw new IllegalArgumentException("No Organization resource found in query bundle");
		}

		MaintainOrgBuilder builder = new MaintainOrgBuilder();
		mapIdentifier(orgResource, builder);
		mapNameAndAlias(orgResource, builder);
		mapRoleType(orgResource, builder);
		if (builder.getRoleType() == OrgRoleType.HDS) {
			mapHdsType(orgResource, builder);
		}
		mapAddresses(orgResource, builder);
		mapTelecom(orgResource, builder);
		mapStatus(orgResource, builder);
		mapNotes(orgResource, builder);

		return builder;
	}

    /*
     * Maps the identifier from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapIdentifier(JSONObject org, MaintainOrgBuilder b) {
		JSONArray identifiers = org.optJSONArray("identifier");

		if (identifiers == null || identifiers.length() == 0) return;
        
		String ipcValue = null;
		String fallbackValue = null;
		for (int i = 0; i < identifiers.length(); i++) {
			JSONObject id = identifiers.optJSONObject(i);

			String system = id.optString("system", null);
			String value = id.optString("value", null);

			if (IdentifierType.IPC.getSourceSystem().equals(system)) {
				ipcValue = value;
				break; // prefer first IPC encountered to match logic in FHIRController
			}
			if (fallbackValue == null) fallbackValue = value; // first non-IPC identifier
		}
		String chosen = ipcValue != null ? ipcValue : fallbackValue;
		if (chosen != null) b.identifier(chosen);
	}

    /*
     * Maps the name and alias from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapNameAndAlias(JSONObject org, MaintainOrgBuilder b) {

		String name = org.optString("name", null);
		if (name != null && !name.isEmpty()) {
			b.name(name);
		}

		JSONArray alias = org.optJSONArray("alias");
		if (alias != null && alias.length() > 0) {
			String a = alias.optString(0, null);
			if (a != null && !a.isEmpty()) {
				b.alias(a);
				return;
			}
		}
	}

    /*
     * Maps the roletype from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapRoleType(JSONObject org, MaintainOrgBuilder b) {
		JSONArray typeArr = org.optJSONArray("type");
		if (typeArr == null || typeArr.length() == 0) return; 
        
		JSONObject codeObj = typeArr.getJSONObject(0)
			.getJSONArray("coding")
			.getJSONObject(0);

		OrgRoleType role = OrgRoleType.resolveRoleType(codeObj.optString("code", null));
		if (role != null) b.roleType(role);
	}

    /*
     * Maps the hdstype from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapHdsType(JSONObject org, MaintainOrgBuilder b) {
		// Only invoked if role already set to HDS
		JSONArray typeArr = org.optJSONArray("type");
		if (typeArr == null || typeArr.length() == 0) return;

		JSONObject ext = typeArr.getJSONObject(0)
            .getJSONArray("extension")
            .getJSONObject(0);

		JSONArray nested = ext.getJSONObject("valueExtension").getJSONArray("extension");
		for (int i = 0; i < nested.length(); i++) {

			JSONObject inner = nested.getJSONObject(i);

			if (!"hdsType".equals(inner.optString("url"))) continue;
			String type = extractCodingCode(inner, "valueCodeableConcept");

			HdsType hdsType = HdsType.resolveHdsType(type);
			
            if (hdsType != null) {
				try { b.hdsType(hdsType); } catch (Exception ignored) {}
			}
			return; // done after hdsType found
		}
	}

    /*
     * Maps the address from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapAddresses(JSONObject org, MaintainOrgBuilder b) {
		JSONArray addresses = org.optJSONArray("address");

		if (addresses == null) return;

		for (int i = 0; i < addresses.length(); i++) {
			JSONObject addr = addresses.optJSONObject(i);

			JSONArray lines = addr.optJSONArray("line");
            //only saving line 1 for now.
			String line1 = lines.optString(0, null);
			String city = addr.optString("city", null);
			String postal = addr.optString("postalCode", null);
			String type = addr.optString("type", "physical");
			String purpose = null;
			JSONArray ext = addr.optJSONArray("extension");

			if (ext != null) {

				for (int j = 0; j < ext.length(); j++) {
					JSONObject e = ext.optJSONObject(j);

					if (e == null) continue;

					if (COMM_PURPOSE_URL.equals(e.optString("url"))) {
						purpose = extractCodingCode(e, "valueCodeableConcept");
					}
				}
			}

            // postalCode is not a required attribute and can be null.
			if (line1 != null && city != null) {
				b.addAddress(type, purpose, line1, city, postal);
			}
		}
	}

    /*
     * Maps the telecoms from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapTelecom(JSONObject org, MaintainOrgBuilder b) {
		JSONArray telecom = org.optJSONArray("telecom");

		if (telecom == null) return;
        
		for (int i = 0; i < telecom.length(); i++) {
			JSONObject t = telecom.optJSONObject(i);

			if (t == null) continue;

			String system = t.optString("system", null);
			String value = t.optString("value", null);
			String purpose = null;
			JSONArray ext = t.optJSONArray("extension");

			if (ext != null) {

				for (int j = 0; j < ext.length(); j++) {
					JSONObject e = ext.optJSONObject(j);

					if (e == null) continue;

					if (COMM_PURPOSE_URL.equals(e.optString("url"))) {
						purpose = extractCodingCode(e, "valueCodeableConcept");
					}
				}
			}
			if (system != null && value != null && !system.isEmpty() && !value.isEmpty()) {
				b.addTelecom(system, purpose, value);
			}
		}
	}

    /*
     * Maps the status from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapStatus(JSONObject org, MaintainOrgBuilder b) {

		JSONArray extensions = org.optJSONArray("extension");

		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);

			if (ext == null) continue;

			if (STATUS_EXTENSION_URL.equals(ext.optString("url"))) {
				JSONArray nested = ext.optJSONArray("extension");
				if (nested == null) continue;
				String statusClass = null;
				String status = null;
				String statusReason = null;
				for (int j = 0; j < nested.length(); j++) {
					JSONObject inner = nested.optJSONObject(j);
					if (inner == null) continue;
					String innerUrl = inner.optString("url", null);
					if ("statusClassCode".equals(innerUrl)) {
						statusClass = extractCodingCode(inner, "valueCodeableConcept");
					} else if ("statusCode".equals(innerUrl)) {
						status = extractCodingCode(inner, "valueCodeableConcept");
					} else if ("statusReasonCode".equals(innerUrl)) {
						statusReason = extractCodingCode(inner, "valueCodeableConcept");
					}
				}
				if (statusClass != null && status != null && statusReason != null) {
					b.addStatus(statusClass, status, statusReason);
				}
			}
		}
	}

    /*
     * Maps the notes from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapNotes(JSONObject org, MaintainOrgBuilder b) {
		JSONArray extensions = org.optJSONArray("extension");

		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {

			JSONObject ext = extensions.optJSONObject(i);

			if (ext == null) continue;

			if (NOTE_EXTENSION_URL.equals(ext.optString("url"))) {

				JSONArray nested = ext.optJSONArray("extension");

				for (int j = 0; j < nested.length(); j++) {
					JSONObject inner = nested.optJSONObject(j);

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
     * Helper to extract coding code from a wrapper object
     * @param wrapper resource JSON
     * @param fieldName field name containing the CodeableConcept
     */
	private static String extractCodingCode(JSONObject wrapper, String fieldName) {
		JSONObject cc = wrapper.optJSONObject(fieldName);
		if (cc == null) return null;
		JSONArray coding = cc.optJSONArray("coding");
		if (coding == null || coding.length() == 0) return null;
		JSONObject first = coding.optJSONObject(0);
		return first != null ? first.optString("code", null) : null;
	}

}
