package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import java.util.Collection;
import java.util.List;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * A page object representing the Search Provider page.
 */
public class SearchProviderPage
extends BasicWebPage
{
    private static final String ACTIVE_UI_STATE_CLASS_NAME = "ui-state-active";

    private static final String CRITERIA_XPATH =
            getSearchSectionXPath("Search by Criteria");

    private static final String IDENTIFIER_XPATH =
            getSearchSectionXPath("Search by Identifier");

    private static final String INCLUDE_HISTORY_CHECKBOX_CSS =
            "form#includeHistoryForm div.ui-chkbox-box";

    private static final String ORGANIZATION_XPATH =
            getSearchSectionXPath("Search for Organization");

    private static final String REGISTRY_IDENTIFIER_XPATH =
            getSearchSectionXPath("Search by Registry Identifier");
    
    private static final String ALERT_ERROR_CSS = "";

    /**
     * Initializes page object and changes selenium's main locator to the Search Provider heading.
     *
     * @param selenium the current SeleniumSession
     */
    public SearchProviderPage(SeleniumSession selenium)
    {
        super(selenium,
              By.xpath("//div[@id='content']//h2[contains(text(),'Search Provider')]"),
              "Search Provider");
    }

    /**
     * Enables or disables include history by checking or unchecking the corresponding checkbox.
     *
     * @param enable
     *        {@code true} to enable include history,
     *        {@code false} to disable include history
     *
     * @return {@code true} if the state of the include history checkbox is being changed,
     *         {@code false} otherwise
     */
    public boolean enableIncludeHistory(boolean enable)
    {
        boolean changed;
        if (grabIncludeHistoryEnabled() != enable)
        {
            selenium_.clickByCss(INCLUDE_HISTORY_CHECKBOX_CSS);
            waitForIncludeHistoryEnabled(enable);
            changed = true;
        }
        else
        {
            changed = false;
        }
        return changed;
    }

    /**
     * Expands or collapses the Search by Criteria section.
     *
     * @param expand whether the section is to be expanded (true) or collapsed (false)
     * @return the search by criteria fragment class for the section
     */
    public SearchProviderCriteriaFragment expandSearchCriteria(boolean expand)
    {
        expandSearchSection(CRITERIA_XPATH, expand);
        SearchProviderCriteriaFragment fragment =
                new SearchProviderCriteriaFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Expands or collapses the Search by Identifier section.
     *
     * @param expand whether the section is to be expanded (true) or collapsed (false)
     * @return       the search by identifier fragment class for the section
     */
    public SearchProviderIdFragment expandSearchIdentifier(boolean expand)
    {
        expandSearchSection(IDENTIFIER_XPATH, expand);
        SearchProviderIdFragment fragment =
                new SearchProviderIdFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Expands or collapses the Search for Organization section.
     *
     * @param expand whether the section is to be expanded (true) or collapsed (false)
     * @return       the search for organization fragment class for the section
     */
    public SearchProviderOrganizationFragment expandSearchOrganization(boolean expand)
    {
        expandSearchSection(ORGANIZATION_XPATH, expand);
        SearchProviderOrganizationFragment fragment =
                new SearchProviderOrganizationFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Expands or collapses the Search by Registry Identifier section.
     *
     * @param expand whether the section is to be expanded (true) or collapsed (false)
     * @return       the search by registry identifier fragment class for the section
     */
    public SearchProviderRegistryIdFragment expandSearchRegistryIdentifier(boolean expand)
    {
        expandSearchSection(REGISTRY_IDENTIFIER_XPATH, expand);
        SearchProviderRegistryIdFragment fragment =
                new SearchProviderRegistryIdFragment(selenium_);
        waitForSearchSectionFragmentExpanded(fragment, expand);
        return fragment;
    }

    /**
     * Grabs the state of the include history checkbox.
     *
     * @return {@code true} if include history is enabled
     *         (i.e. the corresponding checkbox is checked),
     *         {@code false} otherwise
     */
    public boolean grabIncludeHistoryEnabled()
    {
        return SeleniumUtils
                .grabElementClassSet(selenium_.findElementByCss(INCLUDE_HISTORY_CHECKBOX_CSS))
                .contains(ACTIVE_UI_STATE_CLASS_NAME);
    }

    /**
     * Determines whether the Search by Criteria section is expanded or not.
     *
     * @return boolean indicating whether the Search by Criteria section is expanded (true) or not (false)
     */
    public boolean grabSearchCriteriaExpanded()
    {
        return grabSearchSectionExpanded(CRITERIA_XPATH);
    }

    /**
     * Determines whether the Search by Criteria section is visible or not.
     *
     * @return boolean indicating whether the Search by Criteria section is visible (true) or invisible (false)
     */
    public boolean grabSearchCriteriaVisible()
    {
        return grabSearchSectionVisible(CRITERIA_XPATH);
    }

    /**
     * Determines whether the Search by Identifier section is expanded or not.
     *
     * @return boolean indicating whether the Search by Identifier section is expanded (true) or not (false)
     */
    public boolean grabSearchIdentifierExpanded()
    {
        return grabSearchSectionExpanded(IDENTIFIER_XPATH);
    }

    /**
     * Determines whether the Search by Identifier section is visible or not.
     *
     * @return boolean indicating whether the Search by Identifier section is visible (true) or invisible (false)
     */
    public boolean grabSearchIdentifierVisible()
    {
        return grabSearchSectionVisible(IDENTIFIER_XPATH);
    }

    /**
     * Determines whether the Search for Organization section is expanded or not.
     *
     * @return boolean indicating whether the Search for Organization section is expanded (true) or not (false)
     */
    public boolean grabSearchOrganizationExpanded()
    {
        return grabSearchSectionExpanded(ORGANIZATION_XPATH);
    }

    /**
     * Determines whether the Search for Organization section is visible or not.
     *
     * @return boolean indicating whether the Search for Organization section is visible (true) or invisible (false)
     */
    public boolean grabSearchOrganizationVisible()
    {
        return grabSearchSectionVisible(ORGANIZATION_XPATH);
    }

    /**
     * Determines whether the Search by Registry Identifier section is expanded or not.
     *
     * @return boolean indicating whether the Search by Registry Identifier section is expanded (true) or not (false)
     */
    public boolean grabSearchRegistryIdentifierExpanded()
    {
        return grabSearchSectionExpanded(REGISTRY_IDENTIFIER_XPATH);
    }

    /**
     * Determines whether the Search by Registry Identifier section is visible or not.
     *
     * @return boolean indicating whether the Search by Registry Identifier section is visible (true) or invisible (false)
     */
    public boolean grabSearchRegistryIdentifierVisible()
    {
        return grabSearchSectionVisible(REGISTRY_IDENTIFIER_XPATH);
    }

   
    /**
     * Submits a search by criteria query with the specified parameters.
     *
     * @param roleTypePrefix            the first few characters of the role type to select
     * @param firstName                 the first name to fill in
     * @param lastName                  the last name to fill in
     * @param genderPrefix              the first few characters of the gender to select
     * @param city                      the city to fill in
     * @param statusCodePrefix          the first few characters of the status code to select
     * @param statusReasonCodePrefix    the first few characters of the status reason code to select
     * @return                          the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchByCriteria(
            String roleTypePrefix,
            String firstName,
            String lastName,
            String genderPrefix,
            String city,
            String statusCodePrefix,
            String statusReasonCodePrefix)
    {
        return searchByCriteria(
                roleTypePrefix,
                firstName,
                lastName,
                genderPrefix,
                city,
                statusCodePrefix,
                statusReasonCodePrefix,
                null,
                null);
    }

    /**
     * Submits a search by criteria query with the specified parameters.
     *
     * @param roleTypePrefix                the first few characters of the role type to select
     * @param firstName                     the first name to fill in
     * @param lastName                      the last name to fill in
     * @param genderPrefix                  the first few characters of the gender to select
     * @param city                          the city to fill in
     * @param statusCodePrefix              the first few characters of the status code to select
     * @param statusReasonCodePrefix        the first few characters of the status reason code to select
     * @param expertisePrefixCollection     a collection of expertise prefixes to use to select expertise options
     * @param languagePrefixCollection      a collection of language prefixes to use to select language options
     * @return                              the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchByCriteria(
            String roleTypePrefix,
            String firstName,
            String lastName,
            String genderPrefix,
            String city,
            String statusCodePrefix,
            String statusReasonCodePrefix,
            Collection<String> expertisePrefixCollection,
            Collection<String> languagePrefixCollection)
    {
        SearchProviderCriteriaFragment search = expandSearchCriteria(true);
		if (roleTypePrefix != null) {
			search.selectRoleType(roleTypePrefix);
		} 
		
		if (firstName != null) {
			search.fillFirstName(firstName);
		} else
			search.clearFirstName();
		if (lastName != null) {
			search.fillLastName(lastName);
		} else
			search.clearLastName();
		if (genderPrefix != null) {
			search.selectGender(genderPrefix);
		} 
		
		if (city != null) {
			search.fillCity(city);
		} else
			search.clearCity();
		if (statusCodePrefix != null) {
			search.selectStatusCode(statusCodePrefix);
		}
		if (statusReasonCodePrefix != null) {
			search.selectStatusReasonCode(statusReasonCodePrefix);
		}
        if (expertisePrefixCollection != null)
        {
            for (String expertisePrefix : expertisePrefixCollection)
            {
                if (expertisePrefix != null)
                {
                    search.selectExpertise(expertisePrefix);
                }
            }
        }
        if (languagePrefixCollection != null)
        {
            for (String languagePrefix : languagePrefixCollection)
            {
                if (languagePrefix != null)
                {
                    search.selectLanguage(languagePrefix);
                }
            }
        }
        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }

    /**
     * Submits a search by identifier query with the specified parameters.
     *
     * @param identifierTypePrefix the first few characters of the identifier type to select
     * @param providerId           the provider ID to fill in
     * @return                     the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchByIdentifier(
            String identifierTypePrefix,
            String providerId)
    {
        SearchProviderIdFragment search = expandSearchIdentifier(true);
        search.selectIdentifierType(identifierTypePrefix);
        if(providerId!=null)
        	search.fillProviderId(providerId);
        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }
    
    
    /**
     * Submits a search by identifier query with the specified parameters.
     *
     * @param identifierTypePrefix the first few characters of the identifier type to select
     * @param providerId           the provider ID to fill in
     * @param expectResultFragment whether to expect a search results fragment or not
     * @return                     the search provider results fragment for the associated search results (or null)
     */
    public SearchProviderResultsFragment searchByIdentifier(
            String identifierTypePrefix,
            String providerId,boolean expectResultFragment)
    {
		SearchProviderIdFragment search = expandSearchIdentifier(true);
		search.selectIdentifierType(identifierTypePrefix);
		if (providerId != null)
			search.fillProviderId(providerId);
		else
			search.clearProviderId();
		search.clickSearchButton();
		if (expectResultFragment)
			return waitForSearchProviderResultsFragment();
		else
			return null;
    }

    /**
     * Submits a search by registry identifier query with the specified parameters.
     *
     * @param registryIdentifierTypePrefix the first few characters of the registry identifier type to select
     * @param registryId                   the registry ID to fill in
     * @return                             the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId)
    {
        return searchByRegistryIdentifier(registryIdentifierTypePrefix, registryId, null);
    }

    /**
     * Submits a search by registry identifier query with the specified parameters.
     *
     * @param registryIdentifierTypePrefix the first few characters of the registry identifier type to select
     * @param registryId                   the registry ID to fill in
     * @param expectResultsFragment        whether to expect a results fragment or not
     * @return                             the search provider results fragment for the associated search results (or null)
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId,boolean expectResultsFragment)
    {
        return searchByRegistryIdentifier(registryIdentifierTypePrefix, registryId, null,expectResultsFragment);
    }

    /**
     * Submits a search by registry identifier query with the specified parameters.
     *
     * @param registryIdentifierTypePrefix the first few characters of the registry identifier type to select
     * @param registryId                   the registry ID to fill in
     * @param registryIdSuffix             the registry ID suffix to fill in
     * @return                             the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId,
            String registryIdSuffix)
    {
        SearchProviderRegistryIdFragment search = expandSearchRegistryIdentifier(true);
        search.selectRegistryIdentifierType(registryIdentifierTypePrefix);
		if (registryId != null)
			search.fillRegistryId(registryId);
		else
			search.clearRegistryId();
		if (registryIdSuffix != null) {
			search.fillRegistryIdSuffix(registryIdSuffix);
		}

		search.clickSearchButton();
		return waitForSearchProviderResultsFragment();
    }
    
    /**
     * Submits a search by registry identifier query with the specified parameters.
     *
     * @param registryIdentifierTypePrefix the first few characters of the registry identifier type to select
     * @param registryId                   the registry ID to fill in
     * @param registryIdSuffix             the registry ID suffix to fill in
     * @param expectResultsFragment        whether to expect a results fragment or not
     * @return                             the search provider results fragment for the associated search results (or null)
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId,
            String registryIdSuffix, boolean expectResultsFragment)
	{
		SearchProviderRegistryIdFragment search = expandSearchRegistryIdentifier(true);
		search.selectRegistryIdentifierType(registryIdentifierTypePrefix);
		if (registryId != null)
			search.fillRegistryId(registryId);
		else
			search.clearRegistryId();
		if (registryIdSuffix != null) {
			search.fillRegistryIdSuffix(registryIdSuffix);
		}
        search.clickSearchButton();
        if(expectResultsFragment)
        return waitForSearchProviderResultsFragment();
        else return null;
    }

    /**
     * Submits a search for organization query with the specified parameters.
     *
     * @param roleTypePrefix the first few characters of the role type to select
     * @param name           the name to fill in
     * @param description    the description to fill in
     * @param addressLine1   the address line 1 to fill in
     * @param city           the city to fill in
     * @return               the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchForOrganization(
            String roleTypePrefix,
            String name,
            String description,
            String addressLine1,
            String city)
    {
        SearchProviderOrganizationFragment search = expandSearchOrganization(true);
        if (roleTypePrefix != null)
        {
            WebElement orgPanel = selenium_.findElement(By.id("accordian:searchByOrganizationForm:provider_identifier_panel"));
            String currentRoleType = search.getRoleTypeMenu().grabSelectedItem();
            if (!currentRoleType.equals(search.selectRoleType(roleTypePrefix)))
                selenium_.waitUntil(ExpectedConditions.stalenessOf(orgPanel));
        }
        
        return fillCommonOrganizationFields(search, name, description, city, addressLine1);
    }

    /**
     * Submits a search for HDS organization query with the specified parameters.
     *
     * @param hdsType       the first few characters of the HDS type to select
     * @param name          the name to fill in
     * @param description   the description to fill in
     * @param city          the city to fill in
     * @param addressLine1  the address line 1 to fill in
     * @return              the search provider results fragment for the associated search results
     */
    public SearchProviderResultsFragment searchHDSOrganization(
            String hdsType,
            String name,
            String description,
            String city,
            String addressLine1) {
    	 SearchProviderOrganizationFragment search = expandSearchOrganization(true);
    	 
        
    	 search.selectRoleType("HDS");
    	 WebElement visibleElement = selenium_
 				.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("accordian:searchByOrganizationForm:hds")));
    	// WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accordian\\:searchByOrganizationForm\\:hds")));

    
    	if(hdsType!=null)
         search.selectHdsType(hdsType);

        return fillCommonOrganizationFields(search, name, description, city, addressLine1);
    }

    public SearchProviderResultsFragment fillCommonOrganizationFields(SearchProviderOrganizationFragment search,
                                                                      String name,
                                                                      String description,
                                                                      String city,
                                                                      String addressLine1) {
        if (name != null) {
            search.fillName(name);
        } else
            search.clearName();
        if (description != null) {
            search.fillDescription(description);
        } else
            search.clearDescription();

        if (addressLine1 != null) {
            search.fillAddressLine1(addressLine1);
        } else
            search.clearAddressLine1();
        if (city != null) {
            search.fillCity(city);
        } else
            search.clearCity();

        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }

    /**
     * Waits for and returns the alert messages fragment.
     *
     * @return the alert messages fragment
     */
    public AlertMessagesFragment waitForAlertMessagesFragment()
    {
        AlertMessagesFragment fragment = new AlertMessagesFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    /**
     * Waits for the include history checkbox to be enabled or disabled.
     *
     * @param enabled {@code true}  to wait for the include history checkbox to be enabled,
     *                {@code false} to wait for it to be disabled
     */
    public void waitForIncludeHistoryEnabled(boolean enabled)
    {
        if (enabled)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector(INCLUDE_HISTORY_CHECKBOX_CSS), ACTIVE_UI_STATE_CLASS_NAME));
        }
        else
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector(INCLUDE_HISTORY_CHECKBOX_CSS), ACTIVE_UI_STATE_CLASS_NAME));
        }
    }

    private void expandSearchSection(String xpath, boolean expand)
    {
        if (grabSearchSectionExpanded(xpath) != expand)
        {
            By locator = By.xpath(xpath);
            selenium_.findElement(locator).click();
            waitForSearchSectionTitleExpanded(locator, expand);
        }
    }

    private static String getSearchSectionXPath(String title)
    {
        return "//div[@id='accordian']" +
                "/div[contains(@class,'ui-accordion-header') and contains(text(),'" +
                title +
                "')]";
    }

    private boolean grabSearchSectionExpanded(String xpath)
    {
        return SeleniumUtils.grabElementClassSet(selenium_.findElement(By.xpath(xpath)))
                .contains(ACTIVE_UI_STATE_CLASS_NAME);
    }

    private boolean grabSearchSectionVisible(String xpath)
    {
        return selenium_.grabElementVisible(By.xpath(xpath));
    }

    private SearchProviderResultsFragment waitForSearchProviderResultsFragment()
    {
        SearchProviderResultsFragment fragment = new SearchProviderResultsFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    private static void waitForSearchSectionFragmentExpanded(
            SearchSectionFragment fragment, boolean expand)
    {
        if (expand)
        {
            fragment.waitForReady();
        }
        else
        {
            fragment.waitForInvisible();
        }
    }

    private void waitForSearchSectionTitleExpanded(By locator, boolean expanded)
    {
        if (expanded)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    locator, ACTIVE_UI_STATE_CLASS_NAME));
        }
        else
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    locator, ACTIVE_UI_STATE_CLASS_NAME));
        }
    }

    /**
     * Grabs all error messages displayed on the page.
     *
     * @return a string of concatenated error messages
     */
	public String grabPageErrorMessage() {

		String alertMsgCss = "span.ui-messages-error-summary";
		StringBuilder msgDisplay = new StringBuilder();
		List<WebElement> alertMsgList = selenium_.findElements(By.cssSelector(alertMsgCss));
		for (WebElement alertMsg : alertMsgList) {
			msgDisplay.append(alertMsg.getText());
		}
		return msgDisplay.toString();
	}

    /**
     * Grabs all warning messages displayed on the page.
     *
     * @return a string of concatenated warning messages
     */
	public String grabWarningErrorMessage() {

		String alertMsgCss = "span.ui-messages-warn-summary";
		StringBuilder msgDisplay = new StringBuilder();
		List<WebElement> alertMsgList = selenium_.findElements(By.cssSelector(alertMsgCss));
		for (WebElement alertMsg : alertMsgList) {
			msgDisplay.append(alertMsg.getText());
		}
		return msgDisplay.toString();
	}

	/**
	 * clear All Expertise
	 */
	public void clearAllExpertise() {
		SearchProviderCriteriaFragment search = expandSearchCriteria(true);
		search.clearAllExpertise();
	}

	/**
     * Clears the expertise selections matching the provided prefixes.
     *
	 * @param expertisePrefixCollection a collection of expertises to clear by prefix
	 */
	public void clearExpertise(List<String> expertisePrefixCollection) {
		SearchProviderCriteriaFragment search = expandSearchCriteria(true);
		if (expertisePrefixCollection != null) {
			for (String expertisePrefix : expertisePrefixCollection) {
				if (expertisePrefix != null) {
					search.clearExpertise(expertisePrefix);
				}
			}
		}

	}

	/**
	 * clear Gender
	 */
	public void clearGender() {
		SearchProviderCriteriaFragment search = expandSearchCriteria(true);
		search.clearGender();
	}

	/**
	 * clear Role Type
	 */
	public void clearRoleType() {
		SearchProviderCriteriaFragment search = expandSearchCriteria(true);
		search.clearRoleType();
	}

}
