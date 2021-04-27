package com.developcollect.easycode.utils;

import cn.hutool.core.util.RuntimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * mac与厂商对应关系
 * http://standards-oui.ieee.org/oui/oui.txt
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/24 10:43
 */
public class HardwareInfoUtil {
    private static final Logger log = LoggerFactory.getLogger(HardwareInfoUtil.class);


    private volatile static String cpuCode;
    private volatile static String diskCode;
    private volatile static String macCode;


    public static String getCpuCode() {
        try {
            if (cpuCode == null) {
                synchronized (HardwareInfoUtil.class) {
                    if (cpuCode == null) {
                        String s = RuntimeUtil.execForStr("wmic cpu get ProcessorID");
                        if (s.indexOf("ProcessorId") >= 0) {
                            s = s.substring("ProcessorId".length()).trim();
                            s = s.split("\\s+")[0];
                            cpuCode = s;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("无法获取硬件信息", e);
            cpuCode = "";
        }

        return cpuCode;
    }


    public static String getDiskCode() {
        try {
            if (diskCode == null) {
                synchronized (HardwareInfoUtil.class) {
                    if (diskCode == null) {
                        String s = RuntimeUtil.execForStr("wmic diskdrive get SerialNumber");
                        if (s.indexOf("SerialNumber") >= 0) {
                            s = s.substring("SerialNumber".length()).trim();
                            s = s.split("\\s+")[0];
                            diskCode = s;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("无法获取硬件信息", e);
            diskCode = "";
        }

        return diskCode;
    }

    public static String getMacCode() {
        try {
            if (macCode == null) {
                synchronized (HardwareInfoUtil.class) {
                    if (macCode == null) {
                        String s = RuntimeUtil.execForStr("wmic nic where netconnectionid!=NULL get macaddress");
                        if (s.indexOf("MACAddress") >= 0) {
                            s = s.substring("MACAddress".length()).trim();
                            String[] split = s.split("\\s+");
                            for (String mac : split) {
                                // vmware
                                if (mac.startsWith("00:50:56") || mac.startsWith("00:1C:14")) {
                                    continue;
                                }
                                macCode = mac;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("无法获取硬件信息", e);
            macCode = "";
        }

        return macCode;
    }




}
