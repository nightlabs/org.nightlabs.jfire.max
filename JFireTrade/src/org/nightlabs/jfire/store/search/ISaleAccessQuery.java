package org.nightlabs.jfire.store.search;

import org.nightlabs.jdo.query.SearchQuery;
import org.nightlabs.jfire.store.ProductType;

/**
 * Interface for Queries which can query the common sale access states of {@link ProductType}s,
 * like confirmed, published, saleable and closed.
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public interface ISaleAccessQuery
	extends SearchQuery
{
	/**
	 * Returns the published.
	 * @return the published
	 */
	public Boolean getPublished();

	/**
	 * Sets the published.
	 * @param published the published to set
	 */
	public void setPublished(Boolean published);

	/**
	 * Returns the confirmed.
	 * @return the confirmed
	 */
	public Boolean getConfirmed();

	/**
	 * Sets the confirmed.
	 * @param confirmed the confirmed to set
	 */
	public void setConfirmed(Boolean confirmed);

	/**
	 * Returns the saleable.
	 * @return the saleable
	 */
	public Boolean getSaleable();

	/**
	 * Sets the saleable.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(Boolean saleable);

	/**
	 * Returns the closed.
	 * @return the closed
	 */
	public Boolean getClosed();

	/**
	 * Sets the closed.
	 * @param closed the closed to set
	 */
	public void setClosed(Boolean closed);

}
