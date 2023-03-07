
package com.scn.jira.worklog.workflow;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.impl.NumberCFType;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateScnWorklogFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    private final CustomFieldManager customFieldManager;
    private final ExtendedConstantsManager extendedConstantsManager;
    public static final String PARAM_FIELD_ID = "fieldId";
    public static final String PARAM_WORKLOG_TYPE_ID = "worklogTypeId";
    public static final String TARGET_FIELD_NAME = "field.name";
    public static final String TARGET_WORKLOG_TYPE_ID = "worklogtype.id";

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put("fields", customFieldManager.getCustomFieldObjects().stream()
            .filter(customField -> customField.getCustomFieldType() instanceof NumberCFType)
            .collect(Collectors.toList()));
        velocityParams.put("worklogTypes", extendedConstantsManager.getWorklogTypeObjects());
        velocityParams.put("factory", this);
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        this.getVelocityParamsForInput(velocityParams);
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        } else {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
            String fieldName = (String) functionDescriptor.getArgs().get(TARGET_FIELD_NAME);
            String worklogTypeId = (String) functionDescriptor.getArgs().get(TARGET_WORKLOG_TYPE_ID);
            velocityParams.put(PARAM_FIELD_ID, fieldName);
            velocityParams.put(PARAM_WORKLOG_TYPE_ID, worklogTypeId);
        }
    }

    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        } else {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
            String fieldName = (String) functionDescriptor.getArgs().get(TARGET_FIELD_NAME);
            Field field = this.customFieldManager.getCustomFieldObject(fieldName);
            velocityParams.put(PARAM_FIELD_ID, field.getNameKey());
        }
    }

    public Map<String, String> getDescriptorParams(Map<String, Object> conditionParams) {
        Map<String, String> params = new HashMap<>();
        String fieldId = this.extractSingleParam(conditionParams, PARAM_FIELD_ID);
        params.put(TARGET_FIELD_NAME, fieldId);
        String worklogTypeId = this.extractSingleParam(conditionParams, PARAM_WORKLOG_TYPE_ID);
        params.put(TARGET_WORKLOG_TYPE_ID, worklogTypeId);
        return params;
    }

}
