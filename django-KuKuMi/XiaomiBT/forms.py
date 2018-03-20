from django.forms import ModelForm
from .models import XiaomiBTDevice, XiaomiBT

class HubForm(ModelForm):
    class Meta:
        model = XiaomiBT
        fields = ['ip', 'iface', 'threshold']
        labels = {
            'ip' : 'ST Hub IP',
            'iface' : 'Bluetooth Interface',
            'threshold' : 'Reporting Threshold'}

class DeviceForm(ModelForm):
    class Meta:
        model = XiaomiBTDevice
        fields = ['name', 'mac']
