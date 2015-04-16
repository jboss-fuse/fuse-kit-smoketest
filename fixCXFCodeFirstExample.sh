#
#
#
sed -i 's/address=\"\/order\/"/address=\"\/orderCode\/"/g' ${FUSE_HOME}/quickstarts/cxf/camel-cxf-code-first/src/main/resources/OSGI-INF/blueprint/camel-route.xml
