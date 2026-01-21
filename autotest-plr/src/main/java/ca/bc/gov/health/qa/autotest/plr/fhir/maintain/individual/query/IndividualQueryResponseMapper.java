package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PractitionerRelationshipCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * Maps a practitioner query JSON response into a {@link MaintainIndividualBuilder}.
 *
 * Error and missed segments will be skipped. Validation of required fields will be
 * caught by MaintainIndividualBuilder.verifyParameters() on build().
 */
public final class IndividualQueryResponseMapper {

	private static final Logger LOG = ExecutionLogManager.getLogger();

	private IndividualQueryResponseMapper() {}

	/** Canonical extension URL for practitioner note wrapper. */
	private static final String NOTE_EXTENSION_URL           = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-note-extension";
    /** Canonical extension URL for practitioner status wrapper */
    private static final String STATUS_EXTENSION_URL         = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-license-status-extension";
    /** Canonical extension URL for practitioner communication purpose wrapper */
    private static final String COMM_PURPOSE_URL             = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-communication-purpose-code-extension";
	/** Canonical extension URL for practitioner birth place */
	private static final String BIRTHPLACE_EXTENSION_URL     = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-birthplace-extension";
	/** Canonical extension URL for practitioner death date */
	private static final String DEATHDATE_EXTENSION_URL      = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-deathdate-extension";
	/** Canonical extension URL for practitioner birth time */
	private static final String BIRTHTIME_EXTENSION_URL      = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-birthtime-extension";
	/** Canonical extension URL for practitioner condition wrapper */
	private static final String CONDITION_EXTENSION_URL      = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-condition-extension";
	/** Canonical extension URL for practitioner disciplinary action wrapper */
	private static final String DISCIPLINARY_EXTENSION_URL   = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-disciplinary-action-extension";
	/** Canonical extension URL for confidentiality */
	private static final String CONFIDENTIALITY_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-confidentiality-extension";
	/** Canonical extension URL for relationship type */
	private static final String RELATIONSHIP_TYPE_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-relationship-type-extension";
	/** Canonical extension URL for practitioner-to-practitioner relationships */
	private static final String PRACTITIONER_RELATIONSHIP_EXTENSION_URL = "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-relationship-extension";

	/** PractitionerRole resourceType constant. */
	private static final String PRACTITIONER_ROLE_TYPE = "PractitionerRole";

	/**
     * Convert a practitioner query bundle into a MaintainIndividualBuilder.
     * @param pracQueryBundle Complete JSON bundle returned by practitioner query
     * @return populated MaintainIndividualBuilder
     * @throws IllegalArgumentException if no Practitioner resource found
     */
	public static MaintainIndividualBuilder fromQueryBundle(JSONObject pracQueryBundle) {
		JSONArray topEntries = pracQueryBundle.optJSONArray("entry");
		if (topEntries == null) {
			throw new IllegalArgumentException("No entries in practitioner query bundle");
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

		if (collectionBundle == null) {
			throw new IllegalArgumentException("No collection bundle (search.mode=match) found in practitioner query response");
		}

		JSONArray innerEntries = collectionBundle.optJSONArray("entry");
		if (innerEntries == null) {
			throw new IllegalArgumentException("Inner collection bundle missing entry array");
		}

		JSONObject pracResource = null;
		JSONArray practitionerRoleArray = new JSONArray();
		for (int i = 0; i < innerEntries.length(); i++) {
			JSONObject inner = innerEntries.optJSONObject(i);
			if (inner == null) continue;
			JSONObject resource = inner.optJSONObject("resource");
			if (resource == null) continue;
			String resourceType = resource.optString("resourceType");
			if ("Practitioner".equals(resourceType) && pracResource == null) {
				pracResource = resource;
			} else if (PRACTITIONER_ROLE_TYPE.equals(resourceType)) {
				practitionerRoleArray.put(resource);
			}
		}

		if (pracResource == null) {
			throw new IllegalArgumentException("No Practitioner resource found in query bundle");
		}

		MaintainIndividualBuilder builder = new MaintainIndividualBuilder();
		populateBuilderFromIndividual(pracResource, builder, collectionBundle);
		mapOrganizationRelationships(practitionerRoleArray, builder);
		return builder;
	}

	/**
	 * Parses all matching Practitioner resources from the searchset bundle and returns
	 * a list of MaintainIndividualBuilder instances.
	 *
	 * If no practitioners are found, an empty list is returned.
	 *
	 * @param pracQueryBundle Complete JSON bundle returned by practitioner query
	 * @return list of populated MaintainIndividualBuilder objects (possibly empty)
	 */
	public static List<MaintainIndividualBuilder> fromQueryBundleAll(JSONObject pracQueryBundle) {
		List<MaintainIndividualBuilder> builders = new ArrayList<>();
		if (pracQueryBundle == null) return builders;

		JSONArray topEntries = pracQueryBundle.optJSONArray("entry");
		if (topEntries == null) return builders;

		for (int i = 0; i < topEntries.length(); i++) {
			JSONObject topEntry = topEntries.optJSONObject(i);
			if (topEntry == null) continue;
			
			JSONObject search = topEntry.optJSONObject("search");
			if (search == null || !"match".equals(search.optString("mode"))) continue;

			JSONObject collectionBundle = topEntry.optJSONObject("resource");
			if (collectionBundle == null) continue;

			JSONArray innerEntries = collectionBundle.optJSONArray("entry");
			if (innerEntries == null) continue;

			// First pass: collect PractitionerRole resources
			JSONArray practitionerRoleArray = new JSONArray();
			for (int j = 0; j < innerEntries.length(); j++) {
				JSONObject inner = innerEntries.optJSONObject(j);
				if (inner == null) continue;
				JSONObject resource = inner.optJSONObject("resource");
				if (resource == null) continue;
				if (PRACTITIONER_ROLE_TYPE.equals(resource.optString("resourceType"))) {
					practitionerRoleArray.put(resource);
				}
			}

			// Second pass: process Practitioner resources
			for (int j = 0; j < innerEntries.length(); j++) {
				JSONObject inner = innerEntries.optJSONObject(j);
				if (inner == null) continue;

				JSONObject resource = inner.optJSONObject("resource");
				if (resource == null || !"Practitioner".equals(resource.optString("resourceType"))) continue;

				MaintainIndividualBuilder builder = new MaintainIndividualBuilder();
				
				try {
					populateBuilderFromIndividual(resource, builder, collectionBundle);
					mapOrganizationRelationships(practitionerRoleArray, builder);
					builders.add(builder);
				} catch (Exception ignored) {
					LOG.info("Error occurred: {}", ignored.getMessage());
					// Skip malformed entries
				}
			}
		}

		return builders;
	}

	/**
	 * Populates a MaintainIndividualBuilder from a single Practitioner resource JSON.
	 * Extracts role type from collection bundle if provided.
	 * @param pracResource the Practitioner resource JSON
	 * @param builder the builder to populate
	 * @param collectionBundle the collection bundle containing PractitionerRole (optional)
	 */
	private static void populateBuilderFromIndividual(JSONObject pracResource, MaintainIndividualBuilder builder, JSONObject collectionBundle) {
		mapIdentifiers(pracResource, builder);
		mapNames(pracResource, builder);
		mapTelecom(pracResource, builder);
		mapAddresses(pracResource, builder);
		mapDemographics(pracResource, builder);
		mapQualifications(pracResource, builder);
		mapCommunication(pracResource, builder);
		mapStatus(pracResource, builder);
		mapNotes(pracResource, builder);
		mapConditions(pracResource, builder);
		mapDisciplinaryActions(pracResource, builder);
		mapConfidentiality(pracResource, builder);
		mapIndividualRelationships(pracResource, builder);
		mapRoleType(collectionBundle, builder);
	}

	/**
	 * Extracts role type from PractitionerRole resources in the collection bundle.
	 * @param collectionBundle the collection Bundle containing PractitionerRole resources
	 * @param builder the builder to populate
	 */
	private static void mapRoleType(JSONObject collectionBundle, MaintainIndividualBuilder builder) {
		if (collectionBundle == null) return;

		JSONArray entries = collectionBundle.optJSONArray("entry");
		if (entries == null) return;

		for (int i = 0; i < entries.length(); i++) {
			JSONObject entry = entries.optJSONObject(i);
			if (entry == null) continue;

			JSONObject resource = entry.optJSONObject("resource");
			if (resource == null || !"PractitionerRole".equals(resource.optString("resourceType"))) continue;

			// Extract first code from code array
			JSONArray codeArray = resource.optJSONArray("code");
			if (codeArray == null || codeArray.length() == 0) continue;

			JSONObject firstCode = codeArray.optJSONObject(0);
			if (firstCode == null) continue;

			String roleCode = extractCodingCode(firstCode, "coding");
			if (roleCode != null) {
				IndividualRoleType roleType = IndividualRoleType.resolveRoleType(roleCode);
				if (roleType != null) {
					builder.roleType(roleType);
				} 
				return; // Only need the first one
			}
		}
	}

    /*
     * Maps the identifiers from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapIdentifiers(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray identifiers = prac.optJSONArray("identifier");
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
     * Maps the name from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapNames(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray names = prac.optJSONArray("name");
		if (names == null || names.length() == 0) return;

		JSONObject name = names.optJSONObject(0);
		if (name == null) return;

		String family = name.optString("family", null);
		if (family != null && !family.isEmpty()) {
			b.familyName(family);
		}

		JSONArray given = name.optJSONArray("given");
		if (given != null && given.length() > 0) {
			String first = given.optString(0, "");
			String middle = given.length() > 1 ? given.optString(1, "") : "";
			String third = given.length() > 2 ? given.optString(2, "") : "";
			b.setNames(first, middle, third);
		}
	}

    /*
     * Maps the telecoms from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapTelecom(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray telecom = prac.optJSONArray("telecom");
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
					if (e != null && COMM_PURPOSE_URL.equals(e.optString("url"))) {
						purpose = extractCodingCode(e, "valueCodeableConcept");
						break;
					}
				}
			}
			if (system != null && value != null && !system.isEmpty() && !value.isEmpty()) {
				b.addTelecom(system, purpose, value);
			}
		}
	}

    /*
     * Maps the address from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapAddresses(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray addresses = prac.optJSONArray("address");
		if (addresses == null) return;

		for (int i = 0; i < addresses.length(); i++) {
			JSONObject addr = addresses.optJSONObject(i);
			JSONArray lines = addr.optJSONArray("line");
			String line1 = lines != null && lines.length() > 0 ? lines.optString(0, null) : null;
			String city = addr.optString("city", null);
			String postal = addr.optString("postalCode", null);
			String type = addr.optString("type", "physical");
			String purpose = null;
			JSONArray ext = addr.optJSONArray("extension");

			if (ext != null) {
				for (int j = 0; j < ext.length(); j++) {
					JSONObject e = ext.optJSONObject(j);
					if (e != null && COMM_PURPOSE_URL.equals(e.optString("url"))) {
						purpose = extractCodingCode(e, "valueCodeableConcept");
						break;
					}
				}
			}

			if (line1 != null && city != null) {
				b.addAddress(type, purpose, line1, city, postal);
			}
		}
	}

    /*
     * Maps demographics (gender, birthDate, birthplace, deathDate) from the Practitioner resource.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapDemographics(JSONObject prac, MaintainIndividualBuilder b) {
		String gender = prac.optString("gender", null);
		String birthDate = null;
		String birthCountry = null;
		String birthProvince = null;
		String deathDate = null;

		// Extract birthDate from _birthDate.extension.birthtime
		JSONObject birthDateExt = prac.optJSONObject("_birthDate");
		if (birthDateExt != null) {
			JSONArray exts = birthDateExt.optJSONArray("extension");
			if (exts != null) {
				for (int i = 0; i < exts.length(); i++) {
					JSONObject ext = exts.optJSONObject(i);
					if (ext != null && BIRTHTIME_EXTENSION_URL.equals(ext.optString("url"))) {
						String dateTime = ext.optString("valueDateTime", null);
						if (dateTime != null) {
							// Extract date portion (YYYY-MM-DD)
							birthDate = dateTime.substring(0, Math.min(10, dateTime.length()));
						}
						break;
					}
				}
			}
		}

		// Extract birthplace and deathDate from top-level extensions
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions != null) {
			for (int i = 0; i < extensions.length(); i++) {
				JSONObject ext = extensions.optJSONObject(i);
				if (ext == null) continue;

				String url = ext.optString("url");
				if (BIRTHPLACE_EXTENSION_URL.equals(url)) {
					JSONObject addr = ext.optJSONObject("valueAddress");
					if (addr != null) {
						birthProvince = addr.optString("state", null);
						birthCountry = addr.optString("country", null);
					}
				} else if (DEATHDATE_EXTENSION_URL.equals(url)) {
					String dateTime = ext.optString("valueDateTime", null);
					if (dateTime != null) {
						deathDate = dateTime.substring(0, Math.min(10, dateTime.length()));
					}
				}
			}
		}

		// Set demographics if we have at least some data
		if (gender != null || birthDate != null) {
			b.setDemographics(birthDate, birthCountry, birthProvince, gender, deathDate);
		}
	}

    /*
     * Maps qualifications (credentials) from the Practitioner resource.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapQualifications(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray qualifications = prac.optJSONArray("qualification");
		if (qualifications == null) return;

		for (int i = 0; i < qualifications.length(); i++) {
			JSONObject qual = qualifications.optJSONObject(i);
			if (qual == null) continue;

			String credentialType = null;
			String designation = null;
			String registrationNumber = null;
			String institutionName = null;
			String institutionCity = null;
			Boolean displayFlag = null;
			String grantedDate = null;

			// Extract credential type from code.coding
			JSONObject code = qual.optJSONObject("code");
			if (code != null) {
				credentialType = extractCodingCode(code, "coding");
			}

			// Extract nested extension fields from bc-practitioner-qualification-extension
			JSONArray exts = qual.optJSONArray("extension");
			if (exts != null) {
				for (int j = 0; j < exts.length(); j++) {
					JSONObject ext = exts.optJSONObject(j);
					if (ext == null) continue;

					if ("http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-qualification-extension".equals(ext.optString("url"))) {
						JSONArray nested = ext.optJSONArray("extension");
						if (nested == null) continue;

						for (int k = 0; k < nested.length(); k++) {
							JSONObject child = nested.optJSONObject(k);
							if (child == null) continue;

							String childUrl = child.optString("url");
							if ("designation".equals(childUrl)) {
								designation = child.optString("valueString", null);
							} else if ("registrationNumber".equals(childUrl)) {
								registrationNumber = child.optString("valueString", null);
							} else if ("equivalencyFlag".equals(childUrl)) {
								displayFlag = child.optBoolean("valueBoolean");
							} else if ("issuedDate".equals(childUrl)) {
								String dateValue = child.optString("valueDate", null);
								if (dateValue != null && dateValue.length() >= 4) {
									grantedDate = dateValue.substring(0, 4);
								}
							}
						}
					}
				}
			}

			// Extract granting institution from contained resources
			String issuerRef = qual.optJSONObject("issuer") != null 
				? qual.optJSONObject("issuer").optString("reference", null) : null;
			if (issuerRef != null && issuerRef.startsWith("#")) {
				String containedId = issuerRef.substring(1);
				JSONArray contained = prac.optJSONArray("contained");
				if (contained != null) {
					for (int c = 0; c < contained.length(); c++) {
						JSONObject org = contained.optJSONObject(c);
						if (org != null && containedId.equals(org.optString("id"))) {
							institutionName = org.optString("name", null);
							JSONArray addrs = org.optJSONArray("address");
							if (addrs != null && addrs.length() > 0) {
								institutionCity = addrs.optJSONObject(0).optString("city", null);
							}
							break;
						}
					}
				}
			}

			if (credentialType != null) {
				b.addCredential(credentialType, designation, registrationNumber, institutionName, institutionCity, 
					displayFlag != null ? displayFlag : true, grantedDate);
			}
		}
	}

    /*
     * Maps communication (expertise) from the Practitioner resource.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapCommunication(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray communication = prac.optJSONArray("communication");
		if (communication == null) return;

		for (int i = 0; i < communication.length(); i++) {
			JSONObject comm = communication.optJSONObject(i);
			if (comm == null) continue;

			String code = null;
			String description = null;

			// Extract code from coding array
			JSONArray coding = comm.optJSONArray("coding");
			if (coding != null && coding.length() > 0) {
				JSONObject codeObj = coding.optJSONObject(0);
				if (codeObj != null) {
					code = codeObj.optString("code", null);
				}
			}

			// Extract description from extension
			JSONArray exts = comm.optJSONArray("extension");
			if (exts != null) {
				for (int j = 0; j < exts.length(); j++) {
					JSONObject ext = exts.optJSONObject(j);
					if (ext == null) continue;

					if ("http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-specialty-source-extension".equals(ext.optString("url"))) {
						description = ext.optString("valueString", null);
						break;
					}
				}
			}

			if (code != null) {
				b.addExpertise(code, description);
			}
		}
	}

    /*
     * Maps the status from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapStatus(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;

			if (STATUS_EXTENSION_URL.equals(ext.optString("url"))) {
				String statusClass = null;
				String statusCode = null;
				String statusReason = null;

				JSONArray nested = ext.optJSONArray("extension");
				if (nested != null) {
					for (int j = 0; j < nested.length(); j++) {
						JSONObject child = nested.optJSONObject(j);
						if (child == null) continue;

						String childUrl = child.optString("url");
						if ("statusClassCode".equals(childUrl)) {
							statusClass = extractCodingCode(child, "valueCodeableConcept");
						} else if ("statusCode".equals(childUrl)) {
							statusCode = extractCodingCode(child, "valueCodeableConcept");
						} else if ("statusReasonCode".equals(childUrl)) {
							statusReason = extractCodingCode(child, "valueCodeableConcept");
						}
					}
				}

				if (statusClass != null && statusCode != null) {
					b.addStatus(statusClass, statusCode, statusReason);
				}
			}
		}
	}

    /*
     * Maps the notes from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapNotes(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;

			if (NOTE_EXTENSION_URL.equals(ext.optString("url"))) {
				String noteText = null;

				JSONArray nested = ext.optJSONArray("extension");
				if (nested != null) {
					for (int j = 0; j < nested.length(); j++) {
						JSONObject child = nested.optJSONObject(j);
						if (child == null) continue;

						if ("text".equals(child.optString("url"))) {
							noteText = child.optString("valueString", null);
							break;
						}
					}
				}

				if (noteText != null) {
					b.addNote(noteText);
				}
			}
		}
	}

    /*
     * Maps conditions from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapConditions(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;

			if (CONDITION_EXTENSION_URL.equals(ext.optString("url"))) {
				String conditionCode = null;
				Boolean restriction = null;
				String restrictionText = null;

				JSONArray nested = ext.optJSONArray("extension");
				if (nested != null) {
					for (int j = 0; j < nested.length(); j++) {
						JSONObject child = nested.optJSONObject(j);
						if (child == null) continue;

						String childUrl = child.optString("url");
						if ("code".equals(childUrl)) {
							conditionCode = extractCodingCode(child, "valueCodeableConcept");
						} else if ("restriction".equals(childUrl)) {
							restriction = child.optBoolean("valueBoolean");
						} else if ("restrictionText".equals(childUrl)) {
							restrictionText = child.optString("valueString", null);
						}
					}
				}

				if (conditionCode != null) {
					b.addCondition(conditionCode, restriction != null ? restriction : false, restrictionText);
				}
			}
		}
	}

    /*
     * Maps disciplinary actions from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapDisciplinaryActions(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;

			if (DISCIPLINARY_EXTENSION_URL.equals(ext.optString("url"))) {
				Boolean displayFlag = null;
				String description = null;
				String archiveDate = null;

				JSONArray nested = ext.optJSONArray("extension");
				if (nested != null) {
					for (int j = 0; j < nested.length(); j++) {
						JSONObject child = nested.optJSONObject(j);
						if (child == null) continue;

						String childUrl = child.optString("url");
						if ("displayFlag".equals(childUrl)) {
							displayFlag = child.optBoolean("valueBoolean");
						} else if ("description".equals(childUrl)) {
							description = child.optString("valueString", null);
						} else if ("archiveDate".equals(childUrl)) {
							String dateTime = child.optString("valueDateTime", null);
							if (dateTime != null) {
								archiveDate = dateTime.substring(0, Math.min(10, dateTime.length()));
							}
						}
					}
				}

				if (description != null) {
					b.addDisciplinaryAction(displayFlag != null ? displayFlag : true, description, archiveDate);
				}
			}
		}
	}

    /*
     * Maps confidentiality from the Practitioner resource to the Individual builder.
     * @param prac resource JSON
     * @param b MaintainIndividualBuilder to populate
     */
	private static void mapConfidentiality(JSONObject prac, MaintainIndividualBuilder b) {
		JSONArray extensions = prac.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;

			if (CONFIDENTIALITY_EXTENSION_URL.equals(ext.optString("url"))) {
				JSONArray nested = ext.optJSONArray("extension");
				if (nested != null) {
					for (int j = 0; j < nested.length(); j++) {
						JSONObject child = nested.optJSONObject(j);
						if (child == null) continue;

						if ("code".equals(child.optString("url"))) {
							String code = extractCodingCode(child, "valueCodeableConcept");
							if ("R".equals(code)) {
								b.confidentiality(true);
							} else if ("N".equals(code)) {
								b.confidentiality(false);
							}
							return;
						}
					}
				}
			}
		}
	}

    /*
     * Helper to extract coding code from a wrapper object or coding array
     * @param wrapper resource JSON
     * @param fieldName field name containing the CodeableConcept or "coding" for direct array
     */
	private static String extractCodingCode(JSONObject wrapper, String fieldName) {
		if ("coding".equals(fieldName)) {
			// Direct coding array (used in communication)
			JSONArray coding = wrapper.optJSONArray("coding");
			if (coding != null && coding.length() > 0) {
				JSONObject first = coding.optJSONObject(0);
				return first != null ? first.optString("code", null) : null;
			}
			return null;
		}
		
		// CodeableConcept wrapper
		JSONObject cc = wrapper.optJSONObject(fieldName);
		if (cc == null) return null;
		JSONArray coding = cc.optJSONArray("coding");
		if (coding == null || coding.length() == 0) return null;
		JSONObject first = coding.optJSONObject(0);
		return first != null ? first.optString("code", null) : null;
	}

	/*
	* Maps the OrganizationRelationships from PractitionerRole resources to the Individual builder.
	* Parses PractitionerRole resources to extract organization identifier and relationship code.
	*
	* @param practitionerRoleArray Array containing PractitionerRole resources
	* @param b MaintainIndividualBuilder to populate
	*/
	private static void mapOrganizationRelationships(JSONArray practitionerRoleArray, MaintainIndividualBuilder b) {
		if (practitionerRoleArray == null) return;
		for (int i = 0; i < practitionerRoleArray.length(); i++) {
			JSONObject role = practitionerRoleArray.optJSONObject(i);
			if (role == null) continue;

			// Extract organization identifier
			JSONObject organization = role.optJSONObject("organization");
			if (organization == null) continue;

			JSONObject identifier = organization.optJSONObject("identifier");
			if (identifier == null) continue;

			String system = identifier.optString("system", null);
			String idValue = identifier.optString("value", null);
			if (system == null || idValue == null || idValue.isEmpty()) continue;

			// Extract relationship code from extension
			String relationshipCode = null;
			JSONArray extensions = role.optJSONArray("extension");
			if (extensions != null) {
				for (int j = 0; j < extensions.length(); j++) {
					JSONObject ext = extensions.optJSONObject(j);
					if (ext == null) continue;
					if (RELATIONSHIP_TYPE_EXTENSION_URL.equals(ext.optString("url"))) {
						relationshipCode = extractCodingCode(ext, "valueCodeableConcept");
						break;
					}
				}
			}

			IdentifierType idType = IdentifierType.resolveIdentifierType(system);
			if (idType != null && relationshipCode != null) {
				b.addOrganizationRelationship(idType, idValue, PractitionerRelationshipCode.resolveCode(relationshipCode));
			}
		}
	}

	/*
	 * Maps individual-to-individual relationships from Practitioner extension array to the Individual builder.
	 * These relationships are stored as extensions in the Practitioner resource itself.
	 *
	 * @param pracResource Practitioner resource JSON
	 * @param b MaintainIndividualBuilder to populate
	 */
	private static void mapIndividualRelationships(JSONObject pracResource, MaintainIndividualBuilder b) {
		JSONArray extensions = pracResource.optJSONArray("extension");
		if (extensions == null) return;

		for (int i = 0; i < extensions.length(); i++) {
			JSONObject ext = extensions.optJSONObject(i);
			if (ext == null) continue;
			if (!PRACTITIONER_RELATIONSHIP_EXTENSION_URL.equals(ext.optString("url"))) continue;

			// Found a practitioner relationship extension
			JSONArray relationshipExtensions = ext.optJSONArray("extension");
			if (relationshipExtensions == null) continue;

			String targetIdSystem = null;
			String targetIdValue = null;
			String relationshipCode = null;

			for (int j = 0; j < relationshipExtensions.length(); j++) {
				JSONObject innerExt = relationshipExtensions.optJSONObject(j);
				if (innerExt == null) continue;
				String innerUrl = innerExt.optString("url", null);

				if ("targetPractitioner".equals(innerUrl)) {
					JSONObject valueRef = innerExt.optJSONObject("valueReference");
					if (valueRef != null) {
						JSONObject identifier = valueRef.optJSONObject("identifier");
						if (identifier != null) {
							targetIdSystem = identifier.optString("system", null);
							targetIdValue = identifier.optString("value", null);
						}
					}
				} else if ("relationshipType".equals(innerUrl)) {
					relationshipCode = extractCodingCode(innerExt, "valueCodeableConcept");
				}
			}

			IdentifierType idType = IdentifierType.resolveIdentifierType(targetIdSystem);
			if (idType != null && targetIdValue != null && relationshipCode != null) {
				b.addIndividualRelationship(idType, targetIdValue, PractitionerRelationshipCode.resolveCode(relationshipCode));
			}
		}
	}
}

