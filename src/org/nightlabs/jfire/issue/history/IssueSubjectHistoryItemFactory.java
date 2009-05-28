package org.nightlabs.jfire.issue.history;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueSubjectHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueSubjectHistoryItemFactory extends I18nTextIssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.I18nTextIssueHistoryItemFactory#createIssueHistoryItem(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected IssueHistoryItem createIssueHistoryItem(User user, Issue issue, String languageID, String oldValue, String newValue) {
		return new IssueSubjectHistoryItem(user, issue, languageID, oldValue, newValue);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.I18nTextIssueHistoryItemFactory#getI18nTextField(org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	protected I18nText getI18nTextField(Issue issue) {
		return issue.getSubject();
	}

}
