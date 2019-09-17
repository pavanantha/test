package com.aep.cx.utils.alerts.notification.msessages;

public class EmailDeliveryHeader {
	private String HashLink;
	private String AccountNumber;
	private String Template;
	private String StreetAddress1;
	private String StreetAddress2;
	private String City;
	private String State;
	private String ZipCode;
	private String FirstName;
	private String AccountNickname;
	private String Preheader;
	private String Subject;
	private String SubscriberKey;
	private String EmailAddress;
	private String ChannelMemberID;
	private String ESID;
	private String LearnMoreLink;
	
	public EmailDeliveryHeader () {
		this.StreetAddress1 = "123 M*";
		this.City = "Columbus";
		this.State = "OH";
		this.ZipCode = "43215-9999";
	}
	
	public String getTemplate() {
		return Template;
	}

	public void setTemplate(String template) {
		Template = template;
	}

	public String getHashLink() {
		return HashLink;
	}

	public void setHashLink(String hashLink) {
		HashLink = hashLink;
	}

	public String getAccountNumber() {
		return AccountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		AccountNumber = accountNumber;
	}

	public String getStreetAddress1() {
		return StreetAddress1;
	}

	public void setStreetAddress1(String streetAddress1) {
		StreetAddress1 = streetAddress1;
	}

	public String getStreetAddress2() {
		return StreetAddress2;
	}

	public void setStreetAddress2(String streetAddress2) {
		StreetAddress2 = streetAddress2;
	}

	public String getCity() {
		return City;
	}

	public void setCity(String city) {
		City = city;
	}

	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String getZipCode() {
		return ZipCode;
	}

	public void setZipCode(String zipCode) {
		ZipCode = zipCode;
	}

	public String getFirstName() {
		return FirstName;
	}

	public void setFirstName(String firstName) {
		FirstName = firstName;
	}

	public String getAccountNickname() {
		return AccountNickname;
	}

	public void setAccountNickname(String accountNickname) {
		AccountNickname = accountNickname;
	}

	public String getPreheader() {
		return Preheader;
	}

	public void setPreheader(String preheader) {
		Preheader = preheader;
	}

	public String getSubject() {
		return Subject;
	}

	public void setSubject(String subject) {
		Subject = subject;
	}

	public String getSubscriberKey() {
		return SubscriberKey;
	}

	public void setSubscriberKey(String subscriberKey) {
		SubscriberKey = subscriberKey;
	}

	public String getEmailAddress() {
		return EmailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		EmailAddress = emailAddress;
	}

	public String getChannelMemberID() {
		return ChannelMemberID;
	}

	public void setChannelMemberID(String channelMemberID) {
		ChannelMemberID = channelMemberID;
	}

	public String getESID() {
		return ESID;
	}

	public void setESID(String eSID) {
		ESID = eSID;
	}

	public String getLearnMoreLink() {
		return LearnMoreLink;
	}

	public void setLearnMoreLink(String learnMoreLink) {
		LearnMoreLink = learnMoreLink;
	}

	@Override
	public String toString() {
		return HashLink + "," + AccountNumber + "," + Template + "," + StreetAddress1 + "," + StreetAddress2 + ","
				+ City + "," + State + "," + ZipCode + "," + FirstName + "," + AccountNickname + "," + Preheader + ","
				+ Subject + "," + SubscriberKey + "," + EmailAddress + "," + ChannelMemberID + "," + ESID + ","
				+ LearnMoreLink;
	}

}
