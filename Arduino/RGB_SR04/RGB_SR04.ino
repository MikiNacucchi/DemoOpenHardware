/*
Linux Day 2013 Bari
OpenHardware: il futuro di Arduino + Arduino incontra Android 
http://ld13bari.github.io/

Author: Alessandro Ruggieri - Miki Nacucchi
Description: Rilevazione della distanza attraverso il sensore HC-SR04, segnalazione della distanza inferiore alla soglia di alert con Led rosso, altrimenti verde


*/
 
#define PIN_RED 10
#define PIN_GREEN 11
#define PIN_BLUE 12
#define PIN_TRIGGER 7
#define PIN_ECHO 8
#define ALERT_ZONE 50 //50cm

long duration, distance;
 
void setup()
{
  Serial.begin(9600);
  pinMode(PIN_RED, OUTPUT);
  pinMode(PIN_GREEN, OUTPUT);
  pinMode(PIN_BLUE, OUTPUT);  
  pinMode( PIN_TRIGGER, OUTPUT );
  pinMode( PIN_ECHO, INPUT );
}
 
void loop()
{
  digitalWrite(PIN_TRIGGER, LOW ); 
  delayMicroseconds(10);
  digitalWrite(PIN_TRIGGER, HIGH ); //Send Ultra-Sound
  delayMicroseconds(10);
  digitalWrite(PIN_TRIGGER, LOW );
  
  duration = pulseIn(PIN_ECHO, HIGH ); //Wait return signal
  
  distance = duration/58;  // uS to cm
  
  if(distance < ALERT_ZONE){
    distance = 0; // => RED 
  }
  
  if( distance>=0 && distance < 400 ) {
    setColor(map(distance,0,400,255,0), map(distance,0,400,0,255), 0);
  }
    
  delay(200); //loop delay
}

void setColor(int red, int green, int blue)
{
  analogWrite(PIN_RED, red);
  analogWrite(PIN_GREEN, green);
  analogWrite(PIN_BLUE, blue);  
}
