import { ErrorMessage, Field } from "@atlaskit/form";
import { AsyncSelect } from "@atlaskit/select";
import React, { useEffect, useState } from "react";
import { request } from "../../api";
import Config from "../../config";
import { ProjectDto } from "../../models";
import { useFlagService } from "../../services/FlagService";

interface ProjectFieldProps {
  value: ProjectDto;
  label: string;
  name: string;
  onChange?(value: ProjectDto): void;
}

const ProjectField: React.FC<ProjectFieldProps> = ({
  value,
  label,
  name,
  onChange,
}) => {
  const [options, setOptions] = useState<ProjectDto[]>([]);
  const { showError } = useFlagService();

  useEffect(() => {
    let isMounted = true;
    request<any[]>({ url: `${Config.JIRA_API}/project`, method: "GET" })
      .then(({ data }) => {
        if (isMounted) {
          setOptions(
            data.map((value) => ({
              id: value.id,
              key: value.key,
              name: value.name,
            }))
          );
        }
      })
      .catch(({ message }) => showError(message));
    return () => {
      isMounted = false;
    };
  }, []);

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

  return (
    <>
      <Field<ProjectDto>
        isRequired={true}
        label={label}
        name={name}
        defaultValue={value}
      >
        {({ fieldProps, error }) => (
          <>
            <AsyncSelect
              {...fieldProps}
              menuPosition={"fixed"}
              onChange={onChange}
              loadOptions={loadOptions}
              className="single-select"
              classNamePrefix="react-select"
              getOptionLabel={(project: ProjectDto) =>
                `${project.name} (${project.key})`
              }
              getOptionValue={(project: ProjectDto) => project.id.toString()}
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
          </>
        )}
      </Field>
    </>
  );
};

export default ProjectField;
