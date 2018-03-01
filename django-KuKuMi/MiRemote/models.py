from django.db import models

class Device(models.Model):
    dev_ID = models.CharField(max_length=200, primary_key=True, unique=True)
    name = models.CharField(max_length=200, unique=True)
    ip = models.CharField(max_length=15, unique=True)
    token = models.CharField(max_length=200)

    def __str__(self):
        return self.name

class Command(models.Model):
    #device = models.ForeignKey(Device, on_delete=models.CASCADE)
    name = models.CharField(max_length=200, primary_key=True, unique=True)
    code = models.CharField(max_length=2000)

    def __str__(self):
        return self.name


