package org.nightlabs.jfire.issue.project;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

/**
 * This {@link ProjectStruct} class provides methods for creating {@link Struct} of {@link Project}.
 *
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
			projectStruct.getName().setText(Locale.GERMAN.getLanguage(), "Projekte");
			projectStruct = pm.makePersistent(projectStruct);
			projectStructLocal = new StructLocal(projectStruct, StructLocal.DEFAULT_SCOPE);
			projectStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Structure for Projects");
			projectStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für Projekte");
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
