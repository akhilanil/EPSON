#include <SoftwareSerial.h>
#include <IRremote.h>

#define DEBUG true
#define privateKey 1019
#define publicKey 3337

#define EPSON 1
//SoftwareSerial esp8266(TX,RX);
SoftwareSerial esp8266(4,5);

int RC4_LENGTH = 15;
char ipd[] = "+IPD,";
char mode[] = "mode=";

char RC4key[10]; //final RC4 key
char authenticationKey[10];//temp key used in  mode 4
int in, ain;
bool isRC4Initialised;

//String RC4Key;
int prga[15], s[15];
IRsend irsend;

void swap(int *a, int *b) {

  int temp;

  temp = *a;
  *a = *b;
  *b = temp;


}



void initRC4()
{
    for(int i = 0; i < RC4_LENGTH; i++)
      s[i] = i;

}

void doKSA()
{
    int i,j;
    i = j = 0;
    for(i = 0; i < RC4_LENGTH; i++)
    {
        j = (j + s[i] + RC4key[i % strlen(RC4key)]) % RC4_LENGTH;
        swap(&s[i], &s[j]);
    }
}

void doPGRA()
{

    int i,j,k;
    i = j = k = 0;

    while(k < RC4_LENGTH)
    {
        i = (i + 1 ) % RC4_LENGTH;

        j = (j + s[i]) % RC4_LENGTH;

        swap(&s[i], &s[j]);

        prga[k] = s[(s[i] + s[j]) % RC4_LENGTH];

        k++;


    }

}

void setup() {

    Serial.begin(9600);
    esp8266.begin(9600);

    sendCommand("AT+RST\r\n",2000,DEBUG); // reset module
    sendCommand("AT+CWMODE=3\r\n",1000,DEBUG); // configure as access point
    sendCommand("AT+CWJAP=\"projectfi\",\"akhila123\"\r\n",3000,DEBUG);

    delay(10000);

    sendCommand("AT+CIFSR\r\n",1000,DEBUG); // get ip address
    sendCommand("AT+CIPMUX=1\r\n",1000,DEBUG); // configure for multiple connections
    sendCommand("AT+CIPSERVER=1,80\r\n",1000,DEBUG); // turn on server on port 80

    ain = 0;
    isRC4Initialised = false;

    pinMode(13,OUTPUT);
    digitalWrite(13,HIGH);
    delay(1500);
    digitalWrite(13,LOW);
    Serial.println("SERVER READY");
}

void loop()
{
    if(esp8266.available())
    {
        if(esp8266.find(ipd))
        {
            // check if the esp is sending a message

            /* wait for the serial buffer to fill up (read all the serial data)*/
            delay(2000);

            /*
             * get the connection id so that we can then disconnect
             * subtract 48 because the read() function returns
             * the ASCII decimal value and 0 (the first decimal number) starts at 48
             */
            int connectionId = esp8266.read()-48;

            /*Advance cursor to "mode="*/
            esp8266.find(mode);

            /*Mode defines the type of http request*/
            int mode = (esp8266).read()-48;

            /*
             * requestResponse holds the data to be send
             * Initially requestResponse contains 9 which is the error code
            */
            String requestResponse = "9";

            switch(mode)
            {
                case 0:
                      /*
                      * 1 indicates Successfull connection
                      */
                      requestResponse = "1";
                      sendHTTPResponse(connectionId,requestResponse);
                      Serial.println("Initial Connection");
                      break;

                case 1:

                      int encryptedKey[10];
                      int a;
                      int i;
                      /*Moving the pointer 7 times such that it reaches '&value=' */
                      for(i = 0; i<7; i++) {(esp8266).read();}

                      i = 0;
                      ain = 0;

                      a = (esp8266).read();
                      while(a!=32)
                      {
                          if(a!=63)
                          {
                            encryptedKey[i] = a - 48;
                            i++;
                          }
                          else
                          {
                            decryptKey(encryptedKey, i-1);
                            i = 0;
                          }
                          a = (esp8266).read();
                      }
                      authenticationKey[ain] = '\0';
                      Serial.print("AUTH KEY:");
                      Serial.println(authenticationKey);

                      if(isRC4Initialised)
                      {
                          if(strcmp(authenticationKey, RC4key) != 0)
                          {
                              requestResponse = "8"; //indicates Authentication faliure
                          }
                          else
                          {
                              requestResponse = "2"; //inidicates Successfull Authentication
                          }
                      }
                      else
                      {
                          strcpy(RC4key, authenticationKey);
                          in = ain;
                          isRC4Initialised = !isRC4Initialised;
                          Serial.print("AUTH KEY:");
                          Serial.println(RC4key);
                          initRC4();
                          doKSA();
                          doPGRA();
                          requestResponse = "2"; //inidicates Successfull Authentication
                      }

                      sendHTTPResponse(connectionId,requestResponse);
                      break;

                case 2:

                      int index;
                      index = 0;
                      int num[10],code[10];
                      char encryptedCode[10];
                      unsigned long int decryptedCode;

                      /*Moving the pointer 7 times such that it reaches '&value=' */
                      for(i = 0; i<7; i++) {(esp8266).read();}

                      a = (esp8266).read();
                      while(a != 32)
                      {
                          encryptedCode[index] = a;
                          code[index] = a;
                          index++;
                          a = (esp8266).read();
                      }
                      encryptedCode[index] = '\0';
                      Serial.print("Encrypted Code:\n");
                      Serial.println(encryptedCode);

                      /*Decrypting using RC4*/

                      for(i = 0; i < index; i++)
                      {
                          code[i] = code[i] ^ prga[i];
                          code[i] = code[i] - 48;

                      }
                      Serial.print("Decrypted Code:\n");
                      printArray(code, index);
                      Serial.print("\n");
                      decryptedCode = getRemoteCode(code, i);
                      Serial.print("Decrypted Code:\n");
                      Serial.println(decryptedCode);
                      sendDataToDevice(decryptedCode, EPSON);

                      requestResponse = "1";//Successfull Connection
                      sendHTTPResponse(connectionId,requestResponse);
                      break;

                /*Inidacates disconnection*/
                case 9:
                      Serial.println("Disconnected");
                      isRC4Initialised = false;
                      in = 0;
                      ain = 0;
                      strcpy(RC4key, "\0");
                      strcpy(authenticationKey, "\0");

                      requestResponse = "1";
                      sendHTTPResponse(connectionId,requestResponse);
                      break;

            }
        }
    }
}

/*
* Name: initialiseKey
* Description: Function that sets RC4 key to null initially
*/

void initialiseKey()
{
    for(int i = 0; i<10; i++)
    {
        RC4key[i] = '\0';
    }
}

void printArray(int val[], int n )
{
    Serial.print("\n");
    for(int i = 0; i < n; i++)
    {
      Serial.print(val[i]);
      Serial.print("  ");
    }
    Serial.print("\n");
}


/*
* Name: sendDataToDevice
* Description: Function that returns SENDS Remote code to IR LED
* Parameters: deviceCode: Holds the RemoteCode
*             deviceName: Specifies The name of the device
*/



String sendDataToDevice(unsigned long int deviceCode, int deviceName)
{

    String response = "9";
    switch(deviceName)
    {
        case 1:

              for (int i = 0; i < 2; i++)
              {
                  irsend.sendNEC(deviceCode, 32); // Sony TV power code
                  delay(40);
              }
              delay(1000);
              response = "1";
              break;

        default:
                response = "9";


    }

    return response;


}



/*
* Name: getRemoteCode
* Description: Function that returns Remote code from array
* Parameters: cipher: Array which contains the cipher text
*             index: Variable which holds the size of cipher
*/

unsigned long int getRemoteCode(int num[], int index)
{
    unsigned long int remoteCode = 0, temp;
    int i = index - 1, j = 0;

    /*This while loop combines the digits cipher in array to form the required Code */
    while(i >= 0)
    {
        temp = num[j];
        remoteCode = remoteCode + (temp * findPower(10, i));
        j++;
        i--;
    }

    return remoteCode;
}

/*
* Name: decryptKey
* Description: Function that decrypts the RC4 key using RSA
* Parameters: cipher: Array which contains the cipher text
*             index: Variable which holds the size of cipher
*/

void decryptKey(int cipher[], int index)
{

    int i = index,j = 0;
    int cipherText = 0;
    int mod, power, num;
    unsigned long temp = 0, val;
    /*This while loop combines the digits cipher in array to form a number*/
    while(i >= 0)
    {
        cipherText = cipherText + (cipher[j] * findPower(10, i));
        j++;
        i--;
    }
    val = num = cipherText;

    power = privateKey;
    mod = publicKey;

    for(i = 1; i<power; i++)
    {
        temp = num * val;
        val = temp % mod;
    }

    authenticationKey[ain] = val;
    ain++;
    
  
}

/*
* Name: findPower
* Description: Utility Function to find power  
* Parameters: base: base value
*             expo: exponent value
*/
long int findPower(int base, int expo) 
{
    int i;
    long int val = 1;
    for(i = 0; i<expo; i++) 
    {
        val = val * base;
    }
    return val;
}



/*
* Name: sendHTTPResponse
* Description: Function that sends HTTP 200, HTML UTF-8 response
*/
void sendHTTPResponse(int connectionId, String content)
{

    String httpResponse;
    String httpHeader;
    httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
    httpHeader += "Content-Length: ";
    httpHeader += content.length();
    httpHeader += "\r\n";
    httpHeader +="Connection: close\r\n\r\n";
    httpResponse = httpHeader + content + " ";
    sendCIPData(connectionId,httpResponse);

}

/*
* Name: sendCIPDATA
* Description: sends a CIPSEND=<connectionId>,<data> command
*
*/
void sendCIPData(int connectionId, String data)
{
    String cipSend = "AT+CIPSEND=";
    cipSend += connectionId;
    cipSend += ",";
    cipSend +=data.length();
    cipSend +="\r\n";
    sendCommand(cipSend,1000,DEBUG);
    sendData(data,1000,DEBUG);
}

/*
* Name: sendCommand
* Description: Function used to send data to ESP8266.
* Params: command - the data/command to send; timeout - the time to wait for a response; debug - print to Serial window?(true = yes, false = no)
* Returns: The response from the esp8266 (if there is a reponse)
*/
String sendCommand(String command, const int timeout, boolean debug)
{

    String response = "";

    /* send the read character to the esp8266 */
    esp8266.print(command);
    
    long int time = millis();
    
    while( (time+timeout) > millis()) 
    {
        while(esp8266.available())
        {
            char c = esp8266.read(); 
            response+=c;     
        }
    }

    return response;
  
}


/*
* Name: sendData
* Description: Function used to send data to ESP8266.
* Params: command - the data/command to send; timeout - the time to wait for a response; debug - print to Serial window?(true = yes, false = no)
* Returns: The response from the esp8266 (if there is a reponse)
*/
String sendData(String command, const int timeout, boolean debug)
{
    String response = "";
    
    int dataSize = command.length();
    char data[dataSize];
    command.toCharArray(data,dataSize);
           
    esp8266.write(data,dataSize); // send the read character to the esp8266
    if(debug)
    {
        Serial.println("\r\n====== HTTP Response From Arduino ======");
        Serial.write(data,dataSize);
        Serial.println("\r\n========================================");
    }
    
    long int time = millis();
    
    while( (time+timeout) > millis())
    {
        while(esp8266.available())
        {
        
            // The esp has data so display its output to the serial window 
            char c = esp8266.read(); // read the next character.
            response+=c;
        }  
    }
    
    if(debug)
    {
        Serial.print(response);
    }
    
    return response;
}     
