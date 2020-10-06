import Button from "@atlaskit/button";
import React from "react";
import styled from "styled-components";

interface AutoTTCaptionProps {
  caption: string;
  createAction(): void;
}

const AutoTTCaption: React.FC<AutoTTCaptionProps> = ({
  caption,
  createAction,
}) => {
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
        <h1>{caption}</h1>
      </CaptionWrapper>
      <ActionWrapper>
        <Button onClick={createAction}>Add user</Button>
      </ActionWrapper>
    </HeaderWrapper>
  );
};

export default AutoTTCaption;
