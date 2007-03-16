package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * !@jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *		detachable="true"
 *		table="JFireTrade_ArticleContainer"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * !@jdo.create-objectid-class
 *		field-order="organisationID, articleContainerIDPrefix, articleContainerID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *
 * @jdo.fetch-group name="ArticleContainer.articles" fields="articles"
 * @jdo.fetch-group name="ArticleContainer.createUser" fields="createUser"
 * @jdo.fetch-group name="ArticleContainer.customer" fields="customer"
 * @jdo.fetch-group name="ArticleContainer.vendor" fields="vendor"
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public abstract class AbstractArticleContainer 
implements ArticleContainer, Serializable, Statable
{	
	public static final String FETCH_GROUP_ARTICLES = "ArticleContainer.articles";
	public static final String FETCH_GROUP_CREATE_USER = "ArticleContainer.createUser";
	public static final String FETCH_GROUP_CUSTOMER = "ArticleContainer.customer";
	public static final String FETCH_GROUP_VENDOR = "ArticleContainer.vendor";
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.Article"
	 *		mapped-by="receptionNote"
	 */
	private Set<Article> articles;
		
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID vendorID = null;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID customerID = null;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean customerID_detached = false;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private LegalEntity customer;
	
	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private OrganisationLegalEntity vendor;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private State state;
	
	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_ArticleContainer_states"
	 *
	 * @jdo.join
	 */
	private List<State> states;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String articleContainerIDPrefix;	
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long articleContainerID;	
//	/**
//	* @jdo.field persistence-modifier="persistent"
//	*/
//	private Date changeDT;
//
//	/**
//	* @jdo.field persistence-modifier="persistent"
//	*/
//	private User changeUser;		
//	
//	/**
//	 * @return Returns the changeDT.
//	 */
//	public Date getChangeDT()
//	{
//		return changeDT;
//	}
//	
//	/**
//	 * @return Returns the changeUser.
//	 */
//	public User getChangeUser()
//	{
//		return changeUser;
//	}

	/**
	 * @deprecated This constructor exists only for JDO!
	 */	
	protected AbstractArticleContainer() {
		super();
	}
	
	/**
	 * 
	 * @param creator the {@link User} which created this ArticleContainer
	 * @param vendor the {@link OrganisationLegalEntity} which created the {@link ArticleContainer}
	 * @param customer the {@link LegalEntity} which recieves teh {@link ArticleContainer}
	 * @param articleContainerIDPrefix teh id Prefix of the ArticleContainer
	 * @param articleContainerID the ID of the ArticleContainer
	 */
	public AbstractArticleContainer(User creator, OrganisationLegalEntity vendor, 
			LegalEntity customer, String articleContainerIDPrefix, long articleContainerID)
	{
		if (creator == null)
			throw new IllegalArgumentException("creator must not be null!");

		if (vendor == null)
			throw new IllegalArgumentException("vendor must not be null!");

		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		ObjectIDUtil.assertValidIDString(articleContainerIDPrefix, "articleContainerIDPrefix");
		
		if (articleContainerID < 0)
			throw new IllegalArgumentException("articleContainerID < 0");
		
		this.organisationID = vendor.getOrganisationID();
		this.articleContainerIDPrefix = articleContainerIDPrefix;
		this.articleContainerID = articleContainerID;
		this.createDT = new Date(System.currentTimeMillis());
		this.createUser = creator;
		this.vendor = vendor;
		this.customer = customer;
		articles = new HashSet<Article>();		
		states = new ArrayList<State>();
	}
	
	/**
	 * @return the createDT
	 */
	public Date getCreateDT()
	{
		return createDT;
	}
	
	/**
	 * @return the createUser
	 */
	public User getCreateUser()
	{
		return createUser;
	}	
		
	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = (AnchorID) JDOHelper.getObjectId(vendor);

		return vendorID;
	}

	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = (AnchorID) JDOHelper.getObjectId(customer);

		return customerID;
	}	
	
	public void addArticle(Article article)
	throws ArticleContainerException
	{		
		articles.add(article);
		for (ArticleContainerListener listener : getListeners()) {
			listener.articleAdded(article);
			listener.articlesChanged(this);
		}		
	}

	public void removeArticle(Article article)
	throws ArticleContainerException
	{
		articles.remove(article);
		for (ArticleContainerListener listener : getListeners()) {
			listener.articleRemoved(article);
			listener.articlesChanged(this);
		}		
	}
	
	public Collection<Article> getArticles() {
		return articles;
	}
		
	/**
	 * @jdo.field persistence-modifier="none"
	 */	
	private transient Set<ArticleContainerListener> listeners;	
	
	protected Collection<ArticleContainerListener> getListeners() {
		if (listeners == null) {
			listeners = new HashSet<ArticleContainerListener>();
		}
		return listeners;
	}
	
	public void addArticleContainerListener(ArticleContainerListener listener) {
		getListeners().add(listener);
	}
	
	public void removeArticleContainerListener(ArticleContainerListener listener) {
		getListeners().remove(listener);
	}
	
//	protected void notifyListeners() {
//		for (ArticleContainerListener listener : getListeners()) {
//			listener.articlesChanged(this);
//		}
//	}	
	
	/**
	 * This method is <b>not</b> intended to be called directly. It is called by
	 * {@link State#State(String, long, User, Statable, org.nightlabs.jfire.jbpm.graph.def.StateDefinition)}
	 * which is called automatically by {@link ActionHandlerNodeEnter}, if this <code>ActionHandler</code> is registered.
	 */
	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		if (!currentState.getStateDefinition().isPublicState())
			throw new IllegalArgumentException("state.stateDefinition.publicState is false!");

		this.state = (State)currentState;
		this.states.add((State)currentState);
	}

	public State getState()
	{
		return state;
	}

	public List<State> getStates()
	{
		if (_states == null)
			_states = Collections.unmodifiableList(states);

		return _states;
	}	
}
