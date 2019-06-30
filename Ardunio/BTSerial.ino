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

#define ThresHold_1  200   // Knock ThresHold variable changed from 200 to 30 for testing
#define ThresHold_2  30   // Knock ThresHold variable changed from 200 to 30 for testing
#define ThresHold_3  200   // Knock ThresHold variable changed from 200 to 30 for testing

void setup() {
  pinMode(knock1,INPUT); //set connected pin to piezo as INPUT
  pinMode(knock2,INPUT); //set connected pin to piezo as INPUT
  pinMode(knock3,INPUT); //set connected pin to piezo as INPUT
  pinMode(A3,INPUT); // set A3 pin as INPUT
  BTserial.begin(9600); //set baudrate to 9600


}

void loop() {
  current_millis = millis();
  
  int knock1Value = analogRead(knock1); //Read the Right piezo sensor then store the value in knock1Value varuable
  int knock2Value = analogRead(knock2); //Read the Left piezo sensor then store the value in knock2Value varuable
  int knock3Value = analogRead(knock3); //Read the Front piezo sensor then store the value in knock3Value varuable
  
  if((knock1Value > ThresHold_1 || knock2Value > ThresHold_2 || knock3Value > ThresHold_3) && ( current_millis - previous_millis >= time_Threshold ) ) //check if the first sensor is knocked
  {
    BTserial.println("knock");
    previous_millis = current_millis;
  }
}
