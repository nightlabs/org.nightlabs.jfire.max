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

package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfDelivery"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, modeOfDeliveryID"
 *
 * @jdo.fetch-group name="ModeOfDelivery.name" fields="name"
 * @jdo.fetch-group name="ModeOfDelivery.flavours" fields="flavours"
 * @jdo.fetch-group name="ModeOfDelivery.this" fetch-groups="default" fields="flavours, name"
 * 
 * @jdo.query
 *		name="getAllModeOfDeliveryFlavourIDs"
 *		query="SELECT JDOHelper.getObjectId(this)"
 * 
 */
@PersistenceCapable(
	objectIdClass=ModeOfDeliveryID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ModeOfDelivery")
@FetchGroups({
	@FetchGroup(
		name=ModeOfDelivery.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=ModeOfDelivery.FETCH_GROUP_FLAVOURS,
		members=@Persistent(name="flavours")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ModeOfDelivery.FETCH_GROUP_THIS_MODE_OF_DELIVERY,
		members={@Persistent(name="flavours"), @Persistent(name="name")})
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getAllModeOfDeliveryFlavourIDs",
		value="SELECT JDOHelper.getObjectId(this)")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ModeOfDelivery
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ModeOfDelivery.name";
	public static final String FETCH_GROUP_FLAVOURS = "ModeOfDelivery.flavours";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_MODE_OF_DELIVERY = "ModeOfDelivery.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String modeOfDeliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfDelivery"
	 */
	@Persistent(
		dependent="true",
		mappedBy="modeOfDelivery",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ModeOfDeliveryName name;

	/**
	 * key: String modeOfDeliveryFlavourPK (see {@link ModeOfDeliveryFlavour#getPrimaryKey()})<br/>
	 * value: ModeOfDeliveryFlavour modeOfDeliveryFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDeliveryFlavour"
	 *		dependent-value="true"
	 *		mapped-by="modeOfDelivery"
	 *
	 * @jdo.key mapped-by="primaryKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
	 */
	@Persistent(
		mappedBy="modeOfDelivery",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="primaryKey")
	@Value(dependent="true")
	private Map<String, ModeOfDeliveryFlavour> flavours = new HashMap<String, ModeOfDeliveryFlavour>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ModeOfDelivery() { }

	public ModeOfDelivery(ModeOfDeliveryID modeOfDeliveryID)
	{
		this(modeOfDeliveryID.organisationID, modeOfDeliveryID.modeOfDeliveryID);
	}
	public ModeOfDelivery(String organisationID, String modeOfDeliveryID)
	{
		this.organisationID = organisationID;
		this.modeOfDeliveryID = modeOfDeliveryID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfDeliveryID);
		this.name = new ModeOfDeliveryName(this);
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfDeliveryID.
	 */
	public String getModeOfDeliveryID()
	{
		return modeOfDeliveryID;
	}
	public static String getPrimaryKey(String organisationID, String modeOfDeliveryID)
	{
		return organisationID + '/' + modeOfDeliveryID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfDeliveryName getName()
	{
		return name;
	}

	/**
	 * @return Returns the flavours.
	 */
	public Collection<ModeOfDeliveryFlavour> getFlavours()
	{
		return flavours.values();
	}

	public ModeOfDeliveryFlavour getFlavour(String organisationID, String modeOfDeliveryFlavourID, boolean throwExceptionIfNotExistent)
	{
		return getFlavour(ModeOfDeliveryFlavour.getPrimaryKey(organisationID, modeOfDeliveryFlavourID), throwExceptionIfNotExistent);
	}
	public ModeOfDeliveryFlavour getFlavour(String modeOfDeliveryFlavourPK, boolean throwExceptionIfNotExistent)
	{
		ModeOfDeliveryFlavour res = (ModeOfDeliveryFlavour) flavours.get(modeOfDeliveryFlavourPK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("No ModeOfDeliveryFlavour with modeOfDeliveryFlavourPK=\"" + modeOfDeliveryFlavourPK + "\" in the ModeOfDelivery \"" + getPrimaryKey() + "\" existing!");
		return res;
	}

	public ModeOfDeliveryFlavour createFlavour(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
	{
		return createFlavour(modeOfDeliveryFlavourID.organisationID, modeOfDeliveryFlavourID.modeOfDeliveryFlavourID);
	}

	/**
	 * Creates a new <tt>ModeOfDeliveryFlavour</tt> or returns a previously created one.
	 * 
	 * @param flavourID The local id (within this <tt>ModeOfDelivery</tt>) for the new flavour.
	 *
	 * @return The newly created <tt>ModeOfDeliveryFlavour</tt> (or an old instance, if already existent before).
	 */
	public ModeOfDeliveryFlavour createFlavour(String organisationID, String modeOfDeliveryFlavourID)
	{
		String modeOfDeliveryFlavourPK = ModeOfDeliveryFlavour.getPrimaryKey(organisationID, modeOfDeliveryFlavourID);
		ModeOfDeliveryFlavour res = (ModeOfDeliveryFlavour) flavours.get(modeOfDeliveryFlavourPK);
		if (res == null) {
			res = new ModeOfDeliveryFlavour(organisationID, modeOfDeliveryFlavourID, this);
			flavours.put(modeOfDeliveryFlavourPK, res);
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<ModeOfDeliveryID> getAllModeOfDeliveryIDs(PersistenceManager pm) {
		Query query = pm.newNamedQuery(ModeOfDelivery.class, "getAllModeOfDeliveryIDs");
		return new HashSet<ModeOfDeliveryID>((Collection<? extends ModeOfDeliveryID>) query.execute());
	}
	

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(modeOfDeliveryID);
	}
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ModeOfDelivery)) return false;
		ModeOfDelivery o = (ModeOfDelivery) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.modeOfDeliveryID, o.modeOfDeliveryID);
	}
}
