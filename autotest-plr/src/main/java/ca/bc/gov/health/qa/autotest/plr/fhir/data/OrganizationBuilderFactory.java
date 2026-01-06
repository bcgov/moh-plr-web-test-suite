package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;

/**
 * Factory responsible for creating and configuring {@link MaintainOrgBuilder}
 * instances using generated test data from {@link OrganizationDataGenerator}.
 * based on configurations sent from {@link OrganizationMaintainConfig}
 * 
 */
public class OrganizationBuilderFactory {

    private final OrganizationDataGenerator dataGen;

    /**
     * Creates a new factory with the provided data generator.
     * @param dataGen singleton (or custom) data generator to supply random values
     */
    public OrganizationBuilderFactory(OrganizationDataGenerator dataGen) {
        this.dataGen = dataGen;
    }

    /**
     * Builds a facility builder with a freshly created {@link OrganizationMaintainConfig}.
     * @return configured builder
     */
    public MaintainOrgBuilder build() { return build(new OrganizationMaintainConfig()); }

    /**
     * Build a {@link MaintainOrgBuilder} using the provided configuration.
     * @param config organization configuration
     * @return populated builder
     */
    public MaintainOrgBuilder build(OrganizationMaintainConfig config) {
        MaintainOrgBuilder builder = new MaintainOrgBuilder();

        if (config.isIdentifierEnabled()) {
            builder.addIdentifier(IdentifierType.ORGID, dataGen.generateNumericId());
        }
        if (config.isNameEnabled()) {
            builder.name(dataGen.generateName());
        }
        // role type always present in config; no conditional needed
        builder.roleType(config.getRoleType());
        // If HDS role type selected, assign a random HDS classification
        if (OrgRoleType.HDS.equals(config.getRoleType())) {
            builder.hdsType(dataGen.randomHdsType());
        }
        if (config.isAddressEnabled()) {
            String[] addr = dataGen.generateAddress();
            builder.addAddress("physical", "BC", addr[0], addr[1], addr[2]);
        }
        // Telecom purpose fixed to BC for simplicity
        if (config.isPhoneEnabled())  builder.addTelecom("phone", "MC", dataGen.generatePhoneNumber());
        if (config.isMobileEnabled()) builder.addTelecom("sms",   "OC", dataGen.generatePhoneNumber());
        if (config.isPagerEnabled())  builder.addTelecom("pager", "HC", dataGen.generatePhoneNumber());
        if (config.isModemEnabled())  builder.addTelecom("other", "CC", dataGen.generatePhoneNumber());
        if (config.isFaxEnabled())    builder.addTelecom("fax",   "DC", dataGen.generatePhoneNumber());
        if (config.isEmailEnabled())  builder.addTelecom("email", "FC", dataGen.generateEmailAddress());
        if (config.isWebsiteEnabled())builder.addTelecom("url",   "BC", dataGen.generateWebsiteUrl());
        if (config.isFtpEnabled())    builder.addTelecom("url",   "MC", dataGen.generateFtpUrl());

        if (config.isAliasEnabled()) {
            builder.alias("Alias " + dataGen.generateNumericId().substring(0,6));
        }
        if (config.isConfidentialityEnabled()) {
            builder.confidentiality(true);
        }

        for (int i = 0; i < config.getNoteCount(); i++) {
            builder.addNote(dataGen.generateNote());
        }
        for (int i = 0; i < config.getStatusCount(); i++) {
            builder.addStatus("LIC", "ACTIVE", "GS");
        }
        return builder;
    }
}
