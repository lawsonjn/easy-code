package com.developcollect.easycode.provider.fakedata;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.developcollect.core.utils.CollectionUtil;
import com.github.javafaker.Faker;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.core.db.DbInfo;
import com.developcollect.easycode.core.db.TableFieldWrap;
import com.developcollect.easycode.core.db.TableInfoWrap;
import com.developcollect.easycode.core.fakedata.*;
import com.developcollect.easycode.ui.OpList;
import com.developcollect.easycode.ui.SDialog;
import com.developcollect.easycode.utils.EasyCodeUtil;
import com.developcollect.easycode.utils.UIUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/6 13:46
 */
@Slf4j
public class FakeDataProvider implements Provider {

    private static final String FILE_SUFFIX = "ecfb";

    private volatile boolean genExitFlg = false;

    private OpList<TableInfoWrap> tableListPanel = new OpList<>();
    private JComboBox<DbInfo> dbComboBox = new JComboBox<>();
    private JPanel tableContainerPanel;
    private JTextField numField;
    private JTextField batchSizeField;

    private final String tableContainerPanelDefaultKey = "";

    private JFrame frame;

    private File ecfbFile;


    @Override
    public String getTitle() {
        return "数据生成";
    }

    @Override
    public Component getComponent(JFrame frame) {
        this.frame = frame;
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel dbLabel = new JLabel("数据库：", SwingConstants.RIGHT);
        List<DbInfo> dbInfos = EASY_CODE_CONFIG.getDbInfos();
        if (!dbInfos.isEmpty()) {
            for (DbInfo dbInfo : EASY_CODE_CONFIG.getDbInfos()) {
                dbComboBox.addItem(dbInfo);
            }
            Integer idx = EASY_CODE_CONFIG.getGenFakeDataDb();
            idx = idx < dbInfos.size() ? idx : 0;
            dbComboBox.setSelectedIndex(idx);
        }
        dbComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedIndex = dbComboBox.getSelectedIndex();
                    EASY_CODE_CONFIG.setGenFakeDataDb(selectedIndex);
                    clearAllTable();
                }
            }
        });

        UIUtil.addDragListener(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> list = (List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        File file = list.get(0);
                        doOpen(file);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    UIUtil.showError(frame, "无法识别文件类型！");
                    log.error("无法识别文件类型", e);
                }
            }
        });

        JLabel numLabel = new JLabel("数量：");
        numField = new JTextField("5000");
        JLabel batchSizeLabel = new JLabel("单批数量：");
        batchSizeField = new JTextField("0");
        JCheckBox errCheckBox = new JCheckBox("忽略错误");
        errCheckBox.setSelected(true);
        JButton btn = new JButton("生成");
        genBtnListener(frame, btn);

        // 递增

        // 表容器
        tableContainerPanel = new JPanel(new CardLayout());
        tableContainerPanel.add(tableContainerPanelDefaultKey, new JPanel());


        JPanel muPanel = new JPanel(new GridBagLayout());
        UIUtil.bag(muPanel, numLabel, 1, 1, 0, 0);
        UIUtil.bag(muPanel, numField, 1, 1, 6, 0);
        UIUtil.bag(muPanel, batchSizeLabel, 1, 1, 0, 0);
        UIUtil.bag(muPanel, batchSizeField, 1, 1, 1, 0);
        UIUtil.bag(muPanel, errCheckBox, 1, 1, 0, 0);
        UIUtil.bag(muPanel, btn, 1, 1, 0, 0);



        UIUtil.bag(panel, UIUtil.hwrap(dbLabel, dbComboBox), 0, 1, 1, 0, new Insets(3, 3, 3, 3));
        UIUtil.bag(panel, muPanel, 0, 1, 0, 0);
        UIUtil.bag(panel, tableListPanel, 1, 1, 1, 1);
        UIUtil.bag(panel, tableContainerPanel, 1, 1, 4, 1);
        addTableListener(frame, dbComboBox);
        return panel;
    }

    private void addTableListener(JFrame frame, JComboBox<DbInfo> dbComboBox) {
        CardLayout cardLayout = (CardLayout) tableContainerPanel.getLayout();

        tableListPanel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    TableInfoWrap tableInfoWrap = tableListPanel.getSelectedItem();
                    cardLayout.show(tableContainerPanel, tableInfoWrap.toString());
                }
//                System.out.println(StrUtil.format("{} {} {}", e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting()));

            }
        });

        tableListPanel.addAddBtnMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DbInfo dbInfo = (DbInfo) dbComboBox.getSelectedItem();
                if (dbInfo == null) {
                    return;
                }
                List<TableInfo> tableInfos = EasyCodeUtil.getDbTable(dbInfo.getUrl(), dbInfo.getUsername(), dbInfo.getPassword());
                List<TableInfoWrap> tableInfoWraps = CollectionUtil.convert(tableInfos, TableInfoWrap::of);
                JList<TableInfoWrap> tableList = new JList<>(UIUtil.toListModel(tableInfoWraps));

                SDialog.builder()
                        .parent(frame)
                        .title("选择表")
                        .body(new JScrollPane(tableList))
                        .foot()
                        .footMouseListener(new SDialog.FootMouseListener() {
                            @Override
                            public void mouseClicked(SDialog.FootMouseEvent footMouseEvent) {
                                if (footMouseEvent.getFootIdx() == 2) {
                                    List<TableInfoWrap> selectedValuesList = tableList.getSelectedValuesList();
                                    for (TableInfoWrap tableInfoWrap : selectedValuesList) {
                                        tableListPanel.addItem(tableInfoWrap);
                                        // 添加table
                                        tableContainerPanel.add(tableInfoWrap.toString(), tablePanel(frame, tableInfoWrap.getTableInfo()));
//                                        if (listPanel.getSelectedIndex() < 0) {
//                                            listPanel.setSelectedIndex(0);
//                                        }
                                    }

                                    footMouseEvent.getSource().dispose();
                                } else {
                                    footMouseEvent.getSource().dispose();
                                }
                            }
                        })
                        .build()
                        .display();
            }
        });

        tableListPanel.addRemoveBtnMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<TableInfoWrap> removed = tableListPanel.removeSelectedItems();
                cardLayout.show(tableContainerPanel, tableContainerPanelDefaultKey);
                for (TableInfoWrap tableInfoWrap : removed) {
                    UIUtil.removeComponentFromCard(cardLayout, tableInfoWrap.toString());
                }
            }
        });
    }

    private void genBtnListener(JFrame frame, JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    List<TableInfoWrap> tableInfoWraps = tableListPanel.getAllItem();
                    List<FakeDataTableInfo> fakeDataTableInfos = new ArrayList<>(tableInfoWraps.size());
                    CardLayout cardLayout = (CardLayout) tableContainerPanel.getLayout();
                    for (TableInfoWrap tableInfoWrap : tableInfoWraps) {
                        FakeDataTableInfo fakeDataTableInfo = new FakeDataTableInfo();
                        fakeDataTableInfo.setTableInfo(tableInfoWrap.getTableInfo());
                        fakeDataTableInfo.setFieldInfos(new LinkedList<>());
                        fakeDataTableInfos.add(fakeDataTableInfo);

                        JPanel componentFromCard = (JPanel) UIUtil.getComponentFromCard(cardLayout, tableInfoWrap.toString());
                        JTable table = (JTable) UIUtil.getComponentByName(componentFromCard, "table");
                        TableModel model = table.getModel();

                        for (int i = 0; i < model.getRowCount(); i++) {
                            Boolean checked = (Boolean) table.getValueAt(i, 0);
                            if (!BooleanUtil.isTrue(checked)) {
                                continue;
                            }
                            TableFieldWrap tableFieldWrap = (TableFieldWrap) table.getValueAt(i, 1);
                            IDbDataFaker dbDataFaker = (IDbDataFaker) table.getValueAt(i, 3);
                            FakeDataFieldInfo fakeDataFieldInfo = new FakeDataFieldInfo();
                            fakeDataFieldInfo.setTableField(tableFieldWrap.getTableField());
                            fakeDataFieldInfo.setDbDataFaker(dbDataFaker);
                            fakeDataTableInfo.getFieldInfos().add(fakeDataFieldInfo);
                        }
                    }



                    //
                    JPanel panel = new JPanel(new GridBagLayout());
                    SDialog dialog = SDialog.builder()
                            .parent(frame)
                            .title("生成假数据")
                            .body(panel)
                            .foot("-", "停止")
                            .footMouseListener(new SDialog.FootMouseListener() {
                                @Override
                                public void mouseClicked(SDialog.FootMouseEvent footMouseEvent) {
                                    JButton source = (JButton) footMouseEvent.getMouseEvent().getSource();
                                    if ("停止".equals(source.getText())) {
                                        genExitFlg = true;
                                    }
                                    footMouseEvent.getSource().dispose();
                                }
                            })
                            .size(320, fakeDataTableInfos.size() * 80 + 70)
                            .build();

                    List<StreamProgress> progresses = new ArrayList<>(fakeDataTableInfos.size());
                    // 创建进度条
                    int num = Integer.parseInt(numField.getText());
                    long batchSize = Long.parseLong(batchSizeField.getText());
                    for (int i = 0; i < fakeDataTableInfos.size(); i++) {
                        JProgressBar progressBar = new JProgressBar();
                        progressBar.setString(StrUtil.format("{}/{}", 0, num));
                        progressBar.setStringPainted(true);
                        JLabel label = new JLabel(tableInfoWraps.get(i).toString() + " 等待中");

                        UIUtil.bag(panel, label, 0, 1, 1, 0);
                        UIUtil.bag(panel, progressBar, 0, 1, 1, 0);


                        int finalI = i;
                        StreamProgress progress = new StreamProgress() {
                            private long begin = 0;

                            @Override
                            public void start() {
                                begin = System.currentTimeMillis();
                                progressBar.setMaximum(num);
                                label.setText(tableInfoWraps.get(finalI).toString() + " 生成中...");
                            }

                            @Override
                            public void progress(long progressSize) {
                                progressBar.setValue((int) progressSize);
                                progressBar.setString(StrUtil.format("{}/{}", progressSize, num));
                            }

                            @Override
                            public void finish() {
                                long time = System.currentTimeMillis() - begin;
                                label.setText(tableInfoWraps.get(finalI).toString() + " 生成结束, 耗时" + formatDuration(time) + "!");
                                if (finalI + 1 == tableInfoWraps.size()) {
                                    JButton footBtn = (JButton) UIUtil.getComponentByName(dialog, "footBtn_1");
                                    footBtn.setText("完成");
                                }
                            }
                        };
                        progresses.add(progress);
                    }

                    ThreadUtil.execAsync(() -> {
                        try{
                            genExitFlg = false;
                            DbInfo dbInfo = (DbInfo) dbComboBox.getSelectedItem();
                            EasyCodeUtil.genFakeData2(dbInfo.getDataSource(), fakeDataTableInfos, num, batchSize, progresses, () -> genExitFlg);
                        } catch (Exception throwables) {
                            UIUtil.showError(dialog, throwables.getMessage());
                            log.error("造假数据错误", throwables);
                        }
                    });

                    dialog.display();
                } catch (Exception e1) {
                    UIUtil.showError(e1.getMessage());
                }
            }
        });
    }



    private JComponent tablePanel(JFrame frame, TableInfo tableInfo) {
        JPanel panel = new JPanel(new BorderLayout());
        List<TableField> fields = tableInfo.getFields();
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 1 || column == 2 || column == 3 || column == 4) {
                    return false;
                }
                return true;
            }
        };
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new Object[]{"", "字段", "类型", "生成器" });
        JTable table = new JTable(tableModel);
        table.setName("table");
        TableColumn tc0 = table.getColumnModel().getColumn(0);
        tc0.setMaxWidth(35);
        tc0.setMinWidth(35);
        tc0.setCellEditor(table.getDefaultEditor(Boolean.class));
        tc0.setCellRenderer(table.getDefaultRenderer(Boolean.class));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumn tc1 = table.getColumnModel().getColumn(1);
        tc1.setMaxWidth(300);

        TableColumn tc2 = table.getColumnModel().getColumn(2);
        tc2.setMaxWidth(150);


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowIndex = table.rowAtPoint(e.getPoint());
                    int columnIndex = table.columnAtPoint(e.getPoint());
                    if (columnIndex == 3) {
                        Object value = table.getModel().getValueAt(rowIndex, columnIndex);
                        IDbDataFaker dbDataFaker = null;
                        if (value instanceof IDbDataFaker) {
                            dbDataFaker = (IDbDataFaker) value;
                        }

                        FakerProviders.showDataFakerBuildDialog(dbDataFaker, frame, df -> {
                            table.getModel().setValueAt(df, rowIndex, columnIndex);
                            table.getModel().setValueAt(true, rowIndex, 0);
                        });
                    }
                }
            }
        });



        for (TableField field : fields) {
            Object[] strings = new Object[4];
            strings[0] = false;
            strings[1] = TableFieldWrap.of(field);
            strings[2] = field.getType();
            strings[3] = "";
            tableModel.addRow(strings);
        }


        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }



    private static String formatDuration(long milli) {
        long tmp = milli;
        long h = milli / (1000 * 60 * 60);
        tmp = tmp - (h * 1000 * 60 * 60);

        long m = tmp / (1000 * 60);
        tmp = tmp - (m * 1000 * 60);

        long s = tmp / 1000;

        // 毫秒部分
        long ms = milli % 1000;

        String sStr = s + "." + ms / 100;

        if (h > 0) {
            return StrUtil.format("{}:{}:{}", h, m, sStr);
        } else if (m > 0) {
            return StrUtil.format("{}:{}", m, sStr);
        } else {
            return StrUtil.format("{}s", sStr);
        }
    }


    @Override
    public void save(ActionEvent e) {
        doSave(false);
    }

    @Override
    public void saveNew(ActionEvent e) {
        doSave(true);
    }


    private void doSave(boolean alwaysNewFile) {
        List<TableInfoWrap> allTable = tableListPanel.getAllItem();
        if (allTable.isEmpty()) {
            return;
        }
        boolean hasFaker = false;
        DbInfo dbInfo = (DbInfo) dbComboBox.getSelectedItem();

        Map<String, Map<String, IDbDataFaker>> tableFakeDataInfoMap = new HashMap<>(allTable.size());
        for (TableInfoWrap tableInfoWrap : allTable) {
            CardLayout cardLayout = (CardLayout) tableContainerPanel.getLayout();
            JPanel componentFromCard = (JPanel) UIUtil.getComponentFromCard(cardLayout, tableInfoWrap.toString());
            JTable table = (JTable) UIUtil.getComponentByName(componentFromCard, "table");
            TableModel model = table.getModel();
            Map<String, IDbDataFaker> fakerMap = new HashMap<>(model.getRowCount());
            tableFakeDataInfoMap.put(tableInfoWrap.getTableInfo().getName(), fakerMap);

            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean checked = (Boolean) table.getValueAt(i, 0);
                if (!BooleanUtil.isTrue(checked)) {
                    continue;
                }
                TableFieldWrap tableFieldWrap = (TableFieldWrap) table.getValueAt(i, 1);
                IDbDataFaker dbDataFaker = (IDbDataFaker) table.getValueAt(i, 3);
                fakerMap.put(tableFieldWrap.getTableField().getName(), dbDataFaker);
                hasFaker = true;
            }
        }

        if (!hasFaker) {
            return;
        }

        if (alwaysNewFile || ecfbFile == null) {
            ecfbFile = UIUtil.showSaveFileChooser(new String[]{FILE_SUFFIX});
        }

        EcfbBean ecfbBean = new EcfbBean(dbInfo, tableFakeDataInfoMap);
        byte[] serialize = ObjectUtil.serialize(ecfbBean);
        FileUtil.writeFromStream(new ByteArrayInputStream(serialize), ecfbFile);
    }

    @Override
    public void open(ActionEvent e) {
        if (ecfbFile == null) {
            doOpen(UIUtil.showFileChooser(new String[]{FILE_SUFFIX}));
        } else {
            doOpen(UIUtil.showFileChooser(ecfbFile.getParent(), new String[]{FILE_SUFFIX}));
        }
    }

    private void doOpen(File file) {
        ecfbFile = file;
        byte[] bytes = FileUtil.readBytes(ecfbFile);
        EcfbBean ecfbBean = ObjectUtil.deserialize(bytes);
        DbInfo ecfbDbInfo = ecfbBean.dbInfo;
        boolean dbInfoFind = false;
        for (int i = 0; i < dbComboBox.getItemCount(); i++) {
            DbInfo item = dbComboBox.getItemAt(i);
            if (item.getUrl().equals(ecfbDbInfo.getUrl())) {
                dbComboBox.setSelectedIndex(i);
                dbInfoFind = true;
                break;
            }
        }
        if (!dbInfoFind) {
            EASY_CODE_CONFIG.getDbInfos().add(ecfbDbInfo);
            dbComboBox.addItem(ecfbDbInfo);
            dbComboBox.setSelectedItem(ecfbDbInfo);
        }
        DbInfo dbInfo = (DbInfo) dbComboBox.getSelectedItem();
        List<TableInfo> tableInfos = EasyCodeUtil.getDbTable(dbInfo.getUrl(), dbInfo.getUsername(), dbInfo.getPassword());
        List<TableInfoWrap> tableInfoWraps = CollectionUtil.convert(tableInfos, TableInfoWrap::of);

        clearAllTable();
        List<String> notFindFields = new LinkedList<>();

        // 填充faker对象
        for (Map.Entry<String, Map<String, IDbDataFaker>> entry : ecfbBean.tableFakeDataInfoMap.entrySet()) {
            String tableName = entry.getKey();
            Map<String, IDbDataFaker> fakerMap = entry.getValue();
            TableInfoWrap tableInfoWrap = CollectionUtil.get(tableInfoWraps, tableInfo -> tableName.equals(tableInfo.toString()));
            if (tableInfoWrap == null) {
                for (String fieldName : fakerMap.keySet()) {
                    notFindFields.add(tableName + "." + fieldName);
                }
                continue;
            }

            tableListPanel.addItem(tableInfoWrap);
            // 添加table
            JComponent panel = tablePanel(frame, tableInfoWrap.getTableInfo());
            tableContainerPanel.add(tableInfoWrap.toString(), panel);
            JTable table = (JTable) UIUtil.getComponentByName(panel, "table");

            for (Map.Entry<String, IDbDataFaker> fakerEntry : fakerMap.entrySet()) {
                boolean findField = false;
                String fieldName = fakerEntry.getKey();
                IDbDataFaker faker = fakerEntry.getValue();
                TableModel tableModel = table.getModel();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    TableFieldWrap tableFieldWrap = (TableFieldWrap) tableModel.getValueAt(i, 1);
                    if (!fieldName.equals(tableFieldWrap.getTableField().getName())) {
                        continue;
                    }
                    findField = true;
                    tableModel.setValueAt(fillFaker(faker), i, 3);
                    table.getModel().setValueAt(true, i, 0);
                }
                if (!findField) {
                    notFindFields.add(tableName + "." + fieldName);
                }
            }
        }

        if (!notFindFields.isEmpty()) {
            JTextArea textArea = UIUtil.onlyShowText(new JTextArea());
            textArea.append("未找到字段：\n");
            for (int i = 1; i < notFindFields.size(); i++) {
                textArea.append(notFindFields.get(i));
                textArea.append("\n");
            }
            JPanel panel = new JPanel(new GridBagLayout());
            UIUtil.bag(panel, new JScrollPane(textArea), 1, 1, 1, 1);
            panel.setPreferredSize(new Dimension(400, 450));

            SDialog.builder()
                    .title("警告")
                    .parent(frame)
                    .body(panel)
                    .build()
                    .display();
        }
    }

    private IDbDataFaker fillFaker(IDbDataFaker faker) {
        Field[] fields = ReflectUtil.getFields(faker.getClass());
        for (Field field : fields) {
            if (Faker.class.isAssignableFrom(field.getType())) {
                ReflectUtil.setFieldValue(faker, field, new Faker(new Locale("zh_CN")));
            } else if (IDbDataFaker.class.isAssignableFrom(field.getType())) {
                fillFaker((IDbDataFaker) ReflectUtil.getFieldValue(faker, field));
            }
        }
        return faker;
    }


    private void clearAllTable() {
        CardLayout cardLayout = (CardLayout) tableContainerPanel.getLayout();
        List<TableInfoWrap> removed = tableListPanel.removeAllItem();
        cardLayout.show(tableContainerPanel, tableContainerPanelDefaultKey);
        for (TableInfoWrap tableInfoWrap : removed) {
            UIUtil.removeComponentFromCard(cardLayout, tableInfoWrap.toString());
        }
    }
}

class EcfbBean implements Serializable {
    DbInfo dbInfo;
    Map<String, Map<String, IDbDataFaker>> tableFakeDataInfoMap;

    public EcfbBean(DbInfo dbInfo, Map<String, Map<String, IDbDataFaker>> tableFakeDataInfoMap) {
        this.dbInfo = dbInfo;
        this.tableFakeDataInfoMap = tableFakeDataInfoMap;
    }
}
