import { ErrorMessage, Field } from "@atlaskit/form";
import { AsyncSelect } from "@atlaskit/select";
import React, { useContext, useEffect, useState } from "react";
import { getIssuesByProjectId } from "../../api";
import { IssueDto, ProjectDto } from "../../models";
import { FlagContext } from "../../services/flag/flagContext";
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
  const [options, setOptions] = useState<IssueDto[]>([]);
  const { showError } = useContext(FlagContext);

  useEffect(() => {
    let isMounted = true;
    if (currentProject) {
      getIssuesByProjectId(currentProject.id)
        .then(({ data }) => {
          if (isMounted) {
            setOptions(
              data.issues.map((value) => ({
                id: value.id,
                key: value.key,
                name: value.fields.summary,
              }))
            );
          }
        })
        .catch(({ message }) => showError(message));
    }
    return () => {
      isMounted = false;
    };
  }, [currentProject]);

  const loadOptions = (query: string) =>
    new Promise((resolve) =>
      resolve(
        options.filter(
          (value) =>
            value.name.toLowerCase().includes(query.toLowerCase()) ||
            value.key.toLowerCase().includes(query.toLowerCase())
        )
      )
    );

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
              onChange={(value: IssueDto) => setCurrentIssue(value)}
              loadOptions={loadOptions}
              className="single-select"
              classNamePrefix="react-select"
              getOptionLabel={(issue: IssueDto) =>
                `${issue.name} (${issue.key})`
              }
              getOptionValue={(issue: IssueDto) => issue.id.toString()}
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
          </>
        )}
      </Field>
    </>
  );
};

export default ProjectIssueField;
