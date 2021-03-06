package org.nightlabs.jfire.personrelation.dao;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.relation.RelationType;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.PersonRelation;
import org.nightlabs.jfire.personrelation.PersonRelationComparator;
import org.nightlabs.jfire.personrelation.PersonRelationFilterCriteria;
import org.nightlabs.jfire.personrelation.PersonRelationManagerRemote;
import org.nightlabs.jfire.personrelation.PersonRelationManagerRemote.TuckedQueryCount;
import org.nightlabs.jfire.personrelation.PersonRelationType;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;
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
				ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);

			monitor.worked(5);

			return ejb.getPersonRelations(personRelationIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(95);
			monitor.done();
		}
	}

	private PersonRelationManagerRemote ejb;

	public PersonRelation getPersonRelation(
			PersonRelationID personRelationID,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		return getJDOObject(
				null, personRelationID,
				fetchGroups, maxFetchDepth,
				monitor
		);
	}

	public List<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	)
	{
		return getJDOObjects(
				null, personRelationIDs,
				fetchGroups, maxFetchDepth,
				monitor
		);
	}

	/**
	 * @deprecated Use {@link #getPersonRelationIDs(PersonRelationFilterCriteria, ProgressMonitor)}
	 */
	@Deprecated
	public synchronized Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading person relation IDs", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			Collection<PersonRelationID> personRelationIDs = ejb.getPersonRelationIDs(
					personRelationTypeID, fromPersonID, toPersonID
			);
			monitor.worked(90);
			return personRelationIDs;
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see PersonRelationManagerRemote#getPersonRelationIDs(PersonRelationFilterCriteria)
	 */
	public synchronized Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationFilterCriteria filterCriteria,
			ProgressMonitor monitor
	) {
		monitor.beginTask("Loading person relation IDs", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			Collection<PersonRelationID> personRelationIDs = ejb.getPersonRelationIDs(filterCriteria);
			monitor.worked(90);
			return personRelationIDs;
		} finally {
			monitor.done();
		}
	}
	
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	/**
	 * @deprecated Use {@link #getPersonRelationIDs(PersonRelationFilterCriteria, ProgressMonitor)}
	 */
	@Deprecated
	public synchronized Collection<PersonRelationID> getFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude,
			PersonRelationComparator personRelationComparator, // Leave this null to suggest no sorting
			ProgressMonitor monitor
	) {
		monitor.beginTask("Loading person relation IDs", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			Collection<PersonRelationID> personRelationIDs = ejb.getFilteredPersonRelationIDs(
					personRelationTypeID, fromPersonID, toPersonID, fromPropertySetIDsToExclude, toPropertySetIDsToExclude, personRelationComparator
			);
			monitor.worked(90);
			return personRelationIDs;
		} finally {
			monitor.done();
		}
	}

	/**
	 * @deprecated Use {@link #getPersonRelationCount(PersonRelationFilterCriteria, ProgressMonitor)}
	 */
	@Deprecated
	public synchronized long getFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude,
			ProgressMonitor monitor
	) {
		monitor.beginTask("Loading person relation count", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			return ejb.getFilteredPersonRelationCount(personRelationTypeID, fromPersonID, toPersonID, fromPropertySetIDsToExclude, toPropertySetIDsToExclude);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	public synchronized Collection<PersonRelationID> getInclusiveFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToInclude,
			Set<PropertySetID> toPropertySetIDsToInclude,
			PersonRelationComparator personRelationComparator, // Leave this null to suggest no sorting
			ProgressMonitor monitor
	) {
		monitor.beginTask("Loading person relation IDs", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			Collection<PersonRelationID> personRelationIDs = ejb.getInclusiveFilteredPersonRelationIDs(
					personRelationTypeID, fromPersonID, toPersonID, fromPropertySetIDsToInclude, toPropertySetIDsToInclude, personRelationComparator
			);
			monitor.worked(90);
			return personRelationIDs;
		} finally {
			monitor.done();
		}
	}

	public synchronized long getInclusiveFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToInclude,
			Set<PropertySetID> toPropertySetIDsToInclude,
			ProgressMonitor monitor
	) {
		monitor.beginTask("Loading person relation count", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			return ejb.getInclusiveFilteredPersonRelationCount(personRelationTypeID, fromPersonID, toPersonID, fromPropertySetIDsToInclude, toPropertySetIDsToInclude);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	// Test. May start this on a new class, since the methods are more intended to be used to serve TuckedPersonRelationTreeNodes.
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	public synchronized TuckedQueryCount getTuckedPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID currentID,
			Set<PropertySetID> propertySetIDsToRoot,
			Set<PropertySetID> propertySetIDsToTuckedChildren,
			ProgressMonitor monitor
	) {
		// Note(s):
		//    I. The input parameter currentID represents the TuckedNode we are dealing with.
		//   II. The given propertySetIDsToRoot are the IDs we want to avoid, in order to avoid open-ended relations.
		//  III. The given propertySetIDsToTuckedChildrean are the IDs we want to seek to keep in the tucked status, if and only if they exist.
		monitor.beginTask("Loading tucked person relation count...", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			return ejb.getTuckedPersonRelationCount(personRelationTypeID, currentID, propertySetIDsToRoot, propertySetIDsToTuckedChildren);

		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|


	/**
	 * Get all those {@link PersonRelation}s that are of the given criteria.
	 * 
	 * @see PersonRelationManagerRemote#getPersonRelationIDs(PersonRelationFilterCriteria).
	 * 
	 * @return the {@link PersonRelation}s that match the given criteria.
	 */
	public synchronized List<PersonRelation> getPersonRelations(PersonRelationFilterCriteria filterCriteria, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading person relations", 100);
		try {
			ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			try {
				monitor.worked(10);
				Collection<PersonRelationID> personRelationIDs = ejb.getPersonRelationIDs(filterCriteria);
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
			ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
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

	/**
	 * FIXME The comments for this method needs to be updated!
	 *
	 * Returns the paths to the Persons that correspond to the nodes in the relation graph that are farthest away from the
	 * startingPoint and connected via intermediary nodes only by the allowed relationType(ID)s. Or in case the maxDepth
	 * is reached, the elements of that iteration are returned.
	 *
	 * @param allowedRelations The ids of the {@link RelationType}s that represent the allowed edges in the graph.
	 * @param ofPerson The source from which to search for the farthest nodes.
	 * @param maxDepth The maximum depth (distance) until which the search is continued.
	 * @param monitor the monitor for progress feed-back.
	 * @return the PersonIDs that correspond to the nodes in the relation graph that are farthest away from the
	 * startingPoint and connected via intermediary nodes only by the allowed relationType(ID)s. Or in case the maxDepth
	 * is reached, the elements of that iteration are returned.
	 */
	public synchronized Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getRelationRootNodes(Set<PersonRelationTypeID> allowedRelations,
			PropertySetID ofPerson, int maxDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Finding related persons", 10);
		try
		{
			ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(2);
			Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> nearestNodes = ejb.getRootNodes(allowedRelations, ofPerson, maxDepth);
			monitor.worked(8);
			monitor.done();
			return nearestNodes;
		}
		catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException("Error finding related persons", e);
		}
		finally
		{
			ejb = null;
		}
	}
	
	public synchronized List<PropertySetID> getRelationRootNodes(Set<PersonRelationTypeID> allowedRelations,
			Set<PropertySetID> personIDs, int maxDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Finding related persons", 10);
		try
		{
			ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(2);
			List<PropertySetID> result = ejb.getRootNodes(allowedRelations, personIDs, maxDepth);
			monitor.worked(8);
			monitor.done();
			return result;
		}
		catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException("Error finding related persons", e);
		}
		finally
		{
			ejb = null;
		}
	}
	

	/**
	 * @see PersonRelationManagerRemote#getPersonRelationIDs(PersonRelationFilterCriteria).
	 */
	public synchronized long getPersonRelationCount(PersonRelationFilterCriteria filterCriteria, ProgressMonitor monitor) {
		monitor.beginTask("Loading person relation count", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
			monitor.worked(10);
			return ejb.getPersonRelationCount(filterCriteria);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	/**
	 * @deprecated Use {@link #getPersonRelationCount(PersonRelationFilterCriteria, ProgressMonitor)}.
	 */
	@Deprecated
	public synchronized long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading person relation count", 100);
		try {
			PersonRelationManagerRemote ejb = getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class);
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
			PropertySetID toPersonID,
			ProgressMonitor monitor
	)
	{
		monitor.beginTask("Creating person relation", 100);
		try {
			getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class).createPersonRelation(
					personRelationTypeID, fromPersonID, toPersonID
			);
		} finally {
			monitor.worked(100);
			monitor.done();
		}
	}

	public void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID, ProgressMonitor monitor
	)
	{
		monitor.beginTask("Deleting person relation", 100);
		try {
			getEjbProvider().getRemoteBean(PersonRelationManagerRemote.class).deletePersonRelation(
					personRelationTypeID, fromPersonID, toPersonID
			);
		} finally {
			monitor.worked(100);
			monitor.done();
		}
	}
}
