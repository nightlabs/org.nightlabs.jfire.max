package org.nightlabs.jfire.personrelation;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.util.Util;

@PersistenceCapable(
		objectIdClass=PersonRelationTypeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFirePersonRelation_PersonRelationType"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class PersonRelationType
{
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String personRelationTypeID;

//	@Persistent(mappedBy="personRelationType")
	private PersonRelationTypeName name;

	private PersonRelationTypeDescription description;

	private String reversePersonRelationTypeID;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient PersonRelationTypeID _reversePersonRelationTypeID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PersonRelationType() { }

	public PersonRelationType(String organisationID, String personRelationTypeID, PersonRelationTypeID reversePersonRelationTypeID) {
		this.organisationID = organisationID;
		this.personRelationTypeID = personRelationTypeID;
		this.name = new PersonRelationTypeName(this);
		this.description = new PersonRelationTypeDescription(this);
		this._reversePersonRelationTypeID = reversePersonRelationTypeID;
		this.reversePersonRelationTypeID = reversePersonRelationTypeID == null ? null : reversePersonRelationTypeID.toString();
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public String getPersonRelationTypeID() {
		return personRelationTypeID;
	}

	public PersonRelationTypeName getName() {
		return name;
	}

	public PersonRelationTypeDescription getDescription() {
		return description;
	}

	public PersonRelationTypeID getReversePersonRelationTypeID() {
		if (_reversePersonRelationTypeID == null) {
			if (reversePersonRelationTypeID == null)
				return null;

			try {
				_reversePersonRelationTypeID = new PersonRelationTypeID(reversePersonRelationTypeID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return _reversePersonRelationTypeID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((personRelationTypeID == null) ? 0 : personRelationTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		PersonRelationType other = (PersonRelationType) obj;
		return (
				Util.equals(this.personRelationTypeID, other.personRelationTypeID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + personRelationTypeID + ']';
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager from this! " + this);

		return pm;
	}

	protected PersonRelationType getReversePersonRelationType()
	{
		PersistenceManager pm = getPersistenceManager();

		PersonRelationTypeID reversePersonRelationTypeID = getReversePersonRelationTypeID();
		if (reversePersonRelationTypeID == null)
			return this;

		return (PersonRelationType) pm.getObjectById(reversePersonRelationTypeID);
	}

	public void postPersonRelationCreated(PersonRelation personRelation)
	{
		PersistenceManager pm = getPersistenceManager();

		PersonRelationType reversePersonRelationType = getReversePersonRelationType();
		Person reverseTo = personRelation.getFrom();
		Person reverseFrom = personRelation.getTo();

		Collection<? extends PersonRelation> reverseRelations = PersonRelation.getPersonRelations(
				pm,
				reversePersonRelationType,
				reverseFrom,
				reverseTo
		);

		// there should only exist 0 or 1 reverse relation for the reversePersonRelationType
		PersonRelation reversePersonRelation = null;
		for (PersonRelation r : reverseRelations) {
			if (JDOHelper.isDeleted(r))
				continue;

			if (reversePersonRelation != null)
				throw new IllegalStateException("There should be only one reverse relation with this type! r1=" + reversePersonRelation + " r2=" + r);

			reversePersonRelation = r;
		}

		if (reversePersonRelation == null) {
			reversePersonRelation = reversePersonRelationType.createPersonRelation(
					reverseFrom,
					reverseTo
			);
		}
	}

	public PersonRelation createPersonRelation(Person from, Person to)
	{
		PersonRelation personRelation = new PersonRelation(this, from, to);

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null)
			personRelation = pm.makePersistent(personRelation);

		return personRelation;
	}

	public void postPersonRelationDeleted(PersonRelation personRelation)
	{
		PersistenceManager pm = getPersistenceManager();

		PersonRelationType reversePersonRelationType = getReversePersonRelationType();
		Person reverseTo = personRelation.getFrom();
		Person reverseFrom = personRelation.getTo();

		Collection<? extends PersonRelation> reverseRelations = PersonRelation.getPersonRelations(
				pm,
				reversePersonRelationType,
				reverseFrom,
				reverseTo
		);

		for (PersonRelation r : reverseRelations) {
			if (!JDOHelper.isDeleted(r))
				continue;

			pm.deletePersistent(r);
		}
	}
}
