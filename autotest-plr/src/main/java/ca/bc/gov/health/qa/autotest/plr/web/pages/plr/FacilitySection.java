package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

public enum FacilitySection {
    IDENTIFIERS("identifiersPanel", "Identifiers"),

    NAMES("nameFacPanel", "Names"),

    CIVIC_ADDRESSES("civicAddressesPanel", "Civic Addresses"),

    OTHER_ADDRESS("addressesPanel", "Other Address"),

    TELECOMMUNICATIONS("telecommunicationsPanel", "Telecommunications"),

    ELECTRONIC_ADDRESSES("eAddressesPanel", "Electronic Addresses"),

    ORGANIZATION_RELATIONSHIPS("organizationRelationshipsPanel", "Organization Relationships"),

    NOTES("notesPanel", "Notes");

    private final String panelId_;
    private final String title_;

    private FacilitySection(String panelId, String title)
    {
        panelId_ = panelId;
        title_ = title;
    }

    public String getPanelId_() { return panelId_; }

    public String getTitle() { return title_; }
}
