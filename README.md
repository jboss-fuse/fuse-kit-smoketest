fuse-kit-smoketest
==================

These tests are intended to be run as part of a Fuse kit smoke test.   
 
To run the quickstart tests, do the following:

0. Set FUSE_HOME to the full path of the Fuse installation, i.e. export FUSE_HOME=/Users/kearls/fuse/jboss-fuse-6.2.0.redhat-016/
1. Run the fixCXFCodeFirstExample.sh
2. cd to ${FUSE_HOME}/quickstarts and build all of the quickstarts
3. In another shell, Run fuse
4. Run the deployQuickStarts.sh script to deploy the quickstarts
5. Run the quickstart tests with the following command; FUSE_HOME must be set

    mvn -DFUSE_HOME=${FUSE_HOME} -Pquickstarts clean install
    
To run the other (non-quickstarts, non-ose) tests

1. Run fuse, and create a fabric.  (TODO check, is this necessary?)
2. Run the tests with the following command:

    mvn -DFUSE_HOME=${FUSE_HOME} -Pnoquickstarts clean install
    
To run the OSE tests

    mvn -Pose -DOSE_USERNAME=demo -DOSE_PASSWORD=openshift -DOSE_PORT=42777 -DOSE_HOSTNAME=fud-mynamespace.openshift.example.com \
        -DFUSE_USER=admin FUSE_PASSWORD=jbXPmnSeYhDu clean install
    
    