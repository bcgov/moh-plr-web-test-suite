package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum EndReason {
	
	
	 CEASE("CEASE - Cease"), 
	 CHG("CHG - Change"),
	 CORR("CORR - Correct");

	private String text;

	EndReason(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static EndReason fromString(String text) {
		for (EndReason b : EndReason.values()) {
			if (b.text.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
	

}