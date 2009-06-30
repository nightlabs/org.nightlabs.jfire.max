package org.nightlabs.jfire.personrelation;

import java.util.ArrayList;
import java.util.Collection;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeMultiParentResolver;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;

public class PersonRelationParentResolver
implements TreeNodeMultiParentResolver
{
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<ObjectID> getParentObjectIDs(Object jdoObject) {
		if (jdoObject instanceof PersonRelation) {
			PersonRelation personRelation = (PersonRelation)jdoObject;
			Collection<PersonRelationID> fromPersonRelationIDs = personRelation.getFromPersonRelationIDs();
			Collection<ObjectID> result = new ArrayList<ObjectID>(fromPersonRelationIDs.size() + 1);
			result.add(personRelation.getFromID());
			result.addAll(fromPersonRelationIDs);
			return result;
		}

		return null;
	}

}
