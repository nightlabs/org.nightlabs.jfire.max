package org.nightlabs.jfire.personrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeMultiParentResolver;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;

public class PersonRelationParentResolver
implements TreeNodeMultiParentResolver
{
	private static final long serialVersionUID = 1L;

	private List<TreeNodeMultiParentResolver> delegates;

	@Override
	public Collection<ObjectID> getParentObjectIDs(Object jdoObject) {
		Collection<ObjectID> result = null;

		if (delegates != null) {
			for (TreeNodeMultiParentResolver delegate : delegates) {
				Collection<ObjectID> parentObjectIDs = delegate.getParentObjectIDs(jdoObject);
				if (parentObjectIDs != null && !parentObjectIDs.isEmpty()) {
					if (result == null)
						result = new ArrayList<ObjectID>(parentObjectIDs.size());

					result.addAll(parentObjectIDs);
				}
			}
		}

		if (jdoObject instanceof PersonRelation) {
			PersonRelation personRelation = (PersonRelation)jdoObject;
			Collection<PersonRelationID> fromPersonRelationIDs = personRelation.getFromPersonRelationIDs();

			if (result == null)
				result = new ArrayList<ObjectID>(fromPersonRelationIDs.size() + 1);

			result.add(personRelation.getFromID());
			result.addAll(fromPersonRelationIDs);
		}

		return result;
	}

	public void addDelegate(TreeNodeMultiParentResolver delegate) {
		if (delegates == null)
			delegates = new LinkedList<TreeNodeMultiParentResolver>();

		delegates.add(delegate);
	}

	public void removeDelegate(TreeNodeMultiParentResolver delegate) {
		if (delegates == null)
			return;

		delegates.remove(delegate);

		if (delegates.isEmpty())
			delegates = null;
	}
}
