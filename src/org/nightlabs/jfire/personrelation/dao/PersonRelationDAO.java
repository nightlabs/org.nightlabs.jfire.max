package org.nightlabs.jfire.personrelation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.PersonRelation;
import org.nightlabs.jfire.personrelation.PersonRelationManagerRemote;
import org.nightlabs.jfire.personrelation.PersonRelationType;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class PersonRelationDAO
extends BaseJDOObjectDAO<PersonRelationID, PersonRelation>
{
	private static PersonRelationDAO sharedInstance;

	public static PersonRelationDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PersonRelationDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PersonRelationDAO();
			}
		}
		return sharedInstance;
	}

	protected PersonRelationDAO() { }

	@Override
	protected Collection<? extends PersonRelation> retrieveJDOObjects(
			Set<PersonRelationID> personRelationIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	) throws Exception
	{
		monitor.beginTask("Retrieving person relations", 100);
		try {
			PersonRelationManagerRemote ejb = this.ejb;
			if (ejb == null)
				ejb = JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties());

			monitor.worked(5);

			return ejb.getPersonRelations(personRelationIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(95);
			monitor.done();
		}
	}

	private PersonRelationManagerRemote ejb;

	/**
	 * Get all those {@link PersonRelation}s that are of a certain {@link PersonRelationType type}
	 * (if specified, otherwise all) and between two persons. It is possible to specify only one
	 * or even no person at all in the filtering. At least one criteria must be specified, though.
	 *
	 * @param personRelationTypeID identifier of the {@link PersonRelationType} or <code>null</code>.
	 * @param fromPersonID identifier of the {@link Person} on the "from"-side of the relation or <code>null</code>.
	 * @param toPersonID identifier of the {@link Person} on the "to"-side of the relation or <code>null</code>.
	 * @param fetchGroups the fetch-groups to use for detachment.
	 * @param maxFetchDepth the maximal fetch-depth of the complete object graph.
	 * @param monitor the monitor for progress feed-back.
	 * @return the {@link PersonRelation}s that match the given criteria.
	 */
	public synchronized List<PersonRelation> getPersonRelations(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading person relations", 100);
		try {
			ejb = JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				monitor.worked(10);
				Collection<PersonRelationID> personRelationIDs = ejb.getPersonRelationIDs(
						personRelationTypeID, fromPersonID, toPersonID
				);
				monitor.worked(20);
				return getJDOObjects(
						null, personRelationIDs,
						fetchGroups, maxFetchDepth,
						new SubProgressMonitor(monitor, 70)
				);
			} finally {
				ejb = null;
			}
		} finally {
			monitor.done();
		}
	}

	public synchronized long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading person relation count", 100);
		try {
			PersonRelationManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(10);
			return ejb.getPersonRelationCount(personRelationTypeID, fromPersonID, toPersonID);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	public void createPersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties()).createPersonRelation(
				personRelationTypeID, fromPersonID, toPersonID
		);
	}

	public void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties()).deletePersonRelation(
				personRelationTypeID, fromPersonID, toPersonID
		);
	}
}
