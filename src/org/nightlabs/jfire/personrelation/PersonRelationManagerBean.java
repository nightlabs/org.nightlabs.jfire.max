package org.nightlabs.jfire.personrelation;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.management.relation.RelationType;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PersonRelationManagerBean
extends BaseSessionBeanImpl
implements PersonRelationManagerRemote
{
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			String baseName = "org.nightlabs.jfire.personrelation.resource.messages"; //$NON-NLS-1$
			ClassLoader loader = PersonRelationManagerBean.class.getClassLoader();
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.friend;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, null));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-friend.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-friend.description"); //$NON-NLS-1$
				}
			}

			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.employing;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.employed));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-employing.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-employing.description"); //$NON-NLS-1$
				}
			}
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.employed;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.employing));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-employed.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-employed.description"); //$NON-NLS-1$
				}
			}

			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.parent;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.child));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-parent.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-parent.description"); //$NON-NLS-1$
				}
			}
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.child;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.parent));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-child.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-child.description"); //$NON-NLS-1$
				}
			}

			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.companyGroup;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.subsidiary));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-companyGroup.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-companyGroup.description"); //$NON-NLS-1$
				}
			}
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.subsidiary;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.companyGroup));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-subsidiary.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-subsidiary.description"); //$NON-NLS-1$
				}
			}

			{
				// TODO: do we need an inverse relation for that?
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.branchOffice;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, null));
					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchOffice.name"); //$NON-NLS-1$
					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchOffice.description"); //$NON-NLS-1$
				}
			}

		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationTypeID> getPersonRelationTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(PersonRelationType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			@SuppressWarnings("unchecked")
			Collection<PersonRelationTypeID> c = (Collection<PersonRelationTypeID>) q.execute();
			return new HashSet<PersonRelationTypeID>(c);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationType> getPersonRelationTypes(
			Collection<PersonRelationTypeID> personRelationTypeIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, personRelationTypeIDs, PersonRelationType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@Override
	public long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			return PersonRelation.getPersonRelationCount(pm, personRelationType, fromPerson, toPerson);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			return NLJDOHelper.getObjectIDList(relations);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, personRelationIDs, PersonRelation.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public void createPersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		if (personRelationTypeID == null)
			throw new IllegalArgumentException("personRelationTypeID must not be null!");

		if (fromPersonID == null)
			throw new IllegalArgumentException("fromPersonID must not be null!");

		if (toPersonID == null)
			throw new IllegalArgumentException("toPersonID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) pm.getObjectById(personRelationTypeID);
			Person fromPerson = (Person) pm.getObjectById(fromPersonID);
			Person toPerson = (Person) pm.getObjectById(toPersonID);

			personRelationType.createPersonRelation(fromPerson, toPerson);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		if (personRelationTypeID == null)
			throw new IllegalArgumentException("personRelationTypeID must not be null!");

		if (fromPersonID == null)
			throw new IllegalArgumentException("fromPersonID must not be null!");

		if (toPersonID == null)
			throw new IllegalArgumentException("toPersonID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) pm.getObjectById(personRelationTypeID);
			Person fromPerson = (Person) pm.getObjectById(fromPersonID);
			Person toPerson = (Person) pm.getObjectById(toPersonID);

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			pm.deletePersistentAll(relations);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the paths to the Persons that correspond to the nodes in the relation graph that are farthest away from the
	 * startingPoint and connected via intermediary nodes only by the allowed relationType(ID)s. Or in case the maxDepth
	 * is reached, the elements of that iteration are returned.
	 *
	 * @param relationTypeIDs The ids of the {@link RelationType}s that represent the allowed edges in the graph.
	 * @param startPoint The source from which to search for the farthest nodes.
	 * @param maxDepth The maximum depth (distance) until which the search is continued.
	 * @return the PersonIDs that correspond to the nodes in the relation graph that are farthest away from the
	 * startingPoint and connected via intermediary nodes only by the allowed relationType(ID)s. Or in case the maxDepth
	 * is reached, the elements of that iteration are returned.
	 */
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Set<Deque<ObjectID>> getRootNodes(Set<PersonRelationTypeID> relationTypeIDs, PropertySetID startPoint, int maxDepth)
	{
		Set<Deque<ObjectID>> result = new HashSet<Deque<ObjectID>>();
		Queue< Deque<ObjectID> > tempNodes = new LinkedList<Deque<ObjectID>>();
		Deque<ObjectID> startPath = new LinkedList<ObjectID>();
		startPath.push(startPoint);
		tempNodes.add(startPath);
		PersistenceManager pm = createPersistenceManager();

		Set<PersonRelationType> relationTypes = new HashSet<PersonRelationType>(
				getPersonRelationTypes(relationTypeIDs,
						new String[] { FetchPlan.DEFAULT, PersonRelation.FETCH_GROUP_TO_ID },
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
				);

		getNearestNodes(pm, relationTypes, tempNodes, result, maxDepth);
		return result;
	}

	/**
	 * Helper method to find all paths to nodes lying farthest away from the nodes in the nextLevelQueue. Allowed edges are defined
	 * by the given relationTypes. In case the maximum recursion depth is reached the currently processed nodes are added.
	 * <p><b>Important</b>: <br>
	 *	 	The resulting Deque is to be treated like a stack. The first element (a PropertySetID of the Person object that
	 *		is the root of this path) is on top of that stack and the last one is the last edge/arc to the node we started
	 *		with. <br>
	 *		It is therefore necessary to treat the Deque as a stack/queue and the elements should be visited in order of the
	 *		iterator or by calling pop()/poll().
	 * </p>
	 * <p>Note: In case there are any cycles in the graph like: X -><sub>1</sub> A -><sub>2</sub> B -><sub>3</sub> C
	 * 		-><sub>4</sub> A. The	path generated will break the cycle and end at the last node before completing it:
	 * 		X -><sub>1</sub> A -><sub>2</sub> B -><sub>3</sub> C. It is stored in the heap as follows:
	 * 		<i>(top)</i> C, -><sub>3</sub>, -><sub>2</sub>, -><sub>1</sub>. <i>(bottom)</i>
	 * </p>
	 *
	 * @param pm The PersistenceManager to use in order to retrieve the Persons corresponding to the PropertySetIDs.
	 * @param relationTypes The valid relation types according to which the graph will be traversed.
	 * @param nextLevelQueue The queue containing the starting nodes and later the nodes to visit in the next iteration.
	 * @param foundPaths The set of paths to the nodes from which there exists no allowed edge to another node and/or
	 * 									 the nodes that were examined in the maxDepth iteration.
	 * @param maxDepth The maximum iteration depth until which to traverse the relation graph.
	 */
	private void getNearestNodes(PersistenceManager pm, Set<PersonRelationType> relationTypes,
			Queue< Deque<ObjectID> > nextLevelQueue, Set< Deque<ObjectID> > foundPaths, int maxDepth)
	{
		if (maxDepth < 0 || nextLevelQueue.isEmpty())
			return;

		int depth = 0;
		boolean firstRun = true;
		// Queue of the elements that need to be visited in the next phase of the breadth-first search.
		Queue< Deque<ObjectID> > currentDepthQueue = new LinkedList<Deque<ObjectID>>();
		do {
			currentDepthQueue.addAll(nextLevelQueue);
			// nextLevel = elements to process in the next step in the breadth-first search.
			nextLevelQueue.clear();
			do {
				Deque<ObjectID> curNodePath = currentDepthQueue.poll();
				Person source;
				if (firstRun)
				{ // the source element passed to this method is NOT a PersonRelationID but a PropertySetID of the source node.
					source = (Person) pm.getObjectById(curNodePath.pop());
					firstRun = false;
				}
				else
				{ // the standard case
					PersonRelation currentRelation = (PersonRelation) pm.getObjectById(curNodePath.peek());
					source = currentRelation.getTo();
				}
				// get the filtered relations from the current node
				final Collection<? extends PersonRelation> personRelations =
					PersonRelation.getPersonRelations(pm, source, null, relationTypes);

				// if there is no connection to other nodes -> we're at the end of the path, hence at the PropertySetID of
				// that node.
				if (personRelations.isEmpty())
				{
					curNodePath.push((ObjectID) pm.getObjectId(source));
					foundPaths.add(curNodePath);
				}
				else
				{
					for (PersonRelation personRelation : personRelations)
					{
						final Deque<ObjectID> tmp = new LinkedList<ObjectID>( curNodePath );
						final ObjectID relationID = (ObjectID) pm.getObjectId(personRelation);
						if (tmp.contains(relationID) && !foundPaths.contains(tmp))
						{
							// We found a circle to which there is no path yet
							// -> stop with the last element before completing the circle.
							ObjectID circleArc = tmp.pop(); // remove the last node of the circle to omit showing it twice.
							PersonRelation circleCompletingRelation = (PersonRelation) pm.getObjectById(circleArc);
							tmp.push(circleCompletingRelation.getFromID()); // add the last node before completion as root node
							foundPaths.add(tmp);
							continue;
						}

						// extend the path and add it to the next step of the breadth-first search.
						tmp.push((ObjectID) pm.getObjectId(personRelation));
						nextLevelQueue.add( tmp );
					}
				}
			}  while (! currentDepthQueue.isEmpty());
			depth++;
		} while (depth < maxDepth && !nextLevelQueue.isEmpty());

		// In case we reached maximum depth -> add all valid nodes.
		if (depth == maxDepth && !nextLevelQueue.isEmpty())
		{
			// Add the PropertySetIDs of the last elements that were touched in order to provide a correct root node.
			for (Deque<ObjectID> deque : nextLevelQueue) {
				ObjectID lastArc = deque.peek();
				PersonRelation lastRelation = (PersonRelation) pm.getObjectId(lastArc);
				deque.push(lastRelation.getToID());
			}

			foundPaths.addAll(nextLevelQueue);
		}
	}

}
