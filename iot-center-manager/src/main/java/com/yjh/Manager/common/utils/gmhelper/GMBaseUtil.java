package com.yjh.manager.common.utils.gmhelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
public class GMBaseUtil {
	static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
