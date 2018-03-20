from .models import *
from rest_framework import serializers

class YeeBTDeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = YeeBTDevice
        fields = ['name', 'mac']
