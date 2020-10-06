import { Field } from "@atlaskit/form";
import { AsyncSelect } from "@atlaskit/select";
import React, { useEffect, useState } from "react";
import { getAllProjects } from "../../api";
import { ProjectDto } from "../../dto";

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
  const [currentValue, setCurrentValue] = useState<ProjectDto>(value);
  const [options, setOptions] = useState<ProjectDto[]>([]);

  useEffect(() => {
    let isMounted = true;
    getAllProjects().then(({ data }) => {
      if (isMounted) {
        setOptions(
          data.map((value) => ({
            id: value.id,
            key: value.key,
            name: value.name,
          }))
        );
      }
    });
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
        isDisabled={true}
        label={label}
        name={name}
        defaultValue={currentValue}
      >
        {({}) => <></>}
      </Field>
      <AsyncSelect
        menuPosition={"fixed"}
        value={currentValue}
        onChange={(value: ProjectDto) => {
          setCurrentValue(value);
          onChange(value);
        }}
        loadOptions={loadOptions}
        id={"project-select"}
        className="single-select"
        classNamePrefix="react-select"
        getOptionLabel={(project: ProjectDto) =>
          `${project.name} (${project.key})`
        }
        getOptionValue={(project: ProjectDto) => project.id.toString()}
      />
    </>
  );
};

export default ProjectField;
