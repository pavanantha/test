package com.aep.cx.macss.customer.subscriptions;

import java.util.ArrayList;

import com.aep.cx.preferences.dao.CustomerContacts;
import com.aep.cx.preferences.dao.CustomerInfo;
import com.aep.cx.preferences.dao.CustomerPreferences;

public class MACSSLayout {

	private String tdatName;
	private CustomerInfo customerInfo = new CustomerInfo();
	private ArrayList<CustomerContacts> customerContacts;
	private ArrayList<CustomerContacts> billPayContacts;
	private ArrayList<CustomerContacts> powerPayContacts;
	private ArrayList<CustomerContacts> orderContacts;
	private CustomerPreferences customerPreferences;
	private CustomerPreferences billPayPreferences;
	private CustomerPreferences powerPayPreferences;
	private CustomerPreferences orderPreferences;

	private MACSSAlertSubscriptions emailPreferences;
	private MACSSAlertSubscriptions textPreferences;
	
	public MACSSLayout(String MQMessage) {
		String[] list = MQMessage.split("\\|");
		this.customerInfo.setAccountNumber(list[1].trim());
		this.customerInfo.setPremiseNumber(list[3].trim());
		this.customerInfo.setStreetAddress(list[5].trim()+ " " + list[8].trim());
		//account stattus (list[4].trim());
		this.customerInfo.setCity(list[9].trim());
		this.customerInfo.setState(list[10].trim());
		this.customerInfo.setZipCode(list[11].trim());
		this.customerInfo.setName((list[12].trim() + " " + list[13].trim()).trim());

		String subText = list[14].trim();
		String[] subList = subText.split("\\~");
		
		ArrayList<SubscribedAlertNames> eAlert = new ArrayList<SubscribedAlertNames>();	
		ArrayList<SubscribedAlertNames> tAlert = new ArrayList<SubscribedAlertNames>();
		customerContacts = new ArrayList<CustomerContacts>();
		billPayContacts = new ArrayList<CustomerContacts>();
		powerPayContacts = new ArrayList<CustomerContacts>();
		orderContacts = new ArrayList<CustomerContacts>();
		
		for (String alert : subList) {
			if (null != alert.trim()) {
				//System.out.println("alert name is :" + alert.substring(0, 16));
				//System.out.println("alert sub is :" + alert.substring(16, 17));
				
				if (alert.substring(16, 17).toLowerCase().contains("n")) {
					eAlert.add(SubscribedAlertNames.valueOf(alert.substring(0, 16).trim()));
					CustomerContacts contact = new CustomerContacts();
					contact.setEndPoint(null);
					contact.setWebId(list[2].trim());
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.OUTAGE)) {
						customerContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.BILLINGPAYMENT)) {
						billPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.POWERPAY)) {
						powerPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.ORDER)) {
						orderContacts.add(contact);						
					}
				}
				
				if (alert.substring(16, 17).toLowerCase().contains("e")) {
					eAlert.add(SubscribedAlertNames.valueOf(alert.substring(0, 16).trim()));
					
					CustomerContacts contact = new CustomerContacts();
					contact.setEndPoint(list[6].trim());
					contact.setWebId(list[2].trim());
					// customerContacts.add(contact);

					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.OUTAGE)) {
						customerContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.BILLINGPAYMENT)) {
						billPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.POWERPAY)) {
						powerPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.ORDER)) {
						orderContacts.add(contact);					
					}
				}

				if (alert.substring(16, 17).toLowerCase().contains("t")) {
					tAlert.add(SubscribedAlertNames.valueOf(alert.substring(0, 16).trim()));
					
					CustomerContacts contact = new CustomerContacts();
					contact.setEndPoint(list[7].trim());
					contact.setWebId(list[2].trim());
					// customerContacts.add(contact);

					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.OUTAGE)) {
						customerContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.BILLINGPAYMENT)) {
						billPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.POWERPAY)) {
						powerPayContacts.add(contact);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.ORDER)) {
						orderContacts.add(contact);						
					}

				}
				if (alert.substring(16, 17).toLowerCase().contains("b")) {
					eAlert.add(SubscribedAlertNames.valueOf(alert.substring(0, 16).trim()));
					tAlert.add(SubscribedAlertNames.valueOf(alert.substring(0, 16).trim()));
					
					CustomerContacts contact = new CustomerContacts();
					contact.setEndPoint(list[6].trim());
					contact.setWebId(list[2].trim());
					// customerContacts.add(contact);
					
					CustomerContacts contact1 = new CustomerContacts();
					contact1.setEndPoint(list[7].trim());
					contact1.setWebId(list[2].trim());
					// customerContacts.add(contact1);

					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.OUTAGE)) {
						customerContacts.add(contact);
						customerContacts.add(contact1);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.BILLINGPAYMENT)) {
						billPayContacts.add(contact);
						billPayContacts.add(contact1);
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.POWERPAY)) {
						powerPayContacts.add(contact);
						powerPayContacts.add(contact1);						
					}
					
					if (SubscribedAlertNames.valueOf(alert.substring(0, 16).trim())
							.equals(SubscribedAlertNames.ORDER)) {
						orderContacts.add(contact);
						orderContacts.add(contact1);						
					}
				}
			}
		}
		
		if (eAlert.size() > 0) {
			emailPreferences = new MACSSAlertSubscriptions();
			emailPreferences.setEndpoint(list[6].trim());
			emailPreferences.setWebID(list[2].trim());
			emailPreferences.setAlertNames(eAlert);
		}
		
		if (tAlert.size() > 0) {
			textPreferences = new MACSSAlertSubscriptions();
			textPreferences.setEndpoint(list[7].trim());
			textPreferences.setWebID(list[2].trim());
			textPreferences.setAlertNames(tAlert);
		}
		
		customerPreferences =  new CustomerPreferences();
		customerPreferences.setCustomerInfo(this.customerInfo);
		customerPreferences.setCustomerContacts(this.customerContacts);
		
		billPayPreferences = new CustomerPreferences();
		billPayPreferences.setCustomerContacts(billPayContacts);
		billPayPreferences.setCustomerInfo(customerInfo);
		
		powerPayPreferences = new CustomerPreferences();
		powerPayPreferences.setCustomerContacts(powerPayContacts);
		powerPayPreferences.setCustomerInfo(customerInfo);
		
		orderPreferences = new CustomerPreferences();
		orderPreferences.setCustomerContacts(orderContacts);
		orderPreferences.setCustomerInfo(customerInfo);
	}
	
	
	public String getTdatName() {
		return tdatName;
	}
	public void setTdatName(String tdatName) {
		this.tdatName = tdatName;
	}

	public MACSSAlertSubscriptions getEmailPreferences() {
		return emailPreferences;
	}


	public void setEmailPreferences(MACSSAlertSubscriptions emailPreferences) {
		this.emailPreferences = emailPreferences;
	}


	public MACSSAlertSubscriptions getTextPreferences() {
		return textPreferences;
	}

	public void setTextPreferences(MACSSAlertSubscriptions textPreferences) {
		this.textPreferences = textPreferences;
	}


	public CustomerPreferences getCustomerPreferences() {
		return customerPreferences;
	}


	public void setCustomerPreferences(CustomerPreferences customerPreferences) {
		this.customerPreferences = customerPreferences;
	}


	public ArrayList<CustomerContacts> getCustomerContacts() {
		return customerContacts;
	}


	public void setCustomerContacts(ArrayList<CustomerContacts> customerContacts) {
		this.customerContacts = customerContacts;
	}


	public ArrayList<CustomerContacts> getBillPayContacts() {
		return billPayContacts;
	}


	public void setBillPayContacts(ArrayList<CustomerContacts> billPayContacts) {
		this.billPayContacts = billPayContacts;
	}


	public ArrayList<CustomerContacts> getPowerPayContacts() {
		return powerPayContacts;
	}


	public void setPowerPayContacts(ArrayList<CustomerContacts> powerPayContacts) {
		this.powerPayContacts = powerPayContacts;
	}


	public CustomerPreferences getBillPayPreferences() {
		return billPayPreferences;
	}


	public void setBillPayPreferences(CustomerPreferences billPayPreferences) {
		this.billPayPreferences = billPayPreferences;
	}


	public CustomerPreferences getPowerPayPreferences() {
		return powerPayPreferences;
	}


	public void setPowerPayPreferences(CustomerPreferences powerPayPreferences) {
		this.powerPayPreferences = powerPayPreferences;
	}


	public CustomerPreferences getOrderPreferences() {
		return orderPreferences;
	}


	public void setOrderPreferences(CustomerPreferences orderPreferences) {
		this.orderPreferences = orderPreferences;
	}
	
	
	
}
