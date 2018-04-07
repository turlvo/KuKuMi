/**
 *  KuKu Mi - Xiaomi BT Temperature / Humidity Sensor's DTH
 *
 *  Copyright 2017 KuKu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "KuKu Mi_XiaomiBT_TempHumi", namespace: "turlvo", author: "KuKu") {

		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Health Check"
		capability "Sensor"
		
        fingerprint endpointId: "1", profileId: "0104", inClusters: "0000, 0003, 0402"
		
	}

	simulator {
		status 'H 40': 'catchall: 0104 FC45 01 01 0140 00 D9B9 00 04 C2DF 0A 01 000021780F'
		status 'H 45': 'catchall: 0104 FC45 01 01 0140 00 D9B9 00 04 C2DF 0A 01 0000218911'
		status 'H 57': 'catchall: 0104 FC45 01 01 0140 00 4E55 00 04 C2DF 0A 01 0000211316'
		status 'H 53': 'catchall: 0104 FC45 01 01 0140 00 20CD 00 04 C2DF 0A 01 0000219814'
		status 'H 43': 'read attr - raw: BF7601FC450C00000021A410, dni: BF76, endpoint: 01, cluster: FC45, size: 0C, attrId: 0000, result: success, encoding: 21, value: 10a4'
	}

	preferences {
		input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}

	tiles(scale: 2) {
        multiAttributeTile(name: "temp&humidity", type: "generic", width: 6, height: 4) {
            tileAttribute("temp&humidity", key: "PRIMARY_CONTROL") {
                attributeState "temp&humidity", label: '${currentValue}',
                    backgroundColors:[
                        [value: 0, color: "#153591"],
                        [value: 5, color: "#1e9cbb"],
                        [value: 10, color: "#90d2a7"],
                        [value: 15, color: "#44b621"],
                        [value: 20, color: "#f1d801"],
                        [value: 25, color: "#d04e00"],
                        [value: 30, color: "#bc2323"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]                                      
                    ]
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Last Update: ${currentValue}', icon: "st.Health & Wellness.health9")
            }
        }
		standardTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label: '${currentValue}°C', icon:"st.Weather.weather2"
		}
		standardTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "humidity", label:'${currentValue}%', icon:"st.Weather.weather12"
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        valueTile("temp&humidity2", "temp&humidity", decoration: "flat", inactiveLabel: false) {
            state "default", label:'${currentValue}', icon: "st.Weather.weather2",
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 5, color: "#1e9cbb"],
                    [value: 10, color: "#90d2a7"],
                    [value: 15, color: "#44b621"],
                    [value: 20, color: "#f1d801"],
                    [value: 25, color: "#d04e00"],
                    [value: 30, color: "#bc2323"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]                                      
                ]
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "temp&humidity2"
        details(["temp&humidity", "temperature", "humidity", "battery", "refresh"])
	}
}



// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "parse: desc: $description"
	def name = parseName(description)
	def value = parseValue(description)
	def temperatureUnit = getTemperatureScale()
    def zeroUnit = '--'
    def mergedValue

    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "lastCheckin", value: now)
    
    if (name == "temperature" && (Float.parseFloat(value) >= 47)) {
    	// Todo. ex) 47.9, 48, 48.1, 48.2
    	log.debug "wrong temp: " + value
    } else {    	
    	if (name == "temperature") {
        	log.debug "report temp event>> currentTemp: $value, beforeTemp: $state.beforeTemp"
            log.debug "report temp event>> beforeHumidity: $state.beforeHumidity"
        	state.beforeTemp = value

           	mergedValue = "${value == null ? zeroUnit : value}°$temperatureUnit / ${state.beforeHumidity == null ? zeroUnit : state.beforeHumidity}%"
            sendEvent(name:"temp&humidity", value: mergedValue, displayed: false)
        } else if (name == "humidity") {
        	log.debug "report humidity>> currentHumidity: $value, beforeHumidity: $state.beforeHumidity"
            log.debug "report humidity>> beforeTemp: $state.beforeTemp"        	
        	state.beforeHumidity = value

            mergedValue = "${state.beforeTemp == null ? zeroUnit : state.beforeTemp}°$temperatureUnit/${value == null ? zeroUnit : value}%"
            sendEvent(name:"temp&humidity", value: mergedValue, displayed: false)
        }
        
		def result = createEvent(name: name, value: value, unit: unit)
        //log.debug "Parse returned ${result?.descriptionText}"
           
        return result
    }	
}

def batterWarning() {
	// if this routine is executed, it means that there is no report during 900 seconds
    log.debug "batterWarning()"
	sendEvent(name:"battery", value:0)
}

private String parseName(String description) {
	if (description?.startsWith("temperature: ")) {
		return "temperature"
	} else if (description?.startsWith("humidity: ")) {
		return "humidity"
	} else if (description?.startsWith("read attr -")) {
		return "battery"
	}
	null
}



private String parseValue(String description) {

	if (description?.startsWith("temperature: ")) {
		def value = ((description - "temperature: ").trim()) as Float 
        
        if (getTemperatureScale() == "C") {
        	if (tempOffset) {
				return (Math.round(value * 10))/ 10 + tempOffset as Float
            } else {
				return (Math.round(value * 10))/ 10 as Float
			}            	
		} else {
        	if (tempOffset) {
				return (Math.round(value * 90/5))/10 + 32 + offset as Float
            } else {
				return (Math.round(value * 90/5))/10 + 32 as Float
			}            	
		}        
        
	} else if (description?.startsWith("humidity: ")) {
		def pct = (description - "humidity: " - "%").trim()
        
		if (pct.isNumber()) {
			return Math.round(new BigDecimal(pct)).toString()
		}
	} else if (description?.startsWith("catchall: ")) {
		return parseCatchAllMessage(description)
	} else {
    log.debug "unknown: $description"
    sendEvent(name: "unknown", value: description)
    }
	null
}


def generateEvent(Map results) {
	def temperatureUnit = getTemperatureScale()
    def zeroUnit = '--'
    def mergedValue
    results.each { name, value ->
		log.debug "generateEvent>> name: $name, value: $value"
        //sendEvent(name: name, value: value)
        
        if (name == "temperature") {
            log.debug "report temp event>> currentTemp: $value, beforeTemp: $state.beforeTemp"
            log.debug "report temp event>> beforeHumidity: $state.beforeHumidity"
            state.beforeTemp = value

            mergedValue = "${value == null ? zeroUnit : value}°$temperatureUnit / ${state.beforeHumidity == null ? zeroUnit : state.beforeHumidity}%"
            sendEvent(name:"temp&humidity", value: mergedValue, displayed: false)
        } else if (name == "humidity") {
            log.debug "report humidity>> currentHumidity: $value, beforeHumidity: $state.beforeHumidity"
            log.debug "report humidity>> beforeTemp: $state.beforeTemp"        	
            state.beforeHumidity = value

            mergedValue = "${state.beforeTemp == null ? zeroUnit : state.beforeTemp}°$temperatureUnit/${value == null ? zeroUnit : value}%"
            sendEvent(name:"temp&humidity", value: mergedValue, displayed: false)
        }

        sendEvent(name: name, value: value, unit: unit)
        
    }
    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
    return null
}