package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateFacilityPage extends ViewFacilityPage {
	
	/*protected final ViewHeaderFragment viewHeader_;
	protected final ViewFacilityPage viewFacilityPage_;*/

	public static final Map<FacilitySection, FacilityDialog> DIALOG_MAP = Map.of(
			FacilitySection.NAMES,new FacilityDialog("maintainFacilityNameDialog", "maintainFacilityNameForm", "effectiveStartDate",
					"effectiveEndDate", "orgNameSubmitButton", "EndReasonType","Add a new Facility Name"),
			FacilitySection.IDENTIFIERS, new FacilityDialog("maintainIdDialog", "maintainIdentifierForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "","Add"), 
			FacilitySection.NOTES, new FacilityDialog("maintainNoteDialog", "maintainNoteForm", "effectiveFromDate",
					"effectiveToDate", "idNoteSubmitButton", "endReasonCode","Add a new Note"),
			FacilitySection.ORGANIZATION_RELATIONSHIPS,new FacilityDialog("maintainOrganizationRelationshipDialog", "maintainOrgRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "orgRelationshipSubmitButton", "EndReasonType","Add a new Provider Relationship"), 
			FacilitySection.TELECOMMUNICATIONS, new FacilityDialog("maintainTelecomDialog", "maintainTelecomForm", "effectiveFromDate",
					"effectiveToDate", "idTeleSubmitButton", "EndReasonType","Add a new Telecommunication"), 
			FacilitySection.ELECTRONIC_ADDRESSES, new FacilityDialog("maintainElectronicAddressDialog", "maintainElectronicAddressForm", "effectiveStartDate",
					"effectiveEndDate", "electronicAddressSubmitButton", "EndReasonType","Add a new Electronic Address"));


	public UpdateFacilityPage(SeleniumSession selenium, URI uri) {
		super(selenium,  uri);
		
	}

	/**
	 * Open facility detail view page
	 *
	 * @param fauthId 	the facility auth ID to open
	 * 
	 */
	public void openFacility(String fauthId) {
		// this.viewFacilityPage_.openFacility(fauthId);
		requireNonNull(fauthId);
		if (uri_ != null) {
			selenium_.getDriver().get(UriUtils.getUriWithQuery(uri_, "f=" + fauthId).toString());
			waitForReady();
		} else {
			throw new UnsupportedOperationException("Page URL is not specified.");
		}
	}

    /**
     * Tries to wait some number of seconds. Will fail the test used in if interrupted.
	 * TODO this should be used as little as possible in favour of selenium implicit waits.
     *
     * @param second					the number of seconds to wait.
     */
	public void waitSeconds(int second) {
		try {
			Thread.sleep(1000L * second);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

	}
	
    /**
     * Gets whether a specified data block is active.
     *
     * @param section	the facility section to look inside
     * @param index		the index of the data block within the data section
     * @return			whether the specified data block is active (true) or not (false)
     */
	public boolean grabDataBlockActive(FacilitySection section, int index) {
		By locator = By.cssSelector(getDataBlockHeaderActiveSelector(section, index));
		return selenium_.searchElement(locator) != null;
	}
	
	/**
	 * get Data Block Header Update Button Selector
	 *
	 * @param section the facility section to select
	 * @param index   the index of data block within the section to select
	 * @return 		  CSS selector of Data Block Header Update Button
	 */
	protected String getDataBlockHeaderUpdateButtonSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions "
				+ " > span >a >img[title^='Update']";

	}

    /**
     * Gets a facility section's active/inactive data block count.
     *
     * @param section		the facility section to count active data blocks within.
     * @param active		whether to check for active (true) or inactive (false) data blocks.
     * @return				the number of active/inactive data blocks depending on the value of active
     */
	public int grabActiveDataBlockCount(FacilitySection section, boolean active) {
		int count = 0;
		for (int i = 0; i < grabDataBlockCount(section); i++) {
			if (grabDataBlockActive(section, i)) {
				if (active) {
					count++;
				}
			} else {
				if (!active) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Clicks a button, waiting if necessary.
	 * Uses a JavaScript Executor to press button if a normal click fails due to interception or stale element.
	 *
	 * @param button WebElement of button to be clicked
	 */
	private void clickbuttonWait(WebElement button) {
		try {
			button.click();
		} catch (ElementClickInterceptedException | StaleElementReferenceException e) {
			JavascriptExecutor js = (JavascriptExecutor) selenium_.getDriver();
			js.executeScript("arguments[0].click();", button);
		}

	}

    /**
     * Attempts to click the update button on a specified data block
     *
     * @param section		the facility section to find the data block's update button within
     * @param index			the specific index of the data block to find and click the update button for
     */
	public void clickDataBlockUpdateButton(FacilitySection section, int index) {
		String selectCss = getDataBlockHeaderUpdateButtonSelector(section, index);
		WebElement updateButton = selenium_.waitUntil(ExpectedConditions
				.elementToBeClickable(By.cssSelector(selectCss)));
		selenium_.scrollIntoView(updateButton);

		try {
			updateButton.click();
		} catch (StaleElementReferenceException e) {
			// Re-locate the element and retry the action
			waitSeconds(2);
			//updateButton = selenium_.findElement(By.cssSelector(selectCss));
			updateButton.click();
		}
		String dialogCss = getDialogCss(section);
		WebElement visibleElement = selenium_
				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));

	}
	
	
	/**
	 * Get Dialog CSS selector
	 *
	 * @param section   the facility section to select
	 * @return 			string of dialog CSS selector
	 */
	private String getDialogCss(FacilitySection section) {
		String dialogName =DIALOG_MAP.get(section).getDialogName();
		String formName=DIALOG_MAP.get(section).getFormName();

		return "div#"+dialogName+" > div#"+dialogName+"_content" +" > form#"+formName;
	}
	
	/**
	 * click Data Block Update Button. Dialog will pop up if successful.
	 * @param section
	 * @param visibleText
	 */
	private void setEndReasonByVisibleText(FacilitySection section, String visibleText) {
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
	 * @param section
	 * @param dropdownName
	 * @param visibleText
	 *
	 */
	private void setDropdownListByVisibleText(FacilitySection section, String dropdownName, String visibleText) {
		if (StringUtils.isEmpty(visibleText))
			return;
		String formName = DIALOG_MAP.get(section).getFormName();

		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropdownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropdownName + "_panel"));

		dropdownMenu.selectItem(visibleText);
	}	
	
	/**
	 * select Drop down List By DropMenu
	 *
	 * @param section
	 * @param dropDownName
	 * @param dropdownText
	 */
	private void selectDropdownListByDropMenu(FacilitySection section, String dropDownName, String dropdownText) {

		String formName = DIALOG_MAP.get(section).getFormName();

		DropDownMenu dropdownMenu = new DropDownMenu(selenium_,
				By.cssSelector("label#" + formName + "\\:" + dropDownName + "_label"),
				By.cssSelector("div#" + formName + "\\:" + dropDownName + "_panel"));

		dropdownMenu.selectItem(dropdownText);

	}
	
	/**
	 * Click Dialog Submit Button
	 *
	 * @param section
	 *
	 */
	public void clickDialogSubmitButton(FacilitySection section) {
		clickDialogSubmitButton(section, false);
	}

	/**
	 * Click the "Update/Add" dialog submission button. If an error is anticipated,
	 * wait for the error message to appear.
	 *
	 * @param section
	 *
	 */
	private void clickDialogSubmitButton(FacilitySection section, boolean expectError) {

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
	 * wait Error Message showing up, and return a copy of message as result
	 * note the result is a set of messages, if there are more than one error messages
	 *
	 * @param section
	 * @return String of error messages
	 */
	public String waitErrorMessage(FacilitySection section) {

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
	 * Click the Header Add Date Block button. A dialog box will appear upon successful completion.
	 *
	 * @param section
	 *
	 */
	public void clickHeaderAddDateBlockButton(FacilitySection section) {
		String title = DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss = getSectionSelector(section) + " > div > div >a > img[title='" + title + "'";

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
		selenium_
				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
	}
	
	/**
	 * check if Header Add Date Block Button Displayed
	 *
	 * @param section
	 * @return True if "Add Date Block" Button Displayed, otherwise return false
	 */
	public boolean isHeaderAddDateBlockButtonDisplayed(FacilitySection section) {
		String title = DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss = getSectionSelector(section) + " > div > div >a > img[title='" + title + "'";
		WebElement updateButton = null;

		try {
			updateButton = selenium_.findElement(By.cssSelector(clickElementCss));

		} catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
		selenium_.scrollIntoView(updateButton);
		return updateButton.isDisplayed();

	}
	
	

	/**
	 * set Dialog Effective From And Effective To.
	 *
	 * @param section
	 * @param effectiveFrom
	 * @param effectiveTo
	 */
	private void setDialogEffectiveFromAndEffectiveTo(FacilitySection section, String effectiveFrom,
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
	 * Click Data Bloc kUpdate Button. Dialog will pop up if successful.
	 * @param section
	 * @return String of Dialog Messages
	 */
	private String getDialogMessages(FacilitySection section) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String alertMsgCss = dialogCss + " >div#" + formName + "\\:" + "messages";
		WebElement alertMsg = selenium_.findElement(By.cssSelector(alertMsgCss));
		if (alertMsg.isDisplayed()) {
			msgDisplay = alertMsg.getText();

		}
		return msgDisplay;

	}
	
	private void selectDropdownListByClick(FacilitySection section, String dropDownName, String dropdownText) {
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);

			String dropListCss = dialogCss + " > div#" + formName + "\\:" + dropDownName
				+ " >div.ui-helper-hidden-accessible" + " > select#" + formName + "\\:" + dropDownName + "_input";
		WebElement dropList = selenium_.findElement(By.cssSelector(dropListCss));
		selenium_.scrollIntoView(dropList);
		Select select = new Select(dropList);
		List<WebElement> options = select.getOptions();
		int index = 0;
		boolean found = false;
		if (!StringUtils.isEmpty(dropdownText)) {
			for (WebElement option : options) {
				String str = option.getAttribute("text");
				if (option.getAttribute("text").equals(dropdownText)) {
					found = true;
					break;
				}

				index++;
			}
		}
		if (found) {
			String labelCss = dialogCss + " > div#" + formName + "\\:" + dropDownName + " > label#" + formName + "\\:"
					+ dropDownName + "_label";
			WebElement label = selenium_.findElement(By.cssSelector(labelCss));
			selenium_.scrollIntoView(label);
			label=selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(labelCss)));
			
			clickbuttonWait(label);

			WebElement element = selenium_.getDriver().switchTo().activeElement();
			for (int i = 0; i < index; i++) {
				element.sendKeys(Keys.ARROW_DOWN);
			}
			element.sendKeys(Keys.RETURN);
		}
		waitSeconds(2);
				
		
	}
	
	/**
	 * Verify if a DropdownList is displayed in a Update Dialog
	 *
	 * @param section
	 * @param index
	 * @param dropDownName
	 * @return True if the DropdownList is displayed, otherwise false
	 */
	public boolean findUpdateDialogDropdownList(FacilitySection section, int index, String dropDownName) {

		clickDataBlockUpdateButton(section, index);
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String dropListCss = dialogCss + " > div#" + formName + "\\:" + dropDownName
				+ " >div.ui-helper-hidden-accessible" + " > select#" + formName + "\\:" + dropDownName + "_input";
		boolean find = true;
		try {
			WebElement dropList = selenium_.findElement(By.cssSelector(dropListCss));
		} catch (Exception e) {
			find = false;
		}

		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return find;

	}
	
	/**
	 * Verify if a input element is displayed in a Update Dialog
	 *
	 * @param section
	 * @param index
	 * @param inputName
	 *
	 * @return true if the input element displayed, otherwise return false
	 */
	public boolean findUpdateDialogInput(FacilitySection section, int index, String inputName) {

		clickDataBlockUpdateButton(section, index);
		String formName = DIALOG_MAP.get(section).getFormName();
		String dialogCss = getDialogCss(section);
		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + inputName;
		boolean find = true;

		try {
			WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		} catch (Exception e) {
			find = false;
		}
		WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
		selenium_.scrollIntoView(cancelButton);
		cancelButton.click();

		return find;

	}
	

	/**
	 * Cease Data Block
	 *
	 * @param section
	 * @param index
	 */
	public void ceaseDataBlock(FacilitySection section, int index) {

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
	 * @param section
	 */
	public void ceaseAllDataBlockUnderSection(FacilitySection section) {
		int count = grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			ceaseDataBlock(section, 0);
		}
	}

	/**
	 * Find And Fill an input Field
	 *
	 * @param dialogCss
	 * @param formName
	 * @param field
	 * @param fieldCss
	 */
	private void findAndFillField(String dialogCss, String formName, String field, String fieldCss) {
		String inputNameCss = dialogCss + " >input#" + formName + "\\:" + fieldCss;
		WebElement inputName = selenium_.findElement(By.cssSelector(inputNameCss));
		inputName.clear();
		if (!StringUtils.isEmpty(field))
			inputName.sendKeys(field);
	}

    /**
     * Attempts to add a name data block with provided values
     *
     * @param name				the name to fill the name field with for the new data block
     * @param desc				the description to fill the description field with for the new data block
     * @param effectiveFrom		the effective from date to fill the effective from field with for the new data block
     * @param effectiveTo		the effective to date to fill the effective to field with for the new data block
     * @return					the full message dialog of errors, if any exist. otherwise an empty string
     */
	public String addNameDataBlock(String name, String desc, String effectiveFrom, String effectiveTo) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.NAMES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.NAMES);

		clickHeaderAddDateBlockButton(FacilitySection.NAMES);

		findAndFillField(dialogCss, formName, name, "shortName");

		findAndFillField(dialogCss, formName, desc, "description");

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NAMES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.NAMES);

		msgDisplay = getDialogMessages(FacilitySection.NAMES);

		if (!StringUtils.isEmpty(msgDisplay)) {
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

    /**
     * Attempts to update a name data block with provided values
     *
     * @param name				the name to update the data block with
     * @param desc				the description to update the data block with
     * @param effectiveFrom		the effective from date to update the data block with
     * @param effectiveTo		the effective to date to update the data block with
	 * @param endReasonCode			the end reason to specify when updating the data block
     * @param index				the index of name data block to update
     * @return					a string of the full message dialog of errors, if any exist. otherwise an empty string
     */
	public String updateNameDataBlock(String name, String desc, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.NAMES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.NAMES);

		clickDataBlockUpdateButton(FacilitySection.NAMES, index);

		findAndFillField(dialogCss, formName, name, "shortName");

		findAndFillField(dialogCss, formName, desc, "description");
		if (endReasonCode != null)
			setEndReasonByVisibleText(FacilitySection.NAMES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NAMES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.NAMES);

		msgDisplay = getDialogMessages(FacilitySection.NAMES);

		if (!StringUtils.isEmpty(msgDisplay)) {
			// cancel button
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

    /**
     * Attempts to add an identifier data block with provided values
     *
     * @param idType			the type of identifier to fill the identifier type field with in the data block
     * @param id				the identifier to fill the identifier field with in the data block
     * @param effectiveFrom		the effective from date to fill the effective from field with in the data block
     * @param effectiveTo		the effective to date to fill the effective to field with in the data block
     * @param expectError		whether to anticipate an error (true) or not (false)
     * @return					a string of the error message, if expectedError is true. otherwise an empty string
     */
	public String addIdentifierDataBlock(String idType, String id, String effectiveFrom, String effectiveTo,boolean expectError) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.IDENTIFIERS).getFormName();
		String dialogCss=getDialogCss(FacilitySection.IDENTIFIERS);
		
		clickHeaderAddDateBlockButton(FacilitySection.IDENTIFIERS);
		//waitSeconds(2);
		
		
		selectDropdownListByClick(FacilitySection.IDENTIFIERS,"identifierType",idType);
		
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(id))inputId.sendKeys(id);
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.IDENTIFIERS,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.IDENTIFIERS);
		
		if(expectError)
			msgDisplay=waitErrorMessage(FacilitySection.IDENTIFIERS);
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Add Note Data Block
	 *
	 * @param id
	 * @param text
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return If a predicted exception occurs, it will capture and return the
	 *         associated error message; otherwise, it waits for the dialog to
	 *         dismiss before returning an empty string.
	 */
	public String addNoteDataBlock(String id, String text, String effectiveFrom, String effectiveTo,
			boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.NOTES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.NOTES);

		clickHeaderAddDateBlockButton(FacilitySection.NOTES);
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

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NOTES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.NOTES);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.NOTES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));

		return msgDisplay;
	}

    /**
     * Attempts to update a note data block with provided values
     *
     * @param text				the note text to update the note text field with
     * @param effectiveFrom		the effective from date to update the effective from field with
     * @param effectiveTo		the effective to date to update the effective to field with
	 * @param endReasonCode		the end reason field to fill the end reason code field with
	 * @param index				the index of note data block to update
     * @param expectError		whether an error is anticipated (true) or not (false)
     * @return					a string of the error message, if expectError is true. otherwise an empty string
     */
	public String updateNoteDataBlock(String text, String effectiveFrom, String effectiveTo, EndReason endReasonCode,
			int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.NOTES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.NOTES);

		clickDataBlockUpdateButton(FacilitySection.NOTES, index);
		waitSeconds(2);

		String inputTextCss = dialogCss + " >textarea#" + formName + "\\:" + "noteText";
		WebElement inputText = selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if (!StringUtils.isEmpty(text))
			inputText.sendKeys(text);

		if (endReasonCode != null)
			setEndReasonByVisibleText(FacilitySection.NOTES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NOTES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.NOTES);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.NOTES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}


	/**
	 * Add Related Organization Data Block
	 *
	 * @param idType
	 * @param id
	 * @param relationType
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return If a predicted exception occurs, it will capture and return the
	 *         associated error message; otherwise, it waits for the dialog to
	 *         dismiss before returning an empty string.
	 */
	public String addRelatedOrganizationDataBlock(String idType, String id, String relationType, String effectiveFrom,
			String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.ORGANIZATION_RELATIONSHIPS).getFormName();
		String dialogCss = getDialogCss(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		clickHeaderAddDateBlockButton(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		setDropdownListByVisibleText(FacilitySection.ORGANIZATION_RELATIONSHIPS, "providerType", idType);

		String inputIdCss = dialogCss + " >input#" + formName + "\\:" + "rpi";
		WebElement inputId = selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if (!StringUtils.isEmpty(id))
			inputId.sendKeys(id);

		setDropdownListByVisibleText(FacilitySection.ORGANIZATION_RELATIONSHIPS, "relationshipType", relationType);

		if (expectError)
			waitSeconds(2);
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ORGANIZATION_RELATIONSHIPS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.ORGANIZATION_RELATIONSHIPS, expectError);

		if (expectError) {
			waitSeconds(10);

			msgDisplay = getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			while (StringUtils.isEmpty(msgDisplay)) {
				waitSeconds(5);
				try {
					msgDisplay = getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
				} catch (StaleElementReferenceException e) {
					waitSeconds(5);
				}
			}
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			selenium_.scrollIntoView(cancelButton);
			cancelButton.click();
		}

		try {
			selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		} catch (org.openqa.selenium.TimeoutException e) {
			msgDisplay = getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			WebElement cancelButton = selenium_.findElement(By.linkText("Cancel"));
			selenium_.scrollIntoView(cancelButton);
			cancelButton.click();
		}
		return msgDisplay;
	}
	/**
	 * update RelationshipOrg Data Block
	 *
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param index
	 * @param expectError
	 * @param endReason
	 * @return
	 */
	public String updateRelatedOrganizationDataBlock(String effectiveFrom, String effectiveTo, EndReason endReason,
			int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.ORGANIZATION_RELATIONSHIPS).getFormName();
		String dialogCss = getDialogCss(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		clickDataBlockUpdateButton(FacilitySection.ORGANIZATION_RELATIONSHIPS, index);

		if (endReason != null)
			setEndReasonByVisibleText(FacilitySection.ORGANIZATION_RELATIONSHIPS, endReason.getText());

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ORGANIZATION_RELATIONSHIPS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}


    /**
     * Attempts to add a telecommunication data block with provided values
     *
     * @param telecomType		the type of telecommunication to add a data block for
     * @param areaCode			the area code to fill the area code field for in the data block
     * @param phoneNumber		the phone number to fill the number field for in the data block
     * @param extension			the extension to fill the extension field for in the data block
     * @param effectiveFrom		the effective from date to fill the effective from field for in the data block
     * @param effectiveTo		the effective to date to fill the effective to field for in the data block
     * @param expectError		whether an error is anticipated (true) or not (false)
     * @return					a string of the error message, if expectError is true. otherwise an empty string
     */
	public String addTelecommunicationDataBlock(String telecomType, String areaCode, String phoneNumber, String extension,
			String effectiveFrom, String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.TELECOMMUNICATIONS).getFormName();
		String dialogCss = getDialogCss(FacilitySection.TELECOMMUNICATIONS);

		clickHeaderAddDateBlockButton(FacilitySection.TELECOMMUNICATIONS);

		setDropdownListByVisibleText(FacilitySection.TELECOMMUNICATIONS, "telecomType", telecomType);

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

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.TELECOMMUNICATIONS);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.TELECOMMUNICATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}


	/**
	 * Attempts to update a telecommunication data block with provided values
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

	public String updateTelecommunicationDataBlock(String areaCode, String phoneNumber, String extension,
			String effectiveFrom, String effectiveTo, EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.TELECOMMUNICATIONS).getFormName();
		String dialogCss = getDialogCss(FacilitySection.TELECOMMUNICATIONS);

		clickDataBlockUpdateButton(FacilitySection.TELECOMMUNICATIONS, index);
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
			setEndReasonByVisibleText(FacilitySection.TELECOMMUNICATIONS, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.TELECOMMUNICATIONS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.TELECOMMUNICATIONS);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.TELECOMMUNICATIONS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;

	}

    /**
     * Attempts to add an electronic address data block with provided values
     *
     * @param type				the type of electronic address to create a data block for
     * @param address			the address to fill the address field for in the data block
     * @param effectiveFrom		the effective from date to fill the effective from field for in the data block
     * @param effectiveTo		the effective to date to fill the effective to field for in the data block
     * @param expectError		whether an error is anticipated (true) or not (false)
     * @return					a string of the error message, if expectError is true. otherwise an empty string
     */
	public String addElectronicAddressDataBlock(String type, String address, String effectiveFrom, String effectiveTo,
			boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.ELECTRONIC_ADDRESSES);

		clickHeaderAddDateBlockButton(FacilitySection.ELECTRONIC_ADDRESSES);

		setDropdownListByVisibleText(FacilitySection.ELECTRONIC_ADDRESSES, "type", type);

		String inputAddressCss = dialogCss + " >input#" + formName + "\\:" + "electronicAddress";
		WebElement inputAddress = selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if (!StringUtils.isEmpty(address))
			inputAddress.sendKeys(address);

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ELECTRONIC_ADDRESSES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.ELECTRONIC_ADDRESSES);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.ELECTRONIC_ADDRESSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	/**
	 * Attempts to update an electronic address data block with provided values.
	 *
	 * @param address			the address to update the data block with
	 * @param effectiveFrom		the effective from date to update the data block with
	 * @param effectiveTo		the effective to date to update the data block with
	 * @param endReasonCode		the end reason string to fill in the End Reason code field
	 * @param index				the index of the electronic address data block to update
	 * @param expectError		whether an error is anticipated (true) or not (false)
	 * @return					a string of the error message, if expectError is true. otherwise an empty string
	 */
	public String updateElectronicAddressDataBlock(String address, String effectiveFrom, String effectiveTo,
			EndReason endReasonCode, int index, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss = getDialogCss(FacilitySection.ELECTRONIC_ADDRESSES);

		clickDataBlockUpdateButton(FacilitySection.ELECTRONIC_ADDRESSES, index);
		waitSeconds(2);

		String inputAddressCss = dialogCss + " >input#" + formName + "\\:" + "electronicAddress";
		WebElement inputAddress = selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if (!StringUtils.isEmpty(address))
			inputAddress.sendKeys(address);
		if (endReasonCode != null)
			setEndReasonByVisibleText(FacilitySection.ELECTRONIC_ADDRESSES, endReasonCode.getText());

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ELECTRONIC_ADDRESSES, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.ELECTRONIC_ADDRESSES);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.ELECTRONIC_ADDRESSES);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	/**
	 * Click CHSA Button
	 */
	public void clickCHSAButton() {
		if (grabDataBlockExpanded(FacilitySection.CIVIC_ADDRESSES, 0))
			expandDataBlock(FacilitySection.CIVIC_ADDRESSES, 0, true);

		WebElement updateChsaButton = selenium_
				.waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//button[./span[text()='Update CHSA']]")));
		selenium_.scrollIntoView(updateChsaButton);
		updateChsaButton.click();

		selenium_
		.waitUntil(ExpectedConditions.stalenessOf(updateChsaButton));
	}
	
	/**
	 * Add Relationship By Auto-Completion
	 *
	 * @param org
	 * @param relationType
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @param expectError
	 * @return If a predicted exception occurs, it will capture and return the
	 *         associated error message; otherwise, it waits for the dialog to
	 *         dismiss before returning an empty string.
	 */
	public String addRelationshipByAutoCompletion(MaintainOrgBuilder org, String relationType, String effectiveFrom,
			String effectiveTo, boolean expectError) {
		String msgDisplay = "";
		String formName = DIALOG_MAP.get(FacilitySection.ORGANIZATION_RELATIONSHIPS).getFormName();
		String dialogCss = getDialogCss(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		clickHeaderAddDateBlockButton(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		String AUTOCOMPLETE_FIELD_CSS = "input#maintainOrgRelationshipForm\\:orgNameAutoComplete_input";
		String panelCss = "span#maintainOrgRelationshipForm\\:orgNameAutoComplete";
		AutocompleteMenu menu = new AutocompleteMenu(selenium_, By.cssSelector(AUTOCOMPLETE_FIELD_CSS),
				By.cssSelector(panelCss));
		menu.selectItemFromPanelMatch(org.getName(), org.getAddressList().get(0).get("line1"));

		setDropdownListByVisibleText(FacilitySection.ORGANIZATION_RELATIONSHIPS, "relationshipType", relationType);

		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ORGANIZATION_RELATIONSHIPS, effectiveFrom, effectiveTo);

		clickDialogSubmitButton(FacilitySection.ORGANIZATION_RELATIONSHIPS, expectError);

		if (expectError)
			msgDisplay = waitErrorMessage(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	
}
