/**
 * 
 */
package org.nightlabs.jfire.personrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.RoleConstants;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;

/**
 * Helper class for convenient access to PersonRelations using {@link PersonRelationFilterCriteria} for filtering.
 * 
 * @author abieber
 */
public class PersonRelationAccess {

	/**
	 * Get all {@link PersonRelationID}s matching the given criteria (and where the to and from persons are visible to the user).
	 * @param pm The PersistenceManager to use.
	 * @param principal JFirePrincipal used to check access rights.
	 * @param filterCriteria The filter-criteria.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<PersonRelationID> getPersonRelationIDs(PersistenceManager pm, JFirePrincipal principal, PersonRelationFilterCriteria filterCriteria)
	{
		Collection<PersonRelationTypeID> personRelationTypeIncludeIDs = filterCriteria.getPersonRelationTypeIncludeIDs();
		Set<PersonRelationType> personRelationTypeIncludes = 
			(Set<PersonRelationType>) (personRelationTypeIncludeIDs == null ? null
			: new HashSet<PersonRelationType>(pm.getObjectsById(personRelationTypeIncludeIDs)));
		Person fromPerson = (Person) (filterCriteria.getFromPersonID() == null ? null : pm.getObjectById(filterCriteria.getFromPersonID()));
		Person toPerson = (Person) (filterCriteria.getToPersonID() == null ? null : pm.getObjectById(filterCriteria.getToPersonID()));

		Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, fromPerson, toPerson, personRelationTypeIncludes);
		// authority filtering
		filterPersonRelationsByRelationType(pm, relations, filterCriteria.getPersonRelationTypeExcludeIDs());
		filterPersonRelationsByAnchorPersons(filterCriteria.getFromPropertySetIDsToExclude(), filterCriteria.getToPropertySetIDsToExclude(), relations);
		relations = filterPersonRelationsByAuthority(pm, principal, relations);
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
		if (excludedRelationTypeIDs == null || excludedRelationTypeIDs.isEmpty())
			return;
		
		LinkedList<PersonRelation> toRemove = new LinkedList<PersonRelation>(relations);
		for (PersonRelation personRelation : relations) {
			PersonRelationTypeID relationTypeID = (PersonRelationTypeID) pm.getObjectId(personRelation.getPersonRelationType());
			if (excludedRelationTypeIDs.contains(relationTypeID)) {
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
		// Begin authority check, only return personRelations which persons are allowed to be seen
		Map<Person, PersonRelation> person2Relation = new HashMap<Person, PersonRelation>();
		for (PersonRelation personRelation : relations) {
			person2Relation.put(personRelation.getFrom(), personRelation);
			person2Relation.put(personRelation.getTo(), personRelation);
		}

		Set<Person> filteredPersons = Authority.filterSecuredObjects(pm, person2Relation.keySet(), principal, RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);
		
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
	
}
