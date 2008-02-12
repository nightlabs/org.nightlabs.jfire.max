package org.nightlabs.jfire.transfer.query;

import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class AbstractTransferQuery<T>
extends JDOQuery<T>
{
	private static final long serialVersionUID = 1L;
	private Date timestampFromIncl = null;
	private Date timestampToIncl = null;

//	private long rangeFromIncl = 0;
//	private long rangeToExcl = Long.MAX_VALUE;

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
			// TODO JPOX WORKAROUND end
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

//	public long getRangeFromIncl()
//	{
//		return rangeFromIncl;
//	}
//	public void setRangeFromIncl(long rangeFromIncl)
//	{
//		this.rangeFromIncl = rangeFromIncl;
//	}
//	public long getRangeToExcl()
//	{
//		return rangeToExcl;
//	}
//	public void setRangeToExcl(long rangeToExcl)
//	{
//		this.rangeToExcl = rangeToExcl;
//	}
	public Date getTimestampFromIncl()
	{
		return timestampFromIncl;
	}
	public void setTimestampFromIncl(Date timestampFromIncl)
	{
		this.timestampFromIncl = timestampFromIncl;
	}
	public Date getTimestampToIncl()
	{
		return timestampToIncl;
	}
	public void setTimestampToIncl(Date timestampToIncl)
	{
		this.timestampToIncl = timestampToIncl;
	}

	public AnchorID getCurrentAnchorID()
	{
		return currentAnchorID;
	}
	public void setCurrentAnchorID(AnchorID currentAnchorID)
	{
		this.currentAnchorID = currentAnchorID;
	}
	public AnchorID getOtherAnchorID()
	{
		return otherAnchorID;
	}
	public void setOtherAnchorID(AnchorID otherAnchorID)
	{
		this.otherAnchorID = otherAnchorID;
	}

	public AnchorID getFromAnchorID()
	{
		return fromAnchorID;
	}
	public void setFromAnchorID(AnchorID fromAnchorID)
	{
		this.fromAnchorID = fromAnchorID;
	}
	public AnchorID getToAnchorID()
	{
		return toAnchorID;
	}
	public void setToAnchorID(AnchorID toAnchorID)
	{
		this.toAnchorID = toAnchorID;
	}
}
