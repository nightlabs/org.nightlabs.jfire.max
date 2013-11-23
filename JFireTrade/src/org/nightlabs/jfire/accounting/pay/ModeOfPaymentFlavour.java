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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.accounting.pay.config.ModeOfPaymentConfigModule;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * A <tt>ModeOfPaymentFlavour</tt> is a subkind of <tt>ModeOfPayment</tt>. An example
 * might be "Credit Card" as <tt>ModeOfPayment</tt> and "VISA" or "Master" as
 * <tt>ModeOfPaymentFlavour</tt>. All flavours of one mode of payment require the same
 * data for processing.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfPaymentFlavour"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, modeOfPaymentFlavourID"
 *
 * @jdo.fetch-group name="ModeOfPaymentFlavour.name" fields="name"
 * @jdo.fetch-group name="ModeOfPaymentFlavour.modeOfPayment" fields="modeOfPayment"
 * @jdo.fetch-group name="ModeOfPaymentFlavour.icon16x16Data" fields="icon16x16Data"
 * @jdo.fetch-group name="ModeOfPaymentFlavour.this" fetch-groups="default" fields="modeOfPayment, name, icon16x16Data"
 *
 * @!jdo.query // this fails with: Cannot perform operation "in" on UnboundVariable and MapValueLiteral "(?,?)"
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup"
 *		query="SELECT
 *			WHERE
 *				paramCustomerGroup.modeOfPaymentFlavours.containsValue(this) ||
 *				(
 *					paramCustomerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *			 		modeOfPayment.flavours.containsValue(this)
 *				)
 *			VARIABLES ModeOfPayment modeOfPayment
 *			PARAMETERS CustomerGroup paramCustomerGroup
 *			import org.nightlabs.jfire.trade.CustomerGroup; import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @!jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup"
 *		query="SELECT
 *			WHERE
 *				(
 *					customerGroup.organisationID == paramOrganisationID &&
 *					customerGroup.customerGroupID == paramCustomerGroupID
 *				)
 *				&&
 *				(
 *					customerGroup.modeOfPaymentFlavours.containsValue(this) ||
 *					(
 *						customerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *				 		modeOfPayment.flavours.containsValue(this)
 *					)
 *				)
 *			VARIABLES CustomerGroup customerGroup; ModeOfPayment modeOfPayment
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.trade.CustomerGroup;
 *			import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND1"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfPaymentFlavours.containsValue(this)
 *			VARIABLES CustomerGroup customerGroup
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.trade.CustomerGroup"
 *
 * @jdo.query
 *		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND2"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfPayments.containsValue(modeOfPayment) &&
 *				modeOfPayment.flavours.containsValue(this)
 *			VARIABLES CustomerGroup customerGroup; ModeOfPayment modeOfPayment
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.trade.CustomerGroup;
 *			import org.nightlabs.jfire.accounting.pay.ModeOfPayment"
 *
 * @jdo.query name="getAllModeOfPaymentFlavourIDs" query="SELECT JDOHelper.getObjectId(this)"
 */
@PersistenceCapable(
	objectIdClass=ModeOfPaymentFlavourID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ModeOfPaymentFlavour")
@FetchGroups({
	@FetchGroup(
		name=ModeOfPaymentFlavour.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=ModeOfPaymentFlavour.FETCH_GROUP_MODE_OF_PAYMENT,
		members=@Persistent(name="modeOfPayment")),
	@FetchGroup(
		name=ModeOfPaymentFlavour.FETCH_GROUP_ICON_16X16_DATA,
		members=@Persistent(name="icon16x16Data")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ModeOfPaymentFlavour.FETCH_GROUP_THIS_MODE_OF_PAYMENT_FLAVOUR,
		members={@Persistent(name="modeOfPayment"), @Persistent(name="name"), @Persistent(name="icon16x16Data")})
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND1",
		value="SELECT WHERE customerGroup.organisationID == paramOrganisationID && customerGroup.customerGroupID == paramCustomerGroupID && customerGroup.modeOfPaymentFlavours.containsValue(this) VARIABLES CustomerGroup customerGroup PARAMETERS String paramOrganisationID, String paramCustomerGroupID import java.lang.String; import org.nightlabs.jfire.trade.CustomerGroup"),
	@javax.jdo.annotations.Query(
		name="getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND2",
		value="SELECT WHERE customerGroup.organisationID == paramOrganisationID && customerGroup.customerGroupID == paramCustomerGroupID && customerGroup.modeOfPayments.containsValue(modeOfPayment) && modeOfPayment.flavours.containsValue(this) VARIABLES CustomerGroup customerGroup; ModeOfPayment modeOfPayment PARAMETERS String paramOrganisationID, String paramCustomerGroupID import java.lang.String; import org.nightlabs.jfire.trade.CustomerGroup; import org.nightlabs.jfire.accounting.pay.ModeOfPayment"),
	@javax.jdo.annotations.Query(
		name="getAllModeOfPaymentFlavourIDs",
		value="SELECT JDOHelper.getObjectId(this)")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ModeOfPaymentFlavour
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ModeOfPaymentFlavour.name";
	public static final String FETCH_GROUP_MODE_OF_PAYMENT = "ModeOfPaymentFlavour.modeOfPayment";
	public static final String FETCH_GROUP_ICON_16X16_DATA = "ModeOfPaymentFlavour.icon16x16Data";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_MODE_OF_PAYMENT_FLAVOUR = "ModeOfPaymentFlavour.this";

	public static final byte MERGE_MODE_UNION = 1;
	public static final byte MERGE_MODE_INTERSECTION = 2;

	/**
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}. Can be <tt>null</tt> in order to return all <tt>ModeOfPaymentFlavour</tt>s.
	 * @param mergeMode Whether the intersection or the combination of all <tt>CustomerGroup</tt> configurations shall be used.
	 * @param filterByConfig
	 * 		If this is <code>true</code> the flavours available found for the given customer-groups will also be filtered by the
	 * 		intersection of the entries configured in the {@link ModeOfPaymentConfigModule} for the current user and the
	 * 		workstation he is currently loggen on.
	 *
	 * @return Returns those <tt>ModeOfPaymentFlavour</tt>s that are available for all given
	 *		<tt>CustomerGroup</tt>s. If <tt>mergeMode</tt> is {@link #MERGE_MODE_UNION},
	 *		they are combined like SQL UNION would do (means, if at least one
	 *		<tt>CustomerGroup</tt>
	 *		contains a certain <tt>ModeOfPaymentFlavour</tt>, it will be in the result).
	 *		If <tt>mergeMode</tt> is {@link #MERGE_MODE_INTERSECTION}, only those
	 *		<tt>ModeOfPaymentFlavour</tt>s are returned that are available to all
	 *		<tt>CustomerGroup</tt>s.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			PersistenceManager pm, Collection<CustomerGroupID> customerGroupIDs, byte mergeMode, boolean filterByConfig)
	{
		if (customerGroupIDs == null)
			return (Collection<ModeOfPaymentFlavour>) pm.newQuery(ModeOfPaymentFlavour.class).execute();


		if (mergeMode != MERGE_MODE_UNION && mergeMode != MERGE_MODE_INTERSECTION)
			throw new IllegalArgumentException("mergeMode invalid! Must be MERGE_MODE_UNION or MERGE_MODE_INTERSECTION!");

		Map<String, ModeOfPaymentFlavour> res = null;
		for (Iterator<CustomerGroupID> itCustomerGroups = customerGroupIDs.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroupID customerGroupID = itCustomerGroups.next();

			Map<String, ModeOfPaymentFlavour> m = getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(
					pm, customerGroupID.organisationID, customerGroupID.customerGroupID, filterByConfig);

			if (res == null) {
				res = m;
			}
			else {
				if (mergeMode == MERGE_MODE_INTERSECTION) {
					// remove all missing
					for (Iterator<String> it = res.keySet().iterator(); it.hasNext(); ) {
						String mopfPK = it.next();
						if (!m.containsKey(mopfPK))
							it.remove();
					}
				} // if (mergeMode == MERGE_MODE_INTERSECTION) {
				else { // if (mergeMode == MERGE_MODE_UNION) {
					// add all additional
					for (Iterator<Map.Entry<String, ModeOfPaymentFlavour>> it = res.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<String, ModeOfPaymentFlavour> me = it.next();
						String mopfPK = me.getKey();
						ModeOfPaymentFlavour mopf = me.getValue();
						res.put(mopfPK, mopf);
					}
				} // if (mergeMode == MERGE_MODE_UNION) {
			}
		}

		return res.values();
	}

	/**
	 * @see #getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager, String, String)
	 */
	public static Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, CustomerGroupID customerGroupID, boolean filterByConfig)
	{
//		pm.getExtent(CustomerGroup.class);
//		CustomerGroup customerGroup = (CustomerGroup) pm.getObjectById(customerGroupID);
		return getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
				pm, customerGroupID.organisationID, customerGroupID.customerGroupID, filterByConfig);
	}

//	/**
//	 * This method finds all <tt>ModeOfPaymentFlavour</tt>s which are assigned to
//	 * a {@link CustomerGroup} - either directly or indirectly via its owner
//	 * {@link ModeOfPayment}.
//	 *
//	 * @param pm The <tt>PersistenceManager</tt> with which to access the datastore.
//	 * @param customerGroup The <tt>CustomerGroup</tt> for which to find the <tt>ModeOfPaymentFlavour</tt>s.
//	 *
//	 * @return A <tt>Collection</tt> with instances of type <tt>ModeOfPaymentFlavour</tt>.
//	 */
//	public static Collection getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, CustomerGroup customerGroup)
//	{
//		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup");
//		return (Collection)query.execute(customerGroup);
//	}

	/**
	 * This method finds all <tt>ModeOfPaymentFlavour</tt>s which are assigned to
	 * a {@link CustomerGroup} - either directly or indirectly via its owner
	 * {@link ModeOfPayment}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> with which to access the datastore.
	 * @param organisationID The first part of the primary key of the <tt>CustomerGroup</tt>.
	 * @param customerGroupID The second part of the primary key of the <tt>CustomerGroup</tt>.
	 * @param filterByConfig
	 * 		If this is <code>true</code> the flavours available found for the given customer-group will also be filtered by the
	 * 		intersection of the entries configured in the {@link ModeOfPaymentConfigModule} for the current user and the
	 * 		workstation he is currently loggen on.
	 *
	 * @return A <tt>Collection</tt> with instances of type <tt>ModeOfPaymentFlavour</tt>.
	 *
	 * @see #getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager, CustomerGroupID)
	 */
	public static Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForOneCustomerGroup(PersistenceManager pm, String organisationID, String customerGroupID, boolean filterByConfig)
	{
		return getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(
				pm, organisationID, customerGroupID, filterByConfig).values();
	}

	@SuppressWarnings("unchecked")
	protected static Map<String, ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursMapForOneCustomerGroup(PersistenceManager pm, String organisationID, String customerGroupID, boolean filterByConfig)
	{
		// return getAvailableModeOfPaymentFlavoursForOneCustomerGroup(pm, CustomerGroupID.create(organisationID, customerGroupID));

//		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup");
//		return (Collection)query.execute(organisationID, customerGroupID);

		// WORKAROUND The normal query returns an empty result, probably because of issues with ORs.
		Map<String, ModeOfPaymentFlavour> m = new HashMap<String, ModeOfPaymentFlavour>();
		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND1");
		for (Iterator<ModeOfPaymentFlavour> it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfPaymentFlavour modeOfPaymentFlavour = it.next();
			m.put(modeOfPaymentFlavour.getPrimaryKey(), modeOfPaymentFlavour);
		}

		query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAvailableModeOfPaymentFlavoursForOneCustomerGroup_WORKAROUND2");
		for (Iterator<ModeOfPaymentFlavour> it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfPaymentFlavour modeOfPaymentFlavour = it.next();
			m.put(modeOfPaymentFlavour.getPrimaryKey(), modeOfPaymentFlavour);
		}

		if (filterByConfig) {
			ModeOfPaymentConfigModule cfMod;
			cfMod = UserConfigSetup.getUserConfigModule(pm, ModeOfPaymentConfigModule.class);
			for (Iterator<Map.Entry<String, ModeOfPaymentFlavour>> it = m.entrySet().iterator(); it.hasNext(); ) {
				if (!cfMod.getModeOfPaymentFlavours().contains(it.next().getValue())) {
					it.remove();
				}
			}
			cfMod = WorkstationConfigSetup.getWorkstationConfigModule(pm, ModeOfPaymentConfigModule.class);
			for (Iterator<Map.Entry<String, ModeOfPaymentFlavour>> it = m.entrySet().iterator(); it.hasNext(); ) {
				if (!cfMod.getModeOfPaymentFlavours().contains(it.next().getValue())) {
					it.remove();
				}
			}
		}

		return m;
	}

	public static Set<ModeOfPaymentFlavourID> getAllModeOfPaymentFlavourIDs(PersistenceManager pm) {
		Query query = pm.newNamedQuery(ModeOfPaymentFlavour.class, "getAllModeOfPaymentFlavourIDs");
		Collection<ModeOfPaymentFlavourID> c = CollectionUtil.castCollection((Collection<?>) query.execute());
		return new HashSet<ModeOfPaymentFlavourID>(c);
	}

	public static Collection<ModeOfPaymentFlavour> getAllModeOfPaymentFlavours(PersistenceManager pm) {
		Query query = pm.newQuery(ModeOfPaymentFlavour.class);
		return CollectionUtil.castCollection((Collection<?>) query.execute());
	}

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
	private String modeOfPaymentFlavourID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ModeOfPayment modeOfPayment;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Date icon16x16Timestamp;
//
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
@Column(sqlType="BLOB")
	private byte[] icon16x16Data;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfPaymentFlavour"
	 */
	@Persistent(
		dependent="true",
		mappedBy="modeOfPaymentFlavour",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ModeOfPaymentFlavourName name;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ModeOfPaymentFlavour()
	{
	}

	public ModeOfPaymentFlavour(String organisationID, String modeOfPaymentFlavourID, ModeOfPayment modeOfPayment)
	{
		this.organisationID = organisationID;
		this.modeOfPaymentFlavourID = modeOfPaymentFlavourID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfPaymentFlavourID);
		this.modeOfPayment = modeOfPayment;
		this.name = new ModeOfPaymentFlavourName(this);
	}

	/**
	 * @return Returns the modeOfPayment.
	 */
	public ModeOfPayment getModeOfPayment()
	{
		return modeOfPayment;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfPaymentFlavourID.
	 */
	public String getModeOfPaymentFlavourID()
	{
		return modeOfPaymentFlavourID;
	}

	public static String getPrimaryKey(String organisationID, String modeOfPaymentFlavourID)
	{
		return organisationID + '/' + modeOfPaymentFlavourID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfPaymentFlavourName getName()
	{
		return name;
	}
//	public Date getIcon16x16Timestamp()
//	{
//		return icon16x16Timestamp;
//	}
//	public void setIcon16x16Timestamp(Date icon16x16Timestamp)
//	{
//		this.icon16x16Timestamp = icon16x16Timestamp;
//	}
	public byte[] getIcon16x16Data()
	{
		return icon16x16Data;
	}
	public void setIcon16x16Data(byte[] icon16x16Data)
	{
		this.icon16x16Data = icon16x16Data;
	}

	/**
	 * Calls {@link #loadIconFromResource(Class, String) } with <code>resourceLoaderClass == this.getClass()</code>
	 * {@link ModeOfPaymentFlavour} and <code>fileName == "ModeOfPaymentFlavour-" + modeOfPaymentFlavourID + ".16x16.png"</code>.
	 * This method is used for the default {@link ModeOfPaymentFlavour}s populated by JFireTrade.
	 * If you want to load icons for a ModeOfPaymentFlavour outside of JFireTrade, you should
	 * use {@link #loadIconFromResource(Class, String)} instead.
	 *
	 * @throws IOException
	 */
	public void loadIconFromResource() throws IOException
	{
		String resourcePath = "resource/" + ModeOfPaymentFlavour.class.getSimpleName() + '-' + modeOfPaymentFlavourID + ".16x16.png";
		loadIconFromResource(
				ModeOfPaymentFlavour.class, resourcePath);
	}

	/**
	 * This method loads an icon from a resource file by calling the method
	 * {@link Class#getResourceAsStream(String)} of
	 * <code>resourceLoaderClass</code>.
	 *
	 * @param resourceLoaderClass The class that is used for loading the file.
	 * @param fileName A filename relative to <code>resourceLoaderClass</code>. Note, that subdirectories are possible, but ".." not.
	 * @throws IOException If loading the resource failed. This might be a {@link FileNotFoundException}.
	 */
	public void loadIconFromResource(Class<?> resourceLoaderClass, String fileName) throws IOException
	{
		InputStream in = resourceLoaderClass.getResourceAsStream(fileName);
		if (in == null)
			throw new FileNotFoundException("Could not find resource: " + fileName);
		try {
			DataBuffer db = new DataBuffer(512);
//			db.maxSizeForRAM = Integer.MAX_VALUE;
			OutputStream out = db.createOutputStream();
			IOUtil.transferStreamData(in, out);
			out.close();

			this.icon16x16Data = db.createByteArray();
		} finally {
			in.close();
		}
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(modeOfPaymentFlavourID);
	}
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ModeOfPaymentFlavour)) return false;
		ModeOfPaymentFlavour o = (ModeOfPaymentFlavour) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.modeOfPaymentFlavourID, o.modeOfPaymentFlavourID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + modeOfPaymentFlavourID + ']' + "(version " + JDOHelper.getVersion(this) + ')';
	}
}
