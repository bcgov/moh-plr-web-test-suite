package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider.IdType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider.OrgNameType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider.RegIdType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationProperties;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Page object for updating Organization providers, extending UpdateProviderPage
 * with specific functionality for Organization Properties section
 */
public class UpdateOrganizationPage extends UpdateProviderPage {

	private static final Logger LOG = ExecutionLogManager.getLogger();

	/**
	 * Enhanced DIALOG_MAP that includes parent's entries plus ORGANIZATION_PROPERTIES.
	 * This shadows the parent's DIALOG_MAP to add organization-specific section.
	 */
	public static final Map<ProviderSection, ProviderDialog> DIALOG_MAP;
	
	static {
		// Create mutable map from parent's entries
		Map<ProviderSection, ProviderDialog> map = new HashMap<>(UpdateProviderPage.DIALOG_MAP);
		// Add organization-specific entry
		map.put(ProviderSection.ORGANIZATION_PROPERTIES, 
				new ProviderDialog("maintainPropertyDialog", "maintainPropertyForm", "effectiveStartDate",
						"effectiveEndDate", "propertySubmitButton", "EndReasonType","Add a new Property"));
		// Make it immutable
		DIALOG_MAP = Map.copyOf(map);
	}

	public UpdateOrganizationPage(SeleniumSession selenium, URI uri) {
		super(selenium, uri);
	}

	/**
	 * Get Dialog CSS selector for organization properties
	 *
	 * @param section the provider section
	 * @return string of dialog CSS selector
	 */
	private String getOrgDialogCss(ProviderSection section) {
		String dialogName = DIALOG_MAP.get(section).getDialogName();
		String formName = DIALOG_MAP.get(section).getFormName();

		return "div#" + dialogName + " > div#" + dialogName + "_content" + " > form#" + formName;
	}

	/**
	 * Click the Header Add Date Block button for organization properties
	 *
	 * @param section the provider section
	 */
	public void clickHeaderAddOrgPropertyButton(ProviderSection section) {
		String title = DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss = getSectionSelector(section) + " > div > div >a > img[title='" + title + "']";

		WebElement clickElement = selenium_
				.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(clickElementCss)));
		selenium_.scrollIntoView(clickElement);
		try {
			Objects.requireNonNull(clickElement).click();
		} catch (StaleElementReferenceException | ElementClickInterceptedException e) {
			waitSeconds(2);
			clickElement = selenium_.findElement(By.cssSelector(clickElementCss));
			clickElement.click();
		}

		String dialogCss = getOrgDialogCss(section);
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Override parent's method to use the child's DIALOG_MAP which includes ORGANIZATION_PROPERTIES
	 * Clicks the update button for a data block and waits for the dialog to appear
	 *
	 * @param section the provider section
	 * @param index the index of the data block to update
	 */
	@Override
	public void clickDataBlockUpdateButton(ProviderSection section, int index) {
		String selectCss = getDataBlockHeaderUpdateButtonSelector(section, index);
		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(selectCss)));
		WebElement updateButton = selenium_.findElementByCss(selectCss);
		selenium_.scrollIntoView(updateButton);

		try {
			updateButton.click();
		} catch (StaleElementReferenceException e) {
			waitSeconds(2);
			updateButton.click();
		}
		
		// Use child's DIALOG_MAP which includes ORGANIZATION_PROPERTIES
		String dialogCss;
		if (section == ProviderSection.ORGANIZATION_PROPERTIES) {
			dialogCss = getOrgDialogCss(section);
		} else {
			// For other sections, use parent's DIALOG_MAP directly
			String dialogName = DIALOG_MAP.get(section).getDialogName();
			String formName = DIALOG_MAP.get(section).getFormName();
			dialogCss = "div#" + dialogName + " > div#" + dialogName + "_content" + " > form#" + formName;
		}
			
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	/**
	 * Pick current date for Effective From field by clicking the datepicker "Today" button
	 *
	 * @param section the provider section
	 * @return the date picked as a string
	 */
	private String pickOrgEffectiveFromCurrentDate(ProviderSection section) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String effectiveFromStr = DIALOG_MAP.get(section).getEffectiveFromStr();
		
		// Click the datepicker trigger button
		String triggerButtonCss = "span#" + formName + "\\:" + effectiveFromStr + " > button.ui-datepicker-trigger";
		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(triggerButtonCss)));
		selenium_.findElementByCss(triggerButtonCss).click();
		
		// Wait for datepicker to appear and click the "Today" button
		By datepickerLocator = By.cssSelector("div#ui-datepicker-div");
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(datepickerLocator));
		
		By todayButtonLocator = By.cssSelector("button.ui-datepicker-current");
		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(todayButtonLocator));
		selenium_.findElement(todayButtonLocator).click();
		
		// Get the value from the input field
		String inputCss = "input#" + formName + "\\:" + effectiveFromStr + "_input";
		return selenium_.findElement(By.cssSelector(inputCss)).getAttribute("value");
	}

	/**
	 * set Dialog Effective From And Effective To for organization properties
	 *
	 * @param section the provider section
	 * @param effectiveFrom the effective from date (can be null to pick current date)
	 * @param effectiveTo the effective to date (can be null to leave empty)
	 */
	private void setOrgDialogEffectiveFromAndEffectiveTo(ProviderSection section, String effectiveFrom,
			String effectiveTo) {
		// If effectiveFrom is null, pick current date
		if (effectiveFrom == null) {
			pickOrgEffectiveFromCurrentDate(section);
		} else if (!StringUtils.isEmpty(effectiveFrom)) {
			String dialogCss = getOrgDialogCss(section);
			String formName = DIALOG_MAP.get(section).getFormName();
			String effectiveFromStr = DIALOG_MAP.get(section).getEffectiveFromStr();
			
			String effectFromCss = dialogCss + " >span#" + formName + "\\:" + effectiveFromStr + " >input#" + formName
					+ "\\:" + effectiveFromStr + "_input";
			WebElement effectFromElement = selenium_.findElement(By.cssSelector(effectFromCss));
			effectFromElement.clear();
			effectFromElement.sendKeys(effectiveFrom);
		}

		// Handle effectiveTo
		if (!StringUtils.isEmpty(effectiveTo)) {
			String dialogCss = getOrgDialogCss(section);
			String formName = DIALOG_MAP.get(section).getFormName();
			String effectiveToStr = DIALOG_MAP.get(section).getEffectiveToStr();
			
			String effectToCss = dialogCss + " >span#" + formName + "\\:" + effectiveToStr + " >input#" + formName + "\\:"
					+ effectiveToStr + "_input";
			WebElement effectToElement = selenium_.findElement(By.cssSelector(effectToCss));
			effectToElement.clear();
			effectToElement.sendKeys(effectiveTo);
		}
	}

	/**
	 * Click Dialog Submit Button for organization properties
	 *
	 * @param section the provider section
	 */
	public void clickOrgDialogSubmitButton(ProviderSection section) {
		clickOrgDialogSubmitButton(section, false);
	}

	/**
	 * Click the "Update/Add" dialog submission button for organization properties
	 *
	 * @param section the provider section
	 * @param expectError whether an error is expected
	 */
	private void clickOrgDialogSubmitButton(ProviderSection section, boolean expectError) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String submitButtonName = DIALOG_MAP.get(section).getSubmitButtonName();
		String dialogCss = getOrgDialogCss(section);

		String buttonCss = dialogCss + " > div.formControls" + " > button#" + formName + "\\:" + submitButtonName;
		WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		waitSeconds(2);
        if (expectError) {
			selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
		}
	}

	/**
	 * Override to handle organization properties with different message selector
	 *
	 * @param section the provider section
	 * @return String of Dialog Messages
	 */
	@Override
	protected String getDialogMessages(ProviderSection section) {
		if (section == ProviderSection.ORGANIZATION_PROPERTIES) {
			String msgDisplay = "";
			String formName = DIALOG_MAP.get(section).getFormName();
			String dialogCss = getOrgDialogCss(section);
			String alertMsgCss = dialogCss + " >div#" + formName + "\\:" + "messages";
			
			try {
				WebElement alertMsg = selenium_.findElement(By.cssSelector(alertMsgCss));
				if (alertMsg.isDisplayed()) {
					msgDisplay = alertMsg.getText();
				}
			} catch (Exception e) {
				// No messages found
			}
			
			return msgDisplay;
		} else {
			return super.getDialogMessages(section);
		}
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
		if (msgDisplay.isEmpty() || msgDisplay.contains("successfully")) return msgDisplay;
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;
	}

	/**
	 * Find And Fill an input field for organization properties (TEXT_FIELD type)
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName the form name
	 * @param field the field value
	 * @param fieldCss the field CSS selector
	 */
	private void findAndFillOrgInputField(String dialogCss, String formName, String field, String fieldCss) {
		String inputNameCss = dialogCss + " >input#" + formName + "\\:" + fieldCss;
		// Wait for the input field to be visible
        selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(inputNameCss)));
		WebElement inputName = selenium_.findElementByCss(inputNameCss);
        waitSeconds(1);

		inputName.clear();
		if (!StringUtils.isEmpty(field))
			inputName.sendKeys(field);
	}

	/**
	 * Find And Fill a textarea field for organization properties (TEXT_AREA type)
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName the form name
	 * @param field the field value
	 * @param fieldCss the field CSS selector
	 */
	private void findAndFillOrgTextAreaField(String dialogCss, String formName, String field, String fieldCss) {
		String textAreaCss = dialogCss + " >textarea#" + formName + "\\:" + fieldCss;
		// Wait for the textarea field to be visible
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(textAreaCss)));
		WebElement textArea = selenium_.findElementByCss(textAreaCss);
        waitSeconds(1);

		textArea.clear();
		if (!StringUtils.isEmpty(field))
			textArea.sendKeys(field);
	}

	/**
	 * Set property value field based on field type
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName the form name
	 * @param propertyValue the property value to set
	 * @param propertyType the organization property type enum
	 */
	private void setOrgPropertyValueByType(String dialogCss, String formName, String propertyValue, OrganizationProperties propertyType) {
		switch (propertyType.getFieldType()) {
			case TEXT_FIELD:
				// Handle text input fields
				findAndFillOrgInputField(dialogCss, formName, propertyValue, propertyType.getCssField());
				break;
			case TEXT_AREA:
				// Handle textarea fields
				findAndFillOrgTextAreaField(dialogCss, formName, propertyValue, propertyType.getCssField());
				break;
			case DROPDOWN_LIST:
				// Handle dropdown selection
				setOrgDropdownListByVisibleText(ProviderSection.ORGANIZATION_PROPERTIES, propertyType.getCssField(), propertyValue);
				break;
			case CHECKBOX:
				// Handle checkbox - propertyValue should be "true" or "false"
				setOrgCheckboxValue(dialogCss, formName, propertyType.getCssField(), Boolean.parseBoolean(propertyValue));
				break;
		}
	}

	/**
	 * Set checkbox value for organization properties
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName the form name
	 * @param fieldCss the field CSS selector
	 * @param checked whether to check or uncheck the checkbox
	 */
	private void setOrgCheckboxValue(String dialogCss, String formName, String fieldCss, boolean checked) {
		// First try selectBooleanButton (used by PCI Flag)
		String buttonCss = dialogCss + " > div#" + formName + "\\:" + fieldCss;
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(buttonCss)));
		WebElement buttonElement = selenium_.findElement(By.cssSelector(buttonCss));
		waitSeconds(1);

		String btnClass = buttonElement.getAttribute("class");

		// Check if it's a selectBooleanButton
		if (buttonElement.getAttribute("class").contains("ui-selectbooleanbutton")) {
			boolean isChecked = buttonElement.getAttribute("class").contains("ui-state-active");

			// Only click if the state needs to change
			if (isChecked != checked) {
				buttonElement.click();
			}
		} else {
			// Handle regular checkbox
			String checkboxCss = buttonCss + " > div.ui-chkbox-box";
			WebElement checkbox = selenium_.findElement(By.cssSelector(checkboxCss));
			String chkClass = checkbox.getAttribute("class");

			// default is to click (in the event of an unlikely null pointer)
			boolean isChecked = !checked;

			if (chkClass != null)
				isChecked = chkClass.contains("ui-state-active");

			// Only click if the state needs to change
			if (isChecked != checked) {
				checkbox.click();
			}
		}
	}

	/**
	 * Attempts to close a dialog by clicking the Cancel button
	 * Silently handles any exceptions if the dialog is not open or Cancel button is unavailable
	 *
	 * @param dialogCss the dialog CSS selector
	 */
	private void attemptToCloseDialog(String dialogCss) {
		try {
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			if (cancelButton.isDisplayed()) {
				cancelButton.click();
				selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
			}
		} catch (Exception e) {
			// Modal might not be open or Cancel button not available, ignore
		}
	}

	/**
	 * Attempts to add a registry identifier data block with provided values
	 * @param identifierType the identifier type
	 * @param identifier the identifier value
	 * @return the full message dialog of errors, if any exist. otherwise an empty string
	 */
	public String addRegistryIdentifierDataBlock(RegIdType identifierType, String identifier) {
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickHeaderAddOrgPropertyButton(ProviderSection.REGISTRY_IDENTIFIERS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Identifier Type dropdown
		setOrgDropdownListByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, "providerType", identifierType.getText());

		// Set Identifier field
		findAndFillOrgInputField(dialogCss, formName, identifier, "identifier");

		clickOrgDialogSubmitButton(ProviderSection.REGISTRY_IDENTIFIERS);

		String msgDisplay = getDialogMessages(ProviderSection.REGISTRY_IDENTIFIERS);

		if (!StringUtils.isEmpty(msgDisplay)) {
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to add an identifier data block with provided values
	 * @param identifierType the identifier type
	 * @param identifier the identifier value
	 * @return the full message dialog of errors, if any exist. otherwise an empty string
	 */
	public String addIdentifierDataBlock(IdType identifierType, String identifier) {
		String formName = DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.IDENTIFIERS);

		clickHeaderAddOrgPropertyButton(ProviderSection.IDENTIFIERS);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Identifier Type dropdown
		setOrgDropdownListByVisibleText(ProviderSection.IDENTIFIERS, "providerType", identifierType.getText());

		// Set Identifier field
		findAndFillOrgInputField(dialogCss, formName, identifier, "identifier");

		clickOrgDialogSubmitButton(ProviderSection.IDENTIFIERS);

		String msgDisplay = getDialogMessages(ProviderSection.IDENTIFIERS);

		if (!StringUtils.isEmpty(msgDisplay)) {
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to add an organization name data block with provided values
	 * @param nameType the organization name type
	 * @param name the organization name
	 * @param description the organization description
	 * @param expectError whether an error is anticipated
	 * @return the full message dialog of errors, if any exist. otherwise an empty string
	 */
	public String addOrganizationNameDataBlock(OrgNameType nameType, String name, String description, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_NAMES).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.ORGANIZATION_NAMES);

		clickHeaderAddOrgPropertyButton(ProviderSection.ORGANIZATION_NAMES);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Name Type dropdown
		setOrgDropdownListByVisibleText(ProviderSection.ORGANIZATION_NAMES, "type", nameType.getText());

		// Set Name field
		findAndFillOrgInputField(dialogCss, formName, name, "shortName");

		// Set Description field
		findAndFillOrgInputField(dialogCss, formName, description, "longName");

		setOrgDialogEffectiveFromAndEffectiveTo(ProviderSection.ORGANIZATION_NAMES,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date());

		clickOrgDialogSubmitButton(ProviderSection.ORGANIZATION_NAMES, expectError);

		if(expectError)
			msgDisplay=waitErrorMessage(ProviderSection.CONDITIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to add an organization property data block with provided values
	 *
	 * @param propertyType the organization property type enum
	 * @param propertyValue the property value to fill
	 * @param effectiveFrom the effective from date
	 * @param effectiveTo the effective to date
	 * @return the full message dialog of errors, if any exist. otherwise an empty string
	 */
	public String addOrganizationPropertyDataBlock(OrganizationProperties propertyType, String propertyValue, String effectiveFrom, String effectiveTo) {
		String msgDisplay;
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_PROPERTIES).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.ORGANIZATION_PROPERTIES);

		try {
			clickHeaderAddOrgPropertyButton(ProviderSection.ORGANIZATION_PROPERTIES);

			// Wait for dialog to be visible and stable
			selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
			waitSeconds(2);

			// Set property type dropdown
			setOrgDropdownListByVisibleText(ProviderSection.ORGANIZATION_PROPERTIES, "PropertyType", propertyType.getDisplayName());

			// Set property value based on field type
			setOrgPropertyValueByType(dialogCss, formName, propertyValue, propertyType);

			setOrgDialogEffectiveFromAndEffectiveTo(ProviderSection.ORGANIZATION_PROPERTIES, effectiveFrom, effectiveTo);

			clickOrgDialogSubmitButton(ProviderSection.ORGANIZATION_PROPERTIES);

			msgDisplay = getDialogMessages(ProviderSection.ORGANIZATION_PROPERTIES);

			if (!StringUtils.isEmpty(msgDisplay)) {
				WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
				cancelButton.click();
			}

			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		} catch (Exception e) {
			// If any exception occurs, attempt to close the modal before propagating the exception
			attemptToCloseDialog(dialogCss);
			// Re-throw the original exception
			throw e;
		}
		return msgDisplay;
	}

	/**
	 * Attempts to add an organization property data block with provided values and current date as effective from
	 *
	 * @param propertyType the organization property type enum
	 * @param propertyValue the property value to fill
	 * @return the full message dialog of errors, if any exist. otherwise an empty string
	 */
	public String addOrganizationPropertyDataBlock(OrganizationProperties propertyType, String propertyValue) {
		return addOrganizationPropertyDataBlock(propertyType, propertyValue, null, null);
	}

	/**
	 * Attempts to update a registry identifier data block with provided values
	 * @param identifier the identifier value
	 * @param endReason the end reason to specify when updating
	 * @param index the index of the data block to update
	 * @param expectError whether an error is anticipated
	 * @return a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateRegistryIdentifierDataBlock(String identifier, EndReason endReason, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.REGISTRY_IDENTIFIERS, index);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Identifier field
		findAndFillOrgInputField(dialogCss, formName, identifier, "identifier");

		if (endReason != null)
			setOrgEndReasonByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, endReason.getText());

		clickOrgDialogSubmitButton(ProviderSection.REGISTRY_IDENTIFIERS);

		if (expectError)
			msgDisplay = waitOrgErrorMessage(ProviderSection.REGISTRY_IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to update an identifier data block with provided values
	 * @param identifier the identifier value
	 * @param endReason the end reason to specify when updating
	 * @param index the index of the data block to update
	 * @param expectError whether an error is anticipated
	 * @return a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateIdentifierDataBlock(String identifier, EndReason endReason, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.IDENTIFIERS, index);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Identifier field
		findAndFillOrgInputField(dialogCss, formName, identifier, "identifier");

		if (endReason != null)
			setOrgEndReasonByVisibleText(ProviderSection.IDENTIFIERS, endReason.getText());

		clickOrgDialogSubmitButton(ProviderSection.IDENTIFIERS);

		if (expectError)
			msgDisplay = waitOrgErrorMessage(ProviderSection.IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to update an organization name data block with provided values
	 *
	 * @param name the organization name
	 * @param description the organization description
	 * @param endReason the end reason to specify when updating
	 * @param index the index of the data block to update
	 * @param expectError whether an error is anticipated
	 * @return a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateOrganizationNameDataBlock(String name, String description, EndReason endReason, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_NAMES).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.ORGANIZATION_NAMES);

		clickDataBlockUpdateButton(ProviderSection.ORGANIZATION_NAMES, index);

		// Wait for dialog to be visible and stable
		selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		waitSeconds(2);

		// Set Name field
		findAndFillOrgInputField(dialogCss, formName, name, "shortName");

		// Set Description field
		findAndFillOrgInputField(dialogCss, formName, description, "longName");

		if (endReason != null)
			setOrgEndReasonByVisibleText(ProviderSection.ORGANIZATION_NAMES, endReason.getText());

		clickOrgDialogSubmitButton(ProviderSection.ORGANIZATION_NAMES);

		if (expectError)
			msgDisplay = waitOrgErrorMessage(ProviderSection.ORGANIZATION_NAMES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to update an organization property data block with provided values
	 *
	 * @param propertyType the organization property type enum
	 * @param propertyValue the property value to update
	 * @param endReason the end reason to specify when updating
	 * @param index the index of the data block to update
	 * @param expectError whether an error is anticipated
	 * @return a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateOrganizationPropertyDataBlock(OrganizationProperties propertyType, String propertyValue,
			EndReason endReason, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_PROPERTIES).getFormName();
		String dialogCss = getOrgDialogCss(ProviderSection.ORGANIZATION_PROPERTIES);

		try {
			clickDataBlockUpdateButton(ProviderSection.ORGANIZATION_PROPERTIES, index);

			// Wait for dialog to be visible and stable
			selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
			waitSeconds(2);

			// Set property value based on field type
			setOrgPropertyValueByType(dialogCss, formName, propertyValue, propertyType);

			if (endReason != null)
				setOrgEndReasonByVisibleText(ProviderSection.ORGANIZATION_PROPERTIES, endReason.getText());

			clickOrgDialogSubmitButton(ProviderSection.ORGANIZATION_PROPERTIES);

			if (expectError)
				msgDisplay = waitOrgErrorMessage(ProviderSection.ORGANIZATION_PROPERTIES);

			try {
				selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
			} catch (Exception e) {
				//If we are not expecting an error, but one occurs, the dialog may still be present, so we may want to fetch the error message.
				msgDisplay = waitOrgErrorMessage(ProviderSection.ORGANIZATION_PROPERTIES);
			}
		} catch (Exception e) {
			// If any exception occurs, attempt to close the modal before propagating the exception
			attemptToCloseDialog(dialogCss);
			// Re-throw the original exception
			throw e;
		}

		return msgDisplay;
	}
	/**
	 * set End Reason By Visible Text for organization properties
	 * 
	 * @param section the provider section
	 * @param visibleText the visible text to select
	 */
	private void setOrgEndReasonByVisibleText(ProviderSection section, String visibleText) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String endReasonName = DIALOG_MAP.get(section).getEndReasonName();

		DropDownMenu endReasonDrop = 
			new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + endReasonName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + endReasonName + "_panel"));

		endReasonDrop.selectItem(visibleText);
	}

	/**
	 * set Drop down List By Visible Text for organization properties
	 *
	 * @param section the provider section
	 * @param dropdownName the dropdown field name
	 * @param visibleText the visible text to select
	 */
	private void setOrgDropdownListByVisibleText(ProviderSection section, String dropdownName, String visibleText) {
		if (StringUtils.isEmpty(visibleText))
			return;
		String formName = DIALOG_MAP.get(section).getFormName();

		// Wait for the dropdown label to be clickable before creating the DropDownMenu
		By labelLocator = By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label");
		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(labelLocator));
		waitSeconds(1);

		DropDownMenu dropdownMenu = 
			new DropDownMenu(selenium_,
				labelLocator,
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));

		dropdownMenu.selectItem(visibleText);
	}

	/**
	 * wait Error Message showing up for organization properties, and return a copy of message as result
	 *
	 * @param section the provider section
	 * @return String of error messages
	 */
	public String waitOrgErrorMessage(ProviderSection section) {
		String msgDisplay = getDialogMessages(section);
		while (StringUtils.isEmpty(msgDisplay)) {
			waitSeconds(5);
			try {
				msgDisplay = getDialogMessages(section);
			} catch (StaleElementReferenceException e) {
				waitSeconds(5);
			}
		}
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return msgDisplay;
	}

	/**
	 * Cease Organization Property Data Block
	 *
	 * @param index the data block index
	 */
	public void ceaseOrganizationPropertyDataBlock(int index) {
		String formName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_PROPERTIES).getFormName();
		String submitButtonName = DIALOG_MAP.get(ProviderSection.ORGANIZATION_PROPERTIES).getSubmitButtonName();
		String dialogCss = getOrgDialogCss(ProviderSection.ORGANIZATION_PROPERTIES);

		try {
			clickDataBlockUpdateButton(ProviderSection.ORGANIZATION_PROPERTIES, index);
			waitSeconds(2);

			setOrgEndReasonByVisibleText(ProviderSection.ORGANIZATION_PROPERTIES, EndReason.CEASE.getText());

			String buttonCss = dialogCss + " > div.formControls" + " > button#" + formName + "\\:" + submitButtonName;
			WebElement button = selenium_.findElement(By.cssSelector(buttonCss));
			button.click();
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		} catch (Exception e) {
			// If any exception occurs, attempt to close the modal before propagating the exception
			attemptToCloseDialog(dialogCss);
			// Re-throw the original exception
			throw e;
		}
	}

	/**
	 * Cease All Organization Property Data Blocks
	 */
	public void ceaseAllOrganizationPropertyDataBlocks() {
		int count = grabActiveDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES, true);
		for (int i = 0; i < count; i++) {
			ceaseOrganizationPropertyDataBlock(0);
		}
	}
}
