package com.items;

import com.items.interfaces.CSVItem;

public class BenchMark implements CSVItem {

	private long time;
	private String method;

	public BenchMark(long time, String method) {
		this.time = time;
		this.method = method;
	}

	@Override
	public String toCSVString() {
        return this.method + "," + this.time;
	}
    
}
