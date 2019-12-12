package com.scn.jira.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@Scanned
public class UpdatedWithoutHistoryFunction extends AbstractJqlFunction {
    private final OfBizDelegator ofBizDelegator;
    private final JqlDateSupport jqlDateSupport;
    private static final String ENTITY_ISSUE = "Issue";

    public UpdatedWithoutHistoryFunction(OfBizDelegator ofBizDelegator, JqlDateSupport jqlDateSupport) {
        this.ofBizDelegator = ofBizDelegator;
        this.jqlDateSupport = jqlDateSupport;
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSet messageSet = new MessageSetImpl();
        List<String> args = functionOperand.getArgs();
        if (applicationUser == null) {
            messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(this.getI18n().getText("jira.jql.function.anonymous.disallowed", this.getFunctionName()));
            return messageSet;
        } else if (args.size() <= 2) {
            return messageSet;
        } else {
            messageSet.addErrorMessage(this.getI18n().getText("updated-without-history-function.invalid.argument.number", this.getFunctionName()));
            return messageSet;
        }
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return Collections.emptyList();
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 0;
    }

    @Nonnull
    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    protected boolean validateDate(MessageSet messageSet, String dateString, String i18nKey) {
        if (dateString != null && dateString.length() != 0 && this.jqlDateSupport.validate(dateString)) {
            return true;
        } else {
            messageSet.addErrorMessage(this.getI18n().getText(i18nKey, this.getFunctionName()));
            return false;
        }
    }
}
