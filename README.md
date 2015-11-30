# Android-based-SMS-Server

### Android Application parses the PDU SMS (Pure hex bytes) which is sent by an embedded hardware and post it to the cloud

* An embedded hardware sends hardware critical information along with its location (latlong) info to a central GSM number in PDU format. In PDU Mode, all SMS messages are represented as binary strings encoded in hexadecimal characters.

* This android application receives these PDU sms messages, parses the hardware information and post it to the cloud. 

* To provide store and forward facility, have used [Reyna library which is hosted on Github] (https://github.com/B2MSolutions/reyna) 

* Application will store the data and post them when there is a valid connection.
