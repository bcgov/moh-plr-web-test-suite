package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.util.Random;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateSimpleHelper {
	
	static public String  generateAlphabetNumericString(int length) {
		String allowedChars = 
		        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		
		Random random = new Random();
	
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            sb.append(allowedChars.charAt(randomIndex));
        }
		return sb.toString();
	}
	
	static public String  generateAlphabetString(int length) {
		String allowedChars =    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		
		Random random = new Random();
	
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            sb.append(allowedChars.charAt(randomIndex));
        }
		return sb.toString();
	}
	
	static public String  generateNumericString(int length) {
		String allowedChars =   "0123456789";
		
		Random random = new Random();
	
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            sb.append(allowedChars.charAt(randomIndex));
        }
		return sb.toString();
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
		Date today = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
		String next_year = dateFormat.format(nextYear);
		return next_year;
	}
	
	
	static public String generateEmail(){            
		return generateAlphabetString(4)+"@"+generateAlphabetString(6)+".com";
	}
	
	static public String generateHTTP(){
		return "http://"+generateAlphabetString(6)+".domain"+".com";
	}
	
	public static void main(String[] args) {
	
	}

}

