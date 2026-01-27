package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

/**
 * Enum representing different types of Organizational Provider Roles.
 */
public enum OrganizationalProviderRoleType {

	ORG("ORG - Organization"), 
	HDS("HDS - Healthcare Delivery Site"),
	CLINIC("CLINIC - Clinic"),
	BUSINESS("BUSINESS - Corporation");	

	private String text;

	OrganizationalProviderRoleType(String text) {
		this.text = text;
	}

	/**
	 * Gets the text representation of the OrganizationalProviderRoleType.
	 * @return the text representation
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Converts a string to its corresponding OrganizationalProviderRoleType enum value.
	 * @param text  the string representation of the OrganizationalProviderRoleType
	 * @return		the corresponding OrganizationalProviderRoleType enum value, or null if not found
	 */
	public static OrganizationalProviderRoleType fromString(String text) {
		for (OrganizationalProviderRoleType b : OrganizationalProviderRoleType.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
