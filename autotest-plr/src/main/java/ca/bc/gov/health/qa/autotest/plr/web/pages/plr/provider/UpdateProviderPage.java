package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderPage extends ViewProviderPage {

	private static final Logger LOG = ExecutionLogManager.getLogger();

	public static final Map<ProviderSection, ProviderDialog> DIALOG_MAP = Map.of(
			ProviderSection.IDENTIFIERS, new ProviderDialog("maintainIdDialog", "maintainIdentifierForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "","Add"),
			ProviderSection.NOTES, new ProviderDialog("maintainNoteDialog", "maintainNoteForm", "effectiveFromDate",
					"effectiveToDate", "idNoteSubmitButton", "endReasonCode","Add a new Note"),
			ProviderSection.ORGANIZATION_RELATIONSHIPS, new ProviderDialog("maintainOrganizationRelationshipDialog", "maintainOrgRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "orgRelationshipSubmitButton", "EndReasonType","Add a new Organization Relationship"),
			ProviderSection.TELECOMMUNICATIONS, new ProviderDialog("maintainTelecomDialog", "maintainTelecomForm", "effectiveFromDate",
					"effectiveToDate", "idTeleSubmitButton", "EndReasonType","Add a new Telecommunication"),
			ProviderSection.ELECTRONIC_ADDRESSES, new ProviderDialog("maintainElectronicAddressDialog", "maintainElectronicAddressForm", "effectiveStartDate",
					"effectiveEndDate", "electronicAddressSubmitButton", "EndReasonType","Add a new Electronic Address"),
			ProviderSection.CONDITIONS, new ProviderDialog("maintainConditionDialog", "maintainConditionForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add a new Condition"),
			ProviderSection.DISCIPLINARY_ACTIONS, new ProviderDialog("maintainDisActionDialog", "maintainDisActionForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add a new Disciplinary Action"),
			ProviderSection.WORK_LOCATIONS, new ProviderDialog("maintainWorkLocationDialog", "maintainWorkLocationForm", "effectiveFromDate",
					"effectiveToDate", "idWLSubmitButton", "EndReasonType","Add a new Work Location"),
			ProviderSection.PROVIDER_RELATIONSHIPS, new ProviderDialog("maintainProviderRelationshipDialog", "maintainProviderRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "providerRelationshipSubmitButton", "EndReasonType","Add a new Provider Relationship"),
			ProviderSection.REGISTRY_USER_RELATIONSHIPS, new ProviderDialog("maintainRegUserRelationshipDialog", "maintainRegUserRelationshipForm", "effectiveFromDate",
					"effectiveToDate", "idRegUserRelationshipSubmitButton", "endReasonCode","Add a new Registry User Relationship")
			);

	public UpdateProviderPage(SeleniumSession selenium, URI uri) {
		super(selenium, uri);
	}

	/**
	 * Open provider detail view page
	 *
	 * @param pauthId the provider authentication ID
	 */
	public void openProvider(String pauthId) {
		requireNonNull(pauthId);
		if (uri_ != null) {
			selenium_.getDriver().get(UriUtils.getUriWithQuery(uri_, "p=" + pauthId).toString());
			waitForReady();
		} else {
			throw new UnsupportedOperationException("Page URL is not specified.");
		}
	}

    /**
     * Tries to wait some number of seconds. Will fail the test used in if interrupted.
	 * TODO this should be used as little as possible in favour of selenium implicit waits.
     *
     * @param second the number of seconds to wait.
     */
	public void waitSeconds(int second) {
		try {
			Thread.sleep(1000L * second);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * get Data Block Header Update Button Selector
	 *
	 * @param section the provider section
	 * @param index the data block index
	 * @return CSS selector of Data Block Header Update Button
	 */
	protected String getDataBlockHeaderUpdateButtonSelector(ProviderSection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions "
				+ " > span >a >img[title^='Update']";
	}

	/**
	 * get Work Location Data Block Update Button Selector
	 * the work location data block has a different structure, so it has its own method to get update button selector
	 * @param index the data block index
	 * @return CSS selector of Work Location Data Block Update Button
	 */
	protected String getWorkLocationUpdateButtonSelector(int index) {
		return getDataBlockSelector(ProviderSection.WORK_LOCATIONS, index) + " > div.ui-panel-content > div > div "
				+ "> div.ui-panel:nth-of-type(1) > div.ui-panel-content > table > tbody > tr > td > "
				+ "div > div.ui-panel-titlebar > div.ui-panel-actions > a > img[title^='Update']";
	}

	/**
	 * Using a Javascript Executor to press button
	 * @param button the button element
	 */
	private void clickButtonWait(WebElement button) {
		try {
			button.click();
		} catch (ElementClickInterceptedException | StaleElementReferenceException e) {
			JavascriptExecutor js = (JavascriptExecutor) selenium_.getDriver();
			js.executeScript("arguments[0].click();", button);
		}
	}

	/**
	 * Click the add button in the header of a provider section, and wait for the dialog to be visible
	 * @param section the provider section
	 * @return the WebElement of the dialog content after clicking the add button and waiting for the dialog to be visible
	 */
	public WebElement clickHeaderAddButton(ProviderSection section)
	{
		String title = DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss = getSectionSelector(section) + " > div > div > a > img[title='" + title + "']";

		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(clickElementCss)));
		WebElement clickElement = selenium_.findElement(By.cssSelector(clickElementCss));
		selenium_.scrollIntoView(clickElement);
		try {
			clickElement.click();
		} catch (StaleElementReferenceException | ElementClickInterceptedException e) {
			waitSeconds(2);
			clickElement = selenium_.findElement(By.cssSelector(clickElementCss));
			clickElement.click();
		}

		String dialogCss = getDialogCss(section);
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		return selenium_.findElementByCss(dialogCss);
	}

    /**
     * Attempts to click the update button on a specified data block
     *
     * @param section the provider section to find the data block's update button within
     * @param index the specific index of the data block to find and click the update button for
     */
	public void clickDataBlockUpdateButton(ProviderSection section, int index) {
		String selectCss;
		if (section.equals(ProviderSection.WORK_LOCATIONS)) {
			expandDataBlock(section, index, true);
			selectCss = getWorkLocationUpdateButtonSelector(index);
		} else selectCss = getDataBlockHeaderUpdateButtonSelector(section, index);

		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(selectCss)));
		WebElement updateButton = selenium_.findElementByCss(selectCss);
		selenium_.scrollIntoView(updateButton);

		try {
			updateButton.click();
		} catch (StaleElementReferenceException e) {
			waitSeconds(2);
			updateButton.click();
		}
		String dialogCss = getDialogCss(section);
		WebElement visibleElement = selenium_
				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Get Dialog Css selector
	 *
	 * @param section the provider section
	 * @return string of dialog CSS selector
	 */
	private String getDialogCss(ProviderSection section) {
		String dialogName = DIALOG_MAP.get(section).getDialogName();
		String formName = DIALOG_MAP.get(section).getFormName();

		return "div#" + dialogName + " > div#" + dialogName + "_content" + " > form#" + formName;
	}

	private boolean getChkBoxState(ProviderSection section, WebElement checkBox)
	{
		if (section.equals(ProviderSection.WORK_LOCATIONS)) {
			String chkBoxClass = checkBox.getAttribute("class");
			if (chkBoxClass != null) return chkBoxClass.contains("ui-icon-check");
		}
		String ariaChecked = checkBox.getAttribute("aria-checked");
		return "true".equals(ariaChecked);
	}

	/**
	 * Check if the checkbox is checked
	 * @param section the provider section
	 * @param checkBoxName the checkbox field name
	 * @return true if the checkbox is checked, false otherwise
	 */
	public boolean isCheckBoxChecked(ProviderSection section, String checkBoxName)
	{
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String checkBoxCss = dialogCss + " > div#" + formName + "\\:" + checkBoxName;
		WebElement checkBox = selenium_.findElement(By.cssSelector(checkBoxCss));
		return getChkBoxState(section, checkBox);
	}

	/**
	 * set End Reason By Visible Text
	 * @param section the provider section
	 * @param visibleText the visible text to select
	 */
	private void setEndReasonByVisibleText(ProviderSection section, String visibleText) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String endReasonName = DIALOG_MAP.get(section).getEndReasonName();

		DropDownMenu endReasonDrop = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + endReasonName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + endReasonName + "_panel"));

		endReasonDrop.selectItem(visibleText);
	}

	/**
	 * set Drop down List By Visible Text
	 *
	 * @param section the provider section
	 * @param dropdownName the dropdown field name
	 * @param visibleText the visible text to select
	 */
	private void setDropdownListByVisibleText(ProviderSection section, String dropdownName, String visibleText) {
		if (StringUtils.isEmpty(visibleText))
			return;
		String formName = DIALOG_MAP.get(section).getFormName();

		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));

		dropdownMenu.selectItem(visibleText);
	}

	/**
	 * get Drop down List Options
	 * @param section the provider section
	 * @param dropdownName the dropdownfield name
	 */
	public List<String> getDropdownListOptions(ProviderSection section, String dropdownName) {
		String formName = DIALOG_MAP.get(section).getFormName();

		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));

		dropdownMenu.expandItemPanel(true);
		return dropdownMenu.grabItemList();
	}

	/**
	 * Click Dialog Submit Button
	 *
	 * @param section the provider section
	 */
	public void clickDialogSubmitButton(ProviderSection section) {
		clickDialogSubmitButton(section, false);
	}

	/**
	 * Click the "Update/Add" dialog submission button. If an error is anticipated,
	 * wait for the error message to appear.
	 *
	 * @param section the provider section
	 * @param expectError whether an error is expected
	 */
	private void clickDialogSubmitButton(ProviderSection section, boolean expectError) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();
		String dialogCss = getDialogCss(section);

		String buttonCss = dialogCss + " > div.formControls" + " > button#" + formName + "\\:" + submitButtonName;
		WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		waitSeconds(2);
		if (expectError) {
			selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
		}
	}

	/**
	 * fill the Condition Data Block
	 * @param conditionType the condition type to select
	 * @param conditionIdentifier the condition identifier to input
	 * @param restriction the restriction flag to indicate whether to check the restriction checkbox
	 * @param explanation the explanation to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 */
	public void fillConditionDataBlock(String conditionType, String conditionIdentifier, boolean restriction,
										String explanation, String effectiveFrom, String effectiveTo)
	{
		String formName = DIALOG_MAP.get(ProviderSection.CONDITIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.CONDITIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.CONDITIONS, "conditionType", conditionType);

		String inputIdCss=dialogCss+" >input#"+formName+"\\:Identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(conditionIdentifier))inputId.sendKeys(conditionIdentifier);

		if (restriction) {
			String restrictionCss = dialogCss + " > input#" + formName + "\\:restriction";
			WebElement restrictionCheckbox = selenium_.findElement(By.cssSelector(restrictionCss));
			restrictionCheckbox.click();
		}

		String explanationCss = dialogCss + " > textarea#" + formName + "\\:explanation";
		WebElement explanationInput = selenium_.findElement(By.cssSelector(explanationCss));
		explanationInput.clear();
		if(!StringUtils.isEmpty(explanation))explanationInput.sendKeys(explanation);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.CONDITIONS, effectiveFrom, effectiveTo);
	}

	/**
	 * fill the Provider Relationship Data Block
	 * @param identifierType the identifier type to select
	 * @param identifier the identifier to input
	 * @param relationshipType the relationship type to select
	 */
	public void fillProviderRelationshipDataBlock(IdentifierType identifierType, String identifier, String relationshipType)
	{
		String formName = DIALOG_MAP.get(ProviderSection.PROVIDER_RELATIONSHIPS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.PROVIDER_RELATIONSHIPS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		if (identifierType != null)
			setDropdownListByVisibleText(ProviderSection.PROVIDER_RELATIONSHIPS, "providerType", identifierType.name());

		if (identifier != null) {
			String inputIdCss = dialogCss + " >input#" + formName + "\\:rpi";
			WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
			inputId.clear();
			if (!StringUtils.isEmpty(identifier)) inputId.sendKeys(identifier);
		}

		if (relationshipType != null)
			setDropdownListByVisibleText(ProviderSection.PROVIDER_RELATIONSHIPS, "relationshipType", relationshipType);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.PROVIDER_RELATIONSHIPS,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date());
	}

	/**
	 * fill the Registry User Relationship Data Block
	 * @param regType the registry type to select
	 * @param regUserID the registry user ID to input
	 * @param userType the registry user type to select
	 */
	public void fillRegUserRelationshipDataBlock(String regType, String regUserID, UserType userType)
	{
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_USER_RELATIONSHIPS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.REGISTRY_USER_RELATIONSHIPS, "regUserRelationshipType", regType);

		String regIdCss = dialogCss + " > input#" + formName + "\\:registryUserId";
		WebElement regId = selenium_.findElement(By.cssSelector(regIdCss));
		regId.clear();
		if (!StringUtils.isEmpty(regUserID)) regId.sendKeys(regUserID);

		setDropdownListByVisibleText(ProviderSection.REGISTRY_USER_RELATIONSHIPS, "regUserType", userType.getRegUserType());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.REGISTRY_USER_RELATIONSHIPS,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date());
	}

	/**
	 * fill the Work Location Data Block
	 * @param locationID the location ID to input
	 * @param defaultFlag the default flag to indicate whether to check the default flag checkbox
	 * @param name the name to input
	 * @param providerType the provider type to select
	 * @param addressInfo the address info to input
	 */
	public void fillWorkLocationDataBlock(String locationID, boolean defaultFlag, String name, String providerType,
										  String addressInfo) {
		String formName = DIALOG_MAP.get(ProviderSection.WORK_LOCATIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.WORK_LOCATIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		String inputIdCss=dialogCss+" >input#"+formName+"\\:wlChid";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(locationID))inputId.sendKeys(locationID);

		if (defaultFlag) {
			String defaultFlagCss = dialogCss + " > div#" + formName + "\\:defaultFlag";
			WebElement defaultFlagCheckbox = selenium_.findElement(By.cssSelector(defaultFlagCss));
			defaultFlagCheckbox.click();
		}

		String wlNameCss = dialogCss+" >input#"+formName+"\\:name";
		WebElement wlName = selenium_.findElement(By.cssSelector(wlNameCss));
		wlName.clear();
		if(!StringUtils.isEmpty(name)) wlName.sendKeys(name);

		setDropdownListByVisibleText(ProviderSection.WORK_LOCATIONS, "providerType", providerType);

		String addressInfoCss = dialogCss + " > textarea#" + formName + "\\:additionalInfo";
		WebElement addressInfoInput = selenium_.findElement(By.cssSelector(addressInfoCss));
		addressInfoInput.clear();
		if(!StringUtils.isEmpty(addressInfo)) addressInfoInput.sendKeys(addressInfo);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.WORK_LOCATIONS,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date());
	}

	/**
	 * performing action of adding Condition Data Block, perform error message check if necessary
	 * @param conditionType the condition type to select
	 * @param conditionIdentifier the condition identifier to input
	 * @param restriction the restriction flag to indicate whether to check the restriction checkbox
	 * @param explanation the explanation to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addConditionDataBlock(String conditionType, String conditionIdentifier, boolean restriction,
										String explanation, String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.CONDITIONS);

		clickHeaderAddButton(ProviderSection.CONDITIONS);

		fillConditionDataBlock(conditionType, conditionIdentifier, restriction, explanation, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.CONDITIONS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.CONDITIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Provider Relationship Data Block, perform error message check if necessary
	 * @param identifierType the identifier type to select
	 * @param identifier the identifier to input
	 * @param relationshipType the relationship type to select
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addProviderRelationshipDataBlock(IdentifierType identifierType, String identifier, String relationshipType, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.PROVIDER_RELATIONSHIPS);

		clickHeaderAddButton(ProviderSection.PROVIDER_RELATIONSHIPS);

		fillProviderRelationshipDataBlock(identifierType, identifier, relationshipType);

		clickDialogSubmitButton(ProviderSection.PROVIDER_RELATIONSHIPS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.PROVIDER_RELATIONSHIPS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Registry User Relationship Data Block, perform error message check if necessary
	 * @param regType the registry type to select
	 * @param regUserId the registry user ID to input
	 * @param userType the registry user type to select
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addRegUserRelationshipDataBlock(String regType, String regUserId, UserType userType,
												  boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

		clickHeaderAddButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

		fillRegUserRelationshipDataBlock(regType, regUserId, userType);

		clickDialogSubmitButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Work Location Data Block, perform error message check if necessary
	 * @param locationID the location ID to input
	 * @param defaultFlag the default flag to indicate whether to check the default flag checkbox
	 * @param name the name to input
	 * @param providerType the provider type to select
	 * @param addressInfo the address info to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addWorkLocationDataBlock(String locationID, boolean defaultFlag, String name, String providerType,
										   String addressInfo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.WORK_LOCATIONS);

		clickHeaderAddButton(ProviderSection.WORK_LOCATIONS);

		fillWorkLocationDataBlock(locationID, defaultFlag, name, providerType, addressInfo);

		clickDialogSubmitButton(ProviderSection.WORK_LOCATIONS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.WORK_LOCATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Disciplinary ActionData Block, perform error message check if necessary
	 * 
	 * @param actionIdentifier id of Disciplinary Action
	 * @param display flag of 'display' of Disciplinary Action
	 * @param description  description of Disciplinary Action
	 * @param archiveDate archive Date of Disciplinary Action
	 * @param effectiveFrom effective From date of Disciplinary Action
	 * @param effectiveTo effective To Date of Disciplinary Action
	 * @param expectError if this action expect returning error messages
	 * 
	 * @return expected error message or empty string if no error message expected 
	 */
	public String addDisciplinaryActionDataBlock(String actionIdentifier, boolean display,
			String description, String archiveDate, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		clickHeaderAddButton(ProviderSection.DISCIPLINARY_ACTIONS);

		fillDisciplinaryActionDataBlock(actionIdentifier, display, description, archiveDate, effectiveFrom,
				effectiveTo);

		clickDialogSubmitButton(ProviderSection.DISCIPLINARY_ACTIONS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.DISCIPLINARY_ACTIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * fill the Disciplinary Action Data Block
	 * 
	 * @param actionIdentifier id of Disciplinary Action 
	 * @param display flag of 'display' of Disciplinary Action 
	 * @param description description of Disciplinary Action
	 * @param archiveDate archive Date of Disciplinary Action
	 * @param effectiveFrom effective From date of Disciplinary Action
	 * @param effectiveTo effective To Date of Disciplinary Action
	 */
	private void fillDisciplinaryActionDataBlock(String actionIdentifier, boolean display, String description,
			String archiveDate, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.DISCIPLINARY_ACTIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		
		String inputIdCss=dialogCss+" >input#"+formName+"\\:Identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(actionIdentifier))inputId.sendKeys(actionIdentifier);

		if (display) {
			String displayCss = dialogCss + " > div#" + formName + "\\:display";
			WebElement displaynCheckbox = selenium_.findElement(By.cssSelector(displayCss));
			displaynCheckbox.click();
		}

		String descriptionCss = dialogCss + " > textarea#" + formName + "\\:description";
		WebElement descriptionInput = selenium_.findElement(By.cssSelector(descriptionCss));
		descriptionInput.clear();
		if(!StringUtils.isEmpty(description))descriptionInput.sendKeys(description);
		
		String archiveDatestr="archiveDate";
		String archiveDateCss = dialogCss + " >span#" + formName + "\\:" + archiveDatestr + " >input#" + formName
				+ "\\:" + archiveDatestr + "_input";
		WebElement archiveDateElement = selenium_.findElement(By.cssSelector(archiveDateCss));
		archiveDateElement.clear();
		if (!StringUtils.isEmpty(effectiveFrom))
			archiveDateElement.sendKeys(effectiveFrom);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.DISCIPLINARY_ACTIONS, effectiveFrom, effectiveTo);
		
	}

	public List<String> getMandatoryFields(ProviderSection section)
	{
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String mandatoryFieldCss = dialogCss + " > div > label > span.ui-outputlabel-rfi";
		List<WebElement> mandatoryFieldSpecifiers = selenium_.findElements(By.cssSelector(mandatoryFieldCss));
		return mandatoryFieldSpecifiers.stream()
				.map(elem -> elem.findElement(By.xpath("./.."))
						.getText().replace("*", ""))
				.toList();
	}

	private void setDialogEffectiveFromAndEffectiveTo(ProviderSection section, String effectiveFrom,
													  String effectiveTo) {
		String dialogCss = getDialogCss(section);
		String formName = DIALOG_MAP.get(section).getFormName();
		String effectiveFromStr = DIALOG_MAP.get(section).getEffectiveFromStr();
		String effectiveToStr = DIALOG_MAP.get(section).getEffectiveToStr();

		String effectFromCss = dialogCss + " >span#" + formName + "\\:" + effectiveFromStr + " >input#" + formName
				+ "\\:" + effectiveFromStr + "_input";
		WebElement effectFromElement = selenium_.findElement(By.cssSelector(effectFromCss));
		effectFromElement.clear();
		if (!StringUtils.isEmpty(effectiveFrom))
			effectFromElement.sendKeys(effectiveFrom);

		String effectToCss = dialogCss + " >span#" + formName + "\\:" + effectiveToStr + " >input#" + formName + "\\:"
				+ effectiveToStr + "_input";
		WebElement effectToElement = selenium_.findElement(By.cssSelector(effectToCss));
		effectToElement.clear();
		if (!StringUtils.isEmpty(effectiveTo))
			effectToElement.sendKeys(effectiveTo);
	}

	/**
	 * wait Error Message showing up, and return a copy of message as result
	 * note the result is a set of messages, if there are more than one error messages
	 *
	 * @param section the provider section
	 * @return String of error messages
	 */
	public String waitErrorMessage(ProviderSection section) {
		final int MAX_ATTEMPTS = 5;
		int attempts = 0;
		String msgDisplay = getDialogMessages(section);
		while (StringUtils.isEmpty(msgDisplay)) {
			if (attempts >= MAX_ATTEMPTS) break;
			waitSeconds(5);
			try {
				msgDisplay = getDialogMessages(section);
			} catch (StaleElementReferenceException e) {
				waitSeconds(5);
			}
			attempts++;
		}
		if (msgDisplay.contains("successfully") || msgDisplay.isEmpty()) return msgDisplay;
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;
	}

	/**
	 * Get Dialog Messages
	 *
	 * @param section the provider section
	 * @return String of dialog messages
	 */
	protected String getDialogMessages(ProviderSection section) {
		StringBuilder msgDisplay = new StringBuilder();
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String msgCss = dialogCss + "> div#" + formName + "\\:messages > div > ul > li";
		
		try {
			java.util.List<WebElement> msgList = selenium_.findElements(By.cssSelector(msgCss));
			for (WebElement msg : msgList) {
				msgDisplay.append(msg.getAttribute("innerText")).append("\n");
			}
		} catch (Exception e) {
			// No messages found
		}
		
		return msgDisplay.toString().trim();
	}

	/**
	 * Cease Data Block
	 *
	 * @param section the provider section
	 * @param index the data block index
	 */
	public void ceaseDataBlock(ProviderSection section, int index) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();

		clickDataBlockUpdateButton(section, index);
		waitSeconds(2);

		String dialogCss = getDialogCss(section);

		setEndReasonByVisibleText(section, EndReason.CEASE.getText());

		String buttonCss = dialogCss + " > div.formControls" + " > button#" + formName + "\\:" + submitButtonName;
		WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Cease All Data Block Under Section
	 *
	 * @param section the provider section
	 */
	public void ceaseAllDataBlockUnderSection(ProviderSection section) {
		int count = grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			ceaseDataBlock(section, 0);
		}
	}

	public void clickDialogCancelButton(ProviderSection providerSection)
	{
		String dialogCss = getDialogCss(providerSection);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	public void cancleAddDisciplinaryActionDataBlock(String actionIdentifier, boolean display, String description, String archiveDate,
			String effectiveFrom, String effectiveTo) {

		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		clickHeaderAddButton(ProviderSection.DISCIPLINARY_ACTIONS);

		fillDisciplinaryActionDataBlock(actionIdentifier, display, description, archiveDate, effectiveFrom,
				effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));

		
	}
}
