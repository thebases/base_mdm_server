# Base MDM - a platform for corporate Android applications

Base MDM is a Mobile Device Management platform for Android devices, designed for corporate app developers and IT managers and is fork of H-mdm

## Features

 - Enrollment to Android 7+ devices through scanning a QR-code
 - Work in "Application mode" without enrollment
 - Customize the mobile desktop design and available applications
 - Automatic deployment of applications through the web panel
 - Mobile device management: groups, configurations, device status
 - Setup the available mobile device capabilities (GPS, Wi-Fi, Bluetooth etc.)
 - Manage the automatic OS update mode on the mobile device
 - Extensible platform design allowing the custom plugin development
 - Collection of application logs in the web panel
 - Centralized configuration of corporate applications

## Quick start

Base MDM control panel is cross-platform (it is written in Java and uses Tomcat web server). However the best OS for the deployment of Base MDM control panel is Ubuntu Linux. 

 - Clone the project and build it (see BUILD.txt for details)
 - Install the web panel to the server by using the installer script
 - Open the web panel and follow the hints to generate a QR code
 - Perform the factory reset on your Android device, tap 7 times on the welcome screen
 - Follow the instructions to scan a QR code and enroll the mobile agent
 




