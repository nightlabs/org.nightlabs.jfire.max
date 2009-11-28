package org.nightlabs.jfire.pbx;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.pbx.id.DefaultPhoneSystemID;

/**
 * One datastore-singleton instance of this class defines the default {@link PhoneSystem} of
 * the local organisation.
 * <p>
 * Since the basic {@link PhoneSystem} is abstract, the {@link #getPhoneSystem()} method initially returns <code>null</code>,
 * until a plug-in providing a concrete <code>PhoneSystem</code> implementation is installed.
 * </p>
 * <p>
 * Plugins containing a concrete {@link PhoneSystem} implementation should set their own default <code>PhoneSystem</code>, if
 * there is no other default yet.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=DefaultPhoneSystemID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireContactPBX_DefaultPhoneSystem"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DefaultPhoneSystem
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	private PhoneSystem phoneSystem;

	/**
	 * @deprecated only for jdo!
	 */
	@Deprecated
	protected DefaultPhoneSystem() { }

	public static DefaultPhoneSystem getDefaultPhoneSystem(PersistenceManager pm)
	{
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		DefaultPhoneSystem defaultPhoneSystem;
		try {
			defaultPhoneSystem = (DefaultPhoneSystem) pm.getObjectById(DefaultPhoneSystemID.create(organisationID));
		} catch (JDOObjectNotFoundException x) {
			defaultPhoneSystem = new DefaultPhoneSystem(organisationID);
			defaultPhoneSystem = pm.makePersistent(defaultPhoneSystem);
		}
		return defaultPhoneSystem;
	}

	protected DefaultPhoneSystem(String organisationID)
	{
		this.organisationID = organisationID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

//	public PhoneSystem getPhoneSystem(boolean throwExceptionIfUndefined) {
//		if (phoneSystem == null)
//			throw new IllegalStateException("No default phone system configured! Make sure you have installed at least one phone system implementation plug-in!");
//
//		return phoneSystem;
//	}

	/**
	 * Get the default phone system or <code>null</code>, if there is none for the current organisation.
	 * @return the local organisation's default phone system or <code>null</code>.
	 */
	public PhoneSystem getPhoneSystem() {
		return phoneSystem;
	}
	/**
	 * Set the default phone system or <code>null</code>, if there is none for the current organisation.
	 * @param phoneSystem the new default phone system or <code>null</code>.
	 */
	public void setPhoneSystem(PhoneSystem phoneSystem) {
		this.phoneSystem = phoneSystem;
	}
}
