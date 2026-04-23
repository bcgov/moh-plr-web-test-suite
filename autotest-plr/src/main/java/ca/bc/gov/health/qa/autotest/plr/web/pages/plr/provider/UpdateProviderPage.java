package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationPurpose;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderPage extends ViewProviderPage {

	private static final Logger LOG = ExecutionLogManager.getLogger();

	/** Pattern to extract codes from displayed format "Description (CODE)" */
	private static final Pattern CODE_IN_PARENS_PATTERN = Pattern.compile("\\(([^)]+)\\)$");

	public static final Map<ProviderSection, ProviderDialog> DIALOG_MAP = Map.ofEntries(
			Map.entry(ProviderSection.REGISTRY_IDENTIFIERS, new ProviderDialog("maintainRegIdDialog", "maintainRegIdForm", "effectiveStartDate",
					"effectiveEndDate", "registryIdSubmitButton", "EndReasonType","Add")),
			Map.entry(ProviderSection.IDENTIFIERS, new ProviderDialog("maintainIdDialog", "maintainIdentifierForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add")),
			Map.entry(ProviderSection.ORGANIZATION_NAMES, new ProviderDialog("maintainOrgNameDialog", "maintainOrgNameForm", "effectiveStartDate",
							"effectiveEndDate", "orgNameSubmitButton", "EndReasonType","Add a new Organizational Name")),
			Map.entry(ProviderSection.PRACTITIONER_NAMES, new ProviderDialog("maintainPersonNameDialog", "maintainPersonNameForm", "effectiveStartDate",
					"effectiveEndDate", "personNameSubmitButton", "EndReasonType","Add a new Practitioner Name")),
			Map.entry(ProviderSection.NOTES, new ProviderDialog("maintainNoteDialog", "maintainNoteForm", "effectiveFromDate",
					"effectiveToDate", "idNoteSubmitButton", "endReasonCode","Add a new Note")),
			Map.entry(ProviderSection.ORGANIZATION_RELATIONSHIPS, new ProviderDialog("maintainOrganizationRelationshipDialog", "maintainOrgRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "orgRelationshipSubmitButton", "EndReasonType","Add a new Organization Relationship")),
			Map.entry(ProviderSection.TELECOMMUNICATIONS, new ProviderDialog("maintainTelecomDialog", "maintainTelecomForm", "effectiveFromDate",
					"effectiveToDate", "idTeleSubmitButton", "EndReasonType","Add a new Telecommunication")),
			Map.entry(ProviderSection.ELECTRONIC_ADDRESSES, new ProviderDialog("maintainElectronicAddressDialog", "maintainElectronicAddressForm", "effectiveStartDate",
					"effectiveEndDate", "electronicAddressSubmitButton", "EndReasonType","Add a new Electronic Address")),
			Map.entry(ProviderSection.CONDITIONS, new ProviderDialog("maintainConditionDialog", "maintainConditionForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add a new Condition")),
			Map.entry(ProviderSection.DISCIPLINARY_ACTIONS, new ProviderDialog("maintainDisActionDialog", "maintainDisActionForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add a new Disciplinary Action")),
            Map.entry(ProviderSection.STATUSES, new ProviderDialog("maintainStatusDialog", "maintainStatusForm", "effectiveFromDate",
                            "effectiveToDate", "idStatusSubmitButton", "endReasonCode", "Add a new Status")),
			Map.entry(ProviderSection.WORK_LOCATIONS, new ProviderDialog("maintainWorkLocationDialog", "maintainWorkLocationForm", "effectiveFromDate",
					"effectiveToDate", "idWLSubmitButton", "EndReasonType","Add a new Work Location")),
			Map.entry(ProviderSection.PROVIDER_RELATIONSHIPS, new ProviderDialog("maintainProviderRelationshipDialog", "maintainProviderRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "providerRelationshipSubmitButton", "EndReasonType","Add a new Provider Relationship")),
			Map.entry(ProviderSection.REGISTRY_USER_RELATIONSHIPS, new ProviderDialog("maintainRegUserRelationshipDialog", "maintainRegUserRelationshipForm", "effectiveFromDate",
					"effectiveToDate", "idRegUserRelationshipSubmitButton", "endReasonCode","Add a new Registry User Relationship")),
			Map.entry(ProviderSection.CREDENTIALS, new ProviderDialog("maintainCredentialDialog", "maintainCredentialForm", "effectiveStartDateCred",
					"effectiveEndDate", "idCredentialSubmitButton", "endReasonCode","Add a new Credential")),
			Map.entry(ProviderSection.EXPERTISE, new ProviderDialog("maintainExpertiseDialog", "maintainExpertiseForm", "effectiveStartDateExpertise",
					"effectiveEndDate", "idExpertiseSubmitButton", "endReasonCode","Add a new Expertise")),
			Map.entry(ProviderSection.ADDRESSES, new ProviderDialog("maintainAddressDialog", "maintainAddressForm", "effectiveFromDate",
					"effectiveToDate", "idAddressSubmitButton", "EndReasonType","Add a new Address"))

			);

	public UpdateProviderPage(SeleniumSession selenium, URI uri) {
		super(selenium, uri);
	}

	private static final String WIDGET_TITLE_SPAN_CSS = "div.ui-dialog-titlebar > span.ui-dialog-title";

	/**
	 * Finds a visible dialog widget by its title prefix
	 *
	 * @param widgetTitlePrefix a string of characters to find in the widget title
	 * @return a WebElement of a widget matching the prefix, or null if not found
	 */
	private WebElement findVisibleWidget(String widgetTitlePrefix) {
		for (WebElement elem : selenium_.findElements(By.cssSelector("div[role='dialog']"))) {
			String title;
			try {
				title = elem.findElement(By.cssSelector(WIDGET_TITLE_SPAN_CSS)).getAttribute("innerHTML");
			} catch (org.openqa.selenium.NoSuchElementException ignore) {
				continue;
			}
			if (title == null || title.isEmpty()) continue;
			if (!title.contains(widgetTitlePrefix)) continue;

			try {
				// regrab element (likely to have become stale) and check visibility
				selenium_.setWaitTimeout(Duration.ofSeconds(10));
				elem = selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.xpath(
						"//div[@role='dialog' and @aria-hidden='false' and .//span[contains(text(),'\" + title + \"')]]")));
			} catch (Exception e) {
				// TODO: handle exception
			}

			// Skip hidden/inactive dialogs
			String ariaHidden = elem.getAttribute("aria-hidden");
			if ("true".equals(ariaHidden)) continue;
			return elem;
		}
		return null;
	}

	/**
	 * Handles address validation dialogs that may appear after submitting an address.
	 * Clicks "Continue w/ Original" button if a validation dialog is displayed.
	 */
	public void handleAddressValidationDialog() {
		waitSeconds(3); // Give UI time to render any validation dialogs

		// Try different dialog title prefixes that may appear
		String[] dialogTitles = {"Address Invalid", "Address Recommended", "Validation", "Address Provided"};
		WebElement validationWidget = null;

		for (String title : dialogTitles) {
			validationWidget = findVisibleWidget(title);
			if (validationWidget != null) break;
		}

		if (validationWidget != null) {
			// Look for "Continue w/ Original" button first, then fall back to any button
			try {
				WebElement continueBtn = validationWidget.findElement(
						By.xpath(".//button[contains(.,\"Continue w/ Original\")]"));
				if (continueBtn.isDisplayed() && continueBtn.isEnabled()) {
					continueBtn.click();

					selenium_.waitUntil(ExpectedConditions.invisibilityOf(validationWidget));
					waitSeconds(2);
					return;
				}
			} catch (org.openqa.selenium.NoSuchElementException ignore) {
				// Fall back to any visible button
			}

			try {
				// Generic fallback: first displayed & enabled button
				for (WebElement btn : validationWidget.findElements(By.tagName("button"))) {
					if (btn.isDisplayed() && btn.isEnabled()) {
						btn.click();
						waitSeconds(2);
						return;
					}
				}
			} catch (org.openqa.selenium.NoSuchElementException ignore) {
				//Generic catch block
			}
		}
		LOG.info("no validation widget");
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
	 * Tries to wait some number of seconds. Will fail the test used in if
	 * interrupted. TODO this should be used as little as possible in favour of
	 * selenium implicit waits.
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
	 * Attempts to update a practitioner name data block with provided values
	 *
	 * @param prefix        the name prefix
	 * @param first         the first name
	 * @param second        the second name
	 * @param third         the third name
	 * @param surname       the surname
	 * @param suffix        the name suffix
	 * @param endReasonType the end reason type
	 * @param index         the data block index
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updatePractitionerNameDataBlock(String prefix, String first, String second, String third,
			String surname, String suffix, EndReason endReasonType, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.PRACTITIONER_NAMES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.PRACTITIONER_NAMES);

		clickDataBlockUpdateButton(ProviderSection.PRACTITIONER_NAMES, index);

		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		findAndFillInputField(dialogCss, formName, prefix, "prefix");
		findAndFillInputField(dialogCss, formName, first, "firstName");
		findAndFillInputField(dialogCss, formName, second, "secondName");
		findAndFillInputField(dialogCss, formName, third, "thirdName");
		findAndFillInputField(dialogCss, formName, surname, "surname");
		findAndFillInputField(dialogCss, formName, suffix, "suffix");

		if (endReasonType != null) {
			setEndReasonByVisibleText(ProviderSection.PRACTITIONER_NAMES, endReasonType.getText());
		}

		clickDialogSubmitButton(ProviderSection.PRACTITIONER_NAMES, expectError);

		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.PRACTITIONER_NAMES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}

		return msgDisplay;
	}

	/**
	 * get Data Block Header Update Button Selector
	 *
	 * @param section the provider section
	 * @param index   the data block index
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
	 *
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
	 * Clicks the add button in the header of a work location
	 * @param section the provider section within the work location
	 *                will only accept Addresses, Electronic Addresses, Telecommunications, and Communication Preferences
	 * @param index   the index of work location to add the data block for
	 * @return the WebElement of the dialog content after clicking the add button and waiting for the dialog to be visible
	 */
	public WebElement clickWLHeaderAddButton(ProviderSection section, int index)
	{
		// expand the work location if hasn't happened yet
		expandDataBlock(ProviderSection.WORK_LOCATIONS, index, true);

		String clickElementCss = getSectionSelector(ProviderSection.WORK_LOCATIONS) + " > div > table > tbody > tr > td > ";
		clickElementCss += String.format("div#wlRepeat\\:%d\\:workLocationPanel > div > div > div > div#wlRepeat\\:%d\\:", index, index);
		switch (section) {
			case ProviderSection.ADDRESSES -> clickElementCss += "workLocationAddressesPanel";
			case ProviderSection.TELECOMMUNICATIONS -> clickElementCss += "workLocationTelecommunicationsPanel";
			case ProviderSection.ELECTRONIC_ADDRESSES -> clickElementCss += "workLocationElectronicAddressesPanel";
			case ProviderSection.COMMUNICATION_PREFERENCE -> clickElementCss += "workLocationInformationRoutesPanel";
			default -> throw new IllegalArgumentException("Section " + section + " is not supported for adding within Work Locations");
		}
		clickElementCss += " > div > div > a > img[title='" + DIALOG_MAP.get(section).getAddButtonImgText() + "']";

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
	 * @return the WebElement of the dialog content after clicking the update button and waiting for the dialog to be visible
     */
	public WebElement clickDataBlockUpdateButton(ProviderSection section, int index) {
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
		return selenium_.findElementByCss(dialogCss);
	}

	/**
	 * Clicks the update button on a specified data block within a work location section
	 * (Addresses, Electronic Addresses, Telecommunications, Communication Preferences)
	 * @param section the provider section within the work location to find the update button
	 *                   will only accept Addresses, Electronic Addresses, Telecommunications, and Communication Preferences
	 * @param wlIndex the index of the work location to find the data block's update button within
	 * @param entityIndex the index of the data block within the work location to find and click the update button for
	 * @return the WebElement of the dialog content after clicking the update button and waiting for the dialog to be visible
	 */
	public WebElement clickWLHeaderUpdateButton(ProviderSection section, int wlIndex, int entityIndex) {
		// expand the work location if hasn't happened yet
		expandDataBlock(ProviderSection.WORK_LOCATIONS, wlIndex, true);

		final String panelType = switch (section) {
			case ProviderSection.ADDRESSES -> "wlAddressPanel";
			case ProviderSection.TELECOMMUNICATIONS -> "telecommunicationPanel";
			case ProviderSection.ELECTRONIC_ADDRESSES -> "eAddressPanel";
			case ProviderSection.COMMUNICATION_PREFERENCE -> "informationRoutePanel";
			default -> throw new IllegalArgumentException("Section " + section + " is not supported for updating within Work Locations");
		};

		final String wlRepeatId = String.format("wlRepeat\\:%d\\:", wlIndex);
		final String wlPanelId = String.format("\\:%d\\:%s", entityIndex, panelType);

		String clickElementCss = getSectionSelector(ProviderSection.WORK_LOCATIONS) + " > div > table > tbody > tr > td > div#";
		clickElementCss += wlRepeatId + "workLocationPanel > div > div > div > div#" + wlRepeatId;
		switch (section) {
			case ProviderSection.ADDRESSES -> clickElementCss += "workLocationAddressesPanel";
			case ProviderSection.TELECOMMUNICATIONS -> clickElementCss += "workLocationTelecommunicationsPanel";
			case ProviderSection.ELECTRONIC_ADDRESSES -> clickElementCss += "workLocationElectronicAddressesPanel";
			case ProviderSection.COMMUNICATION_PREFERENCE -> clickElementCss += "workLocationInformationRoutesPanel";
			default -> throw new IllegalArgumentException("Section " + section + " is not supported for updating within Work Locations");
		}
		clickElementCss += " > div > table > tbody > tr > td > div[id^='" + wlRepeatId + "'][id*='" + wlPanelId + "'] > div > div > a > img[title^='Update']";

		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(clickElementCss)));
		WebElement updateButton = selenium_.findElementByCss(clickElementCss);
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
		return selenium_.findElementByCss(dialogCss);
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
		String checkBoxCss = dialogCss + " > div#" + formName + "\\:" + checkBoxName + " > div > span";
		WebElement checkBox = selenium_.findElement(By.cssSelector(checkBoxCss));
		return getChkBoxState(section, checkBox);
	}

	/**
	 * set End Reason By Visible Text
	 *
	 * @param section     the provider section
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
	 * @param section      the provider section
	 * @param dropdownName the dropdown field name
	 * @param visibleText  the visible text to select
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
	 *
	 * @param section      the provider section
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
	 * Sets the address country dropdown value and waits for UI to update.
	 * This is useful when testing province dropdown changes based on country selection.
	 *
	 * @param country the country value to select (e.g., "CA - CANADA", "US - UNITED STATES")
	 */
	public void setAddressCountry(String country) {
		setDropdownListByVisibleText(ProviderSection.ADDRESSES, "country", country);
		waitSeconds(2); // Wait for province field to update after country change
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
	 * @param section     the provider section
	 * @param expectError whether an error is expected
	 */
	private void clickDialogSubmitButton(ProviderSection section, boolean expectError) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();
		String dialogCss = getDialogCss(section);

		// Use a selector that matches any button whose id starts with the expected button name
		String buttonCss = dialogCss + " div.formControls button[id^='" + formName + ":" + submitButtonName + "']";
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
			String explanation, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.CONDITIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.CONDITIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.CONDITIONS, "conditionType", conditionType);

		String inputIdCss = dialogCss + " >input#" + formName + "\\:Identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(conditionIdentifier))
			inputId.sendKeys(conditionIdentifier);

		if (restriction) {
			String restrictionCss = dialogCss + " > input#" + formName + "\\:restriction";
			WebElement restrictionCheckbox = selenium_.findElement(By.cssSelector(restrictionCss));
			restrictionCheckbox.click();
		}

		String explanationCss = dialogCss + " > textarea#" + formName + "\\:explanation";
		WebElement explanationInput = selenium_.findElement(By.cssSelector(explanationCss));
		explanationInput.clear();
		if (!StringUtils.isEmpty(explanation))
			explanationInput.sendKeys(explanation);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.CONDITIONS, effectiveFrom, effectiveTo);
	}

	/**
	 * fill the Credential Data Block
	 * @param credentialType the credential type to select
	 * @param designation the designation to input
	 * @param registrationNo the registration number to input
	 * @param institution the institution to input
	 * @param city the city to input
	 * @param country the country to select
	 * @param province the province to select
	 * @param equivalency the equivalency flag to indicate whether to check the equivalency checkbox
	 * @param year the year to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 */
	public void fillCredentialDataBlock(String credentialType, String designation, String registrationNo,
										String institution, String city, String country, String province,
										boolean equivalency, String year, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.CREDENTIALS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.CREDENTIALS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "credentialType", credentialType);

		findAndFillInputField(dialogCss, formName, designation, "designation");
		findAndFillInputField(dialogCss, formName, registrationNo, "regNo");

		if (!StringUtils.isEmpty(institution))
			fillAutocompleteField(institution, formName, "institution_input", "institution_panel");

		if (!StringUtils.isEmpty(city))
			fillAutocompleteField(city, formName, "cityCred_input", "cityCred_panel");

		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "countryCred", country);
		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "provinceCred", province);

		if (equivalency) {
			String equivalencyFlagCss = dialogCss + " > div#" + formName + "\\:equivalencyFlag";
			WebElement equivalencyFlagCheckbox = selenium_.findElement(By.cssSelector(equivalencyFlagCss));
			equivalencyFlagCheckbox.click();
		}

		if (!StringUtils.isEmpty(year))
			fillAutocompleteFieldRaw(year, formName, "yearIssued_input");

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.CREDENTIALS, effectiveFrom, effectiveTo);
	}

	/**
	 * fill the Credential Data Block with raw input without using autocomplete field,
	 * this method is used for negative test when the input value is not in the autocomplete list
	 * @param credentialType the credential type to select
	 * @param designation the designation to input
	 * @param registrationNo the registration number to input
	 * @param institution the institution to input
	 * @param city the city to input
	 * @param country the country to select
	 * @param province the province to select
	 * @param equivalency the equivalency flag to indicate whether to check the equivalency checkbox
	 * @param year the year to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 */
	public void fillCredentialDataBlockRaw(String credentialType, String designation, String registrationNo,
			String institution, String city, String country, String province,
			boolean equivalency, String year, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.CREDENTIALS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.CREDENTIALS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "credentialType", credentialType);

		findAndFillInputField(dialogCss, formName, designation, "designation");
		findAndFillInputField(dialogCss, formName, registrationNo, "regNo");

		if (!StringUtils.isEmpty(institution))
			fillAutocompleteFieldRaw(institution, formName, "institution_input");

		if (!StringUtils.isEmpty(city))
			fillAutocompleteFieldRaw(city, formName, "cityCred_input");

		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "countryCred", country);
		setDropdownListByVisibleText(ProviderSection.CREDENTIALS, "provinceCred", province);

		if (equivalency) {
			String equivalencyFlagCss = dialogCss + " > div#" + formName + "\\:equivalencyFlag";
			WebElement equivalencyFlagCheckbox = selenium_.findElement(By.cssSelector(equivalencyFlagCss));
			equivalencyFlagCheckbox.click();
		}

		if (!StringUtils.isEmpty(year))
			fillAutocompleteFieldRaw(year, formName, "yearIssued_input");

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.CREDENTIALS, effectiveFrom, effectiveTo);
	}

	/**
	 * fill the Expertise Data Block
	 * @param expertise the expertise to select
	 * @param sourceCode the source code to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 */
	public void fillExpertiseDataBlock(String expertise, String sourceCode, String effectiveFrom, String effectiveTo)
	{
		String formName = DIALOG_MAP.get(ProviderSection.EXPERTISE).getFormName();
		String dialogCss = getDialogCss(ProviderSection.EXPERTISE);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.EXPERTISE, "expertise", expertise);

		findAndFillInputField(dialogCss, formName, sourceCode, "sourceCode");

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.EXPERTISE, effectiveFrom, effectiveTo);
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

		if (identifier != null)
			findAndFillInputField(dialogCss, formName, identifier, "rpi");

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

		if (regType != null)
			setDropdownListByVisibleText(ProviderSection.REGISTRY_USER_RELATIONSHIPS,
					"regUserRelationshipType", regType);

		findAndFillInputField(dialogCss, formName, regUserID, "registryUserId");

		if (userType != null)
			setDropdownListByVisibleText(ProviderSection.REGISTRY_USER_RELATIONSHIPS,
					"regUserType", userType.getRegUserType());

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

		findAndFillInputField(dialogCss, formName, locationID, "wlChid");

		if (defaultFlag) {
			String defaultFlagCss = dialogCss + " > div#" + formName + "\\:defaultFlag";
			WebElement defaultFlagCheckbox = selenium_.findElement(By.cssSelector(defaultFlagCss));
			defaultFlagCheckbox.click();
		}

		findAndFillInputField(dialogCss, formName, name, "name");

		setDropdownListByVisibleText(ProviderSection.WORK_LOCATIONS, "providerType", providerType);

		findAndFillTextAreaField(dialogCss, formName, addressInfo, "additionalInfo");

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.WORK_LOCATIONS,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date());
	}

	/**
	 * fill the Work Location Data Block in update scenario,
	 * the effective date fields cannot be manually selected, will fill in hardcoded values
	 * @param defaultFlag the default flag to indicate whether to check the default flag checkbox
	 * @param name the name to input
	 * @param providerType the provider type to select
	 * @param addressInfo the address info to input
	 * @param endReason the end reason to select
	 */
	public void fillWorkLocationDataBlockUpdate(boolean defaultFlag, String name, String providerType,
												String addressInfo, EndReason endReason) {
		String formName = DIALOG_MAP.get(ProviderSection.WORK_LOCATIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.WORK_LOCATIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		String defaultFlagCss = dialogCss + " > div#" + formName + "\\:defaultFlag > div > span";
		WebElement defaultFlagCheckbox = selenium_.findElement(By.cssSelector(defaultFlagCss));
		if (getChkBoxState(ProviderSection.WORK_LOCATIONS, defaultFlagCheckbox) != defaultFlag) defaultFlagCheckbox.click();

		String wlNameCss = dialogCss+" >input#"+formName+"\\:name";
		WebElement wlName = selenium_.findElement(By.cssSelector(wlNameCss));
		wlName.clear();
		if(!StringUtils.isEmpty(name)) wlName.sendKeys(name);

		if (providerType != null)
			setDropdownListByVisibleText(ProviderSection.WORK_LOCATIONS, "providerType", providerType);

		String addressInfoCss = dialogCss + " > textarea#" + formName + "\\:additionalInfo";
		WebElement addressInfoInput = selenium_.findElement(By.cssSelector(addressInfoCss));
		addressInfoInput.clear();
		if(!StringUtils.isEmpty(addressInfo)) addressInfoInput.sendKeys(addressInfo);

		setEndReasonByVisibleText(ProviderSection.WORK_LOCATIONS, endReason.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.WORK_LOCATIONS, "2000-01-01", "2999-01-01");
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
			String explanation, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.CONDITIONS);

		clickHeaderAddButton(ProviderSection.CONDITIONS);

		fillConditionDataBlock(conditionType, conditionIdentifier, restriction, explanation, effectiveFrom,
				effectiveTo);

		clickDialogSubmitButton(ProviderSection.CONDITIONS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.CONDITIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Credential Data Block, perform error message check if necessary
	 * @param credentialType the credential type to select
	 * @param designation the designation to input
	 * @param registrationNo the registration number to input
	 * @param institution the institution to input
	 * @param city the city to input
	 * @param country the country to select
	 * @param province the province to select
	 * @param equivalency the equivalency flag to indicate whether to check the equivalency checkbox
	 * @param year the year to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addCredentialDataBlock(String credentialType, String designation, String registrationNo,
										String institution, String city, String country, String province,
										boolean equivalency, String year, String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.CREDENTIALS);

		clickHeaderAddButton(ProviderSection.CREDENTIALS);

		fillCredentialDataBlock(credentialType, designation, registrationNo, institution, city, country,
				province, equivalency, year, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.CREDENTIALS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.CREDENTIALS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Credential Data Block with raw input without using autocomplete field, perform error message check if necessary
	 * @param credentialType the credential type to select
	 * @param designation the designation to input
	 * @param registrationNo the registration number to input
	 * @param institution the institution to input
	 * @param city the city to input
	 * @param country the country to select
	 * @param province the province to select
	 * @param equivalency the equivalency flag to indicate whether to check the equivalency checkbox
	 * @param year the year to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addCredentialDataBlockRaw(String credentialType, String designation, String registrationNo,
			String institution, String city, String country, String province,
			boolean equivalency, String year, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.CREDENTIALS);

		clickHeaderAddButton(ProviderSection.CREDENTIALS);

		fillCredentialDataBlockRaw(credentialType, designation, registrationNo, institution, city, country,
				province, equivalency, year, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.CREDENTIALS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.CREDENTIALS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Expertise Data Block, perform error message check if necessary
	 * @param expertise the expertise to select
	 * @param sourceCode the source code to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addExpertiseDataBlock(String expertise, String sourceCode, String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.EXPERTISE);

		clickHeaderAddButton(ProviderSection.EXPERTISE);

		fillExpertiseDataBlock(expertise, sourceCode, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.EXPERTISE, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.EXPERTISE);

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
	 * performing action of adding Work Location Address Data Block, perform error message check if necessary
	 * @param wlIndex the index of work location to add the address data block for
	 * @param addressType the address type to select
	 * @param purpose the purpose to select
	 * @param addressLine1 the address line 1 to input
	 * @param addressLine2 the address line 2 to input
	 * @param addressLine3 the address line 3 to input
	 * @param city the city to input
	 * @param province the province to input
	 * @param postalCode the postal code to input
	 * @param country the country to select
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addWLAddressDataBlock(int wlIndex, String addressType, String purpose,
										String addressLine1, String addressLine2, String addressLine3,
										String city, String province, String postalCode, String country,
										String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickWLHeaderAddButton(ProviderSection.ADDRESSES, wlIndex);

		fillAddressDataBlock(addressType, purpose, addressLine1, addressLine2, addressLine3, city, province, country,
								postalCode, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);

		// Handle address validation popups that may appear (only when not expecting error)
		handleAddressValidationDialog();

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * performing action of adding Work Location Telecom Data Block, perform error message check if necessary
	 * @param wlIndex the index of work location to add the telecom data block for
	 * @param telecomType the telecom type to select
	 * @param purpose the purpose to select
	 * @param areaCode the area code to input
	 * @param number the number to input
	 * @param extension the extension to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addWLTelecomDataBlock(int wlIndex, String telecomType, String purpose, String areaCode, String number, String extension,
										String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickWLHeaderAddButton(ProviderSection.TELECOMMUNICATIONS, wlIndex);

		fillTelecommunicationDataBlock(telecomType, purpose, areaCode, number, extension, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.TELECOMMUNICATIONS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.TELECOMMUNICATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Work Location Electronic Address Data Block, perform error message check if necessary
	 * @param wlIndex the index of work location to add the electronic address data block for
	 * @param eAddressType the electronic address type to select
	 * @param purpose the purpose to select
	 * @param address the electronic address to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String addWLElecAddressDataBlock(int wlIndex, String eAddressType, String purpose, String address,
										String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		clickWLHeaderAddButton(ProviderSection.ELECTRONIC_ADDRESSES, wlIndex);

		fillElectronicAddressDataBlock(eAddressType, purpose, address, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ELECTRONIC_ADDRESSES, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.ELECTRONIC_ADDRESSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of updating Work Location Address Data Block, perform error message check if necessary
	 * @param wlIndex the index of work location to update the address data block for
	 * @param entityIndex the index of the address data block within the work location to update
	 * @param addressLine1 the address line 1 to input
	 * @param addressLine2 the address line 2 to input
	 * @param addressLine3 the address line 3 to input
	 * @param city the city to input
	 * @param province the province to input
	 * @param postalCode the postal code to input
	 * @param country the country to select
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param endReason the end reason to select
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String updateWLAddressDataBlock(int wlIndex, int entityIndex,
									   String addressLine1, String addressLine2, String addressLine3,
									   String city, String province, String postalCode, String country,
									   String effectiveFrom, String effectiveTo, EndReason endReason,
									   boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickWLHeaderUpdateButton(ProviderSection.ADDRESSES, wlIndex, entityIndex);

		fillAddressDataBlock(null, null, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		if (endReason != null) {
			setEndReasonByVisibleText(ProviderSection.ADDRESSES, endReason.getText());
		}

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);

		// Handle address validation popups that may appear (only when not expecting error)
		handleAddressValidationDialog();

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * performing action of updating Work Location Telecom Data Block, perform error message check if necessary
	 * @param wlIndex the index of work location to update the telecom data block for
	 * @param entityIndex the index of the telecom data block within the work location to update
	 * @param areaCode the area code to input
	 * @param number the number to input
	 * @param extension the extension to input
	 * @param effectiveFrom the effective from date to input
	 * @param effectiveTo the effective to date to input
	 * @param endReason the end reason to select
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String updateWLTelecomDataBlock(int wlIndex, int entityIndex, String areaCode, String number, String extension,
										String effectiveFrom, String effectiveTo, EndReason endReason, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickWLHeaderUpdateButton(ProviderSection.TELECOMMUNICATIONS, wlIndex, entityIndex);

		fillTelecommunicationDataBlock(null, null, areaCode, number, extension, effectiveFrom, effectiveTo);

		if (endReason != null) {
			setEndReasonByVisibleText(ProviderSection.TELECOMMUNICATIONS, endReason.getText());
		}

		clickDialogSubmitButton(ProviderSection.TELECOMMUNICATIONS, expectError);

		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.TELECOMMUNICATIONS);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * performing action of updating Work Location Data Block, perform error message check if necessary
	 * @param defaultFlag the default flag to indicate whether to check the default flag checkbox
	 * @param name the name to input
	 * @param providerType the provider type to select
	 * @param addressInfo the address info to input
	 * @param endReason the end reason to select
	 * @param expectError if this action expect returning error messages
	 * @return expected error message or empty string if no error message expected
	 */
	public String updateWorkLocationDataBlock(boolean defaultFlag, String name, String providerType, String addressInfo,
											  EndReason endReason, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.WORK_LOCATIONS);

		clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);

		fillWorkLocationDataBlockUpdate(defaultFlag, name, providerType, addressInfo, endReason);

		clickDialogSubmitButton(ProviderSection.WORK_LOCATIONS, expectError);

		if (expectError) msgDisplay = waitErrorMessage(ProviderSection.WORK_LOCATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * performing action of adding Disciplinary ActionData Block, perform error
	 * message check if necessary
	 * 
	 * @param actionIdentifier id of Disciplinary Action
	 * @param display          flag of 'display' of Disciplinary Action
	 * @param description      description of Disciplinary Action
	 * @param archiveDate      archive Date of Disciplinary Action
	 * @param effectiveFrom    effective From date of Disciplinary Action
	 * @param effectiveTo      effective To Date of Disciplinary Action
	 * @param expectError      if this action expect returning error messages
	 * 
	 * @return expected error message or empty string if no error message expected
	 */
	public String addDisciplinaryActionDataBlock(String actionIdentifier, boolean display, String description,
			String archiveDate, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		clickHeaderAddButton(ProviderSection.DISCIPLINARY_ACTIONS);

		fillDisciplinaryActionDataBlock(actionIdentifier, display, description, archiveDate, effectiveFrom,
				effectiveTo);

		clickDialogSubmitButton(ProviderSection.DISCIPLINARY_ACTIONS, expectError);

		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.DISCIPLINARY_ACTIONS);
			// Cancel the dialog since it stays open after an error
			clickDialogCancelButton(ProviderSection.DISCIPLINARY_ACTIONS);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * fill the Disciplinary Action Data Block
	 * 
	 * @param actionIdentifier id of Disciplinary Action
	 * @param display          flag of 'display' of Disciplinary Action
	 * @param description      description of Disciplinary Action
	 * @param archiveDate      archive Date of Disciplinary Action
	 * @param effectiveFrom    effective From date of Disciplinary Action
	 * @param effectiveTo      effective To Date of Disciplinary Action
	 */
	private void fillDisciplinaryActionDataBlock(String actionIdentifier, boolean display, String description,
			String archiveDate, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.DISCIPLINARY_ACTIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		String inputIdCss = dialogCss + " >input#" + formName + "\\:Identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(actionIdentifier))
			inputId.sendKeys(actionIdentifier);

		if (display) {
			String displayCss = dialogCss + " > div#" + formName + "\\:display";
			WebElement displaynCheckbox = selenium_.findElement(By.cssSelector(displayCss));
			displaynCheckbox.click();
		}

		String descriptionCss = dialogCss + " > textarea#" + formName + "\\:description";
		WebElement descriptionInput = selenium_.findElement(By.cssSelector(descriptionCss));
		descriptionInput.clear();
		if (!StringUtils.isEmpty(description))
			descriptionInput.sendKeys(description);

		String archiveDatestr = "archiveDate";
		String archiveDateCss = dialogCss + " >span#" + formName + "\\:" + archiveDatestr + " >input#" + formName
				+ "\\:" + archiveDatestr + "_input";
		WebElement archiveDateElement = selenium_.findElement(By.cssSelector(archiveDateCss));
		archiveDateElement.clear();
		if (!StringUtils.isEmpty(effectiveFrom))
			archiveDateElement.sendKeys(effectiveFrom);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.DISCIPLINARY_ACTIONS, effectiveFrom, effectiveTo);

	}

	public List<String> getMandatoryFields(ProviderSection section) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String mandatoryFieldCss = dialogCss + " > div > label > span.ui-outputlabel-rfi";
		List<WebElement> mandatoryFieldSpecifiers = selenium_.findElements(By.cssSelector(mandatoryFieldCss));
		return mandatoryFieldSpecifiers.stream()
				.map(elem -> elem.findElement(By.xpath("./..")).getText().replace("*", "")).toList();
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
	 * wait Error Message showing up, and return a copy of message as result note
	 * the result is a set of messages, if there are more than one error messages
	 *
	 * @param section the provider section
	 * @return String of error messages
	 */
	public String waitErrorMessage(ProviderSection section) {
		final int MAX_ATTEMPTS = 10;
		int attempts = 0;
		String msgDisplay = getDialogMessages(section);
		while (StringUtils.isEmpty(msgDisplay)) {
			if (attempts >= MAX_ATTEMPTS) break;
			waitSeconds(3);
			try {
				msgDisplay = getDialogMessages(section);
			} catch (StaleElementReferenceException e) {
				waitSeconds(5);
			}
			attempts++;
		}
		if (msgDisplay.contains("successfully") || msgDisplay.isEmpty()) {
			LOG.info("Error did not occur / dialog disappeared earlier than expected.");
			return msgDisplay;
		}
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
	 * Find And Fill an input field for organization properties (TEXT_FIELD type)
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName  the form name
	 * @param field     the field value
	 * @param fieldCss  the field CSS selector
	 */
	private void findAndFillInputField(String dialogCss, String formName, String field, String fieldCss) {
		String inputNameCss = dialogCss + " >input#" + formName + "\\:" + fieldCss;
		// Wait for the input field to be visible

		WebElement inputName = selenium_
				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(inputNameCss)));

		inputName.clear();
		if (!StringUtils.isEmpty(field))
			inputName.sendKeys(field);
	}


	/**
	 * Find And Fill an input field for organization properties (TEXT_FIELD type)
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName  the form name
	 * @param field     the field value
	 * @param fieldCss  the field CSS selector
	 */
	private void findAndFillTextAreaField(String dialogCss, String formName, String field, String fieldCss) {
		String inputNameCss = dialogCss + " >textarea#" + formName + "\\:" + fieldCss;
		// Wait for the input field to be visible

		WebElement inputName = selenium_
				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(inputNameCss)));

		inputName.clear();
		if (!StringUtils.isEmpty(field))
			inputName.sendKeys(field);
	}

	
	private void fillAutocompleteField(String field, String formName, String inputCss, String panelCss) {
		if (StringUtils.isEmpty(field)) return;

		String inputCssSelector = "input#" + formName + "\\" + ":" + inputCss;
		String panelCssSelector = "span#" + formName + "\\" + ":" + panelCss;

		WebElement inputElement = selenium_.findElement(By.cssSelector(inputCssSelector));
		inputElement.clear();
		inputElement.sendKeys(field);

		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(panelCssSelector)));
		waitSeconds(1);
		List<WebElement> items = selenium_.findElements(By.cssSelector(panelCssSelector + " li.ui-autocomplete-item"));
		if (!items.isEmpty()) {
			items.getFirst().click();
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(panelCssSelector)));
		}
	}

	private void fillAutocompleteFieldRaw(String field, String formName, String inputCss) {
		if (StringUtils.isEmpty(field)) return;

		String inputCssSelector = "input#" + formName + "\\" + ":" + inputCss;

		WebElement inputElement = selenium_.findElement(By.cssSelector(inputCssSelector));
		inputElement.clear();
		inputElement.sendKeys(field);

		// Tab out to avoid autocomplete panel interference
		inputElement.sendKeys(Keys.TAB);
		waitSeconds(1);
	}

	/**
	 * Cease Data Block
	 *
	 * @param section the provider section
	 * @param index   the data block index
	 */
	public void ceaseDataBlock(ProviderSection section, int index) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();

		clickDataBlockUpdateButton(section, index);
		waitSeconds(2);

		String dialogCss = getDialogCss(section);

		setEndReasonByVisibleText(section, EndReason.CEASE.getText());

		// Use descendant selector (space) instead of direct child (>) to handle nested div structures
		String buttonCss = dialogCss + " div.formControls" + " button#" + formName + "\\:" + submitButtonName;
		WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Cease Last Data Block - ceases the last active data block in the section
	 *
	 * @param section the provider section
	 */
	public void ceaseLastDataBlock(ProviderSection section) {
		int count = grabActiveDataBlockCount(section, true);
		if (count > 0) {
			ceaseDataBlock(section, count - 1);
		}
	}

	/**
	 * Gets a list of purpose codes currently used for a specific address type.
	 * Uniqueness rule for addresses is based on Address Type + Purpose Code combination.
	 *
	 * @param addressTypeCode the address type code (e.g., "M" for Mailing, "P" for Physical)
	 * @return list of purpose codes currently in use for that address type (e.g., "BC", "MC", "HC")
	 */
	public List<String> getUsedPurposeCodesForAddressType(String addressTypeCode) {
		List<String> usedCodes = new ArrayList<>();
		int totalCount = grabDataBlockCount(ProviderSection.ADDRESSES);

		for (int i = 0; i < totalCount; i++) {
			// Skip inactive (ceased) blocks
			if (!grabDataBlockActive(ProviderSection.ADDRESSES, i)) {
				continue;
			}

			LinkedHashMap<String, String> content = grabDataBlockContent(ProviderSection.ADDRESSES, i);
			String addressType = content.get("Address Type");
			String purposeValue = content.get("Address Purpose");

			if (addressType != null && purposeValue != null) {
				// Extract the code from format "Description (CODE)" -> "CODE"
				String typeCode = extractCodeFromParens(addressType);
				// Only add if the address type matches
				if (typeCode != null && typeCode.equals(addressTypeCode)) {
					// Extract the purpose code from format "Description (CODE)" -> "CODE"
					String purposeCode = extractCodeFromParens(purposeValue);
					if (purposeCode != null) {
						usedCodes.add(purposeCode);
					}
				}
			}
		}
		return usedCodes;
	}

	/**
	 * Extracts the code from within parentheses at the end of a string.
	 * For example: "Ministry Contact (MC)" -> "MC"
	 *
	 * @param value the string containing a code in parentheses
	 * @return the extracted code, or null if not found
	 */
	private String extractCodeFromParens(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		Matcher matcher = CODE_IN_PARENS_PATTERN.matcher(value);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * Gets a list of purpose codes currently used in a section (for telecoms, electronic addresses)
	 *
	 * @param section the provider section
	 * @return list of purpose codes currently in use (e.g., "BC", "MC", "HC")
	 */
	public List<String> getUsedPurposeCodes(ProviderSection section) {
		List<String> usedCodes = new ArrayList<>();
		int totalCount = grabDataBlockCount(section);

		String purposeKey;
		switch (section) {
			case TELECOMMUNICATIONS:
				purposeKey = "Telecom Purpose";
				break;
			case ELECTRONIC_ADDRESSES:
				purposeKey = "Electronic Address Purpose";
				break;
			default:
				return usedCodes;
		}

		for (int i = 0; i < totalCount; i++) {
			// Skip inactive (ceased) blocks
			if (!grabDataBlockActive(section, i)) {
				continue;
			}

			LinkedHashMap<String, String> content = grabDataBlockContent(section, i);
			String purposeValue = content.get(purposeKey);
			if (purposeValue != null && !purposeValue.isEmpty()) {
				// Extract the code from format "Description (CODE)" -> "CODE"
				String code = extractCodeFromParens(purposeValue);
				if (code != null) {
					usedCodes.add(code);
				}
			}
		}
		return usedCodes;
	}

	/**
	 * Gets an available telecommunication purpose code that is not currently used for the given address type.
	 * Since uniqueness is based on Address Type + Purpose Code, a purpose code can be reused
	 * if it's only used by a different address type.
	 *
	 * @param addressTypeCode the address type code (e.g., "M" for Mailing, "P" for Physical)
	 * @return the text of an available TelecommunicationPurpose, or null if all are used
	 */
	public String getAvailablePurposeCodeForAddressType(String addressTypeCode) {
		List<String> usedCodes = getUsedPurposeCodesForAddressType(addressTypeCode);

		for (TelecommunicationPurpose purpose : TelecommunicationPurpose.values()) {
			if (!usedCodes.contains(purpose.getStartText())) {
				return purpose.getText();
			}
		}
		return null; // All purpose codes are used for this address type
	}

	/**
	 * Gets an available telecommunication purpose code that is not currently used in the section
	 *
	 * @param section the provider section (TELECOMMUNICATIONS or ELECTRONIC_ADDRESSES)
	 * @return the text of an available TelecommunicationPurpose, or null if all are used
	 */
	public String getAvailablePurposeCode(ProviderSection section) {
		List<String> usedCodes = getUsedPurposeCodes(section);

		for (TelecommunicationPurpose purpose : TelecommunicationPurpose.values()) {
			if (!usedCodes.contains(purpose.getStartText())) {
				return purpose.getText();
			}
		}
		return null; // All purpose codes are used
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
		   try {
			   List<WebElement> cancelButtons = selenium_.findElements(By.linkText("Cancel"));
			   if (!cancelButtons.isEmpty() && cancelButtons.get(0).isDisplayed()) {
				   selenium_.scrollIntoView(cancelButtons.get(0));
				   cancelButtons.get(0).click();
				   selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
			   } else {
				   // Cancel button not found or not visible, dialog may already be closed
				   // Optionally log or handle gracefully
			   }
		   } catch (Exception e) {
			   // Handle exception gracefully, dialog may already be closed
			   // Optionally log error
		   }
	}

	public void cancleAddDisciplinaryActionDataBlock(String actionIdentifier, boolean display, String description,
			String archiveDate, String effectiveFrom, String effectiveTo) {

		String dialogCss = getDialogCss(ProviderSection.DISCIPLINARY_ACTIONS);

		clickHeaderAddButton(ProviderSection.DISCIPLINARY_ACTIONS);

		fillDisciplinaryActionDataBlock(actionIdentifier, display, description, archiveDate, effectiveFrom,
				effectiveTo);

		   try {
			   List<WebElement> cancelButtons = selenium_.findElements(By.linkText("Cancel"));
			   if (!cancelButtons.isEmpty() && cancelButtons.get(0).isDisplayed()) {
				   selenium_.scrollIntoView(cancelButtons.get(0));
				   cancelButtons.get(0).click();
			   }
		   } catch (Exception e) {
			   // Handle exception gracefully
		   }

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));


	}

	/**
	 * Fill the Electronic Address Data Block form fields
	 *
	 * @param type          the type of electronic address (e.g., "E - Email", "F - FTP", "H - HTTP")
	 * @param purpose       the purpose of electronic address (e.g., "MC - Ministry Contact")
	 * @param address       the electronic address value
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	private void fillElectronicAddressDataBlock(String type, String purpose, String address,
			String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		setDropdownListByVisibleText(ProviderSection.ELECTRONIC_ADDRESSES, "type", type);
		setDropdownListByVisibleText(ProviderSection.ELECTRONIC_ADDRESSES, "purpose", purpose);

		String inputAddressCss = dialogCss + " >input#" + formName + "\\:electronicAddress";
		WebElement inputAddress = selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if (!StringUtils.isEmpty(address))
			inputAddress.sendKeys(address);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ELECTRONIC_ADDRESSES, effectiveFrom, effectiveTo);
	}

	/**
	 * Attempts to add an electronic address data block with provided values
	 *
	 * @param type          the type of electronic address (e.g., "E - Email", "F - FTP", "H - HTTP")
	 * @param purpose       the purpose of electronic address (e.g., "MC - Ministry Contact")
	 * @param address       the electronic address value
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String addElectronicAddressDataBlock(String type, String purpose, String address,
			String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		clickHeaderAddButton(ProviderSection.ELECTRONIC_ADDRESSES);

		fillElectronicAddressDataBlock(type, purpose, address, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ELECTRONIC_ADDRESSES, expectError);

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ELECTRONIC_ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
			clickDialogCancelButton(ProviderSection.ELECTRONIC_ADDRESSES);
		}

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Cancel adding an electronic address data block after filling the form
	 *
	 * @param type          the type of electronic address
	 * @param purpose       the purpose of electronic address
	 * @param address       the electronic address value
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	public void cancelAddElectronicAddressDataBlock(String type, String purpose, String address,
			String effectiveFrom, String effectiveTo) {
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		clickHeaderAddButton(ProviderSection.ELECTRONIC_ADDRESSES);

		fillElectronicAddressDataBlock(type, purpose, address, effectiveFrom, effectiveTo);

		   try {
			   List<WebElement> cancelButtons = selenium_.findElements(By.linkText("Cancel"));
			   if (!cancelButtons.isEmpty() && cancelButtons.get(0).isDisplayed()) {
				   selenium_.scrollIntoView(cancelButtons.get(0));
				   cancelButtons.get(0).click();
			   }
		   } catch (Exception e) {
			   // Handle exception gracefully
		   }

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Attempts to update an electronic address data block with provided values
	 *
	 * @param address       the electronic address value to update
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param endReasonCode the end reason code
	 * @param index         the data block index to update
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updateElectronicAddressDataBlock(String address, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		clickDataBlockUpdateButton(ProviderSection.ELECTRONIC_ADDRESSES, index);
		waitSeconds(2);

		String inputAddressCss = dialogCss + " >input#" + formName + "\\:electronicAddress";
		WebElement inputAddress = selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if (!StringUtils.isEmpty(address))
			inputAddress.sendKeys(address);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ELECTRONIC_ADDRESSES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ELECTRONIC_ADDRESSES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ELECTRONIC_ADDRESSES, expectError);

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ELECTRONIC_ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
			clickDialogCancelButton(ProviderSection.ELECTRONIC_ADDRESSES);
		}

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Fill the Address Data Block form fields
	 *
	 * @param addressType   the type of address (e.g., "P - Physical location", "M - Mailing address")
	 * @param purpose       the purpose of the address (e.g., "MC - Ministry Contact")
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code (e.g., "BC - British Columbia")
	 * @param country       the country code (e.g., "CA - CANADA")
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	private void fillAddressDataBlock(String addressType, String purpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String province, String country,
			String postalCode, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		if(addressType != null){
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "addressType", addressType);
		}
		if(purpose != null){
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "addressPurpose", purpose);
		}

		// Fill address line 1
		String addressLine1Css = dialogCss + " >input#" + formName + "\\:addressLine1";
		WebElement addressLine1Element = selenium_.findElement(By.cssSelector(addressLine1Css));
		addressLine1Element.clear();
		if (!StringUtils.isEmpty(addressLine1))
			addressLine1Element.sendKeys(addressLine1);

		// Fill address line 2
		String addressLine2Css = dialogCss + " >input#" + formName + "\\:addressLine2";
		WebElement addressLine2Element = selenium_.findElement(By.cssSelector(addressLine2Css));
		addressLine2Element.clear();
		if (!StringUtils.isEmpty(addressLine2))
			addressLine2Element.sendKeys(addressLine2);

		// Fill address line 3
		String addressLine3Css = dialogCss + " >input#" + formName + "\\:addressLine3";
		WebElement addressLine3Element = selenium_.findElement(By.cssSelector(addressLine3Css));
		addressLine3Element.clear();
		if (!StringUtils.isEmpty(addressLine3))
			addressLine3Element.sendKeys(addressLine3);

		// Set country dropdown FIRST (this may change province field to text input for non-CA/US)
		if (!StringUtils.isEmpty(country)) {
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "country", country);
			// Wait for province fragment to update after country change
			waitSeconds(2);
		}

		// Fill city (autocomplete input) - type city and click first autocomplete result
		if (city != null) {
			String cityInputCss = "input#" + formName + "\\:city_input";
			String cityPanelCss = "span#" + formName + "\\:city_panel";
			WebElement cityInput = selenium_.findElement(By.cssSelector(cityInputCss));
			cityInput.clear();
			cityInput.sendKeys(city);
			// Wait for autocomplete panel to appear and click first item
			selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cityPanelCss)));
			waitSeconds(1);
			List<WebElement> cityItems = selenium_.findElements(By.cssSelector(cityPanelCss + " li.ui-autocomplete-item"));
			if (!cityItems.isEmpty()) {
				cityItems.get(0).click();
				selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(cityPanelCss)));
			}
		}

		// Set province - check if it's a dropdown (CA/US) or text input (other countries)
		if (!StringUtils.isEmpty(province)) {
			String provinceTxtCss = dialogCss + " input#" + formName + "\\:province_txt";
			List<WebElement> provinceTxtElements = selenium_.findElements(By.cssSelector(provinceTxtCss));
			if (!provinceTxtElements.isEmpty() && provinceTxtElements.get(0).isDisplayed()) {
				// Province is a text input (non-CA/US country)
				WebElement provinceTxt = provinceTxtElements.get(0);
				provinceTxt.clear();
				provinceTxt.sendKeys(province);
			} else {
				// Province is a dropdown (CA/US)
				setDropdownListByVisibleText(ProviderSection.ADDRESSES, "province_address", province);
			}
		}

		// Fill postal code
		String postalCodeCss = dialogCss + " >input#" + formName + "\\:postalCode";
		WebElement postalCodeElement = selenium_.findElement(By.cssSelector(postalCodeCss));
		postalCodeElement.clear();
		if (!StringUtils.isEmpty(postalCode))
			postalCodeElement.sendKeys(postalCode);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ADDRESSES, effectiveFrom, effectiveTo);
	}

	/**
	 * Attempts to add an address data block with provided values
	 *
	 * @param addressType   the type of address (e.g., "P - Physical location", "M - Mailing address")
	 * @param purpose       the purpose of the address (e.g., "MC - Ministry Contact")
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code (e.g., "BC - British Columbia")
	 * @param country       the country code (e.g., "CA - CANADA")
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String addAddressDataBlock(String addressType, String purpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String province, String country,
			String postalCode, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickHeaderAddButton(ProviderSection.ADDRESSES);

		fillAddressDataBlock(addressType, purpose, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);

		// Handle address validation popups that may appear (only when not expecting error)
		handleAddressValidationDialog();

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * Attempts to add an address data block with provided values, using raw city input (no autocomplete).
	 * Use this method when the city value is not expected to appear in autocomplete suggestions
	 * (e.g., when testing validation with invalid/long city names).
	 *
	 * @param addressType   the type of address (e.g., "P - Physical location", "M - Mailing address")
	 * @param purpose       the purpose of the address (e.g., "MC - Ministry Contact")
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name (entered without autocomplete)
	 * @param province      the province/state code (e.g., "BC - British Columbia")
	 * @param country       the country code (e.g., "CA - CANADA")
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String addAddressDataBlockRawCity(String addressType, String purpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String province, String country,
			String postalCode, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickHeaderAddButton(ProviderSection.ADDRESSES);

		fillAddressDataBlockRawCity(addressType, purpose, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			// Handle address validation popups that may appear (only when not expecting error)
			handleAddressValidationDialog();
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * Fills the address data block form with raw city input (no autocomplete selection).
	 */
	private void fillAddressDataBlockRawCity(String addressType, String purpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String province, String country,
			String postalCode, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		if(addressType != null){
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "addressType", addressType);
		}
		if (purpose != null) {
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "addressPurpose", purpose);
		}

		// Fill address line 1
		String addressLine1Css = dialogCss + " >input#" + formName + "\\:addressLine1";
		WebElement addressLine1Element = selenium_.findElement(By.cssSelector(addressLine1Css));
		addressLine1Element.clear();
		if (!StringUtils.isEmpty(addressLine1))
			addressLine1Element.sendKeys(addressLine1);

		// Fill address line 2
		String addressLine2Css = dialogCss + " >input#" + formName + "\\:addressLine2";
		WebElement addressLine2Element = selenium_.findElement(By.cssSelector(addressLine2Css));
		addressLine2Element.clear();
		if (!StringUtils.isEmpty(addressLine2))
			addressLine2Element.sendKeys(addressLine2);

		// Fill address line 3
		String addressLine3Css = dialogCss + " >input#" + formName + "\\:addressLine3";
		WebElement addressLine3Element = selenium_.findElement(By.cssSelector(addressLine3Css));
		addressLine3Element.clear();
		if (!StringUtils.isEmpty(addressLine3))
			addressLine3Element.sendKeys(addressLine3);

		// Set country dropdown FIRST (this may change province field to text input for non-CA/US)
		if (!StringUtils.isEmpty(country)) {
			setDropdownListByVisibleText(ProviderSection.ADDRESSES, "country", country);
			// Wait for province fragment to update after country change
			waitSeconds(2);
		}

		// Fill city (raw input - no autocomplete selection)
		if (city != null) {
			String cityInputCss = "input#" + formName + "\\:city_input";
			WebElement cityInput = selenium_.findElement(By.cssSelector(cityInputCss));
			cityInput.clear();
			cityInput.sendKeys(city);
			// Tab out to avoid autocomplete panel interference
			cityInput.sendKeys(Keys.TAB);
			waitSeconds(1);
		}

		// Set province - check if it's a dropdown (CA/US) or text input (other countries)
		if (!StringUtils.isEmpty(province)) {
			String provinceTxtCss = dialogCss + " input#" + formName + "\\:province_txt";
			List<WebElement> provinceTxtElements = selenium_.findElements(By.cssSelector(provinceTxtCss));
			if (!provinceTxtElements.isEmpty() && provinceTxtElements.get(0).isDisplayed()) {
				// Province is a text input (non-CA/US country)
				WebElement provinceTxt = provinceTxtElements.get(0);
				provinceTxt.clear();
				provinceTxt.sendKeys(province);
			} else {
				// Province is a dropdown (CA/US)
				setDropdownListByVisibleText(ProviderSection.ADDRESSES, "province_address", province);
			}
		}

		// Fill postal code
		String postalCodeCss = dialogCss + " >input#" + formName + "\\:postalCode";
		WebElement postalCodeElement = selenium_.findElement(By.cssSelector(postalCodeCss));
		postalCodeElement.clear();
		if (!StringUtils.isEmpty(postalCode))
			postalCodeElement.sendKeys(postalCode);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ADDRESSES, effectiveFrom, effectiveTo);
	}

	/**
	 * Cancel adding an address data block after filling the form
	 *
	 * @param addressType   the type of address
	 * @param purpose       the purpose of the address
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code
	 * @param country       the country code
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	public void cancelAddAddressDataBlock(String addressType, String purpose, String addressLine1,
			String addressLine2, String addressLine3, String city, String province, String country,
			String postalCode, String effectiveFrom, String effectiveTo) {
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickHeaderAddButton(ProviderSection.ADDRESSES);

		fillAddressDataBlock(addressType, purpose, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Attempts to update an address data block with provided values
	 *
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code
	 * @param country       the country code
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param endReasonCode the end reason code
	 * @param index         the data block index to update
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updateAddressDataBlock(String addressLine1, String addressLine2, String addressLine3,
			String city, String province, String country, String postalCode,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickDataBlockUpdateButton(ProviderSection.ADDRESSES, index);
		waitSeconds(3);

		//Only fill in the address fields that are valid for an update (address type and purpose cannot be updated, so pass in null to keep existing values)
		fillAddressDataBlock(null, null, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ADDRESSES, endReasonCode.getText());

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);

		// Handle address validation popups that may appear (only when not expecting error)
		handleAddressValidationDialog();

		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}

		//Small wait as sometimes due to speed, page stalls
        waitSeconds(3);

		return msgDisplay;
	}

	/**
	 * Attempts to update an address data block with provided values
	 *
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code
	 * @param country       the country code
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param endReasonCode the end reason code
	 * @param index         the data block index to update
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updateAddressDataBlockRawCity(String addressLine1, String addressLine2, String addressLine3,
			String city, String province, String country, String postalCode,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ADDRESSES);

		clickDataBlockUpdateButton(ProviderSection.ADDRESSES, index);
		waitSeconds(2);

		//Only fill in the address fields that are valid for an update (address type and purpose cannot be updated, so pass in null to keep existing values)
		fillAddressDataBlockRawCity(null, null, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ADDRESSES, endReasonCode.getText());

		clickDialogSubmitButton(ProviderSection.ADDRESSES, expectError);


		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.ADDRESSES);
		} else {
			// Handle address validation popups that may appear
			handleAddressValidationDialog();
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}

		//Small wait as sometimes due to speed, page stalls
        waitSeconds(2);

		return msgDisplay;
	}

	/**
	 * Attempts to update an address data block with provided values
	 *
	 * @param addressLine1  the first line of the address
	 * @param addressLine2  the second line of the address (optional)
	 * @param addressLine3  the third line of the address (optional)
	 * @param city          the city name
	 * @param province      the province/state code
	 * @param country       the country code
	 * @param postalCode    the postal code (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param endReasonCode the end reason code
	 * @param index         the data block index to update
	 */
	public void updateCancelAddressDataBlock(String addressLine1, String addressLine2, String addressLine3,
			String city, String province, String country, String postalCode,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index) {

		clickDataBlockUpdateButton(ProviderSection.ADDRESSES, index);
		waitSeconds(2);

		//Only fill in the address fields that are valid for an update (address type and purpose cannot be updated, so pass in null to keep existing values)
		fillAddressDataBlock(null, null, addressLine1, addressLine2, addressLine3,
				city, province, country, postalCode, effectiveFrom, effectiveTo);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ADDRESSES, endReasonCode.getText());

		clickDialogCancelButton(ProviderSection.ADDRESSES);
	}

	/**
	 * Fill the Telecommunication Data Block form fields
	 *
	 * @param telecomType   the type of telecommunication (e.g., "T - Telephone", "MB - Mobile")
	 * @param purpose       the purpose of telecommunication (e.g., "MC - Ministry Contact")
	 * @param areaCode      the area code
	 * @param phoneNumber   the phone number
	 * @param extension     the extension (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	private void fillTelecommunicationDataBlock(String telecomType, String purpose, String areaCode,
			String phoneNumber, String extension, String effectiveFrom, String effectiveTo) {
		String formName = DIALOG_MAP.get(ProviderSection.TELECOMMUNICATIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		if (telecomType != null) {
			setDropdownListByVisibleText(ProviderSection.TELECOMMUNICATIONS, "telecomType", telecomType);
			waitSeconds(1); // Wait for AJAX update after dropdown selection
		}

		// Organizations use "telecomPurposeFiltered", practitioners use "telecomPurpose"
		if (purpose != null) {
			String purposePanelCss = "div#" + formName + "\\:telecomPurposeFiltered_panel";
			if (!selenium_.findElements(By.cssSelector(purposePanelCss)).isEmpty()) {
				setDropdownListByVisibleText(ProviderSection.TELECOMMUNICATIONS, "telecomPurposeFiltered", purpose);
			} else {
				setDropdownListByVisibleText(ProviderSection.TELECOMMUNICATIONS, "telecomPurpose", purpose);
			}
		}

		// Fill area code
		String areaCodeCss = dialogCss + " >input#" + formName + "\\:AreaCode";
		WebElement areaCodeElement = selenium_.findElement(By.cssSelector(areaCodeCss));
		areaCodeElement.clear();
		if (!StringUtils.isEmpty(areaCode))
			areaCodeElement.sendKeys(areaCode);

		// Fill phone number
		String phoneNumberCss = dialogCss + " >input#" + formName + "\\:Phone_Number";
		WebElement phoneNumberElement = selenium_.findElement(By.cssSelector(phoneNumberCss));
		phoneNumberElement.clear();
		if (!StringUtils.isEmpty(phoneNumber))
			phoneNumberElement.sendKeys(phoneNumber);

		// Fill extension
		String extensionCss = dialogCss + " >input#" + formName + "\\:extension";
		WebElement extensionElement = selenium_.findElement(By.cssSelector(extensionCss));
		extensionElement.clear();
		if (!StringUtils.isEmpty(extension))
			extensionElement.sendKeys(extension);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);
	}

	/**
	 * Attempts to add a telecommunication data block with provided values
	 *
	 * @param telecomType   the type of telecommunication (e.g., "T - Telephone", "MB - Mobile")
	 * @param purpose       the purpose of telecommunication (e.g., "MC - Ministry Contact")
	 * @param areaCode      the area code
	 * @param phoneNumber   the phone number
	 * @param extension     the extension (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String addTelecommunicationDataBlock(String telecomType, String purpose, String areaCode,
			String phoneNumber, String extension, String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickHeaderAddButton(ProviderSection.TELECOMMUNICATIONS);

		fillTelecommunicationDataBlock(telecomType, purpose, areaCode, phoneNumber, extension, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.TELECOMMUNICATIONS, expectError);

		if (expectError) {
			// Wait for error message (waitErrorMessage already clicks Cancel when done)
			msgDisplay = waitErrorMessage(ProviderSection.TELECOMMUNICATIONS);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * Cancel adding a telecommunication data block after filling the form
	 *
	 * @param telecomType   the type of telecommunication
	 * @param purpose       the purpose of telecommunication
	 * @param areaCode      the area code
	 * @param phoneNumber   the phone number
	 * @param extension     the extension (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 */
	public void cancelAddTelecommunicationDataBlock(String telecomType, String purpose, String areaCode,
			String phoneNumber, String extension, String effectiveFrom, String effectiveTo) {
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickHeaderAddButton(ProviderSection.TELECOMMUNICATIONS);

		fillTelecommunicationDataBlock(telecomType, purpose, areaCode, phoneNumber, extension, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Attempts to update a telecommunication data block with provided values
	 *
	 * @param areaCode      the area code
	 * @param phoneNumber   the phone number
	 * @param extension     the extension (optional)
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo   the effective to date
	 * @param endReasonCode the end reason code
	 * @param index         the data block index to update
	 * @param expectError   whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updateTelecommunicationDataBlock(String areaCode, String phoneNumber, String extension,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.TELECOMMUNICATIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickDataBlockUpdateButton(ProviderSection.TELECOMMUNICATIONS, index);
		waitSeconds(2);

		// Update area code
		String areaCodeCss = dialogCss + " >input#" + formName + "\\:AreaCode";
		WebElement areaCodeElement = selenium_.findElement(By.cssSelector(areaCodeCss));
		areaCodeElement.clear();
		if (!StringUtils.isEmpty(areaCode))
			areaCodeElement.sendKeys(areaCode);

		// Update phone number
		String phoneNumberCss = dialogCss + " >input#" + formName + "\\:Phone_Number";
		WebElement phoneNumberElement = selenium_.findElement(By.cssSelector(phoneNumberCss));
		phoneNumberElement.clear();
		if (!StringUtils.isEmpty(phoneNumber))
			phoneNumberElement.sendKeys(phoneNumber);

		// Update extension
		String extensionCss = dialogCss + " >input#" + formName + "\\:extension";
		WebElement extensionElement = selenium_.findElement(By.cssSelector(extensionCss));
		extensionElement.clear();
		if (!StringUtils.isEmpty(extension))
			extensionElement.sendKeys(extension);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.TELECOMMUNICATIONS, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.TELECOMMUNICATIONS);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.TELECOMMUNICATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * add Identifiers Data Block
	 *
	 * @param idType        id type
	 * @param id            id
	 * @param effectiveFrom effective from date
	 * @param effectiveTo   effective to date
	 * @param expectError   if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string.
	 */
	public String addIdentifiersDataBlock(String idType, String id, String effectiveFrom, String effectiveTo,
			boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.IDENTIFIERS);

		clickHeaderAddButton(ProviderSection.IDENTIFIERS);
		// dropdown irtype
		if (!StringUtils.isEmpty(idType))
			setDropdownListByVisibleText(ProviderSection.IDENTIFIERS, "providerType", idType);
		// identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(id))
			inputId.sendKeys(id);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * update Identifiers Data Block
	 *
	 * @param id			id
	 * @param effectiveFrom effective from date
	 * @param effectiveTo   effective to date
	 * @param endReasonCode the end reason code
	 * @param index		 the data block index to update
	 * @param expectError   if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string.
	 */
	public String updateIdentifiersDataBlock(String id, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.IDENTIFIERS, index);

		// identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(id))
			inputId.sendKeys(id);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.IDENTIFIERS, endReasonCode.getText());
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * add Note Data Block
	 *
	 * @param id the note id
	 * @param text the note text
	 * @param effectiveFrom effective from date
	 * @param effectiveTo effective to date
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string.
	 */
	public String addNoteDataBlock(String id, String text, String effectiveFrom, String effectiveTo,
			boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.NOTES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.NOTES);

		clickHeaderAddButton(ProviderSection.NOTES);
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(id))
			inputId.sendKeys(id);

		String inputTextCss = dialogCss + " >textarea#" + formName + "\\:" + "noteText";
		WebElement inputText = selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if (!StringUtils.isEmpty(text))
			inputText.sendKeys(text);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.NOTES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.NOTES);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.NOTES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));

		return msgDisplay;
	}

	/**
	 * update Note Data Block
	 *
	 * @param text the note text
	 * @param effectiveFrom effective from date
	 * @param effectiveTo effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string
	 */
	public String updateNoteDataBlock(String text, String effectiveFrom, String effectiveTo, EndReason endReasonCode,
			int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.NOTES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.NOTES);

		clickDataBlockUpdateButton(ProviderSection.NOTES, index);
		waitSeconds(2);

		String inputTextCss = dialogCss + " >textarea#" + formName + "\\:" + "noteText";
		WebElement inputText = selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if (!StringUtils.isEmpty(text))
			inputText.sendKeys(text);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.NOTES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.NOTES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.NOTES);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.NOTES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * add RegIdentifiers Data Block
	 *
	 * @param regIdType the registry identifier type
	 * @param regId the registry identifier
	 * @param effectiveFrom effective from date
	 * @param effectiveTo effective to date
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string
	 */
	public String addRegIdentifiersDataBlock(String regIdType, String regId, String effectiveFrom, String effectiveTo,
			boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickHeaderAddButton(ProviderSection.REGISTRY_IDENTIFIERS);
		// fill up reg id type
		if (!StringUtils.isEmpty(regIdType))
			setDropdownListByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, "providerType", regIdType);
		// reg identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(regId))
			inputId.sendKeys(regId);

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.REGISTRY_IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.REGISTRY_IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.REGISTRY_IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * update RegIdentifiers Data Block
	 *
	 * @param regId the registry identifier
	 * @param effectiveFrom effective from date
	 * @param effectiveTo effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string
	 */
	public String updateRegIdentifiersDataBlock(String regId, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.REGISTRY_IDENTIFIERS, index);

		// reg identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(regId))
			inputId.sendKeys(regId);
		// end reason code
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.TELECOMMUNICATIONS, expectError);

		if (expectError) {
			msgDisplay = waitErrorMessage(ProviderSection.TELECOMMUNICATIONS);
		} else {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		}
		return msgDisplay;
	}

	/**
	 * add Status Data Block
	 *
	 * @param statusClassCode the status class code
	 * @param statusCode the status code
	 * @param statusReasonCode the status reason code
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string
	 */
	public String addStatusDataBlock(String statusClassCode, String statusCode, String statusReasonCode,
			String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickHeaderAddButton(ProviderSection.STATUSES);
		// fill up Status Class Code
		if (!StringUtils.isEmpty(statusClassCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusClassCode", statusClassCode);
		// Status Code
		if (!StringUtils.isEmpty(statusCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusCode", statusCode);
		// Status Reason Code
		if (!StringUtils.isEmpty(statusReasonCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusReasonCode", statusReasonCode);

		// effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.STATUSES, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.STATUSES, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.STATUSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * update Status Data Block
	 *
	 * @param statusCode the status class code
	 * @param statusReasonCode the status code
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty
	 *         string
	 */
	public String updateStatusDataBlock(String statusCode, String statusReasonCode, String effectiveFrom,
			String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickDataBlockUpdateButton(ProviderSection.STATUSES, index);

		// Status Code
		if (!StringUtils.isEmpty(statusCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusCode", statusCode);
		// Status Reason Code
		if (!StringUtils.isEmpty(statusReasonCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusReasonCode", statusReasonCode);
		//
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.STATUSES, endReasonCode.getText());
		// effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.STATUSES, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.STATUSES, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.STATUSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * cease Data Block By Key
	 *
	 * @param section
	 * @param key
	 * @param value
	 */
	public void ceaseDataBlockByKey(ProviderSection section, String key, String value) {
		int count = this.grabDataBlockCount(section);
		for (int index = 0; index < count; index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result = content.get(key);
			if (value.equals(result)) {
				this.ceaseDataBlock(section, index);
				break;
			}

		}

	}

	/**
	 * grab Data Block ByK ey
	 *
	 * @param section
	 * @param key
	 * @param value
	 * @return data block content
	 */
	public LinkedHashMap<String, String> grabDataBlockByKey(ProviderSection section, String key, String value) {
		LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
		int count = this.grabDataBlockCount(section);
		for (int index = 0; index < count; index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result = content.get(key);
			if (value.equals(result)) {
				resultMap = content;
				break;
			}

		}
		return resultMap;

	}

	/**
	 * find Data Bloack Index ByKey
	 *
	 * @param section
	 * @param key
	 * @param value
	 * @return Index or 0
	 */
	public int findDataBloackIndexByKey(ProviderSection section, String key, String value) {
		int indexReturn = 0;
		LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
		int count = this.grabDataBlockCount(section);
		for (int index = 0; index < count; index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result = content.get(key);
			if (value.equals(result)) {
				indexReturn = index;
				break;
			}

		}
		return indexReturn;

	}

	/**
	 * get Add Data Bloack Dropdown Menu List
	 *
	 * @param section
	 * @param dropdownName
	 * @return List of that dropdwon menu options
	 */
	public List<String> getAddDataBloackDropdownMenuList(ProviderSection section, String dropdownName) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		// String dropdownName="providerType";
		clickHeaderAddButton(section);
		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));
		dropdownMenu.expandItemPanel(true);
		List<String> providerTypeList = dropdownMenu.grabItemList();
		dropdownMenu.selectItem("Select One");
		waitSeconds(2);
		providerTypeList.remove("Select One");
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();
		return providerTypeList;
	}

	/**
	 * get Status Reason Code List
	 *
	 * @param statusCode
	 * @return Status Reason Code List
	 */
	public List<String> getStatusReasonCodeList(String statusCode) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickHeaderAddButton(ProviderSection.STATUSES);
		setDropdownListByVisibleText(ProviderSection.STATUSES, "statusCode", statusCode);
		waitSeconds(2);
		String dropdownName = "statusReasonCode";
		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));
		dropdownMenu.expandItemPanel(true);
		List<String> providerTypeList = dropdownMenu.grabItemList();
		dropdownMenu.selectItem("Select One");
		waitSeconds(2);
		providerTypeList.remove("Select One");
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();
		return providerTypeList;
	}

	/**
	 * update Note Data Block but click Cancel button at last
	 *
	 * @param text the note text
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateNoteDataBlockCancel(String text, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.NOTES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.NOTES);

		clickDataBlockUpdateButton(ProviderSection.NOTES, index);
		waitSeconds(2);

		String inputTextCss = dialogCss + " >textarea#" + formName + "\\:" + "noteText";
		WebElement inputText = selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if (!StringUtils.isEmpty(text))
			inputText.sendKeys(text);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.NOTES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.NOTES, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;

	}

	/**
	 * update Identifier Data Block but click Cancel button at last
	 *
	 * @param id the identifier
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param b
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateIdentifierDataBlockCancel(String id, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean b) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.IDENTIFIERS, index);

		// identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(id))
			inputId.sendKeys(id);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.IDENTIFIERS, endReasonCode.getText());
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.IDENTIFIERS, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;

	}

	/**
	 * update RegIdentifiers Data Block but click Cancel button at last
	 *
	 * @param regId the registry identifier
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param b
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateRegIdentifiersDataBlockCancel(String regId, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean b) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.REGISTRY_IDENTIFIERS, index);

		// reg identifier
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "identifier";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(regId))
			inputId.sendKeys(regId);
		// end reason code
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, endReasonCode.getText());
		// effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.REGISTRY_IDENTIFIERS, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;

	}

	/**
	 * update Status Data Block but click Cancel button at last
	 *
	 * @param statusCode the status code
	 * @param statusReasonCode the status reason code
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @param endReasonCode the end reason code
	 * @param index the data block index to update
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateStatusDataBlockCancel(String statusCode, String statusReasonCode, String effectiveFrom,
			String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickDataBlockUpdateButton(ProviderSection.STATUSES, index);

		// Status Code
		if (!StringUtils.isEmpty(statusCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusCode", statusCode);
		// Status Reason Code
		if (!StringUtils.isEmpty(statusReasonCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES, "statusReasonCode", statusReasonCode);
		//
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.STATUSES, endReasonCode.getText());
		// effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.STATUSES, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;
	}

	/**
	 * cease Data Block with expecting error message
	 *
	 * @param section
	 * @param index
	 * @return
	 */
	public String ceaseDataBlockWithError(ProviderSection section, int index) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();

		clickDataBlockUpdateButton(section, index);
		waitSeconds(2);

		String dialogCss = getDialogCss(section);

		setEndReasonByVisibleText(section, EndReason.CEASE.getText());

		String buttonCss = dialogCss + " > div.formControls" + " > button#" + formName + "\\:" + submitButtonName;
		WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		msgDisplay = waitErrorMessage(section);
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Checks whether the Update button is displayed for a data block header.
	 *
	 * @param section the facility section containing the data block
	 * @param index   the zero-based index of the data block within the section
	 * @return true if the Update button is present and visible; false otherwise
	 */
	public boolean isDataBlockUpdateButtonDisplayed(ProviderSection section, int index) {

		WebElement updateButton = null;
		try {
			updateButton = selenium_
					.findElement(By.cssSelector(getDataBlockHeaderUpdateButtonSelector(section, index)));
		} catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
		selenium_.scrollIntoView(updateButton);
		return updateButton.isDisplayed();
	}

	/**
	 * Attempts to update an electronic address data block with provided values.but click 'Cancel' button  at last
	 *
	 * @param address			the address to update the data block with
	 * @param effectiveFrom		the effective from date to update the data block with
	 * @param effectiveTo		the effective to date to update the data block with
	 * @param endReasonCode		the end reason string to fill in the End Reason code field
	 * @param index				the index of the electronic address data block to update
	 * @param expectError		whether an error is anticipated (true) or not (false)
	 * @return					a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateElectronicAddressDataBlockCancel(String address, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ELECTRONIC_ADDRESSES);

		clickDataBlockUpdateButton(ProviderSection.ELECTRONIC_ADDRESSES, index);
		waitSeconds(2);

		String inputAddressCss = dialogCss + " >input#" + formName + "\\:" + "electronicAddress";
		WebElement inputAddress = selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if (!StringUtils.isEmpty(address))
			inputAddress.sendKeys(address);
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ELECTRONIC_ADDRESSES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ELECTRONIC_ADDRESSES, effectiveFrom, effectiveTo);


		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;
	}

	/**
	 * Attempts to update a telecommunication data block with provided values, but click 'Cancel' button  at last
	 *
	 * @param areaCode			the area code to update the data block with
	 * @param phoneNumber		the phone number to update the data block with
	 * @param extension			the extension to update the data block with
	 * @param effectiveFrom		the effective from date to update the data block with
	 * @param effectiveTo		the effective to date to update the data block with
	 * @param endReasonCode			the end reason to fill the end reason code field with
	 * @param index				the index of the telecom data block to update
	 * @param expectError		whether an error is anticipated (true) or not (false)
	 * @return					a string of the error message, if expectError is true. otherwise an empty string
	 */

	public String updateTelecommunicationDataBlockCancel(String areaCode, String phoneNumber, String extension,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.TELECOMMUNICATIONS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.TELECOMMUNICATIONS);

		clickDataBlockUpdateButton(ProviderSection.TELECOMMUNICATIONS, index);
		waitSeconds(2);

		String inputAreaCodeCss = dialogCss + " >input#" + formName + "\\:" + "AreaCode";
		WebElement inputAreaCode = selenium_.findElement(By.cssSelector(inputAreaCodeCss));
		inputAreaCode.clear();
		if (!StringUtils.isEmpty(areaCode))
			inputAreaCode.sendKeys(areaCode);

		String inputPhoneNumberCss = dialogCss + " >input#" + formName + "\\:" + "Phone_Number";
		WebElement inputPhoneNumber = selenium_.findElement(By.cssSelector(inputPhoneNumberCss));
		inputPhoneNumber.clear();
		if (!StringUtils.isEmpty(phoneNumber))
			inputPhoneNumber.sendKeys(phoneNumber);

		String inputExtensionCss = dialogCss + " >input#" + formName + "\\:" + "extension";
		WebElement inputExtension = selenium_.findElement(By.cssSelector(inputExtensionCss));
		inputExtension.clear();
		if (!StringUtils.isEmpty(extension))
			inputExtension.sendKeys(extension);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.TELECOMMUNICATIONS, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;

	}


	/** Attempts to update a OrganizationalName data block with provided values
	 * @param orgName			name
	 * @param orgLongName       long name
	 * @param effectiveFrom		the effective from date to update the data block with
	 * @param effectiveTo		the effective to date to update the data block with
	 * @param endReasonCode			the end reason to fill the end reason code field with
	 * @param index				the index of the telecom data block to update
	 * @param expectError		whether an error is anticipated (true) or not (false)
	 * @return a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateOrganizationalNameDataBlock(String orgName, String orgLongName,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_NAMES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.ORGANIZATION_NAMES);

		clickDataBlockUpdateButton(ProviderSection.ORGANIZATION_NAMES, index);
		waitSeconds(2);

		String inputOrgNameCss = dialogCss + " >input#" + formName + "\\:" + "shortName";
		WebElement inputOrgName = selenium_.findElement(By.cssSelector(inputOrgNameCss));
		inputOrgName.clear();
		if (!StringUtils.isEmpty(orgName))
			inputOrgName.sendKeys(orgName);

		String inputOrgLongNameCss = dialogCss + " >input#" + formName + "\\:" + "longName";
		WebElement inputOrgLongName = selenium_.findElement(By.cssSelector(inputOrgLongNameCss));
		inputOrgLongName.clear();
		if (!StringUtils.isEmpty(orgLongName))
			inputOrgLongName.sendKeys(orgLongName);

		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.ORGANIZATION_NAMES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(ProviderSection.ORGANIZATION_NAMES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.ORGANIZATION_NAMES);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.ORGANIZATION_NAMES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;

	}


	/**determine if Update Dialog Span Editable
	 * @param section
	 * @param index
	 * @param value
	 * @return true if editable, false if not editable
	 */
	public boolean isUpdateDialogSpanEditable(ProviderSection section, int index, String value) {
		clickDataBlockUpdateButton(section, index);
		boolean result=true;

		String xpath = String.format(
		        "//span[contains(text(), '%s')]",
		        value
		    );
		WebElement element = selenium_.getDriver().findElement(By.xpath(xpath));
		String tagName = element.getTagName();

		if (!tagName.equals("input") && !tagName.equals("textarea")) {
			result=false;
		}

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return result;
	}

	/**grab Update Dialog Label Text
	 * @param section
	 * @param index
	 * @param labelText
	 * @return the span text that right to the label
	 */
	public String grabUpdateDialogLabelText(ProviderSection section, int index, String labelText) {
		clickDataBlockUpdateButton(section, index);
		String result="";

		String xpath = String.format(
		        "//label[contains(normalize-space(), '%s')]/ancestor::div[1]/following::span[@style='font-weight: bold;'][1]",
		        labelText
		    );
		try {
	            // Locate the adjacent span element using the XPath
	            WebElement spanElement = selenium_.getDriver().findElement(By.xpath(xpath));

	            result = spanElement.getText();

	        } catch (org.openqa.selenium.NoSuchElementException e) {


	        }

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return result;
	}
}
