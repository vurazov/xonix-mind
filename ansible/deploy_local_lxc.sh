#!/usr/bin/env bash
cd lxc
vagrant destroy -f
vagrant up
cd ../..
mvn clean install -DskipTests
cd ansible
ansible-playbook playbook.yml -v -i lxctest