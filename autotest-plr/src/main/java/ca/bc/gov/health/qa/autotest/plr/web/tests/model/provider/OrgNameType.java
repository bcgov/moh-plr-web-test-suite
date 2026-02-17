package ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider;

/**
 * Enumeration for Organization Name Types
 */
public enum OrgNameType {
    /** Current Known Name */
    CURR("CURR - Current Known Name"),
    /** Credential Name */
    CRED("CRED - Credential Name");

    private final String text;

    OrgNameType(String text) { this.text = text; }

    /**
     * Get the text representation of the OrgNameType
     * @return the text
     */
    public String getText() { return text; }

    /**
     * Get OrgNameType from text
     * @param text the text
     * @return the OrgNameType
     */
    public static OrgNameType fromText(String text) {
        for (OrgNameType type : OrgNameType.values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No OrgNameType with text: " + text);
    }
}
