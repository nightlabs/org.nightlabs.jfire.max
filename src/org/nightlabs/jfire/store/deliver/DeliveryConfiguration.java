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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.store.deliver.id.DeliveryConfigurationID;

/**
 * An instance of this class is assigned to a
 * {@link org.nightlabs.jfire.store.ProductType}. It will then be
 * used to control which
 * {@link org.nightlabs.jfire.store.deliver.ModeOfDelivery}s/{@link org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour}s
 * are available. Many <tt>ProductType</tt>s usually use the same
 * <tt>DeliveryConfiguration</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=DeliveryConfigurationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryConfiguration")
@FetchGroups({
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_MODE_OF_DELIVERIES,
		members=@Persistent(name="modeOfDeliveries")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_MODE_OF_DELIVERY_FLAVOURS,
		members=@Persistent(name="modeOfDeliveryFlavours")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_INCLUDED_SERVER_DELIVERY_PROCESSORS,
		members=@Persistent(name="includedServerDeliveryProcessors")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_EXCLUDED_SERVER_DELIVERY_PROCESSORS,
		members=@Persistent(name="excludedServerDeliveryProcessors")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_INCLUDED_CLIENT_DELIVERY_PROCESSOR_FACTORY_IDS,
		members=@Persistent(name="includedClientDeliveryProcessorFactoryIDs")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_EXCLUDED_CLIENT_DELIVERY_PROCESSOR_FACTORY_IDS,
		members=@Persistent(name="excludedClientDeliveryProcessorFactoryIDs")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_CROSS_TRADE_DELIVERY_COORDINATOR,
		members=@Persistent(name="crossTradeDeliveryCoordinator")),
	@FetchGroup(
		name=DeliveryConfiguration.FETCH_GROUP_THIS_DELIVERY_CONFIGURATION,
		members={@Persistent(name="name"), @Persistent(name="modeOfDeliveries"), @Persistent(name="modeOfDeliveryFlavours"), @Persistent(name="includedServerDeliveryProcessors"), @Persistent(name="excludedServerDeliveryProcessors"), @Persistent(name="includedClientDeliveryProcessorFactoryIDs"), @Persistent(name="excludedClientDeliveryProcessorFactoryIDs"), @Persistent(name="crossTradeDeliveryCoordinator")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_DELIVERY_CONFIGURATION = "DeliveryConfiguration.this";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String deliveryConfigurationID;

	/**
	 * key: String primaryKey<br/>
	 * value: ModeOfDelivery
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_modeOfDeliveries"
	)
	private Map<String, ModeOfDelivery> modeOfDeliveries;

	/**
	 * key: String primaryKey<br/>
	 * value: ModeOfDeliveryFlavour
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_modeOfDeliveryFlavours"
	)
	private Map<String, ModeOfDeliveryFlavour> modeOfDeliveryFlavours;


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

	@Persistent
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

	@Persistent
	private byte clientDeliveryProcessorSupportMode = CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_ALL;


	/**
	 * This <tt>Map</tt> allows to specify a certain subset of server-sided processors
	 * which is available in this configuration. For activation of this <tt>Map</tt>
	 * {@link #serverDeliveryProcessorSupportMode} must be set to
	 * {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED}.
	 *
	 * key: String primaryKey<br/>
	 * value: ServerDeliveryProcessor
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_includedServerDeliveryProcessors",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ServerDeliveryProcessor> includedServerDeliveryProcessors;

	/**
	 * This <tt>Map</tt> allows to specify a certain subset of server-sided processors
	 * which is NOT available in this configuration. For activation of this <tt>Map</tt>
	 * {@link #serverDeliveryProcessorSupportMode} must be set to
	 * {@link #SERVER_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED}.
	 *
	 * key: String primaryKey<br/>
	 * value: ServerDeliveryProcessor
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_excludedServerDeliveryProcessors",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ServerDeliveryProcessor> excludedServerDeliveryProcessors;

	/**
	 * This <tt>Set</tt> allows to specify a certain subset of client-sided processors
	 * which is available in this configuration. For activation of this <tt>Set</tt>
	 * {@link #clientDeliveryProcessorSupportMode} must be set to
	 * {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_INCLUDED}.
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_includedClientDeliveryProcessorFactoryIDs"
	)
	private Set<String> includedClientDeliveryProcessorFactoryIDs;

	/**
	 * This <tt>Set</tt> allows to specify a certain subset of client-sided processors
	 * which is NOT available in this configuration. For activation of this <tt>Set</tt>
	 * {@link #clientDeliveryProcessorSupportMode} must be set to
	 * {@link #CLIENT_DELIVERY_PROCESSOR_SUPPORT_MODE_EXCLUDED}.
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryConfiguration_excludedClientDeliveryProcessorFactoryIDs"
	)
	private Set<String> excludedClientDeliveryProcessorFactoryIDs;

	@Persistent(
		dependent="true",
		mappedBy="deliveryConfiguration"
	)
	private DeliveryConfigurationName name;

	@Persistent
	private CrossTradeDeliveryCoordinator crossTradeDeliveryCoordinator = null;

	/**
	 * @deprecated Constructor only for JDO!
	 */
	@Deprecated
	protected DeliveryConfiguration() { }

	public DeliveryConfiguration(DeliveryConfigurationID deliveryConfigurationID)
	{
		this(deliveryConfigurationID.organisationID, deliveryConfigurationID.deliveryConfigurationID);
	}

	public DeliveryConfiguration(String organisationID, String deliveryConfigurationID)
	{
		this.organisationID = organisationID;
		this.deliveryConfigurationID = deliveryConfigurationID;

		name = new DeliveryConfigurationName(this);

		modeOfDeliveries = new HashMap<String, ModeOfDelivery>();
		modeOfDeliveryFlavours = new HashMap<String, ModeOfDeliveryFlavour>();
		includedServerDeliveryProcessors = new HashMap<String, ServerDeliveryProcessor>();
		excludedServerDeliveryProcessors = new HashMap<String, ServerDeliveryProcessor>();
		includedClientDeliveryProcessorFactoryIDs = new HashSet<String>();
		excludedClientDeliveryProcessorFactoryIDs = new HashSet<String>();
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
		modeOfDeliveries.put(modeOfDelivery.getPrimaryKey(), modeOfDelivery);
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
	public Collection<ModeOfDelivery> getModeOfDeliveries()
	{
		return Collections.unmodifiableCollection(modeOfDeliveries.values());
	}

	public void addModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		modeOfDeliveryFlavours.put(modeOfDeliveryFlavour.getPrimaryKey(), modeOfDeliveryFlavour);
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
	public Collection<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours()
	{
		return Collections.unmodifiableCollection(modeOfDeliveryFlavours.values());
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
