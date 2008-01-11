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
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingConstants {

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
	 * ValueProvider id for a value provider that queries a {@link Collection} of {@link UserID}s from the user 
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_USERS = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, 
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID, 
			Collection.class.getName() + "<" + User.class.getName() + ">"
		);
	
	/**
	 * ValueProvider id for a value provider that queries a {@link UserID} for a {@link UserGroup} from the user 
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_USER_GROUP = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, 
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID, 
			UserGroup.class.getName()
		);
	
	// Current Usergroup not possible, as as the current User can be in multiple user groups.
	
	/**
	 * ValueProvider id for a value provider that queries a {@link Collection} of {@link UserID}s from the user 
	 */
	public static final ValueProviderID VALUE_PROVIDER_ID_USER_GROUPS = ValueProviderID.create(
			Organisation.DEV_ORGANISATION_ID, 
			ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS.valueProviderCategoryID, 
			Collection.class.getName() + "<" + UserGroup.class.getName() + ">"
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
