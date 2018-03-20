from django.conf.urls import url, include
from django.urls import path
from rest_framework import routers

from . import views

#router = routers.DefaultRouter()
#router.register(r'devices', viewset.DeviceViewSet)
#router.register(r'commands', viewset.CommandViewSet)

urlpatterns = [
    path('', views.index, name='xiaomibtindex'),
    url(r'^device/create', views.dev_create, name='xiaomibtdev_create'),
    url(r'^device/edit/(?P<name>.*)/$', views.dev_edit, name='xiaomibtdev_edit'),
    url(r'^device/delete/(?P<name>.*)/$', views.dev_del, name='xiaomibtdev_del'),

    url(r'^ajax/validate_device_value/$', views.validate_device_value, name='validate_device_value'),

    url(r'^api/devices', views.dev_list, name='xiaomibtdev_list'),
    url(r'^api/device/discover', views.discover, name='xiaomibtdiscover'),
    url(r'^api/interfaces', views.interface, name='xiaomibtinterface'),
    url(r'^api/device/(?P<dev>.*)/command/(?P<cmd>.*)', views.send_cmd, name='xiaomibtsend_cmd'),
]
