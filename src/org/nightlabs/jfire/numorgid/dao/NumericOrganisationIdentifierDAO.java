package org.nightlabs.jfire.numorgid.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.numorgid.NumericOrganisationIdentifier;
import org.nightlabs.jfire.numorgid.NumericOrganisationIdentifierManager;
import org.nightlabs.jfire.numorgid.NumericOrganisationIdentifierManagerUtil;
import org.nightlabs.jfire.numorgid.UnknownNumericOrganisationIdentifierException;
import org.nightlabs.jfire.numorgid.UnknownOrganisationException;
import org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class NumericOrganisationIdentifierDAO extends BaseJDOObjectDAO<NumericOrganisationIdentifierID, NumericOrganisationIdentifier>
{
	private static NumericOrganisationIdentifierDAO sharedInstance;
	public static NumericOrganisationIdentifierDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new NumericOrganisationIdentifierDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<NumericOrganisationIdentifier> retrieveJDOObjects(
			Set<NumericOrganisationIdentifierID> objectIDs,
			String[] fetchGroups,
			int maxFetchDepth,
			ProgressMonitor monitor)
	throws Exception
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected NumericOrganisationIdentifier retrieveJDOObject(
			NumericOrganisationIdentifierID objectID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Loading numeric organisation identifier", 100);
		try {
			NumericOrganisationIdentifierManager m = NumericOrganisationIdentifierManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(50);
			return m.getNumericOrganisationIdentifier(objectID);
		} finally {
			monitor.worked(50);
			monitor.done();
		}
	}

	/**
	 * Get the numeric organisation-id for the given qualified <code>organisationID</code>.
	 *
	 * @param organisationID the qualified organisation-id - i.e. a unique String-identifier similar to a host name.
	 * @param monitor the monitor for progress feed back.
	 * @return the numeric organisation-id.
	 * @throws UnknownOrganisationException if there is no organisation with the given qualified <code>organisationID</code> registered in the network's root-organisation.
	 */
	public int getNumericOrganisationID(String organisationID, ProgressMonitor monitor)
	throws UnknownOrganisationException
	{
		NumericOrganisationIdentifierID id = NumericOrganisationIdentifierID.create(organisationID);
		return getJDOObject(null, id, null, 1, monitor).getNumericOrganisationID();
	}

	/**
	 * Get the qualified organisation-id (i.e. a unique String-identifier similar to a host name) of the organisation
	 * having the numeric alias specified by the given <code>numericOrganisationID</code>.
	 *
	 * @param numericOrganisationID the numeric organisation-id (i.e. a numeric alias) of a certain organisation.
	 * @param monitor the monitor for progress feed back.
	 * @return the qualified organisation-id.
	 * @throws UnknownNumericOrganisationIdentifierException if there is no organisation registered with the given numeric organisation-id.
	 */
	public String getQualifiedOrganisationID(int numericOrganisationID, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading numeric organisation identifier", 100);
		try {
			String cacheScope = NumericOrganisationIdentifierDAO.class.getName() + "#getQualifiedOrganisationID";
			Integer objectID = new Integer(numericOrganisationID);

			NumericOrganisationIdentifier numericOrganisationIdentifier = (NumericOrganisationIdentifier) getCache().get(cacheScope, objectID, (String[])null, 1);
			if (numericOrganisationIdentifier != null)
				monitor.worked(50);
			else {
				NumericOrganisationIdentifierManager m;
				try {
					m = NumericOrganisationIdentifierManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					monitor.worked(50);
					numericOrganisationIdentifier = m.getNumericOrganisationIdentifier(numericOrganisationID);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				getCache().put(cacheScope, objectID, numericOrganisationIdentifier, (String[])null, 1);
			}
			return numericOrganisationIdentifier.getOrganisationID();
		} finally {
			monitor.worked(50);
			monitor.done();
		}
	}
}
