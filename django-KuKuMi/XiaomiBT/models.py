from django.db import models

class XiaomiBTDevice(models.Model):
    name = models.CharField(max_length=200, unique=True)
    mac = models.CharField(max_length=17, unique=True)

    def __str__(self):
        return self.name

class XiaomiBT(models.Model):
    ip = models.CharField(max_length=200, unique=True)
    iface = models.CharField(max_length=17, unique=True)
    threshold = models.CharField(max_length=17, unique=True)

    def __str__(self):
        return self.ip