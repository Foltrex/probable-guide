import Button from "@atlaskit/button";
import React, { useContext } from "react";
import styled from "styled-components";
import { AutoTTContext } from "../../services/autoTT/autoTTContext";

const AutoTTCaption: React.FC = () => {
  const { onCreate } = useContext(AutoTTContext);

  const HeaderWrapper = styled.div`
    display: flex;
    align-items: center;
  `;
  const CaptionWrapper = styled.div`
    width: 100%;
  `;

  const ActionWrapper = styled.div`
    text-align: right;
    padding-right: 20px;
  `;

  return (
    <HeaderWrapper>
      <CaptionWrapper>
        <h1>Auto time tracking users</h1>
      </CaptionWrapper>
      <ActionWrapper>
        <Button onClick={onCreate}>Add user</Button>
      </ActionWrapper>
    </HeaderWrapper>
  );
};

export default AutoTTCaption;
