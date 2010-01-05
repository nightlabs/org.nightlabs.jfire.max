package org.nightlabs.jfire.personrelation;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

@Remote
public interface PersonRelationManagerRemote {

	void initialise() throws Exception;

	Collection<PersonRelationTypeID> getPersonRelationTypeIDs();

	Collection<PersonRelationType> getPersonRelationTypes(
			Collection<PersonRelationTypeID> personRelationTypeIDs,
			String[] fetchGroups, int maxFetchDepth
	);

	long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	Collection<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth
	);

	void createPersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	/**
	 * This method traverses the graph of Persons (PropertySetIDs) by following the relations that contained in the set
	 * of allowed ones (relationTypeIDs) until either the maximum depth (maxDepth) is reached or we encountered a person
	 * from which there is no other reachable via the allowed relation types.
	 *
	 * @param relationTypeIDs The allowed relation types that are used to filter the available relations from any person.
	 * @param startPoint The person from which the search starts.
	 * @param maxDepth The maximum depth until which the search continues.
	 * @return The set of persons that are left after either maxDepth search phases or have no allowed relation to any
	 * 	other reachable person.
	 */
	Set<Deque<ObjectID>> getNearestNodes(
			Set<PersonRelationTypeID> relationTypeIDs,
			PropertySetID startPoint, int maxDepth
	);

}
