package com.aep.cx.outage.alerts.domains;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OutageData {
  @JsonProperty(value = "Data",required = false)
  private ArrayList<OutageEvent> outageData;

public ArrayList<OutageEvent> getOutageData() {
	return outageData;
}

public void setOutageData(ArrayList<OutageEvent> outageData) {
	this.outageData = outageData;
}



}