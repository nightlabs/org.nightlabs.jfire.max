package org.nightlabs.jfire.personrelation;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

/**
 * This exception is thrown to prevent a duplicate relation between two
 * persons. For every given {@link PersonRelationType} and one from-{@link Person}
 * and one to-<code>Person</code>, there must only exist 0 or 1 {@link PersonRelation}.
 * <p>
 * It is allowed to have multiple relations between the same two persons, if
 * the <code>PersonRelationType</code> or the direction is different. For example,
 * the following scenarios are legal:
 * </p>
 * <p>
 * <b><u>Scenario 1 (allowed):</u></b>
 * <ul>
 * <li>personA ---friend---&gt; personB
 * <li>personA ---employer---&gt; personB
 * </ul>
 * </p>
 * <p>
 * <b><u>Scenario 2 (allowed):</u></b>
 * <ul>
 * <li>personA ---friend---&gt; personB
 * <li>personB ---friend---&gt; personA
 * </ul>
 * </p>
 * <p>
 * But the following is illegal (and would cause this exception to be thrown):
 * </p>
 * <p>
 * <b><u>Scenario 3 (exception):</u></b>
 * <ul>
 * <li>personA ---friend---&gt; personB
 * <li>personA ---friend---&gt; personB
 * </ul>
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DuplicatePersonRelationException
extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private PersonRelationID alreadyExistingPersonRelationID;
	private PersonRelationTypeID personRelationTypeID;
	private PropertySetID fromPersonID;
	private PropertySetID toPersonID;

	private static String createMessage(PersonRelation alreadyExistingPersonRelation) {
		PersonRelationID alreadyExistingPersonRelationID = PersonRelationID.create(
				alreadyExistingPersonRelation.getOrganisationID(),
				alreadyExistingPersonRelation.getPersonRelationID()
		);
		PersonRelationTypeID personRelationTypeID = PersonRelationTypeID.create(
				alreadyExistingPersonRelation.getPersonRelationType().getOrganisationID(),
				alreadyExistingPersonRelation.getPersonRelationType().getPersonRelationTypeID()
		);
		PropertySetID fromPersonID = PropertySetID.create(
				alreadyExistingPersonRelation.getFrom().getOrganisationID(),
				alreadyExistingPersonRelation.getFrom().getPropertySetID()
		);
		PropertySetID toPersonID = PropertySetID.create(
				alreadyExistingPersonRelation.getTo().getOrganisationID(),
				alreadyExistingPersonRelation.getTo().getPropertySetID()
		);

		return (
				"There already exists another person relation of the same type between the same two persons!"
				+ " alreadyExisting='"
				+ alreadyExistingPersonRelationID
				+ "' type='"
				+ personRelationTypeID
				+ "' from='"
				+ fromPersonID
				+ "' to='"
				+ toPersonID
				+ "'"
		);
	}

	public DuplicatePersonRelationException(PersonRelation alreadyExistingPersonRelation) {
		super(createMessage(alreadyExistingPersonRelation));
		alreadyExistingPersonRelationID = PersonRelationID.create(
				alreadyExistingPersonRelation.getOrganisationID(),
				alreadyExistingPersonRelation.getPersonRelationID()
		);
		personRelationTypeID = PersonRelationTypeID.create(
				alreadyExistingPersonRelation.getPersonRelationType().getOrganisationID(),
				alreadyExistingPersonRelation.getPersonRelationType().getPersonRelationTypeID()
		);
		fromPersonID = PropertySetID.create(
				alreadyExistingPersonRelation.getFrom().getOrganisationID(),
				alreadyExistingPersonRelation.getFrom().getPropertySetID()
		);
		toPersonID = PropertySetID.create(
				alreadyExistingPersonRelation.getTo().getOrganisationID(),
				alreadyExistingPersonRelation.getTo().getPropertySetID()
		);
	}

	public PersonRelationID getAlreadyExistingPersonRelationID() {
		return alreadyExistingPersonRelationID;
	}

	public PersonRelationTypeID getPersonRelationTypeID() {
		return personRelationTypeID;
	}

	public PropertySetID getFromPersonID() {
		return fromPersonID;
	}

	public PropertySetID getToPersonID() {
		return toPersonID;
	}
}
