# README #

use gradlew first then

To build the game, use the packr app. You can also use the batch file:

```
pachthemall.bat
```


You may add the following in the config files:
```
#!json
"resources": [
         ".\\desktop\\build\\libs\\fonts",
         ".\\desktop\\build\\libs\\particles",
         ".\\desktop\\build\\libs\\preference",
         ".\\desktop\\build\\libs\\screens",
         ".\\desktop\\build\\libs\\sounds",
         ".\\desktop\\build\\libs\\tournament_levels",
         ".\\desktop\\build\\libs\\jungle_tiles_new.png",
         ".\\desktop\\build\\libs\\object_types.json",
         ".\\desktop\\build\\libs\\texture_obj.atlas",
         ".\\desktop\\build\\libs\\texture_obj.png"
        ]

```

If not you will need to copy paste the content of the `./core/assets` folder in each build manually.