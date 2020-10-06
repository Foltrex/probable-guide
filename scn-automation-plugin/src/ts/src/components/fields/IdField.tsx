import { Field } from "@atlaskit/form";
import Textfield from "@atlaskit/textfield";
import React from "react";

const IdField: React.FC<{ value: number }> = ({ value }) => {
  return (
    <div hidden={true}>
      <Field<number> isDisabled={true} label="ID" name="id" defaultValue={value}>
        {({ fieldProps }) => <Textfield hidden={true} {...fieldProps} />}
      </Field>
    </div>
  );
};

export default IdField;
