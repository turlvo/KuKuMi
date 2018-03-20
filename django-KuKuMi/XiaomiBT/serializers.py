from .models import *
from rest_framework import serializers


class HubSerializer(serializers.ModelSerializer):
    class Meta:
        model = XiaomiBT
        fields = ['ip', 'iface', 'threshold']

class XiaomiBTDeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = XiaomiBTDevice
        fields = ['name', 'mac']
