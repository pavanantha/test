package com.aep.cx.utils.enums;
import java.io.PrintStream.*;

public class AlertNames {
    
	public MessageTypeGlobal GlobalAlertType;

    public String AlertType;

	public AlertNames() {}

    public AlertNames(String alertType) {
            this.AlertType = alertType.toLowerCase().trim();

            switch (this.AlertType)
            {
                case "predicted":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "confirmed":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "reminder":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "restored":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "cancelled":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "etr":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "outage":
                    this.GlobalAlertType = MessageTypeGlobal.OUTAGE;
                    break;

                case "bill-due":
                    this.GlobalAlertType = MessageTypeGlobal.BILLDUE;
                    break;

                case "pending-payment":
                    this.GlobalAlertType = MessageTypeGlobal.PAYMENT;
                    break;

                case "disc-notice":
                    this.GlobalAlertType = MessageTypeGlobal.DISCONNECTISSUED;
                    break;

                case "payment":
                    this.GlobalAlertType = MessageTypeGlobal.PAYMENT;
                    break;

                case "welcome-email":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "welcome-text":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "confirm-email":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "confirm-text":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "unsub-text":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "unsub-text-or":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "unsub-text-ppay":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "dopt-text":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "dopt-confirm":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;

                case "psovpp":
                    this.GlobalAlertType = MessageTypeGlobal.PSOVPP;
                    break;

                case "vpp-event":
                    this.GlobalAlertType = MessageTypeGlobal.VPPEVENT;
                    break;

                case "ppay-pending":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-welcome":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-payment":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-dailybal":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-lowbal":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-zerobal":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-disconnect":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-reconnect":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-moratorium":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-insuff":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-pre-payment":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-enrollfail":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-unenroll":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-xferall":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-bal-adj":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-ebill":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;

                case "ppay-req-to-enrl":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;
                case "ppay-seasonal":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;
                case "ppay-seasonaloff":
                    this.GlobalAlertType = MessageTypeGlobal.POWERPAY;
                    break;
                case "crsp-email":
                    this.GlobalAlertType = MessageTypeGlobal.CRSPEMAIL;
                    break;
                case "auto-registered":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;
                case "auto-welcome":
                    this.GlobalAlertType = MessageTypeGlobal.SUBSCRIPTION;
                    break;
                case "uid-forgot":
                    this.GlobalAlertType = MessageTypeGlobal.REGISTRATION;
                    break;
                case "pwd-forgot":
                    this.GlobalAlertType = MessageTypeGlobal.REGISTRATION;
                    break;
                case "pwd-changed":
                    this.GlobalAlertType = MessageTypeGlobal.REGISTRATION;
                    break;
                case "usr-per-lock":
                    this.GlobalAlertType = MessageTypeGlobal.REGISTRATION;
                    break;
                case "usr-tmp-lock":
                    this.GlobalAlertType = MessageTypeGlobal.REGISTRATION;
                    break;
                case "cpp-enrollment":
                case "cpp-update":
                case "cpp-cancel":
                case "cpp-acknowledge":
                System.out.println("CPP input: " + this.AlertType);

                    this.GlobalAlertType = MessageTypeGlobal.CPPENROLLMENT;
                break;
                default:
                    System.out.println("No alert names, input: " + this.AlertType);
                    this.GlobalAlertType = MessageTypeGlobal.NONE;
                    break;
            }
        }    
}
