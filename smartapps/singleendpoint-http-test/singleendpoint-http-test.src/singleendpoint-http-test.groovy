/**
 *  SingleEndpoint_HTTP_TEST
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
    name: "SingleEndpoint_HTTP_TEST",
    namespace: "SingleEndpoint_HTTP_TEST",
    author: "Brian Seal",
    description: "test",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

preferences {
    section(title: "Select Devices") {
        input "light", "capability.switch", title: "Select a light or outlet", required: true, multiple:false
    }
}

// Since the SmartApp doesn't have any dependencies when it's installed or updated,
// we don't need to worry about those states.
def installed() {}
def updated() {}


// This block defines an endpoint, and which functions will fire depending on which type
// of HTTP request you send
mappings {
    // The path is appended to the endpoint to make requests
    path("/switch") {
        // These actions link HTTP verbs to specific callback functions in your SmartApp
        action: [
            GET: "getSwitch", // "When an HTTP GET request is received, run getSwitch()"
            PUT: "setSwitch"
        ]
    }
}


// Callback functions
def getSwitch() {
    // This returns the current state of the switch in JSON
    return light.currentState("switch")
}

def setSwitch() {
    switch(request.JSON.value) {
        case "on":
            light.on();
            break;
        case "off":
            light.off();
            break;
        default:
            break;
    }
}