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
	definition (name: "KuKu Mi_miremote_TV", namespace: "turlvo", author: "KuKu") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        
        command "power"
        command "volup"
        command "chup"
        command "mute"
        command "voldown"
        command "chdown"
        command "menu"
        command "home"
        command "input"
        command "number_1"
        command "number_2"
        command "number_3"
        command "number_4"
        command "number_5"
        command "number_6"
        command "number_7"
        command "number_8"
        command "number_9"
        command "number_0"
        command "back"
        
        command "custom1"
        command "custom2"
        command "custom3"
        command "custom4"
        command "custom5"
        
        command "virtualOn"
        command "virtualOff"
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

        valueTile("power", "device.power", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "POWER", action: "power"
            state "no", label: "unavail", action: ""
        }
        valueTile("volup", "device.volup", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "+\nVOLUME", action: "volup"
            state "no", label: "unavail", action: ""
        }
        valueTile("chup", "device.chup", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "∧\nCHANNEL", action: "chup"
            state "no", label: "unavail", action: ""
        }
        
        valueTile("mute", "device.mute", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "MUTE", action: "mute"
            state "no", label: "unavail", action: ""
        }
        valueTile("voldown", "device.voldown", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "VOLUME\n-", action: "voldown"
            state "no", label: "unavail", action: ""
        }
        valueTile("chdown", "device.chdown", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "CHANNEL\n∨", action: "chdown"
            state "no", label: "unavail", action: ""
        }

        valueTile("menu", "device.menu", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "MENU", action: "menu"
            state "no", label: "unavail", action: ""
        }
        valueTile("home", "device.home", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "HOME", action: "home"
            state "no", label: "unavail", action: ""
        }
        valueTile("input", "device.input", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "INPUT", action: "input"
            state "no", label: "unavail", action: ""
        }
        
        valueTile("number_1", "device.number_1", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "1", action: "number_1"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_2", "device.number_2", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "2", action: "number_2"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_3", "device.number_3", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "3", action: "number_3"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_4", "device.number_4", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "4", action: "number_4"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_5", "device.number_5", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "5", action: "number_5"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_6", "device.number_6", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "6", action: "number_6"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_7", "device.number_7", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "7", action: "number_7"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_8", "device.number_8", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "8", action: "number_8"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_9", "device.number_9", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "9", action: "number_9"
            state "no", label: "unavail", action: ""
        }
        valueTile("back", "device.back", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "BACK", action: "back"
            state "no", label: "unavail", action: ""
        }
        valueTile("number_0", "device.number_0", width: 2, height: 1, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "0", action: "number_0"
            state "no", label: "unavail", action: ""
        }
    }

	main(["switch"])
	details(["power", "volup", "chup",
            "mute", "voldown", "chdown",
            "menu", "home", "input",
            "number_1", "number_2", "number_3",
            "number_4", "number_5", "number_6",
            "number_7", "number_8", "number_9",
            "back", "number_0"])
}

def installed() {
	log.debug "installed()"
	//configure()
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def power() {
    log.debug "child power()"
    log.debug "power>> ${device.currentState("switch")?.value}"
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
		off()
    } else {
        on()
    }
}

def volup() {
    log.debug "child volup()"
    parent.command(this, "volup")
}

def chup() {
    log.debug "child chup()"
    parent.command(this, "chup")
}

def mute() {
    log.debug "child mute()"
    parent.command(this, "mute")
}

def voldown() {
    log.debug "child voldown()"
    parent.command(this, "voldown")
}

def chdown() {
    log.debug "child chdown()"
    parent.command(this, "chdown")    
}

def menu() {
    log.debug "child menu()"
    parent.command(this, "menu")    
}

def home() {
    log.debug "child home()"
    parent.command(this, "home")    
}

def input() {
    log.debug "child input()"
    parent.command(this, "input")    
}

def back(value) {
    log.debug "child back()"
    parent.command(this, "back")   
}

def number_1(value) {
    log.debug "child number_1()"
    parent.commandValue(this, "1")   
}
def number_2(value) {
    log.debug "child number_2()"
    parent.commandValue(this, "2")   
}
def number_3(value) {
    log.debug "child number_3()"
    parent.commandValue(this, "3")   
}
def number_4(value) {
    log.debug "child number_4()"
    parent.commandValue(this, "4")   
}
def number_5(value) {
    log.debug "child number_5()"
    parent.commandValue(this, "5")   
}
def number_6(value) {
    log.debug "child number_6()"
    parent.commandValue(this, "6")   
}
def number_7(value) {
    log.debug "child number_7()"
    parent.commandValue(this, "7")   
}
def number_8(value) {
    log.debug "child number_8()"
    parent.commandValue(this, "8")   
}
def number_9(value) {
    log.debug "child number_9()"
    parent.commandValue(this, "9")   
}
def number_0(value) {
    log.debug "child number_0()"
    parent.commandValue(this, "0")   
}

def custom1() {
    log.debug "child custom1()"
    parent.command(this, "custom1")
}

def custom2() {
    log.debug "child custom2()"
    parent.command(this, "custom2")
}

def custom3() {
    log.debug "child custom3()"
    parent.command(this, "custom3")
}

def custom4() {
    log.debug "child custom4()"
    parent.command(this, "custom4")
}

def custom5() {
    log.debug "child custom5()"
    parent.command(this, "custom5")
}

def on() {
	log.debug "child on()"
	parent.command(this, "on")
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
	parent.command(this, "off")
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

def poll() {
	log.debug "poll()"
}

def parseEventData(Map results) {
    results.each { name, value ->
        //Parse events and optionally create SmartThings events
    }
}