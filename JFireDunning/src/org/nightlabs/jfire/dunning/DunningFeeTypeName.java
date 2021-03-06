package org.nightlabs.jfire.dunning;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeNameID;

/**
 * An extended class of {@link I18nText} that represents the {@link DunningFeeType}'s name.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningFeeTypeNameID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_FeeTypeName"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningFeeTypeName
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
	private long dunningFeeTypeID;

	@Join
	@Persistent(table="JFireDunning_FeeTypeName_texts", defaultFetchGroup="true")
	private Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DunningFeeTypeName() {}

	public DunningFeeTypeName(DunningFeeType dunningFeeType)
	{
		this.organisationID = dunningFeeType.getOrganisationID();
		this.dunningFeeTypeID = dunningFeeType.getFeeTypeID();
		this.names = new HashMap<String, String>();
	}

	public String getOrganisationID()
	{
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
		return organisationID + "/" + dunningFeeTypeID; //$NON-NLS-1$
	}
}