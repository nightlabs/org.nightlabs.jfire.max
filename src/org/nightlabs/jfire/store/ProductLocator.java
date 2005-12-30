/*
 * Created on May 9, 2005
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;

/**
 * This is a tagging interface which doesn't offer any logic. An implementation
 * must carry information with which a {@link org.nightlabs.jfire.store.ProductType}
 * is able to locate the desired {@link org.nightlabs.jfire.store.Product}. Hence,
 * the <tt>ProductLocator</tt> implementation is specific and dependent on the
 * <tt>ProductType</tt> implementation. For a seat, the locator could e.g. specify
 * the section, row and column (or a SeatID which is unique within a certain seating
 * map).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface ProductLocator extends Serializable
{

}
