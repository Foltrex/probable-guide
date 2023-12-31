import Avatar from "@atlaskit/avatar";
import Button from "@atlaskit/button";
import { Checkbox } from "@atlaskit/checkbox";
import DynamicTable from "@atlaskit/dynamic-table";
import React from "react";
import styled from "styled-components";
import TrashIcon from "@atlaskit/icon/glyph/trash";
import EditIcon from "@atlaskit/icon/glyph/edit";
import CopyIcon from "@atlaskit/icon/glyph/copy";
import ArrowRightIcon from "@atlaskit/icon/glyph/arrow-right";
import { AutoTTDto } from "../../models";
import {
  getProjectOrIssueURL,
  getUserAvatarURL,
  getUserProfileURL,
} from "../../utils";

interface ComponentProps {
  items: AutoTTDto[];
  isLoaded: boolean;
  onEdit(id: number): void;
  onCopy(id: number): void;
  onDelete(id: number): void;
  onStartJob(id: number): void;
}

const AvatarWrapper = styled.div`
  margin-right: 8px;
`;

const NameWrapper = styled.span`
  display: flex;
  align-items: center;
`;

const AutoTTTable: React.FC<ComponentProps> = ({
  items,
  isLoaded,
  onEdit,
  onCopy,
  onDelete,
  onStartJob,
}) => {
  const head = {
    cells: [
      {
        key: "user",
        content: "User",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "project",
        content: "Project",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "issue",
        content: "Issue",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "ratedtime",
        content: "Rated Time",
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "startdate",
        content: "Starting date",
        shouldTruncate: true,
        isSortable: false,
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
        shouldTruncate: true,
        width: undefined,
      },
      {
        key: "active",
        content: "Active",
        shouldTruncate: true,
        isSortable: false,
        width: undefined,
      },
      { key: "actions", content: "Actions", shouldTruncate: true },
    ],
  };

  const rows = items.map((item) => ({
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
                href={getUserProfileURL(item.user.username)}
                name={item.user.name}
                size="medium"
              />
            </AvatarWrapper>
            <a target="_blank" href={getUserProfileURL(item.user.username)}>
              {`${item.user.name} (${item.user.username})`}
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
        key: `startdate-${item.id}`,
        content: new Date(item.startDate!).toLocaleDateString(),
      },
      {
        key: `worklogtype-${item.id}`,
        content: item.worklogType ? item.worklogType.name : "",
      },
      {
        key: `createdby-${item.id}`,
        content: (
          <a target="_blank" href={getUserProfileURL(item.author.username)}>
            {item.author.username}
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
          <a
            target="_blank"
            href={getUserProfileURL(item.updateAuthor.username)}
          >
            {item.updateAuthor.username}
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
              onClick={onStartJob.bind(this, item.id)}
              appearance="link"
              title="Start job"
            >
              <ArrowRightIcon label="Start job" />
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
      emptyView={<h2>No records</h2>}
      isLoading={!isLoaded}
      head={head}
      rows={rows}
      loadingSpinnerSize="large"
    />
  );
};

export default AutoTTTable;
