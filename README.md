# IntelliJ Live Templates Sharing Plugin
The plugin allows to share custom IntelliJ IDEA live templates with a team.

## Sharing Live Templates
1. Create custom live template group in Settings and add your live templates there. Press `OK` to save the changes.  
![custom live template](images/customTemplate.png)
2. Invoke the `Share Custom Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)
![share custom live template](images/shareCustomLiveTemplate.png)
3. After action successfully executed, you will see your live templates in the `.idea/liveTemplates` folder
![shared.png](images/shared.png)
4. Commit shared live templates to your repository

## Importing Live Templates
Everybody with plugin installed who open repository will import shared live templates automatically when they open project next time.

### Manual import
There is the action to import live templates manually.
Invoke the `Import Project Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)

## Limitations
- The plugin works by copying shared live templates groups to the IDE settings, so technically when you open the project with shared live templates the plugin will add shared groups from project to your IDE settings.
- The plugin supports **only custom** live templates group. If you add live templates to predefined groups, they will not be shared.
