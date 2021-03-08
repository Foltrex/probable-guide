package com.scn.jira.worklog.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class RecentProjectFunction extends AbstractJqlFunction {
    private final UserProjectHistoryManager userProjectHistoryManager;

    public RecentProjectFunction(UserProjectHistoryManager userProjectHistoryManager) {
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser searcher, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        return validateNumberOfArgs(operand, 0);
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand,
                                        @Nonnull TerminalClause terminalClause) {
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

    @Nonnull
    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.PROJECT;
    }

    @Override
    public boolean isList() {
        return super.isList();
    }
}
