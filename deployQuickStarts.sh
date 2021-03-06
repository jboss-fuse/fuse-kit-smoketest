#!/usr/bin/env bash
set -x

#if [ -z "$FUSE_HOME" ]; then export FUSE_HOME=${HOME}/fuse/jboss-fuse-${VERSION};  fi
if [ -z "$FUSE_HOME" ];
then
    echo "FUSE_HOME must be set"
    exit
fi

if [ ! -d "$FUSE_HOME" ]; then
    echo "FUSE_HOME: $FUSE_HOME does not exist"
    exit
fi



VERSION="${FUSE_HOME//*fuse-/}"
VERSION=$(echo $VERSION | sed 's/\///g')

echo "Using FUSE_HOME: $FUSE_HOME and VERSION $VERSION"

echo Starting at `date`

${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-cbr/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-eips/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-errorhandler/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log-wiki/${VERSION}"

${FUSE_HOME}/bin/client -u admin -p admin "features:install cxf"
${FUSE_HOME}/bin/client -u admin -p admin "features:install fabric-cxf"
${FUSE_HOME}/bin/client -u admin -p admin "features:install cxf-ws-security"

${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-code-first/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-contract-first/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-rest/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-rest/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-soap/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-soap/${VERSION}"

###### ${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-amq/${VERSION}"

${FUSE_HOME}/bin/client -u admin -p admin "features:install camel-box"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-box/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "features:install camel-linkedin"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-linkedin/${VERSION}"
${FUSE_HOME}/bin/client -u admin -p admin "features:install camel-salesforce"
${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-salesforce/${VERSION}"

${FUSE_HOME}/bin/client -u admin -p admin "features:install camel-olingo2"
#ENTESB-5048
#${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-odata/${VERSION}"

#####${FUSE_HOME}/bin/client -u admin -p admin "features:install camel-sap"
#####${FUSE_HOME}/bin/client -u admin -p admin "osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-sap/${VERSION}"
echo finished at `date`


