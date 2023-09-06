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
    private String processSubscription(int days) throws Exception {
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

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, days);
            subscription.setExpiration(cal.getTime());
            this.subscriptionRepository.save(subscription);
            return subscription.getSubKey();
        }
    }

    public String getSubscribeLink(int days) throws Exception {
        // 个人独享
        final String defaultRemarks = "%E4%B8%AA%E4%BA%BA%E7%8B%AC%E4%BA%AB";
        return this.getSubscribeLinkWithRemarks(days, defaultRemarks);
    }

    public String getSubscribeLinkWithRemarks(int days, String urlEncodedRemarks) throws Exception {
        return this.assembleSubscribeLink(this.processSubscription(days), urlEncodedRemarks);
    }

    /**
     * Assemble the subscribe link.
     * sub://<base64 encoded subscribe link>#<url encoded remarks>
     *
     * @param subKey            subscription key
     * @param urlEncodedRemarks url encoded remarks
     * @return a full subscribe link
     */
    private String assembleSubscribeLink(String subKey, String urlEncodedRemarks) {
        String subLink = "http://" + ipAddress + "/sub-link/" + subKey;
        String base64EncodedSubLink = Base64.getEncoder().encodeToString(subLink.getBytes(StandardCharsets.UTF_8));
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

            return this.readFileAndEncodeToBase64(masterLinksFilePath);
        }
        log.info("Subscription {} NOT found!", urlEncodedSubKey);
        return "";
    }


    private String readFileAndEncodeToBase64(String filePath) throws IOException {
        // Read all bytes from the file
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // Encode the bytes to Base64
        return Base64.getEncoder().encodeToString(fileContent);
    }
}