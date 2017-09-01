stage 'define tools'
// Get the tools and override EVs which may be set on the node
def M2_HOME = tool 'maven-3.3.9'
def JAVA_HOME = tool 'jdk8'
env.JAVA_HOME = "${JAVA_HOME}"
env.M2_HOME = "${M2_HOME}"
env.PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"

// Get the zipfile name and home directory from the full download URL
env.FUSE_KIT_URL = "${FUSE_KIT_URL}"

def lastSlash = FUSE_KIT_URL.lastIndexOf("/");
def zipFileName = FUSE_KIT_URL.substring(lastSlash + 1, FUSE_KIT_URL.length());
def temp = zipFileName.substring(0, zipFileName.length() - 4); // strip off .zip
def version = temp.substring("jboss-fuse-karaf-7.0.0.fuse-".size());
def quickstartVersion = temp.substring("jboss-fuse-karaf-".size());
def fuseHome = "jboss-fuse-karaf-7.0.0.fuse-" + version

env.FUSE_HOME = "${fuseHome}"

currentBuild.description = fuseHome

stage 'cleanup from previous runs'
if (isUnix()) {
    sh 'pkill -f org.apache.karaf.main.Main || true'  // TODO can we do something similar on windows?
}
cleanup("jboss-fuse-karaf-7.0.0.fuse-*")

stage 'download kit'
downloadAndUnzipKit(FUSE_KIT_URL, zipFileName)
uncommentAdminUserPassword(fuseHome)

try {
    // Start the broker
    stage 'starting broker'
    startBroker(fuseHome)
    sleep 120

    // Build and deploy the quickstarts
    stage 'Build Quickstarts'
    maven('--version')
    maven("--file ${fuseHome}/quickstarts/beginner/pom.xml --fail-never clean install")

    stage 'deploy quickstarts'
    deployQuickstarts(fuseHome, quickstartVersion)

    stage 'Quickstart tests'
    if (isUnix()) {
        maven('-DFUSE_HOME=${PWD}/${FUSE_HOME} -Pquickstarts -Dsurefire.rerunFailingTestsCount=2 -Djboss.fuse.bom.version=' + version + ' clean test')
    } else {
        maven('-DFUSE_HOME=' + fuseHome + ' -Pquickstarts -Dsurefire.rerunFailingTestsCount=2 -Djboss.fuse.bom.version=' + version + ' clean test')
    }
    stage 'Create a fabric'
    executeClientCommand(fuseHome, 'fabric:create --wait-for-provisioning --bootstrap-timeout 300000')

    stage 'Other tests'
    // deploy the camel-cbr quickstart first, the CreateChildContainerTest depends on the profile
    maven("--file ${fuseHome}/quickstarts/beginner/camel-cbr/pom.xml fabric8:deploy")
    maven('-DFUSE_HOME=' + fuseHome + ' -Dsurefire.rerunFailingTestsCount=2 -Pnoquickstarts -Djboss.fuse.bom.version=' + version + ' clean test')
} finally {
    stage 'Final shutdown'
    try {
        stopBroker(fuseHome)
    } catch (Exception e) {
        echo 'Ignoring exception on broker shutdown'
    }
    stage 'shutdown complete'
    // FIXME!!!! step([$class: 'JUnitResultArchiver', testDataPublishers: [[$class: 'JUnitFlakyTestDataPublisher']], testResults: '**/target/*-reports/*.xml']

    if (!isUnix()) {
        build job: 'Reboot_windows', quietPeriod: 30, wait: false
    }
}

// TODO find somewhere to put this code so it can be shared.
def cleanup(directoryName) {
    if (isUnix()) {
        sh 'rm -rf ' + directoryName
    } else {
        bat 'rm -rf ' + directoryName
    }
}

def downloadAndUnzipKit(downloadUrl, zipFileName) {
    if (isUnix()) {
        //sh 'wget --no-verbose --no-check-certificate ' + downloadUrl
        sh 'curl --remote-name --insecure --silent ' + downloadUrl
        sh 'unzip -q ' + zipFileName
    } else {
        bat 'wget --no-verbose --no-check-certificate ' + downloadUrl
        bat 'unzip -q ' + zipFileName
    }
}

def uncommentAdminUserPassword(fuseHomeDirectory) {
    if (isUnix()) {
        // sh 'sed -i \'s/^#admin/admin/g\' ' + fuseHomeDirectory + '/etc/users.properties'
        // Workaround for AIX
        sh 'echo "admin=admin,admin,manager,viewer,Monitor, Operator, Maintainer, Deployer, Auditor, Administrator, SuperUser" >> ' + fuseHomeDirectory + '/etc/users.properties'

    } else {
        bat 'sed -i \'s/^#admin/admin/g\' ' + fuseHomeDirectory + '/etc/users.properties'
    }
}

def updateFuseBomVersion(version) {
    if (isUnix()) {
        sh "sed -i 's/<jboss.fuse.bom.version>.*<\\/jboss.fuse.bom.version>/<jboss.fuse.bom.version>${version}<\\/jboss.fuse.bom.version>/g' pom.xml"
        sh 'grep bom.version pom.xml'

    }
}

def startBroker(fuseHomeDirectory) {
    if (isUnix()) {
        sh './' + fuseHomeDirectory + '/bin/start'
    } else {
        bat fuseHomeDirectory + '\\bin\\start'
    }
}

def stopBroker(fuseHomeDirectory) {
    if (isUnix()) {
        sh './' + fuseHomeDirectory + '/bin/stop || true'  // TODO ignore errors only on final shutdown
    } else {
        bat fuseHomeDirectory + '\\bin\\stop'
    }
}

def executeClientCommand(fuseHomeDirectory, command) {  // TODO always assume -u admin -p admin?
    if (isUnix()) {
        sh './' + fuseHomeDirectory + '/bin/client -u admin -p admin \"' + command + '\"'
    } else {
        bat fuseHomeDirectory + '\\bin\\client -u admin -p admin -h localhost \"' + command + '\"'
    }
}

def maven(command) {
    if (isUnix()) {
        sh 'mvn ' + command
    } else {
        bat 'mvn ' + command
    }
}

def deployQuickstarts(fuseHomeDirectory, version) {
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/beginner-camel-cbr/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/beginner-camel-eips/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/beginner-camel-errorhandler/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/beginner-camel-log/' + version)
    // beginner-camel-log-wiki does not seem like it is needed for fuse 7
    //executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/beginner-camel-log-wiki/' + version)

    executeClientCommand(fuseHomeDirectory, 'feature:install cxf')
    executeClientCommand(fuseHomeDirectory, 'feature:install fabric-cxf')
    executeClientCommand(fuseHomeDirectory, 'feature:install cxf-ws-security')

    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-camel-cxf-code-first/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-camel-cxf-contract-first/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-rest/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-secure-rest/' + version)   // FIXME
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-soap/' + version)
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/cxf-secure-soap/' + version)

    executeClientCommand(fuseHomeDirectory, 'feature:install camel-box')
    // failing, filed ENTESB-7249
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-box/' + version)
    executeClientCommand(fuseHomeDirectory, 'feature:install camel-linkedin')
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-linkedin/' + version)
    executeClientCommand(fuseHomeDirectory, 'feature:install camel-salesforce')
    executeClientCommand(fuseHomeDirectory, 'bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-salesforce/' + version)

/*  FIXME check bugs and see which of these should still be working
    ###### ${FUSE_HOME}/bin/client bundle:install -s mvn:org.jboss.quickstarts.fuse/camel-amq/${VERSION}"

    ${FUSE_HOME}/bin/client feature:install camel-olingo2"
    #ENTESB-5048
    #${FUSE_HOME}/bin/client bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-odata/${VERSION}"

    #####${FUSE_HOME}/bin/client feature:install camel-sap"
    #####${FUSE_HOME}/bin/client bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-sap/${VERSION}"
*/
}
