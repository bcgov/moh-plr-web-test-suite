package ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider;

/**
 * Enumeration for Registry ID Types
 */
public enum RegIdType {
    /** Common Provider Number */
    CPN("CPN - Common Provider NUMBER"),
    /** Internal Provider Code */
    IPC("IPC - Internal Provider ID");

    private final String text;

    RegIdType(String text) { this.text = text; }

    /**
     * Get registry ID enum text
     * @return string value
     */
    public String getText() { return this.text; }

    /**
     * Get registry ID enu, from string
     * @param text string value
     * @return RegIdType enum
     */
    public static RegIdType fromString(String text) {
        for (RegIdType b : RegIdType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
