package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import org.nightlabs.jfire.trade.store.id.ProductTypeNameID;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.store.id.ProductTypeNameID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductType.name" fields="productType, names"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="names"
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */@PersistenceCapable(
	objectIdClass=ProductTypeNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductTypeName")
@FetchGroups({
	@FetchGroup(
		name="ProductType.name",
		members={@Persistent(name="productType"), @Persistent(name="names")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members=@Persistent(name="names")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members=@Persistent(name="names")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members=@Persistent(name="names")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members=@Persistent(name="names"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ProductTypeName
extends I18nText
implements Inheritable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String productTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ProductType productType;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_ProductTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_ProductTypeName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	protected Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypeName() { }

	public ProductTypeName(ProductType product)
	{
		this.productType = product;
		this.organisationID = product.getOrganisationID();
		this.productTypeID = product.getProductTypeID();
		this.names = new HashMap<String, String>();
	}
	
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}
	
	@Override
	protected String getFallBackValue(String languageID)
	{
		return ProductType.getPrimaryKey(organisationID, productTypeID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the productTypeID.
	 */
	public String getProductTypeID()
	{
		return productTypeID;
	}

	/**
	 * @return Returns the simpleProductType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}

	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if ("names".equals(fieldName))
			return new StaticFieldMetaData(fieldName);

		return null;
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		return new JDOSimpleFieldInheriter();
	}
}
