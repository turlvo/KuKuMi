#!/bin/sh
echo "Updating KuKuMi Server..."
cd /root/KuKuMi/
git checkout -f
git pull

cd /root/KuKuMi/django-KuKuMi
python manage.py makemigrations
python manage.py migrate
