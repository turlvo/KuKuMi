from django.db import models

class YeeBTDevice(models.Model):
    name = models.CharField(max_length=200, unique=True)
    mac = models.CharField(max_length=17, unique=True)

    def __str__(self):
        return self.name