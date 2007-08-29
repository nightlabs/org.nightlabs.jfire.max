package org.nightlabs.jfire.store.query;

import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

public class ProductTransferQuery
extends JDOQuery<ProductTransfer>
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

		if (currentRepositoryID != null)
			filter.append(" && (JDOHelper.getObjectId(this.from) == :currentRepositoryID || JDOHelper.getObjectId(this.to) == :currentRepositoryID)");

		if (otherRepositoryID != null)
			filter.append(" && (JDOHelper.getObjectId(this.from) == :otherRepositoryID || JDOHelper.getObjectId(this.to) == :otherRepositoryID)");

		if (fromRepositoryID != null)
			filter.append(" && JDOHelper.getObjectId(this.from) == :fromRepositoryID");

		if (toRepositoryID != null)
			filter.append(" && JDOHelper.getObjectId(this.to) == :toRepositoryID");

		q.setFilter(filter.toString());
		q.setRange(rangeFromIncl, rangeToExcl);
		q.setOrdering("this.timestamp DESCENDING, organisationID ASCENDING, transferTypeID ASCENDING, transferID DESCENDING");

		return q;
	}

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
