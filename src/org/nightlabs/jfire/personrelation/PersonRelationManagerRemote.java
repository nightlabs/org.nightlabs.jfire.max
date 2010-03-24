package org.nightlabs.jfire.personrelation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
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

	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			PersonRelationComparator personRelationComparator // Leave this null to indicate no sorting
	);

	Collection<PersonRelationID> getFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude,
			PersonRelationComparator personRelationComparator // Leave this null to indicate no sorting
	);

	long getFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude
	);

	Collection<PersonRelationID> getInclusiveFilteredPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToInclude,
			Set<PropertySetID> toPropertySetIDsToInclude,
			PersonRelationComparator personRelationComparator // Leave this null to indicate no sorting
	);

	long getInclusiveFilteredPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			Set<PropertySetID> fromPropertySetIDsToExclude,
			Set<PropertySetID> toPropertySetIDsToExclude
	);
	
	
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	// Test. May start this on a new class, since the methods are more intended to be used to serve TuckedPersonRelationTreeNodes.
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|
	public class TuckedQueryCount implements Serializable {
		private static final long serialVersionUID = 8694083868135075128L;
		public long actualChildCount;
		public long tuckedChildCount;
	}

	TuckedQueryCount getTuckedPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID currentID,
			Set<PropertySetID> propertySetIDsToRoot,
			Set<PropertySetID> propertySetIDsToTuckedChildren
	);
	// -------------- ++++++++++ ----------------------------------------------------------------------------------------------------------- ++ ----|

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

	Map<Class<? extends ObjectID>, List<Deque<ObjectID>>> getRootNodes(
			Set<PersonRelationTypeID> relationTypeIDs,
			PropertySetID startPoint, int maxDepth);

}
