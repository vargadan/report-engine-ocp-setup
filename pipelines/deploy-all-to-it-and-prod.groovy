node {

	def APP_1 = "report-order-manager"
	def APP_2 = "dummy-report-factory"
	def APP_3 = "identity-server"
	def APP_4 = "report-renderer"
	def APP_5 = "report-repository"
	def APP_6 = "report-uploader"
  
   	def IT_PROJECT = "ctr-it"
   	def PROD_PROJECT = "ctr-prod"
   	
   	def VERSION = "0.0.1"
   	
   	def PORT = 8080
   	
   	def verbose = false;
   	
   	stage('to IT') {
   		envSetup(IT_PROJECT, APP_1, VERSION, PORT, true)
   		envSetup(IT_PROJECT, APP_2, VERSION, PORT, true)
   		envSetup(IT_PROJECT, APP_3, VERSION, PORT, true)
   		envSetup(IT_PROJECT, APP_4, VERSION, PORT, true)
   		envSetup(IT_PROJECT, APP_5, VERSION, PORT, true)
   		envSetup(IT_PROJECT, APP_6, VERSION, PORT, true)
   		
   		openshiftVerifyDeployment(deploymentConfig: APP_1, namespace: IT_PROJECT, verbose: verbose)
   		openshiftVerifyDeployment(deploymentConfig: APP_2, namespace: IT_PROJECT, verbose: verbose)
   		openshiftVerifyDeployment(deploymentConfig: APP_3, namespace: IT_PROJECT, verbose: verbose)
   		openshiftVerifyDeployment(deploymentConfig: APP_4, namespace: IT_PROJECT, verbose: verbose)
   		openshiftVerifyDeployment(deploymentConfig: APP_5, namespace: IT_PROJECT, verbose: verbose)
   		openshiftVerifyDeployment(deploymentConfig: APP_6, namespace: IT_PROJECT, verbose: verbose)
   	}
   	
   	stage('to PROD') {
   		sh "oc tag ctr-cicd/${APP_1}:latest ${PROD_PROJECT}/${APP_1}:latest"
   		sh "oc tag ctr-cicd/${APP_2}:latest ${PROD_PROJECT}/${APP_2}:latest"
   		sh "oc tag ctr-cicd/${APP_3}:latest ${PROD_PROJECT}/${APP_3}:latest"
   		sh "oc tag ctr-cicd/${APP_4}:latest ${PROD_PROJECT}/${APP_4}:latest"
   		sh "oc tag ctr-cicd/${APP_5}:latest ${PROD_PROJECT}/${APP_5}:latest"
   		sh "oc tag ctr-cicd/${APP_6}:latest ${PROD_PROJECT}/${APP_6}:latest"   		
   	}
}


def envSetup(project, appName, version, port, recreate) {

	GET_DC_OUT = sh (
		script: "oc get deploymentconfig -l app=${appName} -n ${project}",
		returnStdout: true
	).trim()
	
	echo "GET_DC_OUT : ${GET_DC_OUT}"
	appExists = GET_DC_OUT.contains(appName)
	if (appExists && !recreate) {
		//since the app exists and we dont recreate it we can exit
		return
	} else if (appExists && recreate) {
		//we can delete the app if we want to recreate
		sh "oc delete deploymentconfig,service,routes -l app=${appName} -n ${project}"
	}
	
	sh "oc tag ctr-cicd/${appName}:latest ${project}/${appName}:latest"	
	
	//now we can create the app since it has either been deleted or it did not exist at all
 	sh "oc new-app ${appName}:latest -n ${project}"
   	sh "oc delete service,routes -l app=${appName} -n ${project}"
   	sh "oc create service clusterip ${appName} --tcp=${port}:${port} -n ${project}"
   	sh "oc expose service ${appName} -n ${project}"		
}