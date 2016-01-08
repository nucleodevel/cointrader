package com.robot;

import java.util.Scanner;

public class Robot {

	private double minimumBuyRate;
	private double minimumSellRate;
	private double minimumCoinAmount;
	private double incDecPrice;
	private int delayTime;
	
	public Robot() {
		
		minimumBuyRate = 0.01;
		minimumSellRate = 0.008;
		minimumCoinAmount = 0.01;
		incDecPrice = 0.00001;
		delayTime = 10;
		
	}
	
	public double getMinimumBuyRate() {
		return minimumBuyRate;
	}

	public void setMinimumBuyRate(double minimumBuyRate) {
		this.minimumBuyRate = minimumBuyRate;
	}

	public double getMinimumSellRate() {
		return minimumSellRate;
	}

	public void setMinimumSellRate(double minimumSellRate) {
		this.minimumSellRate = minimumSellRate;
	}

	public double getMinimumCoinAmount() {
		return minimumCoinAmount;
	}

	public void setMinimumCoinAmount(double minimumCoinAmount) {
		this.minimumCoinAmount = minimumCoinAmount;
	}

	public double getIncDecPrice() {
		return incDecPrice;
	}

	public void setIncDecPrice(double incDecPrice) {
		this.incDecPrice = incDecPrice;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}
	
	public void readParams() {
		Scanner scan = new Scanner(System.in );
		 
	    System.out.println("Enter the minimum buy rate (use ,): ");
	    try {
	    	minimumBuyRate = scan.nextDouble();
	    } 
	    catch (Exception ex) {
	    	minimumBuyRate = 0.01;
	    	System.out.println("Error: setting " + minimumBuyRate);
	    }
	    scan.nextLine();
	    
	    System.out.println("Enter the minimum sell rate (use ,): ");
	    try {
	    	minimumSellRate = scan.nextDouble();
	    } 
	    catch (Exception ex) {
	    	minimumSellRate = 0.008;
	    	System.out.println("Error: setting " + minimumSellRate);
	    }
	    scan.nextLine();
	    
	    System.out.println("Enter the inc/dec price (use ,): ");
	    try {
	    	incDecPrice = scan.nextDouble();
	    } 
	    catch (Exception ex) {
	    	incDecPrice = 0.00001;
	    	System.out.println("Error: setting " + incDecPrice);
	    }
	    scan.nextLine();
	    
	    System.out.println("Enter the delay: ");
	    try {
	    	delayTime = scan.nextInt();
	    } 
	    catch (Exception ex) {
	    	delayTime = 10;
	    	System.out.println("Error: setting " + delayTime);
	    }
	    scan.nextLine();
	    
	    scan.close();
	}

}
