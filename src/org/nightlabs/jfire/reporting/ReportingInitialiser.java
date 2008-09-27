/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportLayoutLocalisationDataID;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig;
import org.nightlabs.jfire.reporting.parameter.config.IGraphicalInfoProvider;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumer;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionUseCaseID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.reporting.textpart.ReportTextPart;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartConfiguration;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Helper class to initialize report layouts. The ReportingInitialiser will
 * recursively scan a given directory and create a tree of {@link ReportCategory}s
 * and {@link ReportLayout}s according to the directory structure it finds.
 * </p>
 * <p>
 * For each folder the initializer finds a category will be created as child of
 * the upper directories one. A descriptor file 'content.xml' can be placed in
 * the directory to define in detail what id and names the category and report layouts
 * should get.
 * </p>
 * <p>
 * The {@link ReportInitialiser} will also create a parameter acquisition workflow
 * defined in the content.xml for the report layout. For further details about how to
 * define the acquisition workflow see the content.xml dtd (http://www.nightlabs.de/dtd/reporting_initialiser_*.dtd)
 * </p>
 * <p>
 * Additionally resource bundles used for localisation of the reports are created by the
 * initializer. These files should be placed in a subfolder 'resource' for each category and should be
 * prefixed with the report layout id. (e.g. DefaultInvoiceLayout_en_EN.properties)
 * </p>
 * <p>
 * The recommended usage is
 * <ul>
 * <li><b>Create the initializer</b>: Use {@link #ReportingInitialiser(String, ReportCategory, String, JFireServerManager, PersistenceManager, String)} to create the initializer and set the
 * base category, root directory and fallback values for ids</li>
 * <li><b>Initialise (sub)directories</b>: Use {@link #initialise()} to start the initialisation</li>
 * </p>
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingInitialiser {

	protected static Logger logger = Logger.getLogger(ReportingInitialiser.class);

	private String scriptSubDir;
	private ReportCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String reportRegistryItemType;
	
//	private SAXException parseException;
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

		// initialise meta-data
		pm.getExtent(ReportCategory.class);

		ReportCategory category;
		try {
			category = (ReportCategory) pm.getObjectById(ReportRegistryItemID.create(organisationID, reportRegistryItemType, reportRegistryItemID));
			if (logger.isDebugEnabled()) {
				ReportRegistryItem p = category.getParentCategory();
				logger.debug("createCategory: already exists: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\" parent=\"" + (p == null ? null : (p.getOrganisationID()+'/'+p.getReportRegistryItemType()+'/'+p.getReportRegistryItemID())) + "\" internal="+category.isInternal());
			}
		} catch (JDOObjectNotFoundException e) {
			category = new ReportCategory(parent, organisationID, reportRegistryItemType, reportRegistryItemID, internal);

			if (logger.isDebugEnabled())
				logger.debug("createCategory: persisting new category: categoryPK=\""+category.getOrganisationID()+'/'+category.getReportRegistryItemType()+'/'+category.getReportRegistryItemID()+"\"");

			category = pm.makePersistent(category);
		}
		return category;
	}
	
	/**
	 * Creates a new ReportingInitialiser with the given parameters.
	 * 
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "JFireReporting.ear/script/General")
	 * @param baseCategory The base category from where the category-tree will be build.
	 * @param jfsm The JFireServerManager to use
	 * @param pm The PersistenceManager to use.
	 * @param registryItemType is the type (identifier) for the reports in categories, sub-categories get the scriptRegistryItemType from their parent
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEV_ORGANISATION_ID}.
	 */
	public ReportingInitialiser(
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
	 * @throws ReportingInitialiserException If initializing fails. 
	 */
	public void initialise()
	throws ReportingInitialiserException
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File reportInitDir = new File(j2eeBaseDir, scriptSubDir);

		if (!reportInitDir.exists())
			throw new IllegalStateException("Report initialisation directory does not exist: " + reportInitDir.getAbsolutePath());

		logger.debug("BEGIN initialization of ReportLayouts");
//		initDefaultParameterSets();

		// initialise meta-data
		pm.getExtent(ReportCategory.class);
		pm.getExtent(ReportLayout.class);
		pm.getExtent(AcquisitionParameterConfig.class);
		pm.getExtent(ReportParameterAcquisitionSetup.class);
		pm.getExtent(ReportParameterAcquisitionUseCase.class);
		pm.getExtent(ValueProvider.class);
		pm.getExtent(ValueAcquisitionSetup.class);

		createReportCategories(reportInitDir, baseCategory);
	}

	private Document parseFile(final File xmlFile)
	throws SAXException, IOException
	{
		if (!xmlFile.exists())
			return null;
		
		DOMParser parser = new DOMParser();
		final SAXException[] parseException = new SAXException[1];
		parser.setErrorHandler(new ErrorHandler(){
			public void error(SAXParseException exception) throws SAXException {
				logger.error("Parse ("+xmlFile+"): ", exception);
				parseException[0] = exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				logger.fatal("Parse ("+xmlFile+"): ", exception);
				parseException[0] = exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
				logger.warn("Parse ("+xmlFile+"): ", exception);
			}
		});
		InputSource inputSource;
		try {
			inputSource = new InputSource(new FileInputStream(xmlFile));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Although checked with .exists() file "+xmlFile+" does not seem to exist. ", e);
		}
		parser.parse(inputSource);
		if (parseException[0] != null)
			throw parseException[0];
		return parser.getDocument();
	}
	
	private Document getCategoryDescriptor(File categoryDir)
	throws SAXException, IOException
	{
		Document doc = categoryDescriptors.get(categoryDir);
		if (doc == null) {
			final File contentFile = new File(categoryDir, "content.xml");
			if (contentFile.exists()) {
				doc = parseFile(contentFile);
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
	
	/**
	 * Read the contents of an {@link I18nText} from an xml node.
	 * The xml element has to be in the following style
	 * <pre>
	 * <elementName language="somelanguage">content</elementName>
	 * </pre>
	 * 
	 * @param elementNode The node to read children from.
	 * @param elementName The name of the sub-elements that form the content.
	 * @param name The {@link I18nText} to fill
	 * @param def This will be set for english if nothing else was set and def != null
	 */
	private void createElementName(Node elementNode, String elementName, I18nText name, String def)
	{
		if (name == null) {
			logger.warn("createElementName called with null element!", new NullPointerException("name"));
			return;
		}
		boolean nameSet = false;
		if (elementNode != null) {
			Collection<Node> nodes = NLDOMUtil.findNodeList(elementNode, elementName);
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
		if (!nameSet && def != null)
			name.setText(Locale.ENGLISH.getLanguage(), def);
	}
	
	/**
	 * Recurses from the given directory and report categories
	 * based on the &lt;report-category&gt; element found in the content.xml files there.
	 * 
	 * @param dir The directory to start recursion
	 * @param parent The {@link ReportCategory}
	 * @throws ReportingInitialiserException
	 */
	protected void createReportCategories(File dir, ReportCategory parent)
	throws ReportingInitialiserException
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
			createElementName(catNode, "name", category.getName(), categoryID);
			createElementName(catNode, "description", category.getDescription(), null);
			logger.debug("create Script Category = "+category.getName());
			
			createReportTextPartConfiguration(new File(dir, "content.xml"), category.getReportRegistryItemID(), category);


			// Create reports
			File[] reports = dir.listFiles(scriptFileNameFilter);
			for (int j=0; j<reports.length; j++) {
				File reportFile = reports[j];
				try {
					createReportLayout(reportFile, category, catDocument, itemType);
				} catch (Exception e) {
					logger.warn("could NOT create ReportLayout for file " + reportFile + "!", e);
				}
			}

			
			// recurse
			File[] subDirs = dir.listFiles(dirFileFilter);
			for (int i=0; i<subDirs.length; i++) {
				createReportCategories(subDirs[i], category);
			}
		} catch (IOException e) {
			throw new ReportingInitialiserException(e);
		} catch (SAXException e) {
			throw new ReportingInitialiserException(e);
		}
	}

	/**
	 * Creates a {@link ReportLayout} from the given file.
	 * 
	 * @param reportFile The file to create a new ReportFile from.
	 * @param category The category to add the report
	 * @param catDocument The document where meta-data of the report can be found
	 * @param reportRegistryItemType The reportRegistryItemType to use as fallback.
	 * @throws Exception
	 */
	protected void createReportLayout(
			File reportFile, ReportCategory category, Document catDocument,
			String reportRegistryItemType)
	throws Exception
	{
		Node reportNode = getReportDescriptor(reportFile, catDocument);
		
		String reportID = IOUtil.getFileNameWithoutExtension(reportFile.getName());
		boolean overwriteOnInit = true;
		
		if (reportNode != null) {
			Node idNode = reportNode.getAttributes().getNamedItem("id");
			if (idNode != null && !"".equals(idNode.getTextContent()))
				reportID = idNode.getTextContent();
//			Node typeNode = reportNode.getAttributes().getNamedItem("type");
//			if (typeNode != null && !"".equals(typeNode.getTextContent()))
//				itemType = typeNode.getTextContent();
			Node overwriteNode = reportNode.getAttributes().getNamedItem("overwriteOnInit");
			if (overwriteNode != null && !"".equals(overwriteNode.getTextContent()))
				overwriteOnInit = Boolean.parseBoolean(overwriteNode.getTextContent());
		}
		
		logger.debug("create ReportLayout = " + reportID);
		ReportLayout layout;
		boolean hadToBeCreated = false;
		try {
			pm.getExtent(ReportLayout.class);
			layout = (ReportLayout) pm.getObjectById(ReportRegistryItemID.create(
					organisationID,
					reportRegistryItemType,
					reportID)
			);
		} catch (JDOObjectNotFoundException e) {
			layout = new ReportLayout(category, organisationID, reportRegistryItemType, reportID);
			layout = pm.makePersistent(layout);
			hadToBeCreated = true;
		}

		boolean doInit = overwriteOnInit || hadToBeCreated;
		if (doInit) {
			File layoutFile = createReportFileFromTemplate(reportFile);
			layout.loadFile(layoutFile);
			layoutFile.delete();
			createElementName(reportNode, "name", layout.getName(), reportID);
			createElementName(reportNode, "description", layout.getDescription(), null);
			createReportLocalisationData(reportFile, reportID, reportNode, layout);
			createReportTextPartConfiguration(reportFile, reportID, layout);
		}
		// This has its own overwriteOnInit
		createReportParameterAcquisitionSetup(reportFile, reportNode, layout);
	}
	
	/**
	 * Creates a new report design file from the given one by replacing
	 * variables in the given File.
	 * <p>
	 * The variables replaced are defined here: {@link #getTemplateReplaceVariables()}
	 * </p>
	 * @param reportFile The template file to replace the variables in
	 * @return The newly created file with all variables replaced.
	 * @throws Exception If something fails.
	 */
	protected File createReportFileFromTemplate(File reportFile)
	throws Exception
	{
		File tmpFile = File.createTempFile(IOUtil.getFileNameWithoutExtension(reportFile.getName()), ".rptdesign", IOUtil.getTempDir());
		tmpFile.deleteOnExit();
		importTemplateToLayoutFile(reportFile, tmpFile);
		return tmpFile;
	}
	
	/**
	 * Get the variable names and values that are replaced by
	 * when a layout is imported or exported.
	 * <p>
	 * By now the variables replaced are:
	 * <ul>
	 * <li>${rootOrganisationID}</li>
	 * <li>${rootOrganisationIDConverted}</li>
	 * <li>${devOrganisationID}</li>
	 * <li>${devOrganisationIDConverted}</li>
	 * </ul>
	 * </p>
	 * @param useAlsoDummyRootOrganisationID Whether to use the local organisationID as ${rootOrganisationID}
	 * 		when there the server is in standalone mode. 
	 * @return The variable names and values
	 * @throws Exception If something fails getting the organisationID or the rootOrganisationID
	 */
	protected static Map<String, String> getTemplateReplaceVariables(boolean useAlsoDummyRootOrganisationID) 
	throws Exception 
	{
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		Map<String, String> variables = new HashMap<String, String>();
		try {
			InitialContext ctx = new InitialContext(SecurityReflector.getInitialContextProperties());
			String rootOrganisationID = null;
			if (Organisation.hasRootOrganisation(ctx) || useAlsoDummyRootOrganisationID)
				 rootOrganisationID = Organisation.getRootOrganisationID(ctx);			
			try {
				if (rootOrganisationID != null) {
					variables.put("rootOrganisationID", rootOrganisationID);
					variables.put("rootOrganisationIDConverted", convertToReportColumnString(rootOrganisationID));
				}
				variables.put("devOrganisationID", Organisation.DEV_ORGANISATION_ID);
				variables.put("devOrganisationIDConverted", convertToReportColumnString(Organisation.DEV_ORGANISATION_ID));
				variables.put("localOrganisationID", organisationID);
				variables.put("localOrganisationIDConverted", convertToReportColumnString(organisationID));
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		return variables;
	}
	
	/**
	 * Export the given ReportLayout file to a template file (variable values replaced by their names).
	 * 
	 * @param reportFile The file to export.
	 * @param exportFile The file to export to.
	 * @throws Exception If something fails.
	 */
	public static void exportLayoutToTemplateFile(File reportFile, File exportFile)
	throws Exception
	{
		try {
			Map<String, String> variables = getTemplateReplaceVariables(false);
			Map<Pattern, String> replacements = new HashMap<Pattern, String>();
			for (Map.Entry<String, String> varEntry : variables.entrySet()) {
				Pattern pat =  Pattern.compile(Pattern.quote(varEntry.getValue()));
				// TODO: FIXME: Pattern.quote does not seem to work with replaceAll later ?!?
//				replacements.put(pat, Pattern.quote("${" + varEntry.getKey() + "}"));
				replacements.put(pat, "\\$\\{" + varEntry.getKey() + "\\}");
			}
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), IOUtil.CHARSET_NAME_UTF_8));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(reportFile), IOUtil.CHARSET_NAME_UTF_8));
			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					String tmpStr = line;
					for (Map.Entry<Pattern, String> varEntry : replacements.entrySet()) {
						tmpStr = varEntry.getKey().matcher(tmpStr).replaceAll(varEntry.getValue());
					}
					writer.write(tmpStr + "\n");					
				}
			} finally {
				reader.close();
				writer.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Import the given template file as ReportLayout file 
	 * (variable names replaced by their values: see {@link #getTemplateReplaceVariables()}).
	 * 
	 * @param templateFile The file to import.
	 * @param layoutFile The file to create.
	 * @throws Exception If something fails.
	 */
	public static void importTemplateToLayoutFile(File templateFile, File layoutFile)
	throws Exception
	{
		Map<String, String> variables = getTemplateReplaceVariables(true);		
		IOUtil.replaceTemplateVariables(layoutFile, templateFile, IOUtil.CHARSET_NAME_UTF_8, variables);
	}
	
	/**
	 * Returns the given string converted so that it can be used
	 * as name of dataset column in a report.
	 * 
	 * @param str The String to convert.
	 * @return The given str with all "." replaced with "_" and "_" with "__"
	 */
	public static String convertToReportColumnString(String str) {
		String key = str.replaceAll("_", "__");
		key = key.replaceAll("\\.", "_");
		return key;
	}
	
	protected FileFilter dirFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !(pathname.toString().endsWith("resource"));
		}
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			String fileExtension = IOUtil.getFileExtension(name);
			return "rptdesign".equals(fileExtension);
		}
	};

	
	protected void createReportParameterAcquisitionSetup(File reportFile, Node reportNode, ReportLayout layout)
	throws ReportingInitialiserException
	{
		Node acquisitionNode = NLDOMUtil.findSingleNode(reportNode, "parameter-acquisition");
		if (acquisitionNode == null)
			return;

		// the setup already persistent
		ReportParameterAcquisitionSetup setup = ReportParameterAcquisitionSetup.getSetupForReportLayout(pm, (ReportRegistryItemID)JDOHelper.getObjectId(layout));
		
		Map<String, Object> id2BindingObject = new HashMap<String, Object>();
		
		Collection<Node> useCaseNodes = NLDOMUtil.findNodeList(acquisitionNode, "use-case");
		for (Node useCaseNode : useCaseNodes) {
			Node useCaseIDNode = useCaseNode.getAttributes().getNamedItem("id");
			if (useCaseIDNode == null)
				throw new ReportingInitialiserException("ReportParameterAcquisitionUseCase element <use-case> must define an id attribute. See file "+reportFile.toString());
			String useCaseID = useCaseIDNode.getNodeValue();
			
			boolean overwriteOnInit = false;
			Node overwriteNode = reportNode.getAttributes().getNamedItem("overwriteOnInit");
			if (overwriteNode != null && !"".equals(overwriteNode.getTextContent()))
				overwriteOnInit = Boolean.parseBoolean(overwriteNode.getTextContent());
			
			ReportParameterAcquisitionUseCase useCase = getUseCase(setup, useCaseID);
			if (!overwriteOnInit && useCase != null)
				// use-case was already created sometime and should not be overwritten
				// continue with next use-case
				continue;
			
			if (useCase != null && overwriteOnInit) {
				// use-case should be overwritten
//				useCase = resetUseCaseAndAcquisitionSetup(setup, useCase);
				if (setup != null) {
					setup.removeUseCase(useCase);
					useCase = null;
				}
			}

			if (setup == null) {
				// if setup not created by now, create it
				setup = new ReportParameterAcquisitionSetup(organisationID, IDGenerator.nextID(ReportParameterAcquisitionSetup.class), layout);
				setup = pm.makePersistent(setup);
			}
			
			// (re-)create the use case
			if (useCase == null)
				useCase = new ReportParameterAcquisitionUseCase(setup, useCaseID);
			createElementName(useCaseNode, "name", useCase.getName(), useCaseID);
			createElementName(useCaseNode, "description", useCase.getDescription(), useCaseID);

			ValueAcquisitionSetup acquisitionSetup = new ValueAcquisitionSetup(organisationID, IDGenerator.nextID(ValueAcquisitionSetup.class), setup, useCase);
			
			id2BindingObject.clear();

			// Read parameter list
			Collection<Node> parameters = NLDOMUtil.findNodeList(useCaseNode, "parameters/parameter");
			for (Node paramNode : parameters) {
				try {
					String id = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "id");
					String name = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "name");
					String type = NLDOMUtil.getNonEmptyAttributeValue(paramNode, "type");
					AcquisitionParameterConfig parameterConfig = new AcquisitionParameterConfig(acquisitionSetup, name, type);
					setGraphicalData(paramNode, parameterConfig);
					id2BindingObject.put(id, parameterConfig);
					acquisitionSetup.getParameterConfigs().add(parameterConfig);
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the parameters declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}
			
			// Read value provider Config List
			Collection<Node> providerConfigs = NLDOMUtil.findNodeList(useCaseNode, "value-provider-configs/provider-config");
			for (Node providerNode : providerConfigs) {
				try {
					String id = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "id");
					String providerOrganisationID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "organisationID");
					String providerCategoryID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "categoryID");
					String valueProviderID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "valueProviderID");
					
					int pageIndex = 0;
					try {
						String pageIndexStr = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "pageIndex");
						if (pageIndexStr == null)
							pageIndexStr = "0";
						pageIndex = Integer.parseInt(pageIndexStr);
					} catch (Exception e) {
						pageIndex = 0;
					}
					int pageRow = 0;
					try {
						String pageRowStr = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "pageRow");
						if (pageRowStr == null)
							pageRowStr = "0";
						pageRow = Integer.parseInt(pageRowStr);
					} catch (Exception e) {
						pageRow = 0;
					}
					int pageColumn = 0;
					try {
						String pageColumnStr = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "pageColumn");
						if (pageColumnStr == null)
							pageColumnStr = "0";
						pageColumn = Integer.parseInt(pageColumnStr);
					} catch (Exception e) {
						pageColumn = 0;
					}

					ValueProviderConfig config = new ValueProviderConfig(acquisitionSetup, IDGenerator.nextID(ValueProviderConfig.class));
					ValueProviderID providerID = ValueProviderID.create(providerOrganisationID, providerCategoryID, valueProviderID);
					ValueProvider provider = null;
					try {
						provider = (ValueProvider) pm.getObjectById(providerID);
						provider.getCategory();
					} catch (JDOObjectNotFoundException e) {
						throw new ReportingInitialiserException("Referenced ValueProvider does not exist "+providerID+". See "+reportFile, e);
					}
					config.setValueProvider(provider);
					config.setPageIndex(pageIndex);
					config.setPageRow(pageRow);
					config.setPageColumn(pageColumn);
					
					boolean allowOutputNull = false;
					String allowNullValue = NLDOMUtil.getAttributeValue(providerNode, "allowNullOutputValue");
					if (allowNullValue != null) {
						try {
							allowOutputNull = Boolean.parseBoolean(allowNullValue);
						} catch (Exception e) {
							allowOutputNull = false;
						}
					}
					config.setAllowNullOutputValue(allowOutputNull);
					
					String showMessage = NLDOMUtil.getAttributeValue(providerNode, "showMessageInHeader");
					if (showMessage != null) {
						try {
							config.setShowMessageInHeader(Boolean.parseBoolean(showMessage));
						} catch (Exception e) {
						}
					}
					
					String growVert = NLDOMUtil.getAttributeValue(providerNode, "growVertically");
					if (growVert != null) {
						try {
							config.setGrowVertically(Boolean.parseBoolean(growVert));
						} catch (Exception e) {
						}
					}
					
					createElementName(providerNode, "message", config.getMessage(), null);
					
					setGraphicalData(providerNode, config);
					
					id2BindingObject.put(id, config);
					acquisitionSetup.getValueProviderConfigs().add(config);
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the value-provider-configs declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}
			
			// Read binding list
			Collection<Node> bindings = NLDOMUtil.findNodeList(useCaseNode, "value-consumer-bindings/value-consumer-binding");
			for (Node bindingNode : bindings) {
				try {
					Node providerNode = NLDOMUtil.findElementNode("binding-provider", bindingNode);
					if (providerNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element provider. See file "+reportFile);
					String providerID = NLDOMUtil.getNonEmptyAttributeValue(providerNode, "id");
					Node parameterNode = NLDOMUtil.findElementNode("binding-parameter", bindingNode);
					if (parameterNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element parameter. See file "+reportFile);
					String parameterName = NLDOMUtil.getNonEmptyAttributeValue(parameterNode, "name");
					Node consumerNode = NLDOMUtil.findElementNode("binding-consumer", bindingNode);
					if (consumerNode == null)
						throw new ReportingInitialiserException("Element value-consumer-binding/binding has to define sub-element consumer. See file "+reportFile);
					String consumerID = NLDOMUtil.getNonEmptyAttributeValue(consumerNode, "id");

					ValueProviderConfig provider = null;
					try {
						provider = (ValueProviderConfig) id2BindingObject.get(providerID);
						if (provider == null)
							throw new IllegalArgumentException("Could not resolve the provider with id=\""+providerID+"\".");
					} catch (Exception e) {
						throw new ReportingInitialiserException("The provider-id of a binding does not point to a ValueProviderConfig. File "+reportFile, e);
					}
					ValueConsumer consumer = null;
					try {
						consumer = (ValueConsumer) id2BindingObject.get(consumerID);
						if (consumer == null)
							throw new IllegalArgumentException("Could not resolve the consumer with id=\""+consumerID+"\".");
					} catch (Exception e) {
						throw new ReportingInitialiserException("The provider-id of a binding does not point to a ValueProviderConfig. File "+reportFile, e);
					}

					ValueConsumerBinding binding = new ValueConsumerBinding(organisationID, IDGenerator.nextID(ValueConsumerBinding.class), acquisitionSetup);
					binding.setProvider(provider);
					binding.setParameterID(parameterName);
					binding.setConsumer(consumer);
					acquisitionSetup.getValueConsumerBindings().add(binding);
				} catch (IllegalArgumentException e) {
					throw new ReportingInitialiserException("Some attribute is missing for the value-consumer-bindings declaration in the parameter-acquisition in file "+reportFile, e);
				}
			}

//			// TODO JPOX WORKAROUND : To directly put the new acquisitionSetup causes a duplicate key exception
//			Object o = setup.getValueAcquisitionSetups().get(useCase);
//			if (o != null) {
//				setup.getValueAcquisitionSetups().remove(useCase);
////				pm.deletePersistent(o);
//			}
//
//			pm.flush();

//			acquisitionSetup = pm.makePersistent(acquisitionSetup);
			// TODO end JPOX workaround

			// make the usecase setup persistent
			setup.addValueAcquisitionSetup(acquisitionSetup);


			// TODO JPOX WORKAROUND : this was not needed with JPOX 1.1 - but now we seem to need it
			if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED)
				pm.flush();


			// check if its the default use case
			boolean defUseCase = false;
			String defaultStr = NLDOMUtil.getAttributeValue(useCaseNode, "default");
			if (defaultStr != null && ! "".equals(defaultStr.trim())) {
				try {
					defUseCase = Boolean.parseBoolean(defaultStr);
				} catch (Exception e) {
					defUseCase = false;
				}
			}
			if (defUseCase)
				setup.setDefaultSetup(acquisitionSetup);
		}
	}
	
	private ReportParameterAcquisitionUseCase getUseCase(ReportParameterAcquisitionSetup setup, String useCaseID) {
		if (setup == null)
			return null;
		for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
			if (entry.getKey().getReportParameterAcquisitionUseCaseID().equals(useCaseID))
				return entry.getKey();
		}
		if (setup.getDefaultSetup() != null &&
			setup.getDefaultSetup().getUseCase() != null &&
			setup.getDefaultSetup().getUseCase().getReportParameterAcquisitionUseCaseID().equals(useCaseID)
		)
			return setup.getDefaultSetup().getUseCase();
		ReportParameterAcquisitionUseCaseID id = ReportParameterAcquisitionUseCaseID.create(setup.getOrganisationID(), setup.getReportParameterAcquisitionSetupID(), useCaseID);
		ReportParameterAcquisitionUseCase useCase = null;
		try {
			useCase = (ReportParameterAcquisitionUseCase) pm.getObjectById(id);
			useCase.getReportParameterAcquisitionSetupID();
		} catch (JDOObjectNotFoundException e) {
			useCase = null;
		}
		return useCase;
	}
	
	private void setGraphicalData(Node elementNode, IGraphicalInfoProvider graphicalInfoProvider) {
		String xStr = NLDOMUtil.getAttributeValue(elementNode, "x");
		int x = 0;
		try {
			x = Integer.parseInt(xStr);
		} catch (Exception e) {
			x = 0;
		}
		graphicalInfoProvider.setX(x);
		
		String yStr = NLDOMUtil.getAttributeValue(elementNode, "y");
		int y = 0;
		try {
			y = Integer.parseInt(yStr);
		} catch (Exception e) {
			y = 0;
		}
		graphicalInfoProvider.setY(y);
	}
	
//	private ReportParameterAcquisitionUseCase resetUseCaseAndAcquisitionSetup(ReportParameterAcquisitionSetup setup, ReportParameterAcquisitionUseCase useCase) {
////		if (setup.getDefaultSetup() != null && setup.getDefaultSetup().getUseCase() != null && setup.getDefaultSetup().getUseCase().equals(useCase))
////			setup.setDefaultSetup(null);
//
//		// TODO JPOX WORKAROUND Deleting the setup via pm.deletePersistent(setup) fails => workaround
//		setup.setDefaultSetup(null);
////		setup.getValueAcquisitionSetups().clear();
//		for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> entry : setup.getValueAcquisitionSetups().entrySet()) {
//			if (entry.getKey().getReportParameterAcquisitionUseCaseID().equals(useCase.getReportParameterAcquisitionUseCaseID())) {
//				for (ValueConsumerBinding binding : entry.getValue().getValueConsumerBindings()) {
//					pm.deletePersistent(binding);
//				}
//				pm.flush();
//				entry.getValue().getValueConsumerBindings().clear();
//				entry.getValue().getParameterConfigs().clear();
//				entry.getValue().getValueProviderConfigs().clear();
//				return entry.getKey();
//			}
//		}
//		pm.flush();
//		return useCase;
//	}
	
	/**
	 * Create the {@link ReportLayoutLocalisationData} objects for the given layout. It will search in a subfolder 'resource'
	 * for entries named like reportID_{locale}.properties
	 * 
	 * @param reportFile The report file currently processed.
	 * @param reportID The id of the {@link ReportLayout} currently processed.
	 * @param reportNode The 'report' node of the content.xml document currenty processed
	 * @param layout The {@link ReportLayout} currently processed.
	 * @throws ReportingInitialiserException
	 */
	protected void createReportLocalisationData(File reportFile, final String reportID, Node reportNode, ReportLayout layout)
	throws ReportingInitialiserException
	{
		// initialise meta-data
		pm.getExtent(ReportLayoutLocalisationData.class);

		File resourceFolder = new File(reportFile.getParentFile(), "resource");
		File[] resourceFiles = resourceFolder.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().startsWith(reportID);
			}
		});
		if (resourceFiles == null)
			return;
		for (File resFile : resourceFiles) {
			String locale = ReportLayoutLocalisationData.extractLocale(resFile.getName());
			if (locale == null)
				locale = "";
			ReportLayoutLocalisationDataID localisationDataID = ReportLayoutLocalisationDataID.create(
					layout.getOrganisationID(), layout.getReportRegistryItemType(), layout.getReportRegistryItemID(), locale
			);
			ReportLayoutLocalisationData localisationData = null;
			try {
				localisationData = (ReportLayoutLocalisationData) pm.getObjectById(localisationDataID);
				localisationData.getLocale();
			} catch (JDOObjectNotFoundException e) {
				localisationData = new ReportLayoutLocalisationData(layout, locale);
				localisationData = pm.makePersistent(localisationData);
			}
			try {
				localisationData.loadFile(resFile);
			} catch (IOException e) {
				throw new ReportingInitialiserException("Could not load localisatino file "+resFile, e);
			}
		}
	}
	
	/**
	 * @param file The report file currently processed (This might be the content.xml file for a category or the .rptdesign file for a layout).
	 * @param reportRegistryItemID The id of the {@link ReportRegistryItem} currently processed.
	 * @param layout The {@link ReportLayout} currently processed.
	 * @throws ReportingInitialiserException
	 */
	protected void createReportTextPartConfiguration(File file, final String reportRegistryItemID, ReportRegistryItem reportRegistryItem)
	throws ReportingInitialiserException
	{
		logger.debug("Initializatin of ReportTextPartConfiguration of " + JDOHelper.getObjectId(reportRegistryItem) + " started.");
		
		// initialise meta-data
		pm.getExtent(ReportLayoutLocalisationData.class);

		File targetConfigurationFile = new File(file.getParentFile(), 
			reportRegistryItemID + "." + 
			ReportTextPartConfiguration.class.getSimpleName() +
			".xml");

		if (!targetConfigurationFile.exists())
			return; // Nothing to do.
		
		Document doc;
		try {
			doc = parseFile(targetConfigurationFile);
		} catch (Exception e) {
			throw new ReportingInitialiserException("Parsing file failed " + targetConfigurationFile, e);
		}
		if (doc == null) {
			logger.error("Parsing " +  targetConfigurationFile + " failed, document is null.");
			return;
		}
		logger.debug("Parsed ReportTextConfiguration file " + targetConfigurationFile);

		// Lookup/Create the configuration object
		ReportTextPartConfiguration configuration = ReportTextPartConfiguration.getReportTextPartConfiguration(pm, reportRegistryItem);
		boolean needsPersisting = false;
		if (configuration == null) {
			needsPersisting = true;
			configuration = new ReportTextPartConfiguration(
					reportRegistryItem.getOrganisationID(), IDGenerator.nextID(ReportTextPartConfiguration.class));
			configuration.setReportRegistryItem(reportRegistryItem);
		}
		
		// Update/Create the ReportTextParts wihtin the configuration
		Collection<Node> nodes = NLDOMUtil.findNodeList(doc, "reportTextPartConfiguration/reportTextPart");		
		for (Node reportTextPartNode : nodes) {
			Node idNode = reportTextPartNode.getAttributes().getNamedItem("id");
			if (idNode == null || "".equals(idNode.getTextContent())) {
				logger.error("No 'id' was specified for a 'reportTextPart' in file " + targetConfigurationFile);
				continue;
			}
			Node typeNode = reportTextPartNode.getAttributes().getNamedItem("type");
			if (typeNode == null || "".equals(typeNode.getTextContent())) {
				logger.error("No 'type' was specified for a 'reportTextPart' in file " + targetConfigurationFile);
				continue;
			}
			ReportTextPart.Type type = ReportTextPart.Type.valueOf(typeNode.getTextContent().toUpperCase());
			if (configuration == null) {
				// create only when a text part was defined
			}
			
			ReportTextPart part = configuration.getReportTextPart(idNode.getTextContent());
			if (part == null) {
				part = new ReportTextPart(configuration, idNode.getTextContent());
				configuration.addReportTextPart(part);
			}
			part.setType(type);
			createElementName(reportTextPartNode, "name", part.getName(), null);
			createElementName(reportTextPartNode, "content", part.getContent(), null);
			logger.debug("Created ReportTextPart with id " + part.getReportTextPartID());
		}
		if (needsPersisting && configuration != null) {
			pm.makePersistent(configuration);
		}
	}
	
}
