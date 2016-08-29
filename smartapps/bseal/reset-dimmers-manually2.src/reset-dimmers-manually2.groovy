definition(
    name: "reset_dimmers_manually2",
    namespace: "bseal",
    author: "Brian Seal",
    description: "reset all dimmers to 100 percent brightness",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "configure")
}

def configure() {
    dynamicPage(name: "configure", title: "Configure Switch and Phrase", install: true, uninstall: true) {

            def actions = location.helloHome?.getPhrases()*.label
            if (actions) {
            actions.sort()
                 section("When the Following Routine is executed:") {
                      log.trace actions
                	  input "actions", "enum", title: "Routine", options: actions, required: true
                }
            }
            section("Reset the following dimmers to 100%") {
                input "dimmers", "capability.switchLevel", title: "Which dimmers", required: true, multiple: true
            }
            section("Lights will be set to 100% for... ") {
                input "offDelaySec", "number", title: "Seconds", required: true
            }
    }
}



def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
    subscribe(location, "routineExecuted", routineChanged)

}

def routineChanged(evt) {
    if(evt.displayName == actions) {
        /* 
        log.debug "routineChanged: $evt"
        log.debug "evt name: ${evt.name}"
        log.debug "evt value: ${evt.value}"
        log.debug "evt displayName: ${evt.displayName}"
        log.debug "evt descriptionText: ${evt.descriptionText}"
        */
        log.debug "Routine: ${evt.displayName} was executed, proceeded with Dimmer Reset"
		dimmers.setLevel(100)
        runIn(offDelaySec, turnOffDimmers)
     }
}



def turnOffDimmers() {
	log.debug "Turning Dimmers off now"
	dimmers.setLevel(0)
}

