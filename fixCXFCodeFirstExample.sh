#
#
#
if [ "$(uname)" == "Darwin" ]; then
    mv ${FUSE_HOME}/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route.xml ${FUSE_HOME}/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route-orig.xml
    sed 's/address=\"\/order\/"/address=\"\/orderCode\/"/g'  /Users/kearls/fuse/jboss-fuse-6.3.0.redhat-025/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route-orig.xml > ${FUSE_HOME}/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route.xml
else
   sed -i 's/address=\"\/order\/"/address=\"\/orderCode\/"/g' ${FUSE_HOME}/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route.xml
fi