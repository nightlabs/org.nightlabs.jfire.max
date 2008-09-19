package org.nightlabs.jfire.issue.project;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class ProjectStruct {

	public static IStruct getProjectStruct(String organisationID, PersistenceManager pm) {
		Struct projectStruct = null;
		StructLocal projectStructLocal = null;
		try {
			projectStruct = Struct.getStruct(organisationID, Project.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			projectStruct = new Struct(organisationID, Project.class.getName(), Struct.DEFAULT_SCOPE);
			projectStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Projects");
			projectStruct.getName().setText(Locale.GERMAN.getLanguage(), "Abkömmlinge");
			projectStruct = pm.makePersistent(projectStruct);
			projectStructLocal = new StructLocal(projectStruct, organisationID, StructLocal.DEFAULT_SCOPE);
			projectStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Project Structure");
			projectStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für einfache Abkömmlinge");
			projectStructLocal = pm.makePersistent(projectStructLocal);
		}
		return projectStruct;
	}

	private static void createDefaultStructure(IStruct projectStruct) {
		try {

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	// *************** DEFAULT StructBlocks StructField IDs ***************************
	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;
}
