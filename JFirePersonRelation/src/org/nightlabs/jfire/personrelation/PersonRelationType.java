package org.nightlabs.jfire.personrelation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeID;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

@PersistenceCapable(
		objectIdClass=PersonRelationTypeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFirePersonRelation_PersonRelationType"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@FetchGroups({
	@FetchGroup(
			name=PersonRelationType.FETCH_GROUP_NAME,
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name=PersonRelationType.FETCH_GROUP_DESCRIPTION,
			members=@Persistent(name="description")
	),
	@FetchGroup(
			name=PersonRelationType.FETCH_GROUP_ICON16x16DATA,
			members=@Persistent(name="icon16x16Data")
	)
})
public class PersonRelationType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final class PredefinedRelationTypes {
		public static final PersonRelationTypeID friend = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "friend"
		);

		public static final PersonRelationTypeID employing = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "employing"
		);
		public static final PersonRelationTypeID employed = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "employed"
		);

		public static final PersonRelationTypeID parent = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "parent"
		);
		public static final PersonRelationTypeID child = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "child"
		);

		public static final PersonRelationTypeID companyGroup = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "companyGroup"
		);
		public static final PersonRelationTypeID subsidiary = PersonRelationTypeID.create(
				Organisation.DEV_ORGANISATION_ID, "subsidiary"
		);

//		public static final PersonRelationTypeID branchOffice = PersonRelationTypeID.create(
//				Organisation.DEV_ORGANISATION_ID, "branch"
//		);
	}

	public static final String FETCH_GROUP_NAME = "PersonRelationType.name";
	public static final String FETCH_GROUP_DESCRIPTION = "PersonRelationType.description";
	public static final String FETCH_GROUP_ICON16x16DATA = "PersonRelationType.icon16x16Data";

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
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] icon16x16Data;
																																					
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PersonRelationType() { }

	public PersonRelationType(PersonRelationTypeID personRelationTypeID, PersonRelationTypeID reversePersonRelationTypeID)
	{
		this(
				personRelationTypeID.organisationID,
				personRelationTypeID.personRelationTypeID,
				reversePersonRelationTypeID
		);
	}

	public PersonRelationType(String organisationID, String personRelationTypeID, PersonRelationTypeID reversePersonRelationTypeID)
	{
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
			reversePersonRelation = reversePersonRelationType.createReversePersonRelation(
					reverseFrom,
					reverseTo,
					personRelation
			);
		}
	}

	/**
	 * Method used by standard-implementation of
	 * {@link #createPersonRelation(Person, Person)} to create the instance of
	 * the PersonRelation to be persisted.
	 * 
	 * @param from The Person the relation should link from.
	 * @param to The Person the relation should link to.
	 * @return A new instance (not yet persisted) instance of {@link PersonRelation}.
	 */
	protected PersonRelation createNewPersonRelationInstance(Person from, Person to) {
		return new PersonRelation(this, from, to);
	}
	
	public PersonRelation createPersonRelation(Person from, Person to)
	{
		PersonRelation personRelation = createNewPersonRelationInstance(from, to);

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null)
			personRelation = pm.makePersistent(personRelation);

		return personRelation;
	}
	
	public PersonRelation createReversePersonRelation(Person from, Person to, PersonRelation personRelation)
	{
		return createPersonRelation(from, to);
	}	

	public void prePersonRelationDelete(PersonRelation personRelation)
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
			if (JDOHelper.isDeleted(r))
				continue;

			pm.deletePersistent(r);
		}
	}
	
	public byte[] getIcon16x16Data()
	{
		return icon16x16Data;
	}
	public void setIcon16x16Data(byte[] icon16x16Data)
	{
		this.icon16x16Data = icon16x16Data;
	}

	/**
	 * Calls {@link #loadIconFromResource(Class, String) } with <code>resourceLoaderClass == this.getClass()</code>
	 * {@link PersonRelationType} and <code>fileName == "PersonRelationType-" + personRelationTypeID + ".16x16.png"</code>.
	 * This method is used for the default {@link PersonRelationType}s created by the module-initialisation.
	 *
	 * @throws IOException
	 */
	public void loadIconFromResource() throws IOException
	{
		String resourcePath = "resource/" + PersonRelationType.class.getSimpleName() + '-' + personRelationTypeID + ".16x16.png";
		loadIconFromResource(
				PersonRelationType.class, resourcePath);
	}

	/**
	 * This method loads an icon from a resource file by calling the method
	 * {@link Class#getResourceAsStream(String)} of
	 * <code>resourceLoaderClass</code>.
	 *
	 * @param resourceLoaderClass The class that is used for loading the file.
	 * @param fileName A filename relative to <code>resourceLoaderClass</code>. Note, that subdirectories are possible, but ".." not.
	 * @throws IOException If loading the resource failed. This might be a {@link FileNotFoundException}.
	 */
	public void loadIconFromResource(Class<?> resourceLoaderClass, String fileName) throws IOException
	{
		InputStream in = resourceLoaderClass.getResourceAsStream(fileName);
		if (in == null)
			throw new FileNotFoundException("Could not find resource: " + fileName);
		try {
			loadIconFromStream(in);
		} finally {
			in.close();
		}
	}
	
	/**
	 * Loads the icon from the given InputStream.
	 * 
	 * @param in The stream to fread from.
	 * @throws IOException ...
	 */
	public void loadIconFromStream(InputStream in) throws IOException {
		DataBuffer db = new DataBuffer(512);
		OutputStream out = db.createOutputStream();
		try {
			IOUtil.transferStreamData(in, out);
		} finally {
			out.close();
		}
		this.icon16x16Data = db.createByteArray();
	}	
}
