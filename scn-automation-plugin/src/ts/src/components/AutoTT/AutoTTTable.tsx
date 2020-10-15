import Avatar from "@atlaskit/avatar";
import Button from "@atlaskit/button";
import { Checkbox } from "@atlaskit/checkbox";
import DynamicTable from "@atlaskit/dynamic-table";
import React from "react";
import styled from "styled-components";
import TrashIcon from "@atlaskit/icon/glyph/trash";
import EditIcon from "@atlaskit/icon/glyph/edit";
import CopyIcon from "@atlaskit/icon/glyph/copy";
import {
  getProjectOrIssueURL,
  getUserAvatarURL,
  getUserProfileURL,
} from "../../api";
import { AutoTTDto } from "../../models";

interface ComponentProps {
  items: AutoTTDto[];
  isLoaded: boolean;
  onEdit(id: number): void;
  onCopy(id: number): void;
  onDelete(id: number): void;
}

const AutoTTTable: React.FC<ComponentProps> = ({
  items,
  isLoaded,
  onEdit,
  onCopy,
  onDelete,
}) => {
  const NameWrapper = styled.span`
    display: flex;
    align-items: center;
  `;

  const AvatarWrapper = styled.div`
    margin-right: 8px;
  `;
  const getHead = () => ({
    cells: [
      {
        key: "user",
        content: "User",
        shouldTruncate: true,
        isSortable: true,
        width: undefined,
      },
      {
        key: "project",
        content: "Project",
        shouldTruncate: true,
        isSortable: true,
        width: undefined,
      },
      {
        key: "issue",
        content: "Issue",
        shouldTruncate: true,
        isSortable: true,
        width: undefined,
      },
      {
        key: "ratedtime",
        content: "Rated Time",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "worklogtype",
        content: "Worklog Type",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "createdby",
        content: "Created by",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "created",
        content: "Created",
        isSortable: true,
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "updatedby",
        content: "Updated by",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "updated",
        content: "Updated",
        isSortable: true,
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "active",
        content: "Active",
        shouldTruncate: true,
        width: undefined,
      },
      { key: "actions", content: "Actions", shouldTruncate: true },
    ],
  });

  const getRows = (items: AutoTTDto[]) =>
    items.map((item) => ({
      key: `row-${item.id}`,
      cells: [
        {
          key: `user-${item.id}`,
          content: (
            <NameWrapper>
              <AvatarWrapper>
                <Avatar
                  src={getUserAvatarURL(item.user.key)}
                  target="_blank"
                  href={getUserProfileURL(item.user.key)}
                  name={item.user.name}
                  size="medium"
                />
              </AvatarWrapper>
              <a target="_blank" href={getUserProfileURL(item.user.key)}>
                {`${item.user.name} (${item.user.key})`}
              </a>
            </NameWrapper>
          ),
        },
        {
          key: `project-${item.id}`,
          content: (
            <a target="_blank" href={getProjectOrIssueURL(item.project.key)}>
              {`${item.project.name} (${item.project.key})`}
            </a>
          ),
        },
        {
          key: `issue-${item.id}`,
          content: (
            <a
              target="_blank"
              href={getProjectOrIssueURL(item.issue.key)}
            >{`${item.issue.name} (${item.issue.key})`}</a>
          ),
        },
        {
          key: `ratedtime-${item.id}`,
          content: item.ratedTime,
        },
        {
          key: `worklogtype-${item.id}`,
          content: item.worklogType ? item.worklogType.name : "",
        },
        {
          key: `createdby-${item.id}`,
          content: (
            <a target="_blank" href={getUserProfileURL(item.author.key)}>
              {item.author.key}
            </a>
          ),
        },
        {
          key: `created-${item.id}`,
          content: new Date(item.created).toLocaleDateString(),
        },
        {
          key: `updatedby-${item.id}`,
          content: (
            <a target="_blank" href={getUserProfileURL(item.updateAuthor.key)}>
              {item.updateAuthor.key}
            </a>
          ),
        },
        {
          key: `updated-${item.id}`,
          content: new Date(item.updated).toLocaleDateString(),
        },

        {
          key: `active-${item.id}`,
          content: <Checkbox isChecked={item.active} />,
        },
        {
          key: `actions-${item.id}`,
          content: (
            <>
              <Button
                onClick={onEdit.bind(this, item.id)}
                appearance="link"
                title="Edit"
              >
                <EditIcon label="Edit" />
              </Button>
              <Button
                onClick={onCopy.bind(this, item.id)}
                appearance="link"
                title="Copy"
              >
                <CopyIcon label="Copy" />
              </Button>
              <Button
                onClick={onDelete.bind(this, item.id)}
                appearance="link"
                title="Delete"
              >
                <TrashIcon label="Delete" />
              </Button>
            </>
          ),
        },
      ],
    }));

  return (
    <DynamicTable
      emptyView={<h2>No items</h2>}
      isLoading={!isLoaded}
      head={getHead()}
      rows={getRows(items)}
      loadingSpinnerSize="large"
    />
  );
};

export default AutoTTTable;
