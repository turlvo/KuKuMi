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
def version() {	return "v1.1.500" }
/*
 *  02/21/2018 >>> v1.0.000 - Release first 'KuKu Mi' SmartApp supporting 'Mi Remote'
 *  02/25/2018 >>> v1.1.000 - Added DTH Type and Device Name system 
 *  03/01/2018 >>> v1.1.500 - Modified 'Fan' DTH and added 'Light' DTH by ShinJjang
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
    page(name: "mainChildPageNext")
    
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
    	//checkServer(atomicState.miApiServerIP)
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
				//checkServer(atomicState.miApiServerIP)
				section() {            
					paragraph "Checking Mi API Server.  Please wait..."
				}
			} else {
				section("") {
					app( name: "MiDevices", title: "Add a device...", appName: "KuKu Mi", namespace: "turlvo", multiple: true, uninstall: false)
				}
			}

            section("Dashboard") {
                href(name: "hrefNotRequired",
                     title: "Dashboard(Local Network Only)",
                     required: false,
                     style: "external",
                     url: "http://${atomicState.miApiServerIP}/miremote",
                     description: "tap to view KuKu Mi API website in mobile browser")
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
       	       input name: "miApiIP", type: "text", required: true, title: "IP address(default 8484 port)?", submitOnChange: true
            }
            
            if (miApiIP) {
            	if (miApiIP.contains(":")) {
            		atomicState.miApiServerIP = miApiIP
                } else {
                	atomicState.miApiServerIP = miApiIP + ":8484"
                }
                log.debug "installPage>> miApiServerIP : ${atomicState.miApiServerIP}"
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
    return dynamicPage(name: "mainChildPage", title: "Add Device", refreshInterval: interval, nextPage: "mainChildPageNext", uninstall: true) {    	
        log.debug "mainChildPage>> parent's atomicState.MiApiServerIP: ${parent.getMiApiServerIP()}"
        atomicState.miApiServerIP = parent.getMiApiServerIP()
        
		section("Xiaomi Device Type :") {                                                  
			input name: "selectXiaomiDeviceType", type: "enum", title: "Select Xiaomi Device Type :", options: ["Mi Remote"], submitOnChange: true, required: true
			log.debug "mainChildPage>> selectXiaomiDeviceType: $selectXiaomiDeviceType"
			if (selectXiaomiDeviceType) {
                switch (selectXiaomiDeviceType) {
                	case "Mi Remote":                    
                    atomicState.xiaomiDeviceType = "miremote"
                    break
                    default:
                        break
                }
                discoverDevices(atomicState.xiaomiDeviceType)
                discoverCommands(atomicState.xiaomiDeviceType)
            }                
        }

        def foundDevices = getDeviceNames(getDevices())
        log.debug "mainChildPage>> foundDevices : ${foundDevices}"
        if (selectXiaomiDeviceType && foundDevices) {
            section("Xiaomi Device :") {                                
                input name: "selectedDevice", type: "enum",  title: "Select Xiaomi Device", multiple: false, options: foundDevices, submitOnChange: true, required: true
                if (selectedDevice) {
                    atomicState.device = selectedDevice
                }
            }

            section("Name :") { 
            	input name: "enteredName", type: "text",  title: "Input device name", submitOnChange: true, required: true
                log.debug "mainChildPage>> enteredName : ${enteredName}"
                if (enteredName) {
                    atomicState.deviceName = enteredName
                }
            }
            section("DTH Type :") {
                def deviceType = ["Custom", "Aircon", "TV", "Fan", "Light"]
                input name: "selectedDthType", type: "enum", title: "Select DTH Type", multiple: false, options: deviceType, submitOnChange: true, required: true
                if (selectedDthType) {
                    atomicState.dthType = selectedDthType
                }
            }
        } else if (atomicState.xiaomiDeviceType) {
            section() {
                paragraph "Discovering devices.  Please wait..."
            }
        }
    }
}

def mainChildPageNext() {
    return dynamicPage(name: "mainChildPageNext", title: "Command Setting", uninstall: true, install: true) {
        def foundCommands = getCommands()

        log.debug "mainChildPage>> deviceCommands : ${foundCommands}"
        if (atomicState.dthType && foundCommands) {
            switch (atomicState.dthType) {
                case "Aircon":
                addAirconDevice(foundCommands)
                break
                case "TV":                    
                addTvDevice(foundCommands)
                break                
                case "Fan":
                addFanDevice(foundCommands)
                break
                case "Light":
                addLightDevice(foundCommands)
                break;
                case "Custom":
                default:
                    log.debug "selectedDeviceType>> default"
                addMiRemoteCommandUI(foundCommands)
            }


        } else if (selectedDevice && atomicState.deviceCommands == null) {
            // log.debug "addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
            section("") {
                paragraph "Loading selected device's command.  This can take a few seconds. Please wait..."
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
    
    monitorMenu() 

}

// Add device page for Fan device
def addFanDevice(foundCommands) {
    state.selectedCommands = [:]

    section("Commands :") {
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedHighSpeed", type: "enum", title: "High Speed", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedMedSpeed", type: "enum", title: "Med Speed", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedLowSpeed", type: "enum", title: "Low Speed", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSwingModeOn", type: "enum", title: "Swing On", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSwingModeOff", type: "enum", title: "Swing Off", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSleepModeOn", type: "enum", title: "Sleep Mode On", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSleepModeOff", type: "enum", title: "Sleep Mode Off", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer", type: "enum", title: "Timer", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "custom1", type: "enum", title: "Custom1", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: foundCommands, submitOnChange: true, multiple: false, required: false  
    }
    //state.selectedCommands["power"] = selectedPower
    state.selectedCommands["on"] = selectedPowerOn
    state.selectedCommands["off"] = selectedPowerOff    
    state.selectedCommands["highSpeed"] = selectedHighSpeed
    state.selectedCommands["medSpeed"] = selectedMedSpeed
    state.selectedCommands["lowSpeed"] = selectedLowSpeed
    state.selectedCommands["swingModeOn"] = selectedSwingModeOn
    state.selectedCommands["swingModeOff"] = selectedSwingModeOff
    state.selectedCommands["timer"] = selectedTimer
    state.selectedCommands["sleepModeOn"] = selectedSleepModeOn
    state.selectedCommands["sleepModeOff"] = selectedSleepModeOff
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5    

	monitorMenu() 
}

// Add device page for Fan device
def addLightDevice(foundCommands) {
    state.selectedCommands = [:]

    section("Commands :") {
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedHighLight", type: "enum", title: "High Light", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedMedLight", type: "enum", title: "Med Light", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedLowLight", type: "enum", title: "Low Light", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSleepModeOn", type: "enum", title: "Sleep Mode On", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedSleepModeOff", type: "enum", title: "Sleep Mode Off", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer30", type: "enum", title: "Timer 30 minutes", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer60", type: "enum", title: "Timer 60 minutes", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "custom1", type: "enum", title: "Custom1", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: foundCommands, submitOnChange: true, multiple: false, required: false  
    }
    //state.selectedCommands["power"] = selectedPower
    state.selectedCommands["on"] = selectedPowerOn
    state.selectedCommands["off"] = selectedPowerOff    
    state.selectedCommands["highLight"] = selectedHighLight
    state.selectedCommands["medLight"] = selectedMedLight
    state.selectedCommands["lowLight"] = selectedLowLight
    state.selectedCommands["sleepModeOn"] = selectedSleepModeOn
    state.selectedCommands["sleepModeOff"] = selectedSleepModeOff
    state.selectedCommands["timer30"] = selectedTimer30
    state.selectedCommands["timer60"] = selectedTimer60
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5    

	monitorMenu() 
}

// Add device page for Aircon
def addAirconDevice(foundCommands) {
    state.selectedCommands = [:]
    
    section("Commands :") {            
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedTempUp", type: "enum", title: "Temperature Up", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedMode", type: "enum", title: "Mode", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedJetCool", type: "enum", title: "JetCool", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "selectedTempDown", type: "enum", title: "Temperature Down", options: foundCommands, submitOnChange: true, multiple: false, required: false    
        input name: "selectedSpeed", type: "enum", title: "Fan Speed", options: foundCommands, submitOnChange: true, multiple: false, required: false   
        input name: "custom1", type: "enum", title: "Custom1", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: foundCommands, submitOnChange: true, multiple: false, required: false  
    }

    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["on"] = selectedPowerOn
    state.selectedCommands["off"] = selectedPowerOff    
    state.selectedCommands["tempup"] = selectedTempUp
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["jetcool"] = selectedJetCool
    state.selectedCommands["tempdown"] = selectedTempDown
    state.selectedCommands["speed"] = selectedSpeed
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5  

	monitorMenu() 
}

// Add device page for TV
def addTvDevice(foundCommands) {
    state.selectedCommands = [:]
    
    section("Commands :") {     
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: foundCommands, submitOnChange: true, multiple: false, required: true
        input name: "selectedVolumeUp", type: "enum", title: "Volume Up", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedChannelUp", type: "enum", title: "Channel Up", options: foundCommands, submitOnChange: true, multiple: false, required: false
        input name: "selectedMute", type: "enum", title: "Mute", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "selectedVolumeDown", type: "enum", title: "Volume Down", options: foundCommands, submitOnChange: true, multiple: false, required: false    
        input name: "selectedChannelDown", type: "enum", title: "Channel Down", options: foundCommands, submitOnChange: true, multiple: false, required: false      
        input name: "selectedMenu", type: "enum", title: "Menu", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "selectedHome", type: "enum", title: "Home", options: foundCommands, submitOnChange: true, multiple: false, required: false    
        input name: "selectedInput", type: "enum", title: "Input", options: foundCommands, submitOnChange: true, multiple: false, required: false              
        input name: "selectedBack", type: "enum", title: "Back", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom1", type: "enum", title: "Custom1", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom2", type: "enum", title: "Custom2", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom3", type: "enum", title: "Custom3", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom4", type: "enum", title: "Custom4", options: foundCommands, submitOnChange: true, multiple: false, required: false  
        input name: "custom5", type: "enum", title: "Custom5", options: foundCommands, submitOnChange: true, multiple: false, required: false  
    }
    
    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["on"] = selectedPowerOn
    state.selectedCommands["off"] = selectedPowerOff  
	state.selectedCommands["volup"] = selectedVolumeUp
    state.selectedCommands["chup"] = selectedChannelUp
    state.selectedCommands["mute"] = selectedMute
    state.selectedCommands["voldown"] = selectedVolumeDown
    state.selectedCommands["chdown"] = selectedChannelDown
    state.selectedCommands["menu"] = selectedMenu
    state.selectedCommands["home"] = selectedHome
    state.selectedCommands["input"] = selectedInput
    state.selectedCommands["back"] = selectedBack
    state.selectedCommands["custom1"] = custom1
    state.selectedCommands["custom2"] = custom2
    state.selectedCommands["custom3"] = custom3
    state.selectedCommands["custom4"] = custom4
    state.selectedCommands["custom5"] = custom5  
 
 	monitorMenu() 
}


// ------------------------------------
// Monitoring sub menu
def monitorMenu() {
    section("State Monitor :") {
        paragraph "It is a function to complement IrDA's biggest drawback. Through sensor's state, synchronize deivce status."
        def monitorType = ["Power Meter", "Contact"]
        input name: "selectedMonitorType", type: "enum", title: "Select Monitor Type", multiple: false, options: monitorType, submitOnChange: true, required: false                    
    }  

    atomicState.selectedMonitorType = selectedMonitorType
    if (selectedMonitorType) {            
        switch (selectedMonitorType) {
            case "Power Meter":
            powerMonitorMenu()                
            break
            case "Contact":
            contactMonitorMenu()
            break
        }
    }
}

def powerMonitorMenu() {
    section("Power Monitor :") {
        input name: "powerMonitor", type: "capability.powerMeter", title: "Device", submitOnChange: true, multiple: false, required: false
        state.triggerOnFlag = false;
        state.triggerOffFlag = false;
        if (powerMonitor) {                
            input name: "triggerOnValue", type: "decimal", title: "On Trigger Watt", submitOnChange: true, multiple: false, required: true
            input name: "triggerOffValue", type: "decimal", title: "Off Trigger Watt", submitOnChange: true, multiple: false, required: true                
        }   
    } 
}

def contactMonitorMenu() {
    section("Contact :") {
        input name: "contactMonitor", type: "capability.contactSensor", title: "Device", submitOnChange: true, multiple: false, required: false
    	if (contactMonitor) {    
            paragraph "[Normal] : Open(On) / Close(Off)\n[Reverse] : Open(Off) / Close(On)"
            input name: "contactMonitorMode", type: "enum", title: "Mode", multiple: false, options: ["Normal", "Reverse"], defaultValue: "Normal", submitOnChange: true, required: true	
    	}
        atomicState.contactMonitorMode = contactMonitorMode
    }
}


// ------------------------------------
// Monitor Handler
// Subscribe power value and change status
def powerMonitorHandler(evt) {
    //def device = []    
    //device = getDeviceByName("$selectedDevice")
    //def deviceId = device.id
    def deviceId = "${atomicState.device}_${atomicState.deviceName}"
    def child = getChildDevice(deviceId)
    def event

    log.debug "value is over triggerValue>> flag: $state.triggerOnFlag, value: $evt.value, triggerValue: ${triggerOnValue.floatValue()}"        
    if (Float.parseFloat(evt.value) >= triggerOnValue.floatValue() && state.triggerOnFlag == false) {    	
        event =  [value: "on"]
        child.generateEvent(event)
        log.debug "value is over send*****"
        state.triggerOnFlag = true
    } else if (Float.parseFloat(evt.value) < triggerOnValue.floatValue()) {
        state.triggerOnFlag = false
    }

    log.debug "value is under triggerValue>> flag: $state.triggerOffFlag, value: $evt.value, triggerValue: ${triggerOffValue.floatValue()}"
    if (Float.parseFloat(evt.value) <= triggerOffValue.floatValue() && state.triggerOffFlag == false){    	
        event =  [value: "off"]        
        child.generateEvent(event)
        log.debug "value is under send*****"
        state.triggerOffFlag = true
    } else if (Float.parseFloat(evt.value) > triggerOffValue.floatValue()) {
        state.triggerOffFlag = false
    }

}

// Subscribe contact value and change status
def contactMonitorHandler(evt) {
    //def device = []    
    //device = getDeviceByName("$selectedDevice")
    //def deviceId = device.id
    def deviceId = "${atomicState.device}_${atomicState.deviceName}"
    def child = getChildDevice(deviceId)
    def event

	def contacted = "off", notContacted = "on"
    if (atomicState.contactMonitorMode == "Reverse") {
    	contacted = "on"
        notContacted = "off"
    }
    log.debug "contactMonitorHandler>> value is : $evt.value"
    if (evt.value == "open") {
        event = [value: notContacted] 
    } else {
        event = [value: contacted] 
    }
    child.generateEvent(event)
}



// ------------------------------------

def getCommandName(cmd) {
	def name = state.selectedCommands[cmd]
	log.debug "getCommandName>> cmd : ${cmd}, name: ${name}"
    return name
}

// Install child device
def initializeChild() {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice, xiaomi type: $atomicState.xiaomiDeviceType"


	unsubscribe()
    if (atomicState.selectedMonitorType == "Power Meter") {  
    	log.debug "Power: $powerMonitor"
    	subscribe(powerMonitor, "power", powerMonitorHandler)
    } else if (atomicState.selectedMonitorType == "Contact") {
    	log.debug "Contact: $contactMonitor"
    	subscribe(contactMonitor, "contact", contactMonitorHandler)
    }
    
    def deviceId = "${atomicState.device}_${atomicState.deviceName}"
    def existing = getChildDevice(deviceId)
    app.updateLabel("$deviceId")
    if (!existing) {
        def childDevice = addChildDevice("turlvo", "KuKu Mi_${atomicState.xiaomiDeviceType}_${atomicState.dthType}", 
        								deviceId, null, ["label": deviceId])
    } else {
        log.debug "Device already created"
        existing.updated()
    }
}


// For child Device
def command(child, command) {
	//def device = getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent command(child)>>  type : $atomicState.xiaomiDeviceType, device: $atomicState.device, command: $command"
    def realCmd = getCommandName(command)
 
 	if (realCmd != null) {
    	sendCommandToDevice(atomicState.xiaomiDeviceType, atomicState.device, realCmd)
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
// return : result of 'discoverCommands(xiaomiDeviceType)' method. It means that recently requested device's commands
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
    return
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
	//log.debug("discoverCommands_response >> $body")

    if(body) {            	
        body.each {            
            //def command = ['label' : it.label, 'slug' : it.slug]
            //log.debug "getCommands_response>> command: $it"
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