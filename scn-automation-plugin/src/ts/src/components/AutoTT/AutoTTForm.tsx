import Button from "@atlaskit/button";
import { Checkbox } from "@atlaskit/checkbox";
import Form, { CheckboxField, ErrorMessage, Field } from "@atlaskit/form";
import ModalDialog, {
  ModalFooter,
  ModalTransition,
} from "@atlaskit/modal-dialog";
import React from "react";
import { AutoTTDto } from "../../models";
import { IdField, ProjectIssueField, UserField } from "../fields";
import WorklogTypeField from "../fields/WorklogTypeField";
import Textfield from "@atlaskit/textfield";

interface ComponentProps {
  data: AutoTTDto;
  onSubmit(data: AutoTTDto): void | Object;
  onClose(): void;
}

const AutoTTForm: React.FC<ComponentProps> = ({ data, onSubmit, onClose }) => {
  const footer = (props: { showKeyline?: boolean }) => (
    <ModalFooter showKeyline={props.showKeyline}>
      <span />
      <div>
        <Button appearance="primary" type="submit">
          {data.id ? "Update" : "Create"}
        </Button>
        <Button appearance="link" onClick={onClose}>
          Cancel
        </Button>
      </div>
    </ModalFooter>
  );

  return (
    <ModalTransition>
      {data && (
        <ModalDialog
          heading={
            data.id ? "Edit auto time tracking" : "Create auto time tracking"
          }
          onClose={onClose}
          components={{
            Container: ({ children, className }) => (
              <Form<AutoTTDto> onSubmit={onSubmit}>
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
          <IdField value={data.id ? data.id : 0} />
          <UserField label="User" name="user" value={data.user}></UserField>
          <ProjectIssueField
            label="Issue"
            name="issue"
            issue={data.issue}
            project={data.project}
          />
          <Field<string>
            label="Rated Time"
            name="ratedTime"
            isRequired={true}
            defaultValue={data.ratedTime}
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
            value={data.worklogType}
          />
          <CheckboxField name="active" defaultIsChecked={data.active}>
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
