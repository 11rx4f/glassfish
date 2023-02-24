# Releasing a new GlassFish version

In this example we assume 7.0.3.

1. Check beforehand that this version does NOT exist in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/main/distributions/glassfish/) else bump the version
2. Go to [GlassFish CI](https://ci.eclipse.org/glassfish/)
3. [Log in](https://ci.eclipse.org/glassfish/login?from=%2Fglassfish%2F)
4. Open [glassfish_1-build-and-stage](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/)
5. Click [Build with parameters](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage//build) in menu 
    - `RELEASE_VERSION` = `7.0.3`
    - `USE_STAGING_REPO` = `false` (uncheck)
    - click [Build] button
6. Wait for it to finish successfully
7. Drill down into this build e.g. [build 79](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/79/)
8. Click [Console Output](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_1-build-and-stage/79/console) in menu
9. Ctrl+F 'orgglassfish', to find release ID, e.g. `Created staging repository with ID "orgglassfish-1230"`, remember this for `STAGING_RELEASE_ID` in a later step
   In case the release ID is not in the log (sometimes it just isn't, we don't know why), use
   go to [jsftemplating_1_build-and-stage]()https://ci.eclipse.org/glassfish/view/JSFTemplating/job/jsftemplating_1_build-and-stage/build
    - `LIST_FIRST` = `true` (check)
    - click [Build] button
   In the output, look for a line like the following:
   ```
   [INFO] orgglassfish-1257    CLOSED   org.glassfish.main:glassfish-main-aggregator:7.0.3
   ```
10. Verify that 7.0.3 is present in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/main/distributions/glassfish/)
11. Verify that a new [7.0.3](https://github.com/eclipse-ee4j/glassfish/tree/7.0.3-BRANCH) branch is created 
12. Run the TCKs against the staged build at https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/
13. Run the [platform TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/10/job/eftl-jakartaeetck-run-100/)
14. Run the [standalone TCK](https://ci.eclipse.org/jakartaee-tck/view/EFTL-Certification-Jobs-10/job/eftl-jakartaeetck-run-standalone/)
15. Wait for it to finish successfully
16. Open [3_staging-to-release](https://ci.eclipse.org/glassfish/view/JSFTemplating/job/jsftemplating_3_staging-to-release/)
17. Click [Build with parameters](https://ci.eclipse.org/glassfish/view/JSFTemplating/job/jsftemplating_3_staging-to-release/build) in menu
    - `STAGING_RELEASE_ID` = `orgglassfish-1257`
    - click [Build] button
18. Wait for it to finish successfully
19. Verify that it's present in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/) (might take up to a hour)
20. If everything is OK, then merge 7.0.3 branch into master via PR
21. Delete the 7.0.3 branch after merge
22. Upload the new release to the Eclipse download folder. 
    go to [glassfish_copy-staging-to-downloads](https://ci.eclipse.org/glassfish/view/GlassFish/job/glassfish_copy-staging-to-downloads/build?delay=0sec)
    - Enter the version to copy; 7.0.3
    - click [Build] button 
23. Create the release on Github: https://github.com/eclipse-ee4j/glassfish/releases click "draft a new release"
24. Create the release on Eclipse: https://projects.eclipse.org/projects/ee4j.glassfish click "create a new release"
25: Create the release on Glassfish.org: do a PR for updating the versions here: https://github.com/eclipse-ee4j/glassfish/tree/master/docs/website/src/main/resources


