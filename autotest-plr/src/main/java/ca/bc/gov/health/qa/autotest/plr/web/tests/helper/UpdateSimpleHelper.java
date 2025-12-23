package ca.bc.gov.health.qa.autotest.plr.web.tests.helper;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

	public static String generateAlphabetNumericString(int length) {
		String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		return generateRandomString(length, allowedChars);
	}

	public static String generateAlphabetString(int length) {
		String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

		return generateRandomString(length, allowedChars);
	}

	static public String generateNumericString(int length) {
		String allowedChars = "0123456789";

		return generateRandomString(length, allowedChars);
	}

	public static String effective_date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateformatted = dateFormat.format(date);
		return dateformatted;
	}

	public static String increment_year_for_effective_date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
		String next_year = dateFormat.format(nextYear);
		return next_year;
	}

	static public String generateEmail() {
		return generateAlphabetString(4) + "@" + generateAlphabetString(6) + ".com";
	}

	public static String generateHTTP() {
		return "http://" + generateAlphabetString(6) + ".domain" + ".com";
	}

	public static String generateFTP() {
		return "ftp://" + generateAlphabetString(6) + ".domain" + ".com";
	}

	/**
	 * TODO (KD) - doc
	 *
	 * @param IfcID
	 * @return
	 */
	public static String getFaultId(String IfcID) {
		assertFalse(StringUtils.isEmpty(IfcID));
		assertTrue(IfcID.startsWith("IFC.") && IfcID.endsWith(".BC.PRS"));

		int startIndex = IfcID.indexOf('.');
		int endIndex = IfcID.indexOf('.', startIndex + 1); // Start searching after the first char
		String extractedPart = IfcID.substring(startIndex + 1, endIndex);

		return String.valueOf(Long.parseLong(extractedPart));
	}

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

}
