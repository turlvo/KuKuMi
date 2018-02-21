/**
 *  KuKu Mi - Virtual Switch for Xiaomi Mi products
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
 *
 *  Version history
 */
def version() {	return "v1.0.000" }
/*
 *  03/28/2017 >>> v1.0.000 - Release first 'KuKu Mi' SmartApp supporting 'Mi Remote'
 */

definition(
    name: "KuKu Mi${parent ? " - Device" : ""}",
    namespace: "turlvo",
    author: "KuKu",
    description: "This is a SmartApp that support to control Xiaomi's devices!",
    category: "Convenience",
    parent: parent ? "turlvo.KuKu Mi" : null,
    singleInstance: true,
    iconUrl: "https://cdn.rawgit.com/turlvo/KuKuMi/master/images/icon/KuKu_Mi_Icon_1x.png",
    iconX2Url: "https://cdn.rawgit.com/turlvo/KuKuMi/master/images/icon/KuKu_Mi_Icon_2x.png",
    iconX3Url: "https://cdn.rawgit.com/turlvo/KuKuMi/master/images/icon/KuKu_Mi_Icon_3x.png")

preferences {
	page(name: "parentOrChildPage")
    
    page(name: "mainPage")
    page(name: "installPage")
    page(name: "mainChildPage")
    
}

// ------------------------------
// Pages related to Parent
def parentOrChildPage() {
	parent ? mainChildPage() : mainPage()
}

// mainPage
// seperated two danymic page by 'isInstalled' value 
def mainPage() {
    if (!atomicState?.isInstalled) {
        return installPage()
    } else {
    	def interval
    	checkServer(atomicState.miApiServerIP)
        if (atomicState.serverStatus) {
            interval = 60
        } else {
            interval = 3
        }
        return dynamicPage(name: "mainPage", title: "", uninstall: true, refreshInterval: interval) {
            //getHubStatus()            
            section("KuKu Mi api Server IP Address :") {
            	href "installPage", title: "", description: "${atomicState.miApiServerIP}"
            }
            
            // Tdo : temp enable
			if (atomicState.serverStatus == true) {
				checkServer(atomicState.miApiServerIP)
				section() {            
					paragraph "Checking Mi API Server.  Please wait..."
				}
			} else {
				section("") {
					app( name: "MiDevices", title: "Add a device...", appName: "KuKu Mi", namespace: "turlvo", multiple: true, uninstall: false)
				}
			}

            section("KuKu Mi Version :") {
                paragraph "${version()}"
            }
        }
    }
}

def installPage() {
	dynamicPage(name: "installPage", title: "", install: !atomicState.isInstalled) {
            section("Enter the KuKu Mi API Server IP address :") {
       	       input name: "miApiIP", type: "text", required: true, title: "IP address?", submitOnChange: true
            }
            
            if (miApiIP) {
            	atomicState.miApiServerIP = miApiIP
            }
    } 	    
}

def initializeParent() {
    atomicState.isInstalled = true
    atomicState.miApiServerIP = miApiIP
    atomicState.hubStatus = "online"
}

def getMiApiServerIP() {
	return atomicState.miApiServerIP
}

// ------------------------------
// Pages realted to Child App
def mainChildPage() {
    def interval
    if (atomicState.serverStatus && atomicState.deviceCommands && atomicState.device) {
        interval = 15
    } else {
        interval = 3
    }
    return dynamicPage(name: "mainChildPage", title: "Add Device", refreshInterval: interval, uninstall: true, install: true) {    	
        log.debug "mainChildPage>> parent's atomicState.MiApiServerIP: ${parent.getMiApiServerIP()}"
        atomicState.miApiServerIP = parent.getMiApiServerIP()
        
		section("Device Type :") {                                                  
			input name: "selectDeviceType", type: "enum", title: "Select Device Type :", options: ["Mi Remote"], submitOnChange: true, required: true
			log.debug "mainChildPage>> selectDeviceType: $selectDeviceType"
			if (selectDeviceType) {
                switch (selectDeviceType) {
                	case "Mi Remote":                    
						atomicState.deviceType = "miremote"
                        break
                    default:
                    	break
                }
                discoverDevices(atomicState.deviceType)
                discoverCommands(atomicState.deviceType)
			}                
		}

        def foundDevices = getDeviceNames(getDevices())
        log.debug "mainChildPage>> foundDevices : ${foundDevices}"
        if (atomicState.deviceType && foundDevices) {
            section("Device :") {                                
                input name: "selectedDevice", type: "enum",  title: "Select Device", multiple: false, options: foundDevices, submitOnChange: true, required: true
                if (selectedDevice) {
                    atomicState.device = selectedDevice
                }
            }

            def foundCommands = getCommands()
            log.debug "mainChildPage>> deviceCommands : ${foundCommands}"
            if (foundCommands) {
                addMiRemoteCommandUI(foundCommands)

            } else if (selectedDevice && atomicState.deviceCommands == null) {
                // log.debug "addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
                section("") {
                    paragraph "Loading selected device's command.  This can take a few seconds. Please wait..."
                }
            }
        } else if (atomicState.deviceType) {
            section() {
                paragraph "Discovering devices.  Please wait..."
            }
        }
    }
}

// Add device page for Default On/Off device
def addMiRemoteCommandUI(foundCommands) {    
    state.selectedCommands = [:]    

    section("Commands :") {            
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedCmd1", type: "enum", title: "Command1", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd2", type: "enum", title: "Command2", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd3", type: "enum", title: "Command3", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd4", type: "enum", title: "Command4", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd5", type: "enum", title: "Command5", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd6", type: "enum", title: "Command6", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd7", type: "enum", title: "Command7", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd8", type: "enum", title: "Command8", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd9", type: "enum", title: "Command9", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd10", type: "enum", title: "Command10", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd11", type: "enum", title: "Command11", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedCmd12", type: "enum", title: "Command12", options: foundCommands, submitOnChange: true, multiple: false, required: false
    }
    state.selectedCommands["on"] = selectedPowerOn
    state.selectedCommands["off"] = selectedPowerOff
    state.selectedCommands["cmd1"] = selectedCmd1
    state.selectedCommands["cmd2"] = selectedCmd2
    state.selectedCommands["cmd3"] = selectedCmd3
    state.selectedCommands["cmd4"] = selectedCmd4
    state.selectedCommands["cmd5"] = selectedCmd5
    state.selectedCommands["cmd6"] = selectedCmd6
    state.selectedCommands["cmd7"] = selectedCmd7
    state.selectedCommands["cmd8"] = selectedCmd8
    state.selectedCommands["cmd9"] = selectedCmd9
    state.selectedCommands["cmd10"] = selectedCmd10
    state.selectedCommands["cmd11"] = selectedCmd11
    state.selectedCommands["cmd12"] = selectedCmd12    

}

def getCommandName(cmd) {
	def name = state.selectedCommands[cmd]
	log.debug "getCommandName>> cmd : ${cmd}, name: ${name}"
    return name
}

// Install child device
def initializeChild() {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice, type: $atomicState.deviceType"
    app.updateLabel("$selectedDevice")

    def device = []    
    device = getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("turlvo", "KuKu Mi_${selectDeviceType}", deviceId, null, ["label": device.name])
    } else {
        log.debug "Device already created"
        existing.updated()
    }
}


// For child Device
def command(child, command) {
	//def device = getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent command(child)>>  type : $atomicState.deviceType, device: $atomicState.device, command: $command"
    
    def result
    result = sendCommandToDevice(atomicState.deviceType, atomicState.device, command)
    if (result && result.message != "ok") {
        sendCommandToDevice(atomicState.device, command)
    }
}

// ------------------------------------
// ------- Default Common Method -------
def installed() {    
    initialize()
}

def updated() {
    //unsubscribe()
    initialize()
}

def initialize() {
	log.debug "initialize()"
	parent ? initializeChild() : initializeParent()
}


def uninstalled() {
	parent ? null : removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


// ---------------------------
// ------- Hub Command -------

// getCommandsOfDevice
// return : result of 'discoverCommands(deviceType)' method. It means that recently requested device's commands
def getCommands() {
    //log.debug "getCommandsOfDevice>> $atomicState.foundCommandOfDevice"
    
    // Todo
    //atomicState.foundCommands = ["turn_on", "turn_off"]
    return atomicState.foundCommands    

}
 
// getHubDevices
// return : searched list of device in Mi Hub when installed
def getDevices() {
	
    // Todo
    //atomicState.devices = ["mi1", "mi2"]
	return atomicState.devices
    
}

def getDeviceNames(devices) {
	def result = []
    
    if(devices) { 
    	devices.each {
        	result.add(it.name)
        }
    }
    
    return result
}    	

def getDeviceByName(name) {
	def result
    
    if(atomicState.devices) { 
    	atomicState.devices.each {
        	if(it.name == name) {
        		result = it
            }              
        }
    }
    
    return result
}   

// --------------------------------
// ------- HubAction Methos -------
// sendCommandToDevice
// parameter :
// - devType : target device type
// - device : target device
// - command : sending command
// return : 'sendCommandToDevice_response()' method callback
def sendCommandToDevice(devType, device, command) {
	log.debug("sendCommandToDevice >> miApiServerIP : ${parent.getMiApiServerIP()}")
    sendHubCommand(setHubAction(parent.getMiApiServerIP(), "/$devType/api/device/$device/command/$command", "sendCommandToDevice_response"))
}

def sendCommandToDevice_response(resp) {
    def result = []
    //log.debug("sendCommandToDevice_response>> resp: ${resp.description}")
    if (parseLanMessage(resp.description).body != null ) {
        def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
        log.debug("sendCommandToDevice_response >> $body")
    } else {
    	log.error("sendCommandToDevice_response >> response body is  null")
    }
}


// discoverCommands
// parameter : 
// - devType : Searching command of devType
// return : 'discoverCommands_response()' method callback
def discoverCommands(devType) {	
    log.debug "discoverCommands>> type:$devType"
    
    sendHubCommand(getHubAction(atomicState.miApiServerIP, "/$devType/api/commands", "discoverCommands_response"))

}

def discoverCommands_response(resp) {
   	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	log.debug("discoverCommands_response >> $body")

    if(body) {            	
        body.each {            
            //def command = ['label' : it.label, 'slug' : it.slug]
            log.debug "getCommands_response>> command: $it"
            result.add(it.name)            
        }
    }
    
    atomicState.foundCommands = result    
}

// discoverDevices
// parameter : 
// - devType : Searching device's type
// return : 'discoverDevices_response()' method callback
def discoverDevices(devType) {
	log.debug "discoverDevices>> $devType"
	sendHubCommand(getHubAction(atomicState.miApiServerIP, "/$devType/api/devices", "discoverDevices_response"))
}

def discoverDevices_response(resp) {
	def result = []
    
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverDevices_response >> $body")
	
    if(body) {            	
        body.each {
            log.debug "discoverDevices_response: $it"
            def device = ['id' : it.dev_ID, 'name' : it.name]
            result.add(device)
        }
    }

    atomicState.devices = result
}


// checkServer
// parameter : 
// - host : ip address searching hubs
// return : 'discoverHubs_response()' method callback
def checkServer(host) {
	log.debug("checkServer: $host")
    return sendHubCommand(getHubAction(host, "/miremote", "checkServer_response"))
}

def checkServer_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("checkServer_response >> $body.hubs")
	
    if(body && body.hubs != null) {            	
//        body.hubs.each {
//            log.debug "checkServer_response: $it"
//            result.add(it)
//        }

        atomicState.serverStatus = true
    } else {
    	atomicState.serverStatus = false
    }    
}

// -----------------------------
// -------Hub Action API -------
// getHubAction
// parameter :
// - host : target address to send 'GET' action
// - url : target url
// - callback : response callback method name
def getHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}

// setHubAction
// parameter :
// - host : target address to send 'POST' action
// - url : target url
// - callback : response callback method name
def setHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("POST ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}