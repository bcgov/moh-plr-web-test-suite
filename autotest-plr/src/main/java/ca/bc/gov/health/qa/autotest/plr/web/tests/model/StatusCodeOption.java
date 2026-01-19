package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum StatusCodeOption {
	CANCELLED("CANCELLED - Cancelled"), 
	ACTIVE("ACTIVE - Active"), 
	TERMINATED("TERMINATED - Terminated"), 
	INACTIVE("INACTIVE - Inactive"), 
	SUSPENDED("SUSPENDED - Suspended"), 
	NULLIFIED("NULLIFIED - Nullified"),
	PENDING("PENDING - Pending"),
	UNKNOWN("UNKNOWN - Unknown");
	
	

	private String text;

	StatusCodeOption(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static StatusCodeOption fromString(String text) {
		for (StatusCodeOption b : StatusCodeOption.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
