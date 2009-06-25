package org.nightlabs.jfire.personrelation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.personrelation.PersonRelationManagerRemote;
import org.nightlabs.jfire.personrelation.PersonRelationType;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class PersonRelationTypeDAO
extends BaseJDOObjectDAO<PersonRelationTypeID, PersonRelationType>
{
	private static PersonRelationTypeDAO sharedInstance;

	public static PersonRelationTypeDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PersonRelationTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PersonRelationTypeDAO();
			}
		}
		return sharedInstance;
	}

	protected PersonRelationTypeDAO() { }

	@Override
	protected Collection<? extends PersonRelationType> retrieveJDOObjects(
			Set<PersonRelationTypeID> personRelationTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	) throws Exception
	{
		monitor.beginTask("Retrieving person relations", 100);
		try {
			PersonRelationManagerRemote ejb = this.ejb;
			if (ejb == null)
				ejb = JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties());

			monitor.worked(5);

			return ejb.getPersonRelationTypes(personRelationTypeIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(95);
			monitor.done();
		}
	}

	private PersonRelationManagerRemote ejb;

	public synchronized List<PersonRelationType> getPersonRelationTypes(
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading person relations", 100);
		try {
			ejb = JFireEjb3Factory.getRemoteBean(PersonRelationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				monitor.worked(10);
				Collection<PersonRelationTypeID> personRelationTypeIDs = ejb.getPersonRelationTypeIDs();
				monitor.worked(20);
				return getJDOObjects(
						null, personRelationTypeIDs,
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
}
