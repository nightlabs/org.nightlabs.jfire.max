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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.GeographySystem;

public class CreateCountryCSVFromLocale
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CreateCountryCSVFromLocale.class);

	public CreateCountryCSVFromLocale()
	{
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {
			Map countries = new HashMap();

			// make sure, we have additional countries unknown to Locale
			for (Iterator it = GeographySystem.sharedInstance().getCountries().iterator(); it.hasNext(); ) {
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
				List countryList = new LinkedList(countries.values());
				Collections.sort(countryList, new Comparator() {
					public int compare(Object obj0, Object obj1)
					{
						Country c0 = (Country) obj0;
						Country c1 = (Country) obj1;
						return c0.getCountryID().compareTo(c1.getCountryID());
					}
				});
				
				Comparator nameEntryComparator = new Comparator() {
					public int compare(Object obj0, Object obj1) {
						Map.Entry me0 = (Map.Entry)obj0;
						Map.Entry me1 = (Map.Entry)obj1;

						String languageID0 = (String)me0.getKey();
						String languageID1 = (String)me1.getKey();

						return languageID0.compareTo(languageID1);
					};
				};

				logger.info("Writing CSV data...");
				for (Iterator itCountry = countryList.iterator(); itCountry.hasNext(); ) {
					Country country = (Country) itCountry.next();
					List names = new LinkedList(country.getName().getTexts());
					// sort by languageID
					Collections.sort(names, nameEntryComparator);

					for (Iterator itName = names.iterator(); itName.hasNext(); ) {
						Map.Entry me = (Map.Entry) itName.next();
						String languageID = (String)me.getKey();
						String name = (String)me.getValue();
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
