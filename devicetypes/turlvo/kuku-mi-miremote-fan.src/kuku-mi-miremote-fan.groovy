/**
 *  KuKu Mi Mi Remote - Virtual Switch for Xiaomi Mi Remote
 *
 *  Copyright 2018 ShinJjang (modified by KuKu <turlvo@gmail.com>)
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
	definition (name: "KuKu Mi_miremote_Fan", namespace: "turlvo", author: "KuKu") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "kukufan"
		command "lowSpeed"
		command "medSpeed"
		command "highSpeed"
		command "swingMode"
		command "sleepMode"
		command "timer"

	}

	tiles (scale:2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.Appliances.appliances11", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.Appliances.appliances11", nextState:"turningOff"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.Appliances.appliances11", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.Appliances.appliances11", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
				attributeState "level", label:'${currentValue}%'
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("lowSpeed", "device.fanspeed", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "LOW", label:'LOW', action: "lowSpeed", icon:"st.quirky.spotter.quirky-spotter-luminance-dark"
  		}
		standardTile("medSpeed", "device.fanspeed", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "MED", label: 'MED', action: "medSpeed", icon:"st.quirky.spotter.quirky-spotter-luminance-light"
		}
		standardTile("highSpeed", "device.fanspeed", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "HIGH", label: 'HIGH', action: "highSpeed", icon:"st.quirky.spotter.quirky-spotter-luminance-bright"
		}
		standardTile("swingMode", "device.swingMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "on", label: 'Swing', action: "swingMode", backgroundColor:"#79b821", icon:"st.motion.motion.active"
			state "off", label: 'off', action: "swingMode", backgroundColor:"#ffffff", icon:"st.motion.motion.active"
		}
		standardTile("sleepMode", "device.sleepMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "on", label:'Natural', action:"sleepMode", backgroundColor:"#79b821", icon: "st.Outdoor.outdoor19", nextState:"off"
			state "on1", label:'Sleep', action:"sleepMode", backgroundColor:"#79b821", icon: "st.Bedroom.bedroom2", nextState:"off"
			state "off", label:'off', action:"sleepMode", backgroundColor:"#ffffff", icon: "st.Appliances.appliances11", nextState:"on"
		}
		standardTile("timer", "device.timer", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "timer", label: 'Set time', action: "timer", icon:"st.Health & Wellness.health7"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2) {
			state "level", action:"switch level.setLevel"
		}


		main(["switch"])
		details(["switch", "lowSpeed", "medSpeed", "highSpeed", "swingMode", "sleepMode", "timer"])
	}
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

def lowSpeed() {
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
    log.debug "child lowSpeed()"
    parent.command(this, "lowSpeed")
    sendEvent(name: "level", value: 30)        
    } else {
    parent.command(this, "on")    
    sendEvent(name: "switch", value: "on")
    log.debug "child lowSpeed()"
    sendEvent(name: "level", value: 30)    
    }
}

def medSpeed() {
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
    log.debug "child medSpeed()"
    parent.command(this, "medSpeed")
    sendEvent(name: "level", value: 60)        
    } else {
    parent.command(this, "on")    
    sendEvent(name: "switch", value: "on")
    log.debug "child medSpeed()"
    sendEvent(name: "level", value: 60)    
    }
}

def highSpeed() {
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
    log.debug "child highSpeed()"
    parent.command(this, "highSpeed")
    sendEvent(name: "level", value: 100)        
    } else {
    parent.command(this, "on")    
    sendEvent(name: "switch", value: "on")
    log.debug "child highSpeed()"
    sendEvent(name: "level", value: 90)    
    }
}

def swingMode() {
    log.debug "child swingMode()"
    log.debug "swingMode>> current: ${device.currentState("swingMode")?.value}"
    def currentState = device.currentState("swingMode")?.value

    if (currentState == "on") {
        sendEvent(name: "swingMode", value: "off")
        log.debug "turn off swingMode"
        parent.command(this, "swingModeOff")
    } else {
        sendEvent(name: "swingMode", value: "on")
        log.debug "turn of swingMode"
        parent.command(this, "swingModeOn")
    }
}

def sleepMode() {
    log.debug "child sleepMode()"
    log.debug "sleepMode>> current: ${device.currentState("sleepMode")?.value}"
    def currentState = device.currentState("sleepMode")?.value

    if (currentState == "on") {
        sendEvent(name: "sleepMode", value: "on1")
        log.debug "turn off sleepMode"
        parent.command(this, "sleepModeOff")
    } else if (currentState == "on1") {
        sendEvent(name: "sleepMode", value: "off")
        log.debug "turn off sleepMode"
        parent.command(this, "sleepModeOff")
    } else if (currentState == "off") {
        sendEvent(name: "sleepMode", value: "on")
        log.debug "turn off sleepMode"
        parent.command(this, "sleepModeOff")
    }
}

def timer() {
    log.debug "child timer()"
    parent.command(this, "timer")
}

def on() {
def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
	    sendEvent(name: "level", value: 100)
   		log.debug "child on()"
	} else {
	    sendEvent(name: "switch", value: "on")
	    sendEvent(name: "level", value: 30)        
   		log.debug "child on()"
	    parent.command(this, "on")
	}
    
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
def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
		log.debug "child off"
		parent.command(this, "off")
	    sendEvent(name: "switch", value: "off")
	    sendEvent(name: "level", value: 0)    
	    sendEvent(name: "sleepMode", value: "off")
	} else {
	    sendEvent(name: "switch", value: "off")
	    sendEvent(name: "level", value: 0)        
   		log.debug "child off()"
	}
}

def setLevel(level) {
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
if(level <= 100 && level >= 75) {
        log.debug "child highSpeed()"
        sendEvent(name: "level", value: 90)
        parent.command(this, "highSpeed")
    } else if(level < 75 && level >= 50) {
        log.debug "child medSpeed()"
        sendEvent(name: "level", value: 60)
        parent.command(this, "medSpeed")
    } else if(level < 50 && level >= 25) {
        log.debug "child lowSpeed()"
        sendEvent(name: "level", value: 30)
        parent.command(this, "lowSpeed")
    } else if(level < 25 && level >= 1) {
        log.debug "child sleepMode()"
        sendEvent(name: "sleepMode", value: "on")
        sendEvent(name: "level", value: 10)
        parent.command(this, "sleepMode")
    } else {
        log.debug "child off"
        parent.command(this, "off")
  	  	sendEvent(name: "switch", value: "off")
    	sendEvent(name: "level", value: 0)    
	    sendEvent(name: "sleepMode", value: "off")
    }    
  } else {
		if(level <= 100 && level >= 30) {
		parent.command(this, "on")
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: 30)
    } else if(level < 25 && level >= 1) {
        log.debug "child sleepMode()"
		parent.command(this, "on")
        sendEvent(name: "switch", value: "on")
        parent.command(this, "sleepMode")
        sendEvent(name: "sleepMode", value: "on")
    } else {
        log.debug "child off"
        sendEvent(name: "switch", value: "off")
        sendEvent(name: "level", value: 0)
	    sendEvent(name: "sleepMode", value: "off")
    }

}
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