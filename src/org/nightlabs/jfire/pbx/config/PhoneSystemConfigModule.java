package org.nightlabs.jfire.pbx.config;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.pbx.DefaultPhoneSystem;
import org.nightlabs.jfire.pbx.PhoneSystem;

/**
 * This is a subclass of {@link org.nightlabs.jfire.config.ConfigModule} and
 * will be managed as workstation-config-module
 * (bound to the workstation, not to the user).
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireContactPhoneSystem_PhoneSystemConfigModule"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
			name=PhoneSystemConfigModule.FETCH_GROUP_PHONE_SYSTEM,
			members=@Persistent(name="phoneSystem")
	),
})
public class PhoneSystemConfigModule
extends ConfigModule
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PHONE_SYSTEM = "PhoneSystemConfigModule.phoneSystem";

	/**
	 * Which configuration (i.e. which PhoneSystem server) to use.
	 * This should default to the “default” PhoneSystem as configured in {@link DefaultPhoneSystem}.
	 */
	private PhoneSystem phoneSystem;

	@Override
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		this.phoneSystem = DefaultPhoneSystem.getDefaultPhoneSystem(pm).getPhoneSystem();
	}

	public PhoneSystem getPhoneSystem() {
		return phoneSystem;
	}

	public void setPhoneSystem(PhoneSystem phoneSystem) {
		if (phoneSystem == null)
			throw new IllegalArgumentException("phoneSystem == null");

		this.phoneSystem = phoneSystem;
	}

	@Override
	public void preInherit(Inheritable mother, Inheritable child) {
		super.preInherit(mother, child);
		getPhoneSystem();
	}
}