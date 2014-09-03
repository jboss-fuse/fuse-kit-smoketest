fuse-kit-smoketest
==================

These tests are intended to be run as part of a Fuse kit smoke test.  They test a subset of the quickstarts.  
 
The smoketest is primarily driven by a Jenkins job.  Before running these tests, the following needs to occur:

1. All quickstarts are build
2. Fuse is running
3. Any quickstarts that need to be tested have been deployed.

The tests must be run with FUSE_HOME set: 
    mvn -DFUSE_HOME=/Users/kearls/fuse/jboss-fuse-6.2.0.redhat-016/ clean install
    

