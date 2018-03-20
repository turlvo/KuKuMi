#!/bin/sh
echo "Updating KuKuMi Server..."
cd /root/KuKuMi/
git pull

cd /root/KuKuMi/django-KuKuMi
python manage.py migrate
