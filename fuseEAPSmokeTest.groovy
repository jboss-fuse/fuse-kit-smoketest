stage 'define tools'
// Get the tools and override EVs which may be set on the node
def M2_HOME = tool 'maven-3.3.9'
def JAVA_HOME = tool 'jdk7'
env.JAVA_HOME = "${JAVA_HOME}"
env.M2_HOME = "${M2_HOME}"
env.PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"

// Get the zipfile name and home directory from the full download URL
env.FUSE_INSTALLER_URL = "${FUSE_INSTALLER_URL}"
env.WILDFLY_KIT_URL = "${WILDFLY_KIT_URL}"

def wildflyLastSlash = WILDFLY_KIT_URL.lastIndexOf("/");
def wildflyZipFileName = WILDFLY_KIT_URL.substring(wildflyLastSlash + 1, WILDFLY_KIT_URL.length());

def lastSlash = FUSE_INSTALLER_URL.lastIndexOf("/");
def jarFileName = FUSE_INSTALLER_URL.substring(lastSlash + 1, FUSE_INSTALLER_URL.length());

//def temp = zipFileName.substring(0, zipFileName.length() - 4); // strip off .zip
//def version = temp.substring("esb-project-7.0.0.fuse-".size());
def fuseHome = "jboss-eap-7.1"

env.FUSE_HOME = "${fuseHome}"

currentBuild.description = fuseHome

stage 'cleanup from previous runs'
if (isUnix()) {
    sh 'pkill -f jboss-modules.jar || true'  // TODO can we do something similar on windows?
}
cleanup("jboss-eap-7.1")

stage 'download kit'
downloadAndUnzipKit(WILDFLY_KIT_URL, wildflyZipFileName)
downloadAndRunFuseInstaller(FUSE_INSTALLER_URL, jarFileName, fuseHome)
//uncommentAdminUserPassword(fuseHome)

try {
    // Build and deploy the quickstarts
    stage 'Build Quickstarts'
    maven('--version')
    maven("--file ${fuseHome}/quickstarts/camel/pom.xml --fail-never clean install")

    stage 'Start Fuse EAP server'
    sh("bin/standalone.sh -c standalone-full.xml &")
    sleep 120

    stage 'deploy quickstarts'
    maven('--version')
    maven("--file ${fuseHome}/quickstarts/camel/pom.xml --fail-never -Pdeploy install")

} finally {
    stage 'Final shutdown'
    try {
        if (isUnix()) {
            sh 'pkill -f jboss-modules.jar || true'  // TODO can we do something similar on windows?
        }
    } catch (Exception e) {
        echo 'Ignoring exception on server shutdown'
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

def downloadAndRunFuseInstaller(downloadUrl, zipFileName, fuseHome) {
    if (isUnix()) {
        //sh 'wget --no-verbose --no-check-certificate ' + downloadUrl
        sh 'curl --remote-name --insecure --silent ' + downloadUrl
        sleep 20
        sh 'java -jar ' + pwd() + `\/` + zipFileName + ' ' + fuseHome
    } else {
        bat 'wget --no-verbose --no-check-certificate ' + downloadUrl
        bat 'java -jar ' + zipFileName + ' ' + fuseHome
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
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-cbr/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-eips/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-errorhandler/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/beginner-camel-log-wiki/' + version)

    executeClientCommand(fuseHomeDirectory, 'features:install cxf')
    executeClientCommand(fuseHomeDirectory, 'features:install fabric-cxf')
    executeClientCommand(fuseHomeDirectory, 'features:install cxf-ws-security')

    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-code-first/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-camel-cxf-contract-first/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-rest/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-rest/' + version)   // FIXME
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-soap/' + version)
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/cxf-secure-soap/' + version)

    executeClientCommand(fuseHomeDirectory, 'features:install camel-box')
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-box/' + version)
    executeClientCommand(fuseHomeDirectory, 'features:install camel-linkedin')
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-linkedin/' + version)
    executeClientCommand(fuseHomeDirectory, 'features:install camel-salesforce')
    executeClientCommand(fuseHomeDirectory, 'osgi:install -s mvn:org.jboss.quickstarts.fuse/camel-salesforce/' + version)

}
