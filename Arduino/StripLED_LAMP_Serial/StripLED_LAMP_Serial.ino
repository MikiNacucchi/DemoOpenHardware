/*
Linux Day 2013 Bari
OpenHardware: il futuro di Arduino + Arduino incontra Android 
http://ld13bari.github.io/

Author: Alessandro Ruggieri - Miki Nacucchi
Description: 
Pilotaggio di una Stripe LED RGB e accensione/spegnimento di un White LED 10mm, 
tramite comandi su Seriale.

  1° Byte: 0x00 LAMP OFF,  !(0x00) LAMP ON
  Successivi 3 Bytes: Color in formato RGB 
  
La scheda Arduino Mega è collegata tramite cavo USB ad un tablet Android, 
su cui è stata realizzata un app che invia i precedenti Byte tramite USB Host.
*/

#define LAMP      2
#define BLUEPIN   4
#define REDPIN    5
#define GREENPIN  6
 
void setup() {
  // initialize both serial ports:
  Serial.begin(9600);
  Serial1.begin(9600);//debug port

  pinMode(LAMP, OUTPUT);
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);  
  
  digitalWrite(LAMP, HIGH);  
}

unsigned int countByte  =  0;
void loop() {
  while (Serial.available()) {
    char inByte = Serial.read();
    Serial1.write(inByte); 
    
    switch(countByte % 4){
      case 0://LAMP
        digitalWrite(LAMP,  (inByte == 0x00 ? HIGH:LOW));//HIGH => OFF
      break;
      case 1://R
        analogWrite(REDPIN,  inByte);
      break;
      case 2://G
        analogWrite(GREENPIN,  inByte);
      break;
      case 3://B
        analogWrite(BLUEPIN,  inByte);
      break;
    }
    
    countByte++;
  }
  
  
  

}
