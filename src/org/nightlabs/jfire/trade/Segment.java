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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.util.Util;

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
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields=""
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields=""
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="segmentType"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="segmentType"
 *
 * @jdo.query name="getSegmentTypeIDsOfOrder" query="SELECT JDOHelper.getObjectId(this.segmentType) WHERE this.order == :order"
 * @jdo.query name="getSegmentIDsOfOrderAndSegmentType" query="SELECT JDOHelper.getObjectId(this) WHERE this.order == :order && this.segmentType == :segmentType"
 */
public class Segment implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ORDER = "Segment.order";
	public static final String FETCH_GROUP_SEGMENT_TYPE = "Segment.segmentType";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_SEGMENT = "Segment.this";

	public static Set<SegmentTypeID> getSegmentTypeIDs(PersistenceManager pm, Order order)
	{
		Query q = pm.newNamedQuery(Segment.class, "getSegmentTypeIDsOfOrder");
		return new HashSet<SegmentTypeID>((Collection<? extends SegmentTypeID>) q.execute(order));
	}

	public static Set<SegmentID> getSegmentIDs(PersistenceManager pm, Order order, SegmentType segmentType)
	{
		Query q = pm.newNamedQuery(Segment.class, "getSegmentIDsOfOrderAndSegmentType");
		return new HashSet<SegmentID>((Collection<? extends SegmentID>) q.execute(order, segmentType));
	}

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
		return IDGenerator.nextID(Segment.class);
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
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(segmentID);
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
	 * @see #getSegmentIDAsString()
	 */
	public long getSegmentID()
	{
		return segmentID;
	}
	public String getSegmentIDAsString()
	{
		return ObjectIDUtil.longObjectIDFieldToString(segmentID);
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

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Segment))
			return false;

		Segment o = (Segment) obj;

		return Util.equals(this.organisationID, o.organisationID) && Util.equals(this.segmentID, o.segmentID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(segmentID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(segmentID) + ']';
	}
}
