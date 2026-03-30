package ca.bc.gov.health.qa.autotest.plr.web.tests.helper;

import java.util.*;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import org.apache.commons.lang3.StringUtils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class UpdateSimpleHelper {

	private static String generateRandomString(int length, String allowedChars) {
		Random random = new Random();

		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(allowedChars.length());
			sb.append(allowedChars.charAt(randomIndex));
		}
		return sb.toString();
	}

	/**
	 * Generates a random alphanumeric (A-Z, a-z, 0-9) string of the specified length.
	 *
	 * @param length the length of the generated string
	 * @return       the generated alphanumeric string
	 */
	public static String generateAlphabetNumericString(int length) {
		String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		return generateRandomString(length, allowedChars);
	}

	/**
	 * Generates a random alphabetic (A-Z, a-z) string of the specified length.
	 *
	 * @param length the length of the generated string
	 * @return 	 	 the generated alphabetic string
	 */
	public static String generateAlphabetString(int length) {
		String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

		return generateRandomString(length, allowedChars);
	}

	/**
	 * Generates a random numeric (0-9) string of the specified length.
	 *
	 * @param length the length of the generated string
	 * @return 	 	 the generated numeric string
	 */
	static public String generateNumericString(int length) {
		String allowedChars = "0123456789";

		return generateRandomString(length, allowedChars);
	}

	/**
	 * Gets today's date in "yyyy-MM-dd" format.
	 *
	 * @return today's date as a string in "yyyy-MM-dd" format
	 */
	public static String effective_date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
        return dateFormat.format(date);
	}

	/**
	 * Increments the current month by one and returns the date in "yyyy-MM-dd" format.
	 *
	 * @return the date one month from today as a string in "yyyy-MM-dd" format
	 */
	static public String increment_month_for_effective_date(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		Date nextYear = cal.getTime();
		return dateFormat.format(nextYear);
	}

	/**
	 * Increments the current year by one and returns the date in "yyyy-MM-dd" format.
	 *
	 * @return the date one year from today as a string in "yyyy-MM-dd" format
	 */
	public static String increment_year_for_effective_date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
        return dateFormat.format(nextYear);
	}

	/**
	 * Generates a random email address.
	 *
	 * @return the generated email address
	 */
	static public String generateEmail() {
		return generateAlphabetString(4) + "@" + generateAlphabetString(6) + ".com";
	}

	/**
	 * Generates a random HTTP URL.
	 *
	 * @return the generated HTTP URL
	 */
	public static String generateHTTP() {
		return "http://" + generateAlphabetString(6) + ".domain" + ".com";
	}

	/**
	 * Generates a random FTP URL.
	 *
	 * @return the generated FTP URL
	 */
	public static String generateFTP() {
		return "ftp://" + generateAlphabetString(6) + ".domain" + ".com";
	}

	/**
	 * Get the digit component from IfcID
	 *
	 * @param IfcID 	an IFC identifier of the form IFC.xxxxxx/BC.PRS
	 * @return 	 		the extracted digit component as a string
	 */
	public static String getFacilityId(String IfcID) {
		assertFalse(StringUtils.isEmpty(IfcID));
		assertTrue(IfcID.startsWith("IFC.") && IfcID.endsWith(".BC.PRS"));

		int startIndex = IfcID.indexOf('.');
		int endIndex = IfcID.indexOf('.', startIndex + 1); // Start searching after the first char
		String extractedPart = IfcID.substring(startIndex + 1, endIndex);

		return String.valueOf(Long.parseLong(extractedPart));
	}
	
	/**
	 * Get the digit component from a registry ID
	 *
	 * @param regIdType	the registry ID type prefix
	 * @param IfcID 	an IFC identifier of the form {regIdType}.xxxxxx/BC.PRS
	 * @return 	 		the extracted digit component as a string
	 */
	public static String getRegIdString(String regIdType,String IfcID) {
		assertFalse(StringUtils.isEmpty(IfcID));
		assertTrue(IfcID.startsWith(regIdType+".") && IfcID.endsWith(".BC.PRS"));

		int startIndex = IfcID.indexOf('.');
		int endIndex = IfcID.indexOf('.', startIndex + 1); // Start searching after the first char

        return IfcID.substring(startIndex + 1, endIndex);
	}

	/**
	 * Determines if a given string represents a positive integer.
	 *
	 * @param str 	the string to be checked
	 * @return 		true if the string represents a positive integer, false otherwise
	 */
	public static boolean isStringPositiveInteger(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		try {
			int number = Integer.parseInt(str);
			// Check if the parsed integer is greater than 0
			return number > 0;
		} catch (NumberFormatException e) {
			// If an exception is caught, the string is not a valid integer
			return false;
		}
	}
	
	/**
	 * convert name to wildcard for example, ("royal", 3) will generate "roy*"
	 *
	 * @param name name to be converted 
	 * @param length length of substring from name. 
	 * @return wildcard name,
	 */
	public static String generateWildcardName(String name, int length) {
		if (StringUtils.isEmpty(name))return "*" ;
		int cutLength = Math.min(name.length(), length);
        return name.substring(0, cutLength)+"*";
	}
	
	/**
	 * Grabs prefix from item string before the '-' character.
	 *
	 * @param item  the item string to extract the prefix from
	 * @return 		the extracted prefix
	 */
	public static String grabPrefix(String item) {
		assertFalse(StringUtils.isEmpty(item));
	
		int endIndex = item.indexOf('-');

        return item.substring(0, endIndex).strip();
		
		
	}
	
	/**
	 * get Random Number
	 * @param min	the min number
	 * @param max 	the max number
	 * @return 		random number between min and max
	 */
	public static int getRandomNumber(int min, int max) {
		// Create a Random object
		Random random = new Random();

		// Generate the random number
		// nextInt((max - min) + 1) generates a number between 0 and 4
		// Adding min (2) shifts the range to be between 2 and 6 (exclusive of 6)
		return random.nextInt((max - min) + 1) + min;

	}
	
	 /**
	  * compare two lists of string
	 * @param arr1 list of string
	 * @param arr2 list of string
	 * @return true if they have same elements after sorting, otherwise false
	 */
	public static boolean haveSameElements(List<String> arr1, List<String> arr2) {
		if (arr1 == null || arr2 == null) {
			return false;
		}
		if (arr1.size() != arr2.size()) {
			return false;
		}
		// Sort both arrays
		Collections.sort(arr1);
		Collections.sort(arr2);
		// Compare the sorted arrays using Arrays.equals()
		return arr1.equals(arr2);
	}

	/**
	 * Get the other provider's MaintainIndividualBuilder based on the given provider type.
	 * @param providers a map containing the MaintainIndividualBuilder instances for each provider type
	 * @param type the provider type for which to get the other provider's MaintainIndividualBuilder
	 * @return the MaintainIndividualBuilder instance for the other provider type
	 * @throws IllegalArgumentException if the provided type is not recognized
	 */
	public static MaintainRequestBuilder getOtherProvider(Map<ProviderType, MaintainRequestBuilder> providers, ProviderType type)
	{
		return switch(type) {
			case BC_PRACTITIONER -> providers.get(ProviderType.OOP_PRACTITIONER);
			case OOP_PRACTITIONER -> providers.get(ProviderType.BC_PRACTITIONER);
			default -> throw new IllegalArgumentException(); // should not occur
		};
	}
	
	
	public static void main(String[] args)
    {
		 List<String> list1 = new ArrayList<>(Arrays.asList("a", "b", "c"));
	        List<String> list2 = new ArrayList<>(Arrays.asList("c", "b", "a"));

	        // Sort both lists
	        haveSameElements(list1,list2);

	        System.out.println("Lists are equal (ignoring order): " +  haveSameElements(list1,list2)); // true
	    
    }

}
