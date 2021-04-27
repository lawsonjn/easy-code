package com.developcollect.easycode.provider;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.ui.SyncConfTextField;
import com.developcollect.easycode.utils.UIUtil;
import org.bouncycastle.crypto.engines.SM2Engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/26 9:10
 */
public class EncodeProvider implements Provider {

    private Map<String, Codec> supportEncoder = new LinkedHashMap<>();

    {
        supportEncoder.put("MD5", new Codec() {
            @Override
            public String encode(String str) {
                return SecureUtil.md5(str);
            }

            @Override
            public String decode(String str) {
                throw new IllegalStateException("MD5无法解码");
            }
        });
        supportEncoder.put("Base64", new Codec() {
            @Override
            public String encode(String str) {
                return Base64.encode(str);
            }

            @Override
            public String decode(String str) {
                return Base64.decodeStr(str);
            }
        });
        supportEncoder.put("URL", new Codec() {
            @Override
            public String encode(String str) {
                return URLUtil.encodeAll(str);
            }

            @Override
            public String decode(String str) {
                return URLUtil.decode(str);
            }
        });
        supportEncoder.put("URL_Q", new Codec() {
            @Override
            public String encode(String str) {
                return URLUtil.encodeQuery(str);
            }

            @Override
            public String decode(String str) {
                return URLUtil.decode(str);
            }
        });
        supportEncoder.put("SM2-123", new Codec() {
            @Override
            public String encode(String str) {
                return SmUtil.sm2(EASY_CODE_CONFIG.getSm2PrivateKey(), EASY_CODE_CONFIG.getSm2PublicKey()).setMode(SM2Engine.Mode.C1C2C3).encryptHex(str, KeyType.PublicKey);
            }

            @Override
            public String decode(String str) {
                return SmUtil.sm2(EASY_CODE_CONFIG.getSm2PrivateKey(), EASY_CODE_CONFIG.getSm2PublicKey()).setMode(SM2Engine.Mode.C1C2C3).decryptStr(str, KeyType.PrivateKey);
            }
        });
        supportEncoder.put("SM2-132", new Codec() {
            @Override
            public String encode(String str) {
                return SmUtil.sm2(EASY_CODE_CONFIG.getSm2PrivateKey(), EASY_CODE_CONFIG.getSm2PublicKey()).setMode(SM2Engine.Mode.C1C3C2).encryptHex(str, KeyType.PublicKey);
            }

            @Override
            public String decode(String str) {
                return SmUtil.sm2(EASY_CODE_CONFIG.getSm2PrivateKey(), EASY_CODE_CONFIG.getSm2PublicKey()).setMode(SM2Engine.Mode.C1C3C2).decryptStr(str, KeyType.PrivateKey);
            }
        });
        supportEncoder.put("SM3", new Codec() {
            @Override
            public String encode(String str) {
                return SmUtil.sm3(str);
            }

            @Override
            public String decode(String str) {
                throw new IllegalStateException("SM3无法解码");
            }
        });
    }

    @Override
    public int order() {
        return 3000;
    }

    @Override
    public String getTitle() {
        return "转码";
    }

    @Override
    public Component getComponent(JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));


        JTextArea textArea1 = new JTextArea();
        textArea1.setLineWrap(true);
        JScrollPane sp1 = new JScrollPane(textArea1);
        textArea1.setText("123456");

        JTextArea textArea2 = new JTextArea();
        textArea2.setLineWrap(true);
        JScrollPane sp2 = new JScrollPane(textArea2);


        JPanel toolPanel = new JPanel(new GridBagLayout());
        JButton encodeBtn = new JButton("编码 >");
        JButton decodeBtn = new JButton("< 解码");
        JComboBox<String> encodeComboBox = new JComboBox<>(supportEncoder.keySet().toArray(new String[0]));
        encodeComboBox.setSelectedIndex(4);

        UIUtil.bag(toolPanel, encodeBtn, 0, 1, 0, 0);
        UIUtil.bag(toolPanel, decodeBtn, 0, 1, 0, 0);
        UIUtil.bag(toolPanel, encodeComboBox, 0, 1, 0, 0);

        addEncodeListener(encodeBtn, encodeComboBox, textArea1, textArea2);
        addDecodeListener(decodeBtn, encodeComboBox, textArea1, textArea2);

        SyncConfTextField publicKeyIn = new SyncConfTextField(EASY_CODE_CONFIG::getSm2PublicKey);
        publicKeyIn.setHintText("公钥");
        SyncConfTextField privateKeyIn = new SyncConfTextField(EASY_CODE_CONFIG::getSm2PrivateKey);
        privateKeyIn.setHintText("私钥");

        UIUtil.bag(panel, publicKeyIn, 0, 1, 1, 0);
        UIUtil.bag(panel, privateKeyIn, 0, 1, 1, 0);
        UIUtil.bag(panel, sp1, 4, 1, 1, 1);
        UIUtil.bag(panel, toolPanel, 1, 1, 0, 1);
        UIUtil.bag(panel, sp2, 4, 1, 1, 1);

        return panel;
    }


    private void addEncodeListener(JButton encodeBtn, JComboBox<String> encodeComboBox, JTextArea t1, JTextArea t2) {
        encodeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String content = t1.getText();
                if (StrUtil.isBlank(content)) {
                    return;
                }

                String selectedItem = (String) encodeComboBox.getSelectedItem();
                String encodeText = supportEncoder.get(selectedItem).encode(content);
                t2.setText(encodeText);
            }
        });
    }

    private void addDecodeListener(JButton decodeBtn, JComboBox<String> encodeComboBox, JTextArea t1, JTextArea t2) {
        decodeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String content = t2.getText();
                if (StrUtil.isBlank(content)) {
                    return;
                }

                String selectedItem = (String) encodeComboBox.getSelectedItem();
                try {
                    String decodeText = supportEncoder.get(selectedItem).decode(content);
                    t1.setText(decodeText);
                } catch (Exception e1) {

                    UIUtil.showInfo(e1.getMessage());
                }
            }
        });

    }

    private interface Codec {

        String encode(String str);

        String decode(String str);
    }
}
