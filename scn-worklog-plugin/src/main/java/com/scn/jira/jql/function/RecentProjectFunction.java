package com.scn.jira.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.LinkedList;
import java.util.List;

@Scanned
public class RecentProjectFunction extends AbstractJqlFunction {
	@ComponentImport
	private final UserProjectHistoryManager userProjectHistoryManager;

	public RecentProjectFunction(UserProjectHistoryManager userProjectHistoryManager) {
		this.userProjectHistoryManager = userProjectHistoryManager;
	}

	@Override
	public MessageSet validate(ApplicationUser searcher, FunctionOperand operand, TerminalClause terminalClause) {
		return validateNumberOfArgs(operand, 0);
	}

	@Override
	public List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, FunctionOperand operand,
			TerminalClause terminalClause) {
		final List<QueryLiteral> literals = new LinkedList<>();
		final List<UserHistoryItem> projects = userProjectHistoryManager
				.getProjectHistoryWithoutPermissionChecks(queryCreationContext.getApplicationUser());
		for (final UserHistoryItem userHistoryItem : projects) {
			final String value = userHistoryItem.getEntityId();
			literals.add(new QueryLiteral(operand, Long.parseLong(value)));
		}

		return literals;
	}

	@Override
	public int getMinimumNumberOfExpectedArguments() {
		return 0;
	}

	@Override
	public JiraDataType getDataType() {
		return JiraDataTypes.PROJECT;
	}
}