package com.scn.jira.jql.function;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.DateRange;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Scanned
public class UpdatedWithoutHistoryFunction extends AbstractJqlFunction {
    private final OfBizDelegator ofBizDelegator;
    private final JqlDateSupport jqlDateSupport;
    private static final String ENTITY_ISSUE = "Issue";

    public UpdatedWithoutHistoryFunction(@ComponentImport OfBizDelegator ofBizDelegator,
                                         @ComponentImport JqlDateSupport jqlDateSupport) {
        this.ofBizDelegator = Assertions.notNull("ofBizDelegator", ofBizDelegator);
        this.jqlDateSupport = Assertions.notNull("jqlDateSupport", jqlDateSupport);
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        MessageSet messageSet = new MessageSetImpl();
        List<String> args = operand.getArgs();
        if (applicationUser == null) {
            messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(this.getI18n().getText("jira.jql.function.anonymous.disallowed", this.getFunctionName()));
            return messageSet;
        } else if (args.size() <= 2) {
            if (args.size() > 0) {
                String dateFromString = args.get(0);
                if (this.validateDate(messageSet, dateFromString, "jira.jql.function.updated.by.invalid.date.from") && args.size() > 1) {
                    String dateToString = args.get(1);
                    if (this.validateDate(messageSet, dateToString, "jira.jql.function.updated.by.invalid.date.to")) {
                        Date dateFrom = this.jqlDateSupport.convertToDate(dateFromString);
                        Date dateTo = this.jqlDateSupport.convertToDate(dateToString);
                        if (dateTo.before(dateFrom)) {
                            messageSet.addErrorMessage(this.getI18n().getText("jira.jql.function.updated.by.invalid.date.range", this.getFunctionName()));
                        }
                    }
                }
            }
            return messageSet;
        } else {
            messageSet.addErrorMessage(this.getI18n().getText("updated-without-history-function.invalid.argument.number", this.getFunctionName()));
            return messageSet;
        }
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        System.out.println("Getting started!");
        List<String> args = operand.getArgs();
        DateRange fromDate = this.getDateArgSafely(args, 0);
        DateRange toDate = this.getDateArgSafely(args, 1);
        List<EntityCondition> conditions = new ArrayList<EntityCondition>();
        conditions.add(new EntityExpr("updated", EntityOperator.GREATER_THAN_EQUAL_TO, new java.sql.Date(0)));
//        conditions.add(new EntityExpr("updated", EntityOperator.LESS_THAN_EQUAL_TO, new java.sql.Date(9223372036854775807L)));
        List<GenericValue> issuesGVs = ofBizDelegator.findByCondition(ENTITY_ISSUE, new EntityConditionList(conditions, EntityOperator.AND), ImmutableList.of("id"));
        System.out.println("End query!");
        System.out.println(issuesGVs.size());

        return issuesGVs.stream().map(v -> new QueryLiteral(operand, v.getLong("id"))).collect(Collectors.toList());
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

    private boolean validateDate(MessageSet messageSet, String dateString, String i18nKey) {
        if (dateString != null && dateString.length() != 0 && this.jqlDateSupport.validate(dateString)) {
            return true;
        } else {
            messageSet.addErrorMessage(this.getI18n().getText(i18nKey, this.getFunctionName()));
            return false;
        }
    }

    private DateRange getDateArgSafely(List<String> args, int index) {
        DateRange dateRange = null;
        if (args.size() > index) {
            dateRange = this.jqlDateSupport.convertToDateRangeWithImpliedPrecision(args.get(index));
        }

        return dateRange;
    }
}
