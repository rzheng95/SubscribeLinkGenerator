package com.rzheng.subscribelinkgenerator.controller;

import com.rzheng.subscribelinkgenerator.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Richard
 */
@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public static final String DEFAULT_REMARKS = "%E4%B8%AA%E4%BA%BA%E7%8B%AC%E4%BA%AB";

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String getLink() {
        return "Hello World!";
    }

    /**
     * 1. remarks and contactInfo must be URL encoded
     * 2. Multiple contact info should be separated by semicolon
     *
     * @param days number of days for the subscription before it expires
     * @param remarks remarks for the subscription
     * @param contactInfo contact info for the subscription
     * @return a subscribe like with remarks and contact info
     */
    @GetMapping("/link/{days}")
    public String getSubscribeLinkWithRemarksAndContactInfo(@PathVariable int days,
                                                            @RequestParam(name = "urlEncodedRemarks", required = false) String remarks,
                                                            @RequestParam(name = "urlEncodedContactInfo", required = false) String contactInfo) throws Exception {
        // Controller reads the urlEncodedRemarks and urlEncodedContactInfo and decodes them by default.
        return this.subscriptionService.getSubscribeLinkWithRemarksAndContactInfo(
                days,
                remarks == null ? DEFAULT_REMARKS : remarks,
                contactInfo == null ? "" : contactInfo);
    }


    // subKey must be url encoded
    @GetMapping("/sub-link")
    public String getProxiesBySubKey(@RequestParam("subKey") String subKey) throws IOException {
        // Controller reads the url encoded sub key and decodes it by default.
        return this.subscriptionService.getProxiesBySubKey(subKey);
    }
}
