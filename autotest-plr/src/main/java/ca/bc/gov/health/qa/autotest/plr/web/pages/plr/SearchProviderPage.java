package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import java.util.Collection;

import org.openqa.selenium.By;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
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

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
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
     * TODO (AZ) - doc
     *
     * @param expand ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param expand ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param expand ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param expand ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchCriteriaExpanded()
    {
        return grabSearchSectionExpanded(CRITERIA_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchCriteriaVisible()
    {
        return grabSearchSectionVisible(CRITERIA_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchIdentifierExpanded()
    {
        return grabSearchSectionExpanded(IDENTIFIER_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchIdentifierVisible()
    {
        return grabSearchSectionVisible(IDENTIFIER_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchOrganizationExpanded()
    {
        return grabSearchSectionExpanded(ORGANIZATION_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchOrganizationVisible()
    {
        return grabSearchSectionVisible(ORGANIZATION_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchRegistryIdentifierExpanded()
    {
        return grabSearchSectionExpanded(REGISTRY_IDENTIFIER_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabSearchRegistryIdentifierVisible()
    {
        return grabSearchSectionVisible(REGISTRY_IDENTIFIER_XPATH);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param roleTypePrefix
     *        ???
     *
     * @param firstName
     *        ???
     *
     * @param lastName
     *        ???
     *
     * @param genderPrefix
     *        ???
     *
     * @param city
     *        ???
     *
     * @param statusCodePrefix
     *        ???
     *
     * @param statusReasonCodePrefix
     *        ???
     *
     * @return ???
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
     * TODO (AZ) - doc (parameters are optional)
     *
     * @param roleTypePrefix
     *        ???
     *
     * @param firstName
     *        ???
     *
     * @param lastName
     *        ???
     *
     * @param genderPrefix
     *        ???
     *
     * @param city
     *        ???
     *
     * @param statusCodePrefix
     *        ???
     *
     * @param statusReasonCodePrefix
     *        ???
     *
     * @param expertisePrefixCollection
     *        ???
     *
     * @param languagePrefixCollection
     *        ???
     *
     * @return ???
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
        if (roleTypePrefix != null)
        {
            search.selectRoleType(roleTypePrefix);
        }
        if (firstName != null)
        {
            search.fillFirstName(firstName);
        }
        if (lastName != null)
        {
            search.fillLastName(lastName);
        }
        if (genderPrefix != null)
        {
            search.selectGender(genderPrefix);
        }
        if (city != null)
        {
            search.fillCity(city);
        }
        if (statusCodePrefix != null)
        {
            search.selectStatusCode(statusCodePrefix);
        }
        if (statusReasonCodePrefix != null)
        {
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
     * TODO (AZ) - doc
     *
     * @param identifierTypePrefix
     *        ???
     *
     * @param providerId
     *        ???
     *
     * @return ???
     */
    public SearchProviderResultsFragment searchByIdentifier(
            String identifierTypePrefix,
            String providerId)
    {
        SearchProviderIdFragment search = expandSearchIdentifier(true);
        search.selectIdentifierType(identifierTypePrefix);
        search.fillProviderId(providerId);
        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param registryIdentifierTypePrefix
     *        ???
     *
     * @param registryId
     *        ???
     *
     * @return ???
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId)
    {
        return searchByRegistryIdentifier(registryIdentifierTypePrefix, registryId, null);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param registryIdentifierTypePrefix
     *        ???
     *
     * @param registryId
     *        ???
     *
     * @param registryIdSuffix
     *        ??? (optional)
     *
     * @return ???
     */
    public SearchProviderResultsFragment searchByRegistryIdentifier(
            String registryIdentifierTypePrefix,
            String registryId,
            String registryIdSuffix)
    {
        SearchProviderRegistryIdFragment search = expandSearchRegistryIdentifier(true);
        search.selectRegistryIdentifierType(registryIdentifierTypePrefix);
        search.fillRegistryId(registryId);
        if (registryIdSuffix != null)
        {
            search.fillRegistryIdSuffix(registryIdSuffix);
        }
        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }

    /**
     * TODO (AZ) - doc (all parameters are optional)
     *
     * @param roleTypePrefix
     *        ???
     *
     * @param name
     *        ???
     *
     * @param description
     *        ???
     *
     * @param addressLine1
     *        ???
     *
     * @param city
     *        ???
     *
     * @return ???
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
            search.selectRoleType(roleTypePrefix);
        }
        if (name != null)
        {
            search.fillName(name);
        }
        if (description != null)
        {
            search.fillDescription(description);
        }
        if (addressLine1 != null)
        {
            search.fillAddressLine1(addressLine1);
        }
        if (city != null)
        {
            search.fillCity(city);
        }
        search.clickSearchButton();
        return waitForSearchProviderResultsFragment();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public AlertMessagesFragment waitForAlertMessagesFragment()
    {
        AlertMessagesFragment fragment = new AlertMessagesFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param enabled ???
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
        return new StringBuilder()
                .append("//div[@id='accordian']")
                .append("/div[contains(@class,'ui-accordion-header') and contains(text(),'")
                .append(title)
                .append("')]")
                .toString();
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
}
