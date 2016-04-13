stage 'define tools'
// Get the tools and override EVs which may be set on the node
def M2_HOME = tool 'maven-3.2.3'  // TODO fix 3.3.3 on windows node
def JAVA_HOME = tool 'jdk8'
env.JAVA_HOME = "${JAVA_HOME}"
env.M2_HOME = "${M2_HOME}"
env.PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"

// Get the zipfile name and home directory from the full download URL
env.FUSE_KIT_URL = "${FUSE_KIT_URL}"

def lastSlash = FUSE_KIT_URL.lastIndexOf("/");
def zipFileName = FUSE_KIT_URL.substring(lastSlash + 1, FUSE_KIT_URL.length());
def temp = zipFileName.substring(0, zipFileName.length() - 4); // strip off .zip
def version = temp.substring("jboss-fuse-full-".size());
def fuseHome = "jboss-fuse-" + version

// TODO doI need all of these?
env.ZIPFILENAME = "${zipFileName}"
env.FUSE_HOME = "${fuseHome}"
env.VERSION = "${version}"
//env.WORKSPACE="${PWD}"    // FIXME do I need this?

stage 'cleanup from previous runs'
if (isUnix()) {
    sh 'pkill -f org.apache.karaf.main.Main || true'  // TODO can we do something similar on windows?
}
cleanup("jboss-fuse*")

stage 'download kit'
downloadAndUnzipKit(FUSE_KIT_URL, zipFileName)
uncommentAdminUserPassword(fuseHome)

sh 'cat ' + fuseHome + '/etc/user.properties'

try {
    // Start the broker
    stage 'starting broker'
    startBroker(fuseHome)
    sleep 120

    // Build and deploy the quickstarts
    stage 'Build and Deploy Quickstarts'
    maven('--version')
    echo '>>>> maven --file ${fuseHome}/quickstarts/pom.xml '
    maven("--file ${fuseHome}/quickstarts/pom.xml clean install")

    stage 'deploy quickstarts'
    // FIXME we need a windows version
    if (isUnix()) {
        sh './deployQuickStarts.sh'
    } else {
        // TODO
    }

    stage 'Quickstart tests'
    // TODO how do deal with ${PWD} here
    maven('-DFUSE_HOME=${PWD}/${FUSE_HOME} -Dsurefire.rerunFailingTestsCount=2 -Pquickstarts clean test')

    /// TODO we need a client command method
    stage 'Create a fabric'
    executeClientCommand(fuseHome, '-u admin -p admin "fabric:create --wait-for-provisioning"')
    /*
    if (isUnix()) {
        sh '${FUSE_HOME}/bin/client -u admin -p admin "fabric:create --wait-for-provisioning"'
    } else {
        // FIXME
    }*/

    stage 'Other tests'
    maven('-DFUSE_HOME=${PWD}/${FUSE_HOME} -Dsurefire.rerunFailingTestsCount=2 -Pnoquickstarts clean test')
} finally {
    stage 'Final shutdown'
    stopBroker(fuseHome)


    echo "Shutdown complete"
    stage 'shutdown complete'
    // FIXME!!!! step([$class: 'JUnitResultArchiver', testDataPublishers: [[$class: 'JUnitFlakyTestDataPublisher']], testResults: '**/target/*-reports/*.xml'])


    if (!unix) {
        build job: 'Reboot_windows', quietPeriod: 30, wait: false
    } else {
        //stage 'clear out workspace'
        //deleteDir()  //Looks like we can't do this on windows  FIXME on unix do this here or in caller
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
        sh 'wget --no-verbose ' + downloadUrl
        sh 'unzip -q ' + zipFileName
    } else {
        bat 'wget --no-verbose --no-check-certificate ' + downloadUrl
        bat 'unzip -q ' + zipFileName
    }
}

def uncommentAdminUserPassword(fuseHomeDirectory) {
    if (isUnix()) {
        sh 'sed -i \'s/^#admin/admin/g\' ' + fuseHomeDirectory + '/etc/users.properties'
    } else {
        bat 'sed -i \'s/^#admin/admin/g\' ' + fuseHomeDirectory + '/etc/users.properties'
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
        sh './' + fuseHomeDirectory + '/bin/stop'
    } else {
        bat fuseHomeDirectory + '\\bin\\stop'
    }
}

def executeClientCommand(fuseHomeDirectory, command) {
    if (isUnix()) {
        sh './' + fuseHomeDirectory + '/bin/client ' + command
    } else {
        bat fuseHomeDirectory + '\\bin\\client '
    }
}

def maven(command) {
    if (isUnix()) {
        sh 'mvn ' + command
    } else {
        bat 'mvn ' + command
    }
}