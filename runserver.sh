#!/bin/sh
echo "Updating KuKuMi..."
cd /root/KuKuMi
git checkout -f
git pull

pip install --upgrade requirements.txt

echo ""
cd /root/KuKuMi/xiaomibt-daemon
python xiaomibt-daemon.py stop

echo "Running KuKu Mi Server..."
cd /root/KuKuMi/django-gentelella
python manage.py makemigrations
python manage.py migrate
python manage.py runserver 0:8484

