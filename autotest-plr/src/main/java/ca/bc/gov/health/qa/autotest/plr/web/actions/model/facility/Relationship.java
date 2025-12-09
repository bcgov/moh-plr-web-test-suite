package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.json.JSONObject;

/**
 * TODO (KD)
 */
public class Relationship {
	private String relationshipIdentifier;
	private String relationshipType;
	private String relatedOrganizationName;
	private String relatedOrganizationIdentifier;	
	private String effectiveFrom;
	private String effectiveTo;
	private String endReason;
	private String dataSource;
	private String dbCreated;
	private String dbExpired;
	private String dataOwnerCode;
	
	
	/**
	 * Gets the relationship identifier.
	 *
	 * @return the relationship identifier
	 */
	public String getRelationshipIdentifier() {
		return relationshipIdentifier;
	}
	/**
	 * Gets the related organization identifier.
	 *
	 * @return the related organization identifier
	 */
	public String getRelatedOrganizationIdentifier() {
		return relatedOrganizationIdentifier;
	}
	/**
	 * Gets a simplified Relationship Type value (text before parenthesis).
	 *
	 * @return the relationship type summary or the original value if null
	 */
	public String getRelationshipTypeSummary() {
		if(relationshipType==null)return relationshipType;
		return relationshipType.split("\\(")[0].stripTrailing();
        
	}
	/**
	 * Gets the related organization name.
	 *
	 * @return the related organization name
	 */
	public String getRelatedOrganizationName() {
		return relatedOrganizationName;
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
	 * Gets the related provider role type.
	 *
	 * @return the role type constant
	 */
	public String getRelatedProviderRoleType() {
		return "ORG";
	}
	    /**
	     * Constructs a Relationship using explicit values.
	     *
	     * @param relationshipIdentifier the relationship identifier
	     * @param relationshipType the relationship type
	     * @param relatedOrganizationName related organization name
	     * @param relatedOrganizationIdentifier related organization identifier
	     * @param effectiveFrom effective from date
	     * @param effectiveTo effective to date
	     * @param endReason end reason
	     * @param dataSource data source
	     * @param dbCreated database created timestamp
	     * @param dbExpired database expired timestamp
	     * @param dataOwnerCode data owner code
	     */
	    public Relationship(String relationshipIdentifier, String relationshipType, String relatedOrganizationName,
		    String relatedOrganizationIdentifier, String effectiveFrom, String effectiveTo, String endReason,
		    String dataSource, String dbCreated, String dbExpired, String dataOwnerCode) {
		super();
		this.relationshipIdentifier = relationshipIdentifier;
		this.relationshipType = relationshipType;
		this.relatedOrganizationName = relatedOrganizationName;
		this.relatedOrganizationIdentifier = relatedOrganizationIdentifier;
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
				relatedOrganizationIdentifier, relatedOrganizationName, relationshipIdentifier, relationshipType);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relationship other = (Relationship) obj;
		return Objects.equals(dataOwnerCode, other.dataOwnerCode) && Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(dbCreated, other.dbCreated) && Objects.equals(dbExpired, other.dbExpired)
				&& Objects.equals(effectiveFrom, other.effectiveFrom) && Objects.equals(effectiveTo, other.effectiveTo)
				&& Objects.equals(endReason, other.endReason)
				&& Objects.equals(relatedOrganizationIdentifier, other.relatedOrganizationIdentifier)
				&& Objects.equals(relatedOrganizationName, other.relatedOrganizationName)
				&& Objects.equals(relationshipIdentifier, other.relationshipIdentifier)
				&& Objects.equals(relationshipType, other.relationshipType);
	}
	
	/**
	 * Constructs a Relationship from a JSON object.
	 *
	 * @param jsonData the JSON data containing fields
	 */
	public Relationship(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

		assertTrue(!jsonData.isNull("Relationship Identifier"));
		this.relationshipIdentifier = jsonData.getString("Relationship Identifier");
		assertTrue(!jsonData.isNull("Relationship Type"));
		this.relationshipType = jsonData.getString("Relationship Type");
		assertTrue(!jsonData.isNull("Related Organization Name"));
		this.relatedOrganizationName = jsonData.getString("Related Organization Name");
		assertTrue(!jsonData.isNull("Related Organization Identifier"));
		this.relatedOrganizationIdentifier = jsonData.getString("Related Organization Identifier");
		


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
	 * Constructs a Relationship from a map of label to value.
	 *
	 * @param map the map with keys matching UI labels
	 */
	public Relationship(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		
		assertNotNull(map.get("Relationship Identifier"));
		this.relationshipIdentifier = map.get("Relationship Identifier");
		assertNotNull(map.get("Relationship Type"));
		this.relationshipType = map.get("Relationship Type");
		assertNotNull(map.get("Related Organization Name"));
		this.relatedOrganizationName = map.get("Related Organization Name");
		assertNotNull(map.get("Related Organization Identifier"));
		this.relatedOrganizationIdentifier = map.get("Related Organization Identifier");
		
		this.effectiveFrom = map.get("Effective From") == null ? null : map.get("Effective From");
		this.effectiveTo = map.get("Effective To") == null ? null : map.get("Effective To");
		this.endReason = map.get("End Reason") == null ? null : map.get("End Reason");
		this.dataSource = map.get("Data Source") == null ? null : map.get("Data Source");
		this.dbCreated = map.get("DB Created") == null ? null : map.get("DB Created");
		this.dbExpired = map.get("DB Expired") == null ? null : map.get("DB Expired");
		this.dataOwnerCode = map.get("Data Owner Code") == null ? null : map.get("Data Owner Code");
	}


}
