package org.nightlabs.jfire.personrelation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

// Not necessary. This is a subsidiary - and if not (i.e. if some specific customer wants other JFire relation types, then we have to put these
// into a customer-specific project  not this JFire-generic project! Marco.
//			{
//				// TODO: do we need an inverse relation for that?
//				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.branchOffice;
//				try {
//					pm.getObjectById(personRelationTypeID);
//				} catch (JDOObjectNotFoundException x) {
//					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, null));
//					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchOffice.name"); //$NON-NLS-1$
//					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchOffice.description"); //$NON-NLS-1$
//				}
//			}

// Moved this to BehrJFireTrade, since this is Behr-specific stuff. Marco.
//			{
//				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.mainLocation;
//				try {
//					pm.getObjectById(personRelationTypeID);
//				} catch (JDOObjectNotFoundException x) {
//					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.branchLocation));
//					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-mainLocation.name"); //$NON-NLS-1$
//					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-mainLocation.description"); //$NON-NLS-1$
//				}
//			}
//			{
//				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.branchLocation;
//				try {
//					pm.getObjectById(personRelationTypeID);
//				} catch (JDOObjectNotFoundException x) {
//					PersonRelationType personRelationType = pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.mainLocation));
//					personRelationType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchLocation.name"); //$NON-NLS-1$
//					personRelationType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.personrelation.PersonRelationType-branchLocation.description"); //$NON-NLS-1$
//				}
//			}

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
	 * Returns the PersonIDs that correspond to the nodes in the relation graph that are farthest away from the
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
	public Set<Deque<PropertySetID>> getNearestNodes(Set<PersonRelationTypeID> relationTypeIDs, PropertySetID startPoint, int maxDepth)
	{
		Set<Deque<PropertySetID>> result = new HashSet<Deque<PropertySetID>>();
		Queue< Deque<PropertySetID> > tempNodes = new LinkedList<Deque<PropertySetID>>();
		Deque<PropertySetID> startPath = new LinkedList<PropertySetID>();
		startPath.push(startPoint);
		tempNodes.add(startPath);

		PersistenceManager pm = createPersistenceManager();
		try {
			Set<PersonRelationType> relationTypes = new HashSet<PersonRelationType>(
					getPersonRelationTypes(relationTypeIDs,
							new String[] { FetchPlan.DEFAULT, PersonRelation.FETCH_GROUP_TO_ID },
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
			);

			getNearestNodes(pm, relationTypes, tempNodes, result, maxDepth);
			return result;

		} finally {
			pm.close();
		}
	}

	/**
	 * Helper method to find all nodes lying farthest away from the nodesToVisit, allowed edges are defined by the
	 * relationTypes. In case the maximum recursion depth is reached the currently available nodes are added.
	 *
	 * @param pm The PersistenceManager to use in order to retrieve the Persons corresponding to the PropertySetIDs.
	 * @param relationTypes The valid relation types according to which the graph will be traversed.
	 * @param nextLevelQueue The queue containing the starting nodes and later the nodes to visit in the next iteration.
	 * @param finalNodes The set of nodes from which there exists no allowed edge to another node and/or the nodes
	 *                   currently examined in the maxDepth iteration.
	 * @param maxDepth The maximum iteration depth until which to traverse the relation graph.
	 */
	private void getNearestNodes(PersistenceManager pm, Set<PersonRelationType> relationTypes,
			Queue< Deque<PropertySetID> > nextLevelQueue, Set< Deque<PropertySetID> > finalNodes, int maxDepth)
	{
		if (maxDepth < 0 || nextLevelQueue.isEmpty())
			return;

		int depth = 0;
		// Queue of the elements that need to be visited along with the paths that are used to get to them.
		Queue< Deque<PropertySetID> > currentDepthQueue = new LinkedList<Deque<PropertySetID>>();
		do {
			currentDepthQueue.addAll(nextLevelQueue);
			nextLevelQueue.clear();
			do {
				Deque<PropertySetID> curNodePath = currentDepthQueue.poll();
				final PropertySetID currentNode = curNodePath.peek();
				final Person tmpSource = (Person) pm.getObjectById(currentNode);
				// It is assumed that the relationTypes are not bidirectional, hence there are no two nodes n1 and n2
				// such that n1 <-relationType-> n2 and n2 <-relationType-> n1! (marius)
				final Collection<? extends PersonRelation> personRelations =
					PersonRelation.getPersonRelations(pm, tmpSource, null, relationTypes);

				if (personRelations.isEmpty())
					finalNodes.add(curNodePath);
				else
				{
					for (PersonRelation personRelation : personRelations)
					{
						final Deque<PropertySetID> tmp = new LinkedList<PropertySetID>( curNodePath );
						final PropertySetID toID = personRelation.getToID();
						if (tmp.contains(toID) && !finalNodes.contains(tmp))
						{
							// We found a circle to which there is no path yet
							// -> stop with the last element before completing the circle.
							finalNodes.add(tmp);
							continue;
						}

						tmp.push(toID);
						nextLevelQueue.add( tmp );
					}
				}
			}  while (! currentDepthQueue.isEmpty());
			depth++;
		} while (depth < maxDepth && !nextLevelQueue.isEmpty());

		// In case we reached maximum depth -> add all valid nodes.
		if (depth == maxDepth && !nextLevelQueue.isEmpty())
			finalNodes.addAll(nextLevelQueue);
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
	public Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getRootNodes(Set<PersonRelationTypeID> relationTypeIDs, PropertySetID startPoint, int maxDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Set<PersonRelationType> relationTypes = new HashSet<PersonRelationType>(
					getPersonRelationTypes(relationTypeIDs,
							new String[] { FetchPlan.DEFAULT, PersonRelation.FETCH_GROUP_TO_ID, PersonRelation.FETCH_GROUP_FROM_ID },
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
			);

			Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> result = getPathsToRoots(pm, relationTypes, startPoint, maxDepth);
			return result;

		} finally {
			pm.close();
		}
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
	 * @param maxDepth The maximum iteration depth until which to traverse the relation graph.
	 * @return The set of paths to the nodes from which there exists no allowed edge to another node and/or
	 * 									 the nodes that were examined in the maxDepth iteration.
	 */
	protected Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getPathsToRoots
	(PersistenceManager pm, Set<PersonRelationType> relationTypes, PropertySetID startID, int maxDepth) {
		// Kai's version.
		// Changes #1: Depth-first-search. This now returns the correct elements in the paths. See earlier version: getNearestPathNodes(...).
		// Changes #2: Returns two sets of paths in a single Map, distinguished by the following keys:
		//               1. Key[PropertySetID.class] :: Deque-path(s) to root(s) containing only PropertySetIDs.
		//               2. Key[PersonRelationID.class] :: Deque-path(s) to root(s) containing contigious PersonRelationID elements ending with the terminal PropertySetID.
		// Changes #3: Switched from Set<Deque<ObjectID>> to List<Deque<ObjectID>>. We need to ensure placement order of the paths (from Changes #2)
		//             to coincide correctly. As it already is, the pairs are created in tandem.

		// Guard #1.
		if (maxDepth < 1)
			return null;

		// Get the initial outgoing PersonRelations from the 'startID'.
		List<Deque<ObjectID>> propertySetIDpathsFound = new ArrayList<Deque<ObjectID>>(); // <-- PropertySetID only.
		List<Deque<ObjectID>> objectIDPathsFound = new ArrayList<Deque<ObjectID>>();      // <-- PersonRelationID, PropertySetID (mixed)

		Deque<ObjectID> foundPPSIDPath = new LinkedList<ObjectID>();
		propertySetIDpathsFound.add(foundPPSIDPath);

		Deque<ObjectID> foundOIDPath = new LinkedList<ObjectID>();
		objectIDPathsFound.add(foundOIDPath);

		final Collection<? extends PersonRelation> pRelnsToRoot = PersonRelation.getPersonRelations(pm, (Person) pm.getObjectById(startID), null, relationTypes);
		if (pRelnsToRoot.isEmpty()) {
			foundOIDPath.push(startID);
			foundPPSIDPath.push(startID);
		}
		else {
			// Call the recursive method.
			for (PersonRelation pRelnToRoot : pRelnsToRoot)
				getPathsToRoots(pm, maxDepth, pRelnToRoot, relationTypes, foundOIDPath, objectIDPathsFound, foundPPSIDPath, propertySetIDpathsFound);
		}


		// An array of only two elements; differentiated by the key classes PropertySetID and PersonRelationID
		Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> results = new ConcurrentHashMap<Class<? extends ObjectID>, List<Deque<ObjectID>>>(2);
		results.put(PropertySetID.class, propertySetIDpathsFound);  // <-- PropertySetID only.
		results.put(PersonRelationID.class, objectIDPathsFound);    // <-- PersonRelationID, PropertySetID (mixed)

		return results; //objectIDPathsFound;
	}

	// The recursive bit to getPathsToRoots.
	private void getPathsToRoots
	(PersistenceManager pm, int maxDepth, PersonRelation pRelnToRoot, Set<PersonRelationType> relationTypes,
	 Deque<ObjectID> foundOIDPath, List<Deque<ObjectID>> foundOIDPaths, Deque<ObjectID> foundPPSIDPath, List<Deque<ObjectID>> foundPPSIDPaths) {
		// Entry point: We need to store the reversed relation.
		PersonRelationType revPRType = pRelnToRoot.getPersonRelationType().getReversePersonRelationType();
		Collection<? extends PersonRelation> revPRelns = PersonRelation.getPersonRelations(pm, revPRType, pRelnToRoot.getTo(), pRelnToRoot.getFrom());
		PersonRelation pRelFromRoot = revPRelns.iterator().next();
		ObjectID pRelFromRootID = (ObjectID) pm.getObjectId(pRelFromRoot);

		// Terminal cases are ALWAYS of the type PropertySetID.
		// Base case 1: Cyclic-element detected. Stop to break the cyclic path.
		// Base case 2: Maximum depth reached.
		if (foundOIDPath.contains(pRelFromRootID) || maxDepth <= 1) {
			foundOIDPath.push(pRelnToRoot.getFromID());
			foundPPSIDPath.push(pRelnToRoot.getFromID());
			return;
		}

		// Store it.
		foundOIDPath.push(pRelFromRootID);            // <-- PersonRelationID
		foundPPSIDPath.push(pRelnToRoot.getFromID()); // <-- PropertySetID

		// Retrieve the next relations.
		Person person_nextFrom = pRelnToRoot.getTo();
		final Collection<? extends PersonRelation> pRelnsToRoot = PersonRelation.getPersonRelations(pm, person_nextFrom, null, relationTypes);

		// Base case 3. Root found.
		// No more forward relations to root from 'person_nextFrom'; i.e. we've arrived at the root.
		// In this case, we add the PropertySetID to 'foundPath' to conform to the PersonRelationTree structure.
		if (pRelnsToRoot.isEmpty()) {
			foundOIDPath.push(pRelnToRoot.getToID());
			foundPPSIDPath.push(pRelnToRoot.getToID());
			return;
		}


		// Recursive case.
		Deque<ObjectID> current_foundPath = foundOIDPath;        // <-- PersonRelationID
		Deque<ObjectID> current_PPSIDfoundPath = foundPPSIDPath; // <-- PropertySetID

		int outGoingPathIndex = 0;
		for (PersonRelation pRelToRoot : pRelnsToRoot) {
			if (outGoingPathIndex > 0) {
				// More than one out-going paths originating from 'person_nextFrom'.
				// These are new, and needs to be duplicated from the original 'foundPath'.
				current_foundPath = new LinkedList<ObjectID>(foundOIDPath);
				foundOIDPaths.add(current_foundPath);

				current_PPSIDfoundPath = new LinkedList<ObjectID>(foundPPSIDPath);
				foundPPSIDPaths.add(current_PPSIDfoundPath);
			}

			getPathsToRoots(pm, maxDepth-1, pRelToRoot, relationTypes, current_foundPath, foundOIDPaths, current_PPSIDfoundPath, foundPPSIDPaths);
			outGoingPathIndex++;
		}
	}


}
