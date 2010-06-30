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
import org.nightlabs.jfire.prop.RoleConstants;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;

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
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));
//			// TODO also only return only number of (authority) filtered person relations
//			return PersonRelation.getPersonRelationCount(pm, personRelationType, fromPerson, toPerson);
			Collection<? extends PersonRelation> personRelations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			// authority filtering
			personRelations = filterPersonRelations(pm, personRelations);
			return personRelations.size();
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
		return getPersonRelationIDs(personRelationTypeID, fromPersonID, toPersonID, null); // Default (from original): Dont sort?
	}


	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	@SuppressWarnings("unchecked")
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
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
			Collection<PersonRelation> filteredResult = filterPersonRelations(pm, relations);
			return (Collection<PersonRelationID>) (personRelationComparator != null ? getSortedPersonRelationIDsByPersonRelationType(filteredResult, personRelationComparator) : NLJDOHelper.getObjectIDList(filteredResult));

		} finally {
			pm.close();
		}
	}

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

	@SuppressWarnings("unchecked")
	@RolesAllowed("_Guest_") // TODO access rights
	@Override
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
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			// authority filtering
			relations = filterPersonRelations(pm, relations);			
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

			// Sort if necessary.
			return (Collection<PersonRelationID>) (personRelationComparator != null ? getSortedPersonRelationIDsByPersonRelationType(relations, personRelationComparator) : NLJDOHelper.getObjectIDList(relations));
		} finally {
			pm.close();
		}
	}

	@Override
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
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			// authority filtering
			relations = filterPersonRelations(pm, relations);			
			long relationsMarkedForExclusionCount = 0;

			// Perform the filtration. Mark elements to be excluded.
			boolean isPerformFilterFrom = fromPropertySetIDsToExclude != null && !fromPropertySetIDsToExclude.isEmpty();
			boolean isPerformFilterTo = toPropertySetIDsToExclude != null && !toPropertySetIDsToExclude.isEmpty();
			for (PersonRelation personRelation : relations) {
				if (isPerformFilterFrom && fromPropertySetIDsToExclude.contains(personRelation.getFromID()) || isPerformFilterTo && toPropertySetIDsToExclude.contains(personRelation.getToID())) // This ensures we dont get repeated elements in the count.
					relationsMarkedForExclusionCount++;
			}
			return relations.size() - relationsMarkedForExclusionCount;
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
			relations = filterPersonRelations(pm, relations);
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
			relations = filterPersonRelations(pm, relations);			
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
			personRelations = filterPersonRelations(pm, personRelations);
			
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
			Collection<PersonRelation> filteredRelations = filterPersonRelations(pm, relations);
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

	/**
	 * Filters the given set of PersonRelations based on the authority settings for PropertySets aka Persons.
	 * Means if a user has not the right to see a certain PropertySet the corresponding PersonRelation where
	 * such a user represents the to or from person, this PersonRelation is removed.
	 *
	 * @param pm The {@link PersistenceManager}
	 * @param relations the Collection of PersonRelations to filter
	 * @return the filtered Collection of PersonRelations.
	 */
	private Collection<PersonRelation> filterPersonRelations(PersistenceManager pm, Collection<? extends PersonRelation> relations)
	{
		// Begin authority check, only return personRelations which persons are allowed to be seen
		Map<Person, PersonRelation> person2Relation = new HashMap<Person, PersonRelation>();
		for (PersonRelation personRelation : relations) {
			person2Relation.put(personRelation.getFrom(), personRelation);
			person2Relation.put(personRelation.getTo(), personRelation);
		}

		Set<Person> filteredPersons = Authority.filterSecuredObjects(pm, person2Relation.keySet(), getPrincipal(), RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);
		
		Set<Person> removedPersons = new HashSet<Person>(person2Relation.keySet());
		removedPersons.removeAll(filteredPersons);
		for (Person removedPerson : removedPersons) {
			PersonRelation removedRelation = person2Relation.get(removedPerson);
			if (removedRelation != null) {
				person2Relation.remove(removedRelation.getFrom());
				person2Relation.remove(removedRelation.getTo());
			}
		}
		return new HashSet<PersonRelation>(person2Relation.values());
	}
}
