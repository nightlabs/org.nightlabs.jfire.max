//package org.nightlabs.jfire.issue.project;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.nightlabs.i18n.I18nText;
//
///**
// * An extended class of {@link I18nText} that represents the {@link ProjectPhase}'s name. 
// * <p>
// * </p>
// * 
// * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
// *
// * @jdo.persistence-capable
// *		identity-type="application"
// *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectPhaseNameID"
// *		detachable="true"
// *		table="JFireIssueTracking_ProjectPhaseName"
// *
// * @jdo.inheritance strategy="new-table"
// *
// * @jdo.create-objectid-class field-order="organisationID, projectPhaseID"
// * 
// * @jdo.fetch-group name="ProjectPhaseName.name" fetch-groups="default" fields="projectPhase, names"
// */ 
//public class ProjectPhaseName 
//extends I18nText{
//	/**
//	 * The serial version of this class.
//	 */
//	private static final long serialVersionUID = 1L;
//	
//	/**
//	 * This is the organisationID to which the project phase's name belongs. Within one organisation,
//	 * all the project phase's names have their organisation's ID stored here, thus it's the same
//	 * value for all of them.
//	 * 
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String organisationID;
//	
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String projectPhaseID;
//	
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private ProjectPhase projectPhase;
//
//	/**
//	 * key: String languageID<br/>
//	 * value: String description
//	 * 
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		default-fetch-group="true"
//	 *		value-type="java.lang.String"
//	 *		table="JFireIssueTracking_ProjectPhaseName_names"
//	 *		null-value="exception"
//	 *
//	 * @jdo.join
//	 */
//	protected Map<String, String> names = new HashMap<String, String>();
//
//	/**
//	 * @deprecated Only for JDO!
//	 */
//	protected ProjectPhaseName()
//	{
//	}
//
//	public ProjectPhaseName(ProjectPhase projectPhase)
//	{
//		this.projectPhase = projectPhase;
//		this.organisationID = projectPhase.getOrganisationID();
//		projectPhaseID = projectPhase.getProjectPhaseID();
//	}
//
//	/**
//	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
//	 */
//	protected Map<String, String> getI18nMap()
//	{
//		return names;
//	}
//	
//	public String getOrganisationID() {
//		return organisationID;
//	}
//	
//	public ProjectPhase getProjectPhase() {
//		return projectPhase;
//	}
//	
//	/**
//	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
//	 */
//	protected String getFallBackValue(String languageID)
//	{
//		return projectPhaseID == null ? languageID : projectPhaseID;
//	}
//}