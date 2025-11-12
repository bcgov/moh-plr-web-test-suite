package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.json.JSONObject;

public class Name {
	String name;
	String description;
	String effectiveFrom;
	String effectiveTo;
	String endReason;
	String dataSource;
	String dbCreated;
	String dbExpired;
	String dataOwnerCode;
	public Name(String name, String description, String effectiveFrom, String effectiveTo, String endReason,
			String dataSource, String dbCreated, String dbExpired, String dataOwnerCode) {
		super();
		this.name = name;
		this.description = description;
		this.effectiveFrom = effectiveFrom;
		this.effectiveTo = effectiveTo;
		this.endReason = endReason;
		this.dataSource = dataSource;
		this.dbCreated = dbCreated;
		this.dbExpired = dbExpired;
		this.dataOwnerCode = dataOwnerCode;
	}
	
	public String getName() {
		return name;
	}

	public Name(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

		assertTrue(!jsonData.isNull("Name"));
		this.name = jsonData.getString("Name");
		
		
		if (!jsonData.isNull("Description"))
			this.description = jsonData.getString("Description");
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

	public Name(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		
		assertNotNull(map.get("Name"));
		this.name = map.get("Name");
		
		this.description = map.get("Description") == null ? null : map.get("Description");
		this.effectiveFrom = map.get("Effective From") == null ? null : map.get("Effective From");
		this.effectiveTo = map.get("Effective To") == null ? null : map.get("Effective To");
		this.endReason = map.get("End Reason") == null ? null : map.get("End Reason");
		this.dataSource = map.get("Data Source") == null ? null : map.get("Data Source");
		this.dbCreated = map.get("DB Created") == null ? null : map.get("DB Created");
		this.dbExpired = map.get("DB Expired") == null ? null : map.get("DB Expired");
		this.dataOwnerCode = map.get("Data Owner Code") == null ? null : map.get("Data Owner Code");
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataOwnerCode, dataSource, dbCreated, dbExpired, description, effectiveFrom, effectiveTo,
				endReason, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Name other = (Name) obj;
		return Objects.equals(dataOwnerCode, other.dataOwnerCode) && Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(dbCreated, other.dbCreated) && Objects.equals(dbExpired, other.dbExpired)
				&& Objects.equals(description, other.description) && Objects.equals(effectiveFrom, other.effectiveFrom)
				&& Objects.equals(effectiveTo, other.effectiveTo) && Objects.equals(endReason, other.endReason)
				&& Objects.equals(name, other.name);
	}
	
	

}
