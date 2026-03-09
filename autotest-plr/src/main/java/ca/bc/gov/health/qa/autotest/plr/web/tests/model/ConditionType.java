package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum ConditionType {
    LOC("LOC - Location"),
    OTH("OTH - Other Conditions"),
    NON_RX("NON-RX - Non-Prescribing"),
    EXP("EXP - Expertise"),
    PRAC("PRAC - Practice Restriction"),
    HON("HON - Honorary"),
    LMCC("LMCC - Lic. of Med.Council of Canada"),
    MCCEE("MCCEE - Med. Counc. Canada Eval. Exam"),
    ACAD("ACAD - Academic"),
    AF("AF - Armed Forces"),
    STANDARD("STANDARD - Standard"),
    ASSC("ASSC - Associate"),
    REGISTRY("REGISTRY - Registry"),
    EDUD("EDUD - Education"),
    FULLRESTRICT("FULLRESTRICT - Full license with Restrictions"),
    INTERNRESIDENT("INTERN/RESIDENT - Intern or Resident"),
    GPH("GPH - Graduate Permit Holder"),
    TIME("TIME - Time limitation"),
    SPECMED("SPECMED - Special Medical"),
    SPECREG("SPECREG - Special Register"),
    ADMIN("ADMIN - Adminstrative");

    private final String text;

    ConditionType(String text) { this.text = text; }

    /**
     * Gets the full text of the condition type, e.g. "LOC - Location".
     * @return the full text of the condition type
     */
    public String getText() { return this.text; }

    /**
     * Gets the start text of the condition type, e.g. "LOC" from "LOC - Location".
     * @return the start text of the condition type
     */
    public String getStartText() { return this.text.split(" ")[0]; }

    /**
     * Gets the end text of the condition type, e.g. "Location" from "LOC - Location".
     * @return the end text of the condition type
     */
    public String getEndText() { return this.text.split(" ")[2]; }

    /**
     * Gets the data field representation of the condition type, e.g. "Location (LOC)" from "LOC - Location".
     * @param text the full text of the condition type
     * @return the data field representation of the condition type
     */
    public static ConditionType fromString(String text) {
        for (ConditionType b : ConditionType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
