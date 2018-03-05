# 'KuKu Mi' Install Guide

'KuKu Mi' project consists of DTH and SmartApp and API server.
It makes SmartThings to control Xiaomi product.
(Currently supports Mi Remote only)


# 0. Preparing

To use 'KuKu Mi', need below environments. 
- 'Docker' system tool
- X86 Architecture server
 

# 1. Install 'KuKu Mi' Daemon

1-1) 'KuKu Mi' Docker Image download

```
# docker search turlvo/kukumi
# docker pull turlvo/kukumi
```

1-2) Execute 'KuKu Mi' container 

```
# docker run --name=KuKuMi --net=host turlvo/kukumi
```

1-3) Enable auto run 'KuKuTVNoti' when rebooted

```
# sudo vim /etc/systemd/system/kukumi.service

<kukutvnoti.service File content>
[Unit]
Description=KuKuMi container
Requires=docker.service
After=docker.service

[Service]
Restart=always
ExecStart=/usr/bin/docker start -a KuKuMi
ExecStop=/usr/bin/docker stop -t 2 KuKuMi

[Install]
WantedBy=multi-user.target

# sudo systemctl enable /etc/systemd/system/kukumi.service
```


# 2. Setting of 'Mi Remote' at 'KuKu Mi Web server'
2-1) Connect to 'http://[KuKu Mi api Server's IP]:8484/miremote' using Web browser

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver1.jpg' width=400>


2-2) Add a 'Mi Remote' device (Discover or Manual)

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver2.jpg' width=400>
<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver2-1.jpg' width=400>


2-3) Add a commands by learning or add a commands by manually

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver3.jpg' width=400>
<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver3-1.jpg' width=400>
<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver3-2.jpg' width=400>


2-4) Added devices and commands screenshot

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/webserver4-1.jpg' width=400>


# 3. Installation of 'Mi Remote' DTH
3-1) Add 'KuKu Mi' DTH in ST IDE
3-2) Add a 'KuKu Mi' SmartApp in ST IDE

https://github.com/turlvo/KuKuMi
```
GitHub Repository Integration
- Owner : turlvo
- Name : KuKuMi
- Branch : master
```


# 4. Installation of 'KuKu Mi' SmartApp
4-1) Install 'KuKu Mi' SmartApp
- 'Add a SmartApp' -> 'My SmartApp' -> select a 'KuKu Mi'

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install1.jpg' width=400>



- Input server's IP (KuKuMi-api server is running)
ex) 192.168.1.137:8484
- Select 'Save' to complete installation

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install2.jpg' width=400>

4-2) Add a device at 'KuKu Mi' SmartApp
- 'Automation' -> 'SmartApps' -> 'KuKu Mi'
- 'Add a device...'
- Enter 'Xiaomi Device Type'

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install3.jpg' width=400>


- Select 'Mi Remote'
- Enter 'Xiaomi Device'
- Select your Mi Remote device (added in KuKu Mi Web Server)

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install4.jpg' width=400>


- Enter 'device name' that you want
- Enter 'DTH Type' and select DTH that you want to install

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install5.jpg' width=400>


- Select to 'Next' to set up command
- Mapping your command to button (added in KuKu Mi Web Server)

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install6.jpg' width=400>


- If you want synchronize device state by Plug and Contact Sensor,
configure 'State Monitor' menu
- To complete adding device, 'select 'Save' button
- You can confirm added device

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install7.jpg' width=400>


4-3) Installed Device Screenshot(Custom DTH)

<img src='https://cdn.rawgit.com/turlvo/KuKuMi/master/images/screenshots/install8.jpg' width=400>



	
