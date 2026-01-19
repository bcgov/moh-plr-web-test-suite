package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum OrganizationalProviderRoleType {

	ORG("ORG - Organization"), 
	HDS("HDS - Healthcare Delivery Site"),
	CLINIC("CLINIC - Clinic"),
	BUSINESS("BUSINESS - Corporation");	

	private String text;

	OrganizationalProviderRoleType(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static OrganizationalProviderRoleType fromString(String text) {
		for (OrganizationalProviderRoleType b : OrganizationalProviderRoleType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
