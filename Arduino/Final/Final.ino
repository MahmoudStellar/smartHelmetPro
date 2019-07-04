#include <SoftSerial.h>
#include <TinyPinChange.h> 

SoftSerial BTserial(0, 1); 
//the bluetooth module  TX | RX pins  is connected to pin D0 and D1

int knock1 = A1;                      // The Right piezo is connected to analog pin A1
int knock2 = A2;                      // The Left piezo is connected to analog pin A2
int knock3 = A0;                      // The Front piezo is connected to analog pin A0

unsigned long current_millis =0;      //variable to store current millis
unsigned long previous_millis =0;     //variable to store previous millis

#define time_Threshold 1000

#define ThresHold  30   // Knock ThresHold variable changed from 200 to 30 after testing


void setup() {
  pinMode(knock1,INPUT); //set connected pin to piezo as INPUT
  pinMode(knock2,INPUT); //set connected pin to piezo as INPUT
  pinMode(knock3,INPUT); //set connected pin to piezo as INPUT
  BTserial.begin(9600); //set baudrate to 9600
}

void loop() {
  current_millis = millis();
  
  int knock1Value = analogRead(knock1); //Read the Right piezo sensor then store the value in knock1Value varuable
  int knock2Value = analogRead(knock2); //Read the Left piezo sensor then store the value in knock2Value varuable
  int knock3Value = analogRead(knock3); //Read the Front piezo sensor then store the value in knock3Value varuable
  
  if((knock1Value > ThresHold || knock2Value > ThresHold || knock3Value > ThresHold) && 
    ( current_millis - previous_millis >= time_Threshold ) ) //check if any sensor is knocked
    {
    BTserial.println("Knock");        //send "Knock" to the mobile to acctive alert mode
    previous_millis = current_millis; //wait 1000 ms or more before sending again
    }
}
