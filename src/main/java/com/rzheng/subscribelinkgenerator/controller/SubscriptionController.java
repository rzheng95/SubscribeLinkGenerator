package com.rzheng.subscribelinkgenerator.controller;

import com.rzheng.subscribelinkgenerator.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Richard
 */
@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String getLink() {
        return "Hello World!";
    }

    @GetMapping("/link/{days}")
    public String getSubscribeLink(@PathVariable int days) throws Exception {
        return this.subscriptionService.getSubscribeLink(days);
    }

    @GetMapping("/link/{days}/{urlEncodedRemarks}")
    public String getSubscribeLinkWithRemarks(@PathVariable int days, @PathVariable String urlEncodedRemarks) throws Exception {
        return this.subscriptionService.getSubscribeLinkWithRemarks(days, urlEncodedRemarks);
    }

    // subKey must be url encoded
    @GetMapping("/sub-link/{subKey}")
    public String getProxiesBySubKey(@PathVariable String subKey) throws IOException {
        // Controller reads the url encoded sub key and decode it by default.
        return this.subscriptionService.getProxiesBySubKey(subKey);
    }
}
