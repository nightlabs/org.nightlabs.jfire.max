package org.nightlabs.jfire.web.webshop;

import java.util.Locale;

/**
 * @author Khaled - khaled[at]nightlabs[dot]de
 * @version $Version$ - $Date$
 */

public class Util {

	public static boolean isValidEmailAddress(String email) {
		return email != null && !email.isEmpty() && email.matches("^[^@]+@([0-9a-zA-Z\\-]+\\.)+[a-zA-Z]+$");
	}
	
	public static boolean isValidUrl(String url) {
		return url.matches("(https?|ftp)://(www\\.)?(\\w+\\.)?\\w+");
	}
	/**
	 * Convert a string based locale into a Locale Object
     * Strings are formatted:
     * language_contry_variant
     *
     **/
    
    public static Locale getLocaleFromString(String localeString) {
        if (localeString == null) return null;
        if (localeString.toLowerCase().equals("default")) return Locale.getDefault();
        int languageIndex = localeString.indexOf('_');
        if (languageIndex  == -1) return null;
        int countryIndex = localeString.indexOf('_', languageIndex +1);
        String country = null;
        if (countryIndex  == -1) {
            if (localeString.length() > languageIndex) {
                country = localeString.substring(languageIndex +1, localeString.length());
            } else {
                return null;
            }
        }
        int variantIndex = -1;
        if (countryIndex != -1) countryIndex = localeString.indexOf('_', countryIndex +1);
        String language = localeString.substring(0, languageIndex);
        String variant = null;
        if (variantIndex  != -1) {

            variant = localeString.substring(variantIndex +1, localeString.length());
        }
        if (variant != null) {
            return new Locale(language, country, variant);
        } else {
            return new Locale(language, country);
        }
    }
	
	
}
