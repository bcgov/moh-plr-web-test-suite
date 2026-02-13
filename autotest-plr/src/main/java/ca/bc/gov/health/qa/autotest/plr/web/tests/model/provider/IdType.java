package ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider;

/**
 * Enumeration for ID Types
 */
public enum IdType {
    /** Common Party Number */
    CPN("CPN - Common Party Number"),
    /** Internal Provider Code */
    IPC("IPC - Internal Provider Code"),
    /** Organization ID */
    ORGID("ORGID - Organization");

    private final String text;

    IdType(String text) {
        this.text = text;
    }

    /**
     * Get the text representation of the IdType
     * @return the text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get IdType from string
     * @param text string value
     * @return IdType enum
     */
    public static IdType fromString(String text) {
        for (IdType b : IdType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
