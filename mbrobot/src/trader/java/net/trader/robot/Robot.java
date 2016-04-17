package net.trader.robot;

import java.io.File;

public class Robot {

	private File file;
	private Integer delayTime;
	
	public Robot() {
		delayTime = 60;		
	}

	public Robot(String fileName, Integer delayTime) {
		this.file = new File(fileName);
		this.delayTime = delayTime;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		if (file == null)
			return "";
		return file.getAbsolutePath();
	}

	public void setFileName(String fileName) {
		this.file = new File(fileName);
	}

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

}
