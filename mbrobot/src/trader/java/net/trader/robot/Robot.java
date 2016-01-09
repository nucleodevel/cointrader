package net.trader.robot;

import java.util.Scanner;

public class Robot {

	private Integer delayTime;
	
	public Robot() {		
		delayTime = 10;		
	}

	public Robot(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}
	
	public void readParams(Scanner scanner) {
		delayTime = getIntFromKeyboard(scanner, delayTime, "delay time");
	}
	
	public Integer getIntFromKeyboard(Scanner scanner, Integer defaultValue, String label) {
		
		Integer value = defaultValue;
		
		System.out.println(
	    	"\nEnter the " + label + " - default " + defaultValue + 
	    	" if invalid real number: "
	    );
	    try {
	    	value = scanner.nextInt();
	    } 
	    catch (Exception ex) {
	    	System.out.println("Type error: setting " + value);
	    }
	    scanner.nextLine();
	    
	    return value;
	    
	}
	
	public Double getDoubleFromKeyboard(Scanner scanner, Double defaultValue, String label) {
		
		Double value = defaultValue;
		
		System.out.println(
	    	"\nEnter the " + label + " - default " + defaultValue + 
	    	" if invalid real number: "
	    );
	    try {
	    	value = scanner.nextDouble();
	    } 
	    catch (Exception ex) {
	    	System.out.println("Type error: setting " + value);
	    }
	    scanner.nextLine();
	    
	    return value;
	    
	}

}
