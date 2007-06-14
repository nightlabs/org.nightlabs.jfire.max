package org.nightlabs.jfire.chezfrancois;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.VoucherTypeActionHandler;

public class DataCreatorVoucher
		extends DataCreator
{
	private VoucherType rootVoucherType;

	public DataCreatorVoucher(PersistenceManager pm, User user)
	{
		super(pm, user);

		pm.getExtent(VoucherType.class);
		rootVoucherType = (VoucherType) pm.getObjectById(
				ProductTypeID.create(organisationID, VoucherType.class.getName()));
	}

	public VoucherType getRootVoucherType()
	{
		return rootVoucherType;
	}

	public VoucherType createCategory(
			VoucherType parent, String productTypeID, VoucherLocalAccountantDelegate localAccountantDelegate,
			IPackagePriceConfig packagePriceConfig, String ... names)
	{
		if (parent == null)
			parent = rootVoucherType;

		try {
			return (VoucherType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		VoucherType pt = new VoucherType(
				organisationID, productTypeID, parent, null, 
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);

		if (packagePriceConfig != null) {
			pt.getFieldMetaData("packagePriceConfig").setValueInherited(false);
			pt.setPackagePriceConfig(packagePriceConfig);
		}
		else {
			pt.setPackagePriceConfig(parent.getPackagePriceConfig());
		}

		if (localAccountantDelegate != null) {
			pt.getFieldMetaData("localAccountantDelegate").setValueInherited(false);
			pt.setLocalAccountantDelegate(localAccountantDelegate);
		}
		else
			pt.setLocalAccountantDelegate(parent.getLocalAccountantDelegate());

		store.addProductType(user, pt, VoucherTypeActionHandler.getDefaultHome(pm, pt));
		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	private List<VoucherType> createdLeafs = new ArrayList<VoucherType>();

	public VoucherType createLeaf(VoucherType category, String productTypeID,
			IPackagePriceConfig packagePriceConfig, VoucherLocalAccountantDelegate localAccountantDelegate,
			String ... names)
	{
		if (category == null)
			category = rootVoucherType;

		VoucherType pt = new VoucherType(
				organisationID, productTypeID, category, null, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);
		if (packagePriceConfig != null) {
			pt.getFieldMetaData("packagePriceConfig").setValueInherited(false);
			pt.setPackagePriceConfig(packagePriceConfig);
		}
		else
			pt.setPackagePriceConfig(category.getPackagePriceConfig());

		if (localAccountantDelegate != null) {
			pt.getFieldMetaData("localAccountantDelegate").setValueInherited(false);
			pt.setLocalAccountantDelegate(localAccountantDelegate);
		}
		else
			pt.setLocalAccountantDelegate(category.getLocalAccountantDelegate());

		store.addProductType(user, pt, VoucherTypeActionHandler.getDefaultHome(pm, pt));

		store.setProductTypeStatus_published(user, pt);
		store.setProductTypeStatus_confirmed(user, pt);
//		store.setProductTypeStatus_saleable(user, pt, true);		

		createdLeafs.add(pt);

		return pt;
	}
	
	public void makeAllLeafsSaleable() {
		for (ProductType pt : createdLeafs) {
			store.setProductTypeStatus_saleable(user, pt, true);
		}
	}
}
