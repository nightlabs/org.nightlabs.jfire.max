package org.nightlabs.jfire.pbx;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.pbx.id.PhoneSystemID;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=PhoneSystemID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFirePBX_PhoneSystem"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@FetchGroups({
	@FetchGroup(
			name="PhoneSystem.name",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="PhoneSystem.callableStructFields",
			members=@Persistent(name="callableStructFields")
	),
})
public abstract class PhoneSystem
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "PhoneSystem.name"; //$NON-NLS-1$
	public static final String FETCH_GROUP_CALLABLE_STRUCT_FIELDS = "PhoneSystem.callableStructFields"; //$NON-NLS-1$

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String phoneSystemID;

	/**
	 *  The name of the configuration.
	 *  The default PhoneSystem should be named “default”,
	 *  but this should not be a default value,
	 *  i.e. new instances should not have any name until the user specifies one.
	 */
	@Persistent(
			dependent="true",
			mappedBy="phoneSystem"
	)
	private PhoneSystemName name;

	/**
	 * The number that is required to start an international call. It is
	 * usually “00” (hence this should be the default), but they are different
	 * in many countries.
	 */
	@Persistent(nullValue=NullValue.EXCEPTION)
	private String internationalCallPrefix;

	/**
	 * A set of {@link StructField} used to specify the phone numbers that allow to be called.
	 */
	@Persistent(nullValue=NullValue.EXCEPTION)
	private Set<StructField> callableStructFields;
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected PhoneSystem() { }

	/**
	 * Create an instance of <code>PhoneSystem</code> with an automatically generated ID. This is
	 * usually the best way in a client-UI (users normally don't want/need to assign IDs themselves).
	 *
	 * @param dummy this argument is ignored and serves only for differentiating this constructor from the default constructor.
	 * @see #PhoneSystem(String, String)
	 */
	public PhoneSystem(boolean dummy) {
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextIDString(PhoneSystem.class)
		);
	}

	/**
	 * Create an instance of <code>PhoneSystem</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param phoneSystemID second part of the primary key. A local identifier within the namespace of the organisation.
	 * @see #PhoneSystem(boolean)
	 */
	public PhoneSystem(String organisationID, String phoneSystemID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(phoneSystemID, "phoneSystemID"); //$NON-NLS-1$
		this.organisationID = organisationID;
		this.phoneSystemID = phoneSystemID;
		this.internationalCallPrefix = "00"; //$NON-NLS-1$
		this.name = new PhoneSystemName(this);
		this.callableStructFields = new HashSet<StructField>();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getPhoneSystemID() {
		return phoneSystemID;
	}


	public PhoneSystemName getName() {
		return name;
	}

	public String getInternationalCallPrefix() {
		return internationalCallPrefix;
	}

	public Set<StructField> getCallableStructFields() {
		return Collections.unmodifiableSet(callableStructFields);
	}
	
	public boolean addCallableStructField(StructField structField) {
		return callableStructFields.add(structField);
	}
	
	public boolean removeCallableStructField(StructField structField) {
		return callableStructFields.remove(structField);
	}
	
	public void setInternationalCallPrefix(String internationalCallPrefix) {
		if (internationalCallPrefix == null)
			throw new IllegalArgumentException("internationalCallPrefix == null"); //$NON-NLS-1$

		this.internationalCallPrefix = internationalCallPrefix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((phoneSystemID == null) ? 0 : phoneSystemID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		PhoneSystem other = (PhoneSystem) obj;
		return (
				Util.equals(this.phoneSystemID, other.phoneSystemID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + phoneSystemID + ']';
	}

	public abstract void call(Call call) throws PhoneSystemException;

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of PhoneSystem is currently not persistent! Cannot obtain PersistenceManager! " + this); //$NON-NLS-1$

		return pm;
	}
}
