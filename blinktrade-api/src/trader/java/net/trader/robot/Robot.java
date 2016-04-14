package net.trader.robot;

public class Robot {

	private Integer delayTime;
	
	public Robot() {		
		delayTime = 60;		
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

}
