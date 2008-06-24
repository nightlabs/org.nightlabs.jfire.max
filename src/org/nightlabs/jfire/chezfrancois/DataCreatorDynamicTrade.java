package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.PackagePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;

public class DataCreatorDynamicTrade
extends DataCreator
{
	private DynamicProductType rootDynamicProductType;

	public DataCreatorDynamicTrade(PersistenceManager pm, User user)
	{
		super(pm, user);

		pm.getExtent(DynamicProductType.class);
		rootDynamicProductType = (DynamicProductType) pm.getObjectById(
				ProductTypeID.create(organisationID, DynamicProductType.class.getName()));
	}

	public DynamicProductType getRootDynamicProductType()
	{
		return rootDynamicProductType;
	}

	public DynamicProductType createCategory(DynamicProductType parent, String productTypeID,
			IInnerPriceConfig innerPriceConfig, String ... names) throws CannotPublishProductTypeException
	{
		if (parent == null)
			parent = rootDynamicProductType;

		try {
			return (DynamicProductType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		DynamicProductType pt = new DynamicProductType(
				organisationID, productTypeID, parent,
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);

		if (innerPriceConfig != null) {
			pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).setValueInherited(false);
			pt.setInnerPriceConfig(innerPriceConfig);
		}
		else {
			pt.setInnerPriceConfig(parent.getInnerPriceConfig());
		}

		pt = (DynamicProductType) store.addProductType(user, pt); // , DynamicProductTypeActionHandler.getDefaultHome(pm, pt));
		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	private List<DynamicProductType> createdLeafs = new ArrayList<DynamicProductType>();

	public DynamicProductType createLeaf(DynamicProductType category, String productTypeID,
			IInnerPriceConfig innerPriceConfig, LegalEntity vendor,
			String ... names) throws CannotPublishProductTypeException, CannotConfirmProductTypeException
	{
		if (category == null)
			category = rootDynamicProductType;

		DynamicProductType pt = new DynamicProductType(
				organisationID, productTypeID, category, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);
		pt.setPackagePriceConfig(new PackagePriceConfig(IDGenerator.getOrganisationID(), PriceConfig.createPriceConfigID()));
		if (innerPriceConfig != null) {
			pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).setValueInherited(false);
			pt.setInnerPriceConfig(innerPriceConfig);
		}
		else {
			pt.setInnerPriceConfig(category.getInnerPriceConfig());
		}
		
		if (vendor != null) {
			pt.setVendor(vendor);
			pt.setOwner(vendor);
			pt.getFieldMetaData(ProductType.FieldName.vendor).setValueInherited(false);
			pt.getFieldMetaData(ProductType.FieldName.owner).setValueInherited(false);
		}
		
		pt = (DynamicProductType) store.addProductType(user, pt); // , DynamicProductTypeActionHandler.getDefaultHome(pm, pt));
		store.setProductTypeStatus_published(user, pt);
		store.setProductTypeStatus_confirmed(user, pt);

		createdLeafs.add(pt);

		return pt;
	}

	public void makeAllLeavesSaleable() throws CannotMakeProductTypeSaleableException {
		for (ProductType pt : createdLeafs) {
			ProductType productType = (ProductType) pm.getObjectById(JDOHelper.getObjectId(pt));
			store.setProductTypeStatus_saleable(user, productType, true);
		}
	}
}
