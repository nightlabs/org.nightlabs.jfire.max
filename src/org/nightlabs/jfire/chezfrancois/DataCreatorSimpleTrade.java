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

package org.nightlabs.jfire.chezfrancois;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaCell;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator;
import org.nightlabs.jfire.accounting.gridpriceconfig.StablePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.CustomerGroupMapper;
import org.nightlabs.jfire.trade.LegalEntity;

public class DataCreatorSimpleTrade
extends DataCreator
{
	private static Logger logger = Logger.getLogger(DataCreatorSimpleTrade.class);

	private SimpleProductType rootSimpleProductType;

	public DataCreatorSimpleTrade(PersistenceManager pm, User user)
	{
		super(pm, user);

		pm.getExtent(SimpleProductType.class);
		rootSimpleProductType = (SimpleProductType) pm.getObjectById(
				ProductTypeID.create(organisationID, SimpleProductType.class.getName()));
	}

	public SimpleProductType createCategory(SimpleProductType parent, String productTypeID, String ... names) throws CannotPublishProductTypeException
	{
		if (parent == null)
			parent = rootSimpleProductType;

		try {
			return (SimpleProductType) pm.getObjectById(ProductTypeID.create(organisationID, productTypeID));
		} catch (JDOObjectNotFoundException x) {
			// not yet existent => create the object!
		}

		SimpleProductType pt = new SimpleProductType(
				organisationID, productTypeID, parent, 
				ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);

		pt = (SimpleProductType) store.addProductType(user, pt);
		store.setProductTypeStatus_published(user, pt);

		return pt;
	}

	private List<SimpleProductType> createdLeafs = new ArrayList<SimpleProductType>(); 

	public SimpleProductType createLeaf(SimpleProductType category, String productTypeID, IInnerPriceConfig innerPriceConfig, String ... names) throws CannotPublishProductTypeException, CannotConfirmProductTypeException
	{
		if (category == null)
			category = rootSimpleProductType;

		SimpleProductType pt = new SimpleProductType(
				organisationID, productTypeID, category, ProductType.INHERITANCE_NATURE_LEAF, ProductType.PACKAGE_NATURE_OUTER);
		setNames(pt.getName(), names);
		pt.setPackagePriceConfig(new StablePriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class)));
		pt.getFieldMetaData("innerPriceConfig").setValueInherited(false);
//		pt.getFieldMetaData("localAccountantDelegate").setValueInherited(false); // TODO this should be a field of ProductTypeLocal - not ProductType!
//		pt.getFieldMetaData("localStorekeeperDelegate").setValueInherited(false); // TODO this should be a field of ProductTypeLocal - not ProductType!
		pt.setInnerPriceConfig(innerPriceConfig);
		pt = (SimpleProductType) store.addProductType(user, pt);

		store.setProductTypeStatus_published(user, pt);
//		store.setProductTypeStatus_confirmed(user, pt);
//		store.setProductTypeStatus_saleable(user, pt, true);		
		
//		createdLeafs.add((ProductTypeID) JDOHelper.getObjectId(pt));
		createdLeafs.add(pt);

		return pt;
	}
	
	public void makeAllLeavesSaleable()
	throws CannotMakeProductTypeSaleableException, CannotConfirmProductTypeException
	{
		for (ProductType pt : createdLeafs) {
			store.setProductTypeStatus_confirmed(user, pt);
			store.setProductTypeStatus_saleable(user, pt, true);
		}
	}
	
	public void createWineProperties(PersistenceManager pm, SimpleProductType productType, String englishShort, String germanShort, String englishLong, String germanLong, String smallImage, String smallImageContentType, String largeImage, String largeImageContentType) {
		SimpleProductTypeStruct.getSimpleProductTypeStruct(productType.getOrganisationID(), pm);
		IStruct struct = StructLocal.getStructLocal(SimpleProductType.class, StructLocal.DEFAULT_SCOPE, pm);
		PropertySet props = productType.getPropertySet();
		productType.getFieldMetaData("propertySet").setValueInherited(false);
		pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_DATA_FIELDS, PropertySet.FETCH_GROUP_FULL_DATA});
		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		// PropertySet should always be detached before exploding! 
		// never explode while being attached! 
		// I got an SQL error because this line was commented out! Marco.
		// Why not explode attached, when intending to set all properties ;-)
		if (JDOHelper.isPersistent(props))
			props = (PropertySet) pm.detachCopy(props);
		
		struct.explodePropertySet(props);
		I18nTextDataField shortDesc;
		try {
			shortDesc = (I18nTextDataField)props.getDataField(SimpleProductTypeStruct.DESCRIPTION_SHORT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}  
		shortDesc.getI18nText().setText(Locale.ENGLISH.getLanguage(), englishShort);
		shortDesc.getI18nText().setText(Locale.GERMAN.getLanguage(), germanShort);
		I18nTextDataField longDesc;
		try {
			longDesc = (I18nTextDataField)props.getDataField(SimpleProductTypeStruct.DESCRIPTION_LONG);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}  
		longDesc.getI18nText().setText(Locale.ENGLISH.getLanguage(), englishLong);
		longDesc.getI18nText().setText(Locale.GERMAN.getLanguage(), germanLong);
		ImageDataField smallImg;
		try {
			smallImg = (ImageDataField)props.getDataField(SimpleProductTypeStruct.IMAGES_SMALL_IMAGE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		InputStream in = getClass().getResourceAsStream("resource/"+smallImage);
		if (in != null) {
			try {
				smallImg.loadStream(in, smallImage, smallImageContentType);
			} catch (IOException e) {
				logger.error(e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		ImageDataField largeImg;
		try {
			largeImg = (ImageDataField)props.getDataField(SimpleProductTypeStruct.IMAGES_LARGE_IMAGE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		in = getClass().getResourceAsStream("resource/"+largeImage);
		if (in != null) {
			try {
				largeImg.loadStream(in, largeImage, largeImageContentType);
			} catch (IOException e) {
				logger.error(e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}		
		struct.implodePropertySet(props); // and it should always be imploded before storing it into the datastore. Marco.

		// TODO JPOX WORKAROUND : this fails sometimes - hence we retry a few times
		for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
			try {
				pm.makePersistent(props);
				props = null;
				break; // successful => break retry loop
			} catch (Exception x) {
				// ignore and try again
			}
			pm.flush();
			pm.evictAll();
		}
		if (props != null)
			pm.makePersistent(props);
	}

	public void calculatePrices()
	throws ModuleException
	{
//		for (ProductTypeID ptID : createdLeafs) {
//			SimpleProductType pt = (SimpleProductType) pm.getObjectById(ptID);
		for (SimpleProductType pt : createdLeafs){
			if (pt.getInnerPriceConfig() != null && pt.getPackagePriceConfig() != null)
				((StablePriceConfig)pt.getPackagePriceConfig()).adoptParameters(pt.getInnerPriceConfig());

			PriceCalculator priceCalculator = new PriceCalculator(pt, new CustomerGroupMapper(pm), new TariffMapper(pm));
			priceCalculator.preparePriceCalculation();
			priceCalculator.calculatePrices();
		}
	}

	public IInnerPriceConfig createInnerPercentagePriceConfig(String name, int percentage, ProductType packageProductType)
	{
		FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class));
//		PriceFragmentType vatNet = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-19-net"));
//		PriceFragmentType vatVal = (PriceFragmentType) pm.getObjectById(PriceFragmentTypeID.create(rootOrganisationID, "vat-de-19-val"));
		PriceFragmentType vatNet = getPriceFragmentTypeVatNet();
		PriceFragmentType vatVal = getPriceFragmentTypeVatVal();
		formulaPriceConfig.addProductType(packageProductType);
		formulaPriceConfig.addPriceFragmentType(vatVal);
		formulaPriceConfig.addPriceFragmentType(vatNet);
		formulaPriceConfig.getName().setText(languageID, name);
		FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
		fallbackFormulaCell.setFormula(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID,
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		ProductTypeID.create(\"" + packageProductType.getOrganisationID() + "\", \"" + packageProductType.getProductTypeID() + "\")\n" +
				"	)\n" +
				") * 0.1;");
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+packageProductType.getPrimaryKey()+"\",\n" +
//				"		null\n" +
//				"	)\n" +
//				") * 0.1;");

		fallbackFormulaCell.setFormula(vatNet,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID + "\", \"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID + "\")\n" +
				"	)\n" +
				") / 1.19;");
//				"cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//				"	)\n"+
//				") / 1.19;");
		fallbackFormulaCell.setFormula(vatVal,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID + "\", \"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID + "\")\n" +
				"	)\n" +
				")\n" +
				"\n" +
				"-\n" +
				"\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + rootOrganisationID + "\", \"vat-de-19-net\")\n" +
				"	)\n" +
				");");
//				"cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//				"	)\n"+
//				")\n" +
//				"\n" +
//				"-\n" +
//				"\n" +
//				"cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+rootOrganisationID+"/vat-de-19-net\"\n" +
//				"	)\n"+
//				");");

		return formulaPriceConfig;
	}

	public IInnerPriceConfig createFixPriceConfig(Tariff[] tariffs, long[] prices, String ... names)
	{
		String[] formulas = new String[prices.length];
		for (int i = 0; i < prices.length; i++) {
			long price = prices[i];
			formulas[i] = String.valueOf(price);
		}
		return createFormulaPriceConfig(tariffs, formulas, names);
	}

	public IInnerPriceConfig createFormulaPriceConfig(Tariff[] tariffs, String[] formulas, String ... names)
	{
		Currency euro = getCurrencyEUR();

//	 create the price config "Car - Middle Class"
		PriceFragmentType totalPriceFragmentType = getPriceFragmentTypeTotal();
		PriceFragmentType vatNet = getPriceFragmentTypeVatNet();
		PriceFragmentType vatVal = getPriceFragmentTypeVatVal();

		StablePriceConfig stablePriceConfig = new StablePriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class));
		FormulaPriceConfig formulaPriceConfig = new FormulaPriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class));
		setNames(formulaPriceConfig.getName(), names);
		
		CustomerGroup customerGroupDefault = trader.getDefaultCustomerGroupForKnownCustomer();
		CustomerGroup customerGroupAnonymous = LegalEntity.getAnonymousCustomer(pm).getDefaultCustomerGroup();
		formulaPriceConfig.addCustomerGroup(customerGroupDefault);
		formulaPriceConfig.addCustomerGroup(customerGroupAnonymous);
		formulaPriceConfig.addCurrency(euro);
		for (int i = 0; i < tariffs.length; i++) {
			Tariff tariff = tariffs[i];
			formulaPriceConfig.addTariff(tariff);			
		}
//			formulaPriceConfig.addProductType(rootSimpleProductType);
		formulaPriceConfig.addPriceFragmentType(totalPriceFragmentType);
		formulaPriceConfig.addPriceFragmentType(vatNet);
		formulaPriceConfig.addPriceFragmentType(vatVal);
		stablePriceConfig.adoptParameters(formulaPriceConfig);

		FormulaCell fallbackFormulaCell = formulaPriceConfig.createFallbackFormulaCell();
		fallbackFormulaCell.setFormula(totalPriceFragmentType,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		CustomerGroupID.create(\"" + organisationID + "\", \"" + CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT + "\")\n" +
				"	)\n" +
				");");
//				"cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		\""+organisationID+"/"+CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT+"\",\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null\n" +
//				"	)\n" +
//				");");
		fallbackFormulaCell.setFormula(vatNet,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID + "\", \"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID + "\")\n" +
				"	)\n" +
				") / 1.19;");
		fallbackFormulaCell.setFormula(vatVal,
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID + "\", \"" + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID + "\")\n" +
				"	)\n" +
				")\n" +
				"\n" +
				"-\n" +
				"\n" +
				"cell.resolvePriceCellsAmount(\n" +
				"	new Array(\n" +
				"		PriceFragmentTypeID.create(\"" + rootOrganisationID + "\", \"vat-de-19-net\")\n" +
				"	)\n" +
				");");
//		fallbackFormulaCell.setFormula(vatNet, "cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//				"	)\n" +
//				") / 1.19;");
//		fallbackFormulaCell.setFormula(vatVal, "cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+Organisation.DEVIL_ORGANISATION_ID+"/_Total_\"\n" +
//				"	)\n" +
//				")\n" +
//
////					"/ 1.19 * 0.19");
//
//				"\n" +
//				"-\n" +
//				"\n" +
//				"cell.resolvePriceCellsAmount(\n" +
//				"	new AbsolutePriceCoordinate(\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		null,\n" +
//				"		\""+rootOrganisationID+"/vat-de-19-net\"\n" +
//				"	)\n" +
//				");");

		for (int i = 0; i < formulas.length; i++) {
			String formula = formulas[i];
			Tariff tariff = tariffs[i];
		
			FormulaCell cell = formulaPriceConfig.createFormulaCell(customerGroupDefault, tariff, euro);
			cell.setFormula(totalPriceFragmentType, formula);
		}

		return formulaPriceConfig;
	}

}
