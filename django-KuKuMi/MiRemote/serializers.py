from .models import *
from rest_framework import serializers

class DeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Device
        fields = ['dev_ID', 'name', 'ip', 'token' ]

class CommandSerializer(serializers.ModelSerializer):
    class Meta:
        model = Command
        fields = ['name', 'code']