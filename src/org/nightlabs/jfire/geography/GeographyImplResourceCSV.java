package org.nightlabs.jfire.geography;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.CollectionUtil;

public class GeographyImplResourceCSV
extends Geography
{
	private static final Logger logger = Logger.getLogger(GeographyImplResourceCSV.class);

	/**
	 * This method sets the system property {@link Geography#PROPERTY_KEY_GEOGRAPHY_CLASS}
	 * to the fully qualified class name of <code>GeographyImplResourceCSV</code>. This method
	 * does not create a shared instance!
	 */
	public static void register()
	{
		System.setProperty(PROPERTY_KEY_GEOGRAPHY_CLASS, GeographyImplResourceCSV.class.getName());
	}

	/**
	 * This method creates a new instance of <code>GeographyImplResourceCSV</code> and sets it
	 * as shared instance. Therefore, a subsequent call to {@link Geography#sharedInstance()} will
	 * return this instance (if it is not overridden by other code). Note, that there is one
	 * shared instance per organisation. The organisationID is determined by {@link SecurityReflector}.
	 */
	public static void createSharedInstance()
	{
		register();
		setSharedInstance(new GeographyImplResourceCSV());
	}

	public GeographyImplResourceCSV()
	{
		csvOrganisationID = getRootOrganisationID();
	}

	private final String csvOrganisationID;
//	private static final String csvOrganisationID = Organisation.DEVIL_ORGANISATION_ID;

	protected static String[] csvLine2Fields(String line)
	{
		StringTokenizer tok = new StringTokenizer(line, ";", true);
		LinkedList<String> res = new LinkedList<String>();
		boolean lastWasSep = false;
		while (tok.hasMoreTokens()) {
			String f = tok.nextToken();
			if (";".equals(f)) {
				if (lastWasSep)
					res.add("");
				lastWasSep = true;
			}
			else {
				lastWasSep = false;
				res.add(f);
			}
		}
		if (lastWasSep)
			res.add("");

		return (String[]) CollectionUtil.collection2TypedArray(res, String.class);
	}

	/**
	 * This method encodes a {@link Country} instance into
	 * one or more lines (one for each language) formatted
	 * according to the CSV file format.
	 *
	 * @param country The region to be encoded.
	 * @return A String containing one or more lines to be written to the CSV.
	 */
	protected static String country2csvLines(Country country)
	{
		StringBuffer csvLine = new StringBuffer();

		if (country.getName().getTexts().isEmpty()) // we ensure that at least one line will be written
			country.getName().setText(Locale.ENGLISH.getLanguage(), country.getName().getText(Locale.ENGLISH.getLanguage()));

		//CountryID;LanguageID;CountryName
		for(Map.Entry<String, String> me : country.getName().getTexts()){
			String languageID = me.getKey();
			String text = me.getValue();

			csvLine.append(country.getCountryID()).append(";");

			csvLine.append(languageID).append(";");
			csvLine.append(text == null ? "" : text);
			csvLine.append("\n");
		}

		return csvLine.toString();
	}
	
	/**
	 * This method encodes a {@link Region} instance into
	 * one or more lines (one for each language) formatted
	 * according to the CSV file format.
	 *
	 * @param region The region to be encoded.
	 * @return A String containing one or more lines to be written to the CSV.
	 */
	protected static String region2csvLines(Region region)
	{
		StringBuffer csvLine = new StringBuffer();

		if (region.getName().getTexts().isEmpty()) // we ensure that at least one line will be written
			region.getName().setText(Locale.ENGLISH.getLanguage(), region.getName().getText(Locale.ENGLISH.getLanguage()));

		//CountryID;RegionID;LanguageID;RegionName
		for(Map.Entry<String, String> me : region.getName().getTexts()){
			String languageID = me.getKey();
			String text = me.getValue();

			csvLine.append(region.getCountryID()).append(";");
			csvLine.append(region.getRegionID()).append(";");

			csvLine.append(languageID).append(";");
			csvLine.append(text == null ? "" : text);
			csvLine.append("\n");
		}

		return csvLine.toString();
	}

	/**
	 * This method encodes a {@link City} instance into
	 * one or more lines (one for each language) formatted
	 * according to the CSV file format.
	 *
	 * @param city The city to be encoded.
	 * @return A String containing one or more lines to be written to the CSV.
	 */
	protected static String city2csvLines(City city)
	{
		StringBuffer csvLine = new StringBuffer();

		if(city != null){
			if (city.getName().getTexts().isEmpty()) // we ensure that at least one line will be written
				city.getName().setText(Locale.ENGLISH.getLanguage(), city.getName().getText(Locale.ENGLISH.getLanguage()));

			for (Map.Entry<String, String> me : city.getName().getTexts()) {
				String languageID = me.getKey();
				String text = me.getValue();

				csvLine.append(city.getCountryID()).append(";");
				csvLine.append(city.getCityID()).append(";");
				csvLine.append(city.getRegion().getRegionID()).append(";");
				csvLine.append(languageID).append(";");
				csvLine.append(text == null ? "" : text);
				csvLine.append("\n");
			}
		}//if
		return csvLine.toString();
	}

	/**
	 * This method encodes a {@link Location} instance into
	 * one or more lines (one for each language) formatted
	 * according to the CSV file format.
	 *
	 * @param location The location to be encoded.
	 * @return A String containing one or more lines to be written to the CSV.
	 */
	protected static String location2csvLines(Location location)
	{
		StringBuffer csvLine = new StringBuffer();

		if (location.getName().getTexts().isEmpty()) // we ensure that at least one line will be written
			location.getName().setText(Locale.ENGLISH.getLanguage(), location.getName().getText(Locale.ENGLISH.getLanguage()));

		for (Map.Entry<String, String> me : location.getName().getTexts()) {
			String languageID = me.getKey();
			String text = me.getValue();

			csvLine.append(location.getCountryID()).append(";");
			csvLine.append(location.getLocationID()).append(";");
			csvLine.append(location.getCity().getCityID()).append(";");
			csvLine.append(location.getDistrict() == null ? "" : location.getDistrict().getDistrictID()).append(";");
			csvLine.append(languageID).append(";");
			csvLine.append(text == null ? "" : text);
			csvLine.append("\n");
		}

		return csvLine.toString();
	}

	/**
	 * This method encodes a {@link District} instance into
	 * one or more lines (one for each language) formatted
	 * according to the CSV file format.
	 *
	 * @param district The district to be encoded.
	 * @return A String containing one or more lines to be written to the CSV.
	 */
	protected static String district2csvLines(District district)
	{
		StringBuffer csvLine = new StringBuffer();

//		if (district.getName().getTexts().isEmpty()) // we ensure that at least one line will be written
//		district.getName().setText(Locale.ENGLISH.getLanguage(), district.getName().getText(Locale.ENGLISH.getLanguage()));

//		for (Map.Entry<String, String> me : district.getName().getTexts()) {
//		String languageID = me.getKey();
//		String text = me.getValue();

//		csvLine.append(district.getCountryID()).append(";");
//		csvLine.append(district.getDistrictID()).append(";");
//		csvLine.append(district.getCity().getCityID()).append(";");
//		csvLine.append(district.getDistrict() == null ? "" : district.getDistrict().getDistrictID()).append(";");
//		csvLine.append(languageID).append(";");
//		csvLine.append(text == null ? "" : text);
//		csvLine.append("\n");
//		}

		return csvLine.toString();
	}

	protected InputStream createCountryCSVInputStream()
	{
		String file = "resource/Data-Country.csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadCountries()
	{
		int row = 0;
		int countryCount = 0;
		int countryLangCount = 0;

		try {
			logger.info("Loading countries from CSV...");

			InputStream in = createCountryCSVInputStream();
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (row == 1)
							continue; // 1st line is header.

						String countryID = fields[0];
						String languageID = fields[1];
						String countryName = fields[2];

						if ("".equals(countryID))
							countryID = null;

						if (countryID != null) {
							if (!ObjectIDUtil.isValidIDString(countryID)) {
								logger.warn("Country-CSV, line "+row+": countryID \"" + countryID + "\" is not a valid ID String! Row ignored!");
								continue;
							}

							Country country = countries.get(countryID);
							if (country == null) {
								country = new Country(this, countryID);
								countries.put(country.getCountryID(), country);
								++countryCount;
							}

							country.getName().setText(languageID, countryName);
							++countryLangCount;
						}
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}
			logger.info("Read " +row+ " rows from country-CSV and added " + countryCount + " new countries & " + countryLangCount + " names.");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected InputStream createRegionCSVInputStream(String countryID)
	{
		String file = "resource/Data-Region-" + countryID + ".csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadRegions(String countryID)
	{
		Country country = countries.get(countryID);
		if (country == null) {
			logger.warn("Country with ID \""+countryID+"\" does not exist! Cannot load regions of this country.");
			return;
		}

		int row = 0;
		int regionCount = 0;
		int regionLangCount = 0;

		try {
			logger.info("Loading regions of country \""+countryID+"\"...");

			InputStream in = createRegionCSVInputStream(countryID);
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (fields.length != 4) {
							logger.warn("Region-CSV for countryID \""+countryID+"\": Invalid number of fields in row "+row+"! Row ignored.");
							continue;
						}

						if (row == 1)
							continue; // 1st line is header.

						String fcountryID = fields[0];
						String regionID = fields[1];
						String languageID = fields[2];
						String regionName = fields[3];

						if (!countryID.equals(fcountryID)) {
							logger.warn("Region-CSV for countryID \""+countryID+"\": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						if ("".equals(regionID))
							regionID = countryID;

						if (!ObjectIDUtil.isValidIDString(regionID)) {
							logger.warn("Region-CSV for countryID \""+countryID+"\", line "+row+": regionID \"" + regionID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						Region region = regions.get(
								Region.getPrimaryKey(countryID, csvOrganisationID, regionID));
						if (region == null) {
							region = new Region(this, csvOrganisationID, regionID, country);
							country.addRegion(region);
							regions.put(region.getPrimaryKey(), region);
							++regionCount;
						}
						if (!"".equals(regionName)) {
							region.getName().setText(languageID, regionName);
							++regionLangCount;
						}
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}
			logger.info("Read " +row+ " rows and added " + regionCount + " new regions & " + regionLangCount + " names for country \"" + countryID + "\".");

			if (regionCount == 0) {
				logger.info("The country \"" + countryID + "\" does not have regions. Creating dummy region with regionID=\""+countryID+"\"!");
				String regionID = countryID;
				Region region = new Region(this, csvOrganisationID, regionID, country);
				region.getName().copyFrom(country.getName());
				country.addRegion(region);
				regions.put(region.getPrimaryKey(), region);
			}

		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected InputStream createCityCSVInputStream(String countryID)
	{
		String file = "resource/Data-City-" + countryID + ".csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadCities(String countryID)
	{
		int row = 0;
		int cityCount = 0;
		int cityLangCount = 0;

		try {
			logger.info("Loading cities of country \""+countryID+"\"...");

			InputStream in = createCityCSVInputStream(countryID);
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (fields.length != 5) {
							logger.warn("City-CSV for countryID \""+countryID+"\": Invalid number of fields in row "+row+"! Row ignored.");
							continue;
						}

						if (row == 1)
							continue; // 1st line is header

						String fcountryID = fields[0];
						String cityID = fields[1];
						String regionID = fields[2];
						String languageID = fields[3];
						String cityName = fields[4];

						if (!countryID.equals(fcountryID)) {
							logger.warn("City-CSV for countryID \""+countryID+"\": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						if (!ObjectIDUtil.isValidIDString(cityID)) {
							logger.warn("City-CSV for countryID \""+countryID+"\", line "+row+": cityID \"" + cityID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						Country country = countries.get(countryID);
						if (country == null) {
							logger.warn("City-CSV for countryID \""+countryID+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						if ("".equals(regionID))
							regionID = countryID;

						String regionPK = Region.getPrimaryKey(countryID, csvOrganisationID, regionID);
						Region region = regions.get(regionPK);
						if (region == null) {
							logger.warn("City-CSV for countryID \""+countryID+"\", line "+row+": Region with PK \""+regionPK+"\" does not exist! Row ignored.");
							continue;
						}

						City city = cities.get(
								City.getPrimaryKey(countryID, csvOrganisationID, cityID));
						if (city == null) {
							city = new City(this, csvOrganisationID, cityID, region);
							region.addCity(city);
							cities.put(city.getPrimaryKey(), city);
							++cityCount;
						}
						city.getName().setText(languageID, cityName);
						++cityLangCount;
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}

			logger.info("Read " +row+ " rows and added " + cityCount + " new cities & " + cityLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected InputStream createDistrictCSVInputStream(String countryID)
	{
		String file = "resource/Data-District-" + countryID + ".csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadDistricts(String countryID)
	{
		int row = 0;
		int districtCount = 0;
		int districtLangCount = 0;

		try {
			logger.info("Loading districts of country \""+countryID+"\"...");

			InputStream in = createDistrictCSVInputStream(countryID);
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (fields.length != 7) {
							logger.warn("District-CSV for countryID \""+countryID+"\": Invalid number of fields ("+fields.length+") in row "+row+"! Row ignored.");
							continue;
						}

						if (row == 1)
							continue; // 1st line is header

						String fcountryID = fields[0];
						String cityID = fields[1];
						String districtID = fields[2];
						// TODO					String languageID = fields[3];
						String districtName = fields[4];
						String latitudeStr = fields[5];
						String longitudeStr = fields[6];

						if (!countryID.equals(fcountryID)) {
							logger.warn("District-CSV for countryID \""+countryID+"\": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						Country country = countries.get(countryID);
						if (country == null) {
							logger.warn("District-CSV for countryID \""+countryID+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						if ("".equals(districtID))
							districtID = cityID;

						if (!ObjectIDUtil.isValidIDString(districtID)) {
							logger.warn("District-CSV for countryID \""+countryID+"\", line "+row+": districtID \"" + districtID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						String cityPK = City.getPrimaryKey(countryID, csvOrganisationID, cityID);
						City city = (City) cities.get(cityPK);
						if (city == null) {
							logger.warn("District-CSV for countryID \""+countryID+"\", line "+row+": City with PK \""+cityPK+"\" does not exist! Row ignored.");
							continue;
						}

						if ("".equals(districtName))
							districtName = city.getName().getText(Locale.getDefault().getLanguage());

						double latitude = 0;
						try {
							latitude = Double.parseDouble(latitudeStr);
						} catch (NumberFormatException x) {
							logger.warn("District-CSV for countryID \""+countryID+"\", line "+row+": latitude \"" + latitudeStr + "\" is not a double! Setting latitude = 0.");
						}

						double longitude = 0;
						try {
							longitude = Double.parseDouble(longitudeStr);
						} catch (NumberFormatException x) {
							logger.warn("District-CSV for countryID \""+countryID+"\", line "+row+": longitude \"" + longitudeStr + "\" is not a double! Setting longitude = 0.");
						}


						District district = districts.get(
								District.getPrimaryKey(countryID, csvOrganisationID, districtID));
						if (district == null) {
							district = new District(this, csvOrganisationID, districtID, city);
							city.addDistrict(district);
							districts.put(district.getPrimaryKey(), district);
							++districtCount;
						}
						//					district.getName().setText(languageID, districtName);
						district.setName(districtName);
						district.setLatitude(latitude);
						district.setLongitude(longitude);
						++districtLangCount;
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}

			logger.info("Read " +row+ " rows and added " + districtCount + " new districts & " + districtLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected InputStream createZipCSVInputStream(String countryID)
	{
		String file = "resource/Data-Zip-" + countryID + ".csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadZips(String countryID)
	{
		int row = 0;
		int zipCount = 0;

		try {
			logger.info("Loading zips of country \""+countryID+"\"...");

			InputStream in = createZipCSVInputStream(countryID);
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (fields.length != 4) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\": Invalid number of fields in row "+row+"! Row ignored.");
							continue;
						}

						if (row == 1)
							continue; // 1st line is header

						String fcountryID = fields[0];
						String cityID = fields[1];
						String districtID = fields[2];
						String zip = fields[3];

						if (!countryID.equals(fcountryID)) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						if ("".equals(districtID))
							districtID = cityID;

						if ("".equals(zip)) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\", line "+row+": zip field is empty! Row ignored!");
							continue;
						}

						Country country = countries.get(countryID);
						if (country == null) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						if (!ObjectIDUtil.isValidIDString(cityID)) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\", line "+row+": cityID \"" + cityID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						String districtPK = District.getPrimaryKey(countryID, csvOrganisationID, districtID);
						District district = (District) districts.get(districtPK);
						if (district == null) {
							logger.warn("Zip-CSV for countryID \""+countryID+"\", line "+row+": District with PK \""+districtPK+"\" does not exist! Row ignored.");
							continue;
						}

						if (!district.getCity().getCityID().equals(cityID)) {
							String languageID = Locale.getDefault().getLanguage();
							City csvCity = (City) cities.get(City.getPrimaryKey(countryID, csvOrganisationID, cityID));
							String csvCityName = csvCity == null ? "{unknown city}" : csvCity.getName().getText(languageID);

							logger.warn("Zip-CSV for countryID \""+countryID+"\", line "+row+": District with PK \""+districtPK+"\" (named \""+district.getName()+"\") has cityID \""+district.getCity().getCityID()+"\" (named \""+district.getCity().getName().getText(languageID)+"\") but csv row declares cityID \""+cityID+"\" (named \"" + csvCityName + "\")! Will add zip \""+zip+"\" to district \""+districtPK+"\" anyway.");
						}

						district.addZip(zip);
						++zipCount;
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}

			logger.info("Read " +row+ " rows and added " + zipCount + " new zips for country \"" + countryID + "\".");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected InputStream createLocationCSVInputStream(String countryID)
	{
		String file = "resource/Data-Location-" + countryID + ".csv";
		InputStream in = Geography.class.getResourceAsStream(file);
		if (in == null)
			logger.warn("File \"" + file + "\" does not exist!");

		return in;
	}

	@Implement
	protected void loadLocations(String countryID)
	{
		int row = 0;
		int locationCount = 0;
		int locationLangCount = 0;

		try {
			logger.info("Loading locations of country \""+countryID+"\"...");

			InputStream in = createLocationCSVInputStream(countryID);
			if (in == null)
				return;

			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						++row;

						if ("".equals(line) || line.startsWith("#"))
							continue;

						String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

						if (fields.length != 6) {
							logger.warn("Location-CSV for countryID \""+countryID+"\": Invalid number of fields in row "+row+"! Row ignored.");
							continue;
						}

						if (row == 1)
							continue; // 1st line is header

						String fcountryID = fields[0];
						String locationID = fields[1];
						String cityID = fields[2];
						String districtID = fields[3];
						String languageID = fields[4];
						String locationName = fields[5];

						if (!countryID.equals(fcountryID)) {
							logger.warn("Location-CSV for countryID \""+countryID+"\": Row "+row+" declares the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						if (!ObjectIDUtil.isValidIDString(locationID)) {
							logger.warn("Location-CSV for countryID \""+countryID+"\", line "+row+": locationID \"" + locationID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						String locationPK = Location.getPrimaryKey(countryID, csvOrganisationID, locationID);

						Country country = countries.get(countryID);
						if (country == null) {
							logger.warn("Location-CSV for countryID \""+countryID+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						String cityPK = City.getPrimaryKey(countryID, csvOrganisationID, cityID);
						City city = cities.get(cityPK);
						if (city == null) {
							logger.warn("Location-CSV for countryID \""+countryID+"\", line "+row+": City with PK \""+cityPK+"\" does not exist! Row ignored.");
							continue;
						}

						District district = null;
						if (!"".equals(districtID)) {
							String districtPK = District.getPrimaryKey(countryID, csvOrganisationID, districtID);
							district = districts.get(districtPK);
							if (district == null)
								logger.warn("Location-CSV for countryID \""+countryID+"\", line "+row+": District with PK \""+districtPK+"\" does not exist! Will NOT assign a district to location \""+locationPK+"\"!");
						}

						Location location = (Location) locations.get(
								Location.getPrimaryKey(countryID, csvOrganisationID, locationID));
						if (location == null) {
							location = new Location(this, csvOrganisationID, locationID, city);
							city.addLocation(location);
							locations.put(location.getPrimaryKey(), location);
							++locationCount;
						}
						location.getName().setText(languageID, locationName);
						++locationLangCount;
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}

			logger.info("Read " +row+ " rows and added " + locationCount + " new locations & " + locationLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
