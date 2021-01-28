import { ErrorMessage, Field } from "@atlaskit/form";
import React from "react";
import { UserDto } from "../../models";
import { AsyncSelect } from "@atlaskit/select";
import { request } from "../../api";
import { useFlagService } from "../../services/FlagService";
import Config from "../../config";

interface UserFieldProps {
  value: UserDto;
  label: string;
  name: string;
}

const UserField: React.FC<UserFieldProps> = ({ value, label, name }) => {
  const { showError } = useFlagService();

  const loadOptions = (query: string) =>
    request<{ users: any[] }>({
      url: `${Config.JIRA_API}/user/picker?query=${query}`,
      method: "GET",
    })
      .then(({ data: { users } }) =>
        users.map((value: { key: string; displayName: string }) => ({
          key: value.key,
          name: value.displayName,
        }))
      )
      .catch(({ message }) => showError(message));

  return (
    <Field<UserDto>
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
            loadOptions={loadOptions}
            className="single-select"
            classNamePrefix="react-select"
            getOptionLabel={(user: UserDto) => user.name}
            getOptionValue={(user: UserDto) => user.key}
          />
          {error && <ErrorMessage>{error}</ErrorMessage>}
        </>
      )}
    </Field>
  );
};

export default UserField;
