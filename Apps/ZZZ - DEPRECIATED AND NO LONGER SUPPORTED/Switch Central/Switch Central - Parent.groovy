/**
 *  Design Usage:
 *  This is the 'Parent' app for scheduled switching
 *
 *
 *  Copyright 2018 Andrew Parker
 *  
 *  This SmartApp is free!
 *
 *  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://www.paypal.me/smartcobra
 *  
 *
 *  I'm very happy for you to use this app without a donation, but if you find it useful
 *  then it would be nice to get a 'shout out' on the forum! -  @Cobra
 *  Have an idea to make this app better?  - Please let me know :)
 *
 *  
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @Cobra
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Last Update: 31/07/2018
 *
 *  Changes:
 *
 * 
 *
 *  
 *  V1.0.0 - POC
 *
 */



definition(
    name:"Switch Central",
    namespace: "Cobra",
    author: "Andrew Parker",
    description: "This is the 'Parent' app for multiple switch child apps",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
    )







preferences {
	 
     page name: "mainPage", title: "", install: true, uninstall: true // ,submitOnChange: true 
     
} 


def installed() {
   
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
	version()
    log.info "There are ${childApps.size()} child smartapps"
    childApps.each {child ->
    log.info "Child app: ${child.label}"
    }
}
 
 
 
def mainPage() {
    dynamicPage(name: "mainPage") {
      installCheck()

    if(state.appInstalled == 'COMPLETE'){
			display()
        
       chooseApps()
    }
 } 
}


def chooseApps(){

    section ("You must have the relevant childapp installed before selecting it here"){
        input "includedApps", "enum", title: "Select Apps To Include", required: false, multiple: true, submitOnChange: true, options: ["1": "Scheduled Switch", "2": "Switch Changeover", "3": "One To Many", "4": "Daily Switch Event"] 
        }
   
    if(!includedApps){ 
        state.choose = "0"
    }
    else{
        state.choose = includedApps
        
    }
   
    if(state.choose == "0"){
        section(){
    paragraph "You MUST choose at least one app above"
        }

    }
       if(state.choose.contains("1")){
section ("<b>Switch Schedule</b>"){} 
      section (){app(name: "switchSchedule", appName: "Switch Central - Scheduled Switch", namespace: "Cobra", title: "<b>Add A New Switch Schedule</b>", multiple: true)}
   }
      if(state.choose.contains("2")){   
        section (" "){}  
        section ("<b>Switch Changeover</b>"){} 
      section (){app(name: "switchChangeover", appName: "Switch Central - Switch Changeover", namespace: "Cobra", title: "<b>Add A New Switch Changeover</b>", multiple: true)} 
      }
     if(state.choose.contains("3")){
         section (" "){} 
        section ("<b>One2Many</b> "){}
      section (){app(name: "switchone2many", appName: "Switch Central - One To Many", namespace: "Cobra", title: "<b>Add A New One To Many Switch</b>", multiple: true)} 
     }
     if(state.choose.contains("4")){
         section (" "){} 
        section ("<b>Daily Switch Event</b> "){}
      section (){app(name: "switchSchedule1", appName: "Switch Central - Schedule A Daily Switch Event", namespace: "Cobra", title: "<b>Add A New Daily Switch Event</b>", multiple: true)}  
     }
        section (" "){}
        section (" "){}
      section() {label title: "Enter a name for this parent app (optional)", required: false}   
    
}

def installCheck(){         
   state.appInstalled = app.getInstallationState() 
  if(state.appInstalled != 'COMPLETE'){
section{paragraph "Please hit 'Done' to install $app.label"}
  }
    else{
 //       log.info "Parent Installed OK"
    }
	}


def version(){
	unschedule()
	schedule("0 0 9 ? * FRI *", updateCheck) //  Check for updates at 9am every Friday
	updateCheck()  
}

def display(){
	if(state.status){
	section{paragraph "Version: $state.version -  $state.Copyright"}
	if(state.status != "Current"){
	section{ 
	paragraph "$state.status"
	paragraph "$state.UpdateInfo"
    }
    }
}
}


def updateCheck(){
    setVersion()
	def paramsUD = [uri: "http://update.hubitat.uk/cobra.json"]
       	try {
        httpGet(paramsUD) { respUD ->
 //  log.warn " Version Checking - Response Data: ${respUD.data}"   // Troubleshooting Debug Code 
       		def copyrightRead = (respUD.data.copyright)
       		state.Copyright = copyrightRead
            def newVerRaw = (respUD.data.versions.Application.(state.InternalName))
            def newVer = (respUD.data.versions.Application.(state.InternalName).replace(".", ""))
       		def currentVer = state.version.replace(".", "")
      		state.UpdateInfo = (respUD.data.versions.UpdateInfo.Application.(state.InternalName))
                state.author = (respUD.data.author)
           
		if(newVer == "NLS"){
            state.status = "<b>** This app is no longer supported by $state.author  **</b>"       
            log.warn "** This app is no longer supported by $state.author **"      
      		}           
		else if(currentVer < newVer){
        	state.status = "<b>New Version Available (Version: $newVerRaw)</b>"
        	log.warn "** There is a newer version of this app available  (Version: $newVerRaw) **"
        	log.warn "** $state.UpdateInfo **"
       		} 
		else{ 
      		state.status = "Current"
      		log.info "You are using the current version of this app"
       		}
      					}
        	} 
        catch (e) {
        	log.error "Something went wrong: CHECK THE JSON FILE AND IT'S URI -  $e"
    		}
 	
}

def setVersion(){
		state.version = "1.1.0"	 
		state.InternalName = "SwitchCentral"  
}


