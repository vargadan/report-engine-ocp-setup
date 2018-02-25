for i in $(seq 1 15);do oc --config ../aws-openshift/admin.kubeconfig replace -f pv$i.yaml;done
oc --config ../aws-openshift/admin.kubeconfig replace -f pv-ocr.yaml