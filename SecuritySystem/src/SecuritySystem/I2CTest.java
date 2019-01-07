package SecuritySystem;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  MCP23017GpioExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.io.IOException;

import pi4java.GpioController;
import pi4java.GpioFactory;
import pi4java.GpioPinDigitalInput;
import pi4java.GpioPinDigitalOutput;
import pi4java.GpioPinDigitalStateChangeEvent;
import pi4java.GpioPinListenerDigital;
import pi4java.I2CBus;
import pi4java.MCP23017GpioProvider;
import pi4java.MCP23017Pin;
import pi4java.PinPullResistance;
import pi4java.PinState;
import sscommon.SSConstants;

//import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
//import com.pi4j.gpio.extension.mcp.MCP23017Pin;
//import com.pi4j.io.gpio.GpioController;
//import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinDigitalInput;
//import com.pi4j.io.gpio.GpioPinDigitalOutput;
//import com.pi4j.io.gpio.PinPullResistance;
//import com.pi4j.io.gpio.PinState;
//import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
//import com.pi4j.io.gpio.event.GpioPinListenerDigital;
//import com.pi4j.io.i2c.I2CBus;

/**
 * <p>
 * This example code demonstrates how to setup a custom GpioProvider
 * for GPIO pin state control and monitoring.
 * </p>  
 * 
 * <p>
 * This example implements the MCP23017 GPIO expansion board.
 * More information about the board can be found here: *
 * http://ww1.microchip.com/downloads/en/DeviceDoc/21952b.pdf
 * </p>
 * 
 * <p>
 * The MCP23017 is connected via I2C connection to the Raspberry Pi and provides
 * 16 GPIO pins that can be used for either digital input or digital output pins.
 * </p>
 * 
 * @author Robert Savage
 */
public class I2CTest {
    
    public static void main(String args[]) throws InterruptedException, IOException {
        
        System.out.println("<--Pi4J--> MCP23017 GPIO Example ... started.");
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        
        // create custom MCP23017 GPIO provider
        final MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);
        final MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21);
        final MCP23017GpioProvider gpioProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x22);
        final MCP23017GpioProvider gpioProvider3 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x23);
        
        // provision gpio input pins from MCP23017
        GpioPinDigitalInput myInputs[] = {
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A0, "1", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A1, "2", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A2, "3", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A3, "4", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A4, "5", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A5, "6", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A6, "7", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_A7, "8", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B0, "9", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B1, "10", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B2, "11", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B3, "12", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B4, "13", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B5, "14", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B6, "15", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider0, MCP23017Pin.GPIO_B7, "16", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A0, "17", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A1, "18", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A2, "19", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A3, "20", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A4, "21", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A5, "22", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A6, "23", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_A7, "24", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B0, "25", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B1, "26", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B2, "27", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B3, "28", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B4, "29", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B5, "30", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B6, "31", PinPullResistance.PULL_UP),
                gpio.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B7, "32", PinPullResistance.PULL_UP)
            };
        
        // create and register gpio pin listener
        gpio.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                        + event.getState());
            }
        }, myInputs);
       
        myInputs[1].setDebounce(SSConstants.INPUT_DEBOUNCE_TIME);
        
        // provision gpio output pins and make sure they are all LOW at startup
        GpioPinDigitalOutput myOutputs[] = {
        	gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A0, "33", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A1, "34", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A2, "35", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A3, "36", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A4, "37", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A5, "38", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A6, "39", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A7, "40", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B0, "41", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B1, "42", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B2, "43", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B3, "44", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B4, "45", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B5, "46", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B6, "47", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B7, "48", PinState.HIGH),
        	gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A0, "49", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A1, "50", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A2, "51", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A3, "52", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A4, "53", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A5, "54", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A6, "55", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A7, "56", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B0, "57", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B1, "58", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B2, "59", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B3, "60", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B4, "61", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B5, "62", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B6, "63", PinState.HIGH),
            gpio.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B7, "64", PinState.HIGH)
          };
        
        // keep program running for 20 seconds
        while (true) {
            gpio.setState(true, myOutputs);
            Thread.sleep(5000);
           gpio.setState(false, myOutputs);
            Thread.sleep(5000);
        }
        
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        //gpio.shutdown();                 
    }
}