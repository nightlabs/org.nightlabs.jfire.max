/*
 * Created on 20.10.2004
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.TransferRegistry;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Transfer"
 *		detachable="true"
 *		table="JFireTrade_ProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ProductTransfer extends Transfer implements Serializable
{
	public static final String TRANSFERTYPEID = "ProductTransfer";

	/**
	 * key: String productPrimaryKey (see {@link Product#getPrimaryKey()})<br/>
	 * value: ProductType product
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Product"
	 *		table="JFireTrade_ProductTransfer_products"
	 *
	 * @jdo.join
	 *
	 * @!jdo.key-column length="130"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 130"
	 */
	private Collection products = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductTransfer() { }

	/**
	 * @param transferRegistry
	 * @param from
	 * @param to
	 * @param container
	 * @param products
	 */
	public ProductTransfer(
			TransferRegistry transferRegistry, Transfer container, User initiator,
			Anchor from, Anchor to, Collection products)
	{
		super(transferRegistry, TRANSFERTYPEID, container, initiator, from, to);
		this.products = new ArrayList(products);
//		this.products = new HashMap();
//
//		// WORKAROUND begin
//		Product p = (Product) products.iterator().next();
//		this.products.put(p.getPrimaryKey(), p);
//		this.products.clear();
//		// WORKAROUND There is a problem in JPOX which causes the Map to ignore the first entry otherwise
//
//		for (Iterator it = products.iterator(); it.hasNext(); ) {
//			Product product = (Product)it.next();
//			this.products.put(product.getPrimaryKey(), product);
//		}
	}

	/**
	 * @return Returns instances of {@link Product}. You MUST NOT manipulate the returned collection!
	 */
	public Collection getProducts()
	{
		return products;
	}

	public void bookTransfer(User user, Map involvedAnchors)
	{
		// TODO We must - either here or somewhere else - ensure that all the nested products are transferred, too (and have the same repository as the package product afterwards).
		super.bookTransfer(user, involvedAnchors);
	}
}
