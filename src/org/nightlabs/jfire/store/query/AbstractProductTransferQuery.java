package org.nightlabs.jfire.store.query;

import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class AbstractProductTransferQuery<E>
extends JDOQuery<E>
{
	private static final long serialVersionUID = 1L;

	private Date timestampFromIncl = null;
	private Date timestampToIncl = null;

	private long rangeFromIncl = 0;
	private long rangeToExcl = Long.MAX_VALUE;

	private AnchorID fromRepositoryID = null;
	private AnchorID toRepositoryID = null;

	private AnchorID currentRepositoryID = null;
	private AnchorID otherRepositoryID = null;


	// TODO JPOX WORKAROUND begin
	@SuppressWarnings("unused")
	private transient Repository fromRepository = null;
	@SuppressWarnings("unused")
	private transient Repository toRepository = null;

	@SuppressWarnings("unused")
	private transient Repository currentRepository = null;
	@SuppressWarnings("unused")
	private transient Repository otherRepository = null;
	// TODO JPOX WORKAROUND end


	@Override
	protected Query prepareQuery()
	{
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(ProductTransfer.class);
		StringBuffer filter = new StringBuffer();

		filter.append("true");

		if (timestampFromIncl != null)
			filter.append(" && this.timestamp >= :timestampFromIncl");

		if (timestampToIncl != null)
			filter.append(" && this.timestamp <= :timestampToIncl");


		if (currentRepositoryID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :currentRepositoryID || JDOHelper.getObjectId(this.to) == :currentRepositoryID)");
			// TODO JPOX WORKAROUND begin
			currentRepository = (Repository) pm.getObjectById(currentRepositoryID);
			filter.append(" && (this.from == :currentRepository || this.to == :currentRepository)");
			// TODO JPOX WORKAROUND end
		}

		if (otherRepositoryID != null) {
//			filter.append(" && (JDOHelper.getObjectId(this.from) == :otherRepositoryID || JDOHelper.getObjectId(this.to) == :otherRepositoryID)");
			// TODO JPOX WORKAROUND begin
			otherRepository = (Repository) pm.getObjectById(otherRepositoryID);
			filter.append(" && (this.from == :otherRepository || this.to == :otherRepository)");
			// TODO JPOX WORKAROUND end
		}

		if (fromRepositoryID != null) {
//			filter.append(" && JDOHelper.getObjectId(this.from) == :fromRepositoryID");
			// TODO JPOX WORKAROUND begin
			fromRepository = (Repository) pm.getObjectById(fromRepositoryID);
			filter.append(" && this.from == :fromRepository");
			// TODO JPOX WORKAROUND end
		}

		if (toRepositoryID != null) {
//			filter.append(" && JDOHelper.getObjectId(this.to) == :toRepositoryID");
			// TODO JPOX WORKAROUND begin
			toRepository = (Repository) pm.getObjectById(toRepositoryID);
			filter.append(" && this.to == :toRepository");
			// TODO JPOX WORKAROUND end
		}

		q.setFilter(filter.toString());
		q.setRange(rangeFromIncl, rangeToExcl);
		q.setOrdering("this.timestamp DESCENDING, organisationID ASCENDING, transferTypeID ASCENDING, transferID DESCENDING");
		setQueryResult(q);

		return q;
	}

	protected abstract void setQueryResult(Query q);

	public long getRangeFromIncl()
	{
		return rangeFromIncl;
	}
	public void setRangeFromIncl(long rangeFromIncl)
	{
		this.rangeFromIncl = rangeFromIncl;
	}
	public long getRangeToExcl()
	{
		return rangeToExcl;
	}
	public void setRangeToExcl(long rangeToExcl)
	{
		this.rangeToExcl = rangeToExcl;
	}
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

	public AnchorID getCurrentRepositoryID()
	{
		return currentRepositoryID;
	}
	public void setCurrentRepositoryID(AnchorID currentRepositoryID)
	{
		this.currentRepositoryID = currentRepositoryID;
	}
	public AnchorID getOtherRepositoryID()
	{
		return otherRepositoryID;
	}
	public void setOtherRepositoryID(AnchorID otherRepositoryID)
	{
		this.otherRepositoryID = otherRepositoryID;
	}

	public AnchorID getFromRepositoryID()
	{
		return fromRepositoryID;
	}
	public void setFromRepositoryID(AnchorID fromRepositoryID)
	{
		this.fromRepositoryID = fromRepositoryID;
	}
	public AnchorID getToRepositoryID()
	{
		return toRepositoryID;
	}
	public void setToRepositoryID(AnchorID toRepositoryID)
	{
		this.toRepositoryID = toRepositoryID;
	}
}
