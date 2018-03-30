#!/bin/sh
echo "Updating KuKuMi..."
cd /root/KuKuMi
./update.sh

echo ""
cd /root/KuKuMi/xiaomibt-daemon
python xiaomibt-daemon.py stop

echo "Running KuKu Mi Server..."
cd /root/KuKuMi/django-gentelella
python manage.py makemigrations
python manage.py migrate
python manage.py runserver 0:8484

