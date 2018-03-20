from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core import serializers
from django.shortcuts import get_object_or_404
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth.models import User
from django.http import JsonResponse

from .models import *
from .forms import DeviceForm, HubForm
from .serializers import HubSerializer, XiaomiBTDeviceSerializer

import logging
import json
import subprocess

logger = logging.getLogger(__name__)

def index(request):
    devices = XiaomiBTDevice.objects.all()
    xiaomi_setting = XiaomiBT.objects.all().first()
    if request.method == 'POST':
        form = HubForm(request.POST, instance=xiaomi_setting)
        XiaomiBT.objects.all().delete()
        if form.is_valid():
            form.save()
            xiaomi_setting = XiaomiBT.objects.all().first()
            send_option_to_daemon(xiaomi_setting.ip, xiaomi_setting.iface, xiaomi_setting.threshold)

        return redirect('/xiaomibt')
    else:
        form = HubForm(instance=xiaomi_setting)
    return render(request, 'XiaomiBT/index.html', {'form': form, 'devices': devices})


def dev_create(request):
    if request.method == 'POST':
        form = DeviceForm(request.POST)
        if form.is_valid():
            form.save()
        return redirect('/xiaomibt')
    else:
        form = DeviceForm()

    return render(request, 'XiaomiBT/device.html', {'form': form})



def dev_edit(request, name):
    fb = XiaomiBTDevice.objects.get(name=name)
    if request.method == 'POST':
        form = DeviceForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
        return redirect('/xiaomibt')
    else:
        form = DeviceForm(instance=fb)

    return render(request, 'XiaomiBT/device.html', {'form': form})


def dev_del(request, id):
    logger.error("dev_del>> device: %s" % (id))
    fb = XiaomiBTDevice.objects.get(dev_ID=id)
    if request.method == 'GET':
        form = DeviceForm(request.POST, instance=fb)
        fb.delete()
        if form.is_valid():
            form.save()
        return redirect('/xiaomibt')
    else:
        form = DeviceForm(instance=fb)

    return render(request, 'XiaomiBT/device.html', {'form': form})



def validate_device_value(request):
    name = request.GET.get('name', None)

    data = {
        'is_taken': False
    }
    if XiaomiBTDevice.objects.filter(name__iexact=name).exists() :
        data = {
            'is_taken': True,
            'error_message' : 'A command with this command already exists.'
        }
    elif ' ' in name :
        data = {
            'is_taken': True,
            'error_message' : 'A space is not permitted in command name.'
        }

    return JsonResponse(data)

def discover(request):
    result = -1
    try:
        result = send_scan_to_daemon()
    except:
        result = -1
    logger.error("discover>> result : %s" % (result))
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

def interface(request):
    result = -1
    try:
        result = discover_bluetooth_interface()
    except:
        result = -1
    logger.error("interface>> result : %s" % (result))
    if result == -1:
        data = {
            'result': False,
            'message': 'interface is failed'
        }
        return JsonResponse(data)
    else:
        data = {
            'result': True,
            'message': result
        }
    return JsonResponse(data)

def start_daemon(request):
    result = -1
    try:
        result = send_start_to_daemon()
    except:
        result = -1
    logger.error("start_daemon>> result : %s" % (result))
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
def dev_list(request):
    if request.method == 'GET':
        devices = XiaomiBTDevice.objects.all()
        serializer = XiaomiBTDeviceSerializer(devices, many=True)
        return Response(serializer.data)

@api_view(['POST',])
def send_cmd(request, dev, cmd):
    is_test = json.loads(request.POST.get('isTest', 'false'))
    logger.error("send_cmd>> device: %s, command: %s, test: %s" % (dev, cmd, is_test))

    if request.method == 'POST':
        try:
            fd = get_object_or_404(XIaomiBTDevice, name=dev)
            target_ip = fd.ip
            target_token = fd.token
            logger.error("send_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except XiaomiBTDevice.DoesNotExist:
            raise Http404("No Device matches the given query.")

        if is_test == True:
            command_code = cmd
        else:
            try:
                fc = get_object_or_404(Command, name=cmd)
                command_code = fc.code
                logger.error("send_cmd>> command >> name : %s, code: %s" % (fc.name, fc.code))
            except Command.DoesNotExist:
                    raise Http404("No Command matches the given query.")

        result = 0
        try:
            pass
            #result = Miio_api.send_command(target_ip, target_token, command_code, 38400)
        except:
            result = -1

        if result == 0:
            data = {
                'result' : True,
                'message' : 'Succeed to send command!!!'
            }
            return JsonResponse(data)
        else :
            data = {
                'result' : False,
                'message' : 'Failed to send command...'
            }
        return JsonResponse(data)

def send_option_to_daemon(ip, iface, threshold):
    p = subprocess.Popen(["python", "/root/KuKuMi/xiaomibt-daemon/xiaomibt-daemon.py", "set", iface, ip, threshold], stdout=subprocess.PIPE)
    print (p.communicate())

def send_scan_to_daemon():
    p = subprocess.Popen(["python", "/root/KuKuMi/xiaomibt-daemon/xiaomibt-daemon.py", "scanresult"], stdout=subprocess.PIPE)
    """while True:
        output = p.stdout.readline()
        if output == '' and p.poll() is not None:
            break
        if output:
            print (output.strip())
    """
    return (p.communicate()[0]).decode('utf-8')

def send_start_to_daemon():
    p = subprocess.Popen(["python", "/root/KuKuMi/xiaomibt-daemon/xiaomibt-daemon.py", "restart"], stdout=subprocess.PIPE)
    return (p.communicate())

def discover_bluetooth_interface():
    p1 = subprocess.Popen(["hciconfig"], stdout=subprocess.PIPE)
    p2 = subprocess.Popen(["grep", "hci"], stdin=p1.stdout, stdout=subprocess.PIPE)
    return (p2.communicate()[0]).decode('utf-8')
