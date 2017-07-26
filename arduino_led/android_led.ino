#include <SPI.h>
#include <Ethernet.h>
int redPin = 3;
int greenPin = 5;
int bluePin = 6;


//byte mac[] = {0x54, 0x26, 0x96, 0xD8, 0xE0, 0xBD};
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
IPAddress ip(192, 168, 1, 177); // Arduino IP Add
EthernetServer server(80); // Web server

// Http data
String reqData; // Request from Smartphone
String header;
int contentSize = -1;
String CONTENT_LENGTH_TXT = "Content-Length: ";

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(redPin, OUTPUT);
  pinMode(bluePin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  Serial.print("Ready...");
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Ethernet.begin(mac, ip);  
  server.begin();  
  Serial.println("Begin...");
//  digitalWrite(4, HIGH);
}

void loop() {
  Serial.println("Client...");
  EthernetClient client = server.available(); // Is there a client (Our Android smartphone) 
  if (client) {
    Serial.println("Client ready..");
    // Let's start reading
    boolean isLastLine = true;
    boolean isBody = false;
    header = "";
    reqData = "";
    int contentLen = 0;
    
    Serial.println("Client connected!");
    while (client.connected()) {
            if (client.available()) { 
              // Read data
              char c = client.read(); 
             
               Serial.print(c);
               
              if (contentSize == contentLen) {
               // Serial.println("Body ["+reqData+"]");

                  Serial.println("Body ["+reqData+"]");
                         // Extract the JSON string like [r,g,b]
                         int pos1 = reqData.indexOf("[");
                         int pos2 = reqData.lastIndexOf("]");
                   
                       // Parse the string looking for ,
                       String colors = reqData.substring(pos1 + 1, pos2);
                       Serial.println("Colors ["+colors+"]");
                       int idx1 = colors.indexOf(',');
                       int idx2 = colors.indexOf(',', idx1+1);
                       int idx3 = colors.indexOf(',', idx2+1);
                       String sRed = colors.substring(0, idx1);
                       String sGreen = colors.substring(idx1 + 1, idx2);
                       String sBlue = colors.substring(idx2 + 1, idx3);
                   
                       // Convert the Red, Green and Blue string values to int
                      int red = sRed.toInt();
                      int green = sGreen.toInt();
                      int blue = sBlue.toInt();
                   
                     // Set the RGB led color according to the values sent by the Android client
                     setColor(red, green,blue);
                   
                     // Create the response to client
                     client.println("HTTP/1.1 200 OK");
                     client.println("Content-Type: text/html");
                     client.println("Connection: close");
                     client.println();
                     // send web page
                     client.println("");
                     client.println(""); 
                     delay(1);
                     break; 
              }
              
              
              if (c == '\n' && isLastLine) {
                  isBody = true;
                  int pos = header.indexOf(CONTENT_LENGTH_TXT);
                  String tmp = header.substring(pos, header.length());
                  //Serial.println("Tmp ["+tmp+"]");
                  int pos1 = tmp.indexOf("\r\n");
                  String size = tmp.substring(CONTENT_LENGTH_TXT.length(), pos1);
                  Serial.println("Size ["+size+"]");
                  contentSize = size.toInt();
                  
              }
              
              if (isBody) {
                reqData += c;
                contentLen++;
              }
              else {
               header += c; 
              }
             
              
              if (c == '\n' ) {
               isLastLine = true;
              }
              else if (c != '\r' ) {
                isLastLine = false;
              }
              
             
              
            }
    }
    delay(1);   
    // Close connection
//    client.println("The chosed color is:" + reqData);
    Serial.println("Stop..");
    client.stop();
  }
  delay(1000);
}

void setColor(int red, int green, int blue) {
  #ifdef COMMON_ANODE
  red = 255 - red;
  green = 255 - green;
  blue = 255 - blue;
  Serial.println(red);
  Serial.println(green);
  Serial.println(blue);
  #endif
 
  analogWrite(redPin, red);
  analogWrite(bluePin, green);
  analogWrite(greenPin, blue);
}

