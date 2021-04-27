package com.developcollect.easycode.provider;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileTypeUtil;
import com.developcollect.core.swing.explorer.ExplorerUtil;
import com.developcollect.core.utils.DateUtil;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.core.utils.StrUtil;
import com.developcollect.extra.qrcode.QrCodeUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.utils.UIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/25 15:58
 */
public class QrCodeProvider implements Provider {

    private static final Logger log = LoggerFactory.getLogger(QrCodeProvider.class);

    private static final int DEFAULT_IMAGE_LEN = 230;

    @Override
    public int order() {
        return 4000;
    }

    public static void main(String[] args) {


        QrCodeProvider qrCodeProvider = new QrCodeProvider();
        JFrame jFrame = new JFrame();
        jFrame.add(qrCodeProvider.getComponent(jFrame), BorderLayout.CENTER);


        jFrame.setBounds(0, 0, 800, 500);
        jFrame.setLocationRelativeTo(null);
        jFrame.setIconImage(UIUtil.getImage("img/ic16.png"));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    @Override
    public String getTitle() {
        return "二维码";
    }

    @Override
    public Component getComponent(JFrame frame) {

        // 右边
        JLabel qrCodeLabel = new JLabel("此处生成二维码");
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel imagePanel = new JPanel(new BorderLayout());


        imagePanel.add(qrCodeLabel, BorderLayout.CENTER);
        imagePanel.setPreferredSize(new Dimension(DEFAULT_IMAGE_LEN + 2, DEFAULT_IMAGE_LEN + 2));
        imagePanel.setMinimumSize(new Dimension(DEFAULT_IMAGE_LEN + 2, DEFAULT_IMAGE_LEN + 2));
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));

        JComboBox<BarcodeFormat> barcodeFormatComboBox = new JComboBox<>();




        JPanel qrCodePanel = new JPanel();
        qrCodePanel.setLayout(new GridBagLayout());
        UIUtil.bag(qrCodePanel, imagePanel, 0, 1, 0, 0);
        UIUtil.bag(qrCodePanel, barcodeFormatComboBox, 0, 1, 0, 0);
        UIUtil.bag(qrCodePanel, Box.createVerticalGlue(), 1, 1, 0, 1);


        // 左边
        JTextArea jt = new JTextArea();
        jt.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jt);

        JButton generateQrCodeBtn = new JButton("生成二维码");
        generateQrCodeBtn.setPreferredSize(new Dimension(10, 50));
        generateQrCodeBtn.setMinimumSize(new Dimension(10, 50));
        JButton saveQrCodeBtn = new JButton("保存");
        saveQrCodeBtn.setEnabled(false);



        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));

        UIUtil.bag(panel, jsp, 10, 5, 1, 1);
        UIUtil.bag(panel, qrCodePanel, 0, 5, 0, 0);

        UIUtil.bag(panel, generateQrCodeBtn, 8, 5, 1, 0);
        UIUtil.bag(panel, saveQrCodeBtn, 2, 5, 0, 0);


        initDecodeBarImage(frame, panel, jt, qrCodeLabel, saveQrCodeBtn);
        initBarcodeFormatComboBox(barcodeFormatComboBox, jt);
        addSaveImageListener(saveQrCodeBtn, qrCodeLabel);
        addGenerateQrCodeListener(generateQrCodeBtn, jt, qrCodeLabel, barcodeFormatComboBox, saveQrCodeBtn);

        return panel;
    }


    private void addGenerateQrCodeListener(JButton generateQrCodeBtn, JTextArea jt, JLabel qrCodeLabel, JComboBox<BarcodeFormat> barcodeFormatComboBox, JButton saveQrCodeBtn) {
        generateQrCodeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                String text = jt.getText();
                if (StrUtil.isBlank(text)) {
                    UIUtil.showInfo("请输入内容");
                    return;
                }
                try {
                    qrCodeLabel.setText("");
                    BarcodeFormat barcodeFormat = (BarcodeFormat) barcodeFormatComboBox.getSelectedItem();
                    if (barcodeFormat == null) {
                        barcodeFormat = BarcodeFormat.QR_CODE;
                    }
                    int width = DEFAULT_IMAGE_LEN;
                    int height = DEFAULT_IMAGE_LEN;

                    if (QrCodeUtil.is1dBarcode(barcodeFormat)) {
                        height = 98;
                    }

                    BufferedImage image = QrCodeUtil.generate(text, barcodeFormat, width, height);
                    qrCodeLabel.setIcon(new ImageIcon(image));
                    saveQrCodeBtn.setEnabled(true);
                } catch (Exception e) {
                    if (e.getMessage().contains("Data too big")) {
                        UIUtil.showError("文字太长");
                    } else {
                        UIUtil.showError("生成错误");
                    }
                    log.error("生成二维码错误", e);
                }
            }
        });
    }

    private void addSaveImageListener(JButton saveQrCodeBtn, JLabel qrCodeLabel) {
        saveQrCodeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = (ImageIcon) qrCodeLabel.getIcon();
                Image image = icon.getImage();
                String saveDir = EASY_CODE_CONFIG.getProductDir();
                File file = new File(StrUtil.format("{}/{}.jpg", saveDir, DateUtil.format(LocalDateTime.now(), "yyMMddHHmmss")));
                FileUtil.mkParentDirs(file);
                ImgUtil.write(image, file);
                ExplorerUtil.select(file);
            }
        });
    }

    private void initDecodeBarImage(JFrame frame, JPanel panel, JTextArea jt, JLabel qrCodeLabel, JButton saveBtn) {
        // 点击二维码图片，实现解码
        qrCodeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = (ImageIcon) qrCodeLabel.getIcon();
                String decode = QrCodeUtil.decode(icon.getImage());
                jt.setText(decode);
            }
        });

        DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> list = (List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        File file = list.get(0);
                        String type = FileTypeUtil.getType(file);
                        if ("jpg".equals(type) || "png".equals(type)) {
                            BufferedImage image = ImgUtil.read(file);
                            Result result = QrCodeUtil.decodeResult(image, true, false);
                            String decode = result.getText();
                            int height = DEFAULT_IMAGE_LEN;
                            if (QrCodeUtil.is1dBarcode(result.getBarcodeFormat())) {
                                height = 98;
                            }
                            jt.setText(decode);
                            qrCodeLabel.setText("");
                            qrCodeLabel.setIcon(new ImageIcon(UIUtil.zoomImage(image, DEFAULT_IMAGE_LEN, height)));
                            saveBtn.setEnabled(true);
                        }
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    UIUtil.showWarn(frame, "无法解析二维码，请确保文件格式正确！");
                    log.error("无法解析二维码", e);
                }
            }
        };
        UIUtil.addDragListener(panel, dropTargetAdapter);
        UIUtil.addDragListener(jt, dropTargetAdapter);
        UIUtil.addDragListener(qrCodeLabel, dropTargetAdapter);
    }


    private void initBarcodeFormatComboBox(JComboBox<BarcodeFormat> barcodeFormatComboBox, JTextArea jt) {

        Map<BarcodeFormat, String> tipMap = new LinkedHashMap<>();
        // 二维码
        tipMap.put(BarcodeFormat.QR_CODE, "QR_CODE\n1.数字型数据（数字0～9）\n2.字母型数据（大写字母A～Z, 9个其他字符：space,$,%,*,+,-,.,/,:）\n3.8位字节型数据\n4.中国汉字字符（GB 2312对应的汉字和非汉字字符）");
        tipMap.put(BarcodeFormat.DATA_MATRIX, "DATA_MATRIX\n可编码字元集包括全部的ASCII字元及扩充ASCII字元，共256个字元");
        tipMap.put(BarcodeFormat.MAXICODE, "MAXICODE\n允许对256个国际字符编码，\n包括值0~127的ASCII字元和128~255的扩展ASCII字元。\n在数字组合模式下，可用6个字码表示9位数字。\n用於代码切换和其他控制字元也包括在其字元集中。");
        tipMap.put(BarcodeFormat.AZTEC, "AZTEC\n所有8位的值都可编码，另外加上两个转义代码\n默认情况下，0-127的码遵循ANSI*3.4（ASCII）解释\n128-255遵循ISO 8859-1：Latin AIphabet No.1解释，这对应ECI 000003");
        tipMap.put(BarcodeFormat.PDF_417, "PDF_417\nPDF417条码可表示数字、字母或二进制数据，也可表示汉字。\n一个PDF417条码最多可容纳1850 个字符或1108 个字节的二进制数据");
        // 一维码
        tipMap.put(BarcodeFormat.CODABAR, "CODABAR\n可以用数字（0至9）、字母（A、B、C、D）以及符号（-、$、/、.、+）来表示字符。");
        tipMap.put(BarcodeFormat.CODE_39, "CODE_39\n只接受如下43个有效输入字符：\n26个大写字母（A - Z），\n十个数字（0 - 9），\n连接号(-),句号（.）,空格,美圆符号($),斜扛(/),加号(+)以及百分号(%)");
        tipMap.put(BarcodeFormat.CODE_93, "CODE_93\nCODE_93和CODE_39具有相同的字符集：\n26个大写字母（A - Z），\n十个数字（0 - 9），\n连接号(-),句号（.）,空格,美圆符号($),斜扛(/),加号(+)以及百分号(%)");
        tipMap.put(BarcodeFormat.CODE_128, "CODE_128\n有三种不同的版本：A（数字、大写字母、控制字符）B（数字、大小字母、字符）C（双位数字）\nCODE128A:标准数字和大写字母,控制符,特殊字符\nCODE128B:标准数字和大写字母,小写字母,特殊字符\nCODE128C:[00]-[99]的数字对集合,共100个");
        tipMap.put(BarcodeFormat.EAN_8, "EAN_8\n具有以下特点：\nEAN_8只能输入数字，码共8位数，包括国别码2位，产品代码5位，及检查码1位。");
        tipMap.put(BarcodeFormat.EAN_13, "EAN_13\nEAN-13条码被划分成了4个区域：\n1）数制\n2）厂商码\n3）商品码\n4）校验位");
        tipMap.put(BarcodeFormat.ITF, "ITF\nITF条码，又称交叉二五条码，主要用于运输包装，是印刷条件较差，不允许印刷EAN-13和UPC-A条码时应选用的一种条码。");
        tipMap.put(BarcodeFormat.RSS_14, "RSS_14");
        tipMap.put(BarcodeFormat.RSS_EXPANDED, "RSS_EXPANDED");
        tipMap.put(BarcodeFormat.UPC_A, "UPC A码具有以下特点：\n" +
                " 1.每个字码皆由7个模组组合成2线条2空白，其逻辑值可用7个二进制数字表示，例如逻辑值0001101代表数字1，逻辑值0为空白，1为线条，故数字1的UPC-A码为粗空白(000)-粗线条(11)-细空白(0)-细线条(1)。\n" +
                " 2.从空白区开始共113个模组，每个模组长0.33mm，条码符号长度为37.29mm。\n" +
                " 3.中间码两侧的资料码编码规则是不同的，左侧为奇，右侧为偶。奇表示线条的个数为奇数；偶表示线条的个数为偶数。\n" +
                " 4.起始码、终止码、中间码的线条高度长於数字码。\n"
        );
        tipMap.put(BarcodeFormat.UPC_E, "UPC_E\n在特定条件下，12位的UPC-A条码可以被表示为一种缩短形式的条码符号即UPC-E条码。UPC-E不同于EAN-13和UPC-A商品条码，也不同于EAN-8，它不含中间分隔符，由左侧空白区、起始符、数据符、终止符、右侧空白区及供人识别字符组成。");
        tipMap.put(BarcodeFormat.UPC_EAN_EXTENSION, "UPC_EAN_EXTENSION");

        for (Map.Entry<BarcodeFormat, String> entry : tipMap.entrySet()) {
            barcodeFormatComboBox.addItem(entry.getKey());
        }


        barcodeFormatComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String text = jt.getText();
                    if (StrUtil.isBlank(text) || tipMap.containsValue(text)) {
                        BarcodeFormat selectedItem = (BarcodeFormat) barcodeFormatComboBox.getSelectedItem();
                        jt.setText(tipMap.get(selectedItem));
                    }
                }
            }
        });


        barcodeFormatComboBox.setSelectedItem(BarcodeFormat.QR_CODE);
        jt.setText(tipMap.get(BarcodeFormat.QR_CODE));
    }


}
