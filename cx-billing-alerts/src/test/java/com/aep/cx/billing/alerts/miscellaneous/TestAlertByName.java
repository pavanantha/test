package com.aep.cx.billing.alerts.miscellaneous;

import java.util.ArrayList;

import com.aep.cx.load.billing.alerts.service.Load2S3BillingService;
import com.aep.cx.macss.customer.subscriptions.MACSSIntegrationWrapper;

public class TestAlertByName {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {

		ArrayList<MACSSIntegrationWrapper> iwList = new ArrayList<MACSSIntegrationWrapper>();
		MACSSIntegrationWrapper iw = new MACSSIntegrationWrapper();
		iw.setMessageString("MBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxPAYPAL01PAYMENT         04014530234040145302xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx                      .00 000000000.00                                                2019-05-28        10.00CKPY2019-05-28-00034                             .002001-01-012001-01-01          .002001-01-01          .002001-01-01            .00          .002019-06-12-11.17.11.333882");
		iwList.add(iw);
		iw = new MACSSIntegrationWrapper();
		iw.setMessageString("MA                                            EXPALERTWELCOME-EMAIL   02000939898020009396                 moba_999@aep.com                                                                                                                                                                                                                                                                                                                moba_999@aep.com                                                                                                                                                                                                                                                                                                                BBNNN          JOSHUA                             ALBERT                             LYNCHBURG           S***UPPR            VA24501                            NNNN            .00 000000000.00                                                                    .00                                                 .00                              .00                    .00             000000000.00 000000000.002019-08-02-14.25.13.214255 ");
		iwList.add(iw);
		iw = new MACSSIntegrationWrapper();
		iw.setMessageString("MA                                            EXPALERTDOPT-TEXT       02000939898020009396                 6145981329                                                                                                                                                                                                                                                                                                                      moba_999@aep.com                                                                                                                                                                                                                                                                                                                BBNNN          JOSHUA                             ALBERT                             LYNCHBURG           S***UPPR            VA24501                            NNNN            .00 000000000.00                                                                    .00                                                 .00                              .00                    .00             000000000.00 000000000.002019-08-02-14.25.13.214255");
		iwList.add(iw);
		
		Load2S3BillingService.Load2s3byAlert(iwList);
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}

}
