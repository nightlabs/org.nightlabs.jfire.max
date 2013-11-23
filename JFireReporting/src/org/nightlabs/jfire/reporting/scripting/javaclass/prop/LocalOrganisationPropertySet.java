/**
 *
 */
package org.nightlabs.jfire.reporting.scripting.javaclass.prop;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySetMetaData;

/**
 * Reporting data-set script that returns the data-fields of the Person assigned to the local organisation.
 *
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class LocalOrganisationPropertySet extends PropertySet {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Inherits and returns empty meta-data (no properties needed).
	 * </p>
	 */
	@Override
	public IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData() {
		return new JFSQueryPropertySetMetaData();
	}

	@Override
	protected IStruct getStruct() {
		return PersonStruct.getPersonStructLocal(getPersistenceManager());
//		return StructLocal.getStructLocal(Person.class, Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE, getPersistenceManager());
	}

	@Override
	protected org.nightlabs.jfire.prop.PropertySet getPropertySet() {
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(getPersistenceManager());
		return localOrganisation.getOrganisation().getPerson();
	}
}
