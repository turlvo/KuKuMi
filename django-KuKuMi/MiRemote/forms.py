from django.forms import ModelForm
from .models import Device, Command

class DeviceForm(ModelForm):
    class Meta:
        model = Device
        fields = ['dev_ID', 'name', 'ip', 'token' ]

class CommandForm(ModelForm):
    class Meta:
        model = Command
        fields = ['name', 'code']