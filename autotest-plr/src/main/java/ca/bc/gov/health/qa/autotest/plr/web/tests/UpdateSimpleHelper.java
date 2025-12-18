package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateSimpleHelper {

    static private String generateRandomString(int length, String allowedChars)
    {
        Random random = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            sb.append(allowedChars.charAt(randomIndex));
        }
        return sb.toString();
    }

	static public String  generateAlphabetNumericString(int length) {
		String allowedChars = 
		        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        return generateRandomString(length, allowedChars);
	}
	
	static public String  generateAlphabetString(int length) {
		String allowedChars =    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		
		return generateRandomString(length, allowedChars);
	}
	
	static public String  generateNumericString(int length) {
		String allowedChars =   "0123456789";
		
		return generateRandomString(length, allowedChars);
	}

	static public String effective_date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateformatted = dateFormat.format(date);
		return dateformatted;
	}
	
	static public String increment_year_for_effective_date(){            
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
		return dateFormat.format(nextYear);
	}
	
	
	static public String generateEmail(){            
		return generateAlphabetString(4)+"@"+generateAlphabetString(6)+".com";
	}
	
	static public String generateHTTP(){
		return "http://"+generateAlphabetString(6)+".domain"+".com";
	}

    /**
     * TODO (KD) - doc
     *
     * @param IfcID
     * @return
     */
	static public String getFaultId(String IfcID) {
        assertFalse(StringUtils.isEmpty(IfcID));
		assertTrue(IfcID.startsWith("IFC.")&&IfcID.endsWith(".BC.PRS"));

        int startIndex = IfcID.indexOf('.');
        int endIndex = IfcID.indexOf('.', startIndex + 1); // Start searching after the first char
        String extractedPart = IfcID.substring(startIndex + 1, endIndex);

        return String.valueOf(Long.parseLong(extractedPart));
	}

}

