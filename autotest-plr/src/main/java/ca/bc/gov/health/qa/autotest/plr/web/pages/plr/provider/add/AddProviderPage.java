package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Add Provider page
 */
public class AddProviderPage extends BasicWebPage {

    final String FORM_TITLE_XPATH = "//div//table//tbody//tr//td//div//div//span[contains(text(),'%s')]";

    private static final String SUBMIT_BUTTON_CSS = "button#form\\:addProviderSubmit";

    private final ProviderType providerType;

    /**
     * Initializes page object and changes selenium's main locator to the Add Provider Heading
     * @param selenium the selenium session
     */
    public AddProviderPage(SeleniumSession selenium, String expectedHeader) {
        super(selenium,
                By.xpath("//div[@id='content']//h2[contains(text(),'Add Provider') and contains(text(),'" + expectedHeader + "')]"),
                "Add Provider");
        this.providerType = switch (expectedHeader) {
            case "(BC Practitioner)" -> ProviderType.BC_PRACTITIONER;
            case "(Out of Province Practitioner)" -> ProviderType.OOP_PRACTITIONER;
            case "(Organization)" -> ProviderType.ORGANIZATION;
            default -> throw new IllegalArgumentException("Unexpected provider type: " + expectedHeader);
        };

        waitForReady();
    }

    /**
     * Changes the provider type in the Add Provider page by clicking the corresponding option in the provider type menu
     * @param providerType the provider type to change to
     * @return a new AddProviderPage object with the provider type changed if the provider type is different from the current one,
     *         and the same AddProviderPage object if the provider type is the same as the current one
     */
    public AddProviderPage changeProviderType(ProviderType providerType)
    {
        if (this.providerType != providerType) {
            String expectedHeader = switch (providerType) {
                case BC_PRACTITIONER -> "(BC Practitioner)";
                case OOP_PRACTITIONER -> "(Out of Province Practitioner)";
                case ORGANIZATION -> "(Organization)";
            };

            List<WebElement> providerMenu = selenium_.findElements(
                    By.cssSelector("div#headerForm\\:subMenuPanelHolder > div > div > menu > li"));
            providerMenu.get(providerType.ordinal()).click();
            selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
            return new AddProviderPage(selenium_, expectedHeader);
        }
        return this;
    }
    
    
    /**
     * Open the add provider/organization/OOP page 
     * @param providerType the provider type to be added
     * @return  a new AddProviderPage object with the provider type specified
     */
    public AddProviderPage openProviderPage( ProviderType providerType) {
        String expectedHeader = switch (providerType) {
            case BC_PRACTITIONER -> "(BC Practitioner)";
            case OOP_PRACTITIONER -> "(Out of Province Practitioner)";
            case ORGANIZATION -> "(Organization)";
        };

        List<WebElement> providerMenu = selenium_.findElements(
                By.cssSelector("div#headerForm\\:subMenuPanelHolder > div > div > menu > li"));
        providerMenu.get(providerType.ordinal()).click();
        selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
        return new AddProviderPage(selenium_, expectedHeader);       
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
     * Fills the identifier form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param roleType the provider role type to select in the form, or null to not select any provider role type.
     *                 will be explicitly cast to either ProviderRoleType or OrganizationalProviderRoleType based on the provider type of the page
     * @param identifierType the identifier type to select in the form, or null to not select any identifier type
     * @param identifier the identifier to fill in the form, or null to not fill any identifier
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderIdFragment object after filling the form with the provided information
     */
    public AddProviderIdFragment fillIdentifier(Object roleType, HdsType hdsType, String hdsSubType,
                                                String identifierType, String identifier, List<Integer> effectiveFrom)
    {
        AddProviderIdFragment fragment = new AddProviderIdFragment(selenium_, providerType);

        if (roleType != null) {
            String prevRoleType = fragment.getProviderRoleType();
            String newRoleType;

            if (roleType.getClass().equals(String.class))
                newRoleType = fragment.selectProviderRoleType((String) roleType);
            else if (providerType.equals(ProviderType.ORGANIZATION))
                newRoleType = fragment.selectProviderRoleType((OrganizationalProviderRoleType) roleType);
            else newRoleType = fragment.selectProviderRoleType((ProviderRoleType) roleType);

            if (!newRoleType.equals(prevRoleType) && !newRoleType.equals("Select One")) {
                WebElement idType = selenium_.findElement(By.cssSelector("div#form\\:identifierType"));
                selenium_.waitUntil(ExpectedConditions.stalenessOf(idType));
            }

            if (hdsType != null && (roleType.equals(OrganizationalProviderRoleType.HDS))) {
                selenium_.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#form\\:hdsTypeId")));

                fragment.selectHdsType(hdsType);
                if (hdsSubType != null) fragment.selectHdsSubType(hdsSubType);
            }
        }
        if (identifierType != null) fragment.selectIdentifierType(identifierType);
        if (identifier != null) fragment.fillIdentifier(identifier);
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the identifier form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param roleType the provider role type to select in the form, or null to not select any provider role type.
     *                 will be explicitly cast to either ProviderRoleType or OrganizationalProviderRoleType based on the provider type of the page
     * @param identifierType the identifier type to select in the form, or null to not select any identifier type
     * @param identifier the identifier to fill in the form, or null to not fill any identifier
     * @return the AddProviderIdFragment object after filling the form with the provided information
     */
    public AddProviderIdFragment fillIdentifier(Object roleType, HdsType hdsType, String hdsSubType,
                                                String identifierType, String identifier)
    {
        AddProviderIdFragment fragment = new AddProviderIdFragment(selenium_, providerType);

        if (roleType != null) {
            String prevRoleType = fragment.getProviderRoleType();
            String newRoleType;

            if (roleType.getClass().equals(String.class))
                newRoleType = fragment.selectProviderRoleType((String) roleType);
            else if (providerType.equals(ProviderType.ORGANIZATION))
                newRoleType = fragment.selectProviderRoleType((OrganizationalProviderRoleType) roleType);
            else newRoleType = fragment.selectProviderRoleType((ProviderRoleType) roleType);

            if (!newRoleType.equals(prevRoleType) || newRoleType.equals("Select One")) {
                WebElement idType = selenium_.findElement(By.cssSelector("div#form\\:identifierType"));
                selenium_.waitUntil(ExpectedConditions.stalenessOf(idType));
            }

            if (hdsType != null && (roleType.equals(OrganizationalProviderRoleType.HDS))) {
                selenium_.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#form\\:hdsTypeId")));

                fragment.selectHdsType(hdsType);
                if (hdsSubType != null) fragment.selectHdsSubType(hdsSubType);
            }
        }
        if (identifierType != null) fragment.selectIdentifierType(identifierType);
        if (identifier != null) fragment.fillIdentifier(identifier);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }
    
    
	public AddProviderIdFragment fillOrganizationIdentifier(OrganizationalProviderRoleType roleType, HdsType hdsType, String hdsSubType,
			String identifierType, String identifier) {
		AddProviderIdFragment fragment = new AddProviderIdFragment(selenium_, providerType);

		if (roleType != null) {
			fragment.selectProviderRoleType(roleType);
			
			if(OrganizationalProviderRoleType.HDS.equals(roleType)){
				WebElement idType = selenium_.findElement(By.cssSelector("div#form\\:identifierType"));
				selenium_.waitUntil(ExpectedConditions.stalenessOf(idType));

			}
			if (hdsType != null && (roleType.equals(OrganizationalProviderRoleType.HDS))) {
				
				selenium_
						.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#form\\:hdsTypeId")));

				fragment.selectHdsType(hdsType);
				if (hdsSubType != null)
					fragment.selectHdsSubType(hdsSubType);
			}
		}
		if (identifierType != null)
			fragment.selectIdentifierType(identifierType);
		if (identifier != null)
			fragment.fillIdentifier(identifier);
		fragment.effectiveFromCurrentDate();

		return fragment;
	}


    /**
     * Fills the status form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param statusClassCode the status class code to select in the form, or null to not select any status class code
     * @param statusCode the status code to select in the form, or null to not select any status code
     * @param statusReasonCode the status reason code to select in the form, or null to not select any status reason code
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderStatusFragment object after filling the form with the provided information
     */
    public AddProviderStatusFragment fillStatus(String statusClassCode, StatusCodeOption statusCode, StatusReasonCodeOption statusReasonCode, List<Integer> effectiveFrom)
    {
        AddProviderStatusFragment fragment = new AddProviderStatusFragment(selenium_);

        if (statusClassCode != null) fragment.selectStatusClassCode(statusClassCode);
        if (statusCode != null) fragment.selectStatusCode(statusCode.getText());
        if (statusReasonCode != null) fragment.selectStatusReasonCode(statusReasonCode.getText());
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the status form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param statusClassCode the status class code to select in the form, or null to not select any status class code
     * @param statusCode the status code to select in the form, or null to not select any status code
     * @param statusReasonCode the status reason code to select in the form, or null to not select any status reason code
     * @return the AddProviderStatusFragment object after filling the form with the provided information
     */
    public AddProviderStatusFragment fillStatus(String statusClassCode, StatusCodeOption statusCode, StatusReasonCodeOption statusReasonCode)
    {
        AddProviderStatusFragment fragment = new AddProviderStatusFragment(selenium_);

        if (statusClassCode != null) fragment.selectStatusClassCode(statusClassCode);
        if (statusCode != null) fragment.selectStatusCode(statusCode.getText());
        if (statusReasonCode != null) fragment.selectStatusReasonCode(statusReasonCode.getText());
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the personal information form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param prefix the prefix to fill in the form, or null to not fill a prefix
     * @param firstName the first name to fill in the form, or null to not fill a first name
     * @param secondName the second name to fill in the form, or null to not fill a second name
     * @param thirdName the third name to fill in the form, or null to not fill a third name
     * @param surname the surname to fill in the form, or null to not fill a surname
     * @return the AddProviderPIFragment object after filling the form with the provided information
     */
    public AddProviderPIFragment fillPI(String prefix, String firstName, String secondName, String thirdName, String surname)
    {
        AddProviderPIFragment fragment = new AddProviderPIFragment(selenium_);

        if (prefix != null) fragment.fillPrefix(prefix);
        if (firstName != null) fragment.fillFirstName(firstName);
        if (secondName != null) fragment.fillSecondName(secondName);
        if (thirdName != null) fragment.fillThirdName(thirdName);
        if (surname != null) fragment.fillSurname(surname);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the personal information form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param prefix the prefix to fill in the form, or null to not fill a prefix
     * @param firstName the first name to fill in the form, or null to not fill a first name
     * @param secondName the second name to fill in the form, or null to not fill a second name
     * @param thirdName the third name to fill in the form, or null to not fill a third name
     * @param surname the surname to fill in the form, or null to not fill a surname
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderPIFragment object after filling the form with the provided information
     */
    public AddProviderPIFragment fillPI(String prefix, String firstName, String secondName, String thirdName, String surname, List<Integer> effectiveFrom)
    {
        AddProviderPIFragment fragment = new AddProviderPIFragment(selenium_);

        if (prefix != null) fragment.fillPrefix(prefix);
        if (firstName != null) fragment.fillFirstName(firstName);
        if (secondName != null) fragment.fillSecondName(secondName);
        if (thirdName != null) fragment.fillThirdName(thirdName);
        if (surname != null) fragment.fillSurname(surname);
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the demographics form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param dateOfBirth the date of birth to fill in the form as a list of integers
     *                    in the format [year, month, day], or null to not fill date of birth
     * @param gender the gender to select in the radio menu, or null to not select any gender
     * @return the AddProviderDemographicFragment object after filling the form with the provided information
     */
    public AddProviderDemographicFragment fillDemographics(List<Integer> dateOfBirth, String gender)
    {
        AddProviderDemographicFragment fragment = new AddProviderDemographicFragment(selenium_);

        if (dateOfBirth != null)
            fragment.dateOfBirthSpecificDate(dateOfBirth.get(0), dateOfBirth.get(1), dateOfBirth.get(2));
        if (gender != null) fragment.getGenderMenu().selectItem(gender);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the demographics form in the Add Provider flow with the provided information,
     * waiting for the form to be ready before filling.
     * @param dateOfBirth the date of birth to fill in the form as a raw string, or null to not fill date of birth
     * @param gender the gender to select in the radio menu, or null to not select any gender
     * @return the AddProviderDemographicFragment object after filling the form with the provided information
     */
    public AddProviderDemographicFragment fillDemographics(String dateOfBirth, String gender)
    {
        AddProviderDemographicFragment fragment = new AddProviderDemographicFragment(selenium_);

        if (dateOfBirth != null) fragment.dateOfBirthRaw(dateOfBirth);
        if (gender != null) fragment.getGenderMenu().selectItem(gender);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the demographics form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param dateOfBirth the date of birth to fill in the form as a list of integers
     *                    in the format [year, month, day], or null to not fill date of birth
     * @param gender the gender to select in the radio menu, or null
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderDemographicFragment object after filling the form with the provided information
     */
    public AddProviderDemographicFragment fillDemographics(List<Integer> dateOfBirth, String gender, List<Integer> effectiveFrom)
    {
        AddProviderDemographicFragment fragment = new AddProviderDemographicFragment(selenium_);

        if (dateOfBirth != null)
            fragment.dateOfBirthSpecificDate(dateOfBirth.get(0), dateOfBirth.get(1), dateOfBirth.get(2));
        if (gender != null) fragment.getGenderMenu().selectItem(gender);

        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the address form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param addressType the address type to select in the form, or null to not select any address type
     * @param addressPurpose the address purpose to select in the form, or null to not select any address purpose
     * @param addressLines a list of strings representing the address lines to fill in the form,
     *                     where the first element is the address line 1,
     *                     the second element is the address line 2,
     *                     and the third element is the address line 3.
     *                     If an element is null, that address line will not be filled.
     * @param city the city to fill in the form, or null to not fill a city
     * @param province the province to select in the form, or null to not select a province
     * @param country the country to select in the form, or null to not select a country
     * @param postalCode the postal code to fill in the form, or null to not fill a postal code
     * @return the AddProviderAddressFragment object after filling the form with the provided information
     */
    public AddProviderAddressFragment fillAddress(String addressType, String addressPurpose,
            List<String> addressLines, String city, String province, String country, String postalCode)
    {
        return fillAddress(addressType, addressPurpose, addressLines, city, province, country, postalCode, null);
    }

    /**
     * Fills the address form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param addressType the address type to select in the form, or null to not select any address type
     * @param addressPurpose the address purpose to select in the form, or null to not select any address purpose
     * @param addressLines a list of strings representing the address lines to fill in the form,
     *                     where the first element is the address line 1,
     *                     the second element is the address line 2,
     *                     and the third element is the address line 3.
     *                     If an element is null, that address line will not be filled.
     * @param city the city to fill in the form, or null to not fill a city
     * @param province the province to select in the form, or null to not select a province
     * @param country the country to select in the form, or null to not select a country
     * @param postalCode the postal code to fill in the form, or null to not fill a postal code
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to fill the current date as the effective from date
     * @return the AddProviderAddressFragment object after filling the form with the provided information
     */
    public AddProviderAddressFragment fillAddress(String addressType, String addressPurpose,
            List<String> addressLines, String city, String province, String country,
            String postalCode, List<Integer> effectiveFrom)
    {
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(selenium_);

        if (addressType != null) addressFragment.selectAddressType(addressType);
        if (addressPurpose != null) addressFragment.selectAddressPurpose(addressPurpose);
        addressFragment.fillAddressLine1(addressLines.get(0));
        addressFragment.fillAddressLine2(addressLines.get(1));
        addressFragment.fillAddressLine3(addressLines.get(2));
        addressFragment.fillCity(city);
        if (province != null) addressFragment.selectProvinceState(province);
        if (country != null) addressFragment.selectCountry(country);
        if (postalCode != null) addressFragment.fillPostalCode(postalCode);

        if (effectiveFrom == null) addressFragment.effectiveFromCurrentDate();
        else  addressFragment.effectiveFromSpecificDate(
                effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return addressFragment;
    }

    /**
     * Fills the address form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param addressType the address type to select in the form, or null to not select any address type
     * @param addressPurpose the address purpose to select in the form, or null to not select any address purpose
     * @param addressAutocompleteField the address to fill in the autocomplete field, or null to not fill any address
     * @param addressAutocompletePrefix the prefix to use when filling in the autocomplete field, or null to not fill any address.
     *                                  This is used to specify a unique portion of the address to ensure the correct address is selected from the autocomplete dropdown.
     * @return the AddProviderAddressFragment object after filling the form with the provided information
     */
    public AddProviderAddressFragment fillAddress(String addressType, String addressPurpose, String addressAutocompleteField, String addressAutocompletePrefix)
    {
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(selenium_);

        if (addressType != null) addressFragment.selectAddressType(addressType);
        if (addressPurpose != null) addressFragment.selectAddressPurpose(addressPurpose);
        addressFragment.fillAddressAutocomplete(addressAutocompleteField, addressAutocompletePrefix);

        addressFragment.effectiveFromCurrentDate();

        return addressFragment;
    }

    /**
     * Fills the address form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param addressType the address type to select in the form, or null to not select any address type
     * @param addressPurpose the address purpose to select in the form, or null to not select any address purpose
     * @param addressAutocompleteField the address to fill in the autocomplete field, or null to not fill any address
     * @param addressAutocompletePrefix the prefix to use when filling in the autocomplete field, or null to not fill any address.
     *                                  This is used to specify a unique portion of the address to ensure the correct address is selected from the autocomplete dropdown.
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderAddressFragment object after filling the form with the provided information
     */
    public AddProviderAddressFragment fillAddress(String addressType, String addressPurpose,
            String addressAutocompleteField, String addressAutocompletePrefix, List<Integer> effectiveFrom)
    {
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(selenium_);

        if (addressType != null) addressFragment.selectAddressType(addressType);
        if (addressPurpose != null) addressFragment.selectAddressPurpose(addressPurpose);
        addressFragment.fillAddressAutocomplete(addressAutocompleteField, addressAutocompletePrefix);

        if (effectiveFrom != null)
            addressFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return addressFragment;
    }

    /**
     * Fills the phone number form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param areaCode the area code to fill in the form, or null to not fill an area code
     * @param phoneNumber the phone number to fill in the form, or null to not fill a phone number
     * @param extension the extension to fill in the form, or null to not fill an extension
     * @return the AddProviderPhoneFragment object after filling the form with the provided information
     */
    public AddProviderPhoneFragment fillPhone(String areaCode, String phoneNumber, String extension)
    {
        AddProviderPhoneFragment phoneFragment = new AddProviderPhoneFragment(selenium_);

        if (areaCode != null) phoneFragment.fillAreaCode(areaCode);
        if (phoneNumber != null) phoneFragment.fillPhoneNumber(phoneNumber);
        if (extension != null) phoneFragment.fillExtension(extension);
        phoneFragment.effectiveFromCurrentDate();

        return phoneFragment;
    }

    /**
     * Fills the phone number form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param areaCode the area code to fill in the form, or null to not fill an area code
     * @param phoneNumber the phone number to fill in the form, or null to not fill a phone number
     * @param extension the extension to fill in the form, or null to not fill an extension
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderPhoneFragment object after filling the form with the provided information
     */
    public AddProviderPhoneFragment fillPhone(String areaCode, String phoneNumber, String extension, List<Integer> effectiveFrom)
    {
        AddProviderPhoneFragment phoneFragment = new AddProviderPhoneFragment(selenium_);

        if (areaCode != null) phoneFragment.fillAreaCode(areaCode);
        if (phoneNumber != null) phoneFragment.fillPhoneNumber(phoneNumber);
        if (extension != null) phoneFragment.fillExtension(extension);
        if (effectiveFrom != null) phoneFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return phoneFragment;
    }

    /**
     * Fills the fax number form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param areaCode the area code to fill in the form, or null to not fill an area code
     * @param faxNumber the fax number to fill in the form, or null to not fill a fax number
     * @return the AddProviderFaxFragment object after filling the form with the provided information
     */
    public AddProviderFaxFragment fillFax(String areaCode, String faxNumber)
    {
        AddProviderFaxFragment faxFragment = new AddProviderFaxFragment(selenium_);

        if (areaCode != null) faxFragment.fillAreaCode(areaCode);
        if (faxNumber != null) faxFragment.fillFaxNumber(faxNumber);
        faxFragment.effectiveFromCurrentDate();

        return faxFragment;
    }

    /**
     * Fills the fax number form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param areaCode the area code to fill in the form, or null to not fill an area code
     * @param faxNumber the fax number to fill in the form, or null to not fill a fax number
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderFaxFragment object after filling the form with the provided information
     */
    public AddProviderFaxFragment fillFax(String areaCode, String faxNumber, List<Integer> effectiveFrom)
    {
        AddProviderFaxFragment faxFragment = new AddProviderFaxFragment(selenium_);

        if (areaCode != null) faxFragment.fillAreaCode(areaCode);
        if (faxNumber != null) faxFragment.fillFaxNumber(faxNumber);
        if (effectiveFrom != null) faxFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return faxFragment;
    }

    /**
     * Fills the email form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param emailAddress the email address to fill in the form, or null to not fill an email address
     * @return the AddProviderEmailFragment object after filling the form with the provided information
     */
    public AddProviderEmailFragment fillEmail(String emailAddress)
    {
        AddProviderEmailFragment emailFragment = new AddProviderEmailFragment(selenium_);

        if (emailAddress != null) emailFragment.fillEmailAddress(emailAddress);
        emailFragment.effectiveFromCurrentDate();

        return emailFragment;
    }

    /**
     * Fills the email form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param emailAddress the email address to fill in the form, or null to not fill an email address
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderEmailFragment object after filling the form with the provided information
     */
    public AddProviderEmailFragment fillEmail(String emailAddress, List<Integer> effectiveFrom)
    {
        AddProviderEmailFragment emailFragment = new AddProviderEmailFragment(selenium_);

        if (emailAddress != null) emailFragment.fillEmailAddress(emailAddress);
        if (effectiveFrom != null) emailFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return emailFragment;
    }

    /**
     * Fills the credential form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param credentialType the credential type to select in the form, or null to not select any credential type
     * @param designation the designation to fill in the form, or null to not fill a designation
     * @param registrationNumber the registration number to fill in the form, or null to not fill a registration number
     * @param institution the institution to fill in the form, or null to not fill an institution
     * @param city the city to fill in the form, or null to not fill a city
     * @param country the country to select in the form, or null to not select a country
     * @param provinceState the province/state to select in the form, or null to not select a province/state
     * @param equivalency whether to enable equivalency in the form
     * @return the AddProviderCredentialFragment object after filling the form with the provided information
     */
    public AddProviderCredentialFragment fillCredentials(String credentialType, String designation,
                                                         String registrationNumber, String institution,
                                                         String city, String country, String provinceState,
                                                         boolean equivalency, String year)
    {
        AddProviderCredentialFragment credentialFragment = new AddProviderCredentialFragment(selenium_);

        if (credentialType != null) credentialFragment.selectCredentialType(credentialType);
        if (designation != null) credentialFragment.fillDesignation(designation);
        if (registrationNumber != null) credentialFragment.fillRegistrationNumber(registrationNumber);
        if (institution != null) credentialFragment.fillInstitution(institution);
        if (city != null) credentialFragment.fillCity(city);
        if (country != null) credentialFragment.selectCountry(country);
        if (provinceState != null) credentialFragment.selectProvinceState(provinceState);
        credentialFragment.enableEquivalency(equivalency);
        if (year != null) credentialFragment.fillYear(year);
        credentialFragment.effectiveFromCurrentDate();

        return credentialFragment;
    }

    /**
     * Fills the credential form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param credentialType the credential type to select in the form, or null to not select any credential type
     * @param designation the designation to fill in the form, or null to not fill a designation
     * @param registrationNumber the registration number to fill in the form, or null to not fill a registration number
     * @param institution the institution to fill in the form, or null to not fill an institution
     * @param city the city to fill in the form, or null to not fill a city
     * @param country the country to select in the form, or null to not select a country
     * @param provinceState the province/state to select in the form, or null to not select a province/state
     * @param equivalency whether to enable equivalency in the form
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderCredentialFragment object after filling the form with the provided information
     */
    public AddProviderCredentialFragment fillCredentials(String credentialType, String designation,
                                                         String registrationNumber, String institution,
                                                         String city, String country, String provinceState,
                                                         boolean equivalency, String year, List<Integer> effectiveFrom)
    {
        AddProviderCredentialFragment credentialFragment = new AddProviderCredentialFragment(selenium_);

        if (credentialType != null) credentialFragment.selectCredentialType(credentialType);
        if (designation != null) credentialFragment.fillDesignation(designation);
        if (registrationNumber != null) credentialFragment.fillRegistrationNumber(registrationNumber);
        if (institution != null) credentialFragment.fillInstitution(institution);
        if (city != null) credentialFragment.fillCity(city);
        if (country != null) credentialFragment.selectCountry(country);
        if (provinceState != null) credentialFragment.selectProvinceState(provinceState);
        credentialFragment.enableEquivalency(equivalency);
        if (year != null) credentialFragment.fillYear(year);
        if (effectiveFrom != null)
            credentialFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return credentialFragment;
    }

    /**
     * Fills the expertise form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param expertise the expertise to select in the form, or null to not select any expertise
     * @param sourceCode the source code to fill in the form, or null to not fill a source code
     * @return the AddProviderExpertiseFragment object after filling the form with the provided information
     */
    public AddProviderExpertiseFragment fillExpertise(String expertise, String sourceCode)
    {
        AddProviderExpertiseFragment expertiseFragment = new AddProviderExpertiseFragment(selenium_);

        if (expertise != null) expertiseFragment.selectExpertise(expertise);
        if (sourceCode != null) expertiseFragment.fillSourceCode(sourceCode);
        expertiseFragment.effectiveFromCurrentDate();

        return expertiseFragment;
    }

    /**
     * Fills the expertise form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param expertise the expertise to select in the form, or null to not select any expertise
     * @param sourceCode the source code to fill in the form, or null to not fill a source code
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [year, month, day], or null to not fill an effective from date
     * @return the AddProviderExpertiseFragment object after filling the form with the provided information
     */
    public AddProviderExpertiseFragment fillExpertise(String expertise, String sourceCode, List<Integer> effectiveFrom)
    {
        AddProviderExpertiseFragment expertiseFragment = new AddProviderExpertiseFragment(selenium_);

        if (expertise != null) expertiseFragment.selectExpertise(expertise);
        if (sourceCode != null) expertiseFragment.fillSourceCode(sourceCode);
        if (effectiveFrom != null)
            expertiseFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return expertiseFragment;
    }

    /**
     * Finds and clicks the submit button to submit the provider with the information filled in.
     * Must be on the final step of the Add Provider flow
     * @return a ViewProviderPage reference to the newly created provider with the information filled in.
     */
    public ViewProviderPage clickSubmitButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SUBMIT_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));

        return new ViewProviderPage(selenium_);
    }

    /**
     * Waits for a step in the Add Provider flow to be available
     *
     * @param step      the title of the step to be locating (header of the div form, e.g. Identifier, Status, etc.)
     * @param next      whether we are waiting for the step to be available (true) or the step to be unavailable (false)
     */
    public void waitForAddProviderStep(String step, boolean next)
    {
        By stepLocator = By.xpath(String.format(FORM_TITLE_XPATH, step));
        if (next)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(stepLocator,
                    "ui-panel-title"));
        }
        else selenium_.waitUntil(SeleniumExpectedConditions.absenceOfElementLocated(stepLocator));
    }

    /**
     * Waits for a widget appearing after attempting to pass an Add Provider Step to be visible and interactable
     *
     * @param widget                    a unique portion in the header of the widget to be located
     * @throws IllegalStateException    when searching for the desired widget times out (most likely not found)
     */
    public void waitForWidgetVisibility(String widget)
    {
        By widgetLocator = By.xpath(String.format(
                "//form//div//div//div//div//span[contains(text(), '%s')]//parent::div//parent::div", widget));

        try
        {
            selenium_.waitUntil(ExpectedConditions.attributeToBe(widgetLocator, "aria-hidden", "false"));
        }
        catch (org.openqa.selenium.TimeoutException e) { throw new IllegalStateException(e.getMessage()); }
        catch (org.openqa.selenium.StaleElementReferenceException e) { waitForWidgetVisibility(widget); }
    }

    /**
     * Advances to the next stage of the Add Provider flow with the Next button.
     *
     * @param currentState     the title of the previous form in the Add Provider flow.
     * @param errorWidget      the widget to wait for visibility for in the event of an error.
     *                         specify the widget if there will be an error with a widget, set to the empty string if
     *                         no error is expected, and set to null if an error with no widget is expected
     */
    public void clickNext(String currentState, String errorWidget)
    {
        WebElement stepTitle = selenium_.findElement(By.xpath(String.format(FORM_TITLE_XPATH, currentState)));

        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getLast().click();

        selenium_.waitUntil(ExpectedConditions.stalenessOf(stepTitle));

        if (errorWidget != null) {
            if (errorWidget.isEmpty()) waitForAddProviderStep(currentState, false);
            else {
                try {
                    waitForWidgetVisibility(errorWidget);
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("Search for widget " + errorWidget + " timed out");
                }
            }
        } else { waitForAddProviderStep(currentState, true); }
    }

    /**
     * Return to the previous stage of the Add Provider flow with the Back button.
     * Requires the Back button to be visible/displayed.
     *
     * @param currentState      the title of the previous form in the Add Provider flow.
     * @param expectedError     whether clicking next is expected to return an error (true) or not (false)
     */
    public void clickBack(String currentState, boolean expectedError)
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getFirst().click();

        if (!expectedError) waitForAddProviderStep(currentState, false);
    }
    
    @FindBy(how = How.XPATH, using = "//div[contains(.,'Address Recommended')]/button[contains(.,'Continue w/ Original')]")
	private WebElement addrValContinueWithOriginal1;
	@FindBy(how = How.XPATH, using = "//div[not(contains(.,'Address Recommended')) and contains(.,'Address Provided')]/button[contains(.,'Continue w/ Original')]")
	private WebElement addrValContinueWithOriginal2;
    
    public static void closeAddressValidationDialogWithContinue(WebElement addrValContinueWithOriginal1,
			WebElement addrValContinueWithOriginal2) throws InterruptedException {
		Thread.sleep(4000);
		if (addrValContinueWithOriginal1.isDisplayed()) {
			addrValContinueWithOriginal1.click();
			Thread.sleep(1000);
		} else if (addrValContinueWithOriginal2.isDisplayed()) {
			addrValContinueWithOriginal2.click();
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
	}
    
    
    public static void closeAddressValidationDialogWithCancel(WebElement addrValContinueWithOriginal1,
			WebElement addrValContinueWithOriginal2, WebElement addrValCancel1, WebElement addrValCancel2)
			throws InterruptedException {
		Thread.sleep(4000);
		if (addrValContinueWithOriginal1.isDisplayed() && addrValCancel1.isDisplayed()) {
			addrValCancel1.click();
			Thread.sleep(1000);
		} else if (addrValContinueWithOriginal2.isDisplayed() && addrValCancel2.isDisplayed()) {
			addrValCancel2.click();
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
	}

	public AddOrganizationNameFragment fillOrganizationName(String name, String desc) {
		AddOrganizationNameFragment fragment = new AddOrganizationNameFragment(selenium_);

	        if (name != null) fragment.fillName(name);
	        if (desc != null) fragment.fillDesc(desc);
	    
	        fragment.effectiveFromCurrentDate();

	        return fragment;
		
	}
	/**
	     * Gets the currently highlighted step in the Add Provider flow
	     *
	     * @return  a string of the highlighted step
	     */
	    public String getStep()
	    {
	        return selenium_.findElementByCss("div.ui-wizard.ui-widget > ul > li.ui-state-highlight").getText();
	    }
}
