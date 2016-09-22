fuse-kit-smoketest
==================

These tests are intended to be run as part of a Fuse kit smoke test.   
 
To run the quickstart tests, do the following:

1, Update the value of jboss.fuse.bom.version in pom.xml to the current version
2. Set FUSE_HOME to the full path of the Fuse installation, i.e. export FUSE_HOME=/Users/kearls/fuse/jboss-fuse-jboss-fuse-6.3.0.redhat-045/
3. cd to ${FUSE_HOME}/quickstarts and build all of the quickstarts
4. In another shell, Run fuse
5. Run the deployQuickStarts.sh script to deploy the quickstarts
6. Run the quickstart tests with the following command; FUSE_HOME must be set

    mvn -DFUSE_HOME=${FUSE_HOME} -Pquickstarts clean install
    
To run the other (non-quickstarts) tests

1. Run fuse, and create a fabric.
2. mvn --file ${FUSE_HOME}/quickstarts/beginner/camel-cbr/pom.xml fabric8:deploy
3. Run the tests with the following command:

    mvn -DFUSE_HOME=${FUSE_HOME} -Pnoquickstarts clean install
