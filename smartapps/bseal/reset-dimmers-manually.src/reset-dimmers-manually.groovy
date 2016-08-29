
definition(
    name: "reset_dimmers_manually",
    namespace: "bseal",
    author: "Brian Seal",
    description: "reset all dimmers to 100 percent brightness",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("When this button is pressed...") {
        input "thebutton", "capability.button", title: "Which Button?", required: true, multiple: false
    }
    section("Reset the following dimmers to 100%") {
        input "dimmers", "capability.switchLevel", title: "Which dimmers", required: true, multiple: true
    }
    section("Lights will be set to 100% for... ") {
        input "offDelaySec", "number", title: "Seconds", required: true
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
	subscribe(thebutton, "button", buttonHandler)
}

def buttonHandler(evt) {
  if (evt.value == "held") {
    log.debug "button was held"
  } else if (evt.value == "pushed") {
		dimmers.setLevel(100)
        runIn(offDelaySec, turnOffDimmers)
        //dimmers.off(delay:${offDelaySec})
  }
}

def turnOffDimmers() {
	log.debug "Turning Dimmers off now"
	dimmers.setLevel(0)
    
}
