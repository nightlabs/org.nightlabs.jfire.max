/**
 *
 */
package org.nightlabs.jfire.dynamictrade.prop;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DynamicProductTypeStruct {

	public static IStruct getSimpleProductTypeStructLocal(PersistenceManager pm) {
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;

		Struct productTypeStruct = null;
		StructLocal productTypeStructLocal = null;
		try {
			productTypeStruct = Struct.getStruct(devOrganisationID, DynamicProductType.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			// person struct not persisted yet.
			productTypeStruct = new Struct(devOrganisationID, DynamicProductType.class.getName(), Struct.DEFAULT_SCOPE);
			createDefaultStructure(productTypeStruct);
			productTypeStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Dynamic products");
			productTypeStruct.getName().setText(Locale.GERMAN.getLanguage(), "Dynamische Produkte");
			productTypeStruct = pm.makePersistent(productTypeStruct);
		}

		try {
			productTypeStructLocal = StructLocal.getStructLocal(pm, devOrganisationID, DynamicProductType.class, productTypeStruct.getStructScope(), StructLocal.DEFAULT_SCOPE);
		} catch (JDOObjectNotFoundException e) {
			productTypeStructLocal = new StructLocal(productTypeStruct, StructLocal.DEFAULT_SCOPE);
			productTypeStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default dynamic product structure");
			productTypeStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für dynamiche Produkte");
			productTypeStructLocal = pm.makePersistent(productTypeStructLocal);
		}
		return productTypeStructLocal;
	}

	private static void createDefaultStructure(IStruct productTypeStruct) {
		try {
			// Nothing done for DynamicProductTypes so far
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
