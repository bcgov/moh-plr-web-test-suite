package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;

/**
 * A page object class for the Search Facility page.
 */
public class SearchFacilityPage extends BasicWebPage
{
    private static final String ACTIVE_UI_STATE_CLASS_NAME = "ui-state-active";

    private static final String IDENTIFIER_XPATH = getSearchSectionXPath("Search by Identifier");

    private static final String CRITERIA_XPATH = getSearchSectionXPath("Search by Criteria");

    /**
     * Initializes page object and changes selenium's main locator to the Search Facility heading
     *
     * @param selenium  the current SeleniumSession
     */
    public SearchFacilityPage(SeleniumSession selenium) {
        super(selenium, By.xpath("//div[@id='content']//h2[contains(text(),'Search Facility')]"),
                "Search Facility");
    }

    /**
     * creates an xpath for a search section (identifier or criteria)
     *
     * @param title     the title of the search section
     * @return  a string xpath to find the search section
     */
    private static String getSearchSectionXPath(String title)
    {
        return new StringBuilder().append("//div[@id='accordian']")
                .append("/div[contains(@class,'ui-accordion-header') and contains(text(), '")
                .append(title)
                .append("')]")
                .toString();
    }

    /**
     * Determines whether the identifier section is expanded or not
     *
     * @return  boolean of if Search by Identifier is expanded (true) or not (false)
     */
    public boolean grabIdentifierSectionExpanded()
    {
        return SeleniumUtils.grabElementClassSet(selenium_.findElement(By.xpath(IDENTIFIER_XPATH)))
                .contains(ACTIVE_UI_STATE_CLASS_NAME);
    }

    /**
     * Determines whether a search section is expanded or not
     *
     * @param xpath xpath to follow to find the section (identifier/criteria)
     * @return  boolean of if the search section (identifier/criteria) is expanded (true) or not (false)
     */
    private boolean grabSearchSectionExpanded(String xpath) {
        return SeleniumUtils.grabElementClassSet(selenium_.findElement(By.xpath(xpath)))
                .contains(ACTIVE_UI_STATE_CLASS_NAME);
    }

    /**
     * Waits for a search section (identifier/criteria) to expand/close
     *
     * @param locator   the locator of the identifier/criteria section
     * @param expanded  whether the search section is currently expanded (true) or closed (false)
     */
    private void waitForSearchSectionTitleExpanded(By locator, boolean expanded)
    {
        if (expanded)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(locator, ACTIVE_UI_STATE_CLASS_NAME));
        }
        else
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(locator, ACTIVE_UI_STATE_CLASS_NAME));
        }
    }

    /**
     * Expands/closes the search section specified by xpath
     *
     * @param xpath     the xpath leading to the search section (identifier/criteria)
     * @param expand    whether the search section is to be expanded (true) or closed (false)
     */
    private void expandSearchSection(String xpath, boolean expand)
    {
        if (grabSearchSectionExpanded(xpath) != expand)
        {
            By locator = By.xpath(xpath);
            selenium_.findElement(locator).click();
            waitForSearchSectionTitleExpanded(locator, expand);
        }
    }

    /**
     * Waits for the search section fragment to expand/close
     *
     * @param fragment  the SearchSectionFragment
     * @param expanded  whether the fragment is currently expanding (true) or closing (false)
     */
    private static void waitForSearchSectionFragmentExpanded(SearchSectionFragment fragment, boolean expanded)
    {
        if (expanded)
        {
            fragment.waitForReady();
        }
        else
        {
            fragment.waitForInvisible();
        }
    }

    /**
     * Expands/closes the Search by Identifier section
     *
     * @param expand    whether the section is to be expanded (true) or closed (false)
     * @return  the search by identifier fragment class for the section
     */
    public SearchFacilityIdFragment expandSearchIdentifier(boolean expand)
    {
        expandSearchSection(IDENTIFIER_XPATH, expand);
        SearchFacilityIdFragment fragment = new SearchFacilityIdFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Waits for the search results to appear
     *
     * @return  the search results fragment class once the results appear
     */
    private SearchFacilityResultsFragment waitForSearchFacilityResultsFragment()
    {
        SearchFacilityResultsFragment fragment = new SearchFacilityResultsFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    /**
     * Submits a query in the Search by Identifier section after filling out each field
     *
     * @param identifierTypePrefix  the first few characters to match when selecting an Identifier Type option
     * @param facilityID    the string to fill the Facility Identifier field with.
     * @param expectedError     whether the expected output of the query is an error (true) or not (false)
     * @return  the search results fragment for the associated search results (if they exist)
     */
    public SearchFacilityResultsFragment searchByIdentifier(String identifierTypePrefix, String facilityID, boolean expectedError)
    {
        SearchFacilityIdFragment search = expandSearchIdentifier(true);
        search.selectIdentifierType(identifierTypePrefix);
        search.fillFacilityId(facilityID);
        search.clickSearchButton();
        if (expectedError)
        {
            return new SearchFacilityResultsFragment(selenium_);
        }
        else
        {
            return waitForSearchFacilityResultsFragment();
        }
    }

    /**
     * Waits for the error/warning messages to appear
     *
     * @return  the alert message fragment class once the message appears
     */
    public AlertMessagesFragment waitForAlertMessagesFragment()
    {
        AlertMessagesFragment fragment = new AlertMessagesFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    /**
     * Expands/closes the Search by Criteria section
     *
     * @param expand    whether the section is to be expanded (true) or closed (false)
     * @return  the search by criteria fragment class for the section
     */
    public SearchFacilityCriteriaFragment expandSearchCriteria(boolean expand)
    {
        expandSearchSection(CRITERIA_XPATH, expand);
        SearchFacilityCriteriaFragment fragment = new SearchFacilityCriteriaFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Submits a query in the Search by Criteria section after filling out each field
     *
     *
     * @param facilityName                  the string to fill the Facility Name field with
     * @param civicAddress                  the string to fill the Civic Address Line 1 field with
     * @param otherAddress                  the string to fill the Other Address Line 1 field with
     * @param cityField                     the string to fill the City field with (with autocomplete)
     * @param cityPrefix                    the first few characters to match when selecting an autocompleted City option.
     *                                      leave null to fill the City field directly through cityField.
     * @param facilityTypePrefix            the first few characters to match when selecting a Facility Type menu option
     * @param serviceDeliveryAreaField      the string to fill the Service Delivery Area field with directly
     * @param serviceDeliveryAreaPrefix     the first few characters to match when selecting an autocompleted Service Delivery Area option.
     *                                      leave null to fill the Service Delivery Area field directly through serviceDeliveryAreaField.
     * @param expectedError                 whether the expected output of the query is an error (true) or not (false)
     * @return                              the search results fragment for the associated search results (if they exist)
     */
    public SearchFacilityResultsFragment searchByCriteria(
            String facilityName,
            String civicAddress,
            String otherAddress,
            String cityField,
            String cityPrefix,
            String facilityTypePrefix,
            String serviceDeliveryAreaField,
            String serviceDeliveryAreaPrefix,
            boolean expectedError)
    {
        SearchFacilityCriteriaFragment search = expandSearchCriteria(true);

        if (facilityName != null) search.fillFacilityName(facilityName);
        if (civicAddress != null) search.fillCivicAddress(civicAddress);
        if (otherAddress != null) search.fillOtherAddress(otherAddress);
        if (cityField != null) search.fillCity(cityField, cityPrefix);
        if (facilityTypePrefix != null) search.selectFacilityType(facilityTypePrefix);
        if (serviceDeliveryAreaField != null) search.fillServiceDeliveryArea(serviceDeliveryAreaField, serviceDeliveryAreaPrefix);
        search.clickSearchButton();
        if (expectedError)
        {
            return new SearchFacilityResultsFragment(selenium_);
        }
        else
        {
            return waitForSearchFacilityResultsFragment();
        }
    }
}
