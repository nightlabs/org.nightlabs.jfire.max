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

/**
 * An instance of this class is assigned to a
 * {@link org.nightlabs.jfire.store.ProductType}. It will then be
 * used to control which
 * {@link org.nightlabs.jfire.store.deliver.ModeOfDelivery}s/{@link org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour}s
 * are available. Many <tt>ProductType</tt>s usually use the same
 * <tt>DeliveryConfiguration</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryConfigurationID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryConfiguration"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryConfigurationID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="DeliveryConfiguration.name" fields="name"
 * @jdo.fetch-group name="DeliveryConfiguration.modeOfDeliveries" fields="modeOfDeliveries"
 * @jdo.fetch-group name="DeliveryConfiguration.modeOfDeliveryFlavours" fields="modeOfDeliveryFlavours"
 * @jdo.fetch-group name="DeliveryConfiguration.includedServerDeliveryProcessors" fields="includedServerDeliveryProcessors"
 * @jdo.fetch-group name="DeliveryConfiguration.excludedServerDeliveryProcessors" fields="excludedServerDeliveryProcessors"
 * @jdo.fetch-group name="DeliveryConfiguration.includedClientDeliveryProcessorFactoryIDs" fields="includedClientDeliveryProcessorFactoryIDs"
 * @jdo.fetch-group name="DeliveryConfiguration.excludedClientDeliveryProcessorFactoryIDs" fields="excludedClientDeliveryProcessorFactoryIDs"
 * @jdo.fetch-group name="DeliveryConfiguration.crossTradeDeliveryCoordinator" fields="crossTradeDeliveryCoordinator"
 *
 * @jdo.fetch-group name="DeliveryConfiguration.this" fields="name, modeOfDeliveries, modeOfDeliveryFlavours, includedServerDeliveryProcessors, excludedServerDeliveryProcessors, includedClientDeliveryProcessorFactoryIDs, excludedClientDeliveryProcessorFactoryIDs, crossTradeDeliveryCoordinator"
 */
public class DeliveryConfiguration
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DeliveryConfiguration.name";
	public static final String FETCH_GROUP_MODE_OF_DELIVERIES = "DeliveryConfiguration.modeOfDeliveries";
	public static final String FETCH_GROUP_MODE_OF_DELIVERY_FLAVOURS	= "DeliveryConfiguration.modeOfDeliveryFlavours";
	public static final String FETCH_GROUP_INCLUDED_SERVER_DELIVERY_PROCESSORS = "DeliveryConfiguration.includedServerDeliveryProcessors";
	public static final String FETCH_GROUP_EXCLUDED_SERVER_DELIVERY_PROCESSORS = "DeliveryConfiguration.excludedServerDeliveryProcessors";
	public static final String FETCH_GROUP_INCLUDED_CLIENT_DELIVERY_PROCESSOR_FACTORY_IDS = "DeliveryConfiguration.includedClientDeliveryProcessorFactoryIDs";
	public static final String FETCH_GROUP_EXCLUDED_CLIENT_DELIVERY_PROCESSOR_FACTORY_IDS = "DeliveryConfiguration.excludedClientDeliveryProcessorFactoryIDs";
	public static final String FETCH_GROUP_CROSS_TRADE_DELIVERY_COORDINATOR = "DeliveryConfiguration.crossTradeDeliveryCoordinator";
	public static final String FETCH_GROUP_THIS_DELIVERY_CONFIGURATION = "DeliveryConfiguration.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryConfigurationID;

	/**
	 * key: String primaryKey<br/>
	 * value: ModeOfDelivery
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ModeOfDelivery"
	 *		table="JFireTrade_DeliveryConfiguration_modeOfDeliveries"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map modeOfDeliveries;

	/**
	 * key: String primaryKey<br/>
	 * value: ModeOfDeliveryFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ModeOfDeliveryFlavour"
	 *		table="JFireTrade_DeliveryConfiguration_modeOfDeliveryFlavours"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map modeOfDeliveryFlavours;


	/**
	 * Because a <tt>Map</tt> member cannot be null in a jdo object, we need to
	 * somehow specify whether the include or the exclude map or none should be used.
	 * <ul>
	 *  <li>
	 *   <tt>SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL</tt> defines that all
	 *   {@link ServerDeliveryProcessor}s are supported and the maps hence ignored.
	 *  </li>
	 *  <li>
	 *   {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED} means that only
	 *   the explicitely declared {@link ServerDeliveryProcessor}s are supported.
	 *   This will cause consultation of {@link #includedServerDeliveryProcessors}.
	 *  </li>
	 *  <li>
	 *   {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED} means that all
	 *   {@link ServerDeliveryProcessor}s except the ones explicitely declared are
	 *   supported.
	 *   This will cause consultation of {@link #excludedServerDeliveryProcessors}.
	 *  </li>
	 * </ul>
	 */
	public static final byte SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL = 0;

	/**
	 * @see #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL
	 */
	public static final byte SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED = 1;

	/**
	 * @see #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL
	 */
	public static final byte SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED = 2;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte serverDeliveryProcessorSupportMode = SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL;


	/**
	 * Because a <tt>Set</tt> member cannot be null in a jdo object, we need to
	 * somehow specify whether the include or the exclude map or none should be used.
	 * <ul>
	 *  <li>
	 *   <tt>CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL</tt> defines that all
	 *   {@link ClientDeliveryProcessor}s are supported and the maps hence ignored.
	 *  </li>
	 *  <li>
	 *   {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED} means that only
	 *   the explicitely declared {@link ClientDeliveryProcessor}s are supported.
	 *   This will cause consultation of {@link #includedClientDeliveryProcessorFactoryIDs}.
	 *  </li>
	 *  <li>
	 *   {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED} means that all
	 *   {@link ClientDeliveryProcessor}s except the ones explicitely declared are
	 *   supported.
	 *   This will cause consultation of {@link #excludedClientDeliveryProcessorFactoryIDs}.
	 *  </li>
	 * </ul>
	 */
	public static final byte CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL = 0;

	/**
	 * @see #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL
	 */
	public static final byte CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED = 1;

	/**
	 * @see #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL
	 */
	public static final byte CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED = 2;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte clientDeliveryProcessorSupportMode = CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL;


	/**
	 * This <tt>Map</tt> allows to specify a certain subset of server-sided processors
	 * which is available in this configuration. For activation of this <tt>Map</tt>
	 * {@link #serverDeliveryProcessorSupportMode} must be set to
	 * {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED}.
	 *
	 * key: String primaryKey<br/>
	 * value: ServerDeliveryProcessor
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ServerDeliveryProcessor"
	 *		table="JFireTrade_DeliveryConfiguration_includedServerDeliveryProcessors"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map includedServerDeliveryProcessors;

	/**
	 * This <tt>Map</tt> allows to specify a certain subset of server-sided processors
	 * which is NOT available in this configuration. For activation of this <tt>Map</tt>
	 * {@link #serverDeliveryProcessorSupportMode} must be set to
	 * {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED}.
	 *
	 * key: String primaryKey<br/>
	 * value: ServerDeliveryProcessor
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ServerDeliveryProcessor"
	 *		table="JFireTrade_DeliveryConfiguration_excludedServerDeliveryProcessors"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map excludedServerDeliveryProcessors;

	/**
	 * This <tt>Set</tt> allows to specify a certain subset of client-sided processors
	 * which is available in this configuration. For activation of this <tt>Set</tt>
	 * {@link #clientDeliveryProcessorSupportMode} must be set to
	 * {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="java.lang.String"
	 *		table="JFireTrade_DeliveryConfiguration_includedClientDeliveryProcessorFactoryIDs"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set includedClientDeliveryProcessorFactoryIDs;

	/**
	 * This <tt>Set</tt> allows to specify a certain subset of client-sided processors
	 * which is NOT available in this configuration. For activation of this <tt>Set</tt>
	 * {@link #clientDeliveryProcessorSupportMode} must be set to
	 * {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="java.lang.String"
	 *		table="JFireTrade_DeliveryConfiguration_excludedClientDeliveryProcessorFactoryIDs"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set excludedClientDeliveryProcessorFactoryIDs;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="deliveryConfiguration"
	 */
	private DeliveryConfigurationName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = null;

	/**
	 * @deprecated Constructor only for JDO!
	 */
	protected DeliveryConfiguration() { }

	public DeliveryConfiguration(String organisationID, String deliveryConfigurationID)
	{
		this.organisationID = organisationID;
		this.deliveryConfigurationID = deliveryConfigurationID;

		name = new DeliveryConfigurationName(this);

		modeOfDeliveries = new HashMap();
		modeOfDeliveryFlavours = new HashMap();
		includedServerDeliveryProcessors = new HashMap();
		excludedServerDeliveryProcessors = new HashMap();
		includedClientDeliveryProcessorFactoryIDs = new HashSet();
		excludedClientDeliveryProcessorFactoryIDs = new HashSet();
	}

	/**
	 * @return Returns the deliveryConfigurationID.
	 */
	public String getDeliveryConfigurationID()
	{
		return deliveryConfigurationID;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	public static String getPrimaryKey(String organisationID, String deliveryConfigurationID)
	{
		return organisationID + '/' + deliveryConfigurationID;
	}
	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, deliveryConfigurationID);
	}
	public void addModeOfDelivery(ModeOfDelivery modeOfDelivery)
	{
		modeOfDeliveries.put(
				modeOfDelivery.getPrimaryKey(), modeOfDelivery);
	}

	public void removeModeOfDelivery(ModeOfDelivery modeOfDelivery)
	{
		removeModeOfDelivery(modeOfDelivery.getPrimaryKey());
	}
	public void removeModeOfDelivery(String organisationID, String modeOfDeliveryID)
	{
		removeModeOfDelivery(ModeOfDelivery.getPrimaryKey(organisationID, modeOfDeliveryID));
	}
	public void removeModeOfDelivery(String modeOfDeliveryPK)
	{
		modeOfDeliveries.remove(modeOfDeliveryPK);
	}

	/**
	 * @return Returns the modeOfDeliveries.
	 */
	public Collection getModeOfDeliveries()
	{
		return modeOfDeliveries.values();
	}

	public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		modeOfDeliveryFlavours.put(
				modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
	}

	public void removeModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		removeModeOfDeliveryFlavour(modeOfDeliveryFlavour.getPrimaryKey());
	}
	public void removeModeOfDeliveryFlavour(String organisationID, String modeOfDeliveryFlavourID)
	{
		removeModeOfDeliveryFlavour(ModeOfDeliveryFlavour.getPrimaryKey(organisationID, modeOfDeliveryFlavourID));
	}
	public void removeModeOfDeliveryFlavour(String modeOfDeliveryFlavourPK)
	{
		modeOfDeliveryFlavours.remove(modeOfDeliveryFlavourPK);
	}

	/**
	 * @return Returns the modeOfDeliveryFlavours.
	 */
	public Collection getModeOfDeliveryFlavours()
	{
		return modeOfDeliveryFlavours.values();
	}

	/**
	 * @return Returns the name.
	 */
	public DeliveryConfigurationName getName()
	{
		return name;
	}

	public CrossTradeDeliveryCoordinator getCrossTradeDeliveryCoordinator()
	{
		return crossTradeDeliveryCoordinator;
	}
	public void setCrossTradeDeliveryCoordinator(CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator)
	{
		this.crossTradeDeliveryCoordinator = crossTradeDeliveryCoordinator;
	}
}
