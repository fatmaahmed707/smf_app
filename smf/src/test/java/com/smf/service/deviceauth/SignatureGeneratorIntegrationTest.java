package com.smf.service.deviceauth;

import static org.junit.jupiter.api.Assertions.*;

import com.smf.model.SmfDevice;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.repo.SmfDeviceRepository;
import com.smf.util.EncryptionUtil;
import com.smf.util.HmacUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SignatureGeneratorIntegrationTest {

    @Autowired
    private SmfDeviceRepository smfDeviceRepository;

    @Autowired
    private RegisteredDeviceRepository registeredDeviceRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private DeviceAuthService deviceAuthService;

    @Test
    void verifyDevice_withSeededDataFromDb() throws InvalidKeyException, NoSuchAlgorithmException {
        String mac = "28:56:2F:4A:87:6C";
        long timestamp = Instant.now().getEpochSecond();

        Optional<SmfDevice> smfDeviceOpt = smfDeviceRepository.findByMacAddress(mac);
        assertTrue(smfDeviceOpt.isPresent(), "SMF device should be seeded");

        SmfDevice smfDevice = smfDeviceOpt.get();
        String decryptedSecret = encryptionUtil.decrypt(smfDevice.getSecret());

        String signature = HmacUtil.computeSignature(mac, timestamp, decryptedSecret);

        boolean result = deviceAuthService.verifyDevice(mac, timestamp, signature);

        assertTrue(result, "Signature verification should succeed with seeded data");
    }

    @Test
    void verifyDevice_invalidSignatureShouldFail() throws InvalidKeyException, NoSuchAlgorithmException {
        String mac = "28:56:2F:4A:87:6C";
        long timestamp = Instant.now().getEpochSecond();
        String invalidSignature = "invalid-signature";

        boolean result = deviceAuthService.verifyDevice(mac, timestamp, invalidSignature);

        assertFalse(result, "Invalid signature should fail verification");
    }
}