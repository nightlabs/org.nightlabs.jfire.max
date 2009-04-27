/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DeleteCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductReferenceGroupID;
import org.nightlabs.jfire.store.id.ProductReferenceID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * This class makes asynchronous deliveries possible. Instead of waiting until all the necessary products have
 * been delivered to an organisation, before this one can deliver it further, a
 * {@link org.nightlabs.jfire.store.deliver.Delivery} can deliver not-yet-existing
 * {@link org.nightlabs.jfire.store.Product}s. If a {@link org.nightlabs.jfire.store.Repository}
 * is used as the "from" of a {@link org.nightlabs.jfire.store.ProductTransfer} where a
 * {@link org.nightlabs.jfire.store.Product} does currently not exist, a <code>ProductReference</code>
 * is created.
 * <p>
 * The <code>ProductReference</code> is deleted, once the {@link org.nightlabs.jfire.store.Product} is
 * delivered (causing the <code>Product</code> to be forwarded immediately).
 * </p>
 * <p>
 * If a <code>ProductReference</code> exists longer than a few minutes, sth. went wrong! That's why the
 * system should check and send emails to an administrator in this case.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductReferenceID"
 *		detachable="true"
 *		table="JFireTrade_ProductReference"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="anchorOrganisationID, anchorAnchorTypeID, anchorAnchorID, productOrganisationID, productProductID"
 *		include-body="id/ProductReferenceID.body.inc"
 *
 * @jdo.query name="getProductReferencesForAnchor" query="SELECT
 *		WHERE anchor == pAnchor
 *		PARAMETERS Anchor pAnchor
 *		import org.nightlabs.jfire.transfer.Anchor"
 *
 * @jdo.query name="getProductReferencesForProduct" query="SELECT
 *		WHERE product == pProduct
 *		PARAMETERS Product pProduct
 *		import org.nightlabs.jfire.store.Product"
 *
 * @jdo.query name="getProductReferencesForProductAndQuantity" query="SELECT
 *		WHERE product == pProduct && quantity == pQuantity
 *		PARAMETERS Product pProduct, int pQuantity
 *		import org.nightlabs.jfire.store.Product"
 *
 * @jdo.query name="getProductReferenceCountForProductReferenceGroup" query="SELECT count(productProductID)
 *		WHERE productReferenceGroup == pProductReferenceGroup
 *		PARAMETERS ProductReferenceGroup pProductReferenceGroup
 *		import org.nightlabs.jfire.store.ProductReferenceGroup"
 */
@PersistenceCapable(
	objectIdClass=ProductReferenceID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductReference")
@Queries({
	@javax.jdo.annotations.Query(
		name="getProductReferencesForAnchor",
		value="SELECT WHERE anchor == pAnchor PARAMETERS Anchor pAnchor import org.nightlabs.jfire.transfer.Anchor"),
	@javax.jdo.annotations.Query(
		name="getProductReferencesForProduct",
		value="SELECT WHERE product == pProduct PARAMETERS Product pProduct import org.nightlabs.jfire.store.Product"),
	@javax.jdo.annotations.Query(
		name="getProductReferencesForProductAndQuantity",
		value="SELECT WHERE product == pProduct && quantity == pQuantity PARAMETERS Product pProduct, int pQuantity import org.nightlabs.jfire.store.Product"),
	@javax.jdo.annotations.Query(
		name="getProductReferenceCountForProductReferenceGroup",
		value="SELECT count(productProductID) WHERE productReferenceGroup == pProductReferenceGroup PARAMETERS ProductReferenceGroup pProductReferenceGroup import org.nightlabs.jfire.store.ProductReferenceGroup")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProductReference
implements Serializable, DeleteCallback
{
	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private final Logger logger = Logger.getLogger(ProductReference.class);

	/**
	 * This method finds out, how many <code>ProductReference</code>s use the given <code>productReferenceGroup</code>.
	 *
	 * @param pm Accessor to the datastore.
	 * @param productReferenceGroup The group for which to search all {@link ProductReference}s that have
	 *		{@link ProductReference#getProductReferenceGroup()} pointing to. Must not be null.
	 * @return Returns instances of <code>ProductReference</code>.
	 */
	public static long getProductReferenceCount(PersistenceManager pm, ProductReferenceGroup productReferenceGroup)
	{
		Query q = pm.newNamedQuery(ProductReference.class, "getProductReferenceCountForProductReferenceGroup");
//		return ((Long)((Collection)q.execute(productReferenceGroup)).iterator().next()).longValue();
		return ((Long)q.execute(productReferenceGroup)).longValue();
	}

	public static ProductReference getProductReference(
			PersistenceManager pm,
			Anchor anchor, Product product, boolean throwExceptionIfNotFound)
	{
		pm.getExtent(ProductReference.class);
		try {
			ProductReference res = (ProductReference) pm.getObjectById(ProductReferenceID.create(anchor, product));
			res.getPrimaryKey(); // workaround for JPOX bug
			return res;
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}
		return null;
	}

	public static ProductReference getProductReference(
			PersistenceManager pm,
			AnchorID anchorID, ProductID productID, boolean throwExceptionIfNotFound)
	{
		pm.getExtent(ProductReference.class);
		try {
			ProductReference res = (ProductReference) pm.getObjectById(ProductReferenceID.create(anchorID, productID));
			res.getPrimaryKey(); // workaround for JPOX bug
			return res;
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends ProductReference> getProductReferences(PersistenceManager pm, Anchor anchor)
	{
		Query q = pm.newNamedQuery(ProductReference.class, "getProductReferencesForAnchor");
		return (Collection<? extends ProductReference>) q.execute(anchor);
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends ProductReference> getProductReferences(PersistenceManager pm, Product product)
	{
		Query q = pm.newNamedQuery(ProductReference.class, "getProductReferencesForProduct");
		return (Collection<? extends ProductReference>) q.execute(product);
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends ProductReference> getProductReferences(
			PersistenceManager pm,
			Product product, int quantity)
	{
		Query q = pm.newNamedQuery(ProductReference.class, "getProductReferencesForProductAndQuantity");
		return (Collection<? extends ProductReference>) q.execute(product, new Integer(quantity));
	}

	/**
	 * This method creates the desired <code>ProductReference</code> if it does not yet exist.
	 *
	 * @return Returns the <code>ProductReference</code> - never returns <code>null</code>.
	 */
	public static ProductReference createProductReference(
			PersistenceManager pm, Anchor anchor, Product product)
	{
		pm.getExtent(ProductReference.class);
		try {
			return (ProductReference) pm.getObjectById(ProductReferenceID.create(anchor, product));
		} catch (JDOObjectNotFoundException x) {
			ProductReference productReference = new ProductReference(anchor, product);
			productReference = pm.makePersistent(productReference);
			return productReference;
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String anchorOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String anchorAnchorTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String anchorAnchorID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String productOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long productProductID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Anchor anchor;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Product product;

	/**
	 * Only outside repositories have productReferenceGroups.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductReferenceGroup productReferenceGroup = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int quantity = 0;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductReference() {}

	public ProductReference(Anchor anchor, Product product)
	{
		this.anchor = anchor;
		this.product = product;
		this.anchorOrganisationID = anchor.getOrganisationID();
		this.anchorAnchorTypeID = anchor.getAnchorTypeID();
		this.anchorAnchorID = anchor.getAnchorID();
		this.productOrganisationID = product.getOrganisationID();
		this.productProductID = product.getProductID();
		this.createDT = new Date();

		if (anchor instanceof Repository) {
			Repository repository = (Repository)anchor;
			if (repository.getRepositoryType().isOutside()) {
				String productReferenceGroupOrganisationID = repository.getOwner().getOrganisationID();
				String productReferenceGroupID = repository.getOwner().getAnchorTypeID() + '.' + repository.getOwner().getAnchorID();

				PersistenceManager pm = JDOHelper.getPersistenceManager(anchor);
				if (pm == null)
					throw new IllegalStateException("Cannot obtain PersistenceManager from anchor!");

				// only OUTSIDE repositories have ProductReferenceGroups
				try {
					productReferenceGroup = (ProductReferenceGroup) pm.getObjectById(ProductReferenceGroupID.create(
							productReferenceGroupOrganisationID,
							anchor.getAnchorTypeID(),
							productReferenceGroupID,
							productOrganisationID,
							productProductID));
				} catch (JDOObjectNotFoundException x) {
					productReferenceGroup = new ProductReferenceGroup(
							productReferenceGroupOrganisationID,
							anchor.getAnchorTypeID(),
							productReferenceGroupID,
							productOrganisationID,
							productProductID);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace(this.getClass().getName() + ".<init>: pk=\"" + getPrimaryKey() + "\"", new Exception("STACKTRACE"));
			else
				logger.debug(this.getClass().getName() + ".<init>: pk=\"" + getPrimaryKey() + "\"");
		}
	}

	public String getAnchorOrganisationID()
	{
		return anchorOrganisationID;
	}
	public String getAnchorAnchorTypeID()
	{
		return anchorAnchorTypeID;
	}
	public String getAnchorAnchorID()
	{
		return anchorAnchorID;
	}
	public String getProductOrganisationID()
	{
		return productOrganisationID;
	}
	public long getProductProductID()
	{
		return productProductID;
	}

	public String getPrimaryKey()
	{
		return anchorOrganisationID + '/' + anchorAnchorTypeID + '/' + anchorAnchorID + '/' + productOrganisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(productProductID);
	}

	/**
	 * @return Returns the <code>Anchor</code> in which this <code>ProductReference</code> exists.
	 */
	public Anchor getAnchor()
	{
		return anchor;
	}

	public Product getProduct()
	{
		return product;
	}

	public ProductReferenceGroup getProductReferenceGroup()
	{
		return productReferenceGroup;
	}

	/**
	 * @return Returns the timestamp when this <code>ProductReference</code> has been created.
	 */
	public Date getCreateDT()
	{
		return createDT;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("ProductReference is currently not attached to a datastore - cannot obtain PersistenceManager!");
		return pm;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public int incQuantity()
	{
		quantity = quantity + 1;
		if (productReferenceGroup == null)
			return quantity;

		if (quantity == 1) {
			int groupQuantity = productReferenceGroup.getQuantity();
			if (groupQuantity == -1) {
				if (productReferenceGroup.getSignificantProductReference().incQuantity() != 0)
					throw new IllegalStateException("ProductReferenceGroup " + productReferenceGroup.getPrimaryKey() + ": productReferenceGroup.getSignificantProductReference().incQuantity() != 0");
				productReferenceGroup.setSignificantProductReference(this);
				quantity = 0;
			}
			else if (groupQuantity == 0) {
				// nothing
			}
			else
				throw new IllegalStateException("ProductReference \"" + getPrimaryKey() + "\" cannot set quantity to " + quantity + ", because productReferenceGroup.quantity=" + groupQuantity + "!");
		}
		else if (quantity == 0) {
			// nothing
		}
		else
			throw new IllegalStateException("ProductReference \"" + getPrimaryKey() + "\" has illegal quantity!");

		productReferenceGroup.setQuantity(quantity);

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace(this.getClass().getName() + ".incQuantity: pk=\"" + getPrimaryKey() + "\" newQuantity=" + quantity, new Exception("STACKTRACE"));
			else
				logger.debug(this.getClass().getName() + ".incQuantity: pk=\"" + getPrimaryKey() + "\" newQuantity=" + quantity);
		}

		return quantity;
	}

	public int decQuantity()
	{
		quantity = quantity - 1;
		if (productReferenceGroup == null)
			return quantity;

		if (quantity == -1) {
			int groupQuantity = productReferenceGroup.getQuantity();
			if (groupQuantity == 1) {
				if (productReferenceGroup.getSignificantProductReference().decQuantity() != 0)
					throw new IllegalStateException("ProductReferenceGroup " + productReferenceGroup.getPrimaryKey() + ": productReferenceGroup.getSignificantProductReference().incQuantity() != 0");
				productReferenceGroup.setSignificantProductReference(this);
				quantity = 0;
			}
			else if (groupQuantity == 0) {
				// nothing
			}
			else
				throw new IllegalStateException("ProductReference \"" + getPrimaryKey() + "\" cannot set quantity to " + quantity + ", because productReferenceGroup.quantity=" + groupQuantity + "!");
		}
		else if (quantity == 0) {
			// nothing
		}
		else
			throw new IllegalStateException("ProductReference \"" + getPrimaryKey() + "\" has illegal quantity!");

		productReferenceGroup.setQuantity(quantity);

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace(this.getClass().getName() + ".decQuantity: pk=\"" + getPrimaryKey() + "\" newQuantity=" + quantity, new Exception("STACKTRACE"));
			else
				logger.debug(this.getClass().getName() + ".decQuantity: pk=\"" + getPrimaryKey() + "\" newQuantity=" + quantity);
		}

		return quantity;
	}

	public void jdoPreDelete()
	{
		if (productReferenceGroup != null) {
			ProductReferenceGroup tmpPRG = productReferenceGroup;
			productReferenceGroup = null; // otherwise we can't delete it.
			PersistenceManager pm  = getPersistenceManager();
			if (getProductReferenceCount(pm, tmpPRG) == 0)
				pm.deletePersistent(tmpPRG);
//			if (prs.size() == 1 && JDOHelper.getObjectId(this).equals(JDOHelper.getObjectId(prs.iterator().next())))
		}

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace(this.getClass().getName() + ".jdoPreDelete: pk=\"" + getPrimaryKey() + "\" quantity=" + quantity, new Exception("STACKTRACE"));
			else
				logger.debug(this.getClass().getName() + ".jdoPreDelete: pk=\"" + getPrimaryKey() + "\" quantity=" + quantity);
		}
	}
}
