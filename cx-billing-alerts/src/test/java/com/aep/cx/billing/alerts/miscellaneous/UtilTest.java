package com.aep.cx.billing.alerts.miscellaneous;

import com.aep.cx.utils.delivery.MessageDeliveryHelper;

public class UtilTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String S = "FOQURFIPQWUERFIWEURTOI UWREOGYEWURYGRWE UYGIUWEYFUEWYFGUEWYGFWYEUGEWYIWEGYGWU";
		String S1 = "XATPYMFOQURFIPQWUERFIWEURTOI UWREOGYEWURYGRWE UYGIUWEYFUEWYFGUEWYGFWYEUGEWYIWEGYGWU";
		
		System.out.println("Result should be false for "+ S.substring(0,6)+"=" +MessageDeliveryHelper.messageContains(S.substring(0,6)));
		System.out.println("Result should be true for "+ S1.substring(0,6)+"=" +MessageDeliveryHelper.messageContains(S1.substring(0,6)));

	}

}
