package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;

/**
 * Factory responsible for creating and configuring {@link MaintainFacilityBuilder}
 * instances using generated test data from {@link FacilityDataGenerator}.
 * based on configurations sent from {@link FacilityMaintainConfig}
 * 
 */
public class FacilityBuilderFactory {

    private final FacilityDataGenerator dataGen;

    /**
     * Creates a new factory with the provided data generator.
     * @param dataGen singleton (or custom) data generator to supply random values
     */
    public FacilityBuilderFactory(FacilityDataGenerator dataGen) {
        this.dataGen = dataGen;
    }

    /**
     * Builds a facility builder with a freshly created {@link FacilityMaintainConfig}.
     * @return configured builder
     */
    public MaintainFacilityBuilder build() {
        return build(new FacilityMaintainConfig());
    }

    /**
     * Build a {@link MaintainFacilityBuilder} using the provided configuration.
     * Optional attributes are added based on enabled flags; notes and organization
     * relationships are generated according to their counts.
     *
     * @param config configuration describing which attributes and counts to include
     * @return populated MaintainFacilityBuilder ready for submission
     */
    public MaintainFacilityBuilder build(FacilityMaintainConfig config) {
        MaintainFacilityBuilder builder = new MaintainFacilityBuilder();

        if(config.isIdentifierEnabled()){
            builder.identifier(dataGen.generateNumericId());
        }
        if (config.isNameEnabled()) {
            builder.name(dataGen.generateName());
        }
        if (config.isAddressEnabled()) {
            String[] addr = dataGen.generateAddress();
            builder.addAddress(addr[0], addr[1], addr[2]);
        }
        if (config.isPhoneEnabled()) {
            builder.addTelecom("phone", dataGen.generatePhoneNumber());
        }
        if (config.isMobileEnabled()) {
            builder.addTelecom("sms", dataGen.generatePhoneNumber());
        }
        if (config.isPagerEnabled()) {
            builder.addTelecom("pager", dataGen.generatePhoneNumber());
        }
        if (config.isModemEnabled()) {
            builder.addTelecom("other", dataGen.generatePhoneNumber());
        }
        if (config.isEmailEnabled()) {
            builder.addTelecom("email", dataGen.generateEmailAddress());
        }
        if (config.isFaxEnabled()) {
            builder.addTelecom("fax", dataGen.generatePhoneNumber());
        }
        if (config.isWebsiteEnabled()) {
            builder.addTelecom("url", dataGen.generateWebsiteUrl());
        }
        if (config.isFtpEnabled()) {
            builder.addTelecom("url", dataGen.generateFtpUrl());
        }
        if (config.isDescriptionEnabled()) {
            builder.description(dataGen.generateDescription());
        }

        int noteCount = config.getNoteCount();
        for (int i = 0; i < noteCount; i++) {
            builder.addNote(dataGen.generateNote());
        }
        // relationships pending future implementation using config.getRelationshipCount()
        return builder;
    }
}
