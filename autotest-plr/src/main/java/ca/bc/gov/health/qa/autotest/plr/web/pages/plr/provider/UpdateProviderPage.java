package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderPage extends ViewProviderPage {

	public static final Map<ProviderSection, ProviderDialog> DIALOG_MAP = Map.ofEntries(
			Map.entry(ProviderSection.REGISTRY_IDENTIFIERS,
					new ProviderDialog("maintainRegIdDialog", "maintainRegIdForm", "effectiveStartDate",
							"effectiveEndDate", "registryIdSubmitButton", "EndReasonType", "Add")),
			Map.entry(ProviderSection.IDENTIFIERS,
					new ProviderDialog("maintainIdDialog", "maintainIdentifierForm", "effectiveFromDate",
							"effectiveToDate", "idSubmitButton", "EndReasonType", "Add")),
			Map.entry(ProviderSection.ORGANIZATION_NAMES,
					new ProviderDialog("maintainOrgNameDialog", "maintainOrgNameForm", "effectiveStartDate",
							"effectiveEndDate", "orgNameSubmitButton", "EndReasonType",
							"Add a new Organizational Name")),
			Map.entry(ProviderSection.PRACTITIONER_NAMES,
					new ProviderDialog("maintainPersonNameDialog", "maintainPersonNameForm", "effectiveStartDate",
							"effectiveEndDate", "personNameSubmitButton", "EndReasonType",
							"Add a new Practitioner Name")),
			Map.entry(ProviderSection.NOTES,
					new ProviderDialog("maintainNoteDialog", "maintainNoteForm", "effectiveFromDate", "effectiveToDate",
							"idNoteSubmitButton", "endReasonCode", "Add a new Note")),
			Map.entry(ProviderSection.ORGANIZATION_RELATIONSHIPS,
					new ProviderDialog("maintainOrganizationRelationshipDialog", "maintainOrgRelationshipForm",
							"effectiveStartDate", "effectiveEndDate", "orgRelationshipSubmitButton", "EndReasonType",
							"Add a new Organization Relationship")),
			Map.entry(ProviderSection.TELECOMMUNICATIONS,
					new ProviderDialog("maintainTelecomDialog", "maintainTelecomForm", "effectiveFromDate",
							"effectiveToDate", "idTeleSubmitButton", "EndReasonType", "Add a new Telecommunication")),
			Map.entry(ProviderSection.ELECTRONIC_ADDRESSES,
					new ProviderDialog("maintainElectronicAddressDialog", "maintainElectronicAddressForm",
							"effectiveStartDate", "effectiveEndDate", "electronicAddressSubmitButton", "EndReasonType",
							"Add a new Electronic Address")),
			Map.entry(ProviderSection.CONDITIONS,
					new ProviderDialog("maintainConditionDialog", "maintainConditionForm", "effectiveFromDate",
							"effectiveToDate", "idSubmitButton", "EndReasonType", "Add a new Condition")),
			Map.entry(ProviderSection.DISCIPLINARY_ACTIONS,
					new ProviderDialog("maintainDisActionDialog", "maintainDisActionForm", "effectiveFromDate",
							"effectiveToDate", "idSubmitButton", "EndReasonType", "Add a new Disciplinary Action")),
			Map.entry(ProviderSection.STATUSES, new ProviderDialog("maintainStatusDialog", "maintainStatusForm",
					"effectiveFromDate", "effectiveToDate", "idStatusSubmitButton", "endReasonCode", "Add a new Status")));

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
	 * Attempts to update a practitioner name data block with provided values
	 * @param prefix the name prefix
	 * @param first the first name
	 * @param second the second name
	 * @param third the third name
	 * @param surname the surname
	 * @param suffix the name suffix
	 * @param endReasonType the end reason type
	 * @param index the data block index
	 * @param expectError whether an error is expected
	 * @return the error message if expectError is true, otherwise an empty string
	 */
	public String updatePractitionerNameDataBlock(
			String prefix, String first, String second, String third, String surname, String suffix,
			EndReason endReasonType, int index, boolean expectError)
	{
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
	 * @param index the data block index
	 * @return CSS selector of Data Block Header Update Button
	 */
	protected String getDataBlockHeaderUpdateButtonSelector(ProviderSection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions "
				+ " > span >a >img[title^='Update']";
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

	public void clickHeaderAddButton(ProviderSection section)
	{
		String title = DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss = getSectionSelector(section) + " > div > div > a > img[title='" + title + "']";

		WebElement clickElement = selenium_
				.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(clickElementCss)));
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
	}

    /**
     * Attempts to click the update button on a specified data block
     *
     * @param section the provider section to find the data block's update button within
     * @param index the specific index of the data block to find and click the update button for
     */
	public void clickDataBlockUpdateButton(ProviderSection section, int index) {
		String selectCss = getDataBlockHeaderUpdateButtonSelector(section, index);
		WebElement updateButton = selenium_.waitUntil(ExpectedConditions
				.elementToBeClickable(By.cssSelector(selectCss)));
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

	public String addConditionDataBlock(String conditionType, String conditionIdentifier, boolean restriction,
										String explanation, String effectiveFrom, String effectiveTo, boolean expectError)
	{
		String msgDisplay = "";
		String dialogCss = getDialogCss(ProviderSection.CONDITIONS);

		clickHeaderAddButton(ProviderSection.CONDITIONS);

		fillConditionDataBlock(conditionType, conditionIdentifier, restriction, explanation, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(ProviderSection.CONDITIONS, expectError);

		if(expectError)
			msgDisplay=waitErrorMessage(ProviderSection.CONDITIONS);

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
		String msgDisplay = "";

		msgDisplay = getDialogMessages(section);
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
	 * Get Dialog Messages
	 *
	 * @param section the provider section
	 * @return String of dialog messages
	 */
	protected String getDialogMessages(ProviderSection section) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String msgCss = dialogCss + "> div#" + formName + "\\:messages > div > ul > li";
		
		try {
			java.util.List<WebElement> msgList = selenium_.findElements(By.cssSelector(msgCss));
			for (WebElement msg : msgList) {
				msgDisplay += msg.getText() + "\n";
			}
		} catch (Exception e) {
			// No messages found
		}
		
		return msgDisplay.trim();
	}

	/**
	 * Find And Fill an input field for organization properties (TEXT_FIELD type)
	 *
	 * @param dialogCss the dialog CSS selector
	 * @param formName the form name
	 * @param field the field value
	 * @param fieldCss the field CSS selector
	 */
	private void findAndFillInputField(String dialogCss, String formName, String field, String fieldCss) {
		String inputNameCss = dialogCss + " >input#" + formName + "\\:" + fieldCss;
		// Wait for the input field to be visible
		WebElement inputName = selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(inputNameCss)));
		inputName.clear();
		if (!StringUtils.isEmpty(field))
			inputName.sendKeys(field);
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
	

	/**add Identifiers Data Block
	 * @param idType id type 
	 * @param id id 
	 * @param effectiveFrom effective from date 
	 * @param effectiveTo effective to date 
	 * @param expectError if error messages are expected
	 * @return If there are error messages, return them; otherwise return an empty string. 
	 */
	public String addIdentifiersDataBlock(String idType, String id, String effectiveFrom, String effectiveTo,boolean expectError) {
		String msgDisplay = "";
		String formName=DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.IDENTIFIERS);

		clickHeaderAddButton(ProviderSection.IDENTIFIERS);
		//dropdown irtype
		if(!StringUtils.isEmpty(idType))
			setDropdownListByVisibleText(ProviderSection.IDENTIFIERS,"providerType",idType);
		//identifier 
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(id))inputId.sendKeys(id);
		
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	/** update Identifiers Data Block
	 * @param id
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param endReasonCode
	 * @param inswx
	 * @param expectError
	 * @return If there are error messages, return them; otherwise return an empty string.
	 */
	public String updateIdentifiersDataBlock(String id, String effectiveFrom, String effectiveTo,EndReason endReasonCode,
			int inswx ,boolean expectError) {
		String msgDisplay = "";
		String formName=DIALOG_MAP.get(ProviderSection.IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.IDENTIFIERS,inswx);
		
		//identifier 
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(id))inputId.sendKeys(id);
		
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.IDENTIFIERS, endReasonCode.getText());
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	/** add Note Data Block
	 * @param id
	 * @param text
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return If there are error messages, return them; otherwise return an empty string.
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
	/**  update Note Data Block
	 * @param text
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param endReasonCode
	 * @param index
	 * @param expectError
	 * @return  If there are error messages, return them; otherwise return an empty string
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
	/** add RegIdentifiers Data Block
	 * @param regIdType
	 * @param regId
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return  If there are error messages, return them; otherwise return an empty string
	 */
	public String addRegIdentifiersDataBlock(String regIdType, String regId, String effectiveFrom,
			String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickHeaderAddButton(ProviderSection.REGISTRY_IDENTIFIERS);
		//fill up reg id type
		if(!StringUtils.isEmpty(regIdType))
			setDropdownListByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS,"providerType",regIdType);
		//reg identifier 
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(regId))inputId.sendKeys(regId);
				
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.REGISTRY_IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.REGISTRY_IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.REGISTRY_IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	/** update RegIdentifiers Data Block
	 * @param regIdType
	 * @param regId
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param endReasonCode
	 * @param index
	 * @param expectError
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateRegIdentifiersDataBlock(String regIdType, String regId, String effectiveFrom,
			String effectiveTo, EndReason endReasonCode,int index,boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.REGISTRY_IDENTIFIERS).getFormName();
		String dialogCss = getDialogCss(ProviderSection.REGISTRY_IDENTIFIERS);

		clickDataBlockUpdateButton(ProviderSection.REGISTRY_IDENTIFIERS, index);
		
		//reg identifier 
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(regId))inputId.sendKeys(regId);
		//end reason code
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.REGISTRY_IDENTIFIERS, endReasonCode.getText());
		//effective dates		
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.REGISTRY_IDENTIFIERS, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.REGISTRY_IDENTIFIERS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.REGISTRY_IDENTIFIERS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	/** add Status Data Block
	 * @param statusClassCode
	 * @param statusCode
	 * @param statusReasonCode
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String addStatusDataBlock(String statusClassCode, String statusCode, String statusReasonCode,
			String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickHeaderAddButton(ProviderSection.STATUSES);
		//fill up Status Class Code
		if(!StringUtils.isEmpty(statusClassCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES,"statusClassCode",statusClassCode);
		//Status Code
		if(!StringUtils.isEmpty(statusCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES,"statusCode",statusCode);
		//Status Reason Code
		if(!StringUtils.isEmpty(statusReasonCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES,"statusReasonCode",statusReasonCode);
		
		//effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.STATUSES, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.STATUSES, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.STATUSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	
	/**update Status Data Block
	 * @param statusClassCode
	 * @param statusCode
	 * @param statusReasonCode
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param endReasonCode
	 * @param index
	 * @param expectError
	 * @return If there are error messages, return them; otherwise return an empty string
	 */
	public String updateStatusDataBlock(String statusClassCode, String statusCode, String statusReasonCode,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode,
			int index,boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);

		clickDataBlockUpdateButton(ProviderSection.STATUSES, index);
		
		//Status Code
		if(!StringUtils.isEmpty(statusCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES,"statusCode",statusCode);
		//Status Reason Code
		if(!StringUtils.isEmpty(statusReasonCode))
			setDropdownListByVisibleText(ProviderSection.STATUSES,"statusReasonCode",statusReasonCode);
		//
		if (endReasonCode != null)
			setEndReasonByVisibleText(ProviderSection.STATUSES, endReasonCode.getText());
		//effective dates
		setDialogEffectiveFromAndEffectiveTo(ProviderSection.STATUSES, effectiveFrom, effectiveTo);
		clickDialogSubmitButton(ProviderSection.STATUSES, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(ProviderSection.STATUSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/** cease Data Block By Key
	 * @param section
	 * @param key
	 * @param value
	 */
	public void ceaseDataBlockByKey(ProviderSection section, String key, String value) {
		int count=this.grabDataBlockCount(section);
		for (int index=0;index<count;index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result=content.get(key);
			if( value.equals(result)) {
				this.ceaseDataBlock(section, index);
				break;
			}
			
		}
		
	}

	/** grab Data Block ByK ey
	 * @param section
	 * @param key
	 * @param value
	 * @return data block content
	 */
	public LinkedHashMap<String,String> grabDataBlockByKey(ProviderSection section, String key, String value) {
		LinkedHashMap<String,String> resultMap = new LinkedHashMap<>();
		int count=this.grabDataBlockCount(section);
		for (int index=0;index<count;index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result=content.get(key);
			if( value.equals(result)) {
				resultMap=content;
				break;
			}
			
		}
		return resultMap;
		
	}

	/** find Data Bloack Index ByKey
	 * @param section
	 * @param key
	 * @param value
	 * @return Index or 0 
	 */
	public int findDataBloackIndexByKey(ProviderSection section, String key, String value) {
		int indexReturn=0;
		LinkedHashMap<String,String> resultMap = new LinkedHashMap<>();
		int count=this.grabDataBlockCount(section);
		for (int index=0;index<count;index++) {
			LinkedHashMap<String, String> content = this.grabDataBlockContent(section, index);
			String result=content.get(key);
			if( value.equals(result)) {
				indexReturn=index;
				break;
			}
			
		}
		return indexReturn;
	
	}

	/** get Add Data Bloack Dropdown Menu List
	 * @param section
	 * @param dropdownName
	 * @return List of that dropdwon menu options
	 */
	public List<String> getAddDataBloackDropdownMenuList(ProviderSection section, String dropdownName) {
		String msgDisplay = "";
		String formName=DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		//String dropdownName="providerType";
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

	/** get Status Reason Code List
	 * @param statusCode
	 * @return  Status Reason Code List
	 */
	public List<String> getStatusReasonCodeList(String statusCode) {
		String msgDisplay = "";
		String formName=DIALOG_MAP.get(ProviderSection.STATUSES).getFormName();
		String dialogCss = getDialogCss(ProviderSection.STATUSES);
		
		clickHeaderAddButton(ProviderSection.STATUSES);
		setDropdownListByVisibleText(ProviderSection.STATUSES,"statusCode",statusCode);
		waitSeconds(2);
		String dropdownName="statusReasonCode";
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
}
