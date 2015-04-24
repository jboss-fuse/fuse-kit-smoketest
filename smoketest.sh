#
# TODO check parameters
#
export OSE_RPM=$1
export APP_NAME=smoketestapp
export SMOKETESTNAMESPACE=smokenamespace
rhc setup --rhlogin demo --password openshift --create-token --server vm.openshift.example.com
rhc app-delete --confirm ${APP_NAME} 
sudo oo-admin-ctl-cartridge --command delete --name fuse-6.2.0
sudo yum remove --assumeyes openshift-origin-cartridge-fuse
rm -rf *.rpm
wget $OSE_RPM
sudo yum --assumeyes localinstall $OSE_RPM
sudo oo-admin-ctl-cartridge --command import-profile --activate
sudo oo-admin-ctl-cartridge --command list 

#
# Create the smoketest app
# 
rhc app-create ${APP_NAME} fuse-6.2.0 --namespace ${SMOKETESTNAMESPACE}ls
rhc apps
rhc show-app ${APP_NAME}

#
# Find the fuse password and OSE...port
#
export OPENSHIFT_APP_UUID=`rhc show-app ${APP_NAME} | grep SSH | sed 's/^.*SSH:[ \t]*//g' | sed 's/@.*//g'`
export FUSE_PASSWORD=`sudo cat /var/lib/openshift/${OPENSHIFT_APP_UUID}/fuse/container/etc/users.properties | sed 's/admin=//g' | sed 's/,.*//g'`
export OPENSHIFT_FUSE_DOMAIN_SSH_PORT=`sudo cat /var/lib/openshift/${OPENSHIFT_APP_UUID}/.env/OPENSHIFT_FUSE_DOMAIN_SSH_PORT`
echo OPENSHIFT_APP_UUID ${OPENSHIFT_APP_UUID}
echo FUSE_PASSWORD ${FUSE_PASSWORD}
echo OPENSHIFT_FUSE_DOMAIN_SSH_PORT ${OPENSHIFT_FUSE_DOMAIN_SSH_PORT}

#
# Run the smoke tests
#
mvn -Pose -DOSE_USERNAME=demo -DOSE_PASSWORD=openshift -DOSE_PORT=${OPENSHIFT_FUSE_DOMAIN_SSH_PORT} -DOSE_HOSTNAME=${APP_NAME}-${SMOKETESTNAMESPACE}.openshift.example.com -DFUSE_USER=admin -DFUSE_PASSWORD=${FUSE_PASSWORD} clean install
