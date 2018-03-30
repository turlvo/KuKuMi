from django.forms import ModelForm
from django.forms.widgets import CheckboxSelectMultiple
from .models import XiaomiBTDevice, XiaomiBTSetting
from .models import MiRemoteDevice, MiRemoteCommand

class XiaomiSettingForm(ModelForm):
    class Meta:
        model = XiaomiBTSetting
        fields = ['ip', 'iface', 'threshold']
        labels = {
            'ip' : 'ST Hub IP',
            'iface' : 'Bluetooth Interface',
            'threshold' : 'Reporting Threshold'}

class XiaomiBTDeviceForm(ModelForm):
    class Meta:
        model = XiaomiBTDevice
        fields = ['name', 'mac']


class MiRemoteDeviceForm(ModelForm):
    class Meta:
        model = MiRemoteDevice
        fields = ['name', 'ip', 'token' ]

class MiRemoteCommandForm(ModelForm):
    class Meta:
        model = MiRemoteCommand
        fields = ['name', 'code']

