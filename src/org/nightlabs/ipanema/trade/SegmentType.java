/*
 * Created on Apr 4, 2005
 */
package org.nightlabs.ipanema.trade;

import java.io.Serializable;
import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.language.Language;
import org.nightlabs.ipanema.language.id.LanguageID;
import org.nightlabs.ipanema.organisation.Organisation;
import org.nightlabs.ipanema.trade.id.SegmentTypeID;


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
 *		objectid-class="org.nightlabs.ipanema.trade.id.SegmentTypeID"
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
	public static final String FETCH_GROUP_THIS_SEGMENT_TYPE = "SegmentType.this";
	public static final String FETCH_GROUP_NAME = "SegmentType.name";

	/**
	 * Though, there might be special segment types to create subscriptions and other
	 * forms of sales with higher logic, this defines the default <tt>SegmentType</tt> that
	 * is used in most of the cases.
	 */
	public static final String DEFAULT_SEGMENT_TYPE_ID = "default";

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

	protected SegmentType()
	{
	}

	public static SegmentType getDefaultSegmentType(PersistenceManager pm)
	{
		SegmentType segmentType;

		pm.getExtent(SegmentType.class);
		try {
			segmentType = (SegmentType) pm.getObjectById(
					SegmentTypeID.create(Organisation.DEVIL_ORGANISATION_ID, SegmentType.DEFAULT_SEGMENT_TYPE_ID));
		} catch (JDOObjectNotFoundException x) {
			segmentType = new SegmentType(Organisation.DEVIL_ORGANISATION_ID, SegmentType.DEFAULT_SEGMENT_TYPE_ID);
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
