/**
 *
 */
package org.nightlabs.jfire.department;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * This {@link DepartmentStruct} class provides methods for creating {@link Struct} of {@link Department}.
 *
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class DepartmentStruct 
{
	public static IStruct getDepartmentStruct(PersistenceManager pm) {
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;

		Struct departmentStruct = null;
		StructLocal departmentStructLocal = null;
		try {
			departmentStruct = Struct.getStruct(devOrganisationID, Department.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			departmentStruct = new Struct(devOrganisationID, Department.class.getName(), Struct.DEFAULT_SCOPE);
//			createDefaultStructure(departmentStruct);
			departmentStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Departments");
			departmentStruct.getName().setText(Locale.GERMAN.getLanguage(), "Departments");
			departmentStruct = pm.makePersistent(departmentStruct);
			departmentStructLocal = new StructLocal(departmentStruct, StructLocal.DEFAULT_SCOPE);
			departmentStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Department Structure");
			departmentStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für Departments");
			departmentStructLocal = pm.makePersistent(departmentStructLocal);
		}
		return departmentStruct;
	}

	private static void createDefaultStructure(IStruct departmentStruct) {
		try {
//			StructBlock sb = PropHelper.createStructBlock(departmentStruct, DESCRIPTION, "Description", "Beschreibung");
//			sb.setUnique(false);
//			I18nTextStructField descShort = PropHelper.createI18nTextField(sb, DESCRIPTION_SHORT, "Short description", "Kurzbeschreibung");
//			descShort.setLineCount(1);
//			I18nTextStructField descLong = PropHelper.createI18nTextField(sb, DESCRIPTION_LONG, "Long description", "Ausführliche Beschreibung");
//			descLong.setLineCount(10);
//
//			sb.addStructField(descShort);
//			sb.addStructField(descLong);
//
//			departmentStruct.addStructBlock(sb);
//
//			// --------
//
//			sb = PropHelper.createStructBlock(departmentStruct, SUBJECT, "Subject", "Bilder");
//			sb.setUnique(false);
//			I18nTextStructField subject = PropHelper.createI18nTextField(sb, SUBJECT_SUBJECT, "Subject", "Lehrfach");
//			subject.setLineCount(1);
//
//			sb.addStructField(subject);
//
//			departmentStruct.addStructBlock(sb);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	// *************** DEFAULT StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

//	public static final StructBlockID DESCRIPTION = StructBlockID.create(DEV_ORGANISATION_ID,"Department.description");
//	public static final StructFieldID DESCRIPTION_SHORT = StructFieldID.create(DESCRIPTION,"Short");
//	public static final StructFieldID DESCRIPTION_LONG = StructFieldID.create(DESCRIPTION,"Long");
//
//	public static final StructBlockID SUBJECT = StructBlockID.create(DEV_ORGANISATION_ID,"Department.subject");
//	public static final StructFieldID SUBJECT_SUBJECT = StructFieldID.create(SUBJECT,"Subject");
}
