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

	/**
	 * Get the number of PersonRelations between the given two persons. If
	 * personRelationTypeID is set the result will be limited to PersonRelations
	 * of that type.
	 * 
	 * @deprecated Use {@link #getPersonRelationCount(PersonRelationFilterCriteria)} instead for more flexibility.
	 * 
	 * @param personRelationTypeID The PersonRelationTypeID to limit the result to.
	 * @param fromPersonID The from-anchor of the relations.
	 * @param toPersonID The to-anchor of the relations.
	 * @return The number of PersonRelations of the given criteria.
	 */
	@Deprecated
	long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	/**
	 * Get the number of PersonRelations for the given criteria. 
	 * You need to set at least toPersonID and fromPersonID in the criteria. 
	 * Additionally you can either limit the result to certain PersonRelationTypes ({@link PersonRelationFilterCriteria#setPersonRelationTypeIncludeIDs(Collection)})
	 * or exclude a list of types ({@link PersonRelationFilterCriteria#setPersonRelationTypeExcludedIDs(Collection)}).
	 * 
	 * @param personFilterCriteria The criteria to limit the result..
	 * @return The number of PersonRelations of the given criteria.
	 */
	long getPersonRelationCount(PersonRelationFilterCriteria personFilterCriteria);
	
	/**
	 * Get the PersonRelations between the given two persons. If
	 * personRelationTypeID is set the result will be limited to PersonRelations
	 * of that type.
	 * 
	 * @deprecated Use {@link #getPersonRelationIDs(PersonRelationFilterCriteria)} instead for more flexibility.
	 * 
	 * @param personRelationTypeID The PersonRelationTypeID to limit the result to.
	 * @param fromPersonID The from-anchor of the relations.
	 * @param toPersonID The to-anchor of the relations.
	 * @return The PersonRelations of the given criteria.
	 */
	@Deprecated
	Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	);

	/**
	 * Get the PersonRelations between the given two persons. If
	 * personRelationTypeID is set the result will be limited to PersonRelations
	 * of that type.
	 * 
	 * @deprecated Use {@link #getPersonRelationIDs(PersonRelationFilterCriteria)} instead for more flexibility.
	 * 
	 * @param personRelationTypeID The PersonRelationTypeID to limit the result to.
	 * @param fromPersonID The from-anchor of the relations.
	 * @param toPersonID The to-anchor of the relations.
	 * @param personRelationComparator A comparator to sort the result.
	 * @return The PersonRelations of the given criteria.
	 */
	@Deprecated
	Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID,
			PersonRelationComparator personRelationComparator // Leave this null to indicate no sorting
	);

	/**
	 * Get the PersonRelations for the given criteria. You need to set at least
	 * toPersonID and fromPersonID in the criteria. Additionally you can either
	 * limit the result to certain PersonRelationTypes ({@link PersonRelationFilterCriteria#setPersonRelationTypeIncludeIDs(Collection)}) 
	 * or exclude a list of types ({@link PersonRelationFilterCriteria#setPersonRelationTypeExcludedIDs(Collection)}) 
	 * as well as set a Comparator to sort the result ({@link PersonRelationFilterCriteria#setPersonRelationComparator(PersonRelationComparator)}).
	 * 
	 * @param personFilterCriteria The criteria to limit the result..
	 * @return The PersonRelations of the given criteria.
	 */
	Collection<PersonRelationID> getPersonRelationIDs(PersonRelationFilterCriteria personRelationFilterCriteria);
	

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
	
	List<PropertySetID> getRootNodes(Set<PersonRelationTypeID> relationTypeIDs,
			Set<PropertySetID> personIDs, int maxDepth);	

}
