#/bin/bash!
oc delete clusterrole application-pipeline
oc delete clusterrole environment-pipeline
VPATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $VPATH
oc create -f $VPATH/application-pipeline-role.yaml
oc create -f $VPATH/environment-pipeline-role.yaml