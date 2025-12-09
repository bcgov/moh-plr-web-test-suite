package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.json.JSONObject;

import static org.testng.Assert.*;

public class Note {
	String noteIdentifier;
	String noteText;
	String effectiveFrom;
	String effectiveTo;
	String endReason;
	String dataSource;
	String dbCreated;
	String dbExpired;
	String dataOwnerCode;
	
	public String getNoteIdentifier() {
		return noteIdentifier;
	}
	public String getDataOwnerCode() {
		return dataOwnerCode;
	}
	
	public String getNoteText() {
		return noteText;
	}
	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}
	public void setNoteIdentifier(String noteIdentifier) { this.noteIdentifier = noteIdentifier; }

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
		if (!jsonData.isNull("DB Created") )
			this.dbCreated = jsonData.getString("DB Created");
		if (!jsonData.isNull("DB Expired") )
			this.dbExpired = jsonData.getString("DB Expired");
		if (!jsonData.isNull("Data Owner Code"))
			this.dataOwnerCode = jsonData.getString("Data Owner Code");

	}

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
