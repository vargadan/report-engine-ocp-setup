node {

	def APP_ROM = "report-order-manager"
	def APP_DRF = "dummy-report-factory"
	def APP_ID = "identity-server"
	def APP_REN = "report-renderer"
	def APP_REP = "report-repository"
	def APP_UP = "report-uploader"
	def APPS = [APP_ROM, APP_DRF, APP_ID, APP_REN, APP_REP, APP_UP] as String[]
  
  	def DEV_PROJECT = "ctr-dev"
   	def IT_PROJECT = "ctr-it"
   	def PROD_PROJECT = "ctr-prod"
   	
   	def VERSION = "0.0.1"
   	
   	def verbose = false;

    def GIT_URL = "https://github.com/vargadan/reportengine-environment/";

   	stage ('Init') {
   		git branch: 'master', url: "${GIT_URL}"
                sh "pwd"
                sh "ls -la"
	}       
   	
   	stage('DEV setup') {
		deleteAll(DEV_PROJECT);
        rabbitMq(DEV_PROJECT);
		envSetup(DEV_PROJECT, APPS);
   	}
   	
   	stage('IT setup') {
		deleteAll(IT_PROJECT);
        rabbitMq(IT_PROJECT);
		envSetup(IT_PROJECT, APPS);
   	}

   	stage('PROD setup') {
		deleteAll(PROD_PROJECT);
        rabbitMq(PROD_PROJECT);
		envSetup(PROD_PROJECT, APPS);
   	}

}

def deleteAll(project) {
	sh "oc delete deploymentconfig,service,routes --all -n ${project}"
}

def rabbitMq(project) {
    sh "oc process -f templates/rabbitmq.yaml | oc create -f - -n ${project}"
}


def envSetup(project, appNames) {
	appNames.each {
		sh "oc process -f templates/app-comp-template.yaml -p APP_NAME=${appName} -p PROJECT_NAME=${project} | oc create -f - -n ${project}"
	}
}

def hasPods(project) {
	POD_COUNT = sh (
		script: "oc get pods -n ${project}",
		returnStdout: true
	).trim()
	return !"No resources found.".equals(POD_COUNT)
}