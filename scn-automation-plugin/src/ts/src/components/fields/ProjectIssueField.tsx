import { ErrorMessage, Field } from "@atlaskit/form";
import { AsyncSelect } from "@atlaskit/select";
import React, { useState } from "react";
import { request } from "../../api";
import Config from "../../config";
import { IssueDto, ProjectDto } from "../../models";
import { useFlagService } from "../../services/FlagService";
import ProjectField from "./ProjectField";

interface ProjectIssueFieldProps {
  issue: IssueDto;
  label: string;
  name: string;
  project: ProjectDto;
}

const ProjectIssueField: React.FC<ProjectIssueFieldProps> = ({
  issue,
  label,
  name,
  project,
}) => {
  const [currentProject, setCurrentProject] = useState<ProjectDto>(project);
  const [currentIssue, setCurrentIssue] = useState<IssueDto>(issue);
  const { showError } = useFlagService();

  const loadOptions = (query: string) =>
    request<{ sections: { id: "hs" | "cs"; issues: any[] }[] }>({
      url:
        `${Config.JIRA_API}/issue/picker?currentJQL=project=${currentProject.id} order by lastViewed DESC` +
        `&showSubTasks=true&showSubTaskParent=true&query=${query}`,
      method: "GET",
    })
      .then(({ data }) =>
        data.sections
          .filter((section) => section.id === "cs")
          .flatMap((value) => value.issues)
          .map((value) => ({
            key: value.key,
            name: value.summaryText,
            displayHtml: `${value.summary} (${value.keyHtml})`,
          }))
      )
      .catch(({ message }) => showError(message));

  const onProjectChange = (value: ProjectDto) => {
    if (!currentProject || !value || value.id != currentProject.id) {
      setCurrentIssue(null);
    }
    setCurrentProject(value);
  };

  return (
    <>
      <ProjectField
        label="Project"
        name="project"
        value={currentProject}
        onChange={onProjectChange}
      />
      <Field<IssueDto>
        isRequired={true}
        isDisabled={!currentProject}
        label={label}
        name={name}
        defaultValue={currentIssue}
      >
        {({ fieldProps, error }) => (
          <>
            <AsyncSelect
              {...fieldProps}
              menuPosition={"fixed"}
              onChange={(value: IssueDto) =>
                setCurrentIssue({ ...value, displayHtml: null })
              }
              loadOptions={loadOptions}
              className="single-select"
              classNamePrefix="react-select"
              getOptionLabel={(issue) => issue.name}
              getOptionValue={(issue) => issue.key}
              formatOptionLabel={(issue) => (
                <>
                  {issue.displayHtml && (
                    <div
                      dangerouslySetInnerHTML={{ __html: issue.displayHtml }}
                    />
                  )}
                  {!issue.displayHtml && `${issue.name} (${issue.key})`}
                </>
              )}
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
          </>
        )}
      </Field>
    </>
  );
};

export default ProjectIssueField;
