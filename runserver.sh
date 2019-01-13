#!/bin/sh
cd /root/KuKuMi/xiaomibt-daemon
python xiaomibt-daemon.py stop

echo "Running KuKu Mi Server..."
cd /root/KuKuMi/django-gentelella
python manage.py runserver 0:8484

