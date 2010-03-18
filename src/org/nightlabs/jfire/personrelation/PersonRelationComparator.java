package org.nightlabs.jfire.personrelation;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * A {@link Comparator} for sorting {@link PersonRelation}s according to some specifications.
 *
 * @author khaireel (at) nightlabs (dot) de
 */
public class PersonRelationComparator implements Comparator<PersonRelation>, Serializable {
	private static final long serialVersionUID = 1865014589351710086L;

	// The hierarchical order of PersonRelationType nodes.
	protected final LinkedList<String> personRelationTypeOrder = new LinkedList<String>() {
		private static final long serialVersionUID = 5701024044956571424L;
	{
		add(PersonRelationType.PredefinedRelationTypes.companyGroup.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.subsidiary.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.employing.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.employed.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.parent.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.child.personRelationTypeID);
		add(PersonRelationType.PredefinedRelationTypes.friend.personRelationTypeID);
	} };

	@Override
	public int compare(PersonRelation pr1, PersonRelation pr2) {
		int compVal = personRelationTypeOrder.indexOf(pr1.getPersonRelationType().getReversePersonRelationTypeID().personRelationTypeID)
                      - personRelationTypeOrder.indexOf(pr2.getPersonRelationType().getReversePersonRelationTypeID().personRelationTypeID);

		return compVal == 0 ? pr1.getTo().getDisplayName().compareTo( pr2.getTo().getDisplayName() ) : compVal;
	}
}
