# THIS PLUGIN IS DISCONTINUED DUE TO MYQ/CHAMBERLAIN DROPPING SUPPORT FOR THEIR API





# hubitat-myq
Driver Code for the MyQ garage door opener integration for Hubitat Elevation

### Pre-requisites
Must have a Hubitat Elevation hub installed  
_(Originally built for C7, but should work for other hub versions aswell)_  
Must have a MyQ account setup (https://myq.com)

### To integrate this into Hubitat, you'll follow these steps:
Start by logging into to your Hubitat local hub

### 2 AVAILABLE OPTIONS
- Driver Code: recognizes as a DOOR

### 1. Drivers Code
1. Navigation, click on _Drivers Code_.
2. Click on [+ New Driver].
3. Select the _From Code_ tab.
  - **DOOR CAPABILITY**  - drivercode-myq-door.groovy
  - **SWITCH CAPABILITY**  - drivercode-myq-switch.groovy
4. Copy and paste in the code from the selected file.
5. Click [Save].

#### 2. Devices
1. Navigation, click on _Devices_.
2. Click [+ Add Virtual Device].
3. Create the new device as such:
  - Device Name: MyQ Garage Door Opener
  - Device Label: MyQ Garage Door Opener
  - Zigbee Id: <blank>
  - Event history size: <blank>
  - State history size: <blank>
  - Device Network Id: <unchanged>
  - Type: MyQ Garage Door Opener
  - Hub mesh enabled: <unchanged>
4. Then click [Save Device].
5. Once the page reloads, scroll down to the Preferences row and set the following:
  - Username aka MyQ Username (email address): <myq.com username>
  - Password aka MyQ Password: <myq.com password>
  - Device Name aka Name of the device to manage: <the name of the device/door that is labeled in MyQ>
6. Then click [Save Preferences].

### Test MyQ Device Setup
1. Navigation, click on _Device_.
2. Click on _MyQ Garage Door Opener_.
3. Click [Refresh] to get the updated door state.

### DEBUGGING/TROUBLESHOOTING
If you edit the driver code in your local Hubitat environment, search for the "refresh()" function and enable debugging by setting the following:  
```groovy
set_DEBUG("off")
...to...
set_DEBUG("on")
```

### DISCLOSURE
I am not a Hubitat or a MyQ Chamberlain developer, this driver code is no way tied to Hubitat or MyQ companies.  I personally had a need for the MyQ garage door opener in my local Hubitat hub, so I created one myself.  It works well for me and this isn't for sale, so feel free to use it, tweak it, whatever you want!

Enjoy!
