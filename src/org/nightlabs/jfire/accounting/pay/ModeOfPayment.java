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

package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfPayment"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, modeOfPaymentID"
 *
 * @jdo.fetch-group name="ModeOfPayment.name" fields="name"
 * @jdo.fetch-group name="ModeOfPayment.flavours" fields="flavours"
 * @jdo.fetch-group name="ModeOfPayment.this" fetch-groups="default" fields="flavours, name"
 *
 * @jdo.query name="getAllModeOfPaymentIDs" query="SELECT JDOHelper.getObjectId(this)"
 */
public class ModeOfPayment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ModeOfPayment.name";
	public static final String FETCH_GROUP_FLAVOURS = "ModeOfPayment.flavours";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_MODE_OF_PAYMENT = "ModeOfPayment.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String modeOfPaymentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfPayment"
	 */
	private ModeOfPaymentName name;

	/**
	 * key: String modeOfPaymentFlavourPK (see {@link ModeOfPaymentFlavour#getPrimaryKey()})<br/>
	 * value: ModeOfPaymentFlavour modeOfPaymentFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfPaymentFlavour"
	 *		dependent-value="true"
	 *		mapped-by="modeOfPayment"
	 *
	 * @jdo.key mapped-by="primaryKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
	 */
	private Map<String, ModeOfPaymentFlavour> flavours = new HashMap<String, ModeOfPaymentFlavour>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ModeOfPayment() { }

	public ModeOfPayment(ModeOfPaymentID modeOfPaymentID)
	{
		this(modeOfPaymentID.organisationID, modeOfPaymentID.modeOfPaymentID);
	}

	public ModeOfPayment(String organisationID, String modeOfPaymentID)
	{
		this.organisationID = organisationID;
		this.modeOfPaymentID = modeOfPaymentID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfPaymentID);
		this.name = new ModeOfPaymentName(this);
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfPaymentID.
	 */
	public String getModeOfPaymentID()
	{
		return modeOfPaymentID;
	}
	public static String getPrimaryKey(String organisationID, String modeOfPaymentID)
	{
		return organisationID + '/' + modeOfPaymentID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfPaymentName getName()
	{
		return name;
	}

	/**
	 * @return Returns the flavours.
	 */
	public Collection<ModeOfPaymentFlavour> getFlavours()
	{
		return flavours.values();
	}

	public ModeOfPaymentFlavour getFlavour(String organisationID, String modeOfPaymentFlavourID, boolean throwExceptionIfNotExistent)
	{
		return getFlavour(ModeOfPaymentFlavour.getPrimaryKey(organisationID, modeOfPaymentFlavourID), throwExceptionIfNotExistent);
	}
	public ModeOfPaymentFlavour getFlavour(String modeOfPaymentFlavourPK, boolean throwExceptionIfNotExistent)
	{
		ModeOfPaymentFlavour res = flavours.get(modeOfPaymentFlavourPK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("No ModeOfPaymentFlavour with modeOfPaymentFlavourPK=\"" + modeOfPaymentFlavourPK + "\" in the ModeOfPayment \"" + getPrimaryKey() + "\" existing!");
		return res;
	}

	public ModeOfPaymentFlavour createFlavour(ModeOfPaymentFlavourID modeOfPaymentFlavourID)
	{
		return createFlavour(modeOfPaymentFlavourID.organisationID, modeOfPaymentFlavourID.modeOfPaymentFlavourID);
	}

	/**
	 * Creates a new <tt>ModeOfPaymentFlavour</tt> or returns a previously created one.
	 *
	 * @param flavourID The local id (within this <tt>ModeOfPayment</tt>) for the new flavour.
	 *
	 * @return The newly created <tt>ModeOfPaymentFlavour</tt> (or an old instance, if already existent before).
	 */
	public ModeOfPaymentFlavour createFlavour(String organisationID, String modeOfPaymentFlavourID)
	{
		String modeOfPaymentFlavourPK = ModeOfPaymentFlavour.getPrimaryKey(organisationID, modeOfPaymentFlavourID);
		ModeOfPaymentFlavour res = flavours.get(modeOfPaymentFlavourPK);
		if (res == null) {
			res = new ModeOfPaymentFlavour(organisationID, modeOfPaymentFlavourID, this);
			flavours.put(modeOfPaymentFlavourPK, res);
		}
		return res;
	}

	public static Set<ModeOfPaymentID> getAllModeOfPaymentIDs(PersistenceManager pm) {
		Query query = pm.newNamedQuery(ModeOfPayment.class, "getAllModeOfPaymentIDs");
		Collection<ModeOfPaymentID> c = CollectionUtil.castCollection((Collection<?>) query.execute());
		return new HashSet<ModeOfPaymentID>(c);
	}

	public static Collection<ModeOfPayment> getAllModeOfPayments(PersistenceManager pm) {
		Query query = pm.newQuery(ModeOfPayment.class);
		return CollectionUtil.castCollection((Collection<?>) query.execute());
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(modeOfPaymentID);
	}
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ModeOfPayment)) return false;
		ModeOfPayment o = (ModeOfPayment) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.modeOfPaymentID, o.modeOfPaymentID);
	}
}
