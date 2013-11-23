package org.nightlabs.jfire.reporting.layout;


/**
 * Utility to parse a file name and split its base name, locale and file extension. This is usually used
 * for "messages[_xy[_AB]].properties" files, where "messages" would be the {@link #getBaseName() base name},
 * "xy_AB" the {@link #getLocale() locale}, "xy" the {@link #getLanguage() language} and "AB" the
 * {@link #getCountry() country}. As indicated by the square brackets, the language and the country are optional.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class LocalisationFileName
{
	public static final char LOCALE_SEPARATOR = '_';
	public static final char EXTENSION_SEPARATOR = '.';

	private String fullName;
	private String baseName;
	private String locale;
	private String language;
	private String country;
	private String extension;

	/**
	 * Create a new instance of {@link LocalisationFileName}.
	 *
	 * @param fullName the complete file name of the file without directory information (i.e. the local name after the last "/" or "\"). This must <b>not</b> be <code>null</code>.
	 */
	public LocalisationFileName(String fullName)
	{
		if (fullName == null)
			throw new IllegalArgumentException("fullName must not be null!");

		this.fullName = fullName;
		String s = fullName;

		int idx = s.indexOf(EXTENSION_SEPARATOR);
		if (idx < 0)
			this.extension = "";
		else {
			this.extension = s.substring(idx + 1);
			s = s.substring(0, idx);
		}

		idx = s.indexOf(LOCALE_SEPARATOR);
		if (idx < 0)
			this.baseName = s;
		else {
			this.baseName = s.substring(0, idx);
			s = s.substring(idx + 1); // +1 because of separator (omit)

			this.locale = s;

			idx = s.indexOf('_');
			if (idx < 0)
				this.language = this.locale;
			else {
				this.language = s.substring(0, idx);
				s = s.substring(idx + 1); // +1 because we want to omit the separator

				idx = s.indexOf('_');
				if (idx < 0)
					this.country = s;
				else {
					this.country = s.substring(0, idx);
					// and we ignore the rest (which shouldn't normally happen anyway)...
				}
			}
		}

		if (baseName == null)
			baseName = "";

		if (locale == null)
			locale = "";

		if (language == null)
			language = "";

		if (country == null)
			country = "";

		if (extension == null)
			extension = "";
	}

	/**
	 * Get the complete file name as passed to the {@link #LocalisationFileName(String) constructor}.
	 *
	 * @return the complete file name.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Get the base name of the file, i.e. the part before the locale. For example, if you created
	 * an instance of {@link LocalisationFileName} with the full name "messages_de_CH.properties",
	 * then this will be "messages".
	 *
	 * @return the base name (never <code>null</code>, but may be an empty string).
	 */
	public String getBaseName() {
		return baseName;
	}
	/**
	 * Get the locale of the file, i.e. the part after base name and before file extension. For example, if you created
	 * an instance of {@link LocalisationFileName} with the full name "messages_de_CH.properties",
	 * then this will be "de_CH".
	 * <p>
	 * If the full name contains more than just language and country, it will be part of the locale, too, but not
	 * of language or country. For example "messages_de_CH_somegarbage.properties" will result in locale being "de_CH_somegarbage"
	 * (and {@link #getLanguage() language} = "de", {@link #getCountry() country} = "CH").
	 * </p>
	 *
	 * @return the locale (never <code>null</code>, but may be an empty string).
	 */
	public String getLocale() {
		return locale;
	}
	/**
	 * Get the language of the file, i.e. the first part of the {@link #getLocale() locale}. For example, if you created
	 * an instance of {@link LocalisationFileName} with the full name "messages_de_CH.properties",
	 * then this will be "de".
	 *
	 * @return the language (never <code>null</code>, but may be an empty string).
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * Get the country of the file, i.e. the second part of the {@link #getLocale() locale}. For example, if you created
	 * an instance of {@link LocalisationFileName} with the full name "messages_de_CH.properties",
	 * then this will be "CH".
	 *
	 * @return the country (never <code>null</code>, but may be an empty string).
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * Get the file extension. For example, if you created
	 * an instance of {@link LocalisationFileName} with the full name "messages_de_CH.properties",
	 * then this will be "properties" (the {@link #EXTENSION_SEPARATOR dot} is not contained).
	 *
	 * @return the file extension (never <code>null</code>, but may be an empty string).
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Create a complete file name for another locale. This is useful, if you have a file name for a certain locale (e.g. "messages_de_CH.properties") and
	 * want to get the next fallback file name, i.e. for "de" only. Thus, you'd pass "de" as <code>anotherLocale</code> and this method would return "messages_de.properties".
	 *
	 * @param anotherLocale the other locale for which a file name shall be generated.
	 * @return the file name.
	 */
	public String createFullName(String anotherLocale)
	{
		return getBaseName() + (anotherLocale.isEmpty() ? "" : LocalisationFileName.LOCALE_SEPARATOR) + anotherLocale + (getExtension().isEmpty() ? "" : EXTENSION_SEPARATOR) + getExtension();
	}
}
