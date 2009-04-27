package org.nightlabs.jfire.dynamictrade.recurring;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.dynamictrade.recurring.id.DynamicProductTypeRecurringArticleNameID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;


/**
 * @author Fitas Amine - fitas at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.dynamictrade.recurring.id.DynamicProductTypeRecurringArticleNameID"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductTypeRecurringArticleName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, dynamicProductTypeRecurringArticleID"
 * 
 * @jdo.fetch-group name="DynamicProductTypeRecurringArticle.name" fields="dynamicProductTypeRecurringArticle, names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="dynamicProductTypeRecurringArticle, names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="dynamicProductTypeRecurringArticle, names"
 */
@PersistenceCapable(
	objectIdClass=DynamicProductTypeRecurringArticleNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProductTypeRecurringArticleName")
@FetchGroups({
	@FetchGroup(
		name="DynamicProductTypeRecurringArticle.name",
		members={@Persistent(name="dynamicProductTypeRecurringArticle"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="dynamicProductTypeRecurringArticle"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="dynamicProductTypeRecurringArticle"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicProductTypeRecurringArticleName extends I18nText {

	/**
	 * The serial version of this class.
	 */
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
	private long  dynamicProductTypeRecurringArticleID; 
	

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DynamicProductTypeRecurringArticle dynamicProductTypeRecurringArticle;


	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireDynamicTrade_DynamicProductTypeRecurringArticleName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDynamicTrade_DynamicProductTypeRecurringArticleName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	public DynamicProductTypeRecurringArticleName(DynamicProductTypeRecurringArticle dynamicProductTypeRecurringArticle)
	{
		this.dynamicProductTypeRecurringArticle = dynamicProductTypeRecurringArticle;
		this.organisationID = dynamicProductTypeRecurringArticle.getOrganisationID();
		this.dynamicProductTypeRecurringArticleID = dynamicProductTypeRecurringArticle.getArticleID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return DynamicProductTypeRecurringArticle.getPrimaryKey(organisationID,dynamicProductTypeRecurringArticleID);
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}
	
	public DynamicProductTypeRecurringArticle getDynamicProductTypeRecurringArticle() {
		return dynamicProductTypeRecurringArticle;
	}
}



