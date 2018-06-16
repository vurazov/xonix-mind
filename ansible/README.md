# Installation and run
ansible folder contains scripts to prepare environment on PROD server and deploy the thumbtack-xonix-mind application on it.


## Local environment
To run deployment scripts the Ansible should be installed on local machine.

## Local deployment for testing
It is useful to start deploy against prod-like environment locally.
LXC container can be used for that. It is used the same OS as on PROD.
You need to install Vagrant from here:
https://www.vagrantup.com/downloads.html
And also install LXC and vagrant LXC provider:
```
sudo apt-get install lxc
vagrant plugin install vagrant-lxc 
```
To start LXC container locally:
```
cd thumbtack-xonix-mind/ansible
./deploy_local_lxc.sh
```
it will run Debian on 10.0.3.16 port.
And application wil be up an running on:
http://10.0.3.16:8888

The Jenkins will be:
http://10.0.3.16:8080


## Deploy on PROD
To thumbtack-xonix-mind/ansible add "prod" folder and add to it inventory for prod environment.
Then:
```
cd thumbtack-xonix-mind/ansible
./deploy.sh
```


