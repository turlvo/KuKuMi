from django.contrib import admin

from .models import Device, Command

admin.site.register(Device)
admin.site.register(Command)
