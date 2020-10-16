import Button from "@atlaskit/button";
import React, { FormEvent, useContext, useRef } from "react";
import styled from "styled-components";
import { AutoTTContext } from "../../services/autoTT/autoTTContext";
import { QuickSearch } from "@atlaskit/quick-search";

const AutoTTCaption: React.FC = () => {
  const { searchText, onCreate, searchAutoTT } = useContext(AutoTTContext);

  const HeaderWrapper = styled.div`
    padding-top: 10px;
    display: flex;
    align-items: center;
  `;
  const CaptionWrapper = styled.div`
    width: 100%;
  `;

  const ActionWrapper = styled.div`
    display: flex;
    text-align: right;
    padding-right: 20px;
  `;

  return (
    <HeaderWrapper>
      <CaptionWrapper>
        <h1>Auto time tracking users</h1>
      </CaptionWrapper>
      <ActionWrapper>
        <QuickSearch
          placeholder="User, project or issue"
          children={null}
          value={searchText}
          onSearchInput={(event) =>
            searchAutoTT((event.target as HTMLInputElement).value)
          }
        />
        <Button onClick={onCreate}>Add user</Button>
      </ActionWrapper>
    </HeaderWrapper>
  );
};

export default AutoTTCaption;
