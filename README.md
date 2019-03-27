# Science Soft Jira Plugins


## Description

Plugins for Jira 7.13.1

Old plugins are stored in the repository:
https://stash.scnsoft.com/projects/JWL/repos/sciencesoft.jwl

Atlassian SDK
https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-linux-or-mac-system/

## Build

For building plugin use atlassian maven.
https://developer.atlassian.com/server/framework/atlassian-sdk/working-with-maven/

Install parent pom via atlas-mvn clean install -Pbuild-super-pom

Set Atlassian maven plugin settings to idea maven plugin, for example:
Maven home directory: /usr/share/atlassian-plugin-sdk-8.0.4/apache-maven-3.5.4
User settings file: /usr/share/atlassian-plugin-sdk-8.0.4/apache-maven-3.5.4/conf/settings.xml

## DB

For Worklog

Copy to Jira/WEB-INF/classes/entitydefs/ 
edit-webapp/WEB-INF/classes/entitydefs/entitymodel-scn-worklog.xml and 
edit-webapp/WEB-INF/classes/entitydefs/entitymodel-scn-worklogtypes.xml 

Merge Jira/WEB-INF/classes/entitydefs/entitygroup.xml
and edit-webapp/WEB-INF/classes/entitydefs/entitygroup.xml

Merge Jira/WEB-INF/classes/entityengine.xml
and edit-webapp/WEB-INF/classes/entityengine.xml

For Logtime

Copy to Jira/WEB-INF/classes/entitydefs/ 
edit-webapp/WEB-INF/classes/entitydefs/entitymodel-scn-worklog.xml 

Merge Jira/WEB-INF/classes/entitydefs/entitygroup.xml
and edit-webapp/WEB-INF/classes/entitydefs/entitygroup.xml

## Plugins

The Package plugins contains actual jar files. 
