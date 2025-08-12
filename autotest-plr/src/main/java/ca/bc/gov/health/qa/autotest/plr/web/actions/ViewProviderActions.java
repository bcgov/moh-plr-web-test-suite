package ca.bc.gov.health.qa.autotest.plr.web.actions;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ProviderDataFields;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class ViewProviderActions
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Pattern VALUE_CODE_PATTERN =
            Pattern.compile("^.*\\((?<code>[^()]+)\\)\\s*$");

    private final SeleniumSession selenium_;
    private final URI             uri_;
    private final UserType        userType_;

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
     *
     * @param userType
     *        ???
     */
    public ViewProviderActions(SeleniumSession selenium, URI uri, UserType userType)
    {
        selenium_ = selenium;
        uri_      = uri;
        userType_ = userType;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param pauthId
     *        internal provider ID
     *
     * @return ???
     *
     * @throws NullPointerException
     *         if {@code pauthId} is {@code null}
     */
    public ViewProviderPage openProvider(String pauthId)
    {
        LOG.info("Open provider view ({}).", pauthId);
        ViewProviderPage viewProvider =
                new ViewProviderPage(selenium_, uri_.resolve("plr/ProviderDetails.xhtml"));
        viewProvider.openProvider(pauthId);
        return viewProvider;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     *
     * @param expectExpanded
     *        ???
     */
    public void verifyDataBlocksExpanded(ProviderType providerType, boolean expectExpanded)
    {
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            verifyDataBlocksExpanded(section, expectExpanded);
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param expectExpanded
     *        ???
     */
    public void verifyDataBlocksExpanded(ProviderSection section, boolean expectExpanded)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
        {
            assertEquals(
                    viewProvider.grabDataBlockExpanded(section, i),
                    expectExpanded,
                    String.format("Data block expanded (%s:%d)", section, i)
                    );
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     */
    public void verifyDataBlockSortOrder(ProviderType providerType)
    {
        for (ProviderSection section : ProviderSection.getProviderSectionSet(providerType))
        {
            // TODO (AZ) - Investigate sorting rules for these sections, skip for now.
            if (   !section.equals(ProviderSection.COMMUNICATION_PREFERENCE)
                && !section.equals(ProviderSection.CONFIDENTIALITY)
                && !section.equals(ProviderSection.DEMOGRAPHICS)
                && !section.equals(ProviderSection.ORGANIZATION_PROPERTIES)
                && !section.equals(ProviderSection.PROVIDER_RELATIONSHIPS))
            {
                verifyDataBlockSortOrder(section);
            }
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section ???
     */
    public void verifyDataBlockSortOrder(ProviderSection section)
    {
        List<String> sortKeyList = ProviderDataFields.getSortKey(section);
        List<String> dateKeyList = new ArrayList<>();
        boolean useActive = section.equals(ProviderSection.STATUSES);
        if (!section.equals(ProviderSection.WORK_LOCATIONS))
        {
            dateKeyList.add("Effective From");
        }
        dateKeyList.add("DB Created");
        List<String> previousValueList = null;
        List<String> previousDateList  = null;
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
        {
            Map<String,String> dataMap = viewProvider.grabDataBlockContent(section, i);
            List<String> valueList = extractDataValueList(sortKeyList, dataMap);
            List<String> dateList  = extractDataValueList(dateKeyList, dataMap);
            if (useActive)
            {
                valueList.addFirst(
                        viewProvider.grabDataBlockActive(section, i) ? "ACTIVE" : "INACTIVE");
            }
            if (previousValueList != null)
            {
                // Compare values in ascending order.
                int result = TextUtils.compareStringLists(previousValueList, valueList);
                if (result == 0)
                {
                    // Compare dates in descending order.
                    result = TextUtils.compareStringLists(dateList, previousDateList);
                }
                if (result > 0)
                {
                    String msg = String.format("Incorrect data block order (%s:%d).", section, i);
                    throw new IllegalStateException(msg);
                }
            }
            previousDateList  = dateList;
            previousValueList = valueList;
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     */
    public void verifyRequiredSections(ProviderType providerType)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            String noRecordsNotice = viewProvider.grabSectionNoRecordsNotice(section);
            if (section.isRequired())
            {
                assertNull(noRecordsNotice, "Section contains data.");
            }
            else
            {
                assertTrue(noRecordsNotice.startsWith("There are no "), "No records to display.");
            }
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     */
    public void verifySectionDataFieldNames(ProviderType providerType)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
            {
                Map<String,String> dataMap = viewProvider.grabDataBlockContent(section, i);
                List<String> fieldNameList = new ArrayList<>(dataMap.keySet());
                List<String> expectedFieldNameList = ProviderDataFields.getFieldNameList(section);
                if (   section.equals(ProviderSection.IDENTIFIERS)
                    && !fieldNameList.contains("Hds Type"))
                {
                    expectedFieldNameList = new ArrayList<>(expectedFieldNameList);
                    expectedFieldNameList.remove("Hds Type");
                }
                assertEquals(
                        fieldNameList,
                        expectedFieldNameList,
                        String.format("Data field name list (%s)", section));
            }
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     */
    public void verifySectionTitles(ProviderType providerType)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            assertEquals(
                    viewProvider.grabSectionTitle(section),
                    section.getTitle(),
                    "Provider section title");
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     *
     * @param expectedInactive
     *        ???
     */
    public void verifySectionsWithActiveDataBlocks(
            ProviderType providerType,
            boolean expectedInactive)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        ViewMode viewMode = viewProvider.getViewHeader().grabViewMode();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            viewProvider.scrollToSection(section);
            assertTrue(
                    viewProvider.grabSectionDisplayed(section),
                    String.format("Section displayed (%s)", section));
            assertTrue(
                    viewProvider.grabActiveDataBlockCount(section, true) > 0,
                    String.format("Presence of active data blocks (%s)", section));
            if (!expectedInactive)
            {
                assertTrue(
                        viewProvider.grabActiveDataBlockCount(section, false) == 0,
                        String.format("Absence of inactive data blocks (%s)", section));
            }
            else
            {
                if (   viewMode.equals(ViewMode.HISTORY)
                    && section.equals(ProviderSection.CONFIDENTIALITY))
                {
                    // NOTE: Special case
                    assertTrue(
                            viewProvider.grabActiveDataBlockCount(section, false) == 0,
                            String.format("Absence of inactive data blocks (%s)", section));
                }
                else
                {
                    assertTrue(
                            viewProvider.grabActiveDataBlockCount(section, false) > 0,
                            String.format("Presence of inactive data blocks (%s)", section));
                }
            }
        }
    }

    private static List<String> extractDataValueList(
            List<String> keyList, Map<String,String> dataMap)
    {
        List<String> valueList = new ArrayList<>();
        for (String key : keyList)
        {
            requireNonNull(key, "Null key.");
            String value = dataMap.get(key);
            if (value != null)
            {
                Matcher matcher = VALUE_CODE_PATTERN.matcher(value);
                if (matcher.matches())
                {
                    value = matcher.group("code");
                }
                valueList.add(value);
            }
            else
            {
                String msg = String.format("Null value for key (%s).", key);
                throw new NullPointerException(msg);
            }
        }
        return valueList;
    }

    private static Set<ProviderSection> getProviderSectionSet(
            ProviderType providerType, UserType userType)
    {
        Set<ProviderSection> set =
                EnumSet.copyOf(ProviderSection.getProviderSectionSet(providerType));
        if (!userType.equals(UserType.ADMIN))
        {
            set.remove(ProviderSection.REGISTRY_IDENTIFIERS);
        }
        return Collections.unmodifiableSet(set);
    }

    private ViewProviderPage waitForViewProviderPage()
    {
        ViewProviderPage page = new ViewProviderPage(selenium_);
        page.waitForReady();
        return page;
    }
}
