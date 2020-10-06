import { Field } from "@atlaskit/form";
import React from "react";
import { UserDto } from "../../dto";
import { AsyncSelect } from "@atlaskit/select";
import { searchUsers } from "../../api";

interface UserFieldProps {
  value: UserDto;
  label: string;
  name: string;
}

const UserField: React.FC<UserFieldProps> = ({ value, label, name }) => {
  const loadOptions = (query: string) =>
    searchUsers(query).then(({ data: { users } }) =>
      users.map((value: { key: string; displayName: string }) => ({
        key: value.key,
        name: value.displayName,
      }))
    );

  return (
    <Field<UserDto>
      isRequired={true}
      label={label}
      name={name}
      defaultValue={value}
    >
      {({ fieldProps: { id, ...rest } }) => (
        <AsyncSelect
          menuPosition={"fixed"}
          loadOptions={loadOptions}
          id={`${id}-select`}
          className="single-select"
          classNamePrefix="react-select"
          getOptionLabel={(user: UserDto) => user.name}
          getOptionValue={(user: UserDto) => user.key}
          {...rest}
        />
      )}
    </Field>
  );
};

export default UserField;
