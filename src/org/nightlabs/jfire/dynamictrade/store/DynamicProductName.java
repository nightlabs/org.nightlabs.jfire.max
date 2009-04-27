/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.dynamictrade.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.dynamictrade.store.id.DynamicProductNameID;
import org.nightlabs.jfire.store.Product;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.dynamictrade.store.id.DynamicProductNameID"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productID"
 *
 * @jdo.fetch-group name="DynamicProduct.name" fields="dynamicProduct, names"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default, DynamicProduct.name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default, DynamicProduct.name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default, DynamicProduct.name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default, DynamicProduct.name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInReceptionNoteEditor" fetch-groups="default, DynamicProduct.name"
 */
@PersistenceCapable(
	objectIdClass=DynamicProductNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProductName")
@FetchGroups({
	@FetchGroup(
		name="DynamicProduct.name",
		members={@Persistent(name="dynamicProduct"), @Persistent(name="names")}),
	@FetchGroup(
		fetchGroups={"default", "DynamicProduct.name"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members={}),
	@FetchGroup(
		fetchGroups={"default", "DynamicProduct.name"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members={}),
	@FetchGroup(
		fetchGroups={"default", "DynamicProduct.name"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={}),
	@FetchGroup(
		fetchGroups={"default", "DynamicProduct.name"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={}),
	@FetchGroup(
		fetchGroups={"default", "DynamicProduct.name"},
		name="FetchGroupsTrade.articleInReceptionNoteEditor",
		members={})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicProductName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
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
	private long productID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DynamicProduct dynamicProduct;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireDynamicTrade_DynamicProductName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDynamicTrade_DynamicProductName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductName()
	{
	}

	public DynamicProductName(DynamicProduct dynamicProduct)
	{
		this.dynamicProduct = dynamicProduct;
		this.organisationID = dynamicProduct.getOrganisationID();
		this.productID = dynamicProduct.getProductID();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return Product.getPrimaryKey(organisationID, productID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getProductID()
	{
		return productID;
	}

	public DynamicProduct getDynamicProduct()
	{
		return dynamicProduct;
	}
}
