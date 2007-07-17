/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportParameterUtil {

	private static Logger logger = Logger.getLogger(ReportParameterUtil.class);
	
	public static class NameEntry {
		String language;
		String name;
		public NameEntry(String language, String name) {
			this.language = language;
			this.name = name;
		}
	}


	public static ValueProviderCategory createValueProviderCategory(
			PersistenceManager pm, ValueProviderCategory parent, ValueProviderCategoryID categoryID,
			NameEntry[] names)
	{
		// initialise meta-data
		pm.getExtent(ValueProviderCategory.class);

		ValueProviderCategory category = null;
		try {
			category = (ValueProviderCategory) pm.getObjectById(categoryID);
			logger.debug("Have ValueProviderCategory "+categoryID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProviderCategory "+categoryID);
			category = new ValueProviderCategory(parent, categoryID.organisationID, categoryID.valueProviderCategoryID, true);
			category = (ValueProviderCategory) pm.makePersistent(category);
			for (NameEntry entry : names) {
				category.getName().setText(entry.language, entry.name);
			}
			logger.debug("Created ValueProviderCategory "+categoryID);
		}
		return category;
	}

	public static ValueProvider createValueProvider(
			PersistenceManager pm, ValueProviderCategory category, ValueProviderID valueProviderID, String outputType,
			NameEntry[] names, NameEntry[] descriptions, NameEntry[] defaultMessages
	)
	{
		// initialise meta-data
		pm.getExtent(ValueProvider.class);

		ValueProvider valueProvider = null;
		try {
			valueProvider = (ValueProvider) pm.getObjectById(valueProviderID);
			logger.debug("Have ValueProvider "+valueProviderID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProvider "+valueProviderID);
			valueProvider = new ValueProvider(category, valueProviderID.valueProviderID, outputType);
			valueProvider = (ValueProvider) pm.makePersistent(valueProvider);
			for (NameEntry entry : names) {
				valueProvider.getName().setText(entry.language, entry.name);
			}
			for (NameEntry entry : descriptions) {
				valueProvider.getDescription().setText(entry.language, entry.name);
			}
			for (NameEntry entry : defaultMessages) {
				valueProvider.getDefaultMessage().setText(entry.language, entry.name);
			}
			logger.debug("Created ValueProvider "+valueProviderID);
		}
		return valueProvider;
	}

}
