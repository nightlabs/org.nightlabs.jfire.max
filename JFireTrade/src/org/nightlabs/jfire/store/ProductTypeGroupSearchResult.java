/**
 * 
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductTypeGroupSearchResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public class Entry 
	implements Serializable 
	{
		private static final long serialVersionUID = 1L;
		
		private ProductTypeGroup productTypeGroup;
		private List<ProductType> productTypes = new LinkedList<ProductType>();
		private Map<ProductTypeID, ProductType> productTypeID2ProductType = new HashMap<ProductTypeID, ProductType>();
				
		public Entry(ProductTypeGroup productTypeGroup) {
			this.productTypeGroup = productTypeGroup;
		}
		
		public void addProductType(ProductType productType) {
			productTypes.add(productType);
			ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
			productTypeID2ProductType.put(productTypeID, productType);
			ProductTypeGroupSearchResult.this.productTypeID2ProductType.put(productTypeID, productType);
		}
		
		public List<ProductType> getProductTypes() {
			return productTypes;
		}
		
		public ProductTypeGroup getProductTypeGroup() {
			return productTypeGroup;
		}
		
		public ProductType getProductType(ProductTypeID productTypeID) {
			return productTypeID2ProductType.get(productTypeID);
		}
	}
	
	private Map<ProductTypeGroup, Entry> group2Entry = new HashMap<ProductTypeGroup, Entry>();
	private Map<ProductTypeGroupID, Entry> groupID2Entry = new HashMap<ProductTypeGroupID, Entry>();
	private Map<ProductTypeID, ProductType> productTypeID2ProductType = new HashMap<ProductTypeID, ProductType>();
	
	public ProductTypeGroupSearchResult() {
		super();
	}
	
	public Collection<Entry> getEntries() {
		return group2Entry.values();
	}
	
	public Set<ProductTypeGroup> getProductTypesGroups() {
		return group2Entry.keySet();
	}
	
	public Entry getEntry(ProductTypeGroup productTypeGroup) {
		return group2Entry.get(productTypeGroup);
	}

	public Entry getEntry(ProductTypeGroupID productTypeGroup) {
		return groupID2Entry.get(productTypeGroup);
	}
		
	public Entry addEntry(ProductTypeGroup productTypeGroup) {
		Entry entry = new Entry(productTypeGroup);
		group2Entry.put(productTypeGroup, entry);
		groupID2Entry.put((ProductTypeGroupID)JDOHelper.getObjectId(productTypeGroup), entry);
		return entry;
	}
	
	public int getSize() {
		return group2Entry.size();
	}
	
	public Collection<ProductType> getAllProductTypes() {
		return productTypeID2ProductType.values();
	}

	public Set<ProductTypeID> getAllProductTypeIDs() {
		return productTypeID2ProductType.keySet();
	}
	
	public ProductType getProductType(ProductTypeID productTypeID) {
		return productTypeID2ProductType.get(productTypeID);
	}
	
//	// TODO: don't iterate each time but build up when corresponding add-methods are called
//	public Set<ProductType> getAllProductTypes() {
//		Set<ProductType> productTypes = new HashSet<ProductType>();
//		for (Map.Entry<ProductTypeGroup, Entry> mapEntry : group2Entry.entrySet()) {
//			productTypes.addAll(mapEntry.getValue().getProductTypes());
//		}
//		return productTypes;
//	}
//	
//	// TODO: don't iterate each time but hold global method which is populates when addProductType is called for node	
//	public ProductType getProductType(ProductTypeID productTypeID) {
//		for (Map.Entry<ProductTypeGroup, Entry> mapEntry : group2Entry.entrySet()) {
//			ProductType productType = mapEntry.getValue().getProductType(productTypeID);
//			if (productType != null)
//				return productType;
//		}
//		return null;
//	}
}
