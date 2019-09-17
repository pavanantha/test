package com.aep.cx.billing.events;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class OrderTracking extends Header{
	private String orderNumber;
	private String orderType;
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime orderDate;
	private ArrayList<Items> completedRequirements;
	private ArrayList<Items> pendingRequirements;
		
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public ArrayList<Items> getCompletedRequirements() {
		return completedRequirements;
	}

	public ArrayList<Items> getPendingRequirements() {
		return pendingRequirements;
	}

	public DateTime getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(DateTime orderDate) {
		this.orderDate = orderDate;
	}
	
	public void setCompletedRequirements(String completedRequirements) {
		DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		ArrayList<Items> itemList = new ArrayList<Items>();
		String [] parts = completedRequirements.split("~");
		for (String string : parts) {
			if (null != string.trim()) {
				Items item = new Items();
				item.setRequirementType(OrderType(string.substring(0,0 + 4)));
				item.setRequirementRequestedDate(shortFormatter.parseDateTime(string.substring(4, 4 + 10)));
				item.setRequirementCompletedDate(shortFormatter.parseDateTime(string.substring(4 + 10, 4 + 10 + 10)));
				item.setRequirementResponsibility(Responsibility(string.substring(4 + 10 + 10,4 + 10 + 10 + 16).trim()));
				itemList.add(item);
			}
		}
		this.completedRequirements = itemList;
	}
	
	public void setPendingRequirements(String completedRequirements) {
	
		DateTimeFormatter shortFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		ArrayList<Items> itemList = new ArrayList<Items>();
		String [] parts = completedRequirements.split("~");
		for (String string : parts) {
			if (null != string.trim()) {
				Items item = new Items();
				item.setRequirementType(OrderType(string.substring(0,0 + 4)));
				item.setRequirementRequestedDate(shortFormatter.parseDateTime(string.substring(4, 4 + 10)));
				item.setRequirementCompletedDate(shortFormatter.parseDateTime(string.substring(4 + 10, 4 + 10 + 10)));
				item.setRequirementResponsibility(Responsibility(string.substring(4 + 10 + 10,4 + 10 + 10 + 16).trim()));
				itemList.add(item);
			}
		}
		this.pendingRequirements = itemList;
	}
	
	public String getEmailContent() {
		StringBuilder requirements = new StringBuilder();
		requirements.append(this.orderNumber + ",");
		requirements.append(this.orderDate.toString("yyyy-MM-dd") + ",");
		
		for (Items items : completedRequirements) {
			requirements.append(items.getRequirementType() + ",");
			requirements.append(items.getRequirementRequestedDate().toString("yyyy-MM-dd") + ",");
			requirements.append(items.getRequirementCompletedDate().toString("yyyy-MM-dd") + ",");
			requirements.append(items.getRequirementResponsibility() + ",");
		}
		
		for(int i=completedRequirements.size();i<5;i++) {
			requirements.append(",,,,");
		}
		
		int count = 0;
		for (Items items : pendingRequirements) {
			count ++;
			requirements.append(items.getRequirementType() + ",");
			requirements.append(items.getRequirementRequestedDate().toString("yyyy-MM-dd") + ",");
			//requirements.append(items.getRequirementCompletedDate().toString("yyyy-MM-dd") + ",");
			if (count == 5) {
				requirements.append(items.getRequirementResponsibility());
			}
			else 
			{
				requirements.append(items.getRequirementResponsibility() + ",");
			}
		}
		for(int i=pendingRequirements.size();i<5;i++) {
			count ++;
			if ( count == 5) {
				requirements.append(",,");
			}
			else {
				requirements.append(",,,");
			}
		}
		return requirements.toString();
	}
	
	public String getMACSSEmailContent() {
		StringBuilder requirements = new StringBuilder();
		requirements.append("&Date"+ this.orderDate.toString("yyyy-MM-dd")+"&CompletedItems:");
		for (Items items : completedRequirements) {
			requirements.append(items.getRequirementType());
			requirements.append(items.getRequirementRequestedDate().toString("yyyy-MM-dd"));
			requirements.append(items.getRequirementCompletedDate().toString("yyyy-MM-dd"));
			requirements.append(items.getRequirementResponsibility());
		}
		
		return requirements.toString();
	}
	
	public String OrderType(String orderType)
    {
        if (orderType.contentEquals("ICNT"))
            return "County Inspection";
        else if (orderType.contentEquals("ICTY"))
            return "City Inspection";
        else if (orderType.contentEquals("ISTA"))
            return "State Inspection";
        else if (orderType.contentEquals("EDIR"))
            return "Retail Provider Order";
        else if (orderType.contentEquals("ECUR"))
            return "Customer Not Ready";
        return "";
    }

    private String Responsibility(String code)
    {
        switch (code)
        {
            case "ECUR":
                return "Customer";
            case "EDIR":
                return "Customer";
            case "ICNT":
                return "Customer";
            case "ICTY":
                return "Customer";
            case "ISTA":
                return "Customer";
            default:
                return "AEP";
        }
    }

}

class Items {
	private String requirementType;
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime requirementRequestedDate;
	@JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
	@JsonSerialize(using = CustomStdDateTimeSerializer.class)
	private DateTime requirementCompletedDate;
	private String requirementResponsibility;
	public String getRequirementType() {
		return requirementType;
	}
	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

	public String getRequirementResponsibility() {
		return requirementResponsibility;
	}
	public void setRequirementResponsibility(String requirementResponsibility) {
		this.requirementResponsibility = requirementResponsibility;
	}
	public DateTime getRequirementRequestedDate() {
		return requirementRequestedDate;
	}
	public void setRequirementRequestedDate(DateTime requirementRequestedDate) {
		this.requirementRequestedDate = requirementRequestedDate;
	}
	public DateTime getRequirementCompletedDate() {
		return requirementCompletedDate;
	}
	public void setRequirementCompletedDate(DateTime requirementCompletedDate) {
		this.requirementCompletedDate = requirementCompletedDate;
	}
	
	
}
