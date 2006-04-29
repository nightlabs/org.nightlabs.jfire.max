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

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.DeliveryNote;

/**
 * One {@link Order}, {@link Offer}, {@link Invoice} or {@link DeliveryNote} may contain
 * many <tt>Segment</tt>s. Each <tt>Segment</tt> knows its {@link SegmentType}. This layer
 * is used to provide additional logic for a certain group of {@link Article}s - e.g. for
 * a subscription. Additionally, the GUI can use different composites to render different
 * kinds of <tt>Segment</tt>s. For example it could decide to hide the <tt>Article</tt>s
 * of one subscription and only to show a "bundle" view of them.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.SegmentID"
 *		detachable="true"
 *		table="JFireTrade_Segment"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, segmentID"
 *
 * @jdo.fetch-group name="Segment.order" fields="order"
 * @jdo.fetch-group name="Segment.segmentType" fields="segmentType"
 * @jdo.fetch-group name="Segment.this" fetch-groups="default" fields="order, segmentType"
 */
public class Segment implements Serializable
{
	public static final String FETCH_GROUP_ORDER = "Segment.order";
	public static final String FETCH_GROUP_SEGMENT_TYPE = "Segment.segmentType";
	public static final String FETCH_GROUP_THIS_SEGMENT = "Segment.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long segmentID;

	public static long createSegmentID()
	{
		return IDGenerator.nextID(Segment.class.getName());
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private SegmentType segmentType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Order order;

	protected Segment()
	{
	}

	public Segment(String organisationID, long segmentID, SegmentType segmentType, Order order)
	{
		this.organisationID = organisationID;
		this.segmentID = segmentID;
		this.primaryKey = getPrimaryKey(organisationID, segmentID);

		this.segmentType = segmentType;
		this.order = order;
	}

	public static String getPrimaryKey(String organisationID, long segmentID)
	{
		return organisationID + '/' + Long.toHexString(segmentID);
	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the segmentID.
	 */
	public long getSegmentID()
	{
		return segmentID;
	}
	/**
	 * @return Returns the order.
	 */
	public Order getOrder()
	{
		return order;
	}
	/**
	 * @return Returns the segmentType.
	 */
	public SegmentType getSegmentType()
	{
		return segmentType;
	}
}
