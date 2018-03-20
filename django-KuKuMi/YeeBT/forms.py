from django.forms import ModelForm
from .models import YeeBTDevice

class DeviceForm(ModelForm):
    class Meta:
        model = YeeBTDevice
        fields = ['name', 'mac']
