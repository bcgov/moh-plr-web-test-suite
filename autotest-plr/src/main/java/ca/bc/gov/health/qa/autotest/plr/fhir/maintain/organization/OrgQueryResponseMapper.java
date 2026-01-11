package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrganizationProperties;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicOwnerBusinessType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicServices;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;

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
	/** Canonical extension URL for organization confidentiality wrapper */
	private static final String CONFIDENTIALITY_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-confidentiality-extension";
	/** Canonical extension URL for availability container */
	private static final String AVAILABILITY_EXTENSION_URL    = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-availability-extension";
	/** Canonical extension URL for availableTime child */
	private static final String AVAILABLE_TIME_EXTENSION_URL  = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-availableTime-extension";
	/** Canonical extension URL for primary care clinic wrapper */
	private static final String PRIMARY_CARE_WRAPPER_URL      = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-primary-care-clinic-extension";
	/** Canonical extension URL for clinic type */
	private static final String CLINIC_TYPE_URL               = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-type-extension";
	/** Canonical extension URL for clinic ownership type */
	private static final String CLINIC_OWNER_BUSINESS_TYPE_URL= "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-ownership-type-extension";
	/** Canonical extension URL for clinic services */
	private static final String CLINIC_SERVICES_URL           = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-service-delivery-type-extension";
	/** Canonical extension URL for clinic owner name */
	private static final String CLINIC_OWNER_URL              = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-owner-extension";
	/** Canonical extension URL for PCI */
	private static final String PCI_EXTENSION_URL             = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-pci-extension";
	/** Canonical extension URL for legal business name */
	private static final String CLINIC_LEGAL_NAME_URL         = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-legal-name-extension";
	/** Canonical extension URL for clinic payee number */
	private static final String CLINIC_PAYEE_NUMBER_URL       = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-payee-number-extension";

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
		mapIdentifiers(orgResource, builder);
		mapNameAndAlias(orgResource, builder);
		mapRoleType(orgResource, builder);
		if (builder.getRoleType() == OrgRoleType.HDS) {
			mapHdsType(orgResource, builder);
		}
		mapAddresses(orgResource, builder);
		mapTelecom(orgResource, builder);
		mapStatus(orgResource, builder);
		mapNotes(orgResource, builder);
		mapConfidentiality(orgResource, builder);

		// Organization properties (primary care + availability + payee)
		OrganizationProperties props = new OrganizationProperties();
		mapAvailability(orgResource, props);
		mapPrimaryCareProperties(orgResource, props);
		mapPayeeNumbers(orgResource, props);
		builder.organizationProperties(props);

		return builder;
	}

    /*
     * Maps the identifiers from the Organization resource to the Organization builder.
     * @param org resource JSON
     * @param b MaintainOrgBuilder to populate
     */
	private static void mapIdentifiers(JSONObject org, MaintainOrgBuilder b) {
		JSONArray identifiers = org.optJSONArray("identifier");

		if (identifiers == null || identifiers.length() == 0) return;
        
		for (int i = 0; i < identifiers.length(); i++) {
			JSONObject id = identifiers.optJSONObject(i);

			String system = id.optString("system", null);
			String value = id.optString("value", null);

			if (system == null || value == null) continue;

			// Store all recognized identifier types into the builder
			for (IdentifierType t : IdentifierType.values()) {
				if (t.getSourceSystem().equals(system)) {
					b.addIdentifier(t, value);
					break;
				}
			}
		}
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

	/*
	 * Maps the confidentiality flag from the Organization resource to the builder.
	 */
	private static void mapConfidentiality(JSONObject org, MaintainOrgBuilder b) {
		JSONArray extensions = org.optJSONArray("extension");
		if (extensions == null) return;
		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;
			if (CONFIDENTIALITY_EXTENSION_URL.equals(ext.optString("url"))) {
				JSONArray nested = ext.optJSONArray("extension");
				if (nested == null) continue;
				for (int j = 0; j < nested.length(); j++) {
					JSONObject inner = nested.optJSONObject(j);
					if (inner == null) continue;
					if ("code".equals(inner.optString("url"))) {
						String code = extractCodingCode(inner, "valueCodeableConcept");
						if (code != null) {
							b.confidentiality("R".equals(code));
						}
						return;
					}
				}
			}
		}
	}

	/*
	 * Maps availability blocks (hours of operation) to OrganizationProperties.
	 * Produces day/time strings like "MON 08:00-17:30" (uppercase day) for builder serialization.
	 */
	private static void mapAvailability(JSONObject org, OrganizationProperties props) {
		JSONArray extensions = org.optJSONArray("extension");
		if (extensions == null) return;
		java.util.List<String> hours = new java.util.ArrayList<>();
		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;
			if (!AVAILABILITY_EXTENSION_URL.equals(ext.optString("url"))) continue;
			JSONArray nested = ext.optJSONArray("extension");
			if (nested == null) continue;
			for (int j = 0; j < nested.length(); j++) {
				JSONObject inner = nested.optJSONObject(j);
				if (inner == null) continue;
				if (!AVAILABLE_TIME_EXTENSION_URL.equals(inner.optString("url"))) continue;
				JSONArray timeParts = inner.optJSONArray("extension");
				if (timeParts == null) continue;
				String day = null; 
				String start = null; 
				String end = null;
				for (int k = 0; k < timeParts.length(); k++) {
					JSONObject part = timeParts.optJSONObject(k);
					if (part == null) continue;
					String url = part.optString("url", null);
					if ("daysOfWeek".equals(url)) {
						day = part.optString("valueCode", null);
					} else if ("availableStartTime".equals(url)) {
						start = part.optString("valueTime", null);
					} else if ("availableEndTime".equals(url)) {
						end = part.optString("valueTime", null);
					}
				}
				if (day != null && start != null && end != null) {
					String s = start.length() >= 5 ? start.substring(0,5) : start;
					String e = end.length() >= 5 ? end.substring(0,5) : end;
					hours.add(day.toUpperCase(java.util.Locale.ROOT) + " " + s + "-" + e);
				}
			}
		}
		if (!hours.isEmpty()) props.setClinicHoursOfOperation(hours);
	}

	/*
	 * Maps primary care clinic nested properties to OrganizationProperties.
	 */
	private static void mapPrimaryCareProperties(JSONObject org, OrganizationProperties props) {
		JSONArray extensions = org.optJSONArray("extension");
		if (extensions == null) return;
		JSONObject wrapper = null;
		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;
			if (PRIMARY_CARE_WRAPPER_URL.equals(ext.optString("url"))) {
				wrapper = ext; break;
			}
		}
		if (wrapper == null) return;
		JSONArray nested = wrapper.optJSONArray("extension");
		if (nested == null) return;
		for (int i = 0; i < nested.length(); i++) {
			JSONObject child = nested.optJSONObject(i);
			if (child == null) continue;
			String childUrl = child.optString("url", null);
			if (CLINIC_TYPE_URL.equals(childUrl)) {
				String text = extractLeafText(child, "clinicType");
				ClinicType ct = resolveClinicType(text);
				if (ct != null) props.setClinicType(ct);
			} else if (CLINIC_OWNER_BUSINESS_TYPE_URL.equals(childUrl)) {
				String text = extractLeafText(child, "ownershipType");
				ClinicOwnerBusinessType ob = resolveOwnerBusinessType(text);
				if (ob != null) props.setClinicOwnerBusinessType(ob);
			} else if (CLINIC_SERVICES_URL.equals(childUrl)) {
				String text = extractLeafText(child, "serviceDeliveryType");
				ClinicServices cs = resolveClinicServices(text);
				if (cs != null) props.setClinicServices(cs);
			} else if (CLINIC_OWNER_URL.equals(childUrl)) {
				String name = extractLeafString(child, "clinicOwner");
				if (name != null && !name.isEmpty()) {
					java.util.List<String> owners = new java.util.ArrayList<>(props.getClinicOwnerNames());
					owners.add(name);
					props.setClinicOwnerNames(owners);
				}
			} else if (PCI_EXTENSION_URL.equals(childUrl)) {
				Boolean flag = extractLeafBoolean(child, "pciFlag");
				if (flag != null) props.setPciFlag(flag);
			} else if (CLINIC_LEGAL_NAME_URL.equals(childUrl)) {
				String legal = extractLeafString(child, "clinicLegalName");
				if (legal != null && !legal.isEmpty()) props.setClinicLegalBusinessName(legal);
			}
		}
	}

	/*
	 * Maps payee numbers from clinic payee number extensions to OrganizationProperties.
	 */
	private static void mapPayeeNumbers(JSONObject org, OrganizationProperties props) {
		JSONArray extensions = org.optJSONArray("extension");
		if (extensions == null) return;
		java.util.List<String> payees = new java.util.ArrayList<>();
		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;
			if (!CLINIC_PAYEE_NUMBER_URL.equals(ext.optString("url"))) continue;
			JSONArray nested = ext.optJSONArray("extension");
			if (nested == null) continue;
			for (int j = 0; j < nested.length(); j++) {
				JSONObject inner = nested.optJSONObject(j);
				if (inner == null) continue;
				if ("payeeNumber".equals(inner.optString("url"))) {
					String val = inner.optString("valueString", null);
					if (val != null && !val.isEmpty()) payees.add(val);
				}
			}
		}
		if (!payees.isEmpty()) props.setPayeeNumber(payees);
	}

	// ---------------------- helpers ----------------------

	private static String extractLeafText(JSONObject wrapper, String leafUrl) {
		JSONArray childExt = wrapper.optJSONArray("extension");
		if (childExt == null) return null;
		for (int i = 0; i < childExt.length(); i++) {
			JSONObject e = childExt.optJSONObject(i);
			if (e == null) continue;
			if (!leafUrl.equals(e.optString("url"))) continue;
			JSONObject cc = e.optJSONObject("valueCodeableConcept");
			if (cc == null) return null;
			return cc.optString("text", null);
		}
		return null;
	}

	private static String extractLeafString(JSONObject wrapper, String leafUrl) {
		JSONArray childExt = wrapper.optJSONArray("extension");
		if (childExt == null) return null;
		for (int i = 0; i < childExt.length(); i++) {
			JSONObject e = childExt.optJSONObject(i);
			if (e == null) continue;
			if (!leafUrl.equals(e.optString("url"))) continue;
			return e.optString("valueString", null);
		}
		return null;
	}

	private static Boolean extractLeafBoolean(JSONObject wrapper, String leafUrl) {
		JSONArray childExt = wrapper.optJSONArray("extension");
		if (childExt == null) return null;
		for (int i = 0; i < childExt.length(); i++) {
			JSONObject e = childExt.optJSONObject(i);
			if (e == null) continue;
			if (!leafUrl.equals(e.optString("url"))) continue;
			if (e.has("valueBoolean")) return e.optBoolean("valueBoolean");
		}
		return null;
	}

	private static ClinicType resolveClinicType(String text) {
		if (text == null) return null;
		for (ClinicType ct : ClinicType.values()) {
			if (text.equals(ct.getText())) return ct;
		}
		return null;
	}

	private static ClinicOwnerBusinessType resolveOwnerBusinessType(String text) {
		if (text == null) return null;
		for (ClinicOwnerBusinessType ob : ClinicOwnerBusinessType.values()) {
			if (text.equals(ob.getText())) return ob;
		}
		return null;
	}

	private static ClinicServices resolveClinicServices(String text) {
		if (text == null) return null;
		for (ClinicServices cs : ClinicServices.values()) {
			if (text.equals(cs.getText())) return cs;
		}
		return null;
	}

}
