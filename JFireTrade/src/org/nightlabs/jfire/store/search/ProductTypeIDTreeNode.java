package org.nightlabs.jfire.store.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

public class ProductTypeIDTreeNode
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static void addProductTypeIDToTree(
			PersistenceManager pm,
			List<ProductTypeIDTreeNode> roots,
			Map<ProductTypeID,
			ProductTypeIDTreeNode> productTypeID2node,
			ProductTypeID productTypeID,
			boolean match
	)
	{
		ProductTypeIDTreeNode node = productTypeID2node.get(productTypeID);

		// check for duplicates - besides duplicates in input-data, this might happen due to recursion.
		if (node == null) {
			ProductType productType = (ProductType) pm.getObjectById(productTypeID);
			ProductTypeID extendedProductTypeID = productType.getExtendedProductTypeID();
			if (extendedProductTypeID == null) {
				// this is a root element
				node = new ProductTypeIDTreeNode(null, productTypeID);
				roots.add(node);
			}
			else {
				addProductTypeIDToTree(pm, roots, productTypeID2node, extendedProductTypeID, false);
				ProductTypeIDTreeNode parentNode = productTypeID2node.get(extendedProductTypeID);
				if (parentNode == null)
					throw new IllegalStateException("productTypeID2node.get(extendedProductTypeID) returned null for " + extendedProductTypeID);

				node = new ProductTypeIDTreeNode(parentNode, productTypeID);
			}
			productTypeID2node.put(productTypeID, node);
		}

		if (match)
			node.setMatch(true);
	}

	public static List<ProductTypeIDTreeNode> buildTree(
			PersistenceManager pm,
			Collection<ProductTypeID> productTypeIDs
	)
	{
		Map<ProductTypeID, ProductTypeIDTreeNode> productTypeID2node = new HashMap<ProductTypeID, ProductTypeIDTreeNode>();
		List<ProductTypeIDTreeNode> roots = new ArrayList<ProductTypeIDTreeNode>();

		for (ProductTypeID productTypeID : productTypeIDs)
			addProductTypeIDToTree(pm, roots, productTypeID2node, productTypeID, true);

		return roots;
	}

	private ProductTypeIDTreeNode parent;
	private ProductTypeID productTypeID;
	private boolean match = false;

	private List<ProductTypeIDTreeNode> children = new ArrayList<ProductTypeIDTreeNode>();

	public ProductTypeIDTreeNode(ProductTypeIDTreeNode parent, ProductTypeID productTypeID) {
		this.parent = parent;
		if (parent != null)
			parent.children.add(this);

		this.productTypeID = productTypeID;
	}

	public ProductTypeIDTreeNode getParent() {
		return parent;
	}

	public List<ProductTypeIDTreeNode> getChildren() {
		return children;
	}

	public ProductTypeID getProductTypeID() {
		return productTypeID;
	}

	public boolean isMatch() {
		return match;
	}

	protected void setMatch(boolean match) {
		this.match = match;
	}
}
