




echo Starting at `date`

%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-cbr/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-eips/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-errorhandler/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log-wiki/%VERSION%"

%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install cxf"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install fabric-cxf"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install cxf-ws-security"

%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-code-first/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-contract-first/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-rest/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-rest/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-soap/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-soap/%VERSION%"

###### %FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-amq/%VERSION%"

%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install camel-box"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-box/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install camel-linkedin"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-linkedin/%VERSION%"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install camel-salesforce"
%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-salesforce/%VERSION%"

%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install camel-olingo2"
#ENTESB-5048
#%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-odata/%VERSION%"

#####%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "features:install camel-sap"
#####%FUSE_HOME%\bin\client.bat -u admin -p admin -h localhost  "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-sap/%VERSION%"
echo finished at `date`

