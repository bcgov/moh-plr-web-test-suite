package ca.bc.gov.health.qa.autotest.plr.fhir.data.organization;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrganizationProperties;

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
        // Generate up to MAX_STATUS_COUNT unique statuses with distinct statusClass values
        int desiredStatuses = Math.min(config.getStatusCount(), MaintainOrgBuilder.MAX_STATUS_COUNT);
        List<String> statusCodes = dataGen.generateUniqueStatusCodes(desiredStatuses);
        for (int i = 0; i < statusCodes.size(); i++) {
            String code = statusCodes.get(i);
            String reason = dataGen.reasonForStatus(code);
            String statusClass = MaintainOrgBuilder.STATUS_CLASSES_ORDER.get(i);
            builder.addStatus(statusClass, code, reason);
        }

        // ----------------------- OrganizationProperties -----------------------
        OrganizationProperties props = new OrganizationProperties();
        boolean anyProp = false;

        if (config.isClinicServicesEnabled()) {
            props.setClinicServices(dataGen.randomClinicServices());
            anyProp = true;
        }
        if (config.isClinicOwnerBusinessTypeEnabled()) {
            props.setClinicOwnerBusinessType(dataGen.randomClinicOwnerBusinessType());
            anyProp = true;
        }
        if (config.isClinicTypeEnabled()) {
            props.setClinicType(dataGen.randomClinicType());
            anyProp = true;
        }
        if (config.isClinicLegalBusinessNameEnabled()) {
            props.setClinicLegalBusinessName(dataGen.generateClinicLegalBusinessName());
            anyProp = true;
        }
        if (config.isPciFlagEnabled()) {
            props.setPciFlag(dataGen.generatePciFlag());
            anyProp = true;
        }
        if (config.getAddressUnitCount() > 0) {
            List<String> list = new ArrayList<>(config.getAddressUnitCount());
            for (int i = 0; i < config.getAddressUnitCount(); i++) {
                list.add(dataGen.generateAddressUnit());
            }
            props.setAddressUnit(list);
            anyProp = true;
        }
        if (config.getClinicHoursOfOperationCount() > 0) {
            List<String> list = new ArrayList<>(config.getClinicHoursOfOperationCount());
            for (int i = 0; i < config.getClinicHoursOfOperationCount(); i++) {
                list.add(dataGen.generateClinicHourEntry());
            }
            props.setClinicHoursOfOperation(list);
            anyProp = true;
        }
        if (config.getClinicOwnerNamesCount() > 0) {
            List<String> list = new ArrayList<>(config.getClinicOwnerNamesCount());
            for (int i = 0; i < config.getClinicOwnerNamesCount(); i++) {
                list.add(dataGen.generateClinicOwnerName());
            }
            props.setClinicOwnerNames(list);
            anyProp = true;
        }
        if (config.getPayeeNumberCount() > 0) {
            List<String> list = new ArrayList<>(config.getPayeeNumberCount());
            for (int i = 0; i < config.getPayeeNumberCount(); i++) {
                list.add(dataGen.generatePayeeNumber());
            }
            props.setPayeeNumber(list);
            anyProp = true;
        }

        if (anyProp) {
            builder.organizationProperties(props);
        }
        return builder;
    }
}
