/**
 * 
 */
package org.nightlabs.jfire.personrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.RoleConstants;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for convenient access to PersonRelations using {@link PersonRelationFilterCriteria} for filtering.
 * 
 * @author abieber
 */
public class PersonRelationAccess {

	/** Logger for this class */
	private static final Logger logger = LoggerFactory.getLogger(PersonRelationAccess.class);
	
	/**
	 * Get all {@link PersonRelationID}s matching the given criteria (and where the to and from persons are visible to the user).
	 * @param pm The PersistenceManager to use.
	 * @param principal JFirePrincipal used to check access rights.
	 * @param filterCriteria The filter-criteria.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<PersonRelationID> getPersonRelationIDs(PersistenceManager pm, JFirePrincipal principal, PersonRelationFilterCriteria filterCriteria)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("getPersonRelationIDs() started with filterCriteria");
			logger.debug(filterCriteria.toHumanReadable());
		}
		Collection<PersonRelationTypeID> personRelationTypeIncludeIDs = filterCriteria.getPersonRelationTypeIncludeIDs();
		Set<PersonRelationType> personRelationTypeIncludes = 
			(Set<PersonRelationType>) (personRelationTypeIncludeIDs == null ? null
			: new HashSet<PersonRelationType>(pm.getObjectsById(personRelationTypeIncludeIDs)));
		Person fromPerson = (Person) (filterCriteria.getFromPersonID() == null ? null : pm.getObjectById(filterCriteria.getFromPersonID()));
		Person toPerson = (Person) (filterCriteria.getToPersonID() == null ? null : pm.getObjectById(filterCriteria.getToPersonID()));

		Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, fromPerson, toPerson, personRelationTypeIncludes);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getPersonRelationIDs() have {} relations after filtering typeIncludes and from/to person", relations.size());
		}
		
		// authority filtering
		filterPersonRelationsByRelationType(pm, relations, filterCriteria.getPersonRelationTypeExcludeIDs());
		if (logger.isDebugEnabled()) {
			logger.debug("getPersonRelationIDs() have {} relations after filtering typeExcludes", relations.size());
		}
		filterPersonRelationsByAnchorPersons(filterCriteria.getFromPropertySetIDsToExclude(), filterCriteria.getToPropertySetIDsToExclude(), relations);
		if (logger.isDebugEnabled()) {
			logger.debug("getPersonRelationIDs() have {} relations after filtering from/to exlcude persons", relations.size());
		}
		relations = filterPersonRelationsByAuthority(pm, principal, relations);
		if (logger.isDebugEnabled()) {
			logger.debug("getPersonRelationIDs() have {} relations after person access-rights", relations.size());
		}
		return (Collection<PersonRelationID>) (filterCriteria.getPersonRelationComparator() != null ? getSortedPersonRelationIDsByPersonRelationType(relations, filterCriteria.getPersonRelationComparator()) : NLJDOHelper.getObjectIDList(relations));
	}
	
	public static long getPersonRelationCount(PersistenceManager pm, JFirePrincipal principal, PersonRelationFilterCriteria filterCriteria)
	{
		return getPersonRelationIDs(pm, principal, filterCriteria).size();
	}
	
	private static void filterPersonRelationsByAnchorPersons(
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude,
			Collection<? extends PersonRelation> relations) {
		Collection<PersonRelation> relationsMarkedForExclusion = new LinkedList<PersonRelation>();

		// Perform the filtration. Mark elements to be excluded.
		boolean isPerformFilterFrom = fromPropertySetIDsToExclude != null && !fromPropertySetIDsToExclude.isEmpty();
		boolean isPerformFilterTo = toPropertySetIDsToExclude != null && !toPropertySetIDsToExclude.isEmpty();
		for (PersonRelation personRelation : relations) {
			if (isPerformFilterFrom && fromPropertySetIDsToExclude.contains(personRelation.getFromID()) || isPerformFilterTo && toPropertySetIDsToExclude.contains(personRelation.getToID())) // This ensures we dont get repeated elements in the LinkedList.
				relationsMarkedForExclusion.add(personRelation);
		}

		// Perform the filtration. Remove marked elements.
		relations.removeAll(relationsMarkedForExclusion);
	}

	private static void filterPersonRelationsByRelationType(PersistenceManager pm, Collection<? extends PersonRelation> relations, Collection<PersonRelationTypeID> excludedRelationTypeIDs)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("filterPersonRelationsByRelationType() Filtering for excluded relation-types {}", excludedRelationTypeIDs);
		}
		if (excludedRelationTypeIDs == null || excludedRelationTypeIDs.isEmpty())
			return;
		
		LinkedList<PersonRelation> toRemove = new LinkedList<PersonRelation>();
		for (PersonRelation personRelation : relations) {
			PersonRelationTypeID relationTypeID = (PersonRelationTypeID) pm.getObjectId(personRelation.getPersonRelationType());
			if (excludedRelationTypeIDs.contains(relationTypeID)) {
				if (logger.isDebugEnabled()) {
					logger.debug("filterPersonRelationsByRelationType() Removing {} as its type {} should be excluded.", personRelation, relationTypeID);
				}
				toRemove.add(personRelation);
			}
		}
		relations.removeAll(toRemove);
	}
	/**
	 * Filters the given set of PersonRelations based on the authority settings for PropertySets aka Persons.
	 * Means if a user has not the right to see a certain PropertySet the corresponding PersonRelation where
	 * such a user represents the to or from person, this PersonRelation is removed.
	 *
	 * @param pm The {@link PersistenceManager}
	 * @param relations the Collection of PersonRelations to filter
	 * @return the filtered Collection of PersonRelations.
	 */
	static Collection<PersonRelation> filterPersonRelationsByAuthority(PersistenceManager pm, JFirePrincipal principal, Collection<? extends PersonRelation> relations)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("filterPersonRelationsByAuthority() Filtering for principal {}", principal);
		}
		// Begin authority check, only return personRelations which persons are allowed to be seen
		Map<Person, PersonRelation> person2Relation = new HashMap<Person, PersonRelation>();
		for (PersonRelation personRelation : relations) {
			person2Relation.put(personRelation.getFrom(), personRelation);
			person2Relation.put(personRelation.getTo(), personRelation);
		}

		Set<Person> filteredPersons = Authority.filterSecuredObjects(pm, person2Relation.keySet(), principal, RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);
		
		Set<Person> removedPersons = new HashSet<Person>(person2Relation.keySet());
		removedPersons.removeAll(filteredPersons);
		if (logger.isDebugEnabled()) {
			logger.debug("filterPersonRelationsByAuthority() Have {} persons to be filtered/removed", removedPersons.size());
		}
		for (Person removedPerson : removedPersons) {
			PersonRelation removedRelation = person2Relation.get(removedPerson);
			if (removedRelation != null) {
				person2Relation.remove(removedRelation.getFrom());
				person2Relation.remove(removedRelation.getTo());
			}
		}
		return new HashSet<PersonRelation>(person2Relation.values());
	}
	
	@SuppressWarnings("unchecked")
	private static Collection<PersonRelationID> getSortedPersonRelationIDsByPersonRelationType
	(Collection<? extends PersonRelation> relations, PersonRelationComparator personRelationComparator) 
	{
		List<PersonRelation> relns = null;
		if (relations instanceof List) {
			relns = (List<PersonRelation>) relations;	
		}
		else {
			relns = new ArrayList<PersonRelation>(relations);
		}
		Collections.sort(relns, personRelationComparator);

		return NLJDOHelper.getObjectIDList(relns);
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
	public static Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getPathsToRoots
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
		List<Deque<ObjectID>> foundPPSIDPathList = new ArrayList<Deque<ObjectID>>(); // <-- PropertySetID only.
		List<Deque<ObjectID>> foundOIDPathList = new ArrayList<Deque<ObjectID>>();   // <-- PersonRelationID, PropertySetID (mixed)


		final Collection<? extends PersonRelation> pRelnsToRoot = PersonRelation.getPersonRelations(pm, (Person) pm.getObjectById(startID), null, relationTypes);
		if (pRelnsToRoot.isEmpty()) {
			Deque<ObjectID> foundPPSIDPath = new LinkedList<ObjectID>();
			foundPPSIDPathList.add(foundPPSIDPath);
			foundPPSIDPath.push(startID);

			Deque<ObjectID> foundOIDPath = new LinkedList<ObjectID>();
			foundOIDPathList.add(foundOIDPath);
			foundOIDPath.push(startID);
		}
		else {
			// Call the recursive method.
			for (PersonRelation pRelnToRoot : pRelnsToRoot) {
				Deque<ObjectID> foundPPSIDPath = new LinkedList<ObjectID>();
				foundPPSIDPathList.add(foundPPSIDPath);

				Deque<ObjectID> foundOIDPath = new LinkedList<ObjectID>();
				foundOIDPathList.add(foundOIDPath);

				// Go gather new paths.
				getPathsToRoots(pm, maxDepth, pRelnToRoot, relationTypes, foundOIDPath, foundOIDPathList, foundPPSIDPath, foundPPSIDPathList);
			}
		}


		// An array of only two elements; differentiated by the key classes PropertySetID and PersonRelationID
		Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> results = new ConcurrentHashMap<Class<? extends ObjectID>, List<Deque<ObjectID>>>(2);
		results.put(PropertySetID.class, foundPPSIDPathList);  // <-- PropertySetID only.
		results.put(PersonRelationID.class, foundOIDPathList); // <-- PersonRelationID, PropertySetID (mixed)

		return results; //objectIDPathsFound;
	}

	// The recursive bit to getPathsToRoots.
	private static void getPathsToRoots
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


		// -------------------------------------------------------------- ++ AMENDED ++ --------------------------------------------------------------||
		// Recursive case.
		// -------------------------------------------------------------- ++ AMENDED ++ --------------------------------------------------------------||
		// A fork is found when we have more than one element in pRelnsToRoot.
		// In this case, the current path that we are pursuing must be duplicated -- as many times as the number of elements found in pRelnsToRoot. [Go by BREADTH].
		// Then for each of these duplicated paths, we continue to add with each element in pRelnsRoot. [Go by DEPTH].
		int forkSize = pRelnsToRoot.size();
		List<Deque<ObjectID>> current_foundPaths = new ArrayList<Deque<ObjectID>>(forkSize);      // <-- PersonRelationID (mixed)
		List<Deque<ObjectID>> current_PPSIDfoundPaths = new ArrayList<Deque<ObjectID>>(forkSize); // <-- PropertySetID

		// [Prep]-[Go by BREADTH]
		for (int i=0; i<forkSize; i++) {
			current_foundPaths.add(i == 0 ? foundOIDPath : new LinkedList<ObjectID>(foundOIDPath));
			current_PPSIDfoundPaths.add(i == 0 ? foundPPSIDPath : new LinkedList<ObjectID>(foundPPSIDPath));
		}

		// [Go by DEPTH]
		int i = 0;
		for (PersonRelation pRelToRoot : pRelnsToRoot) {
			Deque<ObjectID> current_foundPath = current_foundPaths.get(i);
			Deque<ObjectID> current_PPSIDfoundPath = current_PPSIDfoundPaths.get(i);

			if (i > 0) {
				foundOIDPaths.add(current_foundPath);
				foundPPSIDPaths.add(current_PPSIDfoundPath);
			}

			getPathsToRoots(pm, maxDepth-1, pRelToRoot, relationTypes, current_foundPath, foundOIDPaths, current_PPSIDfoundPath, foundPPSIDPaths); // Recursive epochs here.
			i++;
		}
		// -------------------------------------------------------------- ++ AMENDED ++ --------------------------------------------------------------||


//		// Recursive case. OLD. Faulty with multi-roots!
//		Deque<ObjectID> current_foundPath = foundOIDPath;        // <-- PersonRelationID
//		Deque<ObjectID> current_PPSIDfoundPath = foundPPSIDPath; // <-- PropertySetID
//		int outGoingPathIndex = 0;
//		for (PersonRelation pRelToRoot : pRelnsToRoot) {
//			if (outGoingPathIndex > 0) {
//				// More than one out-going paths originating from 'person_nextFrom'.
//				// These are new, and needs to be duplicated from the original 'foundPath'.
//				current_foundPath = new LinkedList<ObjectID>(foundOIDPath);
//				foundOIDPaths.add(current_foundPath);
//
//				current_PPSIDfoundPath = new LinkedList<ObjectID>(foundPPSIDPath);
//				foundPPSIDPaths.add(current_PPSIDfoundPath);
//			}
//
//			getPathsToRoots(pm, maxDepth-1, pRelToRoot, relationTypes, current_foundPath, foundOIDPaths, current_PPSIDfoundPath, foundPPSIDPaths);
//			outGoingPathIndex++;
//		}
	}

//	@RolesAllowed("_Guest_") // TODO access rights
//	@Override
//	public Map<PropertySetID, Map<Class<? extends ObjectID>, List<Deque<ObjectID>>>> getRootNodes(
//			Set<PersonRelationTypeID> relationTypeIDs, Set<PropertySetID> personIDs, int maxDepth)
//	{
//		PersistenceManager pm = createPersistenceManager();
//		try {
//			Set<PersonRelationType> relationTypes = new HashSet<PersonRelationType>(
//					getPersonRelationTypes(relationTypeIDs,
//							new String[] { FetchPlan.DEFAULT, PersonRelation.FETCH_GROUP_TO_ID, PersonRelation.FETCH_GROUP_FROM_ID },
//							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
//			);
//			TreeMap<PropertySetID, Map<Class<? extends ObjectID>, List<Deque<ObjectID>>>> map = new TreeMap<PropertySetID, Map<Class<? extends ObjectID>,List<Deque<ObjectID>>>>();
//			for (PropertySetID personID : personIDs)
//			{
//				Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> result = getPathsToRoots(pm, relationTypes, personID, maxDepth);
//				map.put(personID, result);
//			}
//			return map;
//		} finally {
//			pm.close();
//		}
//	}


	
}
