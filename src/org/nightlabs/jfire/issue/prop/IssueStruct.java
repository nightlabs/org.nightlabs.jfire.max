/**
 *
 */
package org.nightlabs.jfire.issue.prop;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueStruct {

	public static IStruct getIssueStruct(String organisationID, PersistenceManager pm) {
		Struct issueStruct = null;
		StructLocal issueStructLocal = null;
		try {
			issueStruct = Struct.getStruct(organisationID, Issue.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			issueStruct = new Struct(organisationID, Issue.class.getName(), Struct.DEFAULT_SCOPE);
//			createDefaultStructure(issueStruct);
			issueStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Issues");
			issueStruct.getName().setText(Locale.GERMAN.getLanguage(), "Abkömmlinge");
			issueStruct = pm.makePersistent(issueStruct);
			issueStructLocal = new StructLocal(issueStruct, organisationID, StructLocal.DEFAULT_SCOPE);
			issueStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Issue Structure");
			issueStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für einfache Abkömmlinge");
			issueStructLocal = pm.makePersistent(issueStructLocal);
		}
		return issueStruct;
	}

	private static void createDefaultStructure(IStruct issueStruct) {
		try {

//			StructBlock sb = PropHelper.createStructBlock(issueStruct, DESCRIPTION, "Description", "Beschreibung");
//			sb.setUnique(false);
//			I18nTextStructField descShort = PropHelper.createI18nTextField(sb, DESCRIPTION_SHORT, "Short description", "Kurzbeschreibung");
//			descShort.setLineCount(1);
//			I18nTextStructField descLong = PropHelper.createI18nTextField(sb, DESCRIPTION_LONG, "Long description", "Ausführliche Beschreibung");
//			descLong.setLineCount(10);
//
//			sb.addStructField(descShort);
//			sb.addStructField(descLong);
//
//			issueStruct.addStructBlock(sb);
//
//			// --------
//
//			sb = PropHelper.createStructBlock(issueStruct, SUBJECT, "Subject", "Bilder");
//			sb.setUnique(false);
//			I18nTextStructField subject = PropHelper.createI18nTextField(sb, SUBJECT_SUBJECT, "Subject", "Lehrfach");
//			subject.setLineCount(1);
//
//			sb.addStructField(subject);
//
//			issueStruct.addStructBlock(sb);

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	// *************** DEFAULT StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

//	public static final StructBlockID DESCRIPTION = StructBlockID.create(DEV_ORGANISATION_ID,"Issue.description");
//	public static final StructFieldID DESCRIPTION_SHORT = StructFieldID.create(DESCRIPTION,"Short");
//	public static final StructFieldID DESCRIPTION_LONG = StructFieldID.create(DESCRIPTION,"Long");
//
//	public static final StructBlockID SUBJECT = StructBlockID.create(DEV_ORGANISATION_ID,"Issue.subject");
//	public static final StructFieldID SUBJECT_SUBJECT = StructFieldID.create(SUBJECT,"Subject");
}
