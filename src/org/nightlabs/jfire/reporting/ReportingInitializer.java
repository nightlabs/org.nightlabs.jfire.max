/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Utils;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Helper class to initialize report layouts. The ReportingInitializer will
 * recusursively scan a given directory and create a tree of {@link ReportCategory}s
 * and {@link ReportLayout}s according to the directory structure it finds.<br/>
 * 
 * For each folder the initializer finds a category will be created as child of
 * the upper directory's one. A descriptor file 'content.xml' can be placed in
 * the directory to define in detail what id and names the category and report layous 
 * should get. <br/>
 * 
 * The recommended usage is 
 * <ul>
 * <li><b>Create the initializer</b>: Use {@link #ReportingInitializer(String, ReportCategory, String, JFireServerManager, PersistenceManager, String)} to create the initializer and set the
 * base category, root directory and fallback values for ids</li>
 * <li><b>Initialize (sub)directories</b>: Use {@link #initialize()} to start the initialization</li>
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingInitializer {

	protected static Logger logger = Logger.getLogger(ReportingInitializer.class);

	private String scriptSubDir;
	private ReportCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String reportRegistryItemType;
	
	private SAXException parseException;
	private Map<File, Document> categoryDescriptors = new HashMap<File, Document>();

	/**
	 * Returns the ReportCategory with the given id-parameters. If neccessary (not found in the datastore) 
	 * it will be newly created.
	 *  
	 * @param pm The PersistenceManager to use.
	 * @param parent The parent category.
	 * @param organisationID Organisation id-parameter.
	 * @param reportRegistryItemType Item type id-parameter.
	 * @param reportRegistryItemID Registry item id-parameter.
	 * @param internal Whether this should be treated as internal categroy.
	 * @return The ReportCategory with the given id-parameters.
	 */
	public static final ReportCategory createCategory(PersistenceManager pm, ReportCategory parent, String organisationID, String reportRegistryItemType, String reportRegistryItemID, boolean internal)  
	{
		if (logger.isDebugEnabled())
			logger.debug("createCategory: categoryPK=\""+organisationID+'/'+reportRegistryItemType+'/'+reportRegistryItemID+"\" parent=\""+ (parent == null ? null : (parent.getOrganisationID()+'/'+parent.getReportRegistryItemType()+'/'+parent.getReportRegistryItemID())) + "\" internal="+internal);

		ReportCategory category;
		try {
			category = (ReportCategory) pm.getObjectById(ReportRegistryItemID.create(organisationID, reportRegistryItemType, reportRegistryItemID));
			if (logger.isDebugEnabled()) {
				ReportRegistryItem p = category.getParentItem();
				logger.debug("createCategory: already exists: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\" parent=\"" + (p == null ? null : (p.getOrganisationID()+'/'+p.getReportRegistryItemType()+'/'+p.getReportRegistryItemID())) + "\" internal="+category.isInternal());
			}
		} catch (JDOObjectNotFoundException e) {
			category = new ReportCategory(parent, organisationID, reportRegistryItemType, reportRegistryItemID, internal);

			if (logger.isDebugEnabled())
				logger.debug("createCategory: persisting new category: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\"");

			category = (ReportCategory)pm.makePersistent(category);
		}
		return category;
	}
	
	/**
	 * Creates a new ReportingInitializer with the given parameters.
	 * 
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "JFireReporting.ear/script/General")
	 * @param baseCategory The base category from where the category-tree will be build.
	 * @param jfsm The JFireServerManager to use
	 * @param pm The PersistenceManager to use.
	 * @param registryItemType is the type (identifier) for the reports in categories, sub-categories get the scriptRegistryItemType from their parent 
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEVIL_ORGANISATION_ID}.
	 */
	public ReportingInitializer(
			String scriptSubDir, ReportCategory baseCategory, String reportRegistryItemType, 
			JFireServerManager jfsm, PersistenceManager pm, String organisationID
		)
	{
		this.scriptSubDir = scriptSubDir;
		this.baseCategory = baseCategory;
		this.jfsm = jfsm;
		this.pm = pm;
		this.organisationID = organisationID;
		this.reportRegistryItemType = reportRegistryItemType;
	}

	private ScriptRegistry scriptRegistry = null;
	protected ScriptRegistry getScriptRegistry()
	{
		if (scriptRegistry == null)
			scriptRegistry = ScriptRegistry.getScriptRegistry(pm);

		return scriptRegistry;
	}

	/**
	 * Start the initializing process.
	 * 
	 * @throws ModuleException
	 */	
	public void initialize() 
	throws ModuleException 
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, scriptSubDir);

		if (!scriptDir.exists())
			throw new IllegalStateException("Script directory does not exist: " + scriptDir.getAbsolutePath());

		logger.info("BEGIN initialization of Scripts");	
//		initDefaultParameterSets();
		createReportCategories(scriptDir, baseCategory);
	}

	private Document getCategoryDescriptor(File categoryDir) 
	throws SAXException, IOException 
	{
		Document doc = categoryDescriptors.get(categoryDir);
		if (doc == null) {
			final File contentFile = new File(categoryDir, "content.xml");
			if (contentFile.exists()) { 
				DOMParser parser = new DOMParser();
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						logger.error("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void fatalError(SAXParseException exception) throws SAXException {
						logger.fatal("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void warning(SAXParseException exception) throws SAXException {
						logger.warn("Parse ("+contentFile+"): ", exception);
					}
				});
				parseException = null;
				InputSource inputSource;
				try {
					inputSource = new InputSource(new FileInputStream(contentFile));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException("Although checked with .exists() file "+contentFile+" does not seem to exist. ", e);
				}
				parser.parse(inputSource);
				if (parseException != null)
					throw parseException;
				doc = parser.getDocument();
				categoryDescriptors.put(categoryDir, doc);
			}
		}
		return doc;
	}
	
	private Node getReportDescriptor(File scriptFile, Document categoryDocument) 
	throws TransformerException 
	{
		Collection<Node> nodes = NLDOMUtil.findNodeList(categoryDocument, "report-category/report");
		for (Node scriptNode : nodes) {
			Node fNode = scriptNode.getAttributes().getNamedItem("file");
			if (fNode != null && scriptFile.getName().equals(fNode.getTextContent()))
				return scriptNode;
		}
		return null;
	}
	
	private void createElementName(Node elementNode, I18nText name, String def) 
	{
		// script name
		boolean nameSet = false;
		if (elementNode != null) {
			Collection<Node> nodes = NLDOMUtil.findNodeList(elementNode, "name");
			for (Node node : nodes) {
				Node lIDNode = node.getAttributes().getNamedItem("language");
				if (lIDNode != null && !"".equals(lIDNode.getTextContent())) {
					name.setText(lIDNode.getTextContent(), node.getTextContent());
					nameSet = true;
				}
				else
					logger.warn("name element of node "+elementNode.getNodeName()+" has an invalid/missing language attribute");
			}
		}
		if (!nameSet)
			name.setText(Locale.ENGLISH.getLanguage(), def);
	}
	
	private void createReportCategories(File dir, ReportCategory parent) 
	throws ModuleException
	{
		try {
			String categoryID = dir.getName();
			String itemType = parent != null ? parent.getReportRegistryItemType() : reportRegistryItemType;
			boolean internal = false;
			Document catDocument = getCategoryDescriptor(dir);
			Node catNode = null;
			if (catDocument != null) {
				catNode = NLDOMUtil.findSingleNode(catDocument, "report-category");
				if (catNode != null) {
					Node typeAttr = catNode.getAttributes().getNamedItem("type");
					if (typeAttr != null && !"".equals(typeAttr.getTextContent()))
						itemType = typeAttr.getTextContent();
					
					Node idAttr = catNode.getAttributes().getNamedItem("id");
					if (idAttr != null && !"".equals(idAttr.getTextContent()))
						categoryID = idAttr.getTextContent();
					
					Node internalAttr = catNode.getAttributes().getNamedItem("internal");
					if (internalAttr != null && !"".equals(typeAttr.getTextContent()))
						internal = Boolean.parseBoolean(internalAttr.getTextContent());
				}
			}

			// Create the category
			ReportCategory category = createCategory(pm, parent, organisationID, itemType, categoryID, internal);

			// create the category name
			createElementName(catNode, category.getName(), categoryID);
			logger.info("create Script Category = "+category.getName());
			

			// Create reports
			File[] reports = dir.listFiles(scriptFileNameFilter);
			for (int j=0; j<reports.length; j++) {
				File reportFile = reports[j];

				Node reportNode = getReportDescriptor(reportFile, catDocument);				
				
				String reportID = Utils.getFileNameWithoutExtension(reportFile.getName());
				boolean overwriteOnInit = true;
				
				if (reportNode != null) {
					Node idNode = reportNode.getAttributes().getNamedItem("id");
					if (idNode != null && !"".equals(idNode.getTextContent()))
						reportID = idNode.getTextContent();
//					Node typeNode = reportNode.getAttributes().getNamedItem("type");
//					if (typeNode != null && !"".equals(typeNode.getTextContent()))
//						itemType = typeNode.getTextContent();
					Node overwriteNode = reportNode.getAttributes().getNamedItem("overwriteOnInit");
					if (overwriteNode != null && !"".equals(overwriteNode.getTextContent()))
						overwriteOnInit = Boolean.parseBoolean(overwriteNode.getTextContent());
				}
				
				try {			
					logger.info("create ReportLayout = " + reportID);				
					ReportLayout layout;
					boolean hadToBeCreated = false;
					try {
						pm.getExtent(ReportLayout.class);
						layout = (ReportLayout) pm.getObjectById(ReportRegistryItemID.create(
								organisationID,
								itemType,
								reportID)
						);
					} catch (JDOObjectNotFoundException e) {
						layout = new ReportLayout(category, organisationID, itemType, reportID);
						layout = (ReportLayout)pm.makePersistent(layout);
						hadToBeCreated = true;
					}
					if (overwriteOnInit || hadToBeCreated) {
						layout.loadFile(reportFile);
					}
					
					createElementName(reportNode, layout.getName(), reportID);
					
				} catch (Exception e) {
					logger.warn("could NOT create ReportLayout "+reportID+"!", e);
				}
			}

			
			// recurse
			File[] subDirs = dir.listFiles(dirFileFilter);		
			for (int i=0; i<subDirs.length; i++) {
				createReportCategories(subDirs[i], category);
			}
		} catch (IOException e) {
			throw new ModuleException(e);
		} catch (TransformerException e) {
			throw new ModuleException(e);
		} catch (SAXException e) {
			throw new ModuleException(e);
		}
	}

	protected FileFilter dirFileFilter = new FileFilter() {	
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}	
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{	
		public boolean accept(File dir, String name) 
		{				
			String fileExtension = Utils.getFileExtension(name);
			return "rptdesign".equals(fileExtension);
		}	
	};
	
}
