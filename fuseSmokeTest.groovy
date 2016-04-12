


    stage 'define tools'
// Get the tools and override EVs which may be set on the node
    def M2_HOME = tool 'maven-3.2.3'  // TODO fix 3.3.3 on windows node
    def JAVA_HOME = tool 'jdk8'
    env.JAVA_HOME="${JAVA_HOME}"
    env.M2_HOME="${M2_HOME}"
    env.PATH = "${M2_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"

// Get the zipfile name and home directory from the full download URL
    env.FUSE_KIT_URL = "${FUSE_KIT_URL}"

    def lastSlash = FUSE_KIT_URL.lastIndexOf("/");
    def zipFileName = FUSE_KIT_URL.substring(lastSlash + 1, FUSE_KIT_URL.length());
    def temp = zipFileName.substring(0, zipFileName.length() - 4); // strip off .zip
    def version = temp.substring("jboss-fuse-full-".size());
    def fuseHome = "jboss-fuse-" + version

    env.ZIPFILENAME="${zipFileName}"
    env.FUSE_HOME="${fuseHome}"
    env.VERSION="${version}"
//env.WORKSPACE="${PWD}"    // FIXME do I need this?

    def unix = isUnix()

    stage 'cleanup from previous runs'
    if (unix) {
        sh 'pkill -f org.apache.karaf.main.Main || true'
        sh 'rm -rf jboss-fuse*'
        sh 'env | sort'
    } else {
        bat 'rm -rf jboss-fuse*'
    }

// Download the kit and unzip it
    stage 'download'
    if(unix) {
        sh 'wget --no-verbose ${FUSE_KIT_URL}'
        sh 'unzip -q ${ZIPFILENAME}'
    } else {
        bat 'wget --no-verbose --no-check-certificate %FUSE_KIT_URL%'
        bat 'unzip -q %ZIPFILENAME%'
    }

// 3. Uncomment admin user in etc/user.properties
    if (unix) {
        sh 'sed -i \'s/^#admin/admin/g\' ${FUSE_HOME}/etc/users.properties'
    } else {
        bat 'sed -i \'s/^#admin/admin/g\' %FUSE_HOME%/etc/users.properties'
    }

    try {
        // 4. Start the broker
        stage 'starting broker'
        if (unix) {
            sh './${FUSE_HOME}/bin/start'
        } else {
            bat '%FUSE_HOME%\\bin\\start'
        }
        sleep 120

        // Build and deploy the quickstarts
        stage 'Build and Deploy Quickstarts'
        if (unix) {
            //sh 'cd ${FUSE_HOME}/quickstarts'
            sh 'pwd'
            sh 'ls'
            sh 'mvn --version'
            sh 'mvn --file ${FUSE_HOME}/quickstarts/pom.xml clean install'

            echo "Deploying quickstarts"
            sh './deployQuickStarts.sh'
        } else {
            // TODO
        }

        stage 'Quickstart tests'
        if (unix) {
            sh 'mvn -DFUSE_HOME=${PWD}/${FUSE_HOME} -Dsurefire.rerunFailingTestsCount=2 -Pquickstarts clean test'
        } else {
            // TODO
        }

        stage 'Create a fabric'
        if (unix) {
            sh '${FUSE_HOME}/bin/client -u admin -p admin "fabric:create --wait-for-provisioning"'
        } else {
            // FIXME
        }

        stage 'Other tests'
        if (unix) {
            sh 'mvn -DFUSE_HOME=${PWD}/${FUSE_HOME} -Dsurefire.rerunFailingTestsCount=2 -Pnoquickstarts clean test'
        } else {
            // TODO
        }

    } finally {
        stage 'Final shutdown'
        if (unix) {
            sh './${FUSE_HOME}/bin/stop'
        } else {
            bat '%FUSE_HOME%\\bin\\stop'
        }

        echo "Shutdown complete"
        stage 'shutdown complete'
        step([$class: 'JUnitResultArchiver', testDataPublishers: [[$class: 'JUnitFlakyTestDataPublisher']], testResults: '**/target/*-reports/*.xml'])


        if (!unix) {
            build job: 'Reboot_windows', quietPeriod: 30, wait: false
        } else {
            stage 'clear out workspace'
            deleteDir()  //Looks like we can't do this on windows
        }
    }