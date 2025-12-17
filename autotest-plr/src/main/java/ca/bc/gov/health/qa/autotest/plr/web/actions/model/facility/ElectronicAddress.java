package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import static org.testng.Assert.*;

import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import org.json.JSONObject;

/**
 * TODO (KD)
 */
public class ElectronicAddress {
	private String type;
	private String purpose;
	private String address;
	private String effectiveFrom;
	private String effectiveTo;
	private String endReason;
	private String dataSource;
	private String dbCreated;
	private String dbExpired;
	private String dataOwnerCode;
	
	/**
	 * Gets a simplified Type value (text before parenthesis).
	 *
	 * @return the type summary or the original value if null
	 */
	public String getTypeSummary() {
		if(type==null) return null;
		return type.split("\\(")[0].stripTrailing();
	}

	/**
	 * Gets a simplified Purpose value (text before parenthesis).
	 *
	 * @return the purpose summary or the original value if null
	 */
	public String getPurposeSummary() {
		if(purpose==null) return null;
		return purpose.split("\\(")[0].stripTrailing();
	}
	
	

	public String getType() {
		return type;
	}

	/**
	 * Gets the electronic address value.
	 *
	 * @return the address
	 */
	public String getAddress() {
		return address;
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
	 * Constructs an ElectronicAddress using explicit values.
	 *
	 * @param type the type
	 * @param purpose the purpose
	 * @param address the address value
	 * @param effectiveFrom effective from date
	 * @param effectiveTo effective to date
	 * @param endReason end reason
	 * @param dataSource data source
	 * @param dbCreated database created timestamp
	 * @param dbExpired database expired timestamp
	 * @param dataOwnerCode data owner code
	 */
	public ElectronicAddress(String type, String purpose, String address, String effectiveFrom, String effectiveTo,
			String endReason, String dataSource, String dbCreated, String dbExpired, String dataOwnerCode) {
		super();
		this.type = type;
		this.purpose = purpose;
		this.address = address;
		this.effectiveFrom = effectiveFrom;
		this.effectiveTo = effectiveTo;
		this.endReason = endReason;
		this.dataSource = dataSource;
		this.dbCreated = dbCreated;
		this.dbExpired = dbExpired;
		this.dataOwnerCode = dataOwnerCode;
	}

	/**
	 * Constructs a ElectronicAddress based on the result of a newly generated facility from FHIR.
	 * TODO add specifications for these fields within the builder as much as possible in future versions
	 *
	 * @param fhirFacility	the facility to create the electronic address for
	 */
	public ElectronicAddress(MaintainFacilityBuilder fhirFacility, Map<String,String> eaddress) {
		super();

		this.type = eaddress.get("type");
		this.purpose = eaddress.get("purpose");
		this.address = eaddress.get("purpose");
		this.effectiveFrom = fhirFacility.getDate();
		this.effectiveTo = "";
		this.endReason = "";
		this.dataSource = ViewFacilityConstants.DATA_SOURCE_DEFAULT;
		this.dbCreated = fhirFacility.getDate();
		this.dbExpired = "";
		this.dataOwnerCode = ViewFacilityConstants.DATA_OWNER_CODE_DEFAULT;
	}
	
	/**
	 * Constructs an ElectronicAddress from a JSON object.
	 *
	 * @param jsonData the JSON data containing fields
	 */
	public ElectronicAddress(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

        assertFalse(jsonData.isNull("Type"));
		this.type = jsonData.getString("Type");
        assertFalse(jsonData.isNull("Purpose"));
		this.purpose = jsonData.getString("Purpose");
        assertFalse(jsonData.isNull("Address"));
		this.address = jsonData.getString("Address");

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

	/**
	 * Constructs an ElectronicAddress from a map of label to value.
	 *
	 * @param map the map with keys matching UI labels
	 */
	public ElectronicAddress(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		
		assertNotNull(map.get("Type"));
		this.type = map.get("Type");
		assertNotNull(map.get("Purpose"));
		this.purpose = map.get("Purpose");
		assertNotNull(map.get("Address"));
		this.address = map.get("Address");
		
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
		return Objects.hash(address, dataOwnerCode, dataSource, dbCreated, dbExpired, effectiveFrom, effectiveTo,
				endReason, purpose, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElectronicAddress other = (ElectronicAddress) obj;
		return Objects.equals(address, other.address) && Objects.equals(dataOwnerCode, other.dataOwnerCode)
				&& Objects.equals(dataSource, other.dataSource) && Objects.equals(dbCreated, other.dbCreated)
				&& Objects.equals(dbExpired, other.dbExpired) && Objects.equals(effectiveFrom, other.effectiveFrom)
				&& Objects.equals(effectiveTo, other.effectiveTo) && Objects.equals(endReason, other.endReason)
				&& Objects.equals(purpose, other.purpose) && Objects.equals(type, other.type);
	}
	
	

}
