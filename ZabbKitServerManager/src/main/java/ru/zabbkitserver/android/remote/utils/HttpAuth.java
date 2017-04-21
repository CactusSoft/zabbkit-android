package ru.zabbkitserver.android.remote.utils;

import android.support.v4.util.ArrayMap;
import android.util.Base64;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.zabbkitserver.android.remote.client.ConnectionConfigurator;
import ru.zabbkitserver.android.remote.client.JSONRPC2SessionException;
import ru.zabbkitserver.android.remote.client.JSONRPC2SessionOptions;

public class HttpAuth {

    private final String TYPE_DIGEST = "digest";
    private final String TYPE_BASIC = "basic";

    public URLConnection tryDigestAuthentication(HttpURLConnection input, String username,
                                                 String password, JSONRPC2SessionOptions options,
                                                 ConnectionConfigurator connectionConfigurator) {

        if (username == null || password == null) {
            return input;
        }

        String type;
        String auth = input.getHeaderField("WWW-Authenticate");
        if (auth == null || !auth.startsWith("Digest ")) {
            type = TYPE_BASIC;
        } else {
            type = TYPE_DIGEST;
        }

        URLConnection result = null;
        try {
            try {
                // Use proxy?
                if (options != null && options.getProxy() != null)
                    result = input.getURL().openConnection(options.getProxy());
                else
                    result = input.getURL().openConnection();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (options != null) {
                result.setConnectTimeout(options.getConnectTimeout());
                result.setReadTimeout(options.getReadTimeout());
                applyHeaders(result, options);
            }

            // Set POST mode
            result.setDoOutput(true);

            if (connectionConfigurator != null)
                connectionConfigurator.configure((HttpURLConnection) result);
            input.disconnect();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        if (TYPE_DIGEST.equals(type)) {
            final ArrayMap<String, String> authFields = splitAuthFields(auth.substring(7));
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
            Joiner colonJoiner = Joiner.on(':');
            String HA1 = null;
            try {
                md5.reset();
                String ha1str = colonJoiner.join(username,
                        authFields.get("realm"), password);
                md5.update(ha1str.getBytes("ISO-8859-1"));
                byte[] ha1bytes = md5.digest();
                HA1 = bytesToHexString(ha1bytes);
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            String HA2 = null;
            try {
                md5.reset();
                String ha2str = colonJoiner.join(input.getRequestMethod(),
                        input.getURL().getPath());
                md5.update(ha2str.getBytes("ISO-8859-1"));
                HA2 = bytesToHexString(md5.digest());
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            String HA3 = null;
            try {
                md5.reset();
                String ha3str = colonJoiner.join(HA1, authFields.get("nonce"), HA2);
                md5.update(ha3str.getBytes("ISO-8859-1"));
                HA3 = bytesToHexString(md5.digest());
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            StringBuilder sb = new StringBuilder(128);
            sb.append("Digest ");
            sb.append("username").append("=\"").append(username).append("\",");
            sb.append("realm").append("=\"").append(authFields.get("realm")).append("\",");
            sb.append("nonce").append("=\"").append(authFields.get("nonce")).append("\",");
            sb.append("uri").append("=\"").append(input.getURL().getPath()).append("\",");
            //sb.append("qop" ).append('=' ).append("auth" ).append(",");
            sb.append("response").append("=\"").append(HA3).append("\"");

            result.addRequestProperty("Authorization", sb.toString());
        }

        if (TYPE_BASIC.equals(type)) {
            StringBuilder pairBuilder = new StringBuilder();
            pairBuilder.append(username);
            pairBuilder.append(":");
            pairBuilder.append(password);
            result.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(pairBuilder.toString().getBytes(), Base64.NO_WRAP));
        }


        return result;
    }


    private ArrayMap<String, String> splitAuthFields(String authString) {
        final ArrayMap<String, String> fields = new ArrayMap<>();
        final CharMatcher trimmer = CharMatcher.anyOf("\"\t ");
        final Splitter commas = Splitter.on(',').trimResults().omitEmptyStrings();
        final Splitter equals = Splitter.on('=').trimResults(trimmer).limit(2);
        String[] valuePair;
        for (String keyPair : commas.split(authString)) {
            valuePair = Iterables.toArray(equals.split(keyPair), String.class);
            fields.put(valuePair[0], valuePair[1]);
        }
        return fields;
    }

    private final String HEX_LOOKUP = "0123456789abcdef";

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(HEX_LOOKUP.charAt((bytes[i] & 0xF0) >> 4));
            sb.append(HEX_LOOKUP.charAt((bytes[i] & 0x0F) >> 0));
        }
        return sb.toString();
    }

    private void applyHeaders(final URLConnection con, JSONRPC2SessionOptions options)
            throws JSONRPC2SessionException {

        // Expect UTF-8 for JSON
        con.setRequestProperty("Accept-Charset", "UTF-8");

        // Add "Content-Type" header?
        if (options.getRequestContentType() != null)
            con.setRequestProperty("Content-Type", options.getRequestContentType());

        // Add "Origin" header?
        if (options.getOrigin() != null)
            con.setRequestProperty("Origin", options.getOrigin());

        // Add "Accept-Encoding: gzip, deflate" header?
        if (options.enableCompression())
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");

        // Add "Cookie" headers?
        if (options.acceptCookies()) {

            StringBuilder buf = new StringBuilder();
            con.setRequestProperty("Cookie", buf.toString());
        }
    }
}