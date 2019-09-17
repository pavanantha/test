package com.aep.cx.utils.opco;

import com.aep.cx.utils.enums.OperatingCompanyType;

public class OperatingCompanyV2 {

    private String fullName;
    private String abbreviatedName;
    private String companyCode;
    private String reportOutageUrl;
    private String outageStatusUrl;
    private String manageAlertUrl;
    private String learnMoreLinkUrl;
    private String unsubscribeUrl;
    private String payMyBillUrl;
    private String sMSHelpUrl;
    private String billAccountURL;
    private String assistAgencyLink;
    private String channelID;
    private String opcoSite;
    private String timeZone;

    private OperatingCompanyType operatingCompanyType;

    public OperatingCompanyV2() {

    }

    public OperatingCompanyV2(OperatingCompanyV2 that) {
        this.learnMoreLinkUrl = that.learnMoreLinkUrl;
        this.manageAlertUrl = that.manageAlertUrl;
        this.reportOutageUrl = that.reportOutageUrl;
        this.payMyBillUrl = that.payMyBillUrl;
        this.unsubscribeUrl = that.unsubscribeUrl;
        this.sMSHelpUrl = that.sMSHelpUrl;
        this.outageStatusUrl = that.outageStatusUrl;
        this.assistAgencyLink = that.assistAgencyLink;
        this.billAccountURL = that.billAccountURL;
    }

    public OperatingCompanyType getOperatingCompanyType() {
        return operatingCompanyType;
    }

    public void setOperatingCompanyType(OperatingCompanyType operatingCompanyType) {
        this.operatingCompanyType = operatingCompanyType;
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

    public String getBillAccountURL() {
        return billAccountURL;
    }

    public void setBillAccountURL(String billAccountURL) {
        this.billAccountURL = billAccountURL;
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

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
