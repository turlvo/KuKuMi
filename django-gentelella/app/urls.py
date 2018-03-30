from django.conf.urls import url
from app import views

urlpatterns = [
    # Matches any html file - to be used for gentella
    # Avoid using your .html in your resources.
    # Or create a separate django app.
    url(r'^.*\.html', views.gentella_html, name='gentella'),

    # The home page
    url(r'^$', views.index, name='index'),

    # Xiaomi BT
    url(r'^xiaomibt$', views.xiaomibt_index, name='xiaomibt_index'),
    url(r'^xiaomibt/device$', views.xiaomibt_device, name='xiaomibt_device'),
    url(r'^xiaomibt/setting$', views.xiaomibt_setting, name='xiaomibt_setting'),
    url(r'^xiaomibt/device/create$', views.xiaomibt_dev_create, name='xiaomibtdev_create'),
    url(r'^xiaomibt/device/edit/(?P<id>.*)', views.xiaomibt_dev_edit, name='xiaomibtdev_edit'),
    url(r'^xiaomibt/device/delete/(?P<id>.*)', views.xiaomibt_dev_del, name='xiaomibtdev_del'),
    url(r'^xiaomibt/setting/create', views.xiaomibt_setting_create, name='xiaomibtsetting_create'),
    url(r'^xiaomibt/setting/edit/(?P<id>.*)', views.xiaomibt_setting_edit, name='xiaomibtsetting_edit'),
    url(r'^xiaomibt/setting/delete/(?P<id>.*)', views.xiaomibt_setting_del, name='xiaomibtsetting_del'),
    url(r'^ajax/validate_device_value/$', views.validate_device_value, name='validate_device_value'),
    url(r'^xiaomibt/api/devices', views.xiaomibt_dev_list, name='xiaomibtdev_list'),
    url(r'^xiaomibt/api/device/discover', views.xiaomibt_discover, name='xiaomibtdiscover'),
    url(r'^xiaomibt/api/interfaces', views.xiaomibt_interface, name='xiaomibtinterface'),
    url(r'^xiaomibt/api/start_daemon', views.xiaomibt_start_daemon, name='xiaomibtstartdaemon'),
    url(r'^xiaomibt/api/daemon_state', views.xiaomibt_get_daemon_state, name='xiaomibtgetdaemonstate'),

    # Mi Remote
    url(r'^miremote$', views.xiaomibt_index, name='miremote_index'),
    url(r'^miremote/device$', views.miremote_device, name='miremote_device'),
    url(r'^miremote/command$', views.miremote_command, name='miremote_command'),
    url(r'^miremote/device/create$', views.miremote_dev_create, name='miremotedev_create'),
    url(r'^miremote/device/edit/(?P<id>.*)', views.miremote_dev_edit, name='miremotedev_edit'),
    url(r'^miremote/device/delete/(?P<id>.*)', views.miremote_dev_del, name='miremotedev_del'),
    url(r'^miremote/command/create', views.miremote_command_create, name='miremotecommand_create'),
    url(r'^miremote/command/edit/(?P<id>.*)', views.miremote_command_edit, name='miremotecommand_edit'),
    url(r'^miremote/command/delete/(?P<id>.*)', views.miremote_command_del, name='miremotecommand_del'),
    url(r'^miremote/api/devices', views.miremote_dev_list, name='miremotedev_list'),
    url(r'^miremote/api/commands', views.miremote_cmd_list, name='miremotecmd_list'),
    url(r'^miremote/api/device/discover$', views.miremote_discover, name='miremotediscover'),
    url(r'^miremote/api/device/(?P<dev>.*)/learn_cmd$', views.miremote_learn_cmd, name='miremotelearn_cmd'),
    url(r'^miremote/api/device/(?P<dev>.*)/read_cmd$', views.miremote_read_cmd, name='miremoteread_cmd'),
    url(r'^miremote/api/device/(?P<dev>.*)/command/(?P<cmd>.*)', views.miremote_send_cmd, name='miremotesend_cmd'),
]
