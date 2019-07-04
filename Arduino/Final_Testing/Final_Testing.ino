#include <SoftSerial.h>
#include <TinyPinChange.h> 

SoftSerial BTserial(0, 1); 
//the bluetooth module  TX | RX pins  is connected to pin 2 and 3

int knock1 = A1;                      // The Right piezo is connected to analog pin A1
int knock2 = A2;                      // The Left piezo is connected to analog pin A2
int knock3 = A0;                      // The Front piezo is connected to analog pin A0

unsigned long current_millis =0;      //variable to store current millis
unsigned long previous_millis =0;     //variable to store previous millis

#define time_Threshold 1000

#define ThresHold  30   // Knock ThresHold variable changed from 200 to 30 for testing


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
  
  if((knock1Value > ThresHold) && ( current_millis - previous_millis >= time_Threshold ) ){
    BTserial.print("Right "); 
    BTserial.print(knock1Value);
    BTserial.print(" / "); 
    BTserial.print(knock2Value);
    BTserial.print(" / "); 
    BTserial.println(knock3Value);
    previous_millis = current_millis;
    }
    
 if((knock2Value > ThresHold) && ( current_millis - previous_millis >= time_Threshold ) ){
    BTserial.print("Left "); 
    BTserial.print(knock1Value);
    BTserial.print(" / "); 
    BTserial.print(knock2Value);
    BTserial.print(" / "); 
    BTserial.println(knock3Value);
    previous_millis = current_millis;
    }

 if((knock3Value > ThresHold) && ( current_millis - previous_millis >= time_Threshold ) ){
    BTserial.print("Front "); 
    BTserial.print(knock1Value);
    BTserial.print(" / "); 
    BTserial.print(knock2Value);
    BTserial.print(" / "); 
    BTserial.println(knock3Value);
    previous_millis = current_millis;
   }
}
