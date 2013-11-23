package org.nightlabs.jfire.pbx;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ClientOnlyPhoneSystem extends PhoneSystem
{
	private static final long serialVersionUID = 1L;

	public static final String ID_DEFAULT_CLIENT_ONLY_PHONE_SYSTEM = ClientOnlyPhoneSystem.class.getName() + ".DEFAULT"; //$NON-NLS-1$

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ClientOnlyPhoneSystem() { }

	public ClientOnlyPhoneSystem(boolean dummy) {
		super(dummy);
	}

	public ClientOnlyPhoneSystem(String organisationID, String phoneSystemID) {
		super(organisationID, phoneSystemID);
	}

	@Override
	public void call(Call call) throws PhoneSystemException {
		// dummy ;-)
	}

}
