/**
 *  KuKu Mi Mi Remote - Virtual Switch for Xiaomi Mi Remote
 *
 *  Copyright 2018 KuKu <turlvo@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

metadata {
	definition (name: "KuKu Mi_miremote_Custom", namespace: "turlvo", author: "KuKu") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        command "virtualOn"
        command "virtualOff"
        
        command "sendCommandByName", ["string"]
        command "cmd1"
        command "cmd2"
        command "cmd3"
        command "cmd4"
        command "cmd5"
        command "cmd6"
        command "cmd7"
        command "cmd8"
        command "cmd9"
        command "cmd10"
        command "cmd11"
        command "cmd12"
        
	}

    preferences {
        input name: "momentaryOn", type: "bool",title: "Enable Momentary on (for garage door controller)", required: false
        input name: "momentaryOnDelay", type: "num",title: "Enable Momentary on dealy time(default 5 seconds)", required: false
    }

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }
        valueTile("cmd1", "device.cmd1", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd1", label: '${currentValue}', action: "cmd1"
        }
        valueTile("cmd2", "device.cmd2", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd2", label: '${currentValue}', action: "cmd2"
        }
        valueTile("cmd3", "device.cmd3", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd3", label: '${currentValue}', action: "cmd3"
        }
        valueTile("cmd4", "device.cmd4", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd4", label: '${currentValue}', action: "cmd4"
        }
        valueTile("cmd5", "device.cmd5", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd5", label: '${currentValue}', action: "cmd5"
        }
        valueTile("cmd6", "device.cmd6", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd6", label: '${currentValue}', action: "cmd6"
        }
        valueTile("cmd7", "device.cmd7", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd7", label: '${currentValue}', action: "cmd7"
        }
        valueTile("cmd8", "device.cmd8", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd8", label: '${currentValue}', action: "cmd8"
        }
        valueTile("cmd9", "device.cmd9", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd9", label: '${currentValue}', action: "cmd9"
        }
        valueTile("cmd10", "device.cmd10", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd10", label: '${currentValue}', action: "cmd10"
        }
        valueTile("cmd11", "device.cmd11", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd11", label: '${currentValue}', action: "cmd11"
        }
        valueTile("cmd12", "device.cmd12", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "cmd12", label: '${currentValue}', action: "cmd12"
        }
        
    }

	main(["switch"])
	details(["cmd1", "cmd2", "cmd3", 
             "cmd4", "cmd5", "cmd6", 
             "cmd7", "cmd8", "cmd9",
             "cmd10", "cmd11", "cmd12"])
}

def installed() {
	log.debug "installed()"
	configure()
}

def updated() {
	log.debug "updated()"
	configure()
}
def initialize() {
    log.debug "initialize()"
    configure()
}

def configure() {
    log.debug "configure>>"
    sendEvent(name: "cmd1", value: parent.getCommandName("cmd1"))
    sendEvent(name: "cmd2", value: parent.getCommandName("cmd2"))
    sendEvent(name: "cmd3", value: parent.getCommandName("cmd3"))
    sendEvent(name: "cmd4", value: parent.getCommandName("cmd4"))
    sendEvent(name: "cmd5", value: parent.getCommandName("cmd5"))
    sendEvent(name: "cmd6", value: parent.getCommandName("cmd6"))
    sendEvent(name: "cmd7", value: parent.getCommandName("cmd7"))
    sendEvent(name: "cmd8", value: parent.getCommandName("cmd8"))
    sendEvent(name: "cmd9", value: parent.getCommandName("cmd9"))
    sendEvent(name: "cmd10", value: parent.getCommandName("cmd10"))
    sendEvent(name: "cmd11", value: parent.getCommandName("cmd11"))
    sendEvent(name: "cmd12", value: parent.getCommandName("cmd12"))
}

def on() {
	log.debug "child on()"
	parent.command(this, parent.getCommandName("on"))
    sendEvent(name: "switch", value: "on")

	if (momentaryOn) {
    	if (settings.momentaryOnDelay == null || settings.momentaryOnDelay == "" ) settings.momentaryOnDelay = 5
    	log.debug "momentaryOnHandler() >> time : " + settings.momentaryOnDelay
    	runIn(Integer.parseInt(settings.momentaryOnDelay), momentaryOnHandler, [overwrite: true])
    }
}

def momentaryOnHandler() {
	log.debug "momentaryOnHandler()"
	sendEvent(name: "switch", value: "off")
}

def off() {
	log.debug "child off"
	parent.command(this, parent.getCommandName("off"))
    sendEvent(name: "switch", value: "off")
}

def virtualOn() {
	log.debug "child on()"	
    sendEvent(name: "switch", value: "on")
}

def virtualOff() {
	log.debug "child off"	
    sendEvent(name: "switch", value: "off")
}

def cmd1() {
    log.debug "child cmd1() : ${device.currentState("cmd1")?.value}"
    parent.command(this, device.currentState("cmd1")?.value)
}
def cmd2() {
    log.debug "child cmd2() : ${device.currentState("cmd2")?.value}"
    parent.command(this, device.currentState("cmd2")?.value)
}
def cmd3() {
    log.debug "child cmd3() : ${device.currentState("cmd3")?.value}"
    parent.command(this, device.currentState("cmd3")?.value)
}
def cmd4() {
    log.debug "child cmd4() : ${device.currentState("cmd4")?.value}"
    parent.command(this, device.currentState("cmd4")?.value)
}
def cmd5() {
    log.debug "child cmd5() : ${device.currentState("cmd5")?.value}"
    parent.command(this, device.currentState("cmd5")?.value)
}
def cmd6() {
    log.debug "child cmd6() : ${device.currentState("cmd6")?.value}"
    parent.command(this, device.currentState("cmd6")?.value)
}
def cmd7() {
    log.debug "child cmd7() : ${device.currentState("cmd7")?.value}"
    parent.command(this, device.currentState("cmd7")?.value)
}
def cmd8() {
    log.debug "child cmd8() : ${device.currentState("cmd8")?.value}"
    parent.command(this, device.currentState("cmd8")?.value)
}
def cmd9() {
    log.debug "child cmd9() : ${device.currentState("cmd9")?.value}"
    parent.command(this, device.currentState("cmd9")?.value)
}

def sendCommandByName(cmd) {
    log.debug "child sendCommandByName() : ${cmd}"
	parent.command(this, cmd)
}

def generateEvent(Map results) {
    results.each { name, value ->
		log.debug "generateEvent>> name: $name, value: $value"
        def currentState = device.currentValue("switch")
		log.debug "generateEvent>> currentState: $currentState"
        if (currentState != value) {
        	log.debug "generateEvent>> changed to $value"
        	sendEvent(name: "switch", value: value)
        } else {
        	log.debug "generateEvent>> not change"
        }
    }
    return null
}