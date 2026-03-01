package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class AddOrganizationNameFragment extends AddProviderStepFragment{
	



    private static final String ORGT_NAME_FIELD_CSS = "input#form\\:orgName";

    private static final String ORG_DESC_FIELD_CSS = "input#form\\:orgLongName";


    /**
     * Initializes page object and changes selenium's main locator to the Personal Information heading
     * @param selenium the selenium session
     */
    public AddOrganizationNameFragment(SeleniumSession selenium)
    {
        super(selenium, "Organization");
        STEP_PREFIX = "orgName";
    }
    

    /**
     * Fills the Organization Name field with the provided string
     * @param name the string to fill the Organization Name field with
     */
   
	public void fillName(String name) {
		selenium_.fillFieldByCss(ORGT_NAME_FIELD_CSS, name);
		
	}
	
	/**
     * Fills the Organization description field with the provided string
     * @param desc the string to fill the Organization description field with
     */
	
	public void fillDesc(String desc) {
		selenium_.fillFieldByCss(ORG_DESC_FIELD_CSS, desc);
	
	}

}
