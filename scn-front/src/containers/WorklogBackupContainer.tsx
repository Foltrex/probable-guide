import Button from "@atlaskit/button";
import Form, { Field } from "@atlaskit/form";
import { request } from "api";
import { ProjectField } from "components/fields";
import Config from "config";
import { ProjectDto } from "models";
import React, { useState } from "react";
import { useFlagService } from "services/FlagService";
import { DatePicker } from "@atlaskit/datetime-picker";
import Spinner from "@atlaskit/spinner";

const WorklogBackupContainer = () => {
  const { showSuccess, showError } = useFlagService();
  const [project, setProject] = useState<ProjectDto>(null);
  const [dateFrom, setDateFrom] = useState<string>("");
  const [dateTo, setDateTo] = useState<string>("");
  const [isLoaded, setIsLoaded] = useState(true);

  const onFullBackupClick = () => {
    setIsLoaded(false);
    request({
      url: Config.API + `/worklog/backup?pid=${project.id}`,
      method: "POST",
    })
      .then(() => {
        setIsLoaded(true);
        showSuccess("Backup is done.");
      })
      .catch(({ message }) => {
        setIsLoaded(true);
        showError(message);
      });
  };

  const onBackupClick = () => {
    setIsLoaded(false);
    request({
      url:
        Config.API +
        `/worklog/backup?pid=${project.id}&from=${dateFrom}&to=${dateTo}`,
      method: "POST",
    })
      .then(() => {
        setIsLoaded(true);
        showSuccess("Backup is done.");
      })
      .catch(({ message }) => {
        setIsLoaded(true);
        showError(message);
      });
  };

  const onDownloadFullBackupClick = () => {
    setIsLoaded(false);
    request({
      url: Config.API + `/worklog/backup/download/csv?pid=${project.id}`,
      method: "GET",
      responseType: "blob",
    })
      .then((response: any) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", `${project.key}.csv`);
        document.body.appendChild(link);
        link.click();
        setIsLoaded(true);
      })
      .catch(({ message }) => {
        setIsLoaded(true);
        showError(message);
      });
  };

  const onDownloadBackupClick = () => {
    setIsLoaded(false);
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
        setIsLoaded(true);
      })
      .catch(({ message }) => {
        setIsLoaded(true);
        showError(message);
      });
  };

  const onCopyWLClick = () => {
    setIsLoaded(false);
    request({
      url:
        Config.API +
        `/worklog/copy-from-scn-worklogs?pid=${project.id}&from=${dateFrom}&to=${dateTo}`,
      method: "POST",
    })
      .then(() => {
        setIsLoaded(true);
        showSuccess("WL are copied from WL*.");
      })
      .catch(({ message }) => {
        setIsLoaded(true);
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
      {!isLoaded && <Spinner size={"xlarge"} />}
      {isLoaded && (
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
                  <br />
                  <Button
                    onClick={onFullBackupClick}
                    title="Full project worklogs backup for all period"
                  >
                    Full backup
                  </Button>
                  <Button
                    onClick={onDownloadFullBackupClick}
                    title="Download full backup (CSV) for all period"
                  >
                    Download full backup (CSV)
                  </Button>
                  <br />
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
              {project && dateFrom && dateTo && (
                <>
                  <br />
                  <Button
                    onClick={onBackupClick}
                    title="Project worklogs backup for period"
                  >
                    Backup
                  </Button>
                  <Button
                    onClick={onDownloadBackupClick}
                    title="Download backup (CSV) for period"
                  >
                    Download backup (CSV)
                  </Button>
                  <Button
                    onClick={onCopyWLClick}
                    title="Create Wl from WL* for period"
                  >
                    WL from WL*
                  </Button>
                </>
              )}
            </form>
          )}
        </Form>
      )}
    </div>
  );
};

export default WorklogBackupContainer;
