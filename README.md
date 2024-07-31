# IntelliJ Live Templates Sharing Plugin
This plugin allows you to share your IntelliJ IDEA live templates with your team. It is a simple way to share your live templates with your team members. You can export your live templates to a file and share it with your team members.

## Sharing Live Templates
1. Create custom live template group in Settings and add your live templates there. Press `OK` to save the changes.  
![custom live template](images/customTemplate.png)
2. Invoke the `Share Custom Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)
![share custom live template](images/shareCustomLiveTemplate.png)
3. After action successfully executed, you will see your live templates in the `.idea/liveTemplates` folder
![shared.png](images/shared.png)
4. Commit and push shared live templates to your repository

## Importing Live Templates
After that, your team members with plugin installed will import these live templates automatically when they open project next time.

### Manual import
There is the action to import live templates manually.
Invoke the `Import Project Live Templates` action from `Go to Action` (`Ctrl/Cmd + Shift + A`)