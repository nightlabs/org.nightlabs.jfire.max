package org.nightlabs.jfire.personrelation;

import java.util.Collection;

import javax.ejb.Remote;

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
}
