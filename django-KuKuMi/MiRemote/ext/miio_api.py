import miio
import logging
import socket
import codecs
import ipaddress

from .device import Device

logging.basicConfig(level=logging.DEBUG)
_LOGGER = logging.getLogger(__name__)

class Miio_api:
    @staticmethod
    def learn_command(ip, token, key):
        mi_ir = miio.chuangmi_ir.ChuangmiIr(ip, token)
        result = mi_ir.learn (key)
        _LOGGER.info("Sending learn_command: result %s", result)
        return result

    @staticmethod
    def read_command(ip, token, key):
        mi_ir = miio.chuangmi_ir.ChuangmiIr(ip, token)
        result = mi_ir.read (key)
        _LOGGER.info("Sending read_command: result %s", result)
        return result

    @staticmethod
    def send_command(ip, token, command, frequency: int):
        mi_ir = miio.chuangmi_ir.ChuangmiIr(ip, token)
        result =  mi_ir.play (command)
        _LOGGER.info("Sending send_command: result %s", result)
        return result

    @staticmethod
    def discover():
        timeout = 5

        seen_devices = []  # type: List[str]
        seen_addrs = []

        addr = '<broadcast>'
        is_broadcast = True
        _LOGGER.info("Sending discovery to %s with timeout of %ss..",
                         addr, timeout)
        # magic, length 32
        helobytes = bytes.fromhex(
            '21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff')

        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        s.settimeout(timeout)
        s.sendto(helobytes, (addr, 54321))
        while True:
            try:
                data, addr = s.recvfrom(1024)
                m = miio.Message.parse(data)  # type: Message
                _LOGGER.debug("Got a response: %s", m)
                if not is_broadcast:
                    return m

                if addr[0] not in seen_addrs:
                    _LOGGER.info("  IP %s (ID: %s) - token: %s",
                                 addr[0],
                                 m.header.value.device_id.decode(),
                                 codecs.encode(m.checksum, 'hex'))
                    dev = {"dev_ID": m.header.value.device_id.decode(), "ip": addr[0], "token": codecs.encode(m.checksum, 'hex')}
                    seen_addrs.append(addr[0])
                    seen_devices.append(dev)
            except socket.timeout:
                if is_broadcast:
                    _LOGGER.info("Discovery done")
                break
            except Exception as ex:
                _LOGGER.warning("error while reading discover results: %s", ex)
                break
        print('array : %s' % seen_devices)
        return seen_devices

if __name__ == '__main__':
    _LOGGER.info ("")
