package org.nightlabs.jfire.personrelation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.management.relation.RelationType;

import org.apache.log4j.Logger;
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
	private static final Logger logger = Logger.getLogger(PersonRelationManagerBean.class);
	
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
	) {
		return getPersonRelationCount(new PersonRelationFilterCriteria(personRelationTypeID, fromPersonID, toPersonID));
	}
	
	@Override
	public long getPersonRelationCount(PersonRelationFilterCriteria filterCriteria)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return PersonRelationAccess.getPersonRelationCount(pm, getPrincipal(), filterCriteria);
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
		return getPersonRelationIDs(new PersonRelationFilterCriteria(personRelationTypeID, fromPersonID, toPersonID));
	}


	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			PersonRelationComparator personRelationComparator // Leave this null to suggest no sorting
	)
	{
		return getPersonRelationIDs(new PersonRelationFilterCriteria(personRelationTypeID, fromPersonID, toPersonID, personRelationComparator));
	}
	
	@SuppressWarnings("unchecked")
	public Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationFilterCriteria filterCriteria
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return PersonRelationAccess.getPersonRelationIDs(pm, getPrincipal(), filterCriteria);
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	protected Collection<PersonRelationID> getSortedPersonRelationIDsByPersonRelationType
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
	 * @deprecated Use {@link #getPersonRelationIDs(PersonRelationFilterCriteria)}
	 */
	@SuppressWarnings("unchecked")
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	@Deprecated
	public Collection<PersonRelationID> getFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude,
			PersonRelationComparator personRelationComparator // Leave this null to suggest no sorting
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return PersonRelationAccess.getPersonRelationIDs(pm, getPrincipal(), new PersonRelationFilterCriteria(
					Collections.singleton(personRelationTypeID), null,
					fromPersonID, toPersonID,
					fromPropertySetIDsToExclude, toPropertySetIDsToExclude,
					personRelationComparator));
		} finally {
			pm.close();
		}
	}

	/**
	 * @deprecated Use {@link #getPersonRelationCount(PersonRelationFilterCriteria)}
	 */
	@Override
	@Deprecated
	public long getFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return PersonRelationAccess.getPersonRelationCount(pm, getPrincipal(), new PersonRelationFilterCriteria(
					Collections.singleton(personRelationTypeID), null,
					fromPersonID, toPersonID,
					fromPropertySetIDsToExclude, toPropertySetIDsToExclude,
					null));
		} finally {
			pm.close();
		}
	}



	// ------->> This direct filter is an 'inclusive' filter -- the direct opposite of the above codes where we filtered off unwanted IDs.
	@SuppressWarnings("unchecked")
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationID> getInclusiveFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToInclude,
			Set<PropertySetID> toPropertySetIDsToInclude,
			PersonRelationComparator personRelationComparator // Leave this null to suggest no sorting
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			// authority filtering
			relations = PersonRelationAccess.filterPersonRelationsByAuthority(pm, getPrincipal(), relations);
			Collection<PersonRelation> filteredInclusiveRelations = new LinkedList<PersonRelation>();

			// Perform the filtration.
			boolean isPerformFilterFrom = fromPropertySetIDsToInclude != null && !fromPropertySetIDsToInclude.isEmpty();
			boolean isPerformFilterTo = toPropertySetIDsToInclude != null && !toPropertySetIDsToInclude.isEmpty();
			for (PersonRelation personRelation : relations) {
				if (isPerformFilterFrom && fromPropertySetIDsToInclude.contains(personRelation.getFromID()) || isPerformFilterTo && toPropertySetIDsToInclude.contains(personRelation.getToID())) // This ensures we dont get repeated elements in the LinkedList.
					filteredInclusiveRelations.add(personRelation);
			}

			relations.clear();
			relations = filteredInclusiveRelations;
			// Sort if necessary.
			return (Collection<PersonRelationID>) (personRelationComparator != null ? getSortedPersonRelationIDsByPersonRelationType(relations, personRelationComparator) : NLJDOHelper.getObjectIDList(relations));
		} finally {
			pm.close();
		}
	}


	@Override
	public long getInclusiveFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToInclude,
			Set<PropertySetID> toPropertySetIDsToInclude
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			// authority filtering
			relations = PersonRelationAccess.filterPersonRelationsByAuthority(pm, getPrincipal(), relations);			
			long count = 0;

			// Perform the filtration.
			boolean isPerformFilterFrom = fromPropertySetIDsToInclude != null && !fromPropertySetIDsToInclude.isEmpty();
			boolean isPerformFilterTo = toPropertySetIDsToInclude != null && !toPropertySetIDsToInclude.isEmpty();
			for (PersonRelation personRelation : relations) {
				if (isPerformFilterFrom && fromPropertySetIDsToInclude.contains(personRelation.getFromID()) || isPerformFilterTo && toPropertySetIDsToInclude.contains(personRelation.getToID())) // This ensures we dont get repeated elements in the LinkedList.
					count++;
			}
			return count;
		} finally {
			pm.close();
		}
	}

	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	// Query countings for actualNodeCount and tuckedNodeCount.
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public TuckedQueryCount getTuckedPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID currentID,
			Set<PropertySetID> propertySetIDsToRoot,
			Set<PropertySetID> propertySetIDsToTuckedChildren
	) {
		// Note(s):
		//    I. The input parameter currentID represents the TuckedNode we are dealing with.
		//   II. The given propertySetIDsToRoot are the IDs we want to avoid, in order to avoid open-ended relations.
		//  III. The given propertySetIDsToTuckedChildrean are the IDs we want to seek to keep in the tucked status, if and only if they exist.
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (currentID == null ? null : pm.getObjectById(currentID));
			Collection<? extends PersonRelation> personRelations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, null);
			// authority filtering
			personRelations = PersonRelationAccess.filterPersonRelationsByAuthority(pm, getPrincipal(), personRelations);
			
			if (logger.isDebugEnabled()) {
				String str = "\n ---- Ref: " + showObjectID(currentID) + " ----";
				str += "\n ~~ personRelationType: " + (personRelationType == null ? "null" : personRelationType.getName().getText());
				str += "\n ~~ fromPerson: " + (fromPerson == null ? "null" : fromPerson.getDisplayName());
				str += "\n ~~ " + showObjectIDs("propertySetIDsToRoot", propertySetIDsToRoot, 10);
				str += "\n ~~ " + showObjectIDs("propertySetIDsToTuckedChildren", propertySetIDsToTuckedChildren, 10);
				str += "\n ~~ personRelations.size(): " + (personRelations == null ? "null" : personRelations.size());

				logger.debug(str);
			}
			
			
			TuckedQueryCount tqCount = new TuckedQueryCount();

			// 1. Retrieve the 'actual' child-node count.
			//    --> At this point in time: This is similar to the method getFilteredPersonRelationCount(), but more specific.
			//    --> On filtration: Exclude any elements from the properySetIDsToRoot.
			tqCount.actualChildCount = personRelations.size();
			boolean isPerformExcludeFilter = propertySetIDsToRoot != null && !propertySetIDsToRoot.isEmpty();

			// 2. Retrieve the 'tucked'-node count.
			//    --> At this point in time: This is similar to the method getInclusiveFilteredPersonRelationCount(), but more specific.
			//    --> On filtration: Only count elements if they are in the given propertySetIDsToTuckedChildren.
			tqCount.tuckedChildCount = 0L;
			boolean isPerformIncludeFilter = propertySetIDsToTuckedChildren != null && !propertySetIDsToTuckedChildren.isEmpty();

			// Both 1. and 2. can run on the same iteration.
			for (PersonRelation personRelation : personRelations) {
				if (logger.isDebugEnabled())
					logger.debug(" ~ personRelation [To]: " + showPersonInfo(personRelation.getTo()));

				PropertySetID propertySetID = personRelation.getToID();
				if (isPerformExcludeFilter && propertySetIDsToRoot.contains(propertySetID)) // On 1.
					tqCount.actualChildCount--;

				if (isPerformIncludeFilter && propertySetIDsToTuckedChildren.contains(propertySetID)) // On 2.
					tqCount.tuckedChildCount++;
			}

			if (logger.isDebugEnabled())
				logger.debug(" :: TuckedNodeCount info gathered: tuckedChildCount = " + tqCount.tuckedChildCount + ", actualChildCount = " + tqCount.actualChildCount);
			
			// Done.
			return tqCount;

		} finally {
			pm.close();
		}
	}
	
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	// -------------------------------------------------------------------------------------------------- ++ ------>>
	//  Kai's debug shits. Will be removed once ALL testings are completed! Kai.
	// -------------------------------------------------------------------------------------------------- ++ ------>>
	// II. Quick debug.
	private String showObjectIDs(String preamble, Collection<? extends ObjectID> objIDs, int modLnCnt) {
		if (objIDs == null)
			return preamble + " :: NULL";

		boolean isLonger = objIDs.size() > modLnCnt;
		String str = preamble + " (" + objIDs.size() + ") :: { " + (isLonger ? "\n     " : "");
		int ctr = 0;
		for (ObjectID objectID : objIDs) {
			str += "(" + ctr + ")" + showObjectID(objectID) + " ";
			ctr++;

			if (ctr % modLnCnt == 0)
				str += "\n     ";
		}

		return str + (isLonger ? "\n   }" : "}");
	}

	// III. Quick debug.
	private String showObjectID(ObjectID objectID) {
		if (objectID == null)
			return "[null]";

		String[] segID = objectID.toString().split("&");
		return "[" + segID[1] + "]";
	}
	
	// IV. Quick debug.
	private String showPersonInfo(Person person) {
		return String.format("%s %s", person.getDisplayName(), showObjectID((ObjectID) JDOHelper.getObjectId(person)));
	}
	// -------------------------------------------------------------------------------------------------- ++ ------>>


	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
//			return NLJDOHelper.getDetachedObjectList(pm, personRelationIDs, PersonRelation.class, fetchGroups, maxFetchDepth);
			
			Collection<PersonRelation> relations = pm.getObjectsById(personRelationIDs);
			Collection<PersonRelation> filteredRelations = PersonRelationAccess.filterPersonRelationsByAuthority(pm, getPrincipal(), relations);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(filteredRelations);			
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
	 * FIXME These comments are outdated. Kai.
	 *
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

			Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> result = PersonRelationAccess.getPathsToRoots(pm, relationTypes, startPoint, maxDepth);
			return result;

		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the {@link PropertySetID} of the persons which represent the root nodes of for the given {@link Set} of
	 * personIDs. The returned {@link List} is sorted in alphabetical order of the displayname of the root nodes.
	 */
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public List<PropertySetID> getRootNodes(Set<PersonRelationTypeID> relationTypeIDs, Set<PropertySetID> personIDs, int maxDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Set<PersonRelationType> relationTypes = new HashSet<PersonRelationType>(
					getPersonRelationTypes(relationTypeIDs,
							new String[] { FetchPlan.DEFAULT, PersonRelation.FETCH_GROUP_TO_ID, PersonRelation.FETCH_GROUP_FROM_ID },
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
			);
			TreeMap<String, PropertySetID> treeMap = new TreeMap<String, PropertySetID>();
			for (PropertySetID personID : personIDs) {
				Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> result = PersonRelationAccess.getPathsToRoots(pm, relationTypes, personID, maxDepth);
				Set<PropertySetID> pathsToRoot = initRelatablePathsToRoots(result);
				for (PropertySetID rootPersonID : pathsToRoot) {
					Person person = (Person) pm.getObjectById(rootPersonID);
					String displayName = person.getDisplayName();
					treeMap.put(displayName, rootPersonID);
				}
			}
			List<PropertySetID> result = new ArrayList<PropertySetID>(treeMap.size());
			for (Map.Entry<String, PropertySetID> entry : treeMap.entrySet()) {
				result.add(entry.getValue());
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Prepares the data from 'relatablePathsToRoots', ready for use in the path-expansion manipulations.
	 * @return the set of {@link PropertySetID}s for the root(s) of the tree.
	 */
	private Set<PropertySetID> initRelatablePathsToRoots(Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> relatablePathsToRoots) {
		List<Deque<ObjectID>> pathsToRoot_PSID = relatablePathsToRoots.get(PropertySetID.class);    // <-- PropertySetID only.
		Set<PropertySetID> rootIDs = new HashSet<PropertySetID>();
		Iterator<Deque<ObjectID>> iterPaths_PSID = pathsToRoot_PSID.iterator();
		int index = 0;
		while (iterPaths_PSID.hasNext()) {
			Deque<ObjectID> path_PSID = iterPaths_PSID.next();
			rootIDs.add((PropertySetID) path_PSID.peekFirst());
			index++;
		}
		// Done.
		return rootIDs;
	}
	
}
