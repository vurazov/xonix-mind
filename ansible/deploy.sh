#!/usr/bin/env bash
cd ..
mvn clean install
cd ansible
ansible-playbook playbook.yml -v -i prod