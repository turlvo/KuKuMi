from django.shortcuts import render
from django.template import loader
from django.http import HttpResponse

from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core import serializers
from django.shortcuts import get_object_or_404
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth.models import User
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from .models import *
from .forms import XiaomiBTDeviceForm, XiaomiSettingForm
from .serializers import XiaomiBTSettingSerializer, XiaomiBTDeviceSerializer

from .forms import MiRemoteDeviceForm, MiRemoteCommandForm
from .serializers import MiRemoteDeviceSerializer, MiRemoteCommandSerializer
from .ext.miio_api import Miio_api

import logging
import json
import subprocess
import re


def index(request):
    xiaomibt_devices = XiaomiBTDevice.objects.all()
    xiaomibt_setting = XiaomiBTSetting.objects.all().first()
    daemon_state = send_get_state_to_daemon()

    miremote_devices = MiRemoteDevice.objects.all()
    miremote_commands = MiRemoteCommand.objects.all()
    context = {}
    template = loader.get_template('app/index.html')
    # return HttpResponse(template.render(context, request))
    return render(request, 'app/index.html',
                  {'miremote_devices': miremote_devices, 'miremote_commands': miremote_commands,
                   'xiaomibt_devices': xiaomibt_devices, 'xiaomibt_setting': xiaomibt_setting, 'daemon_state': daemon_state})


def gentella_html(request):
    context = {}
    # The template to be loaded as per gentelella.
    # All resource paths for gentelella end in .html.

    # Pick out the html file name from the url. And load that template.
    load_template = request.path.split('/')[-1]
    template = loader.get_template('app/' + load_template)
    return HttpResponse(template.render(context, request))


logger = logging.getLogger(__name__)


def xiaomibt_index(request):
    devices = XiaomiBTDevice.objects.all()
    logger.error("xiaomibt_index")
    return render(request, 'app/xiaomibt_device.html', {'devices': devices})


def xiaomibt_device(request):
    devices = XiaomiBTDevice.objects.all()
    logger.error("xiaomibt_device")
    return render(request, 'app/xiaomibt_device.html', {'devices': devices})


def xiaomibt_setting(request):
    xiaomi_setting = XiaomiBTSetting.objects.all().first()
    daemon_state = send_get_state_to_daemon()
    return render(request, 'app/xiaomibt_setting.html', {'setting': xiaomi_setting, 'daemon_state': daemon_state})


def xiaomibt_dev_create(request):
    if request.method == 'POST':
        form = XiaomiBTDeviceForm(request.POST)
        data = {}
        if form.is_valid():
            form.save()

            data = {
                'result': 'OK',
                'message': 'Added!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': form.errors
            }
        return JsonResponse(data)


def xiaomibt_dev_edit(request, id):
    if request.method == 'POST':
        fb = XiaomiBTDevice.objects.get(id=id)
        form = XiaomiBTDeviceForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
            data = {
                'result': 'OK',
                'message': 'Updated!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to update data'
            }

        return JsonResponse(data)


def xiaomibt_dev_del(request, id):
    if request.method == 'DELETE':
        fb = XiaomiBTDevice.objects.get(id=id)
        if None != fb:
            fb.delete()
            data = {
                'result': 'OK',
                'message': 'Deleted!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to delete data'
            }

        return JsonResponse(data)


def xiaomibt_setting_create(request):
    logger.error(
        "xiaomibt_setting_create %s, %s, %s" % (request.POST['ip'], request.POST['iface'], request.POST['threshold']))
    if request.method == 'POST':
        XiaomiBTSetting.objects.all().delete()
        form = XiaomiSettingForm(data=request.POST)
        print (form.is_valid(), form.errors, type(form.errors))
        data = {}

        if form.is_valid():
            form.save()
            xiaomi_setting = XiaomiBTSetting.objects.all().first()
            print(xiaomi_setting)
            send_option_to_daemon(xiaomi_setting.ip, xiaomi_setting.iface, xiaomi_setting.threshold)

            data = {
                'result': 'OK',
                'message': 'Saved!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': form.errors
            }
        return JsonResponse(data)


def xiaomibt_setting_edit(request, id):
    if request.method == 'POST':
        fb = XiaomiBTSetting.objects.get(id=id)
        form = XiaomiSettingFrom(request.POST, instance=fb)
        if form.is_valid():
            form.save()
            data = {
                'result': 'OK',
                'message': 'Updated!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to update data'
            }

        return JsonResponse(data)


def xiaomibt_setting_del(request, id):
    if request.method == 'DELETE':
        fb = XiaomiBTSetting.objects.get(id=id)
        if None != fb:
            fb.delete()
            data = {
                'result': 'OK',
                'message': 'Deleted!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to delete data'
            }

        return JsonResponse(data)


def validate_input_value(request):
    name = request.GET.get('data', None)

    data = {
        'is_taken': False
    }
    if ' ' in name:
        data = {
            'is_taken': True,
            'error_message': 'A space is not permitted in input.'
        }

    return JsonResponse(data)


def xiaomibt_discover(request):
    result = -1
    try:
        result = send_scan_to_daemon()
    except:
        result = -1
        #result = 0
    logger.error("discover>> result : %s" % (result.replace('\'', '\"')))
    data = {}
    if result == -1:
        data = {
            'result': False,
            'message': 'Discovering is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': json.loads(result.replace('\'', '\"'))
        }
    return JsonResponse(data)
    #return JsonResponse({'result': True, 'message': ["4c:65:a8:d0:9b:cc", "4c:65:a8:d0:1c:cf"]})


def xiaomibt_interface(request):
    result = -1
    try:
        result = discover_bluetooth_interface()
    except:
        # result = -1
        result = 0

    p = re.compile('hci\d')
    founds = p.findall(result)
    founds.append("None")
    logger.error("interface>> result2: %s" % (founds))

    data = {}
    if result == -1:
        data = {
            'result': False,
            'message': 'interface is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': sorted(founds)
        }
    return JsonResponse(data)
    #return JsonResponse({'result': True, 'message': ['hci0', 'hci1']})


def xiaomibt_get_daemon_state(request):
    result = -1
    try:
        result = send_get_state_to_daemon()
    except:
        # result = -1
        result = 0
    data = {}
    if result == -1:
        data = {
            'result': False,
            'message': 'interface is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': str(result)
        }
    return JsonResponse(data)

def xiaomibt_start_daemon(request):
    result = -1
    try:
        result = send_start_to_daemon()
    except:
        result = -1

    data = {}
    if result == -1:
        data = {
            'result': False,
            'message': 'start_daemon is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': result
        }
    return JsonResponse(data)


@api_view(['GET', ])
def xiaomibt_dev_list(request):
    if request.method == 'GET':
        devices = XiaomiBTDevice.objects.all()
        serializer = XiaomiBTDeviceSerializer(devices, many=True)
        return Response(serializer.data)


def send_option_to_daemon(ip, iface, threshold):
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "set", iface, ip, threshold])
    return (p.communicate()[0])


def send_scan_to_daemon():
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "scanresult"], stdout=subprocess.PIPE)
    """while True:
        output = p.stdout.readline()
        if output == '' and p.poll() is not None:
            break
        if output:
            print (output.strip())
    """
    return (p.communicate()[0]).decode('utf-8')

def send_get_state_to_daemon():
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "state"], stdout=subprocess.PIPE)
    return (p.communicate()[0]).decode('utf-8')


def send_start_to_daemon():
    p = subprocess.Popen(["python", "../xiaomibt-daemon/xiaomibt-daemon.py", "restart"])
    return (p.communicate()[0])


def discover_bluetooth_interface():
    p1 = subprocess.Popen(["hciconfig"], stdout=subprocess.PIPE)
    p2 = subprocess.Popen(["grep", "hci"], stdin=p1.stdout, stdout=subprocess.PIPE)
    return (p2.communicate()[0]).decode('utf-8')


""" Mi Remote """


def miremote_index(request):
    devices = MiRemoteDevice.objects.all()
    logger.debug("miremote_index")
    return render(request, 'app/miremote_device.html', {'devices': devices})


def miremote_device(request):
    devices = MiRemoteDevice.objects.all()
    logger.debug("miremote_device")
    return render(request, 'app/miremote_device.html', {'devices': devices})


def miremote_command(request):
    commands = MiRemoteCommand.objects.all()
    devices = MiRemoteDevice.objects.all()
    logger.debug("miremote_command")
    return render(request, 'app/miremote_command.html', {'devices': devices, 'commands': commands})


def miremote_dev_create(request):
    if request.method == 'POST':
        form = MiRemoteDeviceForm(request.POST)
        data = {}
        if form.is_valid():
            form.save()

            data = {
                'result': 'OK',
                'message': 'Added!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to save data'
            }
        return JsonResponse(data)


def miremote_dev_edit(request, id):
    if request.method == 'POST':
        fb = MiRemoteDevice.objects.get(id=id)
        form = MiRemoteDeviceForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
            data = {
                'result': 'OK',
                'message': 'Updated!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to update data'
            }

        return JsonResponse(data)


def miremote_dev_del(request, id):
    if request.method == 'DELETE':
        fb = MiRemoteDevice.objects.get(id=id)
        if None != fb:
            fb.delete()
            data = {
                'result': 'OK',
                'message': 'Deleted!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to delete data'
            }

        return JsonResponse(data)


def miremote_command_create(request):
    logger.debug("xiaomibt_setting_create")
    if request.method == 'POST':
        form = MiRemoteCommandForm(request.POST)
        data = {}
        if form.is_valid():
            form.save()

            data = {
                'result': 'OK',
                'message': 'Added!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to save data'
            }
        return JsonResponse(data)


def miremote_command_edit(request, id):
    if request.method == 'POST':
        fb = MiRemoteCommand.objects.get(id=id)
        form = MiRemoteCommandForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
            data = {
                'result': 'OK',
                'message': 'Updated!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to update data'
            }

        return JsonResponse(data)


def miremote_command_del(request, id):
    if request.method == 'DELETE':
        fb = MiRemoteCommand.objects.get(id=id)
        if None != fb:
            fb.delete()
            data = {
                'result': 'OK',
                'message': 'Deleted!!!'
            }
        else:
            data = {
                'result': 'Fail',
                'message': 'Failed to delete data'
            }

        return JsonResponse(data)


def miremote_discover(request):
    result = -1
    try:
        result = Miio_api.discover()
    except:
        result = -1
    logger.debug("discover>> result : %s" % (result))
    if result == -1:
        data = {
            'result': False,
            'message': 'Discovering is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': result
        }
    return JsonResponse(data)


def miremote_learn_cmd(request, dev):
    if request.method == 'GET':
        try:
            fd = get_object_or_404(MiRemoteDevice, name=dev)
            logger.debug("learn_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except MiRemoteDevice.DoesNotExist:
            raise Http404("No Device matches the given query.")

        result = -1
        try:
            result = Miio_api.learn_command(fd.ip, fd.token, 333)
        except:
            result = -1

        if result == -1:
            data = {
                'result': False,
                'message': 'Learning command is failed'
            }
            return JsonResponse(data)
        else:
            data = {
                'result': True,
                'message': 'Learning command is started(10seconds)...'
            }
        return JsonResponse(data)


def miremote_read_cmd(request, dev):
    if request.method == 'GET':
        try:
            fd = get_object_or_404(MiRemoteDevice, name=dev)
            logger.debug("read_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except MiRemoteDevice.DoesNotExist:
            raise Http404("No Device matches the given query.")

        result = -1
        try:
            result = Miio_api.read_command(fd.ip, fd.token, 333)
        except:
            result = -1

        data = {
            'result': False,
            'message': 'Failed to read command'
        }
        if result != -1:
            logger.debug("read_cmd>> result : %s" % (result['code']))
            if result['code'] != '':
                data = {
                    'result': True,
                    'message': result['code']
                }

        return JsonResponse(data)


@api_view(['GET', ])
def miremote_dev_list(request):
    if request.method == 'GET':
        devices = MiRemoteDevice.objects.all()
        serializer = MiRemoteDeviceSerializer(devices, many=True)
        return Response(serializer.data)


@api_view(['GET', ])
def miremote_cmd_list(request):
    if request.method == 'GET':
        commands = MiRemoteCommand.objects.all()
        serializer = MiRemoteCommandSerializer(commands, many=True)
        return Response(serializer.data)


@api_view(['POST', ])
def miremote_send_cmd(request, dev, cmd):
    is_test = json.loads(request.POST.get('isTest', 'false'))
    logger.debug("send_cmd>> device: %s, command: %s, test: %s" % (dev, cmd, is_test))

    if request.method == 'POST':
        try:
            fd = get_object_or_404(MiRemoteDevice, name=dev)
            target_ip = fd.ip
            target_token = fd.token
            logger.debug("send_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except MiRemoteDevice.DoesNotExist:
            raise Http404("No Device matches the given query.")

        if is_test == True:
            command_code = cmd
        else:
            try:
                fc = get_object_or_404(MiRemoteCommand, name=cmd)
                command_code = fc.code
                logger.debug("send_cmd>> command >> name : %s, code: %s" % (fc.name, fc.code))
            except MiRemoteCommand.DoesNotExist:
                raise Http404("No Command matches the given query.")

        result = 0
        try:
            result = Miio_api.send_command(target_ip, target_token, command_code, 38400)
        except:
            result = -1

        if result == 0:
            data = {
                'result': True,
                'message': 'Succeed to send command!!!'
            }
            return JsonResponse(data)
        else:
            data = {
                'result': False,
                'message': 'Failed to send command...'
            }
        return JsonResponse(data)
