// Control for HP 6602 ink jet print head
// Details on: https://homofaciens.de/technics-machines-printhead-hp6602_en.htm

#define LED_1       13
#define SWITCH_01   A0

#define NOZZLE_01     2
#define NOZZLE_02     3
#define NOZZLE_03     4
#define NOZZLE_04     5
#define NOZZLE_05     6
#define NOZZLE_06     7
#define NOZZLE_07     8
#define NOZZLE_08     9
#define NOZZLE_09    10
#define NOZZLE_10    11
#define NOZZLE_11    12
#define NOZZLE_12    A1

#define DIR_X        A2
#define STEP_X       A3
#define DIR_Y        A4
#define STEP_Y       A5

#define STEP_PAUSE       1000
#define COMMAND_PAUSE       5
#define DOT_PAUSE_LONG   1000
#define DOT_PAUSE_SHORT     5

int i;
char comByte = 0;
char dataMode = 0;
long dotWidth = 11;
long lineHeight = 234;
long stepsDone = 0;

int nozzles[] = {NOZZLE_06, NOZZLE_07, NOZZLE_08, NOZZLE_09, NOZZLE_12, NOZZLE_11, NOZZLE_10, NOZZLE_03, NOZZLE_02, NOZZLE_01, NOZZLE_04, NOZZLE_05};


// Initialize communication through USB
void establishContact() {
  while (Serial.available() <= 0) {
    Serial.print('X');   // send a capital X
    delay(100);
  }
}


void motorStep(long stepWidth, int dirPin, int stepPin, long stepPause){
  if(stepWidth < 0){
    digitalWrite(dirPin, HIGH);
    stepWidth = -stepWidth;
  }
  else{
    digitalWrite(dirPin, LOW);
  }
  for(long i=0; i<stepWidth; i++){
    digitalWrite(stepPin, HIGH);
    delayMicroseconds(COMMAND_PAUSE);
    digitalWrite(stepPin, LOW);
    delayMicroseconds(stepPause);
  }
}

void makeDot(int nozzle, long dotPause){
  int i = 0;
  digitalWrite(nozzle, HIGH);
  delayMicroseconds(1);
  //for(i=0; i<1; i++);
  digitalWrite(nozzle, LOW);
  delayMicroseconds(dotPause);
}

// the setup routine runs once when you press reset:
void setup(){
  dataMode = 0;
  stepsDone = 0;

  pinMode(LED_1, OUTPUT);
  pinMode(SWITCH_01, INPUT);
  digitalWrite(SWITCH_01, HIGH); // Activate pull-up resistor

  pinMode(DIR_X, OUTPUT);
  pinMode(STEP_X, OUTPUT);
  pinMode(DIR_Y, OUTPUT);
  pinMode(STEP_Y, OUTPUT);

  for(i=0; i<12; i++){
    pinMode(nozzles[i], OUTPUT);
  }

  motorStep(100, DIR_X, STEP_X, STEP_PAUSE);
  motorStep(-100, DIR_X, STEP_X, STEP_PAUSE);
  motorStep(100, DIR_Y, STEP_Y, STEP_PAUSE);
  motorStep(-100, DIR_Y, STEP_Y, STEP_PAUSE);

  // start serial port at 115200 bps:
  Serial.begin(115200);
  establishContact();
}

// the loop routine runs over and over again forever:
void loop() {

  comByte = 0;
  if (Serial.available() > 0){//if valid, read from serial:
    //get incoming byte:
    comByte = Serial.read();
    Serial.print('R');   // send 'R' to initiate next data from computer

    if(dataMode == 0){

      if(comByte == 'd'){
        dataMode = 1;
      }

      if(comByte == 'r'){// Return line
        motorStep(-stepsDone, DIR_X, STEP_X, STEP_PAUSE/10);
        stepsDone = 0;
      }
      if(comByte == 'n'){// New line
        motorStep(-lineHeight, DIR_Y, STEP_Y, STEP_PAUSE/10);
        //motorStep(-dotWidth, DIR_X, STEP_X, STEP_PAUSE/10);//only why printhead wasn't othogonal
        stepsDone = 0;
      }
      if(comByte == 'N'){// Forward paper for one dot
        motorStep(-lineHeight/12, DIR_Y, STEP_Y, STEP_PAUSE/10);
        stepsDone = 0;
      }


      if(comByte == 'T'){// Test nozzles
        for(int i = 0; i < 12; i++){
          makeDot(nozzles[i], DOT_PAUSE_LONG);
          delay(2000);
        }
      }

      if(comByte == 't'){// Test nozzles
        for(int i = 0; i < 12; i++){
          for(int j = 0; j < 100; j++){
            makeDot(nozzles[i], DOT_PAUSE_LONG);
            motorStep(dotWidth, DIR_X, STEP_X, DOT_PAUSE_LONG/dotWidth + 10);
          }
        }
        motorStep(-lineHeight, DIR_Y, STEP_Y, STEP_PAUSE/4);
        for(int i = 0; i < 12; i++){
          for(int j = 0; j < 100; j++){
            makeDot(nozzles[i], DOT_PAUSE_LONG);
            motorStep(-dotWidth, DIR_X, STEP_X, DOT_PAUSE_LONG/dotWidth + 10);
          }
        }
      }

      if(comByte == 'x'){
        motorStep(100, DIR_X, STEP_X, STEP_PAUSE);
      }
      if(comByte == 'X'){
        motorStep(-100, DIR_X, STEP_X, STEP_PAUSE);
      }
      if(comByte == 'y'){
        motorStep(100, DIR_Y, STEP_Y, STEP_PAUSE);
      }
      if(comByte == 'Y'){
        motorStep(-100, DIR_Y, STEP_Y, STEP_PAUSE);
      }
    }
    else{
      if(dataMode == 1){
        for(int i = 0; i < 8; i++){
          if(comByte & (1<<i)){
            makeDot(nozzles[i], DOT_PAUSE_SHORT);
          }
        }
      }
      if(dataMode == 2){
        for(int i = 0; i < 4; i++){
          if(comByte & (1<<i)){
            makeDot(nozzles[i+8], DOT_PAUSE_SHORT);
          }
        }
      }
      dataMode++;
      if(dataMode == 3){
        motorStep(dotWidth, DIR_X, STEP_X, DOT_PAUSE_LONG/dotWidth + 10);
        stepsDone+=dotWidth;
        dataMode = 0;
      }
    }

  }//if (Serial.available() > 0){






}
