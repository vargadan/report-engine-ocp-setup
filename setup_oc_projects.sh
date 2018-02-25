#!/bin/bash

#create projects
oc new-project ctr-env --display-name="Client Tax Reporting - Environment Automation"
oc new-project ctr-cicd --display-name="Client Tax Reporting - CICD"
oc new-project ctr-dev --display-name="Client Tax Reporting - DEV"
oc new-project ctr-it --display-name="Client Tax Reporting - IT"
oc new-project ctr-prod --display-name="Client Tax Reporting - PROD"

#create roles
./roles/create-pipeline-roles.sh

oc policy add-role-to-user environment-pipeline system:serviceaccount:ctr-env:jenkins -n ctr-cicd
oc policy add-role-to-user environment-pipeline system:serviceaccount:ctr-env:jenkins -n ctr-dev
oc policy add-role-to-user application-pipeline system:serviceaccount:ctr-cicd:jenkins -n ctr-dev

oc policy add-role-to-user environment-pipeline system:serviceaccount:ctr-env:jenkins -n ctr-it
oc policy add-role-to-user application-pipeline system:serviceaccount:ctr-cicd:jenkins -n ctr-it

oc policy add-role-to-user environment-pipeline system:serviceaccount:ctr-env:jenkins -n ctr-prod
oc policy add-role-to-user application-pipeline system:serviceaccount:ctr-cicd:jenkins -n ctr-prod

#create environment automation resources
oc process -f templates/pipelines.environment.yaml | oc create -f - -n ctr-env
#create application cicd resources
oc process -f templates/nexus.yaml | oc create -f - -n ctr-cicd
oc process -f templates/pipelines.application.yaml | oc create -f - -n ctr-cicd

oc project ctr-cicd