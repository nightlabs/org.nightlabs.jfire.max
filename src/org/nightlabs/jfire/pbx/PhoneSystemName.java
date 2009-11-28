package org.nightlabs.jfire.pbx;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.pbx.id.PhoneSystemNameID;

/**
 * An extended class of {@link I18nText} that represents the {@link PhoneSystem}'s name.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=PhoneSystemNameID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireContactPBX_PhoneSystemName"
)
@FetchGroups({
	@FetchGroup(
			fetchGroups={"default"},
			name="PhoneSystem.name",
			members={@Persistent(name="phoneSystem"), @Persistent(name="names")}
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PhoneSystemName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue type's name belongs. Within one organisation,
	 * all the issue type's names have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String phoneSystemID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PhoneSystem phoneSystem;

	@Join
	@Persistent(table="JFireContactPBX_PhoneSystemName_names", defaultFetchGroup="true")
	private Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PhoneSystemName() {}

	public PhoneSystemName(PhoneSystem phoneSystem) {
		this.phoneSystem = phoneSystem;
		this.organisationID = this.phoneSystem.getOrganisationID();
		this.phoneSystemID = this.phoneSystem.getPhoneSystemID();
		this.names = new HashMap<String, String>();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return organisationID + "/" + phoneSystemID;
	}
}