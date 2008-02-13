package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Calendar;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 *  @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.IDPrefixCfID"
 *		detachable="true"
 *		table="JFireTrade_IDPrefixCf"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, idPrefixCfID"
 */
public class IDPrefixCf
implements Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * Use the current year automatically without asking the user.
	 */
	public static final String STRATEGY_YEAR_AUTO = "YearAuto";
	/**
	 * Ask the user, but propose him the current year. He can choose from a combo showing
	 * all the {@link #globalIDPrefixes }, the previous, the current and the next year (years as first items).
	 */
	public static final String STRATEGY_YEAR_ASK = "YearAsk";
	/**
	 * Use the default value from this ConfigModule automatically without asking the user.
	 */
	public static final String STRATEGY_DEFAULT_AUTO = "DefaultAuto";
	/**
	 * Ask the user, but propose him the default id-prefix. He can choose from a combo showing
	 * all the {@link #globalIDPrefixes }.
	 */
	public static final String STRATEGY_DEFAULT_ASK = "DefaultAsk";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long idPrefixCfID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IDPrefixCf() { }

	public IDPrefixCf(
			String organisationID, long idPrefixCfID,
			TradeConfigModule tradeConfigModule, String articleContainerClassName)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");

		if (idPrefixCfID < 0)
			throw new IllegalArgumentException("idPrefixCfID < 0");

		this.organisationID = organisationID;
		this.idPrefixCfID = idPrefixCfID;
		this.tradeConfigModule = tradeConfigModule;
		this.articleContainerClassName = articleContainerClassName;

		strategy = STRATEGY_YEAR_AUTO;
//		values = new ArrayList();
	}

	private String strategy = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="50"
	 */
	private String defaultValue = null;

	public static final String ARTICLE_CONTAINER_CLASS_NAME_GLOBAL = "global";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String articleContainerClassName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private TradeConfigModule tradeConfigModule;

//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="java.lang.String"
//	 *		table="JFireTrade_IDPrefixCf_values"
//	 *
//	 * @jdo.order column="orderIndex"
//	 */
//	private List values = null;

	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getIdPrefixCfID()
	{
		return idPrefixCfID;
	}

	public String getArticleContainerClassName()
	{
		return articleContainerClassName;
	}

	public TradeConfigModule getTradeConfigModule()
	{
		return tradeConfigModule;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String getStrategy()
	{
		return strategy;
	}

	public void setStrategy(String strategy)
	{
		this.strategy = strategy;
	}

//	public List getValues()
//	{
//		return values;
//	}

	public String getDefaultIDPrefix()
	{
		if (IDPrefixCf.STRATEGY_YEAR_AUTO.equals(getStrategy()) || IDPrefixCf.STRATEGY_YEAR_ASK.equals(getStrategy()))
			return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		else if (IDPrefixCf.STRATEGY_DEFAULT_AUTO.equals(getStrategy()) || IDPrefixCf.STRATEGY_DEFAULT_ASK.equals(getStrategy())) {
			String res = getDefaultValue();
			if (!ObjectIDUtil.isValidIDString(res))
				throw new IllegalStateException("Invalid defaultValue in IDPrefixCf: " + JDOHelper.getObjectId(this));
			return res;
		}
		else
			throw new IllegalStateException("Unknown strategy in IDPrefixCf: " + JDOHelper.getObjectId(this));
	}

}
