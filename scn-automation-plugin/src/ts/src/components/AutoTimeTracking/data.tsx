import React from "react";
import Avatar from "@atlaskit/avatar";
import { Checkbox } from "@atlaskit/checkbox";
import styled from "styled-components";
import {
  getProjectOrIssueURL,
  getUserAvatarURL,
  getUserProfileURL,
} from "../../api";
import { AutoTTDto } from "../../dto";
import { HeadType, RowType } from "@atlaskit/dynamic-table/dist/cjs/types";
import Button from "@atlaskit/button";
import TrashIcon from "@atlaskit/icon/glyph/trash";
import EditIcon from "@atlaskit/icon/glyph/edit";
import CopyIcon from "@atlaskit/icon/glyph/copy";

export const head: HeadType = {
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
};

const NameWrapper = styled.span`
  display: flex;
  align-items: center;
`;

const AvatarWrapper = styled.div`
  margin-right: 8px;
`;

export const getRows = (
  autoTTList: AutoTTDto[],
  editAction: (id: number) => void,
  copyAction: (id: number) => void,
  deleteAction: (id: number) => void
): RowType[] =>
  autoTTList.map((autoTT: AutoTTDto) => ({
    key: `row-${autoTT.id}`,
    cells: [
      {
        key: `user-${autoTT.id}`,
        content: (
          <NameWrapper>
            <AvatarWrapper>
              <Avatar
                src={getUserAvatarURL(autoTT.user.key)}
                href={getUserProfileURL(autoTT.user.key)}
                name={autoTT.user.name}
                size="medium"
              />
            </AvatarWrapper>
            <a href={getUserProfileURL(autoTT.user.key)}>
              {`${autoTT.user.name} (${autoTT.user.key})`}
            </a>
          </NameWrapper>
        ),
      },
      {
        key: `project-${autoTT.id}`,
        content: (
          <a href={getProjectOrIssueURL(autoTT.project.key)}>
            {`${autoTT.project.name} (${autoTT.project.key})`}
          </a>
        ),
      },
      {
        key: `issue-${autoTT.id}`,
        content: (
          <a
            href={getProjectOrIssueURL(autoTT.issue.key)}
          >{`${autoTT.issue.name} (${autoTT.issue.key})`}</a>
        ),
      },
      {
        key: `worklogtype-${autoTT.id}`,
        content: autoTT.worklogType ? autoTT.worklogType.name : "",
      },
      {
        key: `createdby-${autoTT.id}`,
        content: (
          <a href={getUserProfileURL(autoTT.author.key)}>
            {`${autoTT.author.name} (${autoTT.author.key})`}
          </a>
        ),
      },
      {
        key: `created-${autoTT.id}`,
        content: new Date(autoTT.created).toLocaleDateString(),
      },
      {
        key: `updatedby-${autoTT.id}`,
        content: (
          <a href={getUserProfileURL(autoTT.updateAuthor.key)}>
            {`${autoTT.updateAuthor.name} (${autoTT.updateAuthor.key})`}
          </a>
        ),
      },
      {
        key: `updated-${autoTT.id}`,
        content: new Date(autoTT.updated).toLocaleDateString(),
      },

      {
        key: `active-${autoTT.id}`,
        content: <Checkbox isChecked={autoTT.active} />,
      },
      {
        key: `actions-${autoTT.id}`,
        content: (
          <>
            <Button
              onClick={editAction.bind(this, autoTT.id)}
              appearance="link"
              title="Edit"
            >
              <EditIcon label="Edit" />
            </Button>
            <Button
              onClick={copyAction.bind(this, autoTT.id)}
              appearance="link"
              title="Copy"
            >
              <CopyIcon label="Copy" />
            </Button>
            <Button
              onClick={deleteAction.bind(this, autoTT.id)}
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
