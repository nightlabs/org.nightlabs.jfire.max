package org.nightlabs.jfire.transfer.query;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class AbstractTransferQuery<T>
extends AbstractJDOQuery<T>
{
	private static final long serialVersionUID = 1L;
	private Date timestampFromIncl = null;
	private Date timestampToIncl = null;

	private AnchorID fromAnchorID = null;
	private AnchorID toAnchorID = null;

	private AnchorID currentAnchorID = null;
	private AnchorID otherAnchorID = null;


	// TODO JPOX WORKAROUND begin
	@SuppressWarnings("unused")
	private transient Anchor fromAnchor = null;
	@SuppressWarnings("unused")
	private transient Anchor toAnchor = null;

	@SuppressWarnings("unused")
	private transient Anchor currentAnchor = null;
	@SuppressWarnings("unused")
	private transient Anchor otherAnchor = null;
	// TODO JPOX WORKAROUND end

	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "AbstractTransferQuery.";
	public static final String PROPERTY_CURRENT_ANCHOR_ID = PROPERTY_PREFIX + "currentAnchorID";
	public static final String PROPERTY_FROM_ANCHOR_ID = PROPERTY_PREFIX + "fromAnchorID";
	public static final String PROPERTY_TO_ANCHOR_ID = PROPERTY_PREFIX + "toAnchorID";
	public static final String PROPERTY_OTHER_ANCHOR_ID = PROPERTY_PREFIX + "otherAnchorID";
	public static final String PROPERTY_TIMESTAMP_FROM = PROPERTY_PREFIX + "timestampFromIncl";
	public static final String PROPERTY_TIMESTAMP_TO = PROPERTY_PREFIX + "timestampToIncl";

	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		final List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		final boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
		if (allFields || PROPERTY_CURRENT_ANCHOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, currentAnchorID) );
		}
		if (allFields || PROPERTY_FROM_ANCHOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, fromAnchorID) );
		}
		if (allFields || PROPERTY_OTHER_ANCHOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, otherAnchorID) );
		}
		if (allFields || PROPERTY_TIMESTAMP_FROM.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, timestampFromIncl) );
		}
		if (allFields || PROPERTY_TIMESTAMP_TO.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, timestampToIncl) );
		}
		if (allFields || PROPERTY_TO_ANCHOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(propertyName, toAnchorID) );
		}
		
		return changedFields;
	}
	
	protected abstract Class<? extends Transfer> getCandidateClass();

	@Override
	protected Query prepareQuery()
	{
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(getCandidateClass());
		StringBuffer filter = new StringBuffer();

		filter.append("true");

		if (timestampFromIncl != null)
			filter.append(" && this.timestamp >= :timestampFromIncl");

		if (timestampToIncl != null)
			filter.append(" && this.timestamp <= :timestampToIncl");


		if (currentAnchorID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :currentAnchorID || JDOHelper.getObjectId(this.to) == :currentAnchorID)");
			// TODO JPOX WORKAROUND begin
			currentAnchor = (Anchor) pm.getObjectById(currentAnchorID);
			filter.append(" && (this.from == :currentAnchor || this.to == :currentAnchor)");
			// TODO JPOX WORKAROUND endsetToAnchorID
		}

		if (otherAnchorID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :otherAnchorID || JDOHelper.getObjectId(this.to) == :otherAnchorID)");
			// TODO JPOX WORKAROUND begin
			otherAnchor = (Anchor) pm.getObjectById(otherAnchorID);
			filter.append(" && (this.from == :otherAnchor || this.to == :otherAnchor)");
			// TODO JPOX WORKAROUND end
		}

		if (fromAnchorID != null) {
//			filter.append(" && JDOHelper.getObjectId(this.from) == :fromAnchorID");
			// TODO JPOX WORKAROUND begin
			fromAnchor = (Anchor) pm.getObjectById(fromAnchorID);
			filter.append(" && this.from == :fromAnchor");
			// TODO JPOX WORKAROUND end
		}

		if (toAnchorID != null) {
//			filter.append(" && JDOHelper.getObjectId(this.to) == :toAnchorID");
			// TODO JPOX WORKAROUND begin
			toAnchor = (Anchor) pm.getObjectById(toAnchorID);
			filter.append(" && this.to == :toAnchor");
			// TODO JPOX WORKAROUND end
		}

		appendToFilter(q, filter);
		q.setFilter(filter.toString());
//		q.setRange(rangeFromIncl, rangeToExcl);
		q.setOrdering("this.timestamp DESCENDING, organisationID ASCENDING, transferTypeID ASCENDING, transferID DESCENDING");
		setQueryResult(q);

		return q;
	}

	protected abstract void appendToFilter(Query q, StringBuffer filter);

	protected abstract void setQueryResult(Query q);

	public Date getTimestampFromIncl()
	{
		return timestampFromIncl;
	}
	public void setTimestampFromIncl(Date timestampFromIncl)
	{
		final Date oldTimestampFromIncl = this.timestampFromIncl;
		this.timestampFromIncl = timestampFromIncl;
		notifyListeners(PROPERTY_TIMESTAMP_FROM, oldTimestampFromIncl, timestampFromIncl);
	}
	public Date getTimestampToIncl()
	{
		return timestampToIncl;
	}
	public void setTimestampToIncl(Date timestampToIncl)
	{
		final Date oldTimestampToIncl = this.timestampToIncl;
		this.timestampToIncl = timestampToIncl;
		notifyListeners(PROPERTY_TIMESTAMP_TO, oldTimestampToIncl, timestampToIncl);
	}

	public AnchorID getCurrentAnchorID()
	{
		return currentAnchorID;
	}
	public void setCurrentAnchorID(AnchorID currentAnchorID)
	{
		final AnchorID oldCurrentAnchorID = this.currentAnchorID;
		this.currentAnchorID = currentAnchorID;
		notifyListeners(PROPERTY_CURRENT_ANCHOR_ID, oldCurrentAnchorID, currentAnchorID);
	}
	public AnchorID getOtherAnchorID()
	{
		return otherAnchorID;
	}
	public void setOtherAnchorID(AnchorID otherAnchorID)
	{
		final AnchorID oldOtherAnchorID = this.otherAnchorID;
		this.otherAnchorID = otherAnchorID;
		notifyListeners(PROPERTY_OTHER_ANCHOR_ID, oldOtherAnchorID, otherAnchorID);	
	}

	public AnchorID getFromAnchorID()
	{
		return fromAnchorID;
	}
	public void setFromAnchorID(AnchorID fromAnchorID)
	{
		final AnchorID oldFromAnchorID = this.fromAnchorID;
		this.fromAnchorID = fromAnchorID;
		notifyListeners(PROPERTY_FROM_ANCHOR_ID, oldFromAnchorID, fromAnchorID);
	}
	public AnchorID getToAnchorID()
	{
		return toAnchorID;
	}
	public void setToAnchorID(AnchorID toAnchorID)
	{
		final AnchorID oldToAnchorID = this.toAnchorID;
		this.toAnchorID = toAnchorID;
		notifyListeners(PROPERTY_TO_ANCHOR_ID, oldToAnchorID, toAnchorID);
	}
}
