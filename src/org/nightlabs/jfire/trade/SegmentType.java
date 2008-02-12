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

package org.nightlabs.jfire.trade;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.id.SegmentTypeID;


/**
 * A <tt>SegmentType</tt> declares additional logic that should apply on a certain
 * group of {@link Article}s within an order/offer/invoice/delivery.
 * <p>
 * Example: A subscription
 * definition will be an instance of a subclass of <tt>SegmentType</tt>. There might e.g.
 * be a SegmentType class named "StaticSubscription" which can be instantiated and configured
 * to have a Set of fixed Events.
 * </p>
 * <p>
 * A {@link Segment} refers to its <tt>SegmentType</tt> and therefore applies this logic
 * within the sale process.
 * </p>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.SegmentTypeID"
 *		detachable="true"
 *		table="JFireTrade_SegmentType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, segmentTypeID"
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.fetch-group name="SegmentType.name" fields="name"
 * @jdo.fetch-group name="SegmentType.this" fetch-groups="default" fields="name"
 */
public class SegmentType implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_THIS_SEGMENT_TYPE = "SegmentType.this";
	public static final String FETCH_GROUP_NAME = "SegmentType.name";

	/**
	 * Though, there might be special segment types to create subscriptions and other
	 * forms of sales with higher logic, this defines the default <tt>SegmentType</tt> that
	 * is used in most of the cases.
	 */
	public static final SegmentTypeID DEFAULT_SEGMENT_TYPE_ID = SegmentTypeID.create(Organisation.DEV_ORGANISATION_ID, "default");

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String segmentTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="segmentType"
	 */
	private SegmentTypeName name;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SegmentType() { }

	public static SegmentType getDefaultSegmentType(PersistenceManager pm)
	{
		SegmentType segmentType;

		pm.getExtent(SegmentType.class);
		try {
			segmentType = (SegmentType) pm.getObjectById(DEFAULT_SEGMENT_TYPE_ID);
		} catch (JDOObjectNotFoundException x) {
			segmentType = new SegmentType(DEFAULT_SEGMENT_TYPE_ID.organisationID, DEFAULT_SEGMENT_TYPE_ID.segmentTypeID);
			segmentType.getName().setText(LanguageID.SYSTEM, "Default");
			pm.makePersistent(segmentType);
		}

		return segmentType;
	}

	public SegmentType(String organisationID, String segmentTypeID)
	{
		this.organisationID = organisationID;
		this.segmentTypeID = segmentTypeID;
		this.primaryKey = getPrimaryKey(organisationID, segmentTypeID);
		this.name = new SegmentTypeName(this);
	}

	public static String getPrimaryKey(String organisationID, String segmentTypeID)
	{
		return organisationID + '/' + segmentTypeID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getSegmentTypeID()
	{
		return segmentTypeID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public SegmentTypeName getName()
	{
		return name;
	}
}
