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
    definition (name: "KuKu Mi_miremote_Light", namespace: "turlvo", author: "KuKu") {
        capability "Switch Level"
        capability "Actuator"
        capability "Indicator"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"

        command "lowLight"
        command "medLight"
        command "highLight"
        command "sleepMode"
        command "time30"
        command "time60"
        command "timer"

    }

    tiles (scale:2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.Lighting.light13", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.Lighting.light11", nextState:"turningOff"
                attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.Lighting.light11", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.Lighting.light13", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
                attributeState "level", label:'${currentValue}%'
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("lowLight", "device.light", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "LOW", label:'25%', action: "lowLight", icon:"st.quirky.spotter.quirky-spotter-luminance-dark"
        }
        standardTile("medLight", "device.light", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "MED", label: '50%', action: "medLight", icon:"st.quirky.spotter.quirky-spotter-luminance-light"
        }
        standardTile("highLight", "device.light", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "HIGH", label: '100%', action: "highLight", icon:"st.quirky.spotter.quirky-spotter-luminance-bright"
        }
        //		standardTile("swingMode", "device.swingMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
        //			state "on", label: 'Swing Mode', action: "swingMode", icon:"st.motion.motion.active", nextState:"off"
        //			state "off", label: 'Swing Mode', action: "swingMode", icon:"st.motion.motion.active", nextState:"on"
        //		}
        standardTile("sleepMode", "device.sleepMode", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "on", label:'Sleep On', action:"sleepMode", backgroundColor:"#79b821", icon: "st.Bedroom.bedroom11", nextState:"off"
            state "off", label:'Sleep Off', action:"sleepMode", backgroundColor:"#ffffff", icon: "st.Bedroom.bedroom11", nextState:"on"
        }
        standardTile("time30", "device.timer", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "time30", label: 'Timer 30min', action: "time30", icon:"st.Health & Wellness.health7"
        }
        standardTile("time60", "device.timer", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "time60", label: 'Timer 60min', action: "time60", icon:"st.Health & Wellness.health7"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2) {
            state "level", action:"switch level.setLevel"
        }
        main(["switch"])
        details(["switch", "lowLight", "medLight", "highLight", "sleepMode", "time30", "time60"])
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

def lowLight() {
    sendEvent(name: "switch", value: "on")    
    log.debug "child lowLight()"
    parent.command(this, "lowLight")
}

def medLight() {
    sendEvent(name: "switch", value: "on")
    log.debug "child medLight()"
    parent.command(this, "medLight")
}

def highLight() {
    sendEvent(name: "switch", value: "on")
    log.debug "child highLight()"
    parent.command(this, "highLight")
}

def sleepMode() {
    log.debug "child sleepMode()"
    log.debug "sleepMode>> current: ${device.currentState("sleepMode")?.value}"
    def currentState = device.currentState("sleepMode")?.value

    if (currentState == "on") {
        sendEvent(name: "sleepMode", value: "off")
        log.debug "turn off sleepMode"
        parent.command(this, "sleepModeOff")
    } else {
        sendEvent(name: "sleepMode", value: "on")
        log.debug "turn of sleepMode"
        parent.command(this, "sleepModeOn")
    }
}

def time30() {
    log.debug "child time30()"
    parent.command(this, "timer30")
}

def time60() {
    log.debug "child time60()"
    parent.command(this, "timer60")
}

def on() {
    sendEvent(name: "switch", value: "on")
    log.debug "child on()"
    parent.command(this, "on")

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
    sendEvent(name: "switch", value: "off")
    log.debug "child off"
    parent.command(this, "off")
}

def setLevel(level) {
    if(level <= 100 && level >= 75) {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: 90)
        log.debug "child highLight()"
        parent.command(this, "highLight")
    } else if(level < 75 && level >= 50) {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: 50)
        log.debug "child medLight()"
        parent.command(this, "medLight")
    } else if(level < 50 && level >= 25) {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: 25)
        log.debug "child lowLight()"
        parent.command(this, "lowLight")
    } else if(level < 25 && level >= 1) {
        sendEvent(name: "sleepMode", value: "on")
        sendEvent(name: "level", value: 10)            
        log.debug "child sleepMode()"
        parent.command(this, "sleepMode")
    } else {
        sendEvent(name: "switch", value: "off")
        sendEvent(name: "level", value: 0)
        log.debug "child off"
        parent.command(this, "off")
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