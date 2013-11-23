package org.nightlabs.jfire.dunning;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Value;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.dunning.id.DunningConfigNameID;

/**
 * An extended class of {@link I18nText} that represents the {@link DunningConfig}'s name.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningConfigNameID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ConfigName"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfigName
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
	private long dunningConfigID;

	@Join
	@Persistent(table="JFireDunning_ConfigName_texts", defaultFetchGroup="true", nullValue=NullValue.EXCEPTION)
	@Value( columns={@Column(jdbcType="CLOB")} )
	private Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningConfigName() {}

	public DunningConfigName(DunningConfig dunningConfig)
	{
		assert dunningConfig != null : "dunningConfig must NOT be null!";
		this.organisationID = dunningConfig.getOrganisationID();
		this.dunningConfigID = dunningConfig.getDunningConfigID();
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
		return organisationID + "/" + dunningConfigID; //$NON-NLS-1$
	}
}