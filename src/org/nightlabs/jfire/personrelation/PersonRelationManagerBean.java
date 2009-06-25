package org.nightlabs.jfire.personrelation;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationID;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.jfire.prop.id.PropertySetID;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PersonRelationManagerBean
extends BaseSessionBeanImpl
implements PersonRelationManagerRemote
{
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.friend;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new PersonRelationType(personRelationTypeID, null));
				}
			}

			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.employing;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.employed));
				}
			}
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.employed;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.employing));
				}
			}

			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.parent;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.child));
				}
			}
			{
				PersonRelationTypeID personRelationTypeID = PersonRelationType.PredefinedRelationTypes.child;
				try {
					pm.getObjectById(personRelationTypeID);
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new PersonRelationType(personRelationTypeID, PersonRelationType.PredefinedRelationTypes.parent));
				}
			}

		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationTypeID> getPersonRelationTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(PersonRelationType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			@SuppressWarnings("unchecked")
			Collection<PersonRelationTypeID> c = (Collection<PersonRelationTypeID>) q.execute();
			return new HashSet<PersonRelationTypeID>(c);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationType> getPersonRelationTypes(
			Collection<PersonRelationTypeID> personRelationTypeIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, personRelationTypeIDs, PersonRelationType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@Override
	public long getPersonRelationCount(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID, PropertySetID toPersonID
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			return PersonRelation.getPersonRelationCount(pm, personRelationType, fromPerson, toPerson);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelationID> getPersonRelationIDs(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) (personRelationTypeID == null ? null : pm.getObjectById(personRelationTypeID));
			Person fromPerson = (Person) (fromPersonID == null ? null : pm.getObjectById(fromPersonID));
			Person toPerson = (Person) (toPersonID == null ? null : pm.getObjectById(toPersonID));

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			return NLJDOHelper.getObjectIDList(relations);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public Collection<PersonRelation> getPersonRelations(
			Collection<PersonRelationID> personRelationIDs,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, personRelationIDs, PersonRelationType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public void createPersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		if (personRelationTypeID == null)
			throw new IllegalArgumentException("personRelationTypeID must not be null!");

		if (fromPersonID == null)
			throw new IllegalArgumentException("fromPersonID must not be null!");

		if (toPersonID == null)
			throw new IllegalArgumentException("toPersonID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) pm.getObjectById(personRelationTypeID);
			Person fromPerson = (Person) pm.getObjectById(fromPersonID);
			Person toPerson = (Person) pm.getObjectById(toPersonID);

			personRelationType.createPersonRelation(fromPerson, toPerson);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_") // TODO access rights
	@Override
	public void deletePersonRelation(
			PersonRelationTypeID personRelationTypeID,
			PropertySetID fromPersonID,
			PropertySetID toPersonID
	)
	{
		if (personRelationTypeID == null)
			throw new IllegalArgumentException("personRelationTypeID must not be null!");

		if (fromPersonID == null)
			throw new IllegalArgumentException("fromPersonID must not be null!");

		if (toPersonID == null)
			throw new IllegalArgumentException("toPersonID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			PersonRelationType personRelationType = (PersonRelationType) pm.getObjectById(personRelationTypeID);
			Person fromPerson = (Person) pm.getObjectById(fromPersonID);
			Person toPerson = (Person) pm.getObjectById(toPersonID);

			Collection<? extends PersonRelation> relations = PersonRelation.getPersonRelations(pm, personRelationType, fromPerson, toPerson);
			pm.deletePersistentAll(relations);
		} finally {
			pm.close();
		}
	}

}
