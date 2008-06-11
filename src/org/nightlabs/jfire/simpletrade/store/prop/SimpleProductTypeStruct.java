/**
 *
 */
package org.nightlabs.jfire.simpletrade.store.prop;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropHelper;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.html.HTMLStructField;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class SimpleProductTypeStruct {

	public static IStruct getSimpleProductTypeStruct(String organisationID, PersistenceManager pm) {
		Struct productTypeStruct = null;
		StructLocal productTypeStructLocal = null;
		try {
			productTypeStruct = Struct.getStruct(organisationID, SimpleProductType.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			// person struct not persisted yet.
			productTypeStruct = new Struct(organisationID, SimpleProductType.class.getName(), Struct.DEFAULT_SCOPE);
			createStandardStructure(productTypeStruct);
			productTypeStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Simple products");
			productTypeStruct.getName().setText(Locale.GERMAN.getLanguage(), "Einfache Produkte");
			productTypeStruct = pm.makePersistent(productTypeStruct);
			productTypeStructLocal = new StructLocal(productTypeStruct, StructLocal.DEFAULT_SCOPE);
			productTypeStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default simple product structure");
			productTypeStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standard Struktur für einfache Produkte");
			pm.makePersistent(productTypeStructLocal);
		}
		return productTypeStruct;
	}


	public static void createStandardStructure(IStruct productTypeStruct) {
		try {

			StructBlock sb = PropHelper.createStructBlock(productTypeStruct, DESCRIPTION, "Description", "Beschreibung");
			sb.setUnique(false);
			I18nTextStructField descShort = PropHelper.createI18nTextField(sb, DESCRIPTION_SHORT, "Short description", "Kurzbeschreibung");
			descShort.setLineCount(5);
			I18nTextStructField descLong = PropHelper.createI18nTextField(sb, DESCRIPTION_LONG, "Long description", "Ausführliche Beschreibung");
			descLong.setLineCount(10);

			sb.addStructField(descShort);
			sb.addStructField(descLong);

			productTypeStruct.addStructBlock(sb);

			// --------

			sb = PropHelper.createStructBlock(productTypeStruct, IMAGES, "Images", "Bilder");
			sb.setUnique(false);
			ImageStructField largeImage = PropHelper.createImageField(sb, IMAGES_LARGE_IMAGE, "Large image", "Grosses Bild");
			largeImage.addImageFormat("jpg");
			largeImage.addImageFormat("png");
			largeImage.addImageFormat("gif");
			largeImage.setMaxSizeKB(1024);
			ImageStructField smallImage = PropHelper.createImageField(sb, IMAGES_SMALL_IMAGE, "Small image", "Kleines Bild");
			smallImage.addImageFormat("jpg");
			smallImage.addImageFormat("png");
			smallImage.addImageFormat("gif");
			smallImage.setMaxSizeKB(200);

			sb.addStructField(largeImage);
			sb.addStructField(smallImage);

			productTypeStruct.addStructBlock(sb);

			// --------

			sb = PropHelper.createStructBlock(productTypeStruct, XINFO, "Extended Information", "Erweiterte Informationen");
			sb.setUnique(false);
			HTMLStructField info = new HTMLStructField(sb, XINFO_INFO);
			info.getName().setText(Locale.GERMAN.getLanguage(), "Produktinformationen");
			info.getName().setText(Locale.ENGLISH.getLanguage(), "Product information");

			sb.addStructField(info);

			productTypeStruct.addStructBlock(sb);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	// *************** STANDARD StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

	public static final StructBlockID DESCRIPTION = StructBlockID.create(DEV_ORGANISATION_ID,"SimpleProductType.description");
	public static final StructFieldID DESCRIPTION_SHORT = StructFieldID.create(DESCRIPTION,"Short");
	public static final StructFieldID DESCRIPTION_LONG = StructFieldID.create(DESCRIPTION,"Long");

	public static final StructBlockID IMAGES = StructBlockID.create(DEV_ORGANISATION_ID,"SimpleProductType.images");
	public static final StructFieldID IMAGES_SMALL_IMAGE = StructFieldID.create(IMAGES,"SmallImage");
	public static final StructFieldID IMAGES_LARGE_IMAGE = StructFieldID.create(IMAGES,"LargeImage");

	public static final StructBlockID XINFO = StructBlockID.create(DEV_ORGANISATION_ID, "SimpleProductType.xinfo");
	public static final StructFieldID XINFO_INFO = StructFieldID.create(XINFO, "Info");

}
