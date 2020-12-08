import Button from "@atlaskit/button";
import Form, { Field, FormFooter } from "@atlaskit/form";
import { request } from "api";
import { ProjectField } from "components/fields";
import Config from "config";
import { ProjectDto } from "models";
import React, { useState } from "react";
import { useFlagService } from "services/FlagService";
import { DatePicker } from "@atlaskit/datetime-picker";

const WorklogBackupContainer = () => {
  const { showSuccess, showError } = useFlagService();
  const [project, setProject] = useState<ProjectDto>(null);
  const [dateFrom, setDateFrom] = useState<string>("");
  const [dateTo, setDateTo] = useState<string>("");
  const [isBackuped, setIsBackuped] = useState<boolean>(true);
  const [isDownloaded, setIsDownloaded] = useState<boolean>(true);

  const onBackupClick = () => {
    setIsBackuped(false);
    request({
      url: Config.API + `/worklog/backup?pid=${project.id}`,
      method: "POST",
    })
      .then(() => {
        setIsBackuped(true);
        showSuccess("Backup is done.");
      })
      .catch(({ message }) => {
        setIsBackuped(true);
        showError(message);
      });
  };

  const onDownloadClick = () => {
    setIsDownloaded(false);
    request({
      url:
        Config.API +
        `/worklog/backup/download/csv?pid=${project.id}&from=${dateFrom}&to=${dateTo}`,
      method: "GET",
      responseType: "blob",
    })
      .then((response: any) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute(
          "download",
          `${project.key}_from_${dateFrom}_to_${dateTo}.csv`
        );
        document.body.appendChild(link);
        link.click();
        setIsDownloaded(true);
      })
      .catch(({ message }) => {
        setIsDownloaded(true);
        showError(message);
      });
  };

  return (
    <div
      style={{
        display: "flex",
        width: "500px",
        margin: "0 auto",
        flexDirection: "column",
      }}
    >
      <Form onSubmit={null}>
        {({ formProps }) => (
          <form {...formProps} name="text-fields">
            <ProjectField
              label="Project"
              name="project"
              value={project}
              onChange={(value) => {
                setProject(value);
              }}
            />
            {project && (
              <>
                <Field
                  name="from"
                  defaultValue={dateFrom}
                  label="Date from"
                  isRequired
                >
                  {({ fieldProps }) => (
                    <DatePicker
                      {...fieldProps}
                      onChange={(value) => setDateFrom(value)}
                    />
                  )}
                </Field>
                <Field
                  name="to"
                  defaultValue={dateTo}
                  label="Date to"
                  isRequired
                >
                  {({ fieldProps }) => (
                    <DatePicker
                      {...fieldProps}
                      onChange={(value) => setDateTo(value)}
                    />
                  )}
                </Field>
              </>
            )}
            <FormFooter align={"start"}>
              {project && (
                <Button isDisabled={!isBackuped} onClick={onBackupClick}>
                  {isBackuped ? "Backup" : "Running"}
                </Button>
              )}
              {project && dateFrom && dateTo && (
                <Button isDisabled={!isDownloaded} onClick={onDownloadClick}>
                  {isDownloaded ? "Download (CSV)" : "Running"}
                </Button>
              )}
            </FormFooter>
          </form>
        )}
      </Form>
    </div>
  );
};

export default WorklogBackupContainer;
