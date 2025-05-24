# IntelliJ Live Templates Sharing Plugin
The plugin allows to share custom IntelliJ IDEA live templates with a team by putting them to `.idea/liveTemplates` folder.

Plugin page: https://plugins.jetbrains.com/plugin/25007-live-templates-sharing

## Sharing Live Templates
1. Create custom live template group in Settings and add live templates there. Press `OK` to save the changes.  
![custom live template](images/customTemplate.png)
2. Invoke the `Share Custom Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)
![share custom live template](images/shareCustomLiveTemplate.png)
3. After action successfully executed, live templates should appear in the `.idea/liveTemplates` folder
![shared.png](images/shared.png)
4. Commit shared live templates to the repository

## Importing Live Templates
Everybody with plugin installed who opens repository will import project live templates automatically when they open project next time.

### Manual import
There is the action to import live templates manually.
Invoke the `Import Project Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)

## Implementation
The plugin works by copying shared live templates groups to the IDE settings.
Technically when a project with shared live templates is opened the plugin will add groups from `.idea/liveTemplates` to the IDE settings. 
And when the project is closed copied templates will be removed from IDE settings.

## Limitations
- If you open multiple projects and some of them contain templates with the same name, only first loaded project's templates with the same name will be used.
- The plugin supports **only custom** live templates group. If you add live templates to predefined groups, they will not be shared.
