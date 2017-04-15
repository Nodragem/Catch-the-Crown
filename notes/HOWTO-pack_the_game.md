> see: <https://github.com/libgdx/libgdx/wiki/Bundling-a-JRE>

## How to build the game:
To build the desktop .jar, the artifact system of intelliJ does not work. 
1) We need to run:
```gradlew desktop:dist``` 
in IntelliJ command line (or the windows 10 command line) 

2) The build will then be foundable at `desktop/build/libs/` run the jar with:

    ```java -jar mygame.jar```

3) To pack the jar into a standalone app, go to Ropegame folder and run **as an Administrator** :

    ```java -jar packr.jar config_xxxx.json```

## Notes:

- you can also run the bat file `packthemall.bat`, it will run all the config files (remember to run as an administrator).
- **WARNING**: the windows32 application needs to be built with the win32 JRE.
- don't work on the zip file of the builds
- get the builds for mac, linux and windows there: <https://github.com/alexkasko/openjdk-unofficial-builds>
- if you want to get the asset folder outside the jar file, go to:

    `C:\Users\geoff\MyDocuments\Bitbucket\catch-the-crown\desktop\build.gradle`

    And comment the line:

    ```// from files(project.assetsDir);```

    in the task `dist(type: Jar)`.
    Then you will need to copy paste the **content** of the asset folder next to the jar file that have been built at `desktop/build/libs/`.
