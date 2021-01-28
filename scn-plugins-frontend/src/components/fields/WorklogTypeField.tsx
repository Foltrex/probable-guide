import { ErrorMessage, Field } from "@atlaskit/form";
import Select from "@atlaskit/select";
import React, { useEffect, useState } from "react";
import { request } from "../../api";
import Config from "../../config";
import { WorklogTypeDto } from "../../models";
import { useFlagService } from "../../services/FlagService";

interface WorklogTypeFieldProps {
  value: WorklogTypeDto;
  label: string;
  name: string;
}

const WorklogTypeField: React.FC<WorklogTypeFieldProps> = ({
  value,
  label,
  name,
}) => {
  const [options, setOptions] = useState<WorklogTypeDto[]>([]);
  const { showError } = useFlagService();

  useEffect(() => {
    let isMounted = true;
    request<WorklogTypeDto[]>({
      url: `${Config.API}/worklog/type`,
      method: "GET",
    })
      .then(({ data }) => {
        if (isMounted) {
          setOptions(data);
        }
      })
      .catch(({ message }) => showError(message));
    return () => {
      isMounted = false;
    };
  }, [value]);

  return (
    <Field<WorklogTypeDto> label={label} name={name} defaultValue={value}>
      {({ fieldProps: { id, ...rest }, error }) => (
        <>
          <Select
            menuPosition={"fixed"}
            isClearable={true}
            options={options}
            id={`${id}-select`}
            className="single-select"
            classNamePrefix="react-select"
            getOptionLabel={(value: WorklogTypeDto) => value.name}
            getOptionValue={(value: WorklogTypeDto) => value.id}
            {...rest}
          />
          {error && <ErrorMessage>{error}</ErrorMessage>}
        </>
      )}
    </Field>
  );
};

export default WorklogTypeField;
