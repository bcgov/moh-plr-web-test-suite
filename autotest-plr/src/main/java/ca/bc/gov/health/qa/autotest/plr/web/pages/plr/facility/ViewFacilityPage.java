package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A page object class for the View Facility page.
 */
public class ViewFacilityPage extends BasicWebPage {
	private static final Pattern DATA_KEY_SUFFIX_PATTERN = Pattern.compile(":$");
	private static final Pattern DATA_KEY_PARENS_PATTERN = Pattern.compile(" \\(.*\\)");
	private static final Pattern DATA_KEY_STAR_PATTERN = Pattern.compile("\\*$");
	private static final String TABLE_ROWS_SELECTOR = " > table > tbody > tr";

	private final ViewHeaderFragment viewHeader_;

	/**
	 * Initializes page object, overloaded constructor for no specified URL
	 *
	 * @param selenium the current SeleniumSession
	 */
	public ViewFacilityPage(SeleniumSession selenium) {
		this(selenium, null);
	}

	/**
	 * Initializes page object and changes selenium's main locator to View Facility
	 * Details heading
	 *
	 * @param selenium the current SeleniumSession
	 * @param uri      the URL to navigate to in inherited methods if applicable
	 */
	public ViewFacilityPage(SeleniumSession selenium, URI uri) {
		super(selenium, By.cssSelector("span#facilityDetailsGroup"), "View Facility Details", uri);
		viewHeader_ = new ViewHeaderFragment(selenium);
	}

	/**
	 * Expand or collapse
	 *
	 * @param expand: True to expand, false to collapse
	 */
	public void expandAll(boolean expand) {
		viewHeader_.expandAll(expand);
	}
	/**
	 *Refresh Page
	 *
	 */
	public void refreshPage() {
		selenium_.getDriver().navigate().refresh();

	}
	/**
	 * Maximize Window
	 *
	 */
	public void maxWindow() {
		selenium_.getDriver().manage().window().maximize();
	}

	/**
	 * Gets the view header
	 *
	 * @return a ViewHeaderFragment reference for the current page
	 */
	public ViewHeaderFragment getViewHeader() {
		return viewHeader_;
	}

	/**
	 * Constructs a CSS selector string to select a facility section's div panel
	 *
	 * @param section the Facility Section to select
	 * @return a CSS selector string for the facility section's div panel
	 */
	protected String getSectionSelector(FacilitySection section) {
		return "div#" + section.getPanelId_();
	}

	/**
	 * Constructs a CSS selector string to select a facility section's div content
	 * panel.
	 *
	 * @param section the Facility Section to select
	 * @return a CSS selector string for the facility section's div content panel
	 */
	protected String getSectionContentSelector(FacilitySection section) {
		return getSectionSelector(section) + "_content";
	}

	/**
	 * Constructs a CSS selector to select a facility section's "data blocks" where
	 * each div is a separate record inside the section (e.g.) in the Organization
	 * Relationship sections, each data block is a different relationship.
	 *
	 * @param section the Facility Section to select
	 * @return a CSS selector string for the facility section's data blocks
	 */
	protected String getDataBlocksSelector(FacilitySection section) {
		return getSectionContentSelector(section) + " > table.recordDetailsPanels > tbody > tr > td > div.ui-panel";
	}

	/**
	 * Constructs a CSS selector to select a specific "data block" from a facility
	 * section by index.
	 *
	 * @param section the Facility Section to select
	 * @param index   the index of data block to specifically select
	 * @return a CSS selector string for a specific data block in a facility section
	 */
	protected String getDataBlockSelector(FacilitySection section, int index) {
		if (index < 0) {
			String msg = String.format("Negative index (%d).", index);
			throw new IllegalArgumentException(msg);
		}
		return getDataBlocksSelector(section) + ":nth-of-type(" + (index + 1) + ")";
	}

	/**
	 * Constructs a CSS selector to select the content panel for a specific "data
	 * block" from a facility section by index.
	 *
	 * @param section the Facility Section to select
	 * @param index   the index of data block to specifically select
	 * @return a CSS selector string for a specific data block's content panel.
	 */
	public String getDataBlockContentSelector(FacilitySection section, int index) {
		return getDataBlockSelector(section, index) + "> div.ui-panel-content";
	}

	/**
	 * Finds the content panel for a specific "data block" in a facility section
	 *
	 * @param section the facility section to select
	 * @param index   the index of data block to specifically select
	 * @return a WebElement of a div containing a specific data block's content
	 *         panel.
	 */
	private WebElement findDataBlockContent(FacilitySection section, int index) {
		return selenium_.findElementByCss(getDataBlockContentSelector(section, index));
	}

	/**
	 * Determines whether a specific "data block"'s content panel in a facility
	 * section is displayed or not
	 *
	 * @param section the facility section to select
	 * @param index   the index of data block to specifically select
	 * @return whether the data block is displayed (true) or not displayed (false)
	 */
	public boolean grabDataBlockExpanded(FacilitySection section, int index) {
		return findDataBlockContent(section, index).isDisplayed();
	}

	/**
	 * Constructs a CSS selector for the header of a specific "data block" in a
	 * facility section
	 *
	 * @param section the facility section to select
	 * @param index   the index of data block to specifically select
	 * @return a CSS selector string to select the header panel of a data block
	 */
	protected String getDataBlockHeaderSelector(FacilitySection section, int index) {
		return getDataBlockSelector(section, index) + " > div.ui-panel-titlebar";
	}

	/**
	 * Constructs a CSS selector for the button to expand/collapse a "data block" in
	 * a facility section
	 *
	 * @param section the facility section to select
	 * @param index   the index of the data block within the facility section to
	 *                specifically select
	 * @return a CSS selector string to select the expand/collapse button of a data
	 *         block
	 */
	private String getDataBlockHeaderExpandSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > a[title='Expand/Collapse']";
	}

	/**
	 * Expands/collapses a "data block" or a specific instance of data within a
	 * facility section
	 *
	 * @param section the facility section to select
	 * @param index   the index of the data block within the facility section to
	 *                select
	 * @param expand  whether to expand (true) or collapse (false) the data block
	 */
	public void expandDataBlock(FacilitySection section, int index, boolean expand) {
		if (grabDataBlockExpanded(section, index) != expand) {
			WebElement expandCollapseButton = selenium_
					.findElement(By.cssSelector(getDataBlockHeaderExpandSelector(section, index)));
			selenium_.scrollIntoView(expandCollapseButton);
			expandCollapseButton.click();

			WebElement content = findDataBlockContent(section, index);
			if (expand) {
				selenium_.waitUntil(ExpectedConditions.visibilityOf(content));

				selenium_.waitUntil(ExpectedConditions.attributeToBe(content, "overflow", "visible"));
			} else {
				selenium_.waitUntil(ExpectedConditions.invisibilityOf(content));
			}
		}
	}

	/**
	 * check if Data Block Expand Button is Displayed
	 *
	 * @param section
	 * @param index
	 * @return True if the Data Block Expand Button is displayed, otherwise false
	 */
	public boolean isDataBlockExpandButtonDisplayed(FacilitySection section, int index) {
		WebElement expandCollapseButton = selenium_
				.findElement(By.cssSelector(getDataBlockHeaderExpandSelector(section, index)));
		selenium_.scrollIntoView(expandCollapseButton);
		return expandCollapseButton.isDisplayed();

	}

	private String getDataBlockHeaderActiveMarkSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions > span > img[title='Active']";

	}

	private String getDataBlockHeaderUpdateButtonSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions "
				+ " > span >a >img[title^='Update']";

	}

	/**
	 * TODO (KD) - doc
	 *
	 * @param section
	 * @param index
	 * @return
	 */
	public boolean isDataBlockActiveMarkDisplayed(FacilitySection section, int index) {
		WebElement activeMark = selenium_
				.findElement(By.cssSelector(getDataBlockHeaderActiveMarkSelector(section, index)));
		selenium_.scrollIntoView(activeMark);
		return activeMark.isDisplayed();
	}

	/**
	 * TODO (KD) - doc
	 *
	 * @param section
	 * @param index
	 * @return
	 */
	public boolean isDataBlockUpdateButtonDisplayed(FacilitySection section, int index) {
		// String cssString = getDataBlockHeaderSelector(section, index)+ " >
		// div.ui-panel-actions " + " > span >a >img[title^='Update']" ;
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
	 * TODO (KD) - doc
	 *
	 * @param section
	 * @param index
	 * @return
	 */
	public String grabDataBlockSummaryLine(FacilitySection section, int index) {
		String cssString = getDataBlockHeaderSelector(section, index);
		cssString = cssString + " > span.ui-panel-title";
		WebElement headerTitle = selenium_.findElement(By.cssSelector(cssString));
		selenium_.scrollIntoView(headerTitle);
		return headerTitle.getText();
	}

	/**
	 * Removes the colon and parentheses from data fields for easier reference
	 *
	 * @param key the key to be formatted and subsequently used in a hash map
	 * @return the formatted key as a string
	 */
	private static String formatDataKey(String key) {
		String formattedKey = DATA_KEY_STAR_PATTERN.matcher(key).replaceAll("");
		formattedKey = DATA_KEY_PARENS_PATTERN.matcher(formattedKey).replaceAll("");
		formattedKey = DATA_KEY_SUFFIX_PATTERN.matcher(formattedKey).replaceAll("");
		return formattedKey;
	}

	/**
	 * Gets the content from a specific data block within a facility section The
	 * Civic Address field has a different structure, so
	 * grabCivicAddressBlockContent must be used instead to obtain civic address
	 * details.
	 *
	 * @param section the facility section to get content from
	 * @param index   the index of data block within the facility section to get
	 *                content from
	 * @return a hash map mapping data block fields (String) to its associated
	 *         values (String)
	 * @throws IllegalStateException If a specific row of data in the block is
	 *                               formatted unexpectedly
	 */
	public LinkedHashMap<String, String> grabDataBlockContent(FacilitySection section, int index) {
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
		expandDataBlock(section, index, true);
		String dataBlockContentSelector = getDataBlockContentSelector(section, index);
		if (section == FacilitySection.ORGANIZATION_RELATIONSHIPS)
			dataBlockContentSelector += " > form";
		dataBlockContentSelector += TABLE_ROWS_SELECTOR;
		List<WebElement> dataRowElementList = selenium_.findElements(By.cssSelector(dataBlockContentSelector));

		return grabBlockContent(section, index, dataMap, dataRowElementList);
	}

	/**
	 * TODO (KD) - doc
	 *
	 * @param section            the facility section to get content from
	 * @param index              the index of block within the facility section to
	 *                           get content from
	 * @param dataMap            the hash map to fill with data block
	 *                           fields/associated values
	 * @param dataRowElementList
	 * @return a hash map mapping data block fields (String) to its associated
	 *         values (String)
	 */
	public LinkedHashMap<String, String> grabBlockContent(FacilitySection section, int index,
			LinkedHashMap<String, String> dataMap, List<WebElement> dataRowElementList) {
		if (!dataRowElementList.isEmpty()) {
			selenium_.scrollIntoView(dataRowElementList.getFirst());
		}
		for (WebElement dataRow : dataRowElementList) {
			List<WebElement> dataEntryList = dataRow.findElements(By.cssSelector("td"));
			int dataColumnCount = dataEntryList.size();
			if (dataColumnCount == 2 || dataColumnCount == 4) {
				dataMap.put(formatDataKey(dataEntryList.get(0).getText()), dataEntryList.get(1).getText());
				if (dataColumnCount == 4) {
					dataMap.put(formatDataKey(dataEntryList.get(2).getText()), dataEntryList.get(3).getText());
				}
			} else {
				String msg = String.format("Invalid data row (%s: %d: %s).", section.getTitle(), index,
						dataRow.getText());
				throw new IllegalStateException(msg);
			}
		}
		return dataMap;
	}

	/**
	 * Adds a civic address data field to a hash map.
	 *
	 * @param dataMap       the hash map to add the data field to
	 * @param dataEntryList a list of web elements from a civic address block
	 */
	private void addFieldDataMap(LinkedHashMap<String, String> dataMap, List<WebElement> dataEntryList, int index) {
		dataMap.put(formatDataKey(dataEntryList.get(index).findElement(By.cssSelector("td")).getText()),
				dataEntryList.get(index + 1).findElement(By.cssSelector("td")).getText());
	}

	private void addFieldDataMapSplit(LinkedHashMap<String, String> dataMap, List<WebElement> dataEntryList,
			int index) {
		String[] provParts = dataEntryList.get(index).findElement(By.cssSelector("td")).getText().split(":");
		String[] countyParts = dataEntryList.get(index + 1).findElement(By.cssSelector("td")).getText().split(":");
		dataMap.put(provParts[0], provParts[1].replaceAll("^[^A-Z]*", ""));
		dataMap.put(countyParts[0], dataEntryList.get(index + 2).findElement(By.cssSelector("td")).getText());
	}

	/**
	 * Open facility detail view page
	 *
	 * @param fauthId
	 * 
	 */
	public void openFacility(String fauthId) {
		requireNonNull(fauthId);
		if (uri_ != null) {
			selenium_.getDriver().get(UriUtils.getUriWithQuery(uri_, "f=" + fauthId).toString());
			waitForReady();
		} else {
			throw new UnsupportedOperationException("Page URL is not specified.");
		}
	}

	/**
	 * Scroll To Section
	 *
	 * @param section
	 * 
	 */
	public void scrollToSection(FacilitySection section) {

		selenium_.scrollIntoView(selenium_.findElementByCss(getSectionSelector(section)));
	}

	/**
	 * get section displayed
	 *
	 * @param section
	 * 
	 */
	public boolean grabSectionDisplayed(FacilitySection section) {
		return selenium_.grabElementVisibleByCss(getSectionSelector(section));
	}

	/**
	 * get section active data block count
	 *
	 * @param section
	 * @param active
	 * 
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
	 * check data block active or not *
	 * 
	 * @param section
	 * @param index
	 * 
	 */
	public boolean grabDataBlockActive(FacilitySection section, int index) {
		By locator = By.cssSelector(getDataBlockHeaderActiveSelector(section, index));
		return selenium_.searchElement(locator) != null;
	}

	/**
	 * get data block header active selector *
	 * 
	 * @param section
	 * @param index
	 * 
	 */

	protected String getDataBlockHeaderActiveSelector(FacilitySection section, int index) {
		return getDataBlockHeaderSelector(section, index) + " > div.ui-panel-actions > span > img[title='Active']";
	}

	protected LinkedHashMap<String, String> grabCivicAddressBlockContent(LinkedHashMap<String, String> dataMap,
			List<WebElement> dataRowElementList) {
		if (!dataRowElementList.isEmpty()) {
			selenium_.scrollIntoView(dataRowElementList.getFirst());
		}
		for (WebElement dataRow : dataRowElementList) {
			List<WebElement> dataEntryList = dataRow.findElements(By.cssSelector("td" + TABLE_ROWS_SELECTOR));
			int dataColumnCount = dataEntryList.size();
			if (dataColumnCount == 2) {
				addFieldDataMap(dataMap, dataEntryList, 0);
			} else if (dataColumnCount == 5 || dataColumnCount == 6) {
				addFieldDataMap(dataMap, dataEntryList.get(0).findElements(By.cssSelector("td" + TABLE_ROWS_SELECTOR)),
						0);
				if (dataColumnCount == 5) {
					addFieldDataMap(dataMap,
							dataEntryList.get(0).findElements(By.cssSelector("td" + TABLE_ROWS_SELECTOR)), 2);
				}
			} else if (dataColumnCount == 4) {
				addFieldDataMapSplit(dataMap,
						dataEntryList.get(0).findElements(By.cssSelector("td" + TABLE_ROWS_SELECTOR)), 0);
			} else {

				String msg = String.format("Invalid data row (%d: %s).", dataColumnCount, dataRow.getText());
				throw new IllegalStateException(msg);
			}
		}
		return dataMap;
	}

	/**
	 * Gets the content from a Civic Addresses data block. All other facility
	 * sections are structured differently, so grabDataBlockContent must be used
	 * instead for any other facility section.
	 *
	 * @param index the index of data block to get content from
	 * @return a hash map mapping civic address data fields (String) to their
	 *         associated values (String)
	 */
	public LinkedHashMap<String, String> grabCivicAddressBlockContent(int index) {
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
		expandDataBlock(FacilitySection.CIVIC_ADDRESSES, index, true);
		List<WebElement> dataRowElementList = selenium_
				.findElements(By.cssSelector(getDataBlockContentSelector(FacilitySection.CIVIC_ADDRESSES, index)
						+ " > div.ui-outputpanel" + TABLE_ROWS_SELECTOR));
		dataMap = grabCivicAddressBlockContent(dataMap, dataRowElementList);
		expandDataBlock(FacilitySection.CIVIC_ADDRESSES, index, false);
		return dataMap;

	}

	/**
	 * grab OrgRelationships Block Content
	 *
	 * @param index
	 * @return
	 */
	public LinkedHashMap<String, String> grabOrgRelationshipsBlockContent(int index) {
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
		expandDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, index, true);
		String cssString = getDataBlockContentSelector(FacilitySection.ORGANIZATION_RELATIONSHIPS, index) + " > form"
				+ TABLE_ROWS_SELECTOR;
		List<WebElement> dataRowElementList = selenium_.findElements(
				By.cssSelector(getDataBlockContentSelector(FacilitySection.ORGANIZATION_RELATIONSHIPS, index)
						+ " > form" + TABLE_ROWS_SELECTOR));
		return grabBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, index, dataMap, dataRowElementList);
	}

	/**
	 * Gets the content from a Civic Addresses data block. All other facility
	 * sections are structured differently, so grabDataBlockContent must be used
	 * instead for any other facility section.
	 *
	 * @return a hash map mapping civic address data fields (String) to their
	 *         associated values (String)
	 */
	@SuppressWarnings("fallthrough")
	public LinkedHashMap<String, String> grabCivicAddressBlockContent() {
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
		expandDataBlock(FacilitySection.CIVIC_ADDRESSES, 0, true);
		List<WebElement> dataRowElementList = selenium_
				.findElements(By.cssSelector(getDataBlockContentSelector(FacilitySection.CIVIC_ADDRESSES, 0)
						+ " > div.ui-outputpanel" + TABLE_ROWS_SELECTOR));
		By tableSelect = By.cssSelector("td" + TABLE_ROWS_SELECTOR);

		if (!dataRowElementList.isEmpty()) {
			selenium_.scrollIntoView(dataRowElementList.getFirst());
		}
		for (WebElement dataRow : dataRowElementList) {
			List<WebElement> dataEntryList = dataRow.findElements(tableSelect);
			int dataColumnCount = dataEntryList.size();
			if (dataColumnCount == 2)
				addFieldDataMap(dataMap, dataEntryList, 0);
			else {
				List<WebElement> extraFields = dataEntryList.getFirst().findElements(tableSelect);
				switch (dataColumnCount) {
				case 4:
					dataMap.put(
							formatDataKey(extraFields.getFirst().findElement(By.cssSelector("div.frmDialogLbl > label"))
									.getText()),
							extraFields.getFirst().findElement(By.cssSelector("div[role] > label")).getText());
					addFieldDataMap(dataMap, extraFields, 1);
					break;
				case 5:
					addFieldDataMap(dataMap, extraFields, 2);
					// fall through
				case 6:
					addFieldDataMap(dataMap, extraFields, 0);
					break;
				default:
					String msg = String.format("Invalid data row (%d: %s).", dataColumnCount, dataRow.getText());
					throw new IllegalStateException(msg);
				}
			}

		}
		return dataMap;
	}

	/**
	 * Determines the number of data blocks in a specific facility section.
	 *
	 * @param section the facility section to find the number of records for.
	 * @return an integer of the number of records for a particular facility
	 *         section.
	 */
	public int grabDataBlockCount(FacilitySection section) {
		List<WebElement> dataBlockList = selenium_.findElements(By.cssSelector(getDataBlocksSelector(section)));
		return dataBlockList.size();
	}

	/**
	 * Opens an organization (provider) page in the Organization Relationships
	 * section.
	 *
	 * @param dataBlockIndex the index of organization relationship to open and
	 *                       click into
	 */
	public void openOrg(int dataBlockIndex) {
		String linkSelector = getDataBlockContentSelector(FacilitySection.ORGANIZATION_RELATIONSHIPS, dataBlockIndex);
		linkSelector += " > form" + TABLE_ROWS_SELECTOR + " > td > a";

		selenium_.scrollIntoView(selenium_.findElement(By.cssSelector(linkSelector)));
		selenium_.findElement(By.cssSelector(linkSelector)).click();
	}

}