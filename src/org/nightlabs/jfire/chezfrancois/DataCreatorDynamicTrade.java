package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.PackagePriceConfig;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductTypeActionHandler;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

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

	public DynamicProductType createCategory(DynamicProductType parent, String productTypeID, String ... names)
	{
		if (parent == null)
			parent = rootDynamicProductType;

		try {
			return (DynamicProductType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		DynamicProductType pt = new DynamicProductType(
				organisationID, productTypeID, parent, null, 
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);

		store.addProductType(user, pt, DynamicProductTypeActionHandler.getDefaultHome(pm, pt));
		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	private List<DynamicProductType> createdLeafs = new ArrayList<DynamicProductType>();

	public DynamicProductType createLeaf(DynamicProductType category, String productTypeID,
//			IInnerPriceConfig innerPriceConfig,
			String ... names)
	{
		if (category == null)
			category = rootDynamicProductType;

		DynamicProductType pt = new DynamicProductType(
				organisationID, productTypeID, category, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);
		pt.setPackagePriceConfig(new PackagePriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class)));
//		pt.getFieldMetaData("innerPriceConfig").setValueInherited(false);
//		pt.setInnerPriceConfig(innerPriceConfig);
		store.addProductType(user, pt, DynamicProductTypeActionHandler.getDefaultHome(pm, pt));

		store.setProductTypeStatus_published(user, pt);
		store.setProductTypeStatus_confirmed(user, pt);
		store.setProductTypeStatus_saleable(user, pt, true);		

		createdLeafs.add(pt);

		return pt;
	}

}
