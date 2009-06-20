package org.nightlabs.jfire.dynamictrade.template;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.dynamictrade.template.id.DynamicProductTemplateID;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=DynamicProductTemplateID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDynamicTrade_DynamicProductTemplate"
)
@FetchGroups({
	@FetchGroup(
			name=DynamicProductTemplate.FETCH_GROUP_NAME,
			members={@Persistent(name="name")}
	),
	@FetchGroup(
			name=DynamicProductTemplate.FETCH_GROUP_PRODUCT_NAME,
			members={@Persistent(name="productName")}
	),
	@FetchGroup(
			name=DynamicProductTemplate.FETCH_GROUP_PARENT_CATEGORY,
			members={@Persistent(name="parentCategory")}
	),
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries({
	@Query(name="getChildDynamicProductTemplates", value="SELECT WHERE this.parentCategory == :parentCategory"),
})
public class DynamicProductTemplate
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DynamicProductTemplate.name";
	public static final String FETCH_GROUP_PRODUCT_NAME = "DynamicProductTemplate.productName";
	public static final String FETCH_GROUP_PARENT_CATEGORY = "DynamicProductTemplate.parentCategory";
	public static final String FETCH_GROUP_PARENT_CATEGORY_ID = "DynamicProductTemplate.parentCategoryID";

	public static Collection<DynamicProductTemplate> getChildDynamicProductTemplates(PersistenceManager pm, DynamicProductTemplate parentCategory)
	{
		javax.jdo.Query q = pm.newNamedQuery(DynamicProductTemplate.class, "getChildDynamicProductTemplates");

		@SuppressWarnings("unchecked")
		Collection<DynamicProductTemplate> c = (Collection<DynamicProductTemplate>) q.execute(parentCategory);
		return c;
	}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dynamicProductTemplateID;

	private boolean isCategory;

	private DynamicProductTemplate parentCategory;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private DynamicProductTemplateID parentCategoryID;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean parentCategoryID_detached;

	@Persistent(mappedBy="dynamicProductTemplate")
	private DynamicProductTemplateName name;

	@Persistent(mappedBy="dynamicProductTemplate")
	private DynamicProductTemplateProductName productName;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTemplate() { }

	public DynamicProductTemplate(DynamicProductTemplate parentCategory, boolean isCategory) {
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(DynamicProductTemplate.class),
				parentCategory,
				isCategory
		);
	}

	public DynamicProductTemplate(String organisationID, long dynamicProductTemplateID, DynamicProductTemplate parentCategory, boolean isCategory) {
		this.organisationID = organisationID;
		this.dynamicProductTemplateID = dynamicProductTemplateID;
		this.parentCategory = parentCategory;
		this.isCategory = isCategory;
		this.name = new DynamicProductTemplateName(this);

		if (!isCategory)
			this.productName = new DynamicProductTemplateProductName(this);
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getDynamicProductTemplateID() {
		return dynamicProductTemplateID;
	}

	public boolean isCategory() {
		return isCategory;
	}

	public DynamicProductTemplate getParentCategory() {
		return parentCategory;
	}

	public DynamicProductTemplateID getParentCategoryID() {
		if (parentCategoryID_detached)
			return parentCategoryID;

		if (parentCategoryID == null)
			parentCategoryID = (DynamicProductTemplateID) JDOHelper.getObjectId(parentCategory);

		return parentCategoryID;
	}

	/**
	 * Get the name of this template.
	 *
	 * @return the name of this template.
	 */
	public DynamicProductTemplateName getName() {
		return name;
	}

	/**
	 * Get the template text for the product name.
	 *
	 * @return the template text for the product name.
	 */
	public DynamicProductTemplateProductName getProductName() {
		return productName;
	}

	@Override
	public void jdoPostDetach(Object o)
	{
		DynamicProductTemplate attached = (DynamicProductTemplate) o;
		DynamicProductTemplate detached = this;

		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		Set<?> fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_PARENT_CATEGORY_ID)) {
			detached.parentCategoryID = attached.getParentCategoryID();
			detached.parentCategoryID_detached = true;
		}
	}

	@Override
	public void jdoPreDetach() {
	}
}
