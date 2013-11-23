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
import org.nightlabs.jfire.dunning.id.DunningConfigDescriptionID;

/**
 * An extended class of {@link I18nText} that represents the description created in an {@link DunningConfig}.
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 * @author Marius Heinzmann <!-- Marius[DOT]Heinzmann[AT]NightLabs[DOT]de -->
 */
@PersistenceCapable(
		objectIdClass=DunningConfigDescriptionID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ConfigDescription")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfigDescription
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the dunningConfig description belongs. Within one organisation,
	 * all the dunningConfig descriptions have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningConfigID;

	@Join
	@Persistent(
		defaultFetchGroup="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_ConfigDescription_texts")
	@Value( columns={@Column(jdbcType="CLOB")} )
	private Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningConfigDescription() {}

	/**
	 * Constructs a new DunningConfigDescription.
	 * @param dunningConfig the dunningConfig that this dunningConfig description is made in
	 */
	public DunningConfigDescription(DunningConfig dunningConfig)
	{
		assert dunningConfig != null : "dunningConfig must NOT be null!";
		this.organisationID = dunningConfig.getOrganisationID();
		this.dunningConfigID = dunningConfig.getDunningConfigID();
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * Returns the dunningConfig id.
	 * @return the dunningConfig id
	 */
	public long getDunningConfigID()
	{
		return dunningConfigID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return "";
	}
}