import { Field } from "@atlaskit/form";
import Select from "@atlaskit/select";
import React, { useContext, useEffect, useState } from "react";
import { getAllWorklogTypes } from "../../api";
import { WorklogTypeDto } from "../../models";
import { FlagContext } from "../../services/flag/flagContext";

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
  const { addError } = useContext(FlagContext);

  useEffect(() => {
    let isMounted = true;
    getAllWorklogTypes()
      .then(({ data }) => {
        if (isMounted) {
          setOptions(
            data.map((value) => ({
              id: value.id,
              name: value.name,
            }))
          );
        }
      })
      .catch(({ message }) => addError(message));
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <Field<WorklogTypeDto> label={label} name={name} defaultValue={value}>
      {({ fieldProps: { id, ...rest } }) => (
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
      )}
    </Field>
  );
};

export default WorklogTypeField;
