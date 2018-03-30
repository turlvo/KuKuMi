from .models import *
from rest_framework import serializers

class XiaomiBTDeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = XiaomiBTDevice
        fields = ['name', 'mac']

class XiaomiBTSettingSerializer(serializers.ModelSerializer):
    class Meta:
        model = XiaomiBTSetting
        fields = ['ip', 'iface', 'threshold']


class MiRemoteDeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = MiRemoteDevice
        fields = ['name', 'ip', 'token' ]

class MiRemoteCommandSerializer(serializers.ModelSerializer):
    class Meta:
        model = MiRemoteCommand
        fields = ['name', 'code']