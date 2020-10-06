import Form, { CheckboxField } from "@atlaskit/form";
import React from "react";
import ModalDialog, { ModalFooter } from "@atlaskit/modal-dialog";
import Button from "@atlaskit/button";
import { AutoTTDto } from "../../dto";
import { Checkbox } from "@atlaskit/checkbox";
import { IdField, ProjectIssueField, UserField } from "../fields";
import WorklogTypeField from "../fields/WorklogTypeField";

interface AutoTimeTrackingDialogProps {
  data: AutoTTDto;
  heading: string;
  onClose: () => void;
  onSubmit: (data: AutoTTDto) => void;
}

const AutoTTDialog: React.FC<AutoTimeTrackingDialogProps> = ({
  data,
  heading,
  onClose,
  onSubmit,
}) => {
  const footer = (props: { showKeyline?: boolean }) => (
    <ModalFooter showKeyline={props.showKeyline}>
      <span />
      <Button appearance="primary" type="submit">
        {data.id ? "Update" : "Create"}
      </Button>
    </ModalFooter>
  );

  return (
    <ModalDialog
      heading={heading}
      onClose={onClose}
      components={{
        Container: ({ children, className }) => (
          <Form onSubmit={onSubmit}>
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
      <WorklogTypeField
        label="Worklog type"
        name="worklogType"
        value={data.worklogType}
      />
      <CheckboxField name="active" defaultIsChecked={data.active}>
        {({ fieldProps }) => <Checkbox {...fieldProps} label="Active" />}
      </CheckboxField>
    </ModalDialog>
  );
};

export default AutoTTDialog;
