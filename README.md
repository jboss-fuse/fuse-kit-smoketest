fuse-kit-smoketest
==================

These tests are intended to be run as part of a Fuse kit smoke test.   
 
To run the quickstart tests, do the following:

1. cd to ${FUSE_INSTALLATION}/quickstarts and build all of the quickstarts
2. Run fuse
3. Run the deployQuickStarts.sh script to deploy the quickstarts
4. Run the tests with the following command; FUSE_HOME must be set

    mvn -DFUSE_HOME=/Users/kearls/fuse/jboss-fuse-6.2.0.redhat-016/ -P quickstarts clean install
    

To run the other (non-quickstarts, non-ose) tests

1. Run fuse, and create a fabric.  (TODO check, is this necessary?)
2. Run the tests with the following command:

    mvn -Pnoquickstarts clean install
    
To run the OSE tests

TODO this requires much more information

    mvn -Pose -DOSE_USERNAME=demo OSE_PASSWORD=openshift -DOSE_PORT=42777 -DOSE_HOSTNAME=fud-mynamespace.openshift.example.com \
        FUSE_USER=admin FUSE_PASSWORD=jbXPmnSeYhDu clean install
    
    