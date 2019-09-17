package com.aep.cx.utils.opco;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OperatingCompany {

	public void populateOperatingCompanyDetails(OperatingCompanyName operatingCompanyName)
    {
        this.opcoName = operatingCompanyName;

        switch (opcoName)
        {
            case AEPOhio:

                this.channelID = "150034";
                this.fullName = this.abbreviatedName = "AEP Ohio";
                this.opcoSite = "http://aepohio.com";
                break;

            case AEPTexas:

                this.channelID = "150041";
                this.fullName = this.abbreviatedName = "AEP Texas";
                this.opcoSite = "http://aeptexas.com";
                break;

            case AppalachianPower:

                this.channelID = "150036";
                this.fullName = "Appalachian Power";
                this.abbreviatedName = "APCo";
                this.opcoSite = "http://apcopwr.com";
                break;

            case IndianaMichiganPower:

                this.channelID = "150038";
                this.fullName = "Indiana Michigan Power";
                this.abbreviatedName = "I%26M";
                this.opcoSite = "http://iandmpwr.com";
                break;

            case KentuckyPower:

                this.channelID = "150039";
                this.fullName = "Kentucky Power";
                this.abbreviatedName = "KY Pwr";
                this.opcoSite = "http://kypco.com";

                break;

            case PSO:

                this.channelID = "150040";
                this.fullName = "Public Service Company of Oklahoma";
                this.abbreviatedName = "PSO";
                this.opcoSite = "http://psoklahoma.com";
                break;

            case SWEPCO:

                this.channelID = "118723";
                this.fullName = "Southwestern Electric Power Company";
                this.abbreviatedName = "SWEPCO";
                this.opcoSite = "http://swepco.com";
                break;

            default:
                this.channelID = "999999";
                this.fullName = "AEPInvalid";
                this.abbreviatedName = "AEPInvalid";
                this.opcoSite = "http://aep.com";
                break;
        }

        this.learnMoreLinkUrl = "";
        this.manageAlertUrl = "/alerts";
        this.reportOutageUrl = "/out";
        this.payMyBillUrl = "/pay/";
        this.unsubscribeUrl = "/alerts/unsubscribe";
        this.sMSHelpUrl = "/help";
        this.outageStatusUrl = "/status";
        this.assistAgencyLink = "/bills/assistance";
    }

	@JsonCreator
    public OperatingCompany(String operatingCompanyNumber)
    {
        this.companyCode = operatingCompanyNumber;

        switch (operatingCompanyNumber)
        {
            case "01":
                this.fullName = "Appalachian Power";
                this.abbreviatedName = "APCO";
                this.opcoName = OperatingCompanyName.AppalachianPower;

                break;

            case "02":
                this.fullName = "Appalachian Power";
                this.abbreviatedName = "APCO";
                this.opcoName = OperatingCompanyName.AppalachianPower;
                break;

            case "03":
                this.fullName = "Kentucky Power";
                this.abbreviatedName = "KY Pwr";
                this.opcoName = OperatingCompanyName.KentuckyPower;
                break;

            case "04":
                this.fullName = "Indiana Michigan Power";
                this.abbreviatedName = "I%26M";
                this.opcoName = OperatingCompanyName.IndianaMichiganPower;
                break;

            case "06":
                this.fullName = "Appalachian Power";
                this.abbreviatedName = "APCO";
                this.opcoName = OperatingCompanyName.AppalachianPower;
                break;

            case "07":
                this.fullName = this.abbreviatedName = "AEP Ohio";
                this.opcoName = OperatingCompanyName.AEPOhio;
                break;

            case "10":
                this.fullName = this.abbreviatedName = "AEP Ohio";
                this.opcoName = OperatingCompanyName.AEPOhio;
                break;

            case "95":
                this.fullName = "Public Service Company of Oklahoma";
                this.abbreviatedName = "PSO";
                this.opcoName = OperatingCompanyName.PSO;
                break;

            case "96":
                this.fullName = "Southwestern Electric Power Company";
                this.abbreviatedName = "SWEPCO";
                this.opcoName = OperatingCompanyName.SWEPCO;
                break;

            case "97":
                this.fullName = "AEP Texas";
                this.abbreviatedName = "AEP Texas";
                this.opcoName = OperatingCompanyName.AEPTexas;
                break;
            case "94":
                this.fullName = "AEP Texas";
                this.abbreviatedName = "AEP Texas";
                this.opcoName = OperatingCompanyName.AEPTexas;
                break;

            default:
                this.fullName = "AEPInvalid";
                this.abbreviatedName = "AEPInvalid";
                this.opcoName = OperatingCompanyName.ALL;
                break;
        }
        
        populateOperatingCompanyDetails(this.opcoName);
    }


    private OperatingCompanyName opcoName;
    private String fullName;
    private String abbreviatedName;
    private String companyCode;
    private String reportOutageUrl;
    private String outageStatusUrl ;
    private String manageAlertUrl ;
    private String learnMoreLinkUrl;
    private String unsubscribeUrl;
    private String payMyBillUrl;
    private String sMSHelpUrl;
    private String assistAgencyLink;
    private String channelID;
    private String opcoSite;
    
	public OperatingCompanyName getOpcoName() {
		return opcoName;
	}

	public void setOpcoName(OperatingCompanyName opcoName) {
		this.opcoName = opcoName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAbbreviatedName() {
		return abbreviatedName;
	}

	public void setAbbreviatedName(String abbreviatedName) {
		this.abbreviatedName = abbreviatedName;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getReportOutageUrl() {
		return reportOutageUrl;
	}

	public void setReportOutageUrl(String reportOutageUrl) {
		this.reportOutageUrl = reportOutageUrl;
	}

	public String getOutageStatusUrl() {
		return outageStatusUrl;
	}

	public void setOutageStatusUrl(String outageStatusUrl) {
		this.outageStatusUrl = outageStatusUrl;
	}

	public String getManageAlertUrl() {
		return manageAlertUrl;
	}

	public void setManageAlertUrl(String manageAlertUrl) {
		this.manageAlertUrl = manageAlertUrl;
	}

	public String getLearnMoreLinkUrl() {
		return learnMoreLinkUrl;
	}

	public void setLearnMoreLinkUrl(String learnMoreLinkUrl) {
		this.learnMoreLinkUrl = learnMoreLinkUrl;
	}

	public String getUnsubscribeUrl() {
		return unsubscribeUrl;
	}

	public void setUnsubscribeUrl(String unsubscribeUrl) {
		this.unsubscribeUrl = unsubscribeUrl;
	}

	public String getPayMyBillUrl() {
		return payMyBillUrl;
	}

	public void setPayMyBillUrl(String payMyBillUrl) {
		this.payMyBillUrl = payMyBillUrl;
	}

	public String getSMSHelpUrl() {
		return sMSHelpUrl;
	}

	public void setSMSHelpUrl(String sMSHelpUrl) {
		this.sMSHelpUrl = sMSHelpUrl;
	}

	public String getAssistAgencyLink() {
		return assistAgencyLink;
	}

	public void setAssistAgencyLink(String assistAgencyLink) {
		this.assistAgencyLink = assistAgencyLink;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getOpcoSite() {
		return opcoSite;
	}

	public void setOpcoSite(String opcoSite) {
		this.opcoSite = opcoSite;
	}
}
