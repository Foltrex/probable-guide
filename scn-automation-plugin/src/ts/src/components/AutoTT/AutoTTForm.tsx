import Button from "@atlaskit/button";
import { Checkbox } from "@atlaskit/checkbox";
import Form, { CheckboxField, ErrorMessage, Field } from "@atlaskit/form";
import ModalDialog, {
  ModalFooter,
  ModalTransition,
} from "@atlaskit/modal-dialog";
import React, { useContext } from "react";
import { AutoTTDto } from "../../models";
import { AutoTTContext } from "../../services/autoTT/autoTTContext";
import { IdField, ProjectIssueField, UserField } from "../fields";
import WorklogTypeField from "../fields/WorklogTypeField";
import Textfield from "@atlaskit/textfield";

const AutoTTForm = () => {
  const { formData, updateForm, addAutoTT, updateAutoTT } = useContext(
    AutoTTContext
  );

  const footer = (props: { showKeyline?: boolean }) => (
    <ModalFooter showKeyline={props.showKeyline}>
      <span />
      <Button appearance="primary" type="submit">
        {formData.id ? "Update" : "Create"}
      </Button>
    </ModalFooter>
  );

  return (
    <ModalTransition>
      {formData && (
        <ModalDialog
          heading={
            formData.id
              ? "Edit auto time tracking"
              : "Create auto time tracking"
          }
          onClose={updateForm.bind(this, null)}
          components={{
            Container: ({ children, className }) => (
              <Form<AutoTTDto>
                onSubmit={formData.id ? updateAutoTT : addAutoTT}
              >
                {({ formProps }) => (
                  <form {...formProps} className={className}>
                    {children}
                  </form>
                )}
              </Form>
            ),
            Footer: footer,
          }}
        >
          <IdField value={formData.id ? formData.id : 0} />
          <UserField label="User" name="user" value={formData.user}></UserField>
          <ProjectIssueField
            label="Issue"
            name="issue"
            issue={formData.issue}
            project={formData.project}
          />
          <Field<string>
            label="Rated Time"
            name="ratedTime"
            isRequired={true}
            defaultValue={formData.ratedTime}
          >
            {({ fieldProps: { isRequired, ...rest }, error }) => (
              <>
                <Textfield {...rest} />
                {error && <ErrorMessage>{error}</ErrorMessage>}
              </>
            )}
          </Field>

          <WorklogTypeField
            label="Worklog type"
            name="worklogType"
            value={formData.worklogType}
          />
          <CheckboxField name="active" defaultIsChecked={formData.active}>
            {({ fieldProps: { ...rest } }) => (
              <Checkbox {...rest} label="Active" />
            )}
          </CheckboxField>
        </ModalDialog>
      )}
    </ModalTransition>
  );
};

export default AutoTTForm;
