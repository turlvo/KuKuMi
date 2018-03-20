from django.conf.urls import url, include
from django.urls import path
from rest_framework import routers

from . import views

#router = routers.DefaultRouter()
#router.register(r'devices', viewset.DeviceViewSet)
#router.register(r'commands', viewset.CommandViewSet)

urlpatterns = [
    path('', views.index, name='btindex'),

    url(r'^device/create', views.dev_create, name='btdev_create'),
    url(r'^device/edit/(?P<name>.*)/$', views.dev_edit, name='btdev_edit'),
    url(r'^device/delete/(?P<name>.*)/$', views.dev_del, name='btdev_del'),

    url(r'^ajax/validate_device_value/$', views.validate_device_value, name='validate_device_value'),

    url(r'^api/devices', views.dev_list, name='btdev_list'),
    url(r'^api/device/discover', views.discover, name='btdiscover'),
    url(r'^api/device/(?P<dev>.*)/command/(?P<cmd>.*)', views.send_cmd, name='btsend_cmd'),
]
