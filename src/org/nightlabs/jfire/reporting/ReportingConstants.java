/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingConstants {
	
	public static final String DESCRIPTOR_FILE 						= "content.xml"; //$NON-NLS-1$
	
	public static final String DESCRIPTOR_FILE_ENCODING				= "utf-8"; //$NON-NLS-1$
	public static final String DESCRIPTOR_FILE_DOCTYPE_SYSTEM		= "http://www.nightlabs.de/dtd/reporting-initialiser-content_0_5.dtd"; //$NON-NLS-1$
	public static final String DESCRIPTOR_FILE_DOCTYPE_PUBLIC		= "-//NightLabs//Reporting Initialiser DTD V 0.5//EN"; //$NON-NLS-1$
	
	public static final String REPORT_CATEGORY_ELEMENT 				= "report-category"; //$NON-NLS-1$
	public static final String REPORT_CATEGORY_ELEMENT_ATTRIBUTE_ID 		= "id"; //$NON-NLS-1$
	public static final String REPORT_CATEGORY_ELEMENT_ATTRIBUTE_TYPE 		= "type"; //$NON-NLS-1$
	
	public static final String REPORT_CATEGORY_ELEMENT_NAME 		= "name"; //$NON-NLS-1$
	
	public static final String REPORT_ELEMENT 						= "report"; //$NON-NLS-1$
	public static final String REPORT_ELEMENT_ATTRIBUTE_FILE 		= "file"; //$NON-NLS-1$
	public static final String REPORT_ELEMENT_ATTRIBUTE_ID 			= "id"; //$NON-NLS-1$
	public static final String REPORT_ELEMENT_ATTRIBUTE_ENGINE_TYPE = "engineType"; //$NON-NLS-1$
	public static final String REPORT_ELEMENT_ATTRIBUTE_OVERWRITE_ON_INIT 	= "overwriteOnInit"; //$NON-NLS-1$
	
	public static final String REPORT_ELEMENT_NAME 					= "name"; //$NON-NLS-1$
	public static final String REPORT_ELEMENT_DESCRIPTION 			= "description"; //$NON-NLS-1$
	
	public static final String PARAMETER_ACQUISITION_ELEMENT 		= "parameter-acquisition"; //$NON-NLS-1$
	public static final String USE_CASE_ELEMENT						= "use-case"; //$NON-NLS-1$
	public static final String USE_CASE_ATTRIBUTE_ID				= "id"; //$NON-NLS-1$
	public static final String USE_CASE_ATTRIBUTE_DEFAULT			= "default"; //$NON-NLS-1$
	
	public static final String USE_CASE_ELEMENT_NAME				= "name"; //$NON-NLS-1$
	public static final String USE_CASE_ELEMENT_DESCRIPTION			= "description"; //$NON-NLS-1$
	
	public static final String PARAMETER_ELEMENT							= "parameter"; //$NON-NLS-1$
	public static final String PARAMETER_ELEMENT_ATTRIBUTE_ID				= "id"; //$NON-NLS-1$
	public static final String PARAMETER_ELEMENT_ATTRIBUTE_NAME				= "name"; //$NON-NLS-1$
	public static final String PARAMETER_ELEMENT_ATTRIBUTE_TYPE				= "type"; //$NON-NLS-1$
	public static final String PARAMETER_ELEMENT_ATTRIBUTE_X				= "x"; //$NON-NLS-1$
	public static final String PARAMETER_ELEMENT_ATTRIBUTE_Y				= "y"; //$NON-NLS-1$
	
	public static final String VALUE_PROVIDER_CONFIGS_ELEMENT		= "value-provider-configs"; //$NON-NLS-1$
	
	public static final String PROVIDER_CONFIG_ELEMENT								= "provider-config"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ID					= "id"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ORGANISATION_ID	= "organisationID"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_CATEGORY_ID		= "categoryID"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_VALUE_PROVIDER_ID	= "valueProviderID"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_INDEX			= "pageIndex"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_ROW			= "pageRow"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_COLUMN		= "pageColumn"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ALLOW_NULL_OUTPUT_VALUE	= "allowNullOutputValue"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_SHOW_MESSAGE_IN_HEADER 	= "showMessageInHeader"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_GROW_VERTICALLY			= "growVertically"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_X							= "x"; //$NON-NLS-1$
	public static final String PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_Y							= "y"; //$NON-NLS-1$
	
	public static final String PROVIDER_CONFIG_ELEMENT_MESSAGE						= "message"; //$NON-NLS-1$
	
	public static final String VALUE_CONSUMER_BINDINGS_ELEMENT						= "value-consumer-bindings"; //$NON-NLS-1$
		
	public static final String VALUE_CONSUMER_BINDING_ELEMENT						= "value-consumer-binding"; //$NON-NLS-1$
	
	public static final String BINDING_PROVIDER_ELEMENT								= "binding-provider"; //$NON-NLS-1$
	public static final String BINDING_PROVIDER_ELEMENT_ATTRIBUTE_ID				= "id"; //$NON-NLS-1$
	
	public static final String BINDING_PARAMETER_ELEMENT							= "binding-parameter"; //$NON-NLS-1$
	public static final String BINDING_PARAMETER_ELEMENT_ATTRIBUTE_NAME				= "name"; //$NON-NLS-1$
	
	public static final String BINDING_CONSUMER_ELEMENT								= "binding-consumer"; //$NON-NLS-1$
	public static final String BINDING_CONSUMER_ELEMENT_ATTRIBUTE_ID				= "id"; //$NON-NLS-1$
	
	
	// TODO: Add things like Geography data
	// TODO: Also need Collections of all things here
	

	/**
	 * ValueProvider category for simple types like String, Intgeger etc.
	 */
	public static final ValueProviderCategoryID VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES = ValueProviderCategoryID.create(
			Organisation.DEV_ORGANISATION_ID,
			"JFireReporting-ValueProviderCategory-SimpleTypes"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link String} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_STRING = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			String.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries an {@link Integer} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_INTEGER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			Integer.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries an {@link BigDecimal} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_BIG_DECIMAL = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			BigDecimal.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Double} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_DOUBLE = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			Double.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Date} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_DATE = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			Date.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link TimePeriod} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_TIME_PERIOD = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			TimePeriod.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Boolean} value from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_BOOLEAN = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES.valueProviderCategoryID,
			Boolean.class.getName()
		);
	
	/**
	 * ValueProvider category for JFire objects like Users, UserGroups, Organisations etc.
	 */
	public static final ValueProviderCategoryID VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS = ValueProviderCategoryID.create(
			Organisation.DEV_ORGANISATION_ID,
			"JFireReporting-ValueProviderCategory-JFireObjects"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link UserID} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_USER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			User.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link UserID} from the user but has the current user preselected.
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_CURRENT_USER = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			User.class.getName() + "#current"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a list of {@link UserID}s from the user but has the current user preselected.
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_CURRENT_USER_MULTIPLE = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Collection.class.getName() + "<" + User.class.getName() + ">" + "#current"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Collection} of {@link UserID}s from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_USERS = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Collection.class.getName() + "<" + User.class.getName() + ">"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link OrganisationID} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_ORGANISATION = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Organisation.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link WorkstationID} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_WORKSTATION = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Workstation.class.getName()
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link WorkstationID} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_CURRENT_WORKSTATION = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Workstation.class.getName() + "#current"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Collection} of {@link WorkstationID} from the user
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_WORKSTATIONS = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID,
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID,
			Collection.class.getName() + "<" + Workstation.class.getName() + ">"
		);
}
