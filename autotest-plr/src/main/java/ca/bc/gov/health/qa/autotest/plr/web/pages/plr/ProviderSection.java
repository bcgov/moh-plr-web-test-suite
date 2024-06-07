package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;

/**
 * TODO (AZ) - doc
 */
public enum ProviderSection
{
    /**
     * Registry Identifiers
     */
    REGISTRY_IDENTIFIERS("regIdentifiersPanel", "Registry Identifiers"),

    /**
     * Identifiers
     */
    IDENTIFIERS("identifiersPanel", "Identifiers"),

    /**
     * Statuses
     */
    STATUSES("statusesPanel", "Statuses"),

    /**
     * Expertise
     */
    EXPERTISE("expertiseListPanel", "Expertise"),

    /**
     * Credentials
     */
    CREDENTIALS("credentialsPanel", "Credentials"),

    /**
     * Names (organization)
     */
    ORGANIZATION_NAMES("nameOrgPanel", "Names"),

    /**
     * Names (practitioner)
     */
    PRACTITIONER_NAMES("nameIndPanel", "Names"),

    /**
     * Addresses
     */
    ADDRESSES("addressesPanel", "Addresses"),

    /**
     * Telecommunications
     */
    TELECOMMUNICATIONS("telecommunicationsPanel", "Telecommunications"),

    /**
     * Electronic Addresses
     */
    ELECTRONIC_ADDRESSES("eAddressesPanel", "Electronic Addresses"),

    /**
     * Demographics
     */
    DEMOGRAPHICS("demographicDetailsPanel", "Demographics"),

    /**
     * Work Locations
     */
    WORK_LOCATIONS("workLocationsPanel", "Work Locations"),

    /**
     * Conditions
     */
    CONDITIONS("conditionsPanel", "Conditions"),

    /**
     * Disciplinary Actions
     */
    DISCIPLINARY_ACTIONS("disciplinaryActionsPanel", "Disciplinary Actions"),

    /**
     * Communication Preference
     */
    COMMUNICATION_PREFERENCE("informationRoutesPanel", "Communication Preference"),

    /**
     * Confidentiality
     */
    CONFIDENTIALITY("confidentialityListPanel", "Confidentiality"),

    /**
     * Provider Relationships
     */
    PROVIDER_RELATIONSHIPS("providerRelationshipsPanel", "Provider Relationships"),

    /**
     * Facility Relationships
     */
    FACILITY_RELATIONSHIPS("facilityRelationshipsPanel", "Facility Relationships"),

    /**
     * Registry User Relationships
     */
    REGISTRY_USER_RELATIONSHIPS("registryUserRelationshipsPanel", "Registry User Relationships"),

    /**
     * Notes
     */
    NOTES("notesPanel", "Notes"),

    /**
     * Organization Properties
     */
    ORGANIZATION_PROPERTIES("propertiesPanelId", "Organization Properties");

    private static final Map<ProviderType,Set<ProviderSection>> PROVIDER_TYPE_SECTION_MAP;
    static
    {
        final Set<ProviderSection> practitionerSectionSet = Collections.unmodifiableSet(EnumSet.of(
                REGISTRY_IDENTIFIERS,
                IDENTIFIERS,
                STATUSES,
                EXPERTISE,
                CREDENTIALS,
                PRACTITIONER_NAMES,
                ADDRESSES,
                TELECOMMUNICATIONS,
                ELECTRONIC_ADDRESSES,
                DEMOGRAPHICS,
                WORK_LOCATIONS,
                CONDITIONS,
                DISCIPLINARY_ACTIONS,
                COMMUNICATION_PREFERENCE,
                CONFIDENTIALITY,
                PROVIDER_RELATIONSHIPS,
                REGISTRY_USER_RELATIONSHIPS,
                NOTES));
        final Set<ProviderSection> organizationSectionSet = Collections.unmodifiableSet(EnumSet.of(
                REGISTRY_IDENTIFIERS,
                IDENTIFIERS,
                STATUSES,
                ORGANIZATION_NAMES,
                ADDRESSES,
                TELECOMMUNICATIONS,
                ELECTRONIC_ADDRESSES,
                WORK_LOCATIONS,
                PROVIDER_RELATIONSHIPS,
                FACILITY_RELATIONSHIPS,
                NOTES,
                ORGANIZATION_PROPERTIES));
        PROVIDER_TYPE_SECTION_MAP = Map.of(
                ProviderType.BC_PRACTITIONER,
                practitionerSectionSet,
                ProviderType.OOP_PRACTITIONER,
                practitionerSectionSet,
                ProviderType.ORGANIZATION,
                organizationSectionSet);
    }

    private static final Set<ProviderSection> REQUIRED_PROVIDER_SECTION_SET =
            Collections.unmodifiableSet(EnumSet.of(
                    REGISTRY_IDENTIFIERS,
                    IDENTIFIERS,
                    STATUSES,
                    ORGANIZATION_NAMES,
                    PRACTITIONER_NAMES,
                    ADDRESSES,
                    DEMOGRAPHICS));

    private final String panelId_;
    private final String title_;

    private ProviderSection(String panelId, String title)
    {
        panelId_ = panelId;
        title_   = title;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getPanelId()
    {
        return panelId_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getTitle()
    {
        return title_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     *
     * @return ???
     *
     * @throws IllegalStateException
     *         if the provider type is not supported
     */
    public static Set<ProviderSection> getProviderSectionSet(ProviderType providerType)
    {
        Set<ProviderSection> providerSectionSet = PROVIDER_TYPE_SECTION_MAP.get(providerType);
        if (providerSectionSet == null)
        {
            String msg = String.format("Unsupported provider type (%s).", providerType);
            throw new IllegalStateException(msg);
        }
        return providerSectionSet;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static Set<ProviderSection> getRequiredProviderSectionSet()
    {
        return REQUIRED_PROVIDER_SECTION_SET;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean isRequired()
    {
        return REQUIRED_PROVIDER_SECTION_SET.contains(this);
    }
}
