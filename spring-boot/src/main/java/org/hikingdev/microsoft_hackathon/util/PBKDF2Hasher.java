package org.hikingdev.microsoft_hackathon.util;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PBKDF2Hasher {
    public static String generateKey(String password, String salt) throws UnsupportedEncodingException {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(), 600000);
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
        return Base64.getEncoder().encodeToString(dk);
    }
}
