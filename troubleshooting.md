# Troubleshooting

* [No main manifest attribute](#no-main-manifest-attribute)

## No main manifest attribute

If after building the artifact and attempting to run the application you are seeing the following message:

    no main manifest attribute, in out/artifacts/zebec_main_jar/zebec.jar
    
Then try updating your project settings so inherit project compile output path. Tt uses IntelliJ and not Gradle tasks to build the artifacts and sometimes it gets overwritten.

<p align="center"><img src="https://github.com/ssoper/Zebec/raw/master/gh/inherit_project.png" alt="Project Setting Screenshot"></p>