package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import java.util.LinkedHashMap;
import java.util.Objects;

import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import static org.testng.Assert.*;

/**
 * Identifier data block content
 */
public class Identifier {
	private String facilityType;
	private String identifier;
	private String idType;
	private String effectiveFrom;
	private String effectiveTo;
	private String endReason;
	private String dataSource;
	private String dbCreated;
	private String dbExpired;
	private String dataOwnerCode;
	
	

	/**
	 * Gets the identifier value.
	 *
	 * @return the identifier string
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the identifier type.
	 *
	 * @return the identifier type
	 */
	public String getIdType() {
		return idType;
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
	 * Constructs an Identifier based on the result of a newly generated facility from FHIR.
	 * TODO add specifications for these fields within the builder as much as possible in future versions
	 *
	 * @param fhirFacility	the facility to create the identifier for
	 */
	public Identifier(MaintainFacilityBuilder fhirFacility) {
		super();

		this.facilityType = ViewFacilityConstants.IDENTIFIER_FACILITY_TYPE_DEFAULT;
		this.identifier = fhirFacility.getIdentifier();
		this.idType = ViewFacilityConstants.IDENTIFIER_IDENTIFIER_TYPE_DEFAULT;
		this.effectiveFrom = fhirFacility.getDate();
		this.effectiveTo = "";
		this.endReason = "";
		this.dataSource = ViewFacilityConstants.DATA_SOURCE_DEFAULT;
		this.dbCreated = fhirFacility.getDate();
		this.dbExpired = "";
		this.dataOwnerCode = ViewFacilityConstants.DATA_OWNER_CODE_DEFAULT;
	}


	/**
	 * Constructs an Identifier from a JSON object.
	 *
	 * @param jsonData the JSON data containing fields
	 */
	public Identifier(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

        assertFalse(jsonData.isNull("Facility Type"));
		this.facilityType = jsonData.getString("Facility Type");
        assertFalse(jsonData.isNull("Identifier"));
		this.identifier = jsonData.getString("Identifier");
        assertFalse(jsonData.isNull("Identifier Type"));
		this.idType = jsonData.getString("Identifier Type");

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
	 * Constructs an Identifier from a map of label to value.
	 *
	 * @param map the map with keys matching UI labels
	 */
	public Identifier(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		assertNotNull(map.get("Facility Type"));
		this.facilityType = map.get("Facility Type");
		assertNotNull(map.get("Identifier"));
		this.identifier = map.get("Identifier");
		assertNotNull(map.get("Identifier Type"));
		this.idType = map.get("Identifier Type");
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
		return Objects.hash(dataOwnerCode, dataSource, dbCreated, dbExpired, effectiveFrom, effectiveTo, endReason,
				facilityType, idType, identifier);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifier other = (Identifier) obj;
		return Objects.equals(dataOwnerCode, other.dataOwnerCode) && Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(dbCreated, other.dbCreated) && Objects.equals(dbExpired, other.dbExpired)
				&& Objects.equals(effectiveFrom, other.effectiveFrom) && Objects.equals(effectiveTo, other.effectiveTo)
				&& Objects.equals(endReason, other.endReason) && Objects.equals(facilityType, other.facilityType)
				&& Objects.equals(idType, other.idType) && Objects.equals(identifier, other.identifier);
	}
	
	/*@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Identifier target = (Identifier) obj;
        return ( StringUtils.equals(this.identifier, target.identifier) &&
        		StringUtils.equals(this.dataOwnerCode, target.dataOwnerCode)	&&
        		StringUtils.equals(this.dataSource, target.dataSource)	&&
        		StringUtils.equals(this.dbCreated, target.dbCreated)	&&
        		StringUtils.equals(this.dbExpired, target.dbExpired)    &&
        		StringUtils.equals(this.effectiveFrom, target.effectiveFrom)	&&
        		StringUtils.equals(this.effectiveTo, target.effectiveTo)	&&
        		StringUtils.equals(this.endReason, target.endReason)	&&
        		StringUtils.equals(this.facilityType, target.facilityType)	&&
        		StringUtils.equals(this.idType, target.idType)	);
	}*/
	
	
	

}
