/*
 * This class configures the GPIO Pins on the Raspberry PI 2 Model B.
 * System Configurations on PI 2:
 * Using the Raspberry Pi Software Configuration Tool (rasp-config) 
 * disable SPI and I2C using these menu options
 *   --> 9 Advanced Options
 *   	--> A6 SPI
 *   		--> Disable
 *   	--> A7 I2C
 *   		--> Disable 
 * Note: Other models may require different pin provisioning and system configuration 
 */
package SecuritySystem;

import java.util.ArrayList;
import java.util.Collection;

import sscommon.SSConstants;








/*
import pi4java.GpioController;
import pi4java.GpioFactory;
import pi4java.GpioPin;
import pi4java.GpioPinDigitalInput;
import pi4java.GpioPinDigitalOutput;
import pi4java.GpioPinDigitalStateChangeEvent;
import pi4java.GpioPinListener;
import pi4java.GpioPinListenerDigital;
import pi4java.I2CBus;
import pi4java.MCP23017GpioProvider;
import pi4java.MCP23017Pin;
import pi4java.Pin;
import pi4java.PinPullResistance;
import pi4java.PinState;
import sscommon.SSConstants;
*/
import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;

public class gpioHandler extends java.lang.Object{
	//create gpio controller to access GPIO Pins
	private static GpioController gpio = null;

	//Gpio providers for I2C devices
	private static MCP23017GpioProvider gpioProvider0 = null;
	private static MCP23017GpioProvider gpioProvider1 = null;
	private static MCP23017GpioProvider gpioProvider2 = null;
	private static MCP23017GpioProvider gpioProvider3 = null;

	private static boolean bFakingGPIOInput = false;

	static GpioPinDigitalInput[] inputSensors = null;
	static GpioPinDigitalOutput[] outputDevices = null;

	//I2C Chip addresses:
	private static int I2C_CHIP_ADDRESS_0x20 = 0x20;
	private static int I2C_CHIP_ADDRESS_0x21 = 0x21;
	private static int I2C_CHIP_ADDRESS_0x22 = 0x22;
	private static int I2C_CHIP_ADDRESS_0x23 = 0x23;
	private static ArrayList<Integer>arrNumberOfPinsPerI2CChip = new ArrayList<Integer>(0);
	public static int NUMBER_OF_PINS_USED_ON_EACH_I2C_CHIP = 16;

	//Pin/terminal provisioning:
	static ArrayList<MCP23017GpioProvider>arrGpioInputProvider = new ArrayList<MCP23017GpioProvider>(0);
	static ArrayList<MCP23017GpioProvider>arrGpioOutputProvider = new ArrayList<MCP23017GpioProvider>(0);
	static ArrayList<Pin>arrInputPins = new ArrayList<Pin>(0);
	static ArrayList<Integer>arrInputChipAddresses = new ArrayList<Integer>(0);
	static ArrayList<String>arrInputTerminalNumbers = new ArrayList<String>(0);
	static ArrayList<PinPullResistance>arrInputPinPullResistances = new ArrayList<PinPullResistance>(0);
	static ArrayList<Pin>arrOutputPins = new ArrayList<Pin>(0);
	static ArrayList<Integer>arrOutputChipAddresses = new ArrayList<Integer>(0);
	static ArrayList<String>arrOutputTerminalNumbers = new ArrayList<String>(0);
	static ArrayList<PinState>arrOutputPinStates = new ArrayList<PinState>(0);

	static ArrayList<Integer>arrTestInputTerminalListeningStatus = new ArrayList<Integer>(0);
	static ArrayList<String>arrTestInputTerminalContactStatus = new ArrayList<String>(0);

	static ArrayList<String>arrTestOutputTerminalContactStatus = new ArrayList<String>(0);
	static ArrayList<Long>arrTestOutputTerminalActivationIntervals = new ArrayList<Long>(0);
	static ArrayList<Long>arrTestOutputTerminalActivationIntervalStartTimes = new ArrayList<Long>(0);


	public gpioHandler(boolean bUsePiGpioPins){
		int iTerminalNumber = 0;
		//Initialize the arrays used to provision pins:
		//Inputs:
		
		//TJR - 5/27/2016 - no longer using GPIO pins at all:
		/*
		if(bUsePiGpioPins){
			arrInputPins.add(RaspiPin.GPIO_00);
			arrInputTerminalNumbers.add("0");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_01);
			arrInputTerminalNumbers.add("1");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_02);
			arrInputTerminalNumbers.add("2");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_03);
			arrInputTerminalNumbers.add("3");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_04);
			arrInputTerminalNumbers.add("4");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_05);
			arrInputTerminalNumbers.add("5");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_06);
			arrInputTerminalNumbers.add("6");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_07);
			arrInputTerminalNumbers.add("7");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//Both pins 8 and 9 (I2C) have none changing internal 1.8K pull up resistors tied to the 3.3V rail
			arrInputPins.add(RaspiPin.GPIO_08);
			arrInputTerminalNumbers.add("8");
			arrInputPinPullResistances.add(null);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_09);
			arrInputTerminalNumbers.add("9");
			arrInputPinPullResistances.add(null);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_10);
			arrInputTerminalNumbers.add("10");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_11);
			arrInputTerminalNumbers.add("11");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_12);
			arrInputTerminalNumbers.add("12");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_13);
			arrInputTerminalNumbers.add("13");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_14);
			arrInputTerminalNumbers.add("14");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_15);
			arrInputTerminalNumbers.add("15");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			arrInputPins.add(RaspiPin.GPIO_16);
			arrInputTerminalNumbers.add("16");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//These pins are not available to us on the Raspberry Pi 2 B:
			//gpio.provisionDigitalInputPin(RaspiPin.GPIO_17, PinPullResistance.PULL_UP),
			//gpio.provisionDigitalInputPin(RaspiPin.GPIO_18, PinPullResistance.PULL_UP),
			//gpio.provisionDigitalInputPin(RaspiPin.GPIO_19, PinPullResistance.PULL_UP),
			//gpio.provisionDigitalInputPin(RaspiPin.GPIO_20, PinPullResistance.PULL_UP),


			arrInputPins.add(RaspiPin.GPIO_21);
			arrInputTerminalNumbers.add("21");
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//******************************************************************************************
			//Outputs:
			arrOutputPins.add(RaspiPin.GPIO_22);
			arrOutputTerminalNumbers.add("22");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_23);
			arrOutputTerminalNumbers.add("23");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_24);
			arrOutputTerminalNumbers.add("24");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_25);
			arrOutputTerminalNumbers.add("25");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_26);
			arrOutputTerminalNumbers.add("26");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_27);
			arrOutputTerminalNumbers.add("27");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_28);
			arrOutputTerminalNumbers.add("28");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

			arrOutputPins.add(RaspiPin.GPIO_29);
			arrOutputTerminalNumbers.add("29");
			arrOutputPinStates.add(PinState.HIGH);
			arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			arrTestOutputTerminalActivationIntervals.add(-1L);
			arrTestOutputTerminalActivationIntervalStartTimes.add(0L);
		}else{
		*/
			//Initialize I2C devices

			//Set the number of 'pins' we're using on each chip:
			//Currently, 16 on each:
			arrNumberOfPinsPerI2CChip.add(NUMBER_OF_PINS_USED_ON_EACH_I2C_CHIP);
			arrNumberOfPinsPerI2CChip.add(NUMBER_OF_PINS_USED_ON_EACH_I2C_CHIP);
			arrNumberOfPinsPerI2CChip.add(NUMBER_OF_PINS_USED_ON_EACH_I2C_CHIP);
			arrNumberOfPinsPerI2CChip.add(NUMBER_OF_PINS_USED_ON_EACH_I2C_CHIP);

			try {
				gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, I2C_CHIP_ADDRESS_0x20);
			} catch (Exception e) {
				System.out.println("[1579025715] I2C address " + Integer.toString(I2C_CHIP_ADDRESS_0x20) + " could not be found on BUS 1. ");
			}

			try {
				gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, I2C_CHIP_ADDRESS_0x21);
			} catch (Exception e) {
				System.out.println("[1579025717] I2C address " + Integer.toString(I2C_CHIP_ADDRESS_0x21) + " could not be found on BUS 1. ");
			}

			try {
				gpioProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, I2C_CHIP_ADDRESS_0x22);
			} catch (Exception e) {
				System.out.println("[1579025720] I2C address " + Integer.toString(I2C_CHIP_ADDRESS_0x22) + " could not be found on BUS 1. ");
			}

			try {
				gpioProvider3 = new MCP23017GpioProvider(I2CBus.BUS_1, I2C_CHIP_ADDRESS_0x23);
			} catch (Exception e) {
				System.out.println("[1579025722] I2C address " + Integer.toString(I2C_CHIP_ADDRESS_0x23) + " could not be found on BUS 1. ");
			}

			//**********************
			//Inputs:

			//1
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A0);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//2
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//3
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A2);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//4
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A3);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//5
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A4);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//6
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_A5);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//7
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputPins.add(MCP23017Pin.GPIO_A6);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//8
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputPins.add(MCP23017Pin.GPIO_A7);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//9
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B0);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//10
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//11
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B2);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//12
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B3);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//13
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B4);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//14
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B5);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//15
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B6);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//16
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider1);
			arrInputPins.add(MCP23017Pin.GPIO_B7);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x21);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//17
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B7);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//18
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B6);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//19
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B5);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//20
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B4);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//21
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B3);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//22
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B2);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//23
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//24
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_B0);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//25
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A7);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//26
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A6);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//27
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A5);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//28
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A4);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//29
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A3);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//30
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A2);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//31
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A1);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);

			//32
			iTerminalNumber++;
			arrGpioInputProvider.add(gpioProvider0);
			arrInputPins.add(MCP23017Pin.GPIO_A0);
			arrInputChipAddresses.add(I2C_CHIP_ADDRESS_0x20);
			arrInputTerminalNumbers.add(Integer.toString(iTerminalNumber));
			arrInputPinPullResistances.add(PinPullResistance.PULL_UP);
			arrTestInputTerminalListeningStatus.add(0);
			arrTestInputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);		

		//}

		//******************************************************************************************
		//Outputs:
		//33
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A0);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//34
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A1);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//35
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A2);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//36
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A3);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//37
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A4);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//38
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A5);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//39
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A6);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//40
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_A7);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);
		
		//41
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A0);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//42
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A1);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//43
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A2);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//44
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A3);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//45
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A4);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//46
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A5);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//47
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A6);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//48
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_A7);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);
		
		//49
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B7);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//50
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B6);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//51
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B5);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//52
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B4);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//53
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B3);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//54
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B2);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//55
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B1);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//56
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider2);
		arrOutputPins.add(MCP23017Pin.GPIO_B0);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x22);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);
		
		//57
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B7);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//58
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B6);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//59
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B5);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//60
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B4);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//61
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B3);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//62
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B2);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//63
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B1);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);

		//64
		iTerminalNumber++;
		arrGpioOutputProvider.add(gpioProvider3);
		arrOutputPins.add(MCP23017Pin.GPIO_B0);
		arrOutputChipAddresses.add(I2C_CHIP_ADDRESS_0x23);
		arrOutputTerminalNumbers.add(Integer.toString(iTerminalNumber));
		arrOutputPinStates.add(PinState.HIGH);
		arrTestOutputTerminalContactStatus.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrTestOutputTerminalActivationIntervals.add(-1L);
		arrTestOutputTerminalActivationIntervalStartTimes.add(0L);
	}

	public void provisionTerminals(boolean bUsePiGpioPins) throws Exception{
		if (bFakingGPIOInput){
			System.out.println("[1579025727] Faking like I'm provisioning pins....");
			return;
		}

		gpio = GpioFactory.getInstance();

		//Provision the pins and assign each a terminal number that corresponds to the numbers on the terminal strip:
		// provision gpio input pins with pull up resistors enabled. Pull up resistors cause the 
		// pin to be pulled high when that pins circuit is open. To get an input signal the pin 
		// must be tied to any ground pin on the PI 2 board. 
		try {
			inputSensors = new GpioPinDigitalInput[getNumberOfInputTerminals()];
		} catch (Exception e) {
			throw new Exception("Error [1473452201] initializing inputsensors object - " + e.getMessage());
		}
		for (int i = 0; i < arrInputPins.size(); i++){
			if(bUsePiGpioPins){
				try {
					inputSensors[i] = gpio.provisionDigitalInputPin(arrInputPins.get(i), arrInputTerminalNumbers.get(i), arrInputPinPullResistances.get(i));
				} catch (Exception e) {
					throw new Exception("Error [1473452202] initializing inputsensors (using GPIO pins) for pin: " + arrInputPins.get(i)
							+ ", terminal number: " + arrInputTerminalNumbers.get(i) + " - " + e.getMessage());
				}
			}else{
				try {
					inputSensors[i] = gpio.provisionDigitalInputPin(arrGpioInputProvider.get(i), arrInputPins.get(i), arrInputTerminalNumbers.get(i), arrInputPinPullResistances.get(i));
				} catch (Exception e) {
					throw new Exception("Error [1473452203] initializing inputsensors for pin: " + arrInputPins.get(i)
							+ ", terminal number: " + arrInputTerminalNumbers.get(i) + " - " + e.getMessage());
				}
			}
		}
		// provision gpio output pins with initial state LOW. 
		//These pins output 1.66V +-.01V and and should be connected to a relay board 
		//to interact with external devices. (Note: Limit current before connecting to ground, 230ohm+) 
		try {
			outputDevices = new GpioPinDigitalOutput[getNumberOfOutputTerminals()];
		} catch (Exception e) {
			throw new Exception("Error [1473452204] initializing outputsensors object - " + e.getMessage());
		}
		for (int i = 0; i < arrOutputPins.size(); i++){
			if(bUsePiGpioPins){
				try {
					outputDevices[i] = gpio.provisionDigitalOutputPin(arrOutputPins.get(i), arrOutputTerminalNumbers.get(i), arrOutputPinStates.get(i));
				} catch (Exception e) {
					throw new Exception("Error [1473452205] initializing outputsensors (using GPIO pins) for pin: " + arrOutputPins.get(i)
							+ ", terminal number: " + arrOutputTerminalNumbers.get(i) + " - " + e.getMessage());
				}	
			}else{
				try {
					outputDevices[i] = gpio.provisionDigitalOutputPin(arrGpioOutputProvider.get(i), arrOutputPins.get(i), arrOutputTerminalNumbers.get(i), arrOutputPinStates.get(i));
				} catch (Exception e) {
					throw new Exception("Error [1473452206] initializing outputsensors for pin: " + arrOutputPins.get(i)
							+ ", terminal number: " + arrOutputTerminalNumbers.get(i) + " - " + e.getMessage());
				}
			}
		}
	}

	public void setInputTerminalListeningStatus(final String sTerminalNumber, boolean bListening) throws Exception{
		if (bFakingGPIOInput){
			if (bListening){
				arrTestInputTerminalListeningStatus.set(getInputPinIndex(sTerminalNumber), 1);
			}else{
				arrTestInputTerminalListeningStatus.set(getInputPinIndex(sTerminalNumber), 0);
			}
			return;
		}
		if (!getIsPinProvisioned(sTerminalNumber)){
			return;
		}
		//Get pin array index corresponding to PI pin number
		int pinIndex = getInputPinIndex(sTerminalNumber);
		if(pinIndex == -1){
			throw new Exception("[1579025761] Requested input terminal " + sTerminalNumber + " can not "
					+ "be found in array of provisioned input pins");
		}
		if(bListening){
			inputSensors[pinIndex].addListener(new GpioPinListenerDigital() {
				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					if(event.getState() == PinState.HIGH){
						ServerRequests.triggerEventHandler(sTerminalNumber, SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED);
					}else{
						ServerRequests.triggerEventHandler(sTerminalNumber, SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_CLOSED);
					}
				}
			});
			//Set debounce interval for input pin
			inputSensors[pinIndex].setDebounce(SSConstants.INPUT_DEBOUNCE_TIME);
		}else{
			//If this pin is armed, disarm it
			if(getInputTerminalListeningStatus(sTerminalNumber)){
				inputSensors[pinIndex].removeAllListeners();
			}
		}	
	}

	public void setFakeTerminalContactStatus(final String sTerminalNumber, boolean bContactOpen) throws Exception{

		int iPinIndex = getInputPinIndex(sTerminalNumber);
		if (iPinIndex == -1){
			throw new Exception("[1579025767] Terminal number '" + sTerminalNumber + "' does not correspond to an input pin on the controller.");
		}

		if (bContactOpen){
			//If we were 'listening' on these terminals, we may need to send a 'trigger' event to report that the state has changed:
			if (arrTestInputTerminalListeningStatus.get(getInputPinIndex(sTerminalNumber)) == 1){
				//If the status is currently CLOSED, then we'll need to report a state change:
				if (arrTestInputTerminalContactStatus.get(getInputPinIndex(sTerminalNumber)).compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					//The 'pin state' would now go to 'HIGH' so we send a 'Trigger state START' report:
					ServerRequests.triggerEventHandler(sTerminalNumber, SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED);
				}else{
					//If the terminal contacts were ALREADY open, we don't need to send any report... 
				}
			}
			//Now just set the terminal status:
			arrTestInputTerminalContactStatus.set(getInputPinIndex(sTerminalNumber), SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		}else{
			//If we were 'listening' on these terminals, we may need to send a 'trigger' event to report that the state has changed:
			if (arrTestInputTerminalListeningStatus.get(getInputPinIndex(sTerminalNumber)) == 1){
				//If the status is currently OPEN, then we'll need to report a state change:
				if (arrTestInputTerminalContactStatus.get(getInputPinIndex(sTerminalNumber)).compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					//The 'pin state' would now go to 'LOW' so we send a 'Trigger state END' report:
					ServerRequests.triggerEventHandler(sTerminalNumber, SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED);
				}else{
					//If the terminal contacts were ALREADY closed, we don't need to send any report...
				}
			}
			//Now just set the terminal status:
			arrTestInputTerminalContactStatus.set(getInputPinIndex(sTerminalNumber), SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);
		}
		return;
	}

	public void setFakingGPIOInput(boolean bFakeIt){
		bFakingGPIOInput = bFakeIt;
	}

	static public boolean getInputTerminalListeningStatus(String sTerminalNumber){
		if (bFakingGPIOInput){
			if (arrTestInputTerminalListeningStatus.get(getInputPinIndex(sTerminalNumber)) == 1){
				return true;
			}else{
				return false;
			}
		}
		Collection<GpioPinListener> PinListeners = inputSensors[getInputPinIndex(sTerminalNumber)].getListeners();		
		return PinListeners.iterator().hasNext();
	}

	public boolean areInputTerminalContactsOpen(String sTerminalNumber) throws Exception{
		if (bFakingGPIOInput){
			if (arrTestInputTerminalContactStatus.get(getInputPinIndex(sTerminalNumber)).compareToIgnoreCase(
					SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
				return true;
			}else{
				return false;
			}
		}
		boolean bTerminalContactsAreOpen = false;

		if(inputSensors[getInputPinIndex(sTerminalNumber)].getState() == PinState.HIGH){
			bTerminalContactsAreOpen = true;
		}else if(inputSensors[getInputPinIndex(sTerminalNumber)].getState() == PinState.LOW){
			bTerminalContactsAreOpen = false;
		}else{
			throw new Exception ("Input terminal " + inputSensors[getInputPinIndex(sTerminalNumber)].getName() + " state can not be determined");
		}
		return bTerminalContactsAreOpen;
	}
	public boolean areOutputTerminalContactsOpen(String sTerminalNumber) throws Exception{
		if (bFakingGPIOInput){
			if (arrTestOutputTerminalContactStatus.get(getOutputPinIndex(sTerminalNumber)).compareToIgnoreCase(
					SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
				return true;
			}else{
				return false;
			}
		}
		boolean bTerminalContactsAreOpen = false;
		if(outputDevices[getOutputPinIndex(sTerminalNumber)].getState() == PinState.HIGH){
			bTerminalContactsAreOpen = true;
		}else if(outputDevices[getOutputPinIndex(sTerminalNumber)].getState() == PinState.LOW){
			bTerminalContactsAreOpen = false;
		}else{
			throw new Exception ("[1579025775] Output terminal " + outputDevices[getOutputPinIndex(sTerminalNumber)].getName() + " state can not be determined");
		}
		return bTerminalContactsAreOpen;
	}

	public void setOutputTerminalContactStatus(String sTerminalNumber, boolean bContactsClose, long lContactClosureInterval) throws Exception{
/*
		//Set the intervals (durations):
		if (bContactsClose){
			arrTestOutputTerminalActivationIntervals.set(getOutputPinIndex(sTerminalNumber), lContactClosureInterval);
			arrTestOutputTerminalActivationIntervalStartTimes.set(getOutputPinIndex(sTerminalNumber), System.currentTimeMillis());
		}else{
			arrTestOutputTerminalActivationIntervals.set(getOutputPinIndex(sTerminalNumber), 0L);
			arrTestOutputTerminalActivationIntervalStartTimes.set(getOutputPinIndex(sTerminalNumber), 0L);
		}
*/
		if (bFakingGPIOInput){
			if (bContactsClose){
				arrTestOutputTerminalContactStatus.set(getOutputPinIndex(sTerminalNumber), SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);
			}else{
				arrTestOutputTerminalContactStatus.set(getOutputPinIndex(sTerminalNumber), SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			}
			return;
		}
		if (!getIsPinProvisioned(sTerminalNumber)){
			throw new Exception("[1579025780] Requested ouput terminal number " + sTerminalNumber + " has not "
					+ "been provisioned");
		}

		int pinIndex = getOutputPinIndex(sTerminalNumber);
		if(pinIndex == -1){
			throw new Exception("[1579025782] Requested ouput terminal number " + sTerminalNumber + " can not "
					+ "be found in array of provisioned output pins");
		}
/*
		if(bContactsClose){
			outputDevices[pinIndex].low();
		}else{
			outputDevices[pinIndex].high();
		}
*/
		
		if(bContactsClose){
			//Passing 
			if (lContactClosureInterval == 0){
				//Open the output contacts
				outputDevices[pinIndex].high();
			}else{
				//Close the output contacts for the set interval
				outputDevices[pinIndex].pulse(lContactClosureInterval, PinState.LOW, false);
			}
		}else{
			//Open the output contacts
			outputDevices[pinIndex].high();
		}
		 
	}

	// TJR - 7/13/2016 - removed this since we switched back to using the 'pulse' function:
	/*
	public long getOutputTerminalInterval(String sTerminalNumber){
		return arrTestOutputTerminalActivationIntervals.get(getOutputPinIndex(sTerminalNumber));
	}
	*/
	// TJR - 7/13/2016 - removed this since we switched back to using the 'pulse' function:
	/*
	public long getOutputTerminalIntervalRemaining(String sTerminalNumber){
		if (arrTestOutputTerminalActivationIntervalStartTimes.get(getOutputPinIndex(sTerminalNumber)) > 0L){
			//The 'active time' so far (i.e., how long the terminal has been active) is equal to the current time MINUS the starting time:
			long lActiveTime = System.currentTimeMillis() - arrTestOutputTerminalActivationIntervalStartTimes.get(getOutputPinIndex(sTerminalNumber));
			//The time remaining is equal to the original 'Interval' (duration) MINUS the active time:
			return arrTestOutputTerminalActivationIntervals.get(getOutputPinIndex(sTerminalNumber)) - lActiveTime;
		}else{
			return 0L;
		}
	}
	*/
	public void updateOutputTerminalDurations() throws Exception{
		for (int i = 0; i < arrTestOutputTerminalActivationIntervals.size(); i++){
			//If a duration has been exhausted, then make sure we turn off the contact closure for that terminal
			//and reset the duration and start times:

			//If the terminal HAS a duration start time:
			if (arrTestOutputTerminalActivationIntervals.get(i) > 0L){
				//If the duration has completed:
				if ((System.currentTimeMillis() - arrTestOutputTerminalActivationIntervalStartTimes.get(i)) >  arrTestOutputTerminalActivationIntervals.get(i)){
					//Reset the output contacts:
					try {
						setOutputTerminalContactStatus(getOutputTerminalNumber(i), false, 0L);
					} catch (Exception e) {
						throw new Exception("Error [1460748622] setting output terminal contact status - " + e.getMessage());
					}
				}
			}
		}
	}

	//Used for diagnostics
	public String getsProvisionedTerminals() {
		String s = "";

		if (bFakingGPIOInput){
			return "Faking GPIO - can't get pin provision status.";
		}
		Collection<GpioPin> provisionedPins = gpio.getProvisionedPins();
		for(GpioPin provisionedPin : provisionedPins){
			s += " - " + provisionedPin.toString() + "\n";
		}
		return s;
	}

	public boolean getIsPinProvisioned(String sTerminalNumber){
		if (bFakingGPIOInput){
			return true;
		}
		Collection<GpioPin> provisionedPins = gpio.getProvisionedPins();
		for(GpioPin provisionedPin : provisionedPins){
			if (provisionedPin.getName().compareToIgnoreCase(sTerminalNumber) == 0){
				return true;
			}
		}
		return false;
	}

	public String getInputTerminalNumber(int iPinIndex){	
		return arrInputTerminalNumbers.get(iPinIndex);
	}
	public String getOutputTerminalNumber(int iPinIndex){	
		return arrOutputTerminalNumbers.get(iPinIndex);
	}

	private static int getInputPinIndex(String sTerminalNumber){	
		for(int i = 0; i < arrInputTerminalNumbers.size(); i++){
			if(arrInputTerminalNumbers.get(i).compareToIgnoreCase(sTerminalNumber)==0){
				return i;
			}
		}
		return -1; 
	}

	private static int getOutputPinIndex(String sTerminalNumber){		
		for(int i = 0; i < arrOutputTerminalNumbers.size(); i++){
			if(arrOutputTerminalNumbers.get(i).compareToIgnoreCase(sTerminalNumber)==0){
				return i;
			}
		}
		return -1; 
	}
	public int getNumberOfInputTerminals(){
		return arrInputPins.size();
	}
	public int getNumberOfOutputTerminals(){
		return arrOutputPins.size();
	}

	public void shutdownGPIO(){
		if (bFakingGPIOInput){
			return;
		}
		gpio.shutdown();
	}
	public ArrayList<Pin> getInputPinArray(){
		return arrInputPins;
	}
	public ArrayList<String> getInputTerminalArray(){
		return arrInputTerminalNumbers;
	}
	public ArrayList<Pin> getOutputPinArray(){
		return arrOutputPins;
	}
	public ArrayList<String> getOutputTerminalArray(){
		return arrOutputTerminalNumbers;
	}
	public GpioPinDigitalInput[] getProvisionedGpioInputArray(){
		return inputSensors;
	}
	public GpioPinDigitalOutput[] getProvisionedGpioOutputArray(){
		return outputDevices;
	}
	public int getI2CInputChipAddress(int iIndex){
		return  arrInputChipAddresses.get(iIndex);
	}
	public int getI2COutputChipAddress(int iIndex){
		return  arrOutputChipAddresses.get(iIndex);
	}

}
