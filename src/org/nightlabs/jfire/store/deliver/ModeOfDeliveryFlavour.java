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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

/**
 * A <tt>ModeOfDeliveryFlavour</tt> is a subkind of <tt>ModeOfDelivery</tt>. An example
 * might be "Physical Mail" as <tt>ModeOfDelivery</tt> and "UPS" or "DHL" as
 * <tt>ModeOfDeliveryFlavour</tt>. All flavours of one mode of delivery require the same
 * data for processing.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfDeliveryFlavour"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, modeOfDeliveryFlavourID"
 *
 * @jdo.fetch-group name="ModeOfDeliveryFlavour.name" fields="name"
 * @jdo.fetch-group name="ModeOfDeliveryFlavour.modeOfDelivery" fields="modeOfDelivery"
 * @jdo.fetch-group name="ModeOfDeliveryFlavour.this" fetch-groups="default" fields="modeOfDelivery, name"
 *
 * @!jdo,query
 *		name="getAvailableModeOfDeliveryFlavoursForOneProductType_WORKAROUND1"
 *		query="SELECT
 *			WHERE
 *				varProductType.organisationID == paramOrganisationID
 *				&&
 *				varProductType.productTypeID == paramProductTypeID
 *				&&
 *				varProductType.deliveryConfiguration.modeOfDeliveryFlavours.containsValue(this)
 *			VARIABLES ProductType varProductType
 *			PARAMETERS String paramOrganisationID, String paramProductTypeID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.store.ProductType"
 *
 * @!jdo,query
 *		name="getAvailableModeOfDeliveryFlavoursForOneProductType_WORKAROUND2"
 *		query="SELECT
 *			WHERE
 *				varProductType.organisationID == paramOrganisationID
 *				&&
 *				varProductType.productTypeID == paramProductTypeID
 *				&&
 *				varProductType.deliveryConfiguration.modeOfDeliveries.containsValue(varModeOfDelivery)
 *				&&
 *				varModeOfDelivery.containsValue(this)
 *			VARIABLES ProductType varProductType, ModeOfDelivery varModeOfDelivery
 *			PARAMETERS String paramOrganisationID, String paramProductTypeID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.store.ProductType;
 *			import org.nightlabs.jfire.store.deliver.ModeOfDelivery"
 *
 * @jdo.query
 *		name="getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND1"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfDeliveryFlavours.containsValue(this)
 *			VARIABLES CustomerGroup customerGroup
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.trade.CustomerGroup"
 *
 * @jdo.query
 *		name="getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND2"
 *		query="SELECT
 *			WHERE
 *				customerGroup.organisationID == paramOrganisationID &&
 *				customerGroup.customerGroupID == paramCustomerGroupID
 *				&&
 *				customerGroup.modeOfDeliveries.containsValue(modeOfDelivery) &&
 *				modeOfDelivery.flavours.containsValue(this)
 *			VARIABLES CustomerGroup customerGroup; ModeOfDelivery modeOfDelivery
 *			PARAMETERS String paramOrganisationID, String paramCustomerGroupID
 *			import java.lang.String;
 *			import org.nightlabs.jfire.trade.CustomerGroup;
 *			import org.nightlabs.jfire.store.deliver.ModeOfDelivery"
 */
public class ModeOfDeliveryFlavour
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "ModeOfDeliveryFlavour.name";
	public static final String FETCH_GROUP_MODE_OF_DELIVERY = "ModeOfDeliveryFlavour.modeOfDelivery";
	public static final String FETCH_GROUP_THIS_MODE_OF_DELIVERY_FLAVOUR = "ModeOfDeliveryFlavour.this";

	public static class ModeOfDeliveryFlavourProductTypeGroupCarrier
	implements Serializable
	{
		/**
		 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}
		 */
		public ModeOfDeliveryFlavourProductTypeGroupCarrier(Collection customerGroupIDs)
		{
			this.customerGroupIDs = customerGroupIDs;
		}

		private Collection customerGroupIDs;

		/**
		 * key: {@link org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID} modeOfDeliveryFlavourID<br/>
		 * value: {@link ModeOfDeliveryFlavour} modeOfDeliveryFlavour 
		 */
		private Map modeOfDeliveryFlavours = new HashMap();

		/**
		 * Contains instances of {@link ModeOfDeliveryFlavourProductTypeGroup}.
		 */
		private List modeOfDeliveryFlavourProductTypeGroups = new ArrayList();

		public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
		{
			modeOfDeliveryFlavours.put(
					JDOHelper.getObjectId(modeOfDeliveryFlavour),
					modeOfDeliveryFlavour);
		}

		public void removeModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
		{
			modeOfDeliveryFlavours.remove(JDOHelper.getObjectId(modeOfDeliveryFlavour));
		}
		public void removeModeOfDeliveryFlavour(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
		{
			modeOfDeliveryFlavours.remove(modeOfDeliveryFlavourID);
		}
		public void clearModeOfDeliveryFlavours()
		{
			modeOfDeliveryFlavours.clear();
		}
		public Collection getModeOfDeliveryFlavours()
		{
			return modeOfDeliveryFlavours.values();
		}
		public ModeOfDeliveryFlavour getModeOfDeliveryFlavour(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
		{
			return (ModeOfDeliveryFlavour) modeOfDeliveryFlavours.get(modeOfDeliveryFlavourID);
		}

		public void addModeOfDeliveryFlavourProductTypeGroup(ModeOfDeliveryFlavourProductTypeGroup modeOfDeliveryFlavourProductTypeGroup)
		{
			modeOfDeliveryFlavourProductTypeGroups.add(modeOfDeliveryFlavourProductTypeGroup);
		}
		public List getModeOfDeliveryFlavourProductTypeGroups()
		{
			return Collections.unmodifiableList(modeOfDeliveryFlavourProductTypeGroups);
		}

		public void mergeGroups()
		{
			// TODO Later, this method can optimize the groups even further. Currently,
			// there's one group per DeliveryConfiguration. In case, these Configurations
			// have the same result (e.g. because of the filter set by the CustomerGroup),
			// they might be merged to reduce the number of wizard pages shown to the user.
			// IMPORTANT: The Configurations might have the same ModeOfDelivery[Flavour]s,
			// but different included/excluded Client/Server-DeliveryProcessors!

//			List newList = new ArrayList();
//			for (Iterator itG = modeOfDeliveryFlavourProductTypeGroups.iterator(); itG.hasNext(); ) {
//				ModeOfDeliveryFlavourProductTypeGroup groupRaw = (ModeOfDeliveryFlavourProductTypeGroup) itG.next();
//
//				ModeOfDeliveryFlavourProductTypeGroup groupConsolidated = null;
//				for (Iterator itN = newList.iterator(); itN.hasNext(); ) {
//					ModeOfDeliveryFlavourProductTypeGroup groupN = (ModeOfDeliveryFlavourProductTypeGroup) itN.next();
////					if (groupRaw.getModeOfDeliveryFlavourIDsSize() != groupN.getModeOfDeliveryFlavourIDsSize())
////						continue;
//
//					if (groupRaw.modeOfDeliveryFlavourIDs.equals(groupN.modeOfDeliveryFlavourIDs))
//						groupConsolidated = groupN;
//				}
//
//				if (groupConsolidated == null)
//					newList.add(groupRaw);
//				else
//					groupConsolidated.addProductTypeIDs(groupRaw.getProductTypeIDs());
//
//			}
//
//			this.modeOfDeliveryFlavourProductTypeGroups = newList;
		}
	}

	// TODO we have to melt the Server&ClientDeliveryProcessors into this (in conjunction
	// with the ModeOfDeliveryFlavours), as the DeliveryConfiguration can include/exclude
	// processors
	// TODO NO! Group by DeliveryConfiguration as it makes things much easier (and faster)
	// and will in most of the cases lead to the same result!
	public static class ModeOfDeliveryFlavourProductTypeGroup
	implements Serializable
	{
		private Set productTypeIDs = new HashSet();
		private Set modeOfDeliveryFlavourIDs = new HashSet();

		public void addProductTypeID(ProductTypeID productTypeID)
		{
			productTypeIDs.add(productTypeID);
		}
		public void addProductTypeIDs(Collection productTypeIDs)
		{
			productTypeIDs.addAll(productTypeIDs);
		}
		public void removeProductTypeID(ProductTypeID productTypeID)
		{
			productTypeIDs.remove(productTypeID);
		}
		public Set getProductTypeIDs()
		{
			return Collections.unmodifiableSet(productTypeIDs);
		}

		public void addModeOfDeliveryFlavourID(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
		{
			modeOfDeliveryFlavourIDs.add(modeOfDeliveryFlavourID);
		}
		public void removeModeOfDeliveryFlavourID(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
		{
			modeOfDeliveryFlavourIDs.remove(modeOfDeliveryFlavourID);
		}
		public int getModeOfDeliveryFlavourIDsSize()
		{
			return modeOfDeliveryFlavourIDs.size();
		}
		public Set getModeOfDeliveryFlavourIDs()
		{
			return Collections.unmodifiableSet(modeOfDeliveryFlavourIDs);
		}
	}

	/**
	 * @param productTypeIDs Instances of {@link ProductTypeID}.
	 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}.
	 * @param mergeMode see {@link #getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(PersistenceManager, Collection, byte)}
	 */
	public static ModeOfDeliveryFlavourProductTypeGroupCarrier
			getModeOfDeliveryFlavourProductTypeGroupCarrier(
					PersistenceManager pm, Collection productTypeIDs,
					Collection customerGroupIDs, byte mergeMode)
	{
		ModeOfDeliveryFlavourProductTypeGroupCarrier res = new ModeOfDeliveryFlavourProductTypeGroupCarrier(customerGroupIDs);

		Map modfAvailableForCustomerGroups = getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(pm, customerGroupIDs, mergeMode);

		Map deliveryConfigs = new HashMap();
		Map groupsByDeliveryConfigPK = new HashMap();
		HashSet modeOfDeliveryFlavourPKs = new HashSet();

		pm.getExtent(ProductType.class);
		for (Iterator itPT = productTypeIDs.iterator(); itPT.hasNext(); ) {
			ProductTypeID productTypeID = (ProductTypeID) itPT.next();
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);

			DeliveryConfiguration cf = productType.getDeliveryConfiguration();
			if (cf == null)
				throw new IllegalStateException("ProductType \""+productType.getPrimaryKey()+"\" has no DeliveryConfiguration assigned!");

			String cfPK = cf.getPrimaryKey();
			ModeOfDeliveryFlavourProductTypeGroup group = (ModeOfDeliveryFlavourProductTypeGroup) groupsByDeliveryConfigPK.get(cfPK);
			if (group == null) {
				deliveryConfigs.put(cfPK, cf);
				group = new ModeOfDeliveryFlavourProductTypeGroup();
				groupsByDeliveryConfigPK.put(cfPK, group);

				for (Iterator itMOD = cf.getModeOfDeliveries().iterator(); itMOD.hasNext(); ) {
					ModeOfDelivery mod = (ModeOfDelivery) itMOD.next();

					for (Iterator itMODF = mod.getFlavours().iterator(); itMODF.hasNext(); ) {
						ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) itMODF.next();
						ModeOfDeliveryFlavourID modfID = (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modf);

						// allowed by CustomerGroups?
						if (modfAvailableForCustomerGroups.containsKey(modf.getPrimaryKey())) {
							group.addModeOfDeliveryFlavourID(modfID);
							modeOfDeliveryFlavourPKs.add(modf.getPrimaryKey());
						}
					}
				}

				for (Iterator itMODF = cf.getModeOfDeliveryFlavours().iterator(); itMODF.hasNext(); ) {
					ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) itMODF.next();
					ModeOfDeliveryFlavourID modfID = (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modf);

					// allowed by CustomerGroups?
					if (modfAvailableForCustomerGroups.containsKey(modf.getPrimaryKey())) {
						group.addModeOfDeliveryFlavourID(modfID);
						modeOfDeliveryFlavourPKs.add(modf.getPrimaryKey());
					}
				}

				res.addModeOfDeliveryFlavourProductTypeGroup(group);
			}

			group.addProductTypeID(productTypeID);
		}

		for (Iterator it = modeOfDeliveryFlavourPKs.iterator(); it.hasNext(); ) {
			String modfPK = (String) it.next();
			ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) modfAvailableForCustomerGroups.get(modfPK);
			if (modf == null)
				throw new IllegalStateException("Found modeOfDeliveryPK \""+modfPK+"\" which is not registered in Map!");

			res.addModeOfDeliveryFlavour(modf);
		}

//		for (Iterator itPT = productTypeIDs.iterator(); itPT.hasNext(); ) {
//			ProductTypeID productTypeID = (ProductTypeID) itPT.next();
//
//			ModeOfDeliveryFlavourProductTypeGroup group = new ModeOfDeliveryFlavourProductTypeGroup();
//			group.addProductTypeID(productTypeID);
//
//			Collection c = getAvailableModeOfDeliveryFlavoursForOneProductType(pm, productTypeID);
//			for (Iterator itMODF = c.iterator(); itMODF.hasNext(); ) {
//				ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) itMODF.next();
//				res.addModeOfDeliveryFlavour(modf);
//				group.addModeOfDeliveryFlavourID(
//						(ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modf));
//			}
//
//			res.addModeOfDeliveryFlavourProductTypeGroup(group);
//		}

		res.mergeGroups();

		return res;
	}
	
	public static final byte MERGE_MODE_ADDITIVE = 1;
	public static final byte MERGE_MODE_SUBTRACTIVE = 2;

	/**
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}
	 * @param mergeMode Whether the intersection or the combination of all <tt>CustomerGroup</tt> configurations shall be used.
	 *
	 * @return Returns those <tt>ModeOfDeliveryFlavour</tt>s that are available for all given
	 *		<tt>CustomerGroup</tt>s. If <tt>mergeMode</tt> is {@link #MERGE_MODE_ADDITIVE},
	 *		they are combined like SQL UNION would do (means, if at least one
	 *		<tt>CustomerGroup</tt>
	 *		contains a certain <tt>ModeOfDeliveryFlavour</tt>, it will be in the result).
	 *		If <tt>mergeMode</tt> is {@link #MERGE_MODE_SUBTRACTIVE}, only those
	 *		<tt>ModeOfDeliveryFlavour</tt>s are returned that are available to all
	 *		<tt>CustomerGroup</tt>s.
	 *		<p>
	 *		key: String modeOfDeliveryFlavourPK<br/>
	 *		value: ModeOfDeliveryFlavour modf
	 */
	protected static Map getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(
			PersistenceManager pm, Collection customerGroupIDs, byte mergeMode)
	{
		if (mergeMode != MERGE_MODE_ADDITIVE && mergeMode != MERGE_MODE_SUBTRACTIVE)
			throw new IllegalArgumentException("mergeMode invalid! Must be MERGE_MODE_ADDITIVE or MERGE_MODE_SUBTRACTIVE!");

		Map res = null;
		for (Iterator itCustomerGroups = customerGroupIDs.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroupID customerGroupID = (CustomerGroupID) itCustomerGroups.next();

			Map m = getAvailableModeOfDeliveryFlavoursMapForOneCustomerGroup(pm, customerGroupID.organisationID, customerGroupID.customerGroupID);

			if (res == null) {
				res = m;
			}
			else {
				if (mergeMode == MERGE_MODE_SUBTRACTIVE) {
					// remove all missing
					for (Iterator it = res.keySet().iterator(); it.hasNext(); ) {
						String modfPK = (String) it.next();
						if (!m.containsKey(modfPK))
							it.remove();
					}
				} // if (mergeMode == MERGE_MODE_SUBTRACTIVE) {
				else { // if (mergeMode == MERGE_MODE_ADDITIVE) {
					// add all additional
					for (Iterator it = res.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry me = (Map.Entry)it.next();
						String modfPK = (String) me.getKey();
						ModeOfDeliveryFlavour modf = (ModeOfDeliveryFlavour) me.getValue();
						res.put(modfPK, modf);
					}
				} // if (mergeMode == MERGE_MODE_ADDITIVE) {
			}
		}

		return res;
	}

	protected static Map getAvailableModeOfDeliveryFlavoursMapForOneCustomerGroup(PersistenceManager pm, String organisationID, String customerGroupID)
	{
		// WORKAROUND The normal query returns an empty result, probably because of issues with ORs. 
		Map m = new HashMap();
		Query query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND1");
		for (Iterator it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) it.next();
			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
		}

		query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND2");
		for (Iterator it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) it.next();
			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
		}

		return m;
	}

//	public static Collection getAvailableModeOfDeliveryFlavoursForOneProductType(PersistenceManager pm, ProductTypeID productTypeID)
//	{
//		return getAvailableModeOfDeliveryFlavoursMapForOneProductType(pm, productTypeID.organisationID, productTypeID.productTypeID).values();
//	}
//
//	public static Collection getAvailableModeOfDeliveryFlavoursForOneProductType(PersistenceManager pm, String organisationID, String productTypeID)
//	{
//		return getAvailableModeOfDeliveryFlavoursMapForOneProductType(pm, organisationID, productTypeID).values();
//	}
//
//	protected static Map getAvailableModeOfDeliveryFlavoursMapForOneProductType(PersistenceManager pm, String organisationID, String productTypeID)
//	{
//		// WORKAROUND The normal query returns an empty result, probably because of issues with ORs. 
//		Map m = new HashMap();
//		Query query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneProductType_WORKAROUND1");
//		for (Iterator it = ((Collection)query.execute(organisationID, productTypeID)).iterator(); it.hasNext(); ) {
//			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) it.next();
//			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
//		}
//
//		query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneProductType_WORKAROUND2");
//		for (Iterator it = ((Collection)query.execute(organisationID, productTypeID)).iterator(); it.hasNext(); ) {
//			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) it.next();
//			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
//		}
//
//		return m;
//	}


	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String modeOfDeliveryFlavourID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ModeOfDelivery modeOfDelivery;
	
	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfDeliveryFlavour"
	 */
	private ModeOfDeliveryFlavourName name;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ModeOfDeliveryFlavour()
	{
	}

	public ModeOfDeliveryFlavour(String organisationID, String modeOfDeliveryFlavourID, ModeOfDelivery modeOfDelivery)
	{
		this.organisationID = organisationID;
		this.modeOfDeliveryFlavourID = modeOfDeliveryFlavourID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfDeliveryFlavourID);
		this.modeOfDelivery = modeOfDelivery;
		this.name = new ModeOfDeliveryFlavourName(this);
	}

	/**
	 * @return Returns the modeOfDelivery.
	 */
	public ModeOfDelivery getModeOfDelivery()
	{
		return modeOfDelivery;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfDeliveryFlavourID.
	 */
	public String getModeOfDeliveryFlavourID()
	{
		return modeOfDeliveryFlavourID;
	}

	public static String getPrimaryKey(String organisationID, String modeOfDeliveryFlavourID)
	{
		return organisationID + '/' + modeOfDeliveryFlavourID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfDeliveryFlavourName getName()
	{
		return name;
	}
}
