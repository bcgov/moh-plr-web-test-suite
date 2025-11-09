package ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Objects;

import org.json.JSONObject;

public class CivicAddress {
	String latitude;
	String longitude;
	String addressLine1;
	String addressLine2;
	String addressLine3;
	String city;
	String provinceState;
	String country;
	String healthAuthority;
	String healthServiceDeliveryArea;
	String localHealthArea;
	String primaryCareNetwork;
	String communityHealthServiceArea;
	
	public String getAddressLine1() {
		return addressLine1;
	}



	public String getCity() {
		return city;
	}



	public String getProvinceStateSummary() {
		if (provinceState==null)return null;
		String[] parts=provinceState.split("-");
		return parts[1].replaceAll("^[^A-Z]*", "");
	}

	public String getDataOwnerCode() {
		return "MOH";
	}


	public CivicAddress(String latitude, String longitude, String addressLine1, String addressLine2,
			String addressLine3, String city, String provinceState, String country, String healthAuthority,
			String healthServiceDeliveryArea, String localHealthArea, String primaryCareNetwork,
			String communityHealthServiceArea) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.city = city;
		this.provinceState = provinceState;
		this.country = country;
		this.healthAuthority = healthAuthority;
		this.healthServiceDeliveryArea = healthServiceDeliveryArea;
		this.localHealthArea = localHealthArea;
		this.primaryCareNetwork = primaryCareNetwork;
		this.communityHealthServiceArea = communityHealthServiceArea;
	}
	
	

	@Override
	public int hashCode() {
		return Objects.hash(addressLine1, addressLine2, addressLine3, city, communityHealthServiceArea, country,
				healthAuthority, healthServiceDeliveryArea, latitude, localHealthArea, longitude, primaryCareNetwork,
				provinceState);
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CivicAddress other = (CivicAddress) obj;
		return Objects.equals(addressLine1, other.addressLine1) && Objects.equals(addressLine2, other.addressLine2)
				&& Objects.equals(addressLine3, other.addressLine3) && Objects.equals(city, other.city)
				&& Objects.equals(communityHealthServiceArea, other.communityHealthServiceArea)
				&& Objects.equals(country, other.country) && Objects.equals(healthAuthority, other.healthAuthority)
				&& Objects.equals(healthServiceDeliveryArea, other.healthServiceDeliveryArea)
				&& Objects.equals(latitude, other.latitude) && Objects.equals(localHealthArea, other.localHealthArea)
				&& Objects.equals(longitude, other.longitude)
				&& Objects.equals(primaryCareNetwork, other.primaryCareNetwork)
				&& Objects.equals(provinceState, other.provinceState);
	}



	public CivicAddress(JSONObject jsonData) {
		super();
		assertNotNull(jsonData);

		if (!jsonData.isNull("Latitude"))
			this.latitude = jsonData.getString("Latitude");
		if (!jsonData.isNull("Longitude"))
			this.longitude = jsonData.getString("Longitude");
		
		
		if (!jsonData.isNull("Address Line 1 "))
			this.addressLine1 = jsonData.getString("Address Line 1");
		if (!jsonData.isNull("Address Line 2"))
			this.addressLine2 = jsonData.getString("Address Line 2");
		if (!jsonData.isNull("Address Line 3"))
			this.addressLine3 = jsonData.getString("Address Line 3");
		if (!jsonData.isNull("City"))
			this.city = jsonData.getString("City");
		if (!jsonData.isNull("Province / State"))
			this.provinceState = jsonData.getString("Province / State");
		if (!jsonData.isNull("Country"))
			this.country = jsonData.getString("Country");


		if (!jsonData.isNull("Health Authority"))
			this.healthAuthority = jsonData.getString("Health Authority");
		if (!jsonData.isNull("Health Service Delivery Area"))
			this.healthServiceDeliveryArea = jsonData.getString("Health Service Delivery Area");
		if (!jsonData.isNull("Local Health Area"))
			this.localHealthArea = jsonData.getString("Local Health Area");
		if (!jsonData.isNull("Primary Care Network"))
			this.primaryCareNetwork = jsonData.getString("Primary Care Network");
		if (!jsonData.isNull("Community Health Service Area") )
			this.communityHealthServiceArea = jsonData.getString("Community Health Service Area");
		

	}

	public CivicAddress(LinkedHashMap<String, String> map) {
		super();
		assertNotNull(map);
		
		this.latitude = map.get("Latitude") == null ? null : map.get("Latitude");
		this.longitude = map.get("Longitude") == null ? null : map.get("Longitude");
		
			
		this.addressLine1 = map.get("Address Line 1") == null ? null : map.get("Address Line 1");
		this.addressLine2 = map.get("Address Line 2") == null ? null : map.get("Address Line 2");
		this.addressLine3 = map.get("Address Line 3") == null ? null : map.get("Address Line 3");
		this.city = map.get("City") == null ? null : map.get("City");
		this.provinceState = map.get("Province / State") == null ? null : map.get("Province / State");
		this.country = map.get("Country") == null ? null : map.get("Country");
		
		this.healthAuthority = map.get("Health Authority") == null ? null : map.get("Health Authority");
		this.healthServiceDeliveryArea = map.get("Health Service Delivery Area") == null ? null : map.get("Health Service Delivery Area");
		this.localHealthArea = map.get("Local Health Area") == null ? null : map.get("Local Health Area");
		this.primaryCareNetwork = map.get("Primary Care Network") == null ? null : map.get("Primary Care Network");
		this.communityHealthServiceArea = map.get("Community Health Service Area") == null ? null : map.get("Community Health Service Area");
	
	}

}
