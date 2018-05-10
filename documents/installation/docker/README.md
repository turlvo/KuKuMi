# Install 'KuKu Mi' Server

1-1) 'KuKu Mi' Docker Image download

```
[X86 Platform]
# docker pull turlvo/kukumi

or

[ARM Platform]
# docker pull turlvo/kukumi-rasp
```

1-2) Execute 'KuKu Mi' API server by running container 

```
[X86 Platform]
# docker run --name=KuKuMi --privileged --net=host turlvo/kukumi

or

[ARM Platform] (Xiaomi BT version is not released)
# docker run --name=KuKuMi  --privileged --net=host turlvo/kukumi-rasp
```

1-3) Enable auto run 'KuKu Mi' container when rebooted (Optional)

```
# sudo vim /etc/systemd/system/kukumi.service

<kukumi.service File content>

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
```

```
# sudo systemctl enable /etc/systemd/system/kukumi.service
```
