package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

public enum FacilitySection {
    /**
     * Identifiers
     */
    IDENTIFIERS("identifiersPanel", "Identifiers"),

    /**
     * Names (Facility)
     */
    NAMES("nameFacPanel", "Names"),

    /**
     * Civic Addresses
     */
    CIVIC_ADDRESSES("civicAddressesPanel", "Civic Addresses"),

    /**
     * Other Addresses
     */
    OTHER_ADDRESS("addressesPanel", "Other Address"),

    /**
     * Telecommunications
     */
    TELECOMMUNICATIONS("telecommunicationsPanel", "Telecommunications"),

    /**
     * Electronic Addresses
     */
    ELECTRONIC_ADDRESSES("eAddressesPanel", "Electronic Addresses"),

    /**
     * Organization Relationships
     */
    ORGANIZATION_RELATIONSHIPS("organizationRelationshipsPanel", "Organization Relationships"),

    /**
     * Notes
     */
    NOTES("notesPanel", "Notes");

    private final String panelId_;
    private final String title_;

    FacilitySection(String panelId, String title)
    {
        panelId_ = panelId;
        title_ = title;
    }

    /**
     * Gets the Panel ID of the facility section div element in the View Facility page
     *
     * @return  a string of the Panel ID in div elements
     */
    public String getPanelId_() { return panelId_; }

    /**
     * Gets the title of the facility section
     *
     * @return  a string of the facility section's title
     */
    public String getTitle() { return title_; }
}
