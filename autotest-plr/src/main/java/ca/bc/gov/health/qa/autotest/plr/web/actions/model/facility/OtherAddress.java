package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.json.JSONObject;

public class OtherAddress {
	String validationStatus;
	String addressType;
	String addressPurpose;
	String addressLine1;
	String addressLine2;
	String addressLine3;
	String city;
	String stateProv;
	String postalZipCode;
	String country;
	String effectiveFrom;
	String effectiveTo;
	String endReason;
	String dataSource;
	String dbCreated;
	String dbExpired;
	String dataOwnerCode;
	
	
	public String getAddressTypeSummary() {
		if(addressType==null)return addressType;
		return addressType.split("(")[0].stripTrailing();
	}
	
	public String getAddressPurposeSummary() {
		if(addressPurpose==null)return addressPurpose;
		return addressPurpose.split("(")[0].stripTrailing();
	}
	
	public String getAddressLine1() {
		return addressLine1;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getStateProv() {
		return stateProv;
	}
	
	public String getDataOwnerCode() {
		return dataOwnerCode;
	}
	
	public OtherAddress(String validationStatus, String addressType, String addressPurpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String stateProv, String postalZipCode,
			String country, String effectiveFrom, String effectiveTo, String endReason, String dataSource,
			String dbCreated, String dbExpired, String dataOwnerCode) {
		super();
		this.validationStatus = validationStatus;
		this.addressType = addressType;
		this.addressPurpose = addressPurpose;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.city = city;
		this.stateProv = stateProv;
		this.postalZipCode = postalZipCode;
		this.country = country;
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
		return Objects.hash(addressLine1, addressLine2, addressLine3, addressPurpose, addressType, city, country,
				dataOwnerCode, dataSource, dbCreated, dbExpired, effectiveFrom, effectiveTo, endReason, postalZipCode,
				stateProv, validationStatus);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OtherAddress other = (OtherAddress) obj;
		return Objects.equals(addressLine1, other.addressLine1) && Objects.equals(addressLine2, other.addressLine2)
				&& Objects.equals(addressLine3, other.addressLine3)
				&& Objects.equals(addressPurpose, other.addressPurpose)
				&& Objects.equals(addressType, other.addressType) && Objects.equals(city, other.city)
				&& Objects.equals(country, other.country) && Objects.equals(dataOwnerCode, other.dataOwnerCode)
				&& Objects.equals(dataSource, other.dataSource) && Objects.equals(dbCreated, other.dbCreated)
				&& Objects.equals(dbExpired, other.dbExpired) && Objects.equals(effectiveFrom, other.effectiveFrom)
				&& Objects.equals(effectiveTo, other.effectiveTo) && Objects.equals(endReason, other.endReason)
				&& Objects.equals(postalZipCode, other.postalZipCode) && Objects.equals(stateProv, other.stateProv)
				&& Objects.equals(validationStatus, other.validationStatus);
	}
	
	
	public OtherAddress(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

		assertTrue(!jsonData.isNull("Validation Status"));
		this.validationStatus = jsonData.getString("Validation Status");
		assertTrue(!jsonData.isNull("Address Type"));
		this.addressType = jsonData.getString("Address Type");
		assertTrue(!jsonData.isNull("Address Purpose"));
		this.addressPurpose = jsonData.getString("Address Purpose");
		
		if (!jsonData.isNull("Address Line 1 "))
			this.addressLine1 = jsonData.getString("Address Line 1");
		if (!jsonData.isNull("Address Line 2"))
			this.addressLine2 = jsonData.getString("Address Line 2");
		if (!jsonData.isNull("Address Line 3"))
			this.addressLine3 = jsonData.getString("Address Line 3");
		if (!jsonData.isNull("City"))
			this.city = jsonData.getString("City");
		if (!jsonData.isNull("State/Prov"))
			this.stateProv = jsonData.getString("State/Prov");
		if (!jsonData.isNull("Postal/Zip Code"))
			this.postalZipCode = jsonData.getString("Postal/Zip Code");
		if (!jsonData.isNull("Country"))
			this.country = jsonData.getString("Country");


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

	public OtherAddress(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		
		assertNotNull(map.get("Validation Status"));
		this.validationStatus = map.get("Validation Status");
		assertNotNull(map.get("Address Type"));
		this.addressType = map.get("Address Type");
		assertNotNull(map.get("Address Purpose"));
		this.addressPurpose = map.get("Address Purpose");
		
		this.addressLine1 = map.get("Address Line 1") == null ? null : map.get("Address Line 1");
		this.addressLine2 = map.get("Address Line 2") == null ? null : map.get("Address Line 2");
		this.addressLine3 = map.get("Address Line 3") == null ? null : map.get("Address Line 3");
		this.city = map.get("City") == null ? null : map.get("City");
		this.stateProv = map.get("State/Prov") == null ? null : map.get("State/Prov");
		this.postalZipCode = map.get("Postal/Zip Code") == null ? null : map.get("Postal/Zip Code");
		this.country = map.get("Country") == null ? null : map.get("Country");
		
		this.effectiveFrom = map.get("Effective From") == null ? null : map.get("Effective From");
		this.effectiveTo = map.get("Effective To") == null ? null : map.get("Effective To");
		this.endReason = map.get("End Reason") == null ? null : map.get("End Reason");
		this.dataSource = map.get("Data Source") == null ? null : map.get("Data Source");
		this.dbCreated = map.get("DB Created") == null ? null : map.get("DB Created");
		this.dbExpired = map.get("DB Expired") == null ? null : map.get("DB Expired");
		this.dataOwnerCode = map.get("Data Owner Code") == null ? null : map.get("Data Owner Code");
	}

}
