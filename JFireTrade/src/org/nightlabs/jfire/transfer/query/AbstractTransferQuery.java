package org.nightlabs.jfire.transfer.query;

import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class AbstractTransferQuery
	extends AbstractJDOQuery
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

	public static final class FieldName
	{
		public static final String currentAnchorID = "currentAnchorID";
		public static final String fromAnchorID = "fromAnchorID";
		public static final String toAnchorID = "toAnchorID";
		public static final String otherAnchorID = "otherAnchorID";
		public static final String timestampFromIncl = "timestampFromIncl";
		public static final String timestampToIncl = "timestampToIncl";
	}

	@Override
	protected void prepareQuery(Query q)
	{
		PersistenceManager pm = getPersistenceManager();
		StringBuffer filter = new StringBuffer();

		filter.append("true");

		if (isFieldEnabled(FieldName.timestampFromIncl) && timestampFromIncl != null)
			filter.append(" && this.timestamp >= :timestampFromIncl");

		if (isFieldEnabled(FieldName.timestampToIncl) && timestampToIncl != null)
			filter.append(" && this.timestamp <= :timestampToIncl");


		if (isFieldEnabled(FieldName.currentAnchorID) && currentAnchorID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :currentAnchorID || JDOHelper.getObjectId(this.to) == :currentAnchorID)");
			// TODO JPOX WORKAROUND begin
			currentAnchor = (Anchor) pm.getObjectById(currentAnchorID);
			filter.append(" && (this.from == :currentAnchor || this.to == :currentAnchor)");
			// TODO JPOX WORKAROUND endsetToAnchorID
		}

		if (isFieldEnabled(FieldName.otherAnchorID) && otherAnchorID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :otherAnchorID || JDOHelper.getObjectId(this.to) == :otherAnchorID)");
			// TODO JPOX WORKAROUND begin
			otherAnchor = (Anchor) pm.getObjectById(otherAnchorID);
			filter.append(" && (this.from == :otherAnchor || this.to == :otherAnchor)");
			// TODO JPOX WORKAROUND end
		}

		if (isFieldEnabled(FieldName.fromAnchorID) && fromAnchorID != null) {
//			filter.append(" && JDOHelper.getObjectId(this.from) == :fromAnchorID");
			// TODO JPOX WORKAROUND begin
			fromAnchor = (Anchor) pm.getObjectById(fromAnchorID);
			filter.append(" && this.from == :fromAnchor");
			// TODO JPOX WORKAROUND end
		}

		if (isFieldEnabled(FieldName.toAnchorID) && toAnchorID != null) {
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
		notifyListeners(FieldName.timestampFromIncl, oldTimestampFromIncl, timestampFromIncl);
	}
	public Date getTimestampToIncl()
	{
		return timestampToIncl;
	}
	public void setTimestampToIncl(Date timestampToIncl)
	{
		final Date oldTimestampToIncl = this.timestampToIncl;
		this.timestampToIncl = timestampToIncl;
		notifyListeners(FieldName.timestampToIncl, oldTimestampToIncl, timestampToIncl);
	}

	public AnchorID getCurrentAnchorID()
	{
		return currentAnchorID;
	}
	public void setCurrentAnchorID(AnchorID currentAnchorID)
	{
		final AnchorID oldCurrentAnchorID = this.currentAnchorID;
		this.currentAnchorID = currentAnchorID;
		notifyListeners(FieldName.currentAnchorID, oldCurrentAnchorID, currentAnchorID);
	}
	public AnchorID getOtherAnchorID()
	{
		return otherAnchorID;
	}
	public void setOtherAnchorID(AnchorID otherAnchorID)
	{
		final AnchorID oldOtherAnchorID = this.otherAnchorID;
		this.otherAnchorID = otherAnchorID;
		notifyListeners(FieldName.otherAnchorID, oldOtherAnchorID, otherAnchorID);
	}

	public AnchorID getFromAnchorID()
	{
		return fromAnchorID;
	}
	public void setFromAnchorID(AnchorID fromAnchorID)
	{
		final AnchorID oldFromAnchorID = this.fromAnchorID;
		this.fromAnchorID = fromAnchorID;
		notifyListeners(FieldName.fromAnchorID, oldFromAnchorID, fromAnchorID);
	}
	public AnchorID getToAnchorID()
	{
		return toAnchorID;
	}
	public void setToAnchorID(AnchorID toAnchorID)
	{
		final AnchorID oldToAnchorID = this.toAnchorID;
		this.toAnchorID = toAnchorID;
		notifyListeners(FieldName.toAnchorID, oldToAnchorID, toAnchorID);
	}
}