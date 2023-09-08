package com.rzheng.subscribelinkgenerator.service;

import com.rzheng.subscribelinkgenerator.entity.Subscription;
import com.rzheng.subscribelinkgenerator.repository.SubscriptionRepository;
import com.rzheng.subscribelinkgenerator.util.AESEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * @author Richard
 */
@Service
public class SubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    @Value(value = "${app.master.links.file.path}")
    private String masterLinksFilePath;

    @Value(value = "${app.ip.address}")
    private String ipAddress;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Use (today's date in MM/dd/yyyy + : + days) format, and encrypt it using AES, then url encode it.
     * Ex. 9/4/2023:60.txt => ratqboK45idJHoQtbyXnqw== => ratqboK45idJHoQtbyXnqw%3D%3D
     *
     * @param days number of days for the subscription before it expires
     * @return a URL encoded ASE encrypted subscription key
     */
    private String processSubscription(int days, String contactInfo) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String today = sdf.format(new Date());
        log.info("Encrypting {}:{}", today, days);
        String encryptKey = AESEncryption.encrypt(today + ":" + days);
        log.info("Encrypted key: {}", encryptKey);
        String urlEncodedEncryptedKey = URLEncoder.encode(encryptKey, StandardCharsets.UTF_8);
        log.info("URL encoded encrypted key: {}", urlEncodedEncryptedKey);


        Optional<Subscription> optionalLink = this.subscriptionRepository.findBySubKey(urlEncodedEncryptedKey);

        if (optionalLink.isPresent()) {
            Subscription subscription = optionalLink.get();
            log.info("Subscription {} found!", subscription.getSubKey());
            return subscription.getSubKey();
        } else {
            log.info("Subscription {} NOT found! Creating new subscription...", urlEncodedEncryptedKey);
            Subscription subscription = new Subscription();
            subscription.setSubKey(urlEncodedEncryptedKey);
            subscription.setContactInfo(contactInfo);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, days);
            subscription.setExpiration(cal.getTime());

            this.subscriptionRepository.save(subscription);
            return subscription.getSubKey();
        }
    }

    public String getSubscribeLinkWithRemarksAndContactInfo(int days, String remarks, String contactInfo) throws Exception {
        String subKey = this.processSubscription(days, contactInfo);
        String urlEncodedRemark = URLEncoder.encode(remarks, StandardCharsets.UTF_8);
        return this.assembleSubscribeLink(subKey, urlEncodedRemark);
    }

    /**
     * Assemble the subscribe link.
     * sub://<base64 encoded subscribe link>#<url encoded remarks>
     *
     * @param urlEncodedSubKey  subscription key
     * @param urlEncodedRemarks url encoded remarks
     * @return a full subscribe link
     */
    private String assembleSubscribeLink(String urlEncodedSubKey, String urlEncodedRemarks) {
        String port = "8080";
        String subLink = "http://" + ipAddress + ":" + port + "/sub-link?subKey=" + urlEncodedSubKey;
        String base64EncodedSubLink = this.base64Encode(subLink);
        return "sub://" + base64EncodedSubLink + "#" + urlEncodedRemarks;
    }

    public String getProxiesBySubKey(String subKey) throws IOException {

        // Check if urlEncodedSubKey is valid
        String urlEncodedSubKey = URLEncoder.encode(subKey, StandardCharsets.UTF_8);
        Optional<Subscription> optionalLink = this.subscriptionRepository.findBySubKey(urlEncodedSubKey);

        if (optionalLink.isPresent()) {
            log.info("Subscription {} found!", urlEncodedSubKey);

            // check if subscription is expired
            Subscription subscription = optionalLink.get();
            Date expiration = subscription.getExpiration();
            Date today = new Date();
            if (today.after(expiration)) {
                log.info("Subscription {} is expired!", urlEncodedSubKey);
                return "";
            }

            StringBuilder fileContent = new StringBuilder(Files.readString(Paths.get(masterLinksFilePath)));
            String contactInfo = subscription.getContactInfo();

            if (contactInfo != null && !contactInfo.isEmpty()) {
                String[] contactInfoArray = contactInfo.split(";");
                for (String contact : contactInfoArray) {
                    String base64EncodedContact = this.base64Encode(contact);
                    String dummyLink = "159.65.237.131:9000:auth_chain_b:aes-256-cfb:tls1.2_ticket_auth:cGFzc3dvcmQ=remarks=" + base64EncodedContact;
                    String base64EncodedDummyLink = this.base64Encode(dummyLink);
                    fileContent.insert(0, "ssr://" + base64EncodedDummyLink + "\n");
                }
            }

            return this.base64Encode(fileContent.toString());
        }
        log.info("Subscription {} NOT found!", urlEncodedSubKey);
        return "";
    }

    private String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}





















