package org.cloudfoundry.identity.uaa.zone;

import org.cloudfoundry.identity.uaa.util.KeyWithCert;
import org.springframework.util.StringUtils;

import java.security.GeneralSecurityException;
import java.util.Map;

/*******************************************************************************
 * Cloud Foundry
 * Copyright (c) [2009-2015] Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 * <p>
 * This product includes a number of subcomponents with
 * separate copyright notices and license terms. Your use of these
 * subcomponents is subject to the terms and conditions of the
 * subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/
public class GeneralIdentityZoneConfigurationValidator implements IdentityZoneConfigurationValidator {
    @Override
    public IdentityZoneConfiguration validate(IdentityZoneConfiguration config, Mode mode) throws InvalidIdentityZoneConfigurationException {
        if(mode ==  Mode.CREATE || mode == Mode.MODIFY) {
            try {
                SamlConfig samlConfig;
                if ((samlConfig = config.getSamlConfig()) != null) {
                    String samlSpCert = samlConfig.getCertificate();
                    String samlSpKey = samlConfig.getPrivateKey();
                    String samlSpkeyPassphrase = samlConfig.getPrivateKeyPassword();
                    if (samlSpKey != null && samlSpCert != null) {
                        KeyWithCert keyWithCert = new KeyWithCert(samlSpKey, samlSpkeyPassphrase, samlSpCert);
                    }
                }
            } catch(GeneralSecurityException ex) {
                throw new InvalidIdentityZoneConfigurationException("There is a security problem with the SAML SP configuration.", ex);
            }

            TokenPolicy tokenPolicy = config.getTokenPolicy();
            if(tokenPolicy != null) {
                String activeKeyId = tokenPolicy.getActiveKeyId();
                Map<String, String> jwtKeys = tokenPolicy.getKeys();
                if(jwtKeys != null) {
                    if(StringUtils.hasText(activeKeyId)) {
                        if(!jwtKeys.containsKey(activeKeyId)) {
                            throw new InvalidIdentityZoneConfigurationException("The specified primary key ID is not present in the configured keys: " + activeKeyId, null);
                        }
                    } else {
                        if(jwtKeys.size() > 1) {
                            throw new InvalidIdentityZoneConfigurationException("Multiple token signing keys are specified, but none is specified to be the primary key.", null);
                        }
                    }
                } else if(StringUtils.hasText(activeKeyId)) {
                    throw new InvalidIdentityZoneConfigurationException("Identity zone cannot specify primary key ID with no zone keys configured.", null);
                }
            }
        }

        return config;
    }
}
