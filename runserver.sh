#!/bin/sh
echo "Updating KuKuMi..."
cd /root/KuKuMi
./update.sh

echo ""

echo "Running XiaomiBT daemon..."
cd /root/KuKuMi/xiaomibt-daemon
python xiaomibt-daemon.py stop
python xiaomibt-daemon.py start

echo ""

echo "Running KuKu Mi Server..."
cd /root/KuKuMi/django-KuKuMi
python manage.py runserver 0:8484

