from django.conf.urls import url, include
from django.urls import path
from rest_framework import routers

from . import views

#router = routers.DefaultRouter()
#router.register(r'devices', viewset.DeviceViewSet)
#router.register(r'commands', viewset.CommandViewSet)

urlpatterns = [
    path('', views.index, name='index'),
    #url(r'^api/', include(router.urls)),
    #url(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    #url(r'^api-v1/', include('rest_framework.urls', namespace='rest_framework_category')),
    #url(r'^devices', views.dev_list, name='dev_list'),
    url(r'^device/create', views.dev_create, name='dev_create'),
    url(r'^device/edit/(?P<id>.*)/$', views.dev_edit, name='dev_edit'),
    url(r'^device/delete/(?P<id>.*)/$', views.dev_del, name='dev_del'),
    url(r'^command/create', views.cmd_create, name='cmd_create'),
    url(r'^command/edit/(?P<name>.*)/$', views.cmd_edit, name='cmd_edit'),
    url(r'^command/delete/(?P<name>.*)/$', views.cmd_del, name='cmd_del'),

    url(r'^ajax/validate_command_value/$', views.validate_command_value, name='validate_command_value'),
    url(r'^ajax/validate_device_value/$', views.validate_device_value, name='validate_device_value'),

    url(r'^api/devices', views.dev_list, name='dev_list'),
    url(r'^api/commands', views.cmd_list, name='cmd_list'),
    url(r'^api/device/discover', views.discover, name='discover'),
    url(r'^api/device/(?P<dev>.*)/learn_cmd', views.learn_cmd, name='learn_cmd'),
    url(r'^api/device/(?P<dev>.*)/read_cmd', views.read_cmd, name='read_cmd'),
    url(r'^api/device/(?P<dev>.*)/command/(?P<cmd>.*)', views.send_cmd, name='send_cmd'),
]
