package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * An {@link IssueHistoryItemFactory} to deal directly with {@link I18nText}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public abstract class I18nTextIssueHistoryItemFactory extends IssueHistoryItemFactory {
	/**
	 * @param issue the issue from which to obtain the {@link I18nText} property. This is once the persistent issue and once the detached issue (this method is called multiple times).
	 * @return the {@link I18nText} reference from the given {@link Issue}.
	 */
	protected abstract I18nText getI18nTextField(Issue issue);

	/**
	 * Define the specific method to construct an {@link IssueHistoryItem} corresponding to this part of the {@link Issue}.
	 */
	protected abstract IssueHistoryItem createIssueHistoryItem(User user, Issue issue, String languageID, String oldValue, String newValue);


	// -------------------------------------------------------------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		I18nText oldText = getI18nTextField(oldPersistentIssue);
		I18nText newText = getI18nTextField(newDetachedIssue);

		// Note: Initially, when there exists no texts, there is nothing to compare with.
		// So first gather a unique set of languages between the old and new.
		Set<String> langIDs = oldText.getLanguageIDs();
		if (langIDs == null || langIDs.isEmpty()) langIDs = newText.getLanguageIDs();
		else {
			langIDs = new HashSet<String>(langIDs);
			for (String langID : newText.getLanguageIDs())
				if ( !langIDs.contains(langID) ) langIDs.add(langID);
		}

		// Check to see if any parts of the Text has been modified.
		Collection<IssueHistoryItem> issueHistoryItems = new ArrayList<IssueHistoryItem>();
		if (langIDs != null) {
			for (String langID : langIDs)
				if ( !oldText.containsLanguageID(langID) || !newText.containsLanguageID(langID) || !oldText.getText(langID, false).equals(newText.getText(langID, false))) {
					String oldValue = oldText.getText(langID, false);
					if (oldValue == null)
						oldValue = "";

					String newValue = newText.getText(langID, false);
					if (newValue == null)
						newValue = "";

					issueHistoryItems.add( createIssueHistoryItem(user, oldPersistentIssue, langID, oldValue, newValue) );
				}
		}

		return issueHistoryItems;
	}

}
