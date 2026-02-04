package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum CssFieldType {
    DROPDOWN_LIST("PropertyValueDL"),
    TEXT_FIELD("PropertyValueTF"),
    TEXT_AREA("PropertyValueTA"),
    CHECKBOX("PropertyValueCb");

    private final String cssField;

    CssFieldType(String cssField) {
        this.cssField = cssField;
    }

    public String getCssField() {
        return cssField;
    }
}
