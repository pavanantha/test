package com.aep.cx.outage.alerts.domains;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.aep.cx.outage.alerts.enums.ValueAddAlertType;
import com.aep.cx.outage.alerts.enums.ValueAddOutageCauseReason;
import com.aep.cx.outage.alerts.enums.ValueAddOutageEstimatedTimeToRestorationType;
import com.aep.cx.outage.alerts.enums.ValueAddOutageSimpleCauseReason;
import com.aep.cx.outage.alerts.enums.ValueAddOutageStatusType;
import com.aep.cx.outage.alerts.enums.ValueAddOutageType;
import com.aep.cx.outage.alerts.enums.ValueAddProcessingReasonType;
import com.aep.cx.utils.time.CustomStdDateTimeDeserializer;
import com.aep.cx.utils.time.CustomStdDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutageEvent {

    final Logger logger = LogManager.getLogger(OutageEvent.class);

    private String premiseNumber;
    private ValueAddProcessingReasonType valueAddProcessingReasonType;
    private String outageNumber;

    @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
    private DateTime outageETR;

    private String premisePowerStatus;
    /*
     * Outage Type Provided by PowerOn - Predicted, EstimateTimeToRestore(ETR),
     * Restored, Cancelled, Held
     */
    private String outageType;
    /*
     * Outage ETR Type Provided by PowerOn - None, Global, Unavailable
     */
    private String outageETRType;
    /*
     * Value Add Operating Copmpany Code - Parsed Operation Company from Premise
     * Number if exist
     */
    private String valueAddOperatingCompany;
    private String valueAddOperatingCompanyTimeZone;
    private String valueAddServerProcessingTimeZone;
    private boolean valueAddBoolPremisePowerStatus;

    /* Time when Outage Event is created */
    @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
    private DateTime outageCreationTime;

    /* Time when Outage Event is Server Processing DateTime */
    private DateTime valueAddServerProcessingDateTime;

    /* Time when Outage Event is Server Processing DateTime */
   
    private DateTime valueAddCurrentDateTime;

    /* Outage Satus - New */
    private String outageStatus;
    private int momentaryWaitThresholdInMinutes = 10;
    private int expiredOutageInMinutes;

    /* Timestamp - Outage as of Time - Time Outage Start */
    @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
    private DateTime outageAsOfTime;

    @JsonDeserialize(using = CustomStdDateTimeDeserializer.class)
    @JsonSerialize(using = CustomStdDateTimeSerializer.class)
    private DateTime outageRestorationTime;

    private String outageCause;
    /* Outage Simple Cause - Simple Descriptive Reason for the Outage */
    private String outageSimpleCause;
    /* Override Flag - ?? */
    private String outageOverideFlag;
    /* Outage Area - Number means?? Find master Areas Table/Lookup */
    private String outageArea;
    /* Number of Total Customers Affected */
    private int outageCustomerCount;
    /* Possible total number of Customers impacted */
    private int outageCustomerMAXCount;
    private ValueAddAlertType valueAddAlertType;

    // @JsonInclude(JsonInclude.Include.NON_NULL)
    private String valueAddAlertName;

    /* Number of Customers that have contacted and reported an issue */
    private int outageTicketCount;
    
    private String batchKey;

    private DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm");

    public OutageEvent() {
    }

    public ValueAddProcessingReasonType getValueAddProcessingReasonType() {
        return valueAddProcessingReasonType;
    }

    public void setValueAddProcessingReasonType(ValueAddProcessingReasonType valueAddProcessingReasonType) {
        this.valueAddProcessingReasonType = valueAddProcessingReasonType;
    }

    public void setValueAddAlertName(String valueAddAlertName) {
        this.valueAddAlertName = valueAddAlertName;
    }

    @JsonIgnore
    public String getValueAddAlertName() {
        switch (valueAddAlertType) {
        case PREDICTED:
            return "predicted";
        case PREDICTEDNOETR:
            return "predictednoetr";
        case ETR:
            return "etr";
        case RESTORED:
            return "restored";
        case CANCELLED:
            return "cancelled";
        default:
            return "none";
        }
    }

    public ValueAddAlertType getValueAddAlertType() {
        return valueAddAlertType;
    }

    public void setValueAddAlertType(ValueAddAlertType valueAddAlertType) {
        this.valueAddAlertType = valueAddAlertType;
    }

    /* Premise Number */
    public String getPremiseNumber() {
        return premiseNumber;
    }

    public void setPremiseNumber(String premiseNumber) {
        this.premiseNumber = premiseNumber;
    }

    public void setOutageNumber(String outageNumber) {
        this.outageNumber = outageNumber;
    }

    public String getOutageNumber() {
        return outageNumber;
    }

    public DateTime getOutageETR() {
        return convertToLocalTime(outageETR);

    }

    public void setOutageETR(DateTime outageETR) {
        this.outageETR = outageETR;
    }

    public String getOutageType() {
        return this.outageType;
    }

    public void setOutageType(String outageType) {
        this.outageType = outageType;
    }

    /*
     * Value Add Outage Type, used to better describe the PowerOn Data and easier
     * Business Rules Predicted, EstimateTimeToRestore(ETR), Restored, Cancelled,
     * Held
     */
    @JsonIgnore
    public ValueAddOutageType getValueAddOutageType() {

        switch (outageType.toLowerCase()) {
        case "predicted":
            return ValueAddOutageType.Predicted;
        case "etr":
            return ValueAddOutageType.EstimatedTimeToRestoration;
        case "restored":
            return ValueAddOutageType.Restored;
        case "cancelled":
            return ValueAddOutageType.CancelledRestore;
        case "held":
            return ValueAddOutageType.Held;
        default:
            return ValueAddOutageType.None;
        }
    }

    public String getOutageETRType() {
        return this.outageETRType;
    }

    public void setOutageETRType(String outageETRType) {
        this.outageETRType = outageETRType;
    }

    /*
     * Value Add Outage ETR Type, used to better describe the PowerOn ETR Types
     */
    @JsonIgnore
    public ValueAddOutageEstimatedTimeToRestorationType getValueAddOutageEstimatedTimeToRestorationType() {

        switch (outageETRType.toLowerCase()) {

        case "g":
        case "global":
            return ValueAddOutageEstimatedTimeToRestorationType.Global;

        case "u":
        case "unavailable":
            return ValueAddOutageEstimatedTimeToRestorationType.Unavailable;

        default:
            return ValueAddOutageEstimatedTimeToRestorationType.None;
        }
    }

    public String getPremisePowerStatus() {
        return this.premisePowerStatus;
    }

    public void setPremisePowerStatus(String premisePowerStatus) {
        this.premisePowerStatus = premisePowerStatus;
    }

    // Getter
    @JsonIgnore
    public boolean getIsPremisePowerOn() {
        switch (premisePowerStatus.toLowerCase()) {
        case "on":
            return true;
        case "off":
            return false;
        default:
            return false;
        }
    }

    public String getOutageStatus() {
        return this.outageStatus;
    }

    public void setOutageStatus(String outageStatus) {
        this.outageStatus = outageStatus;
    }

    /*
     * Value Add Outage Status Type - Parsed from literal Outage Status
     */
    // Getter
    @JsonIgnore
    public ValueAddOutageStatusType getValueAddOutageStatusType() {

        switch (outageStatus.toLowerCase()) {
        case "new":
            return ValueAddOutageStatusType.New;
        default:
            return ValueAddOutageStatusType.None;
        }
    }

    public void setOutageCreationTime(DateTime outageCreDateTime) {
        this.outageCreationTime = outageCreDateTime;
    }

    // Getter
    public DateTime getOutageCreationTime() {
        return convertToLocalTime(outageCreationTime);
    }

	/**
	 * @return DateTime of OPCO
	 */
	public DateTime convertToLocalTime(DateTime dateTime) {
		DecimalFormat df = new DecimalFormat("00");
		if ( dateTime != null && valueAddOperatingCompanyTimeZone != null) {
             String dateString = String.valueOf( dateTime.getYear())
            		 				+ df.format( dateTime.getMonthOfYear());
             dateString += df.format( dateTime.getDayOfMonth());
             dateString += df.format( dateTime.getHourOfDay())
            		 		+ df.format( dateTime.getMinuteOfHour());

            return  DateTime.parse(dateString, dtFormatter.withZone(DateTimeZone.forID(valueAddOperatingCompanyTimeZone)));
        } else {
            return  dateTime;
        }
	}

    // Setter
    public void setMomentaryWaitThresholdInMinutes(int momentaryWaitThresholdInMinutes) {
        this.momentaryWaitThresholdInMinutes = momentaryWaitThresholdInMinutes;
    }

    // Getter
    public int getExpiredOutageInMinutes() {
        return expiredOutageInMinutes;
    }

    // Setter
    public void setExpiredOutageInMinutes(int expiredOutageInMinutes) {
        this.expiredOutageInMinutes = expiredOutageInMinutes;
    }

    @JsonIgnore
    public boolean getIsInMomentaryWait() {

        if (valueAddOperatingCompanyTimeZone != null && valueAddCurrentDateTime != null) {
            if (getOutageCreationTime().plusMinutes(this.momentaryWaitThresholdInMinutes)
                    .getMillis() < valueAddCurrentDateTime.getMillis()) {
                return false;
            } else {
                return true;
            }
        }

        return false;
        // TODO: Throw an Exception
    }

    public void setOutageAsOfTime(DateTime outageAsOfTime) {
        this.outageAsOfTime = outageAsOfTime;
    }

    public DateTime getOutageAsOfTime() {
    	return convertToLocalTime(outageAsOfTime);
    }

    public void setOutageArea(String outageArea) {
        this.outageArea = outageArea;
    }

    public String getOutageCause() {
        return outageCause;
    }

    public void setOutageCause(String outageCause) {
        this.outageCause = outageCause;
    }

    /*
     * Value Add Outage Cause Type - Parsed from literal Outage Status
     */
    private ValueAddOutageCauseReason valueAddOutageCause;

    @JsonIgnore
    public ValueAddOutageCauseReason getValueAddOutageCauseReason() {

        switch (outageStatus.toLowerCase()) {
        // case "new":
        // return ValueAddOutageCauseReason.New;
        default:
            return ValueAddOutageCauseReason.None;
        }
    }

    public String getOutageSimpleCause() {
        return outageSimpleCause;
    }

    public void setOutageSimpleCause(String outageSimpleCause) {
        this.outageSimpleCause = outageSimpleCause;
    }

    /*
     * Value Add Outage Simple Cause Type - Parsed from literal outageSimpleCause
     */
    @JsonIgnore
    public ValueAddOutageSimpleCauseReason getValueAddOutageSimpleCauseReason() {

        switch (outageSimpleCause.toLowerCase()) {
        // case "new":
        // return ValueAddOutageCauseReason.New;
        default:
            return ValueAddOutageSimpleCauseReason.None;
        }
    }

    public String getOutageOverideFlag() {
        return outageOverideFlag;
    }

    public void setOutageOverideFlag(String outageOverideFlag) {
        this.outageOverideFlag = outageOverideFlag;
    }

    public int getOutageCustomerCount() {
        return this.outageCustomerCount;
    }

    public void setOutageCustomerCount(int outageCustomerCount) {
        this.outageCustomerCount = outageCustomerCount;
    }

    public int getOutageCustomerMAXCount() {
        return this.outageCustomerMAXCount;
    }

    public void setOutageCustomerMAXCount(int outageCustomerMAXCount) {
        this.outageCustomerMAXCount = outageCustomerMAXCount;
    }

    public int getOutageTicketCount() {
        return outageTicketCount;
    }

    public void setOutageTicketCount(int outageTicketCount) {
        this.outageTicketCount = outageTicketCount;
    }

    public DateTime getOutageRestorationTime() {
    	return convertToLocalTime(outageRestorationTime);
    }

    public void setOutageRestorationTime(DateTime outageRestorationTime) {
        this.outageRestorationTime = outageRestorationTime;
    }

    public String getValueAddOperatingCompanyTimeZone() {
        return valueAddOperatingCompanyTimeZone;
    }

    public void setValueAddOperatingCompanyTimeZone(String valueAddOperatingCompanyTimeZone) {
        this.valueAddOperatingCompanyTimeZone = valueAddOperatingCompanyTimeZone;
    }

    public String getValueAddServerProcessingTimeZone() {
        return valueAddServerProcessingTimeZone;
    }

    public void setValueAddServerProcessingTimeZone(String valueAddServerProcessingTimeZone) {
        this.valueAddServerProcessingTimeZone = valueAddServerProcessingTimeZone;
    }

    public DateTime getValueAddServerProcessingDateTime() {
        return valueAddServerProcessingDateTime;
    }

    public void setValueAddServerProcessingDateTime(DateTime serverProcessingDateTime) {
        this.valueAddServerProcessingDateTime = serverProcessingDateTime;
    }

    @JsonIgnore
    public DateTime getValueAddCurrentDateTime() {
        return this.valueAddCurrentDateTime;
    }

    public void setValueAddCurrentDateTime(DateTime datetime) {
        // Only set the Current DateTime Once
        if (valueAddOperatingCompanyTimeZone != null && valueAddCurrentDateTime == null) {
            this.valueAddCurrentDateTime = datetime.withZone(DateTimeZone.forID(valueAddOperatingCompanyTimeZone));
        }else {
        	 this.valueAddCurrentDateTime = null;
        }
    }

	public String getBatchKey() {
		return batchKey;
	}

	public void setBatchKey(String batchKey) {
		this.batchKey = batchKey;
	}
}
