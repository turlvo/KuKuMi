from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core import serializers
from django.shortcuts import get_object_or_404
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth.models import User
from django.http import JsonResponse

from .serializers import DeviceSerializer, CommandSerializer
from .models import *
from .forms import DeviceForm, CommandForm

import logging
import json
from .ext.miio_api import Miio_api

logger = logging.getLogger(__name__)

def index(request):
    devices = Device.objects.all()
    commands = Command.objects.all()
    return render(request, 'MiRemote/index.html', {'devices': devices, 'commands': commands})

def dev_create(request):
    if request.method == 'POST':
        form = DeviceForm(request.POST)
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = DeviceForm()


    return render(request, 'MiRemote/device.html', {'form': form})

def cmd_create(request):
    try:
        fd = Device.objects.all().first()
    except Device.DoesNotExist:
        raise Http404("No Device matches the given query.")

    if request.method == 'POST':
        form = CommandForm(request.POST)
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = CommandForm()

    return render(request, 'MiRemote/command.html', {'form': form, 'device': fd})


def dev_edit(request, id):
    fb = Device.objects.get(dev_ID=id)
    if request.method == 'POST':
        form = DeviceForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = DeviceForm(instance=fb)

    return render(request, 'MiRemote/device.html', {'form': form})


def dev_del(request, id):
    logger.error("dev_del>> device: %s" % (id))
    fb = Device.objects.get(dev_ID=id)
    if request.method == 'GET':
        form = DeviceForm(request.POST, instance=fb)
        fb.delete()
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = DeviceForm(instance=fb)

    return render(request, 'MiRemote/device.html', {'form': form})


def cmd_edit(request, name):
    fb = Command.objects.get(name=name)
    if request.method == 'POST':
        form = CommandForm(request.POST, instance=fb)
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = CommandForm(instance=fb)

    return render(request, 'MiRemote/command.html', {'form': form})


def cmd_del(request, name):
    logger.error("cmd_del>> command: %s" % (name))
    fb = Command.objects.get(name=name)
    if request.method == 'GET':
        form = CommandForm(request.POST, instance=fb)
        fb.delete()
        if form.is_valid():
            form.save()
        return redirect('/miremote')
    else:
        form = CommandForm(instance=fb)

    return render(request, 'MiRemote/command.html', {'form': form})

def validate_command_value(request):
    name = request.GET.get('name', None)

    data = {
        'is_taken': False
    }
    if Command.objects.filter(name__iexact=name).exists() :
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
        result = Miio_api.discover()
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

def learn_cmd(request, dev):
    if request.method == 'GET':
        try:
            fd = get_object_or_404(Device, name=dev)
            logger.error("learn_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except Device.DoesNotExist:
            raise Http404("No Device matches the given query.")


        result = -1
        try:
            result = Miio_api.learn_command(fd.ip, fd.token, 333)
        except:
            result = -1

        if result == -1:
            data = {
                'result' : False,
                'message' : 'Learning command is failed'
            }
            return JsonResponse(data)
        else :
            data = {
                'result' : True,
                'message' : 'Learning command is started(10seconds)...'
            }
        return JsonResponse(data)

def read_cmd(request, dev):
    if request.method == 'GET':
        try:
            fd = get_object_or_404(Device, name=dev)
            logger.error("read_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except Device.DoesNotExist:
            raise Http404("No Device matches the given query.")

        result = -1
        try:
            result = Miio_api.read_command(fd.ip, fd.token, 333)
        except:
            result = -1

        data = {
            'result' : False,
            'message' : 'Failed to read command'
        }
        if result != -1:
            logger.error("read_cmd>> result : %s" % (result['code']))
            if result['code'] != '':
                data = {
                    'result' : True,
                    'message' : result['code']
                }

        return JsonResponse(data)


@api_view(['GET', ])
def dev_list(request):
    if request.method == 'GET':
        devices = Device.objects.all()
        serializer = DeviceSerializer(devices, many=True)
        return Response(serializer.data)

@api_view(['GET', ])
def cmd_list(request):
    if request.method == 'GET':
        commands = Command.objects.all()
        serializer = CommandSerializer(commands, many=True)
        return Response(serializer.data)

@api_view(['POST', ])
def send_cmd(request, dev, cmd):
    logger.error("send_cmd>> device: %s, command: %s" % (dev, cmd))
    if request.method == 'POST':
        try:
            fd = get_object_or_404(Device, name=dev)
            logger.error("send_cmd>> device >> name: %s, ip: %s, token: %s" % (fd.name, fd.ip, fd.token))
        except Device.DoesNotExist:
            raise Http404("No Device matches the given query.")

        try:
            fc = get_object_or_404(Command, name=cmd)
            logger.error("send_cmd>> command >> name : %s, code: %s" % (fc.name, fc.code))
        except Command.DoesNotExist:
            raise Http404("No Command matches the given query.")

        result = 0
        try:
            result = Miio_api.send_command(fd.ip, fd.token, fc.code, 38400)
        except:
            result = -1

        if result != 0:
            return HttpResponse(status=500)
        else :
            return HttpResponse(status=204)