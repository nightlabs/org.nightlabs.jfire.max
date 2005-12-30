/*
 * Created on Apr 4, 2005
 */
package org.nightlabs.ipanema.trade;

import java.io.Serializable;

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
 *		objectid-class="org.nightlabs.ipanema.trade.id.SegmentID"
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
