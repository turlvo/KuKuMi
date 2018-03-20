#!/bin/sh
echo "Updating KuKuMi..."
cd /root/KuKuMi
./update.sh

echo ""

echo "Running KuKu Mi Server..."
cd /root/KuKuMi/django-KuKuMi
python manage.py runserver 0:8484

