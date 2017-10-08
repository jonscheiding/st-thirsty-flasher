/**
 *  Thirsty Flasher
 *
 *  Copyright 2017 Jon Scheiding
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
definition(
    name: "Thirsty Flasher",
    namespace: "jonscheiding",
    author: "Jon Scheiding",
    description: "SmartApp that flashes a light when a water sensor goes dry.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png")


preferences {
    section("Water Sensor") {
        input "sensor", "capability.waterSensor", title: "Sensor", required: true
    }
    section("Switch To Flash") {
        input "light", "capability.switch", title: "Switch", required: true
    }
    section("Only In Modes") {
        input "modes", "mode", title: "Modes", multiple: true, required: false
    }
}

def shouldBeFlashing() {
    if (sensor.currentWater != "dry") {
    	log.debug("No need to flash, sensor is ${sensor.currentWater}.")
        return false
    }

    if (modes != null) {
        if (!modes.any { it == location.currentMode }) {
	    	log.debug("No need to flash, mode is ${location.currentMode}.")
            return false
        }
    }
    
    log.debug("Flash light because sensor is ${sensor.currentWater} and mode is ${location.currentMode}.")

    return true
}

def flashIfNecessary(e) {
    if (!shouldBeFlashing()) {
        return
    }
    
    light.on()

    flipSwitch()
    runIn(2, flipSwitch)
    runIn(4, flashIfNecessary)
}

def flipSwitch() {
    if(light.currentSwitch == "off") {
    	log.debug("Turn light on.")
        light.on()
    } else {
	    log.debug("Turn light off.")
        light.off()
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(sensor, "water.dry", flashIfNecessary)
    subscribe(location, "mode", flashIfNecessary)
}
