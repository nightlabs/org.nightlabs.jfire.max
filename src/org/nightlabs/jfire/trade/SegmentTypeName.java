/*
 * Created on Apr 20, 2005
 */
package org.nightlabs.jfire.trade;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.SegmentTypeNameID"
 *		detachable="true"
 *		table="JFireTrade_SegmentTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, segmentTypeID"
 *
 * @jdo.fetch-group name="SegmentType.name" fields="segmentType, names"
 * @jdo.fetch-group name="SegmentType.this" fields="segmentType, names"
 */
public class SegmentTypeName extends I18nText
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String segmentTypeID;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		dependent="true"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_SegmentTypeName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, javax.jdo.PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private SegmentType segmentType;

	/**
	 * @deprecated Only for JDO!
	 */
	protected SegmentTypeName()
	{
	}

	public SegmentTypeName(SegmentType segmentType)
	{
		this.organisationID = segmentType.getOrganisationID();
		this.segmentTypeID = segmentType.getSegmentTypeID();
		this.segmentType = segmentType;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue)
	{
		this.name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText()
	{
		return name;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return CustomerGroup.getPrimaryKey(organisationID, segmentTypeID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getSegmentTypeID()
	{
		return segmentTypeID;
	}

	public SegmentType getSegmentType()
	{
		return segmentType;
	}

}
