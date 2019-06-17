#include <SoftSerial.h>
#include <TinyPinChange.h> 

SoftSerial BTserial(0, 2); 
//the bluetooth module  TX | RX pins  is connected to pin 2 and 3

int knockPin = A0;                      //  piezo1 is connected to analog pin A0
int knockPin2 = A2;                     //  piezo2 is connected to analog pin A2   
int knockPin3 = A3;                     //  piezo3 is connected to analog pin A3 

#define knockThresHold  100   // Knock ThresHold variable changed from 200 to 30 for testing

void setup() {
  BTserial.begin(9600);
  pinMode(A0, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT);
  }

void loop() {
  
  int knockSensorValue = analogRead(knockPin);
  int knockSensorValue2 = analogRead(knockPin2);
  int knockSensorValue3 = analogRead(knockPin3);
  //read the sensor and store it in the variable reading
  
  if(knockSensorValue>knockThresHold|knockSensorValue2>knockThresHold|knockSensorValue3>knockThresHold){
    BTserial.print("Knock");            //if the sensorValue(1,2 or 3) is larger than knockThresHold send "Knock"
    BTserial.println();                 //declear the end of the message 
  }
  }

  delay(20);
 // delay to avoid overloading the serial port buffer
}
