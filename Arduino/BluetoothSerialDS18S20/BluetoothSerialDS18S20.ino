/*
Linux Day 2013 Bari
OpenHardware: il futuro di Arduino + Arduino incontra Android 
http://ld13bari.github.io/

Author: Alessandro Ruggieri - Miki Nacucchi
Description: 
Rilevamento della temperatura mediante sensore digitale Maxim DS18B20, connesso con protocollo 1-Wire.
La temperatura rilevazta viene trasmessa sulla porta Seriale, a cui è collegato un modulo CSR HC-06 Bluetooth-UART.
L'app Android, effettuato il paring e la connessione al modulo bluetooth, riceve la temperatura secondo lo standard SPP.

basato su: http://playground.arduino.cc/Learning/OneWire

TODO 
comunicazione della distanza rilevata ad app Android, tramite SoftModem
https://code.google.com/p/arms22/downloads/detail?name=SoftModem-005.zip
*/

#include <OneWire.h>

#define DS_PIN    8
#define LED_PIN  13

OneWire ds(DS_PIN); 
int HighByte, LowByte, TReading, SignBit, Tc_100, Whole, Fract;

void setup(void) {
  Serial.begin(9600);
  pinMode(LED_PIN, OUTPUT);
}

void loop(void) {
  byte present = 0;
  byte data[12];
  byte addr[8];

//////////////// SENSOR TEST ////////////////////////////////////////
//  if ( !ds.search(addr)) {
//     // Serial.print("No more addresses.\n");
//      ds.reset_search();
//      return;
//  }
//
//
//  if ( OneWire::crc8( addr, 7) != addr[7]) {
//      Serial.print("CRC is not valid!\n");
//      return;
//  }
//
//  if ( addr[0] == 0x10) {
//      //Serial.print("Device is a DS18S20 family device.\n");
//  }
//  else if ( addr[0] == 0x28) {
//      //Serial.print("Device is a DS18B20 family device.\n");
//  }
//  else {
//      Serial.print("Device family is not recognized: 0x");
//      Serial.println(addr[0],HEX);
//      return;
//  }
//////////////// SENSOR TEST END ////////////////////////////////////////

  ds.reset();
  ds.select(addr);
  ds.write(0x44,1);         // start conversion, with parasite power on at the end

  delay(1000);    
  
  present = ds.reset();
  ds.select(addr);    
  ds.write(0xBE);         // Read Scratchpad

 /* Serial.print("P=");
  Serial.print(present,HEX);
  Serial.print(" ");*/
  int i  =  0;
  for ( i = 0; i < 9; i++) {           // we need 9 bytes
    data[i] = ds.read();
    /*Serial.print(data[i], HEX);
    Serial.print(" ");*/
  }
 /* Serial.print(" CRC=");
  Serial.print( OneWire::crc8( data, 8), HEX);
  Serial.println();*/
    
  LowByte = data[0];
  HighByte = data[1];
  TReading = (HighByte << 8) + LowByte;
  SignBit = TReading & 0x8000;  // test most sig bit
  if (SignBit) // negative
  {
    TReading = (TReading ^ 0xffff) + 1; // 2's comp
  }
  Tc_100 = (6 * TReading) + TReading / 4;    // multiply by (100 * 0.0625) or 6.25

  Whole = Tc_100 / 100;  // separate off the whole and fractional portions
  Fract = Tc_100 % 100;


  if (SignBit) // If its negative
  {
     Serial.print("-");
  }
  Serial.print(Whole);
  Serial.print(".");
  
  if (Fract < 10)
  {
     Serial.print("0");     
  }
  Serial.println(Fract);
}
