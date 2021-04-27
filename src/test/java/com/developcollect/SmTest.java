package com.developcollect;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.github.javafaker.Faker;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.util.Locale;

public class SmTest {


    public static final String SM2_PUBLIC_KEY = "04b922a18b5b46ee5498751a4f8a9641954031b6eda407657682c42a5e028a83f6af68fb06cb403399f1e8ac818d08e42ae3219e5dae5b1f48df4442de3cb95a64";
    public static final String SM2_PRIVATE_KEY = "0081ac0cee8cf46688a3996eb2b26917f95e5e5abe2ebd68c0f58ea30c7e00597c";

    public static void main(String[] args) {
//        createSm2KeyTest();
//        sm2Test();
//        sm3Test();
        sss();
    }


    public static void sm2Test() {
        SM2 sm2 = SmUtil.sm2(SM2_PRIVATE_KEY, SM2_PUBLIC_KEY);
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        sm2.setEncoding(new PlainDSAEncoding());

        String s = sm2.decryptStr("04b3d741c1602767b03642deefd26b2b60d52b599c8c3b641df3f4ac501d9fe5c2e1948e4a803d49c831b054dcc36c2b96a8b884a3354d842108d47273ef7a47a5b931233fed3ea0ebbe89f0837149f9cd75a00da1bcc0f59cc3663878a28efb969b8f26d957e5", KeyType.PrivateKey);
        System.out.println(s);
        String d = sm2.decryptStr("0486ccaef6498a5c0f53c7b9d961da5f4e691d3aac86e3b8a8ad46c5d37df151acadadbab193c4cb3ca062146083bd0b1f0854c9393f5503fb72e65e8fd2ed9717cafbcf399aab63d5b91b929f73b5cc70b762a7b109793b09fe6d7be9e9ded9f0310069f61bdd", KeyType.PrivateKey);
        System.out.println(d);
    }

    public static void createSm2KeyTest() {
        //需要加密的明文
        String text = "我是一段测试aaaa";
        //创建sm2 对象
        SM2 sm2 = SmUtil.sm2();
        //这里会自动生成对应的随机秘钥对 , 注意！ 这里一定要强转，才能得到对应有效的秘钥信息
        byte[] privateKey = BCUtil.encodeECPrivateKey(sm2.getPrivateKey());
        //这里公钥不压缩  公钥的第一个字节用于表示是否压缩  可以不要
        byte[] publicKey = ((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(false);
        //这里得到的 压缩后的公钥   ((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(true);
        // byte[] publicKeyEc = BCUtil.encodeECPublicKey(sm2.getPublicKey());
        //打印当前的公私秘钥
        System.out.println("私钥: " + HexUtil.encodeHexStr(privateKey));
        System.out.println("公钥: " + HexUtil.encodeHexStr(publicKey));
        //得到明文对应的字节数组
        byte[] dateBytes = text.getBytes();
        System.out.println("数据: " + HexUtil.encodeHexStr(dateBytes));
        //这里需要手动设置，sm2 对象的默认值与我们期望的不一致
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        sm2.setEncoding(new PlainDSAEncoding());
        //计算签名
        byte[] sign = sm2.sign(dateBytes, null);
        System.out.println("签名: " + HexUtil.encodeHexStr(sign));
        // 校验  验签
        boolean verify = sm2.verify(dateBytes, sign);
        System.out.println(verify);

        String encryptHex = sm2.encryptHex("123456", KeyType.PublicKey);
        System.out.println("加密：" + encryptHex);
        String decryptStr = sm2.decryptStr(encryptHex, KeyType.PrivateKey);
        System.out.println("解密：" + decryptStr);
    }


    public static void sss() {
        SM2 sm2 = SmUtil.sm2("00ED5BC2B0A15FD60828BCDCEE03423A538DDA70E4B3D3754CAD5828689AB6ADDD", null);
        sm2.setMode(SM2Engine.Mode.C1C2C3);
        sm2.setEncoding(new PlainDSAEncoding());

        String s = sm2.decryptStr("04D3AF041029CAA0025985D7A700D7D33CEC635FD60FFF9A1A0950766345FA2E0E397DC23E320883DF971CA5807772325F30AA85A78A4FCD89ABD1CC3C026BAAAF5C05B7C37D1B196C4A41B8B764D6AEEEB5ABBBCBB98BD34955DD0DD36EC659CCAEC200148F1D", KeyType.PrivateKey);
        System.out.println(s);
    }

    public static void sm3Test() {
        System.out.println(SmUtil.sm3("haifeisi"));
    }


    public void testFaker() {
        Faker faker = new Faker(new Locale("zh_CN"));
    }
}
