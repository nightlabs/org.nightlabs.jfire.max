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
 * This {@link IssueStruct} class provides methods for creating {@link Struct} of {@link Issue}.
 *
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueStruct {

	public static IStruct getIssueStruct(PersistenceManager pm) {
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;

		Struct issueStruct = null;
		StructLocal issueStructLocal = null;
		try {
			issueStruct = Struct.getStruct(devOrganisationID, Issue.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			issueStruct = new Struct(devOrganisationID, Issue.class.getName(), Struct.DEFAULT_SCOPE);
			createDefaultStructure(issueStruct);
			issueStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Issues");
			issueStruct.getName().setText(Locale.GERMAN.getLanguage(), "Issues");
			issueStruct = pm.makePersistent(issueStruct);
			
			issueStructLocal = new StructLocal(issueStruct, StructLocal.DEFAULT_SCOPE);
			issueStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Issue Structure");
			issueStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für Issues");
			issueStructLocal = pm.makePersistent(issueStructLocal);
		}
		return issueStruct;
	}

	private static void createDefaultStructure(IStruct issueStruct) {
		try {

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	// *************** DEFAULT StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;
}
