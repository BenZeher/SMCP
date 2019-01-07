/**
 * This Class tests all wiring of the PI and outputs the terminal number in binary to the output LEDs.
 * This class can be run on the PI to confirm all wires are connected and determine the terminal number in the web app.
 * **/
 
package SecuritySystem;

//import pi4java.GpioPinDigitalStateChangeEvent;
//import pi4java.GpioPinListenerDigital;
//import pi4java.PinState;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;



public class WiringTest {
	
	static boolean bUsePiGpioPins = true;
	
    public static void main(String args[]) throws InterruptedException {
        System.out.println("GPIO Wiring Test started...");
       
        System.out.println("Provisioning Pins...");
        final gpioHandler gpio = new gpioHandler(bUsePiGpioPins);
        try {
			gpio.provisionTerminals(bUsePiGpioPins);
		} catch (Exception e) {
			System.out.println("Could not provision pins. - " + e.getMessage());
			System.exit(1);
		}
         
        if(gpio.getProvisionedGpioOutputArray().length < 5){
        	System.out.println("Wiring Test needs 5 or more outputs provisioned to run");
        	System.exit(1);
        }
		
		System.out.println("Check to make sure all output LEDs are ON..");

		for(int i = 0; i < gpio.getProvisionedGpioOutputArray().length; i++){
			gpio.getProvisionedGpioOutputArray()[i].pulse(5000, PinState.LOW);
		}
		
		Thread.sleep(5080);
		
		System.out.println("Adding state change listener to pins...");
		for(int i = 0; i < gpio.getProvisionedGpioInputArray().length; i++){
			gpio.getProvisionedGpioInputArray()[i].addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                int iPinNumber = -1;
            	try{
                	iPinNumber = Integer.parseInt(event.getPin().getName());
                }catch (Exception e){
                	System.out.println("Could not parse pin number" + event.getPin().getName() + " - " + e.getMessage());
                }
                if (event.getState() == PinState.LOW){
                	switch(iPinNumber){
                	case 0:  displayBinaryValue(gpio, 0,0,0,0,0); break;
                	case 1:  displayBinaryValue(gpio, 0,0,0,0,1); break;
                	case 2:  displayBinaryValue(gpio, 0,0,0,1,0); break;
                	case 3:  displayBinaryValue(gpio, 0,0,0,1,1); break;
                	case 4:  displayBinaryValue(gpio, 0,0,1,0,0); break;
                	case 5:  displayBinaryValue(gpio, 0,0,1,0,1); break;
                	case 6:  displayBinaryValue(gpio, 0,0,1,1,0); break;
                	case 7:  displayBinaryValue(gpio, 0,0,1,1,1); break;
                	case 8:  displayBinaryValue(gpio, 0,1,0,0,0); break;
                	case 9:  displayBinaryValue(gpio, 0,1,0,0,1); break;
                	case 10: displayBinaryValue(gpio, 0,1,0,1,0); break;
                	case 11: displayBinaryValue(gpio, 0,1,0,1,1); break;
                	case 12: displayBinaryValue(gpio, 0,1,1,0,0); break;
                	case 13: displayBinaryValue(gpio, 0,1,1,0,1); break;
                	case 14: displayBinaryValue(gpio, 0,1,1,1,0); break;
                	case 15: displayBinaryValue(gpio, 0,1,1,1,1); break;
                	case 16: displayBinaryValue(gpio, 1,0,0,0,0); break;
                	case 17: displayBinaryValue(gpio, 1,0,0,0,1); break;
                	case 18: displayBinaryValue(gpio, 1,0,0,1,0); break;
                	case 19: displayBinaryValue(gpio, 1,0,0,1,1); break;
                	case 20: displayBinaryValue(gpio, 1,0,1,0,0); break;
                 	case 21: displayBinaryValue(gpio, 1,0,1,0,1); break;
                 	case 22: displayBinaryValue(gpio, 1,0,1,1,0); break;
                 	case 23: displayBinaryValue(gpio, 1,0,1,1,1); break;
                 	case 24: displayBinaryValue(gpio, 1,1,0,0,0); break;
                 	case 25: displayBinaryValue(gpio, 1,1,0,0,1); break;
                 	case 26: displayBinaryValue(gpio, 1,1,0,1,0); break;
                 	case 27: displayBinaryValue(gpio, 1,1,0,1,1); break;
                 	case 28: displayBinaryValue(gpio, 1,1,1,0,0); break;
                 	case 29: displayBinaryValue(gpio, 1,1,1,0,1); break;
            		default: displayBinaryValue(gpio, 1,1,1,1,0); break;
                
                	}
                
                }
                
            }
            
        });
		}
		
		System.out.println("Closing each terminal input will display the binary terminal value on the output LEDs.\n");
		
        // keep program running until user aborts (CTRL-C)
        for (;;) {

            Thread.sleep(500);
        }
        
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller        
    }
    
    static void displayBinaryValue(gpioHandler gpio, 
    		int sixteen, 
    		int eight, 
    		int four, 
    		int two, 
    		int one
    		){
    	int iOutputArrayLenth = gpio.getProvisionedGpioOutputArray().length - 1;
    	if(one == 1){
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth].low();
    	}else{
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth].high();
    	}
    	if(two == 1){
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 1].low();
    	}else{
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 1].high();
    	}
    	if(four == 1){
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 2].low();
    	}else{
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 2].high();
    	}
    	if(eight == 1){
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 3].low();
    	}else{
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 3].high();
    	}
    	if(sixteen == 1){
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 4].low();
    	}else{
    		gpio.getProvisionedGpioOutputArray()[iOutputArrayLenth - 4].high();
    	}

    }
}