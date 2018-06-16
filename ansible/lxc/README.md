
https://askubuntu.com/questions/399195/how-do-i-setup-a-ldap-backed-irc-server
https://lowendbox.com/blog/how-to-setup-your-own-irc-server-using-inspircd-on-ubuntu/

sudo apt-get update
sudo apt-get install inspircd
sudo nano /etc/inspircd/inspircd.conf

<bind address="10.0.3.15" port="6667" type="clients">
<bind address="" port="6667" type="clients">


<module name="m_ldapauth.so">
<ldapauth baserdn="cn=admin,dc=vagrant,dc=local"
          attribute="uid"
          server="ldap://10.0.3.15"
          allowpattern="Guest*"
          killreason="Access denied"
          searchscope="subtree"
          binddn=""
          bindauth=""
          verbose="yes"
          userfield="yes" >



sudo service inspircd stop
sudo service inspircd start

sudo service slapd stop
sudo service slapd start

sudo ufw allow ldap

sudo apt install weechat-curses
weechat
/server add dalnet 10.0.3.15/6667
/set irc.server.dalnet.nicks «ник1,ник2,ник3,ник4,ник5″
/set irc.server.dalnet.username "Имя_пользователя"
/set irc.server.dalnet.realname "Реальное_имя"

#/set irc.server.dalnet.autoconnect on
/set irc.server.dalnet.autojoin "#channel1,#channel2"
#/set irc.server.dalnet.command "/msg nickserv identify xxxxxx"
/save

/connect dalnet
/join #channel2
msg nickserv register pass mail

/quit
/msg NickServ REGISTER password youremail@example.com
/msg NickServ IDENTIFY foo password

/connect dalnet 6667 root:12345


------
sudo apt-get install irssi
irssi
/connect 10.0.3.15
/oper root 12345



/connect -ssl 10.0.3.15 6667 stuff JoeBloggs


/connect  10.0.3.15 6667 root2 root3
/quit

irssi
/connect  10.0.3.15 20000
/QUOTE PASS emek:Yc6Aj2:tlswe
/QUOTE PASS cbalz:M475Gd:tlswe

/server add -net mynode 10.0.3.15 6697 emekhanikov:abcde

sudo ngircd -n

sudo bip -n -f /home/vagrant/.bip/bip.conf

sudo systemctl status ngircd.service
sudo journalctl -xe