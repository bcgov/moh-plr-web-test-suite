package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.EndReason;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

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
	 * @param fauthId
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

	public void waitSeconds(int second) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

	}
	
	private String getSectionSelector(FacilitySection section) {
		return "div#" + section.getPanelId_();
	}
	
	private String getSectionContentSelector(FacilitySection section) {
		return getSectionSelector(section) + "_content";
	}

	
	private String getDataBlocksSelector(FacilitySection section) {
		return getSectionContentSelector(section) + " > table.recordDetailsPanels > tbody > tr > td > div.ui-panel";
	}
	
	public int grabDataBlockCount(FacilitySection section) {
		return selenium_.findElements(By.cssSelector(getDataBlocksSelector(section))).size();
	}
	
	private String getDataBlockHeaderActiveSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions > span > img[title='Active']";
	}
	
	private String getDataBlockSelector(FacilitySection section, int index) {
		if (index < 0) {
			String msg = String.format("Negative index (%d).", index);
			throw new IllegalArgumentException(msg);
		}
		return getDataBlocksSelector(section) + ":nth-of-type(" + (index + 1) + ")";
	}
	
	private String getDataBlockHeaderSelector(FacilitySection section, int index) {
		return getDataBlockSelector(section, index) + " > div.ui-panel-titlebar";
	}
	public boolean grabDataBlockActive(FacilitySection section, int index) {
		By locator = By.cssSelector(getDataBlockHeaderActiveSelector(section, index));
		return selenium_.searchElement(locator) != null;
	}
	
	protected String getDataBlockHeaderUpdateButtonSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions "
				+ " > span >a >img[title^='Update']";

	}
	
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
	
	private String getDialogCss(FacilitySection section) {
		String dialogName =DIALOG_MAP.get(section).getDialogName();
		String formName=DIALOG_MAP.get(section).getFormName();

		return "div#"+dialogName+" > div#"+dialogName+"_content" +" > form#"+formName;
	}
	
	private void setEndReasonByVisibleText(FacilitySection section, String visibleText) {
		
		String formName=DIALOG_MAP.get(section).getFormName();
		String endReasonName=DIALOG_MAP.get(section).getEndReasonName();
		
		String dialogCss=getDialogCss(section);
		String dropListCss=dialogCss+" > div#"+formName+"\\:"+endReasonName+" >div.ui-helper-hidden-accessible"+" > select#"+formName+"\\:"+endReasonName+"_input";
		WebElement dropList=selenium_.findElement(By.cssSelector(dropListCss));
		selenium_.scrollIntoView(dropList);
		Select select= new Select(dropList);
		List<WebElement> ll = select.getOptions();
		
		select.selectByVisibleText(visibleText);
	}
	
	
	private void setDropdownListByVisibleText(FacilitySection section, String dropdownName,String visibleText) {
		
		String formName=DIALOG_MAP.get(section).getFormName();
		String dialogCss=getDialogCss(section);
		
		String dropListCss=dialogCss+" > div#"+formName+"\\:"+dropdownName+" >div.ui-helper-hidden-accessible"+" > select#"+formName+"\\:"+dropdownName+"_input";
		WebElement dropList=selenium_.findElement(By.cssSelector(dropListCss));
		selenium_.scrollIntoView(dropList);
		Select select= new Select(dropList);
		select.selectByVisibleText(visibleText);
	}
	
	private void clickDialogSubmitButton(FacilitySection section) {
		
		String formName=DIALOG_MAP.get(section).getFormName();
		String submitButtonName=DIALOG_MAP.get(section).getSubmitButtonName();
		String dialogCss=getDialogCss(section);
		
		String buttonCss=dialogCss+" > div.formControls"+" > button#"+formName+"\\:"+submitButtonName;
		WebElement button=selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		waitSeconds(2);
	}

	private void clickDialogSubmitButton(FacilitySection section,boolean expectError) {
		
		String formName=DIALOG_MAP.get(section).getFormName();
		String submitButtonName=DIALOG_MAP.get(section).getSubmitButtonName();
		String dialogCss=getDialogCss(section);
		
		String buttonCss=dialogCss+" > div.formControls"+" > button#"+formName+"\\:"+submitButtonName;
		WebElement button=selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		waitSeconds(2);
		if(expectError) {
			selenium_.waitUntil(ExpectedConditions.visibilityOf(button));
		}
	}
	

	private void clickHeaderAddDateBlockButton(FacilitySection section) {
		String title=DIALOG_MAP.get(section).getAddButtonImgText();
		String clickElementCss=getSectionSelector(section)+" > div > div >a > img[title='"+title+"'";
		//WebElement clickElement=selenium_.findElement(By.cssSelector(clickElementCss));
		WebElement clickElement =selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector(clickElementCss)));
		selenium_.scrollIntoView(clickElement);
		try {
			clickElement.click();
	    } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
	        // Re-locate the element and retry the action
	    	waitSeconds(2);
	    	clickElement =selenium_.findElement(By.cssSelector(clickElementCss));
	        clickElement.click();
	    }
	
		
		String dialogCss=getDialogCss(section);
		WebElement visibleElement = selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dialogCss)));
		//waitSeconds(2);
	}
	
	private void setDialogEffectiveFromAndEffectiveTo(FacilitySection section,String effectiveFrom, String effectiveTo) {
		String dialogCss=getDialogCss(section);
		String formName=DIALOG_MAP.get(section).getFormName();
		String effectiveFromStr=DIALOG_MAP.get(section).getEffectiveFromStr();
		String effectiveToStr=DIALOG_MAP.get(section).getEffectiveToStr();
		//<span id="maintainFacilityNameForm:effectiveStartDate"class="ui-calendar ui-trigger-calendar"><input	id="maintainFacilityNameForm:effectiveStartDate_input"
		String effectFromCss=dialogCss+" >span#"+formName+"\\:"+effectiveFromStr+" >input#"+formName+"\\:"+effectiveFromStr+"_input";
		WebElement effectFromElement=selenium_.findElement(By.cssSelector(effectFromCss));
		effectFromElement.clear();
		if(!StringUtils.isEmpty(effectiveFrom))effectFromElement.sendKeys(effectiveFrom);
		
		//<span id="maintainFacilityNameForm:effectiveEndDate"class="ui-calendar ui-trigger-calendar"><inputid="maintainFacilityNameForm:effectiveEndDate_input"
		String effectToCss=dialogCss+" >span#"+formName+"\\:"+effectiveToStr+" >input#"+formName+"\\:"+effectiveToStr+"_input";
		WebElement effectToElement=selenium_.findElement(By.cssSelector(effectToCss));
		effectToElement.clear();
		if(!StringUtils.isEmpty(effectiveTo))effectToElement.sendKeys(effectiveTo);				
	}

	private String getDialogMessages(FacilitySection section) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(section).getFormName();
		String dialogCss=getDialogCss(section);
		String alertMsgCss=dialogCss+" >div#"+formName+"\\:"+"messages";
		WebElement alertMsg=selenium_.findElement(By.cssSelector(alertMsgCss));
		if(alertMsg.isDisplayed()) {
			msgDisplay=alertMsg.getText();
			
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
			label.click();

			WebElement element = selenium_.getDriver().switchTo().activeElement();
			for (int i = 0; i < index; i++) {
				element.sendKeys(Keys.ARROW_DOWN);
			}
			element.sendKeys(Keys.RETURN);
		}
		waitSeconds(2);
				
		
	}
	public void ceaseDataBlock(FacilitySection section,int index) {
		
		String formName=DIALOG_MAP.get(section).getFormName();
		String submitButtonName=DIALOG_MAP.get(section).getSubmitButtonName();
		
		clickDataBlockUpdateButton(section, index);
		waitSeconds(2);
		
		String dialogCss=getDialogCss(section);
			
		setEndReasonByVisibleText(section,EndReason.CEASE.getText());
		
		String buttonCss=dialogCss+" > div.formControls"+" > button#"+formName+"\\:"+submitButtonName;
		WebElement button=selenium_.findElement(By.cssSelector(buttonCss));
		button.click();
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
	}

	public String addNameDataBlock(String name, String desc, String effectiveFrom, String effectiveTo) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.NAMES).getFormName();
		String dialogCss=getDialogCss(FacilitySection.NAMES);
		
		clickHeaderAddDateBlockButton(FacilitySection.NAMES);
		//waitSeconds(2);
		
		//<input id="maintainFacilityNameForm:shortName"
		String inputNameCss=dialogCss+" >input#"+formName+"\\:"+"shortName";
		WebElement inputName=selenium_.findElement(By.cssSelector(inputNameCss));
		inputName.clear();
		if(!StringUtils.isEmpty(name))inputName.sendKeys(name);
		
		//<input id="maintainFacilityNameForm:description"
		String inputDescCss=dialogCss+" >input#"+formName+"\\:"+"description";
		WebElement inputDesc=selenium_.findElement(By.cssSelector(inputDescCss));
		inputDesc.clear();
		if(!StringUtils.isEmpty(desc))inputDesc.sendKeys(desc);
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NAMES,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.NAMES);
		
		msgDisplay=getDialogMessages(FacilitySection.NAMES);
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	public String updateNameDataBlock(String name, String desc, String effectiveFrom, String effectiveTo,int index) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.NAMES).getFormName();
		String dialogCss=getDialogCss(FacilitySection.NAMES);
		
		clickDataBlockUpdateButton(FacilitySection.NAMES, index);
		//waitSeconds(2);
		
		//<input id="maintainFacilityNameForm:shortName"
		String inputNameCss=dialogCss+" >input#"+formName+"\\:"+"shortName";
		WebElement inputName=selenium_.findElement(By.cssSelector(inputNameCss));
		inputName.clear();
		if(!StringUtils.isEmpty(name))inputName.sendKeys(name);
		
		//<input id="maintainFacilityNameForm:description"
		String inputDescCss=dialogCss+" >input#"+formName+"\\:"+"description";
		WebElement inputDesc=selenium_.findElement(By.cssSelector(inputDescCss));
		inputDesc.clear();
		if(!StringUtils.isEmpty(desc))inputDesc.sendKeys(desc);
		
		setEndReasonByVisibleText(FacilitySection.NAMES,EndReason.CHG.getText());
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NAMES,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.NAMES);
		
		msgDisplay=getDialogMessages(FacilitySection.NAMES);
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}

	public String addIdentifierDataBlock(String idType, String id, String effectiveFrom, String effectiveTo) {
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
		
		msgDisplay=getDialogMessages(FacilitySection.IDENTIFIERS);
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}



	public String addNoteDataBlock(String id, String text, String effectiveFrom,
			String effectiveTo) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.NOTES).getFormName();
		String dialogCss=getDialogCss(FacilitySection.NOTES);
		
		clickHeaderAddDateBlockButton(FacilitySection.NOTES);
		//waitSeconds(2);
		
		
		//selectDropdownListByClick(FacilitySection.IDENTIFIERS,"identifierType",idType);
		
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"identifier";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(id))inputId.sendKeys(id);
		
		String inputTextCss=dialogCss+" >textarea#"+formName+"\\:"+"noteText";
		WebElement inputText=selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if(!StringUtils.isEmpty(text))inputText.sendKeys(text);
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NOTES,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.NOTES);
		
		msgDisplay=getDialogMessages(FacilitySection.NOTES);
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));

		return msgDisplay;
	}



	public String updateNoteDataBlock( String text, String effectiveFrom, String effectiveTo,
			int index) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.NOTES).getFormName();
		String dialogCss=getDialogCss(FacilitySection.NOTES);
		
		clickDataBlockUpdateButton(FacilitySection.NOTES, index);
		//waitSeconds(2);
		
		String inputTextCss=dialogCss+" >textarea#"+formName+"\\:"+"noteText";
		WebElement inputText=selenium_.findElement(By.cssSelector(inputTextCss));
		inputText.clear();
		if(!StringUtils.isEmpty(text))inputText.sendKeys(text);
		
		
		setEndReasonByVisibleText(FacilitySection.NOTES,EndReason.CHG.getText());
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.NOTES,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.NOTES);
		
		msgDisplay=getDialogMessages(FacilitySection.NOTES);
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;	
	}

	
	public String addRelatedOrganizationDataBlock(String idType, String id, String relationType,String effectiveFrom,
			String effectiveTo, boolean expectError) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.ORGANIZATION_RELATIONSHIPS).getFormName();
		String dialogCss=getDialogCss(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		
		clickHeaderAddDateBlockButton(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		//waitSeconds(2);
		
		
		selectDropdownListByClick(FacilitySection.ORGANIZATION_RELATIONSHIPS,"providerType",idType);
		
		String inputIdCss=dialogCss+" >input#"+formName+"\\:"+"rpi";
		WebElement inputId=selenium_.findElement(By.cssSelector(inputIdCss));
		inputId.clear();
		if(!StringUtils.isEmpty(id))inputId.sendKeys(id);
		
		selectDropdownListByClick(FacilitySection.ORGANIZATION_RELATIONSHIPS,"relationshipType",relationType);
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ORGANIZATION_RELATIONSHIPS,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.ORGANIZATION_RELATIONSHIPS,expectError);
		
		//msgDisplay=getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		if (expectError) {
			int count=3;
			msgDisplay = getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			while (StringUtils.isEmpty(msgDisplay) && count!=0) {
				waitSeconds(5);count--;
				msgDisplay = getDialogMessages(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			}
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	public String addTelecommunicationDataBlock(String type, String areaCode, String phoneNumber,String extension,
			String effectiveFrom,String effectiveTo, boolean expectError) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.TELECOMMUNICATIONS).getFormName();
		String dialogCss=getDialogCss(FacilitySection.TELECOMMUNICATIONS);
		
		clickHeaderAddDateBlockButton(FacilitySection.TELECOMMUNICATIONS);
		//waitSeconds(2);
		
		
		selectDropdownListByClick(FacilitySection.TELECOMMUNICATIONS,"telecomType",type);
		
		String inputAreaCodeCss=dialogCss+" >input#"+formName+"\\:"+"AreaCode";
		WebElement inputAreaCode=selenium_.findElement(By.cssSelector(inputAreaCodeCss));
		inputAreaCode.clear();
		if(!StringUtils.isEmpty(areaCode))inputAreaCode.sendKeys(areaCode);
		
		String inputPhoneNumberCss=dialogCss+" >input#"+formName+"\\:"+"Phone_Number";
		WebElement inputPhoneNumber=selenium_.findElement(By.cssSelector(inputPhoneNumberCss));
		inputPhoneNumber.clear();
		if(!StringUtils.isEmpty(phoneNumber))inputPhoneNumber.sendKeys(phoneNumber);
		
		String inputExtensionCss=dialogCss+" >input#"+formName+"\\:"+"extension";
		WebElement inputExtension=selenium_.findElement(By.cssSelector(inputExtensionCss));
		inputExtension.clear();
		if(!StringUtils.isEmpty(extension))inputExtension.sendKeys(extension);
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.TELECOMMUNICATIONS,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.TELECOMMUNICATIONS);
		
		if (expectError) {
			int count=3;
			msgDisplay = getDialogMessages(FacilitySection.TELECOMMUNICATIONS);
			while (StringUtils.isEmpty(msgDisplay) && count!=0) {
				waitSeconds(5);count--;
				msgDisplay = getDialogMessages(FacilitySection.TELECOMMUNICATIONS);
			}
		}
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}
	
	public String addElectronicAddressDataBlock(String type, String address,
			String effectiveFrom,String effectiveTo,boolean expectError) {
		String msgDisplay="";
		String formName=DIALOG_MAP.get(FacilitySection.ELECTRONIC_ADDRESSES).getFormName();
		String dialogCss=getDialogCss(FacilitySection.ELECTRONIC_ADDRESSES);
		
		clickHeaderAddDateBlockButton(FacilitySection.ELECTRONIC_ADDRESSES);
		//waitSeconds(2);
		
		
		selectDropdownListByClick(FacilitySection.ELECTRONIC_ADDRESSES,"type",type);
		
		String inputAddressCss=dialogCss+" >input#"+formName+"\\:"+"electronicAddress";
		WebElement inputAddress=selenium_.findElement(By.cssSelector(inputAddressCss));
		inputAddress.clear();
		if(!StringUtils.isEmpty(address))inputAddress.sendKeys(address);
		
		
		setDialogEffectiveFromAndEffectiveTo(FacilitySection.ELECTRONIC_ADDRESSES,effectiveFrom,effectiveTo);
		
		
		clickDialogSubmitButton(FacilitySection.ELECTRONIC_ADDRESSES);
		

		if (expectError) {
			int count=3;
			msgDisplay = getDialogMessages(FacilitySection.ELECTRONIC_ADDRESSES);
			while (StringUtils.isEmpty(msgDisplay) && count!=0) {
				waitSeconds(5);count--;
				msgDisplay = getDialogMessages(FacilitySection.ELECTRONIC_ADDRESSES);
			}
		}
		
		
		if(!StringUtils.isEmpty(msgDisplay)){
			//cancel button
			WebElement cancelButton=selenium_.findElement(By.linkText("Cancel"));
			cancelButton.click();
		}
		
		
		selenium_.waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(dialogCss)));
		return msgDisplay;
	}



	




}
