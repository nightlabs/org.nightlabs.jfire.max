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

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.util.Util;

/**
 * <p>
 * A tariff describes a situation in which a certain price is applicable to a certain product.
 * What exactly a tariff means is highly use-case-specific. For example, when selling an admission
 * ticket, tariffs might be "Normal price", "Student" and "Senior citizen". In another context,
 * you might use tariffs to differentiate between VIPs and normal customers.
 * </p>
 * <p>
 * Being a means to assign multiple prices to the same {@link ProductType}, a {@link Tariff}
 * is used for example in the {@link GridPriceConfig} representing one of the grid's dimensions.
 * </p>
 * <p>
 * You can either instantiate {@link Tariff} directly or declare a specific sub-class containing
 * additional use-case-specific data. Note, that some methods will not list instances
 * of sub-classes as they don't know how to handle them.
 * </p>
 */
 /* <p>
 * Tariffs can either be visible and usable globally or in a use-case-specific {@link #getScope() scope}.
 * </p>
 */
@PersistenceCapable(
		objectIdClass=TariffID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_Tariff"
)
@FetchGroups({
	@FetchGroup(
			name=Tariff.FETCH_GROUP_NAME,
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsTrade.articleInOrderEditor",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsTrade.articleInOfferEditor",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsTrade.articleInInvoiceEditor",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsTrade.articleInDeliveryNoteEditor",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsPriceConfig.edit",
			members=@Persistent(name="name")
	),
	@FetchGroup(
			name="FetchGroupsEntityUserSet.replicateToReseller",
			members=@Persistent(name="name")
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class Tariff
implements Serializable, AttachCallback, DetachCallback
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 2L;

	public static final String FETCH_GROUP_NAME = "Tariff.name"; //$NON-NLS-1$

//	/**
//	 * Specifies the global {@link #getScope() scope}.
//	 */
//	public static final String SCOPE_GLOBAL = "global";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String tariffID;

	@Persistent(mappedBy="tariff", dependent="true")
	private TariffName name;

	private int tariffIndex;

//	@Persistent(nullValue=NullValue.EXCEPTION)
//	@Column(defaultValue=SCOPE_GLOBAL)
//	private String scope;

	/**
	 * @deprecated Only for JDO! Don't use this constructor directly!
	 */
	@Deprecated
	protected Tariff() { }

	public Tariff(final TariffID tariffID)
	{
		if (tariffID == null) {
			this.organisationID = IDGenerator.getOrganisationID();
			this.tariffID = createTariffID();
		}
		else {
			this.organisationID = tariffID.organisationID;
			this.tariffID = tariffID.tariffID;
		}

		this.name = new TariffName(this);
		this.tariffIndex = Integer.MAX_VALUE;
//		this.scope = SCOPE_GLOBAL;
	}

	/**
	 * Get the primary key fields concatenated in one string separated by '/'.
	 * @param organisationID the first part of the primary key.
	 * @param tariffID the 2nd part of the primary key.
	 * @return the concatenated primary key.
	 */
	public static String getPrimaryKey(final String organisationID, final String tariffID)
	{
		return organisationID + '/' + tariffID;
	}

	public static String createTariffID()
	{
		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Tariff.class));
	}

	/**
	 * Get the JDO object id.
	 * <p>
	 * Note, do not remove this method, references might not be found, but are made through jsp.
	 * </p>
	 * @return the JDO object id.
	 */
	public TariffID getObjectId()
	{
		return (TariffID)JDOHelper.getObjectId(this);
	}	
	
	/**
	 * Get the ID of the organisation which owns (created) this <code>Tariff</code> instance.
	 * @return the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * Get the local identifier within the namespace of the {@link #getOrganisationID() organisation}.
	 * @return Returns the tariffID.
	 */
	public String getTariffID()
	{
		return tariffID;
	}

	/**
	 * Get the primary key fields concatenated in one string separated by '/'.
	 * @return the concatenated primary key.
	 */
	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, tariffID);
	}

	/**
	 * Get the multilingual name of this tariff.
	 * @return the name.
	 */
	public TariffName getName()
	{
		return name;
	}

	/**
	 * <p>
	 * Get the global order hint of this tariff. This should be taken into account when
	 * sorting tariffs in a user-independent way or when determining the initial ordering
	 * for a user. The {@link TariffOrderConfigModule} allows to sort user-specifically
	 * and should be used instead, when there is a user context.
	 * </p>
	 * <p>
	 * Note, that this is only a hint and there might be multiple tariffs with the same
	 * <code>tariffIndex</code>.
	 * </p>
	 * @return the global tariff index (for ordering).
	 */
	public int getTariffIndex() {
		return tariffIndex;
	}

	/**
	 * Set the global order hint of this tariff. See {@link #getTariffIndex()} for further infos.
	 * @param tariffIndex the global tariff index (for ordering).
	 */
	public void setTariffIndex(final int tariffIndex) {
		this.tariffIndex = tariffIndex;
	}

//	/**
//	 * Get the scope of this tariff.
//	 * @return the scope of this tariff.
//	 */
//	public String getScope() {
//		return scope;
//	}
//	public void setScope(final String scope) {
//		this.scope = scope;
//	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		final Tariff o = (Tariff) obj;
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.tariffID, this.tariffID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(tariffID);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + organisationID + ',' + tariffID + ']';
	}

	@Override
	public void jdoPostAttach(final Object o) {
	}

	@Override
	public void jdoPreAttach() {
	}

	@Override
	public void jdoPostDetach(final Object o) {
	}

	@Override
	public void jdoPreDetach() {
	}
}
