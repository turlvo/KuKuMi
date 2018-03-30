from __future__ import unicode_literals

from django.db import models

class XiaomiBTDevice(models.Model):
    name = models.CharField(max_length=200, unique=True)
    mac = models.CharField(max_length=17, unique=True)

    def __str__(self):
        return self.name

class XiaomiBTSetting(models.Model):
    ip = models.CharField(max_length=200)
    iface = models.CharField(max_length=17)
    threshold = models.CharField(max_length=17,)

    def __str__(self):
        return self.ip


class MiRemoteDevice(models.Model):
    name = models.CharField(max_length=200, unique=True)
    ip = models.CharField(max_length=15, unique=True)
    token = models.CharField(max_length=200, unique=True)

    def __str__(self):
        return self.name

class MiRemoteCommand(models.Model):
    #device = models.ForeignKey(Device, on_delete=models.CASCADE)
    name = models.CharField(max_length=200,  unique=True)
    code = models.CharField(max_length=2000)

    def __str__(self):
        return self.name
