package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
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
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderPage extends ViewProviderPage {

	public static final Map<ProviderSection, ProviderDialog> DIALOG_MAP = Map.of(
			ProviderSection.REGISTRY_IDENTIFIERS, new ProviderDialog("maintainRegIdDialog", "maintainRegIdForm", "effectiveStartDate",
					"effectiveEndDate", "registryIdSubmitButton", "EndReasonType","Add"),
			ProviderSection.IDENTIFIERS, new ProviderDialog("maintainIdDialog", "maintainIdentifierForm", "effectiveFromDate",
					"effectiveToDate", "idSubmitButton", "EndReasonType","Add"),
			ProviderSection.ORGANIZATION_NAMES, new ProviderDialog("maintainOrgNameDialog", "maintainOrgNameForm", "effectiveStartDate",
							"effectiveEndDate", "orgNameSubmitButton", "EndReasonType","Add a new Organizational Name"),
			ProviderSection.PRACTITIONER_NAMES, new ProviderDialog("maintainPersonNameDialog", "maintainPersonNameForm", "effectiveStartDate",
					"effectiveEndDate", "personNameSubmitButton", "EndReasonType","Add a new Practitioner Name"),
			ProviderSection.NOTES, new ProviderDialog("maintainNoteDialog", "maintainNoteForm", "effectiveFromDate",
					"effectiveToDate", "idNoteSubmitButton", "endReasonCode","Add a new Note"),
			ProviderSection.ORGANIZATION_RELATIONSHIPS, new ProviderDialog("maintainOrganizationRelationshipDialog", "maintainOrgRelationshipForm", "effectiveStartDate",
					"effectiveEndDate", "orgRelationshipSubmitButton", "EndReasonType","Add a new Organization Relationship"), 
			ProviderSection.TELECOMMUNICATIONS, new ProviderDialog("maintainTelecomDialog", "maintainTelecomForm", "effectiveFromDate",
					"effectiveToDate", "idTeleSubmitButton", "EndReasonType","Add a new Telecommunication"), 
			ProviderSection.ELECTRONIC_ADDRESSES, new ProviderDialog("maintainElectronicAddressDialog", "maintainElectronicAddressForm", "effectiveStartDate",
					"effectiveEndDate", "electronicAddressSubmitButton", "EndReasonType","Add a new Electronic Address"));

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
		String dialogCss = getDialogCss(section);
		String msgCss = dialogCss + " > ul.messages" + " > li.message";
		
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
}
