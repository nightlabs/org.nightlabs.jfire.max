/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.geography.resource.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyImplResourceCSV;

public class CreateCountryCSVFromLocale
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CreateCountryCSVFromLocale.class);

	public CreateCountryCSVFromLocale()
	{
	}

	public static void main(String[] args)
	{
		try {
			GeographyImplResourceCSV.register();

			Map<String, Country> countries = new HashMap<String, Country>();

			// make sure, we have additional countries unknown to Locale
			for (Iterator<Country> it = Geography.sharedInstance().getCountries().iterator(); it.hasNext(); ) {
				Country country = (Country) it.next();
				countries.put(country.getCountryID(), country);
			}

			logger.info("Loading countries from "+Locale.class.getName()+"...");
			String[] countryIDs = Locale.getISOCountries();
			String[] languageIDs = Locale.getISOLanguages();
			String defaultLanguageID = Locale.getDefault().getLanguage();

			int countryCount = 0;
			int countryLangCount = 0;

			// make sure, the default languageID is the first - we ignore a subsequent duplicate
			String[] l = new String[languageIDs.length + 1];
			System.arraycopy(languageIDs, 0, l, 1, languageIDs.length);
			l[0] = defaultLanguageID;
			languageIDs = l;

			URL fileURL = CreateCountryCSVFromLocale.class.getResource("..");
			File file = new File(
					new File(fileURL.toURI()).getAbsolutePath().replaceAll("\\/bin\\/", "\\/src\\/"),
					"Data-Country-from_java_util_Locale.csv");
			FileOutputStream fout = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(fout);
			try {
				writer.write("CountryID;LanguageID;CountryName\n");

				for (int i = 0; i < countryIDs.length; ++i) {
					String countryID = countryIDs[i];

					if ("".equals(countryID))
						continue;

					Country country = (Country) countries.get(countryID);
//					if (country != null)
//						continue; // ignore all countries that are already loaded from the CSV or have already been processed

					if (country == null) {
						country = new Country(countryID);
						countries.put(countryID, country);
						++countryCount;
					}

					for (int m = 0; m < languageIDs.length; ++m) {
						String languageID = languageIDs[m];
						if (m != 0 && defaultLanguageID.equals(languageID)) // if the default language is not the first, we'll ignore it (it's a duplicate)
							continue;

						Locale locale = new Locale(languageID, countryID);

						String countryName = locale.getDisplayCountry(locale);
						if (defaultLanguageID.equals(languageID) || !country.getName().getText(defaultLanguageID).equals(countryName)) {
							country.getName().setText(languageID, countryName);
//							writer.write(countryID + ';' + languageID + ';' + countryName + '\n');
							++countryLangCount;
						}
					}
				}

				logger.info("Sorting " + countries.size() + " countries by countryID...");
				List<Country> countryList = new ArrayList<Country>(countries.values());
				Collections.sort(countryList, new Comparator<Country>() {
					public int compare(Country c0, Country c1)
					{
						return c0.getCountryID().compareTo(c1.getCountryID());
					}
				});
				
				Comparator<Map.Entry<String, String>> nameEntryComparator = new Comparator<Map.Entry<String,String>>() {
					public int compare(Map.Entry<String, String> me0, Map.Entry<String, String> me1) {
						String languageID0 = me0.getKey();
						String languageID1 = me1.getKey();
						return languageID0.compareTo(languageID1);
					};
				};

				logger.info("Writing CSV data...");
				for (Iterator<Country> itCountry = countryList.iterator(); itCountry.hasNext(); ) {
					Country country = (Country) itCountry.next();
					List<Map.Entry<String, String>> names = new ArrayList<Map.Entry<String,String>>(country.getName().getTexts());
					// sort by languageID
					Collections.sort(names, nameEntryComparator);

					for (Iterator<Map.Entry<String, String>> itName = names.iterator(); itName.hasNext(); ) {
						Map.Entry<String, String> me = itName.next();
						String languageID = me.getKey();
						String name = me.getValue();
						writer.write(country.getCountryID() + ';' + languageID + ';' + name + '\n');
					}
				}

			} finally {
				writer.close();
				fout.close();
			}
			logger.info("Added "+countryCount+" new  countries & "+countryLangCount+" names from "+Locale.class.getName()+".");
		} catch (Throwable t) {
			logger.error("CSV creation failed!", t);
		}
	}

}
