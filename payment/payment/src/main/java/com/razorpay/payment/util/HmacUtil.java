package com.razorpay.payment.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HmacUtil {

    public static String generateHmac(String secret,String payload){
        try {
          Mac hmac256 =   Mac.getInstance("HmacSHA256");
         SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(),"HmacSHA256");

         hmac256.init(secretKeySpec);

         byte[] hash = hmac256.doFinal(payload.getBytes());

         return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
