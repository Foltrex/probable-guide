import Button from "@atlaskit/button";
import React from "react";
import styled from "styled-components";
import { QuickSearch } from "@atlaskit/quick-search";

interface ComponentProps {
  onCreate(): void;
  onSearch(text: string): void;
  searchText: string;
}

const AutoTTCaption: React.FC<ComponentProps> = ({
  onCreate,
  onSearch,
  searchText,
}) => {
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
        <h1>Auto Time-Tracking Users</h1>
      </CaptionWrapper>
      <ActionWrapper>
        <QuickSearch
          placeholder="Search..."
          children={null}
          value={searchText}
          onSearchInput={(event) =>
            onSearch((event.target as HTMLInputElement).value)
          }
        />
        <Button onClick={onCreate}>Add user</Button>
      </ActionWrapper>
    </HeaderWrapper>
  );
};

export default AutoTTCaption;
