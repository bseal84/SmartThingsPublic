/**
 *  tomysql2
 *
 *  Copyright 2016 Brian Seal
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
    name: "test_ST",
    namespace: "test_ST",
    author: "Brian Seal",
    description: "to my mysql",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


 preferences {
    page(name: "page1", title: "Select sensor and actuator types", nextPage: "page2", uninstall: true) {
        section {
            input("sensorType", "enum", options: [
                "contactSensor":"Open/Closed Sensor",
                "motionSensor":"Motion Sensor",
                "switch": "Switch",
                "moistureSensor": "Moisture Sensor"])

            input("actuatorType", "enum", options: [
                "switch": "Light or Switch",
                "lock": "Lock"]
            )
        }
    }

    page(name: "page2", title: "Select devices and action", install: true, uninstall: true)

}

def page2() {
    dynamicPage(name: "page2") {
        section {
            input(name: "sensor", type: "capability.$sensorType", title: "If the $sensorType device")
            input(name: "sensorAction", type: "enum", title: "is", options: attributeValues(sensorType))
        }
        section {
            input(name: "actuator", type: "capability.$actuatorType", title: "Set the $actuatorType")
            input(name: "actuatorAction", type: "enum", title: "to", options: actions(actuatorType))
         }

    }
}

private attributeValues(attributeName) {
    switch(attributeName) {
        case "switch":
            return ["on","off"]
        case "contactSensor":
            return ["open","closed"]
        case "motionSensor":
            return ["active","inactive"]
        case "moistureSensor":
            return ["wet","dry"]
        default:
            return ["UNDEFINED"]
    }
}

private actions(attributeName) {
    switch(attributeName) {
        case "switch":
            return ["on","off"]
        case "lock":
            return ["lock","unlock"]
        default:
            return ["UNDEFINED"]
    }
}
