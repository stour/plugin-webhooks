# plugin webhooks

Handle webhook events and update Codenvy factories accordingly.

## Concepts
* Webhook configuration: Represents a webhook configured on GitHub or VSTS.
* Connector: Represents a connection to a third-party service where Codenvy factory URL will be displayed. Currently Jenkins connector is available.

## GitHub webhooks
Two type of GitHub events are processed:
* Push event: If a webhook is configured for event.repository and if in this webhook a factory is listed that contains a project with location = event.repository and branch = event.branch then check if URL of this factory is present on configured connectors (third-party services) and if not add it.
* Pull Request event: If a webhook is configured for event.baseRepository and if in this webhook a factory is listed that contains a project with location = event.headRepository and branch = event.headBranch then update this project with location = event.baseRepository, commitId = event.headCommitId and clean branch parameter if set.

#### Configure
* `/home/codenvy/github-webhooks.properties`: List of GitHub webhooks.
* `/home/codenvy/connectors.properties`: List of configured connectors.
* `/home/codenvy/credentials.properties`: username and password used to authenticate against Codenvy.

1. On GitHub go to https://github.com/{user}/{repository}/settings/hooks and configure a new webhook for the repository. Set the 'api/github-webhook' URL of your Codenvy instance (example: http://internal.codenvycorp.com/api/github-webhook).
2. Create factories for your repository (or let them be generated by [Codenvy JIRA add-on](https://github.com/codenvy/codenvy-jira-addon)).
3. On your Codenvy instance, make sure that webhook, connector and credentials properties files are ready.
4. Let's work on your feature branch, push changes and finally merge your branch.
5. Factories related to your repository and branch will stay up to date with your last contributions. If connectors where configured your third-party services will always show up to date Codenvy factories.

Examples of configuration files can be found in [GitHub test resources](codenvy-plugin-webhooks-github/src/test/resources).

## Microsoft VSTS webhooks
Two type of VSTS events are processed:
* Work Item created event: If a webhook and a parent factory are configured for the VSTS Team Project the event is related to and VSTS credentials (provided as part of the webhook) are correct then generate Develop and Review factories for the work item and push factories link to VSTS extension storage.
* Pull Request updated event: If pull request state is 'merged' and merge status is 'completed' then update configured factories that contain projects related to the repository and branch given in event. Update factory is: set feature branch head commitId as 'commitId' parameter value and clean 'branch' parameter. This way feature branch can be deleted and it will still be possible to open the factory and clone project at right commitId.

#### Configure
* `/home/codenvy/vsts-webhooks.properties`: List of VSTS webhooks.
* `/home/codenvy/credentials.properties`: username and password used to authenticate against Codenvy.

1. Go to https://{account}.visualstudio.com/DefaultCollection/{project-name}/_admin/_servicehooks and configure: a) a new webhook for 'Work item created' events and b) a new webhook for 'Pull request updated' events. Webhook URL to set is http://{codenvy-instance-hostname}/api/vsts-webhook for both.
2. On your Codenvy instance, make sure that webhook and credentials properties files are ready. In particular VSTS credentials given as part of the webhook are secondary credentials generated as described [here](https://www.visualstudio.com/en-us/integrate/get-started/auth/overview).
3. Create parent factory for the Team Project. The parent factory must be named same as the Team Project and created by same Codenvy user as specified in `credentials.properties`.
4. Create a new Work Item on the VSTS Team Project.
5. Open the work item and click _Developer Workspace_ or _Reviewer Workspace_ to open related factories. You can check new factories on Codenvy dashboard too.

Examples of configuration files can be found in [VSTS test resources](codenvy-plugin-webhooks-vsts/src/test/resources).
