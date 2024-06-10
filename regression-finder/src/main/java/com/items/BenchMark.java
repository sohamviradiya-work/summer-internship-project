package com.items;

import com.items.interfaces.CSVItem;

public class BenchMark implements CSVItem {

	private static final int SECONDS_PER_MINUTE = 60;
	private static final double MILLISECONDS_PER_SECOND = 1000;
	private long time;
	private String method;

	public BenchMark(long time, String method) {
		this.time = time;
		this.method = method;
	}

	@Override
	public String toCSVString() {
        return this.method + "," + getTime();
	}

	public String getTime(){
		double seconds = time/MILLISECONDS_PER_SECOND;
		long minutes = (long) Math.floor(seconds/SECONDS_PER_MINUTE);
		seconds -= minutes*SECONDS_PER_MINUTE;
		return minutes + "," + seconds;
	}
    
}
