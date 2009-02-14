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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.deliver.config.ModeOfDeliveryConfigModule;
import org.nightlabs.jfire.store.deliver.id.DeliveryConfigurationID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

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
 * @jdo.fetch-group name="ModeOfDeliveryFlavour.icon16x16Data" fields="icon16x16Data"
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
 *
 * @jdo.query
 *		name="getAllModeOfDeliveryFlavourIDs"
 *		query="SELECT JDOHelper.getObjectId(this)"
 */
public class ModeOfDeliveryFlavour
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ModeOfDeliveryFlavour.name";
	public static final String FETCH_GROUP_MODE_OF_DELIVERY = "ModeOfDeliveryFlavour.modeOfDelivery";
	public static final String FETCH_GROUP_ICON_16X16_DATA = "ModeOfDeliveryFlavour.icon16x16Data";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_MODE_OF_DELIVERY_FLAVOUR = "ModeOfDeliveryFlavour.this";

	public static class ModeOfDeliveryFlavourProductTypeGroupCarrier
	implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}
		 */
		public ModeOfDeliveryFlavourProductTypeGroupCarrier(Collection<CustomerGroupID> customerGroupIDs)
		{
			this.customerGroupIDs = customerGroupIDs;
		}

		private Collection<CustomerGroupID> customerGroupIDs;

		/**
		 * key: {@link org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID} modeOfDeliveryFlavourID<br/>
		 * value: {@link ModeOfDeliveryFlavour} modeOfDeliveryFlavour
		 */
		private Map<ModeOfDeliveryFlavourID, ModeOfDeliveryFlavour> modeOfDeliveryFlavours =
			new HashMap<ModeOfDeliveryFlavourID, ModeOfDeliveryFlavour>();

		/**
		 * Contains instances of {@link ModeOfDeliveryFlavourProductTypeGroup}.
		 */
		private List<ModeOfDeliveryFlavourProductTypeGroup> modeOfDeliveryFlavourProductTypeGroups =
			new ArrayList<ModeOfDeliveryFlavourProductTypeGroup>();

		public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
		{
			modeOfDeliveryFlavours.put(
					(ModeOfDeliveryFlavourID)JDOHelper.getObjectId(modeOfDeliveryFlavour),
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
		public Collection<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours()
		{
			return modeOfDeliveryFlavours.values();
		}
		public ModeOfDeliveryFlavour getModeOfDeliveryFlavour(ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
		{
			return modeOfDeliveryFlavours.get(modeOfDeliveryFlavourID);
		}

		public void addModeOfDeliveryFlavourProductTypeGroup(ModeOfDeliveryFlavourProductTypeGroup modeOfDeliveryFlavourProductTypeGroup)
		{
			modeOfDeliveryFlavourProductTypeGroups.add(modeOfDeliveryFlavourProductTypeGroup);
		}
		public List<ModeOfDeliveryFlavourProductTypeGroup> getModeOfDeliveryFlavourProductTypeGroups()
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

	public static class ModeOfDeliveryFlavourProductTypeGroup
	implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private DeliveryConfigurationID deliveryConfigurationID;
		private Set<ProductTypeID> productTypeIDs = new HashSet<ProductTypeID>();
		private Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs = new HashSet<ModeOfDeliveryFlavourID>();

		public void addProductTypeID(ProductTypeID productTypeID)
		{
			productTypeIDs.add(productTypeID);
		}
		public void addProductTypeIDs(Collection<ProductTypeID> productTypeIDs)
		{
			productTypeIDs.addAll(productTypeIDs);
		}
		public void removeProductTypeID(ProductTypeID productTypeID)
		{
			productTypeIDs.remove(productTypeID);
		}
		public Set<ProductTypeID> getProductTypeIDs()
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
		public Set<ModeOfDeliveryFlavourID> getModeOfDeliveryFlavourIDs()
		{
			return Collections.unmodifiableSet(modeOfDeliveryFlavourIDs);
		}
		public DeliveryConfigurationID getDeliveryConfigurationID() {
			return deliveryConfigurationID;
		}
		public void setDeliveryConfigurationID(
				DeliveryConfigurationID deliveryConfigurationID) {
			this.deliveryConfigurationID = deliveryConfigurationID;
		}
	}

	/**
	 * @param productTypeIDs Instances of {@link ProductTypeID}.
	 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}.
	 * @param mergeMode see {@link #getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(PersistenceManager, Collection, byte)}
	 * @param filterByConfig
	 * 		If this is <code>true</code> the flavours available found for the given product-types and customer-groups will also be filtered by the 
	 * 		intersection of the entries configured in the {@link ModeOfDeliveryConfigModule} for the current user and the 
	 * 		workstation he is currently loggen on. 
	 * 
	 */
	public static ModeOfDeliveryFlavourProductTypeGroupCarrier
			getModeOfDeliveryFlavourProductTypeGroupCarrier(
					PersistenceManager pm, Collection<ProductTypeID> productTypeIDs,
					Collection<CustomerGroupID> customerGroupIDs, byte mergeMode, boolean filterByConfig)
	{
		ModeOfDeliveryFlavourProductTypeGroupCarrier res = new ModeOfDeliveryFlavourProductTypeGroupCarrier(customerGroupIDs);

		Map<String, ModeOfDeliveryFlavour> modfAvailableForCustomerGroups = getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(pm, customerGroupIDs, mergeMode, filterByConfig);

		Map<String, DeliveryConfiguration> deliveryConfigs = new HashMap<String, DeliveryConfiguration>();
		Map<String, ModeOfDeliveryFlavourProductTypeGroup> groupsByDeliveryConfigPK = new HashMap<String, ModeOfDeliveryFlavourProductTypeGroup>();
		Set<String> modeOfDeliveryFlavourPKs = new HashSet<String>();

		pm.getExtent(ProductType.class);
		for (Iterator<ProductTypeID> itPT = productTypeIDs.iterator(); itPT.hasNext(); ) {
			ProductTypeID productTypeID = itPT.next();
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);

			DeliveryConfiguration deliveryConfiguration = productType.getDeliveryConfiguration();
			if (deliveryConfiguration == null)
				throw new IllegalStateException("ProductType \""+productType.getPrimaryKey()+"\" has no DeliveryConfiguration assigned!");

			DeliveryConfigurationID deliveryConfigurationID = (DeliveryConfigurationID) JDOHelper.getObjectId(deliveryConfiguration);
			if (deliveryConfigurationID == null)
				throw new IllegalStateException("JDOHelper.getObjectId(deliveryConfiguration) returned null!");

			String cfPK = deliveryConfiguration.getPrimaryKey();
			ModeOfDeliveryFlavourProductTypeGroup group = groupsByDeliveryConfigPK.get(cfPK);
			if (group == null) {
				deliveryConfigs.put(cfPK, deliveryConfiguration);
				group = new ModeOfDeliveryFlavourProductTypeGroup();
				group.setDeliveryConfigurationID(deliveryConfigurationID);
				groupsByDeliveryConfigPK.put(cfPK, group);

				for (Iterator<ModeOfDelivery> itMOD = deliveryConfiguration.getModeOfDeliveries().iterator(); itMOD.hasNext(); ) {
					ModeOfDelivery mod = itMOD.next();

					for (Iterator<ModeOfDeliveryFlavour> itMODF = mod.getFlavours().iterator(); itMODF.hasNext(); ) {
						ModeOfDeliveryFlavour modf = itMODF.next();
						ModeOfDeliveryFlavourID modfID = (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modf);

						// allowed by CustomerGroups?
						if (modfAvailableForCustomerGroups.containsKey(modf.getPrimaryKey())) {
							group.addModeOfDeliveryFlavourID(modfID);
							modeOfDeliveryFlavourPKs.add(modf.getPrimaryKey());
						}
					}
				}

				for (Iterator<ModeOfDeliveryFlavour> itMODF = deliveryConfiguration.getModeOfDeliveryFlavours().iterator(); itMODF.hasNext(); ) {
					ModeOfDeliveryFlavour modf = itMODF.next();
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

		for (Iterator<String> it = modeOfDeliveryFlavourPKs.iterator(); it.hasNext(); ) {
			String modfPK = it.next();
			ModeOfDeliveryFlavour modf = modfAvailableForCustomerGroups.get(modfPK);
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
	 *		<tt>CustomerGroup</tt>s. If <tt>mergeMode</tt> is {@link #MERGE_MODE_UNION},
	 *		they are combined like SQL UNION would do (means, if at least one
	 *		<tt>CustomerGroup</tt>
	 *		contains a certain <tt>ModeOfDeliveryFlavour</tt>, it will be in the result).
	 *		If <tt>mergeMode</tt> is {@link #MERGE_MODE_INTERSECTION}, only those
	 *		<tt>ModeOfDeliveryFlavour</tt>s are returned that are available to all
	 *		<tt>CustomerGroup</tt>s.
	 *		<p>
	 *		key: String modeOfDeliveryFlavourPK<br/>
	 *		value: ModeOfDeliveryFlavour modf
	 */
	protected static Map<String, ModeOfDeliveryFlavour> getAvailableModeOfDeliveryFlavoursMapForAllCustomerGroups(PersistenceManager pm, Collection<CustomerGroupID> customerGroupIDs, byte mergeMode, boolean filterByConfig)
	{
		if (mergeMode != MERGE_MODE_ADDITIVE && mergeMode != MERGE_MODE_SUBTRACTIVE)
			throw new IllegalArgumentException("mergeMode invalid! Must be MERGE_MODE_UNION or MERGE_MODE_INTERSECTION!");

		Map<String, ModeOfDeliveryFlavour> res = null;
		for (Iterator<CustomerGroupID> itCustomerGroups = customerGroupIDs.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroupID customerGroupID = itCustomerGroups.next();

			Map<String, ModeOfDeliveryFlavour> m = getAvailableModeOfDeliveryFlavoursMapForOneCustomerGroup(pm, customerGroupID.organisationID, customerGroupID.customerGroupID, filterByConfig);

			if (res == null) {
				res = m;
			}
			else {
				if (mergeMode == MERGE_MODE_SUBTRACTIVE) {
					// remove all missing
					for (Iterator<String> it = res.keySet().iterator(); it.hasNext(); ) {
						String modfPK = it.next();
						if (!m.containsKey(modfPK))
							it.remove();
					}
				} // if (mergeMode == MERGE_MODE_INTERSECTION) {
				else { // if (mergeMode == MERGE_MODE_UNION) {
					// add all additional
					for (Iterator<Map.Entry<String, ModeOfDeliveryFlavour>> it = res.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<String, ModeOfDeliveryFlavour> me = it.next();
						String modfPK = me.getKey();
						ModeOfDeliveryFlavour modf = me.getValue();
						res.put(modfPK, modf);
					}
				} // if (mergeMode == MERGE_MODE_UNION) {
			}
		}

		if (res == null)
			return new HashMap<String, ModeOfDeliveryFlavour>();
		else
			return res;
	}

	protected static Map<String, ModeOfDeliveryFlavour> getAvailableModeOfDeliveryFlavoursMapForOneCustomerGroup(
			PersistenceManager pm, String organisationID, String customerGroupID, boolean filterByConfig)
	{
		// WORKAROUND The normal query returns an empty result, probably because of issues with ORs.
		Map<String, ModeOfDeliveryFlavour> m = new HashMap<String, ModeOfDeliveryFlavour>();
		Query query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND1");
		for (Iterator<ModeOfDeliveryFlavour> it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = it.next();
			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
		}

		query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAvailableModeOfDeliveryFlavoursForOneCustomerGroup_WORKAROUND2");
		for (Iterator<ModeOfDeliveryFlavour> it = ((Collection)query.execute(organisationID, customerGroupID)).iterator(); it.hasNext(); ) {
			ModeOfDeliveryFlavour modeOfDeliveryFlavour = it.next();
			m.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
		}

		if (filterByConfig) {
			ModeOfDeliveryConfigModule cfMod;
			cfMod = UserConfigSetup.getUserConfigModule(pm, ModeOfDeliveryConfigModule.class);
			for (Iterator<Map.Entry<String, ModeOfDeliveryFlavour>> it = m.entrySet().iterator(); it.hasNext(); ) {
				if (!cfMod.getModeOfDeliveryFlavours().contains(it.next().getValue())) {
					it.remove();
				}
			}
			cfMod = WorkstationConfigSetup.getWorkstationConfigModule(pm, ModeOfDeliveryConfigModule.class);
			for (Iterator<Map.Entry<String, ModeOfDeliveryFlavour>> it = m.entrySet().iterator(); it.hasNext(); ) {
				if (!cfMod.getModeOfDeliveryFlavours().contains(it.next().getValue())) {
					it.remove();
				}
			}
		}
		
		return m;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<ModeOfDeliveryFlavour> getAllModeOfDeliveryFlavours(PersistenceManager pm) {
		HashSet<ModeOfDeliveryFlavour> result = new HashSet<ModeOfDeliveryFlavour>();
		for (Iterator it = pm.getExtent(ModeOfDeliveryFlavour.class).iterator(); it.hasNext(); ) {
			result.add((ModeOfDeliveryFlavour) it.next());
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<ModeOfDeliveryFlavourID> getAllModeOfDeliveryFlavourIDs(PersistenceManager pm) {
		Query query = pm.newNamedQuery(ModeOfDeliveryFlavour.class, "getAllModeOfDeliveryFlavourIDs");
		return new HashSet<ModeOfDeliveryFlavourID>((Collection<? extends ModeOfDeliveryFlavourID>) query.execute());
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

//	/**
//	* @jdo.field persistence-modifier="persistent"
//	*/
//	private Date icon16x16Timestamp;
//
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	private byte[] icon16x16Data;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
//	public Date getIcon16x16Timestamp()
//	{
//	return icon16x16Timestamp;
//	}
//	public void setIcon16x16Timestamp(Date icon16x16Timestamp)
//	{
//	this.icon16x16Timestamp = icon16x16Timestamp;
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
	 * Calls {@link #loadIconFromResource(Class, String) } with <code>resourceLoaderClass == </code>
	 * {@link ModeOfDeliveryFlavour} and <code>fileName == "ModeOfDeliveryFlavour-" + modeOfDeliveryFlavourID + ".16x16.png"</code>.
	 * This method is used for the default {@link ModeOfDeliveryFlavour}s populated by JFireTrade.
	 *
	 * @throws IOException
	 */
	public void loadIconFromResource() throws IOException
	{
		String resourcePath = "resource/" + ModeOfDeliveryFlavour.class.getSimpleName() + '-' + modeOfDeliveryFlavourID + ".16x16.png";
		loadIconFromResource(
				ModeOfDeliveryFlavour.class, resourcePath);
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
	public void loadIconFromResource(Class resourceLoaderClass, String fileName) throws IOException
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
		return Util.hashCode(organisationID) + Util.hashCode(modeOfDeliveryFlavourID);
	}
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ModeOfDeliveryFlavour)) return false;
		ModeOfDeliveryFlavour o = (ModeOfDeliveryFlavour) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.modeOfDeliveryFlavourID, o.modeOfDeliveryFlavourID);
	}
}
