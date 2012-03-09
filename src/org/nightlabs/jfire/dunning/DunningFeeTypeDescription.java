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
import org.nightlabs.jfire.dunning.id.DunningFeeTypeDescriptionID;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * An extended class of {@link I18nText} that represents the description created in an {@link DunningFeeType}.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=DunningFeeTypeDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_FeeTypeDescription")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFeeTypeDescription
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the dunningFeeType description belongs. Within one organisation,
	 * all the dunningFeeType descriptions have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long dunningFeeTypeID;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private DunningFeeType dunningFeeType;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireDunning_DunningFeeTypeDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_FeeTypeDescription_texts",
		defaultFetchGroup="true")
	@Value( columns={@Column(sqlType="CLOB")} )
	private Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningFeeTypeDescription()
	{
	}

	public DunningFeeTypeDescription(DunningFeeType dunningFeeType)
	{
		this.organisationID = dunningFeeType.getOrganisationID();
		this.dunningFeeTypeID = IDGenerator.nextID(DunningFeeTypeDescription.class);
		this.descriptions = new HashMap<String, String>();
	}

//	/**
//	 * Constructs a new DunningFeeTypeDescription.
//	 * @param dunningFeeType the dunningFeeType that this dunningFeeType description is made in
//	 */
//	public DunningFeeTypeDescription(String organisationID, long dunningFeeTypeID, DunningFeeType dunningFeeType)
//	{
//		this.dunningFeeType = dunningFeeType;
//		this.organisationID = organisationID;
//		this.dunningFeeTypeID = dunningFeeTypeID;
//	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * Returns the dunningFeeType id.
	 * @return the dunningFeeType id
	 */
	public long getDunningFeeTypeID()
	{
		return dunningFeeTypeID;
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