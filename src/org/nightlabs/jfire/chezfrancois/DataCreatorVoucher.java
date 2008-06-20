package org.nightlabs.jfire.chezfrancois;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.editor2d.Layer;
import org.nightlabs.i18n.unit.resolution.IResolutionUnit;
import org.nightlabs.i18n.unit.resolution.ResolutionImpl;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.ScriptEditor2DFactory;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.TextScriptDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.impl.ScriptEditor2DFactoryImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.editor2d.iofilter.VoucherXStreamFilter;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.scripting.VoucherScriptingConstants;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.print.page.A4Page;
import org.nightlabs.print.page.PredefinedPageUtil;

public class DataCreatorVoucher
		extends DataCreator
{
	private VoucherType rootVoucherType;
	private List<VoucherType> createdLeafs = new ArrayList<VoucherType>();
	
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
			IPackagePriceConfig packagePriceConfig, String ... names) throws CannotPublishProductTypeException
	{
		if (parent == null)
			parent = rootVoucherType;

		try {
			return (VoucherType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		VoucherType pt = new VoucherType(
				organisationID, productTypeID, parent,
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);

		if (packagePriceConfig != null) {
			pt.getFieldMetaData(ProductType.FieldName.packagePriceConfig).setValueInherited(false);
			pt.setPackagePriceConfig(packagePriceConfig);
		}
		else {
			pt.setPackagePriceConfig(parent.getPackagePriceConfig());
		}

		pt = (VoucherType) store.addProductType(user, pt);

		if (localAccountantDelegate != null) {
			pt.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
			pt.getProductTypeLocal().setLocalAccountantDelegate(localAccountantDelegate);
		}
		else
			pt.getProductTypeLocal().setLocalAccountantDelegate(parent.getProductTypeLocal().getLocalAccountantDelegate());

		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	public VoucherType createLeaf(VoucherType category, String productTypeID,
			IPackagePriceConfig packagePriceConfig, VoucherLocalAccountantDelegate localAccountantDelegate,
			String ... names) throws CannotPublishProductTypeException, CannotConfirmProductTypeException
	{
		if (category == null)
			category = rootVoucherType;

		VoucherType pt = new VoucherType(
				organisationID, productTypeID, category, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);
		if (packagePriceConfig != null) {
			pt.getFieldMetaData(ProductType.FieldName.packagePriceConfig).setValueInherited(false);
			pt.setPackagePriceConfig(packagePriceConfig);
		}
		else
			pt.setPackagePriceConfig(category.getPackagePriceConfig());

		pt = (VoucherType) store.addProductType(user, pt);

		if (localAccountantDelegate != null) {
			pt.getProductTypeLocal().getFieldMetaData(ProductTypeLocal.FieldName.localAccountantDelegate).setValueInherited(false);
			pt.getProductTypeLocal().setLocalAccountantDelegate(localAccountantDelegate);
		}
		else
			pt.getProductTypeLocal().setLocalAccountantDelegate(category.getProductTypeLocal().getLocalAccountantDelegate());


		store.setProductTypeStatus_published(user, pt);
		store.setProductTypeStatus_confirmed(user, pt);
//		store.setProductTypeStatus_saleable(user, pt, true);

		createdLeafs.add(pt);

		return pt;
	}
	
	public void makeAllLeafsSaleable() throws CannotMakeProductTypeSaleableException {
		for (ProductType pt : createdLeafs) {
			store.setProductTypeStatus_saleable(user, pt, true);
		}
	}
	
	public VoucherLayout createVoucherLayout() 
	{
		ScriptRootDrawComponent root = null;
		// generate demo voucherlayout 
		if (root == null) {
			// create a ScriptRootDrawComponent
			ScriptEditor2DFactory factory = new ScriptEditor2DFactoryImpl();
			root = factory.createScriptRootDrawComponent(true);		
			root.setResolution(new ResolutionImpl(IResolutionUnit.dpiUnit, 300));
			Rectangle pageBounds = PredefinedPageUtil.getPageBounds(
					root.getModelUnit(), new A4Page());
			root.getCurrentPage().setPageBounds(pageBounds);
			Layer layer = root.getCurrentLayer();
			
			String fontName = "Bitstream Vera Sans";
			
			TextScriptDrawComponent scriptText = factory.createTextScriptDrawComponent("Name", fontName, 24, Font.BOLD,
					100, 100, layer);
			scriptText.setScriptRegistryItemID(VoucherScriptingConstants.OID.SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_NAME);
			layer.addDrawComponent(scriptText);
			
			TextScriptDrawComponent priceText = factory.createTextScriptDrawComponent("Price", fontName, 18, Font.PLAIN,
					100, 500, layer);
			priceText.setScriptRegistryItemID(VoucherScriptingConstants.OID.SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_PRICE);
			layer.addDrawComponent(scriptText);

			BarcodeDrawComponent barcode = factory.createBarcode(
					BarcodeDrawComponent.Type.TYPE_128, "temp", 1000, 100,
					BarcodeDrawComponent.WidthScale.SCALE_3, 300, BarcodeDrawComponent.Orientation.HORIZONTAL, true, layer,
					VoucherScriptingConstants.OID.SCRIPT_REGISTRY_ITEM_ID_SCRIPT_VOUCHER_KEY);
			layer.addDrawComponent(barcode);
		}
		
		try {
			// encode it
			File f = File.createTempFile("tmp.JFireChezFrancois.", "."+VoucherXStreamFilter.FILE_EXTENSION);
			FileOutputStream out = new FileOutputStream(f);
			try {
				new VoucherXStreamFilter().write(root, out);
			} finally {
				out.close();
			}
			VoucherLayout voucherLayout = new VoucherLayout(IDGenerator.getOrganisationID(), IDGenerator.nextID(VoucherLayout.class));
			voucherLayout.loadFile(f);
			return voucherLayout;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
