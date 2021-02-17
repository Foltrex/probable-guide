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
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.*;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Scanned
public class IssuesWhereScnWorklogDateFunction extends AbstractJqlFunction {
    private final OfBizDelegator ofBizDelegator;
    private final JqlDateSupport jqlDateSupport;
    private static final String ENTITY_SCN_WORKLOG = "ScnWorklog";
    private static final Map<String, EntityOperator> ENTITY_OPERATOR_MAP = new HashMap<>() {
        {
            this.put("=", EntityOperator.EQUALS);
            this.put("<>", EntityOperator.NOT_EQUAL);
            this.put("<", EntityOperator.LESS_THAN);
            this.put(">", EntityOperator.GREATER_THAN);
            this.put("<=", EntityOperator.LESS_THAN_EQUAL_TO);
            this.put(">=", EntityOperator.GREATER_THAN_EQUAL_TO);
        }
    };

    public IssuesWhereScnWorklogDateFunction(OfBizDelegator ofBizDelegator,
                                             JqlDateSupport jqlDateSupport) {
        this.ofBizDelegator = ofBizDelegator;
        this.jqlDateSupport = jqlDateSupport;
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand,
                               @Nonnull TerminalClause terminalClause) {
        MessageSet messageSet = new MessageSetImpl();
        List<String> args = functionOperand.getArgs();
        if (applicationUser == null) {
            messageSet.addErrorMessage(
                this.getI18n().getText("jira.jql.function.anonymous.disallowed", this.getFunctionName())
            );
        } else if (args.size() == 2) {
            String operator = args.get(0);
            if (ENTITY_OPERATOR_MAP.get(operator) == null) {
                messageSet.addErrorMessage(
                    this.getI18n().getText("scn.worklog.jql.function.invalid.comparator", operator)
                );
            }
            String dateString = args.get(1);
            this.validateDate(messageSet, dateString);
        } else {
            messageSet.addErrorMessage(
                this.getI18n().getText("issues-where-scn-worklog-date-function.invalid.argument.number")
            );
        }

        return messageSet;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext,
                                        @Nonnull FunctionOperand functionOperand,
                                        @Nonnull TerminalClause terminalClause) {
        List<String> args = functionOperand.getArgs();
        Date date = this.getDateArgSafely(args);
        List<GenericValue> issuesGVs = ofBizDelegator.findByCondition(ENTITY_SCN_WORKLOG,
            new EntityExpr("startdate", ENTITY_OPERATOR_MAP.get(args.get(0)), new Timestamp(date.getTime())),
            ImmutableList.of("issue")
        );
        return issuesGVs.stream()
            .mapToLong(value -> value.getLong("issue"))
            .distinct()
            .mapToObj(value -> new QueryLiteral(functionOperand, value))
            .collect(Collectors.toList());
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 2;
    }

    @Nonnull
    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    private void validateDate(MessageSet messageSet, String dateString) {
        if (dateString == null || dateString.length() == 0 || !this.jqlDateSupport.validate(dateString)) {
            messageSet.addErrorMessage(this.getI18n().getText("scn.worklog.jql.function.invalid.date"));
        }
    }

    @Nonnull
    private Date getDateArgSafely(@Nonnull List<String> args) {
        Date date = new Date();
        final int DATE_INDEX = 1;
        if (args.size() > DATE_INDEX) {
            date = this.jqlDateSupport.convertToDate(args.get(DATE_INDEX));
        }
        return date;
    }
}
