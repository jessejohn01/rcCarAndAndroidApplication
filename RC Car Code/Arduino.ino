/*
  Arduino Turn LED On/Off using Serial Commands
  Created April 22, 2015
  Hammad Tariq, Incubator (Pakistan)

  It's a simple sketch which waits for a character on serial
  and in case of a desirable character, it turns an LED on/off.

  Possible string values:
  a (to turn the LED on)
  b (tor turn the LED off)
*/

#include <Servo.h>
//#include <String.h>
Servo motor;
Servo steering;
String data;
String motorValue;
String steeringValue;
int mV;
int sV;
int commaIndex;

String inputString = "";
int value = 0;
int steeringMax = 165;
int steeringMin = 95;
void setup()                    // run once, when the sketch starts
{

  motor.attach(3);
  delay(1);
  steering.attach(9);
  motor.write(80);
  delay(2000);
  Serial.begin(9600);            // set the baud rate to 9600, same should be of your Serial Monitor
  pinMode(2, OUTPUT);
  //pinMode(3, OUTPUT);
  steering.write(125);
}

void loop()
{

    while (Serial.available() > 0)
    {
        char recieved = Serial.read();
        data += recieved; 

        // Process message when new line character is recieved
        if (recieved == '\n')
        {
            Serial.print("Arduino Received: ");
            Serial.print(data);

            commaIndex = data.indexOf('.');
            motorValue = data.substring(0,commaIndex);
            steeringValue = data.substring(commaIndex + 1);

            mV = motorValue.toInt();
            sV = steeringValue.toInt();
            Serial.println(sV);
            Serial.println(mV);
            if(mV <=1510 && mV >= 1350){
               motor.write(mV);
            }else if(mV >= 1240 && mV <=1340){
                 motor.write(mV);
            }
            if(sV < steeringMax && sV >= steeringMin){
              steering.write(sV);
            }
            data = ""; // Clear recieved buffer
            motorValue = "";
            steeringValue = "";
            Serial.flush();


        }
    }
//delay(8);



  
}
