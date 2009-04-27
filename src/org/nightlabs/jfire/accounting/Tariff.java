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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting;

import java.io.Serializable;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * TODO Shall I really put this class into JFireTrade? Or is it Ticketing specific?
 *			Does it help me here or does it make things complicated?
 *			To have it here makes sense, if I define directly in CustomerGroup which Tariffs
 *			they're allowed to sell. Or should I better use our Authority based ACL to manage
 *			Tariffs? Or should a CustomerGroup automatically create an Authority? That seems to
 *			make sense. We wanted to make Authorities markable "internal" and then use them for
 *			such purposes...
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.TariffID"
 *		detachable="true"
 *		table="JFireTrade_Tariff"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, tariffID"
 *		include-body="id/TariffID.body.inc"
 *
 * @jdo.fetch-group name="Tariff.name" fields="name"
 * @jdo.fetch-group name="Tariff.this" fetch-groups="default" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="name"
 *
 * @jdo.query
 *		name="getTariffByName"
 *		query="SELECT
 *			WHERE
 *				this.name.names.get(paramLanguageID)==paramName
 *		PARAMETERS String paramLanguageID, String paramName
 *		import java.lang.String;"
 */
@PersistenceCapable(
	objectIdClass=TariffID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Tariff")
@FetchGroups({
	@FetchGroup(
		name=Tariff.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={"default"},
		name=Tariff.FETCH_GROUP_THIS_TARIFF,
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsEntityUserSet.replicateToReseller",
		members=@Persistent(name="name"))
})
//@Queries(
//	@javax.jdo.annotations.Query(name="getTariffByName", value="SELECT WHERE this.name.names.get(:paramLanguageID) == :paramName")
//)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Tariff
implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Tariff.name"; //$NON-NLS-1$

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_TARIFF = "Tariff.this"; //$NON-NLS-1$

//	/**
//	 * Return a {@link Collection} of Tariffs with the given name in the given Locale language.
//	 * @param pm the PersistenceManager to use
//	 * @param name the name in the given locale language
//	 * @param locale the Locale to search with its language in the I18nText of the tariff
//	 * @return a {@link Collection} of Tariffs with the given name in the given Locale language
//	 */
//	@SuppressWarnings("unchecked")
//	public static Collection<Tariff> getTariffByName(PersistenceManager pm, String name, Locale locale) {
//		Query q = pm.newNamedQuery(Tariff.class, "getTariffByName"); //$NON-NLS-1$
//		return (Collection<Tariff>)q.execute(locale.getLanguage(), name);
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 */
//	private long tariffID = -1;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)
	private String tariffID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="tariff"
	 */
	@Persistent(
		dependent="true",
		mappedBy="tariff",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TariffName name;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int tariffIndex;

	public Tariff() { }

	public Tariff(String organisationID, String tariffID)
	{
		this.organisationID = organisationID;
		this.tariffID = tariffID;
		this.primaryKey = getPrimaryKey(organisationID, tariffID);
		this.name = new TariffName(this);
		this.tariffIndex = Integer.MAX_VALUE;
	}

	public static String getPrimaryKey(String organisationID, String tariffID)
	{
//		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(tariffID);
		return organisationID + '/' + tariffID;
	}

	public static String createTariffID()
	{
		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Tariff.class));
	}

	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public TariffID getObjectId()
	{
		return (TariffID)JDOHelper.getObjectId(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the tariffID.
	 */
	public String getTariffID()
	{
		return tariffID;
	}
//	/**
//	 * @param tariffID The tariffID to set.
//	 */
//	public void setTariffID(long tariffID)
//	{
//		this.tariffID = tariffID;
//		this.primaryKey = getPrimaryKey(getOrganisationID(), tariffID);
//		this.name.setTariffID(tariffID);
//	}
//	public static String getPrimaryKey(String organisationID, String tariffID)
//	{
//		return organisationID + '/' + tariffID;
//	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}

//	/**
//	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
//	 */
//	public void jdoPreStore()
//	{
//		if (tariffID < 0) {
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//			this.setTariffID(
//					TariffRegistry.getTariffRegistry(pm).createTariffID());
//		}
//	}

	/**
	 * @return Returns the name.
	 */
	public TariffName getName()
	{
		return name;
	}

//	/**
//	 * @see org.nightlabs.i18n.Localizable#localize(java.lang.String)
//	 */
//	public void localize(String languageID)
//	{
//		name.localize(languageID);
//	}
//
//	/**
//	 * @see org.nightlabs.jdo.LocalizedDetachable#detachCopyLocalized(java.lang.String, javax.jdo.PersistenceManager)
//	 */
//	public LocalizedDetachable detachCopyLocalized(String languageID, PersistenceManager pm)
//	{
//		Tariff tariff = (Tariff) pm.detachCopy(this);
//		tariff.name.localize(languageID, this.name);
//		return tariff;
//	}

	public int getTariffIndex() {
		return tariffIndex;
	}

	public void setTariffIndex(int tariffIndex) {
		this.tariffIndex = tariffIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Tariff)) return false;
		Tariff o = (Tariff) obj;
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.tariffID, this.tariffID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(tariffID);
	}

	@Override
	public String toString() {
		try {
			return getName().getText();
		} catch (JDODetachedFieldAccessException e) {
			return super.toString();
		}
	}
}
