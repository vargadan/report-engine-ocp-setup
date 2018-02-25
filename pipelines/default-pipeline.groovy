node('maven') {
   // define commands
   def mvnCmd = "mvn -s configuration/cicd-settings.xml"

   stage 'Build'
   git branch: 'eap-7', url: 'http://gogs:3000/gogs/openshift-tasks.git'
   def v = version()
   sh "${mvnCmd} clean install -DskipTests=true"

   stage 'Test and Analysis'
   parallel (
       'Test': {
           sh "${mvnCmd} test"
           step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
       },
       'Static Analysis': {
           sh "${mvnCmd} jacoco:report sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -DskipTests=true"
       }
   )

   stage 'Push to Nexus'
   sh "${mvnCmd} deploy -DskipTests=true"

   stage 'Deploy DEV'
   sh "rm -rf oc-build && mkdir -p oc-build/deployments"
   sh "cp target/openshift-tasks.war oc-build/deployments/ROOT.war"
   // clean up. keep the image stream
   sh "oc project ${DEV_PROJECT}"
   sh "oc delete bc,dc,svc,route -l app=tasks -n ${DEV_PROJECT}"
   // create build. override the exit code since it complains about exising imagestream
   sh "oc new-build --name=tasks --image-stream=jboss-eap70-openshift --binary=true --labels=app=tasks -n ${DEV_PROJECT} || true"
   // build image
   sh "oc start-build tasks --from-dir=oc-build --wait=true -n ${DEV_PROJECT}"
   // deploy image
   sh "oc new-app tasks:latest -n ${DEV_PROJECT}"
   sh "oc expose svc/tasks -n ${DEV_PROJECT}"

   stage 'Deploy STAGE'
   input message: "Promote to STAGE?", ok: "Promote"
   sh "oc project ${STAGE_PROJECT}"
   // tag for stage
   sh "oc tag ${DEV_PROJECT}/tasks:latest ${STAGE_PROJECT}/tasks:${v}"
   // clean up. keep the imagestream
   sh "oc delete bc,dc,svc,route -l app=tasks -n ${STAGE_PROJECT}"
   // deploy stage image
   sh "oc new-app tasks:${v} -n ${STAGE_PROJECT}"
   sh "oc expose svc/tasks -n ${STAGE_PROJECT}"
}

def version() {
  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
  matcher ? matcher[0][1] : null
}
