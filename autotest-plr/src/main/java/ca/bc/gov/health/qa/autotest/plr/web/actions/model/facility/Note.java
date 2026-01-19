package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import java.util.LinkedHashMap;
import java.util.Objects;

import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import org.json.JSONObject;
import static org.testng.Assert.*;

/**
 * Note data block content
 */
public class Note {
	private String noteIdentifier;
	private String noteText;
	private String effectiveFrom;
	private String effectiveTo;
	private String endReason;
	private String dataSource;
	private String dbCreated;
	private String dbExpired;
	private String dataOwnerCode;

	/**
	 * Gets the note identifier.
	 *
	 * @return the note identifier
	 */
	public String getNoteIdentifier() {
		return noteIdentifier;
	}

	/**
	 * Gets the data owner code.
	 *
	 * @return the data owner code or null
	 */
	public String getDataOwnerCode() {
		return dataOwnerCode;
	}

	/**
	 * Gets the dnoteText.
	 */
	public String getNoteText() {
		return noteText;
	}

	/**
	 * Constructs a Note using explicit values.
	 *
	 * @param noteIdentifier the note identifier
	 * @param noteText       the note text content
	 * @param effectiveFrom  effective from date
	 * @param effectiveTo    effective to date
	 * @param endReason      end reason
	 * @param dataSource     data source
	 * @param dbCreated      database created timestamp
	 * @param dbExpired      database expired timestamp
	 * @param dataOwnerCode  data owner code
	 */
	public Note(String noteIdentifier, String noteText, String effectiveFrom, String effectiveTo, String endReason,
			String dataSource, String dbCreated, String dbExpired, String dataOwnerCode) {
		super();
		this.noteIdentifier = noteIdentifier;
		this.noteText = noteText;
		this.effectiveFrom = effectiveFrom;
		this.effectiveTo = effectiveTo;
		this.endReason = endReason;
		this.dataSource = dataSource;
		this.dbCreated = dbCreated;
		this.dbExpired = dbExpired;
		this.dataOwnerCode = dataOwnerCode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataOwnerCode, dataSource, dbCreated, dbExpired, effectiveFrom, effectiveTo, endReason,
				noteIdentifier, noteText);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		return Objects.equals(dataOwnerCode, other.dataOwnerCode) && Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(dbCreated, other.dbCreated) && Objects.equals(dbExpired, other.dbExpired)
				&& Objects.equals(effectiveFrom, other.effectiveFrom) && Objects.equals(effectiveTo, other.effectiveTo)
				&& Objects.equals(endReason, other.endReason) && Objects.equals(noteIdentifier, other.noteIdentifier)
				&& Objects.equals(noteText, other.noteText);
	}

	/**
	 * Constructs an Note based on the result of a newly generated facility from FHIR.
	 * TODO add specifications for these fields within the builder as much as possible in future versions
	 *
	 * @param fhirFacility	the facility to create the note for
	 */
	public Note(MaintainFacilityBuilder fhirFacility, int index) {
		super();

		this.noteIdentifier = fhirFacility.getNoteList().get(index).getOrDefault("identifier", "null");
		this.noteText = fhirFacility.getNoteList().get(index).get("text");
		this.effectiveFrom = fhirFacility.getDate();
		this.effectiveTo = "";
		this.endReason = "";
		this.dataSource = ViewFacilityConstants.DATA_SOURCE_DEFAULT;
		this.dbCreated = fhirFacility.getDate();
		this.dbExpired = "";
		this.dataOwnerCode = ViewFacilityConstants.DATA_OWNER_CODE_DEFAULT;
	}

	/**
	 * Constructs a Note from a JSON object.
	 *
	 * @param jsonData the JSON data containing fields
	 */
	public Note(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

		assertFalse(jsonData.isNull("Note Identifier"));
		this.noteIdentifier = jsonData.getString("Note Identifier");
		assertFalse(jsonData.isNull("Note Text"));
		this.noteText = jsonData.getString("Note Text");

		if (!jsonData.isNull("Effective From"))
			this.effectiveFrom = jsonData.getString("Effective From");
		if (!jsonData.isNull("Effective To"))
			this.effectiveTo = jsonData.getString("Effective To");
		if (!jsonData.isNull("End Reason"))
			this.endReason = jsonData.getString("End Reason");
		if (!jsonData.isNull("Data Source"))
			this.dataSource = jsonData.getString("Data Source");
		if (!jsonData.isNull("DB Created"))
			this.dbCreated = jsonData.getString("DB Created");
		if (!jsonData.isNull("DB Expired"))
			this.dbExpired = jsonData.getString("DB Expired");
		if (!jsonData.isNull("Data Owner Code"))
			this.dataOwnerCode = jsonData.getString("Data Owner Code");

	}

	/**
	 * Constructs a Note from a map of label to value.
	 *
	 * @param map the map with keys matching UI labels
	 */
	public Note(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		assertNotNull(map.get("Note Identifier"));
		this.noteIdentifier = map.get("Note Identifier");
		assertNotNull(map.get("Note Text"));
		this.noteText = map.get("Note Text");

		this.effectiveFrom = map.get("Effective From") == null ? null : map.get("Effective From");
		this.effectiveTo = map.get("Effective To") == null ? null : map.get("Effective To");
		this.endReason = map.get("End Reason") == null ? null : map.get("End Reason");
		this.dataSource = map.get("Data Source") == null ? null : map.get("Data Source");
		this.dbCreated = map.get("DB Created") == null ? null : map.get("DB Created");
		this.dbExpired = map.get("DB Expired") == null ? null : map.get("DB Expired");
		this.dataOwnerCode = map.get("Data Owner Code") == null ? null : map.get("Data Owner Code");
	}

}
