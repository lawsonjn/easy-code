package com.developcollect.easycode.provider.fakedata;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.developcollect.easycode.core.fakedata.*;
import com.developcollect.easycode.ui.AttachField;
import com.developcollect.easycode.ui.IconTextField;
import com.developcollect.easycode.ui.SDialog;
import com.developcollect.easycode.utils.UIUtil;
import sun.awt.SunToolkit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 13:42
 */
public class FakerProviders {
    private static List<FakerProvider> fakeProviders = new LinkedList<>();

    static {
        fakeProviders.add(new FakerProvider<IdDbDataFaker>() {
            @Override
            public String name() {
                return "id生成器";
            }

            @Override
            public IdDbDataFaker faker(Map<String, Object> map) {
                IdDbDataFaker idDbDataFaker = new IdDbDataFaker();
                Object idType = map.get("idType");
                if ("自增id".equals(idType)) {
                    idDbDataFaker.setIdType(IdDbDataFaker.TYPE_SELF_INCREASING);
                } else if ("递增id".equals(idType)) {
                    idDbDataFaker.setIdType(IdDbDataFaker.TYPE_INCREASE);
                    idDbDataFaker.setBeginId(Long.parseLong(map.get("beginValue").toString()));
                    idDbDataFaker.setStep(Long.parseLong(map.get("step").toString()));
                } else if ("uuid".equals(idType)) {
                    idDbDataFaker.setIdType(IdDbDataFaker.TYPE_UUID);
                } else if ("雪花id".equals(idType)) {
                    idDbDataFaker.setIdType(IdDbDataFaker.TYPE_SNOWFLAKE_ID);

                    idDbDataFaker.setWorkId(Long.parseLong(map.get("workerId").toString()));
                    idDbDataFaker.setDatacenterId(Long.parseLong(map.get("datacenterId").toString()));
                } else if ("SequenceId".equals(idType)) {
                    idDbDataFaker.setIdType(IdDbDataFaker.TYPE_SEQUENCE_ID);

                    try {
                        idDbDataFaker.setWorkId(Long.parseLong(map.get("workerId").toString()));
                    } catch (Exception e) {
                        idDbDataFaker.setWorkId(null);
                    }
                    try {
                        idDbDataFaker.setDatacenterId(Long.parseLong(map.get("datacenterId").toString()));
                    } catch (Exception e) {
                        idDbDataFaker.setDatacenterId(null);
                    }
                }

                return idDbDataFaker;
            }

            @Override
            public JComponent component(IdDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel idLabel = new JLabel("id类型：");
                JComboBox<String> idComboBox = new JComboBox<>();
                idComboBox.setName("idType");
                idComboBox.addItem("自增id");
                idComboBox.addItem("递增id");
                idComboBox.addItem("uuid");
                idComboBox.addItem("雪花id");
                idComboBox.addItem("SequenceId");

                CardLayout cardLayout = new CardLayout();
                JPanel detailPanel = new JPanel(cardLayout);

                // 递增id
                JPanel incrementIdPanel = new JPanel(new GridBagLayout());
                JLabel startLabel = new JLabel("起始值：");
                JTextField startField = new JTextField("0");
                startField.setName("beginValue");
                JLabel stepLabel = new JLabel("步长：");
                JTextField stepField = new JTextField("1");
                stepField.setName("step");
                UIUtil.bag(incrementIdPanel, startLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(incrementIdPanel, startField, 0, 1, 1, 0);
                UIUtil.bag(incrementIdPanel, stepLabel, 1, 1, 0, 0);
                UIUtil.bag(incrementIdPanel, stepField, 0, 1, 1, 0);
                UIUtil.bag(incrementIdPanel, Box.createGlue(), 0, 1, 1, 1);

                // 雪花id
                JPanel snowflakeIdPanel = new JPanel(new GridBagLayout());
                JLabel workerIdLabel = new JLabel("终端ID：");
                JTextField workerIdField = new JTextField("1");
                workerIdField.setName("workerId");
                JLabel datacenterIdLabel = new JLabel("数据中心ID：");
                JTextField datacenterIdField = new JTextField("1");
                datacenterIdField.setName("datacenterId");
                UIUtil.bag(snowflakeIdPanel, workerIdLabel, 1, 1, 0, 0);
                UIUtil.bag(snowflakeIdPanel, workerIdField, 0, 1, 1, 0);
                UIUtil.bag(snowflakeIdPanel, datacenterIdLabel, 1, 1, 0, 0);
                UIUtil.bag(snowflakeIdPanel, datacenterIdField, 0, 1, 1, 0);
                UIUtil.bag(snowflakeIdPanel, Box.createGlue(), 0, 1, 1, 1);


                detailPanel.add("自增id", Box.createGlue());
                detailPanel.add("递增id", incrementIdPanel);
                detailPanel.add("uuid", Box.createGlue());
                detailPanel.add("雪花id", snowflakeIdPanel);

                idComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String item = e.getItem().toString();
                            if ("SequenceId".equals(item)) {
                                workerIdField.setText("");
                                datacenterIdField.setText("");
                                cardLayout.show(detailPanel, "雪花id");
                                return;
                            } else if ("雪花id".equals(item)) {
                                workerIdField.setText("1");
                                datacenterIdField.setText("1");
                            }
                            cardLayout.show(detailPanel, item);
                        }
                    }
                });


                if (dbDataFaker != null) {
                    if (dbDataFaker.getIdType() == IdDbDataFaker.TYPE_SELF_INCREASING) {
                        idComboBox.setSelectedIndex(0);
                    } else if (dbDataFaker.getIdType() == IdDbDataFaker.TYPE_INCREASE) {
                        idComboBox.setSelectedIndex(1);
                        startField.setText(Long.toString(dbDataFaker.getBeginId()));
                        stepField.setText(Long.toString(dbDataFaker.getStep()));
                    } else if (dbDataFaker.getIdType() == IdDbDataFaker.TYPE_UUID) {
                        idComboBox.setSelectedIndex(2);
                    } else if (dbDataFaker.getIdType() == IdDbDataFaker.TYPE_SNOWFLAKE_ID) {
                        idComboBox.setSelectedIndex(3);
                        workerIdField.setText(Long.toString(dbDataFaker.getWorkId()));
                        datacenterIdField.setText(Long.toString(dbDataFaker.getDatacenterId()));
                    } else if (dbDataFaker.getIdType() == IdDbDataFaker.TYPE_SEQUENCE_ID) {
                        idComboBox.setSelectedIndex(4);
                        if (dbDataFaker.getWorkId() != null) {
                            workerIdField.setText(Long.toString(dbDataFaker.getWorkId()));
                        }
                        if (dbDataFaker.getDatacenterId() != null) {
                            datacenterIdField.setText(Long.toString(dbDataFaker.getDatacenterId()));
                        }
                    }
                }

                UIUtil.bag(panel, idLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, idComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, detailPanel, 0, 1, 1, 1);

                panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<RandomDbDataFaker>() {
            @Override
            public String name() {
                return "随机值生成器";
            }

            @Override
            public RandomDbDataFaker faker(Map<String, Object> map) {
                RandomDbDataFaker randomDbDataFaker = new RandomDbDataFaker();
                String type = (String) map.get("type");
                switch (type) {
                    case "随机Int":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_INT);
                        randomDbDataFaker.setMin(Long.parseLong(map.get("intMin").toString()));
                        randomDbDataFaker.setMax(Long.parseLong(map.get("intMax").toString()));
                        break;
                    case "随机Long":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_LONG);
                        randomDbDataFaker.setMin(Long.parseLong(map.get("longMin").toString()));
                        randomDbDataFaker.setMax(Long.parseLong(map.get("longMax").toString()));
                        break;
                    case "随机Boolean":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_BOOLEAN);
                        break;
                    case "随机字符串":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_STRING);
                        randomDbDataFaker.setBaseString(map.get("baseString").toString());
                        randomDbDataFaker.setMin(Long.parseLong(map.get("stringMin").toString()));
                        randomDbDataFaker.setMax(Long.parseLong(map.get("stringMax").toString()));
                        break;
                    case "随机颜色":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_COLOR);
                        break;
                    case "随机元素":
                        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_OPTION);
                        String baseOption = map.get("baseOption").toString();
                        randomDbDataFaker.setBaseList(Arrays.asList(baseOption.split("\n")));
                        break;
                    default:
                }
                return randomDbDataFaker;
            }

            @Override
            public JComponent component(RandomDbDataFaker dbDataFaker) {
                JLabel label = new JLabel("递变类型：");
                JComboBox<String> typeComboBox = new JComboBox<>();
                typeComboBox.setName("type");
                typeComboBox.addItem("随机Int");
                typeComboBox.addItem("随机Long");
                typeComboBox.addItem("随机Boolean");
                typeComboBox.addItem("随机字符串");
                typeComboBox.addItem("随机颜色");
                typeComboBox.addItem("随机元素");

                // int
                JPanel panel1 = new JPanel(new GridBagLayout());
                JLabel intMinLabel = new JLabel("最小值：");
                JTextField intMinField = new JTextField("0");
                intMinField.setName("intMin");

                JLabel intMaxLabel = new JLabel("最大值：");
                JTextField intMaxField = new JTextField("256");
                intMaxField.setName("intMax");

                UIUtil.bag(panel1, intMinLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel1, intMinField, 0, 1, 1, 0);
                UIUtil.bag(panel1, intMaxLabel, 1, 1, 0, 0);
                UIUtil.bag(panel1, intMaxField, 0, 1, 1, 0);
                UIUtil.bag(panel1, Box.createGlue(), 0, 1, 1, 1);

                // long
                JPanel panel2 = new JPanel(new GridBagLayout());
                JLabel longMinLabel = new JLabel("最小值：");
                JTextField longMinField = new JTextField("0");
                longMinField.setName("longMin");

                JLabel longMaxLabel = new JLabel("最大值：");
                JTextField longMaxField = new JTextField("2256");
                longMaxField.setName("longMax");

                UIUtil.bag(panel2, longMinLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel2, longMinField, 0, 1, 1, 0);
                UIUtil.bag(panel2, longMaxLabel, 1, 1, 0, 0);
                UIUtil.bag(panel2, longMaxField, 0, 1, 1, 0);
                UIUtil.bag(panel2, Box.createGlue(), 0, 1, 1, 1);

                // String
                JPanel panel3 = new JPanel(new GridBagLayout());
                JLabel stringBaseLabel = new JLabel("基础字符：");
                JTextField stringBaseField = new JTextField(RandomUtil.BASE_CHAR_NUMBER);
                stringBaseField.setName("baseString");

                JLabel stringMinLabel = new JLabel("最小长度：");
                JTextField stringMinField = new JTextField("0");
                stringMinField.setName("stringMin");

                JLabel stringMaxLabel = new JLabel("最大长度：");
                JTextField stringMaxField = new JTextField("256");
                stringMaxField.setName("stringMax");

                UIUtil.bag(panel3, stringBaseLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel3, stringBaseField, 0, 1, 1, 0);
                UIUtil.bag(panel3, stringMinLabel, 1, 1, 0, 0);
                UIUtil.bag(panel3, stringMinField, 0, 1, 1, 0);
                UIUtil.bag(panel3, stringMaxLabel, 1, 1, 0, 0);
                UIUtil.bag(panel3, stringMaxField, 0, 1, 1, 0);
                UIUtil.bag(panel3, Box.createGlue(), 0, 1, 1, 1);

                JPanel panel4 = new JPanel(new GridBagLayout());
                JLabel optionBaseLabel = new JLabel("基础元素：");
                JTextArea optionBaseField = new JTextArea("1\n2\n3\n4\n5");
                optionBaseField.setLineWrap(true);
                optionBaseField.setName("baseOption");


                UIUtil.bag(panel4, optionBaseLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel4, new JScrollPane(optionBaseField), 0, 1, 1, 1);





                CardLayout cardLayout = new CardLayout();
                JPanel detailPanel = new JPanel(cardLayout);
                detailPanel.add("随机Int", panel1);
                detailPanel.add("随机Long", panel2);
                detailPanel.add("随机Boolean", Box.createGlue());
                detailPanel.add("随机字符串", panel3);
                detailPanel.add("随机颜色", Box.createGlue());
                detailPanel.add("随机元素", panel4);

                typeComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String item = e.getItem().toString();
                            cardLayout.show(detailPanel, item);
                        }
                    }
                });


                if (dbDataFaker != null) {
                    if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_INT) {
                        typeComboBox.setSelectedIndex(0);
                        intMinField.setText(Long.toString(dbDataFaker.getMin()));
                        intMaxField.setText(Long.toString(dbDataFaker.getMax()));
                    } else if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_LONG) {
                        typeComboBox.setSelectedIndex(1);
                        longMinField.setText(Long.toString(dbDataFaker.getMin()));
                        longMaxField.setText(Long.toString(dbDataFaker.getMax()));
                    } else if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_BOOLEAN) {
                        typeComboBox.setSelectedIndex(2);
                    } else if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_STRING) {
                        typeComboBox.setSelectedIndex(3);
                        stringBaseField.setText(dbDataFaker.getBaseString());
                        stringMinField.setText(Long.toString(dbDataFaker.getMin()));
                        stringMaxField.setText(Long.toString(dbDataFaker.getMax()));
                    } else if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_COLOR) {
                        typeComboBox.setSelectedIndex(4);
                    } else if (dbDataFaker.getType() == RandomDbDataFaker.TYPE_OPTION) {
                        typeComboBox.setSelectedIndex(5);
                        optionBaseField.setText(StrUtil.join("\n", dbDataFaker.getBaseList()));
                    }
                }

                JPanel panel = new JPanel(new GridBagLayout());

                UIUtil.bag(panel, label, 1, 1, 0, 0);
                UIUtil.bag(panel, typeComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, detailPanel, 0, 1, 1, 1);

                panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<IncreaseDbDataFaker>() {
            @Override
            public String name() {
                return "递变数值";
            }

            @Override
            public IncreaseDbDataFaker faker(Map<String, Object> map) {
                IncreaseDbDataFaker increaseDbDataFaker = new IncreaseDbDataFaker();
                increaseDbDataFaker.setBegin(Convert.toBigDecimal(map.get("beginValue")));

                Object type = map.get("type");
                if ("固定递变".equals(type)) {
                    increaseDbDataFaker.setRandomStep(false);
                    increaseDbDataFaker.setStep(Convert.toBigDecimal(map.get("step")));
                } else if ("随机递变".equals(type)) {
                    increaseDbDataFaker.setRandomStep(true);
                    increaseDbDataFaker.setStep(Convert.toBigDecimal(map.get("stepMin")));
                    increaseDbDataFaker.setStep(Convert.toBigDecimal(map.get("stepMax")));
                }
                return increaseDbDataFaker;
            }

            @Override
            public JComponent component(IncreaseDbDataFaker dbDataFaker) {
                JLabel label = new JLabel("递变类型：");
                JComboBox<String> typeComboBox = new JComboBox<>();
                typeComboBox.setName("type");
                typeComboBox.addItem("固定递变");
                typeComboBox.addItem("随机递变");

                // 固定
                JPanel panel1 = new JPanel(new GridBagLayout());
                JLabel startLabel1 = new JLabel("起始值：");
                JTextField startField1 = new JTextField("0");
                startField1.setName("beginValue");
                JLabel stepLabel = new JLabel("步长：");
                JTextField stepField = new JTextField("1");
                stepField.setName("step");
                UIUtil.bag(panel1, startLabel1, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel1, startField1, 0, 1, 1, 0);
                UIUtil.bag(panel1, stepLabel, 1, 1, 0, 0);
                UIUtil.bag(panel1, stepField, 0, 1, 1, 0);
                UIUtil.bag(panel1, Box.createGlue(), 0, 1, 1, 1);

                // 随机
                JPanel panel2 = new JPanel(new GridBagLayout());
                JLabel startLabel = new JLabel("起始值：");
                JTextField startField = new JTextField("0");
                startField.setName("beginValue");

                JLabel stepMinLabel = new JLabel("步长最小值：");
                JTextField stepMinField = new JTextField("1");
                stepMinField.setName("stepMin");
                JLabel stepMaxLabel = new JLabel("步长最大值：");
                JTextField stepMaxField = new JTextField("1");
                stepMaxField.setName("stepMax");

                UIUtil.bag(panel2, startLabel, 1, 1, 0, 0, GridBagConstraints.BOTH, new Insets(3,0,3,0));
                UIUtil.bag(panel2, startField, 0, 1, 1, 0);
                UIUtil.bag(panel2, stepMinLabel, 1, 1, 0, 0);
                UIUtil.bag(panel2, stepMinField, 0, 1, 1, 0);
                UIUtil.bag(panel2, stepMaxLabel, 1, 1, 0, 0);
                UIUtil.bag(panel2, stepMaxField, 0, 1, 1, 0);
                UIUtil.bag(panel2, Box.createGlue(), 0, 1, 1, 1);



                CardLayout cardLayout = new CardLayout();
                JPanel detailPanel = new JPanel(cardLayout);
                detailPanel.add("固定递变", panel1);
                detailPanel.add("随机递变", panel2);

                typeComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String item = e.getItem().toString();
                            cardLayout.show(detailPanel, item);
                        }
                    }
                });


                if (dbDataFaker != null) {

                    if (dbDataFaker.isRandomStep()) {
                        typeComboBox.setSelectedIndex(1);
                        startField.setText(dbDataFaker.getBegin().toPlainString());
                        stepMinField.setText(dbDataFaker.getStepMin().toPlainString());
                        stepMaxField.setText(dbDataFaker.getStepMax().toPlainString());
                    } else {
                        typeComboBox.setSelectedIndex(0);
                        startField1.setText(dbDataFaker.getBegin().toPlainString());
                        stepField.setText(dbDataFaker.getStep().toPlainString());
                    }
                }

                JPanel panel = new JPanel(new GridBagLayout());

                UIUtil.bag(panel, label, 1, 1, 0, 0);
                UIUtil.bag(panel, typeComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, detailPanel, 0, 1, 1, 1);

                panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<DateDbDataFaker>() {
            @Override
            public String name() {
                return "日期生成器";
            }

            @Override
            public DateDbDataFaker faker(Map<String, Object> map) {
                DateDbDataFaker dateDbDataFaker = new DateDbDataFaker();
                String type = map.get("type").toString();
                dateDbDataFaker.setFormat(map.get("format").toString());
                switch (type) {
                    case "随机日期":
                        dateDbDataFaker.setMin(map.get("min").toString());
                        dateDbDataFaker.setMax(map.get("max").toString());
                        break;

                    case "固定递变日期":
                        dateDbDataFaker.setType(DateDbDataFaker.TYPE_INCREASE);
                        dateDbDataFaker.setBegin(map.get("begin").toString());
                        dateDbDataFaker.setStep(Long.parseLong(map.get("step").toString()));
                        break;

                    case "随机递变日期":
                        dateDbDataFaker.setType(DateDbDataFaker.TYPE_INCREASE);
                        dateDbDataFaker.setRandomStep(true);
                        dateDbDataFaker.setBegin(map.get("begin").toString());
                        dateDbDataFaker.setStepMin(Long.parseLong(map.get("minStep").toString()));
                        dateDbDataFaker.setStepMax(Long.parseLong(map.get("maxStep").toString()));
                        break;
                    default:
                }
                return dateDbDataFaker;
            }

            @Override
            public JComponent component(DateDbDataFaker dbDataFaker) {
                JLabel formatLabel = new JLabel("日期格式：");
                JTextField formatField = new JTextField("yyyy-MM-dd HH:mm:ss");
                formatField.setName("format");

                JLabel label = new JLabel("生成规则：");
                JComboBox<String> ruleComboBox = new JComboBox<>();
                ruleComboBox.setName("type");
                ruleComboBox.addItem("随机日期");
                ruleComboBox.addItem("固定递变日期");
                ruleComboBox.addItem("随机递变日期");

                // 随机日期
                JLabel minLabel = new JLabel("最小：");
                JTextField minField = new JTextField("1990-01-01 00:00:00");
                minField.setName("min");
                JLabel maxLabel = new JLabel("最大：");
                JTextField maxField = new JTextField("2022-01-01 00:00:00");
                maxField.setName("max");

                JPanel panel1 = new JPanel(new GridBagLayout());
                UIUtil.bag(panel1, minLabel, 1, 1, 0, 0);
                UIUtil.bag(panel1, minField, 0, 1, 1, 0);
                UIUtil.bag(panel1, maxLabel, 1, 1, 0, 0);
                UIUtil.bag(panel1, maxField, 0, 1, 1, 0);
                UIUtil.bag(panel1, Box.createGlue(), 0, 1, 1, 1);

                // 递变
                JLabel beginLabel1 = new JLabel("起始值：");
                JTextField beginField1 = new JTextField("2021-01-01 00:00:00");
                beginField1.setName("begin");
                JLabel stepLabel = new JLabel("步长：");
                JTextField stepField = new JTextField("300000");
                stepField.setName("step");
                JPanel panel2 = new JPanel(new GridBagLayout());
                UIUtil.bag(panel2, beginLabel1, 1, 1, 0, 0);
                UIUtil.bag(panel2, beginField1, 0, 1, 1, 0);
                UIUtil.bag(panel2, stepLabel, 1, 1, 0, 0);
                UIUtil.bag(panel2, stepField, 0, 1, 1, 0);
                UIUtil.bag(panel2, Box.createGlue(), 0, 1, 1, 1);

                // 随机递变
                JLabel beginLabel2 = new JLabel("起始值：");
                JTextField beginField2 = new JTextField("2021-01-01 00:00:00");
                beginField2.setName("begin");
                JLabel minStepLabel = new JLabel("最小步长：");
                JTextField minStepField = new JTextField("300000");
                minStepField.setName("minStep");
                JLabel maxStepLabel = new JLabel("最大步长：");
                JTextField maxStepField = new JTextField("300000");
                maxStepField.setName("maxStep");
                JPanel panel3 = new JPanel(new GridBagLayout());
                UIUtil.bag(panel3, beginLabel2, 1, 1, 0, 0);
                UIUtil.bag(panel3, beginField2, 0, 1, 1, 0);
                UIUtil.bag(panel3, minStepLabel, 1, 1, 0, 0);
                UIUtil.bag(panel3, minStepField, 0, 1, 1, 0);
                UIUtil.bag(panel3, maxStepLabel, 1, 1, 0, 0);
                UIUtil.bag(panel3, maxStepField, 0, 1, 1, 0);
                UIUtil.bag(panel3, Box.createGlue(), 0, 1, 1, 1);

                CardLayout cardLayout = new CardLayout();
                JPanel detailPanel = new JPanel(cardLayout);
                detailPanel.add("随机日期", panel1);
                detailPanel.add("固定递变日期", panel2);
                detailPanel.add("随机递变日期", panel3);

                ruleComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String item = e.getItem().toString();
                            cardLayout.show(detailPanel, item);
                        }
                    }
                });

                if (dbDataFaker != null) {
                    formatField.setText(dbDataFaker.getFormat());
                    if (dbDataFaker.getType() == DateDbDataFaker.TYPE_RANDOM) {
                        ruleComboBox.setSelectedIndex(0);
                        minField.setText(dbDataFaker.getMin());
                        maxField.setText(dbDataFaker.getMax());
                    } else {
                        if (dbDataFaker.isRandomStep()) {
                            ruleComboBox.setSelectedIndex(2);
                            minStepField.setText(Long.toString(dbDataFaker.getStepMin()));
                            maxStepField.setText(Long.toString(dbDataFaker.getStepMax()));
                            beginField2.setText(dbDataFaker.getBegin());
                        } else {
                            ruleComboBox.setSelectedIndex(1);
                            stepField.setText(Long.toString(dbDataFaker.getStep()));
                            beginField1.setText(dbDataFaker.getBegin());
                        }
                    }
                }

                JPanel p = new JPanel(new GridBagLayout());
                UIUtil.bag(p, formatLabel, 1, 1, 0, 0);
                UIUtil.bag(p, formatField, 0, 1, 1, 0);
                UIUtil.bag(p, label, 1, 1, 0, 0);
                UIUtil.bag(p, ruleComboBox, 0, 1, 1, 0);
                UIUtil.bag(p, detailPanel, 0, 1, 1, 1);

                p.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                return p;
            }
        });

        fakeProviders.add(new FakerProvider<ConstantDbDataFaker>() {
            @Override
            public String name() {
                return "固定值";
            }

            @Override
            public ConstantDbDataFaker faker(Map<String, Object> map) {
                ConstantDbDataFaker constantDbDataFaker = new ConstantDbDataFaker();
                constantDbDataFaker.setConstant(map.get("constant").toString());
                return constantDbDataFaker;
            }

            @Override
            public JComponent component(ConstantDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel idLabel = new JLabel("固定值：");
                JTextArea constantField = new JTextArea();
                constantField.setName("constant");

                if (dbDataFaker != null) {
                    constantField.setText(dbDataFaker.getConstant());
                }

                UIUtil.bag(panel, idLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, new JScrollPane(constantField), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<WrapDbDataFaker>() {
            @Override
            public String name() {
                return "固定部分";
            }

            @Override
            public WrapDbDataFaker faker(Map<String, Object> map) {
                WrapDbDataFaker wrapDbDataFaker = new WrapDbDataFaker();
                wrapDbDataFaker.setFormat(map.get("wrapFormat").toString());
                wrapDbDataFaker.setDbDataFaker((IDbDataFaker) map.get("wrapDataFaker"));
                return wrapDbDataFaker;
            }

            @Override
            public JComponent component(WrapDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel formatLabel = new JLabel("模板：");
                JTextField formatField = new JTextField();
                formatField.setText("测试${value}后缀");
                formatField.setName("wrapFormat");
                JLabel wrapLabel = new JLabel("嵌套生成器：");
                AttachField wrapField = new AttachField();
                wrapField.setName("wrapDataFaker");

                wrapField.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == 1 && e.getClickCount() == 2) {
                            JTextField source = (JTextField) e.getSource();
                            Component containingWindow = SunToolkit.getContainingWindow(source);
                            showDataFakerBuildDialog(dbDataFaker, containingWindow, wrapField::setAttach);
                        }
                    }
                });

                if (dbDataFaker != null) {
                    formatField.setText(dbDataFaker.getFormat());
                    wrapField.setAttach(dbDataFaker.getDbDataFaker());
                }

                UIUtil.bag(panel, formatLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, formatField, 0, 1, 1, 0);
                UIUtil.bag(panel, wrapLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, wrapField, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<PhoneNumberDbDataFaker>() {
            @Override
            public String name() {
                return "手机号";
            }

            @Override
            public PhoneNumberDbDataFaker faker(Map<String, Object> map) {
                PhoneNumberDbDataFaker phoneNumberDbDataFaker = new PhoneNumberDbDataFaker();
                Object prefix = map.get("prefix");
                phoneNumberDbDataFaker.setPrefix(prefix.toString());
                return phoneNumberDbDataFaker;
            }

            @Override
            public JComponent component(PhoneNumberDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel idLabel = new JLabel("前缀：");
                JTextField prefixField = new JTextField();
                prefixField.setName("prefix");

                if (dbDataFaker != null) {
                    prefixField.setText(dbDataFaker.getPrefix());
                }

                UIUtil.bag(panel, idLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, prefixField, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<PeopleIdentityDbDataFaker>() {
            @Override
            public String name() {
                return "身份证号";
            }

            @Override
            public PeopleIdentityDbDataFaker faker(Map<String, Object> map) {
                return new PeopleIdentityDbDataFaker();
            }

            @Override
            public JComponent component(PeopleIdentityDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<NameDbDataFaker>() {
            @Override
            public String name() {
                return "随机姓名";
            }

            @Override
            public NameDbDataFaker faker(Map<String, Object> map) {
                NameDbDataFaker nameDbDataFaker = new NameDbDataFaker();
                Object namePart = map.get("namePart");
                if ("姓".equals(namePart)) {
                    nameDbDataFaker.setNamePart(NameDbDataFaker.PART_FIRST_NAME);
                } else if ("名".equals(namePart)) {
                    nameDbDataFaker.setNamePart(NameDbDataFaker.PART_LAST_NAME);
                } else {
                    nameDbDataFaker.setNamePart(NameDbDataFaker.PART_FULL_NAME);
                }

                return nameDbDataFaker;
            }

            @Override
            public JComponent component(NameDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel idLabel = new JLabel("名字：");
                JComboBox<String> idComboBox = new JComboBox<>();
                idComboBox.setName("namePart");
                idComboBox.addItem("姓名");
                idComboBox.addItem("姓");
                idComboBox.addItem("名");

                if (dbDataFaker != null) {
                    if (dbDataFaker.getNamePart() == NameDbDataFaker.PART_FIRST_NAME) {
                        idComboBox.setSelectedIndex(1);
                    } else if (dbDataFaker.getNamePart() == NameDbDataFaker.PART_LAST_NAME) {
                        idComboBox.setSelectedIndex(2);
                    } else {
                        idComboBox.setSelectedIndex(0);
                    }

                }

                UIUtil.bag(panel, idLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, idComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<AddressDbDataFaker>() {
            @Override
            public String name() {
                return "地址生成器";
            }

            @Override
            public AddressDbDataFaker faker(Map<String, Object> map) {
                AddressDbDataFaker addressDbDataFaker = new AddressDbDataFaker();
                Object addressPart = map.get("addressPart");
                if ("省".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_PROVINCE);
                } else if ("省缩写".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_PROVINCE_ABBR);
                } else if ("市".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_CITY);
                } else if ("邮编".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_POSTCODE);
                } else if ("地址".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_ADDRESS);
                } else if ("全地址".equals(addressPart)) {
                    addressDbDataFaker.setType(AddressDbDataFaker.TYPE_FULL_ADDRESS);
                }
                return addressDbDataFaker;
            }

            @Override
            public JComponent component(AddressDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel idLabel = new JLabel("部分：");
                JComboBox<String> partComboBox = new JComboBox<>();
                partComboBox.setName("addressPart");
                partComboBox.addItem("省");
                partComboBox.addItem("省缩写");
                partComboBox.addItem("市");
                partComboBox.addItem("邮编");
                partComboBox.addItem("地址");
                partComboBox.addItem("全地址");

                if (dbDataFaker != null) {
                    partComboBox.setSelectedIndex(dbDataFaker.getType() - 1);
                }

                UIUtil.bag(panel, idLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, partComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<ReferenceDbDataFaker>() {
            @Override
            public String name() {
                return "引用其他字段";
            }

            @Override
            public ReferenceDbDataFaker faker(Map<String, Object> map) {
                ReferenceDbDataFaker referenceDbDataFaker = new ReferenceDbDataFaker();
                referenceDbDataFaker.setFormat(map.get("format").toString());
                return referenceDbDataFaker;
            }

            @Override
            public JComponent component(ReferenceDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel formatLabel = new JLabel("格式：");
                JTextArea formatTextArea = new JTextArea();
                formatTextArea.setText("${table.field}");
                formatTextArea.setName("format");

                if (dbDataFaker != null) {
                    formatTextArea.setText(dbDataFaker.getFormat());
                }

                UIUtil.bag(panel, formatLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, new JScrollPane(formatTextArea), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<IpDbDataFaker>() {
            @Override
            public String name() {
                return "IP生成器";
            }

            @Override
            public IpDbDataFaker faker(Map<String, Object> map) {
                IpDbDataFaker ipDbDataFaker = new IpDbDataFaker();
                ipDbDataFaker.setIpv4((boolean) map.get("isIPv4"));
                ipDbDataFaker.setPrivateIp((boolean) map.get("isPrivateIp"));
                return ipDbDataFaker;
            }

            @Override
            public JComponent component(IpDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JCheckBox ipV4CheckBox = new JCheckBox("IPv4");
                ipV4CheckBox.setName("isIPv4");
                ipV4CheckBox.setSelected(true);
                JCheckBox privateIpCheckBox = new JCheckBox("内网IP");
                privateIpCheckBox.setName("isPrivateIp");
                privateIpCheckBox.setSelected(false);


                if (dbDataFaker != null) {
                    ipV4CheckBox.setSelected(dbDataFaker.isIpv4());
                    privateIpCheckBox.setSelected(dbDataFaker.isPrivateIp());
                }

                UIUtil.bag(panel, ipV4CheckBox, 0, 1, 0, 0);
                UIUtil.bag(panel, privateIpCheckBox, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<FileDbDataFaker>() {
            @Override
            public String name() {
                return "文件生成器";
            }

            @Override
            public FileDbDataFaker faker(Map<String, Object> map) {
                FileDbDataFaker fileDbDataFaker = new FileDbDataFaker();
                String separator = (String) map.get("separator");
                if (StrUtil.isNotBlank(separator)) {
                    fileDbDataFaker.setSeparator(separator);
                }
                String roots = (String) map.get("roots");
                if (StrUtil.isNotBlank(roots)) {
                    List<String> strings = StrUtil.splitTrim(roots, ",");
                    fileDbDataFaker.setRoots(strings.toArray(new String[0]));
                }
                String suffixes = (String) map.get("suffixes");
                if (StrUtil.isNotBlank(suffixes)) {
                    List<String> strings = StrUtil.splitTrim(suffixes, ",");
                    fileDbDataFaker.setSuffixes(strings.toArray(new String[0]));
                }
                boolean onlyFilename = (boolean) map.get("onlyFilename");
                fileDbDataFaker.setOnlyFilename(onlyFilename);
                return fileDbDataFaker;
            }

            @Override
            public JComponent component(FileDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel separatorLabel = new JLabel("分隔符：");
                JTextField separatorField = new JTextField();
                separatorField.setName("separator");

                JLabel rootsLabel = new JLabel("根符号：");
                JTextField rootsField = new JTextField();
                rootsField.setName("roots");

                JLabel suffixesLabel = new JLabel("后缀：");
                JTextField suffixesField = new JTextField();
                suffixesField.setName("suffixes");

                JCheckBox onlyFilenameCheckBox = new JCheckBox("只有文件名");
                onlyFilenameCheckBox.setName("onlyFilename");



                if (dbDataFaker != null) {
                    String separator = dbDataFaker.getSeparator();
                    if (separator != null) {
                        separatorField.setText(separator);
                    }
                    String[] roots = dbDataFaker.getRoots();
                    if (roots != null) {
                        rootsField.setText(StrUtil.join(", ", roots));
                    }
                    String[] suffixes = dbDataFaker.getSuffixes();
                    if (suffixes != null) {
                        suffixesField.setText(StrUtil.join(", ", suffixes));
                    }
                    onlyFilenameCheckBox.setSelected(dbDataFaker.isOnlyFilename());
                } else {
                    onlyFilenameCheckBox.setSelected(false);
                    separatorField.setText(File.separator);
                    OsInfo osInfo = SystemUtil.getOsInfo();
                    if (osInfo.isWindows()) {
                        rootsField.setText(StrUtil.join(", ", FileDbDataFaker.DEFAULT_WIN_ROOTS));
                    } else {
                        rootsField.setText(StrUtil.join(", ", FileDbDataFaker.DEFAULT_LINUX_ROOTS));
                    }
                    suffixesField.setText(StrUtil.join(", ", FileDbDataFaker.DEFAULT_SUFFIXES));
                }

                UIUtil.bag(panel, separatorLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, separatorField, 0, 1, 1, 0);
                UIUtil.bag(panel, rootsLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, rootsField, 0, 1, 1, 0);
                UIUtil.bag(panel, suffixesLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, suffixesField, 0, 1, 1, 0);
                UIUtil.bag(panel, onlyFilenameCheckBox, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<JSONReferenceDbDataFaker>() {
            @Override
            public String name() {
                return "引用JSON";
            }

            @Override
            public JSONReferenceDbDataFaker faker(Map map) {
                JSONReferenceDbDataFaker jsonReferenceDbDataFaker = new JSONReferenceDbDataFaker();
                return jsonReferenceDbDataFaker;
            }

            @Override
            public JComponent component(JSONReferenceDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel jsonFileLabel = new JLabel("JSON文件：");
                IconTextField jsonFileField = UIUtil.setFileChooserField(new IconTextField(), "选择JSON文件", "json");
                jsonFileField.setName("jsonPath");

                JCheckBox cycleCheckBox = new JCheckBox("重复读取");

                JLabel formatLabel = new JLabel("格式：");
                JTextArea formatField = new JTextArea();
                formatField.setName("format");

                if (dbDataFaker != null) {
                    cycleCheckBox.setSelected(dbDataFaker.isCycle());
                    jsonFileField.setText(dbDataFaker.getJsonPath());
                    formatField.setText(dbDataFaker.getFormat());
                } else {
                    cycleCheckBox.setSelected(true);
                }

                UIUtil.bag(panel, jsonFileLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, jsonFileField, 0, 1, 1, 0);
                UIUtil.bag(panel, cycleCheckBox, 0, 1, 1, 0);
                UIUtil.bag(panel, formatLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, new JScrollPane(formatField), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<UserAgentDbDataFaker>() {
            @Override
            public String name() {
                return "UserAgent生成器";
            }

            @Override
            public UserAgentDbDataFaker faker(Map map) {
                UserAgentDbDataFaker userAgentDbDataFaker = new UserAgentDbDataFaker();
                String userAgentType = (String) map.get("userAgentType");
                int type;
                switch (userAgentType) {
                    case "AOL":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_AOL;
                        break;
                    case "chrome":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_CHROME;
                        break;
                    case "firefox":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_FIREFOX;
                        break;
                    case "IE":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_INTERNET_EXPLORER;
                        break;
                    case "netscape":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_NETSCAPE;
                        break;
                    case "opera":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_OPERA;
                        break;
                    case "safari":
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_SAFARI;
                        break;
                    default:
                        type = UserAgentDbDataFaker.USER_AGENT_TYPE_ANY;
                }
                userAgentDbDataFaker.setUserAgentType(type);
                return userAgentDbDataFaker;
            }

            @Override
            public JComponent component(UserAgentDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel browserLabel = new JLabel("浏览器：");
                JComboBox<String> browserComboBox = new JComboBox<>();
                browserComboBox.setName("userAgentType");
                browserComboBox.addItem("任意");
                browserComboBox.addItem("AOL");
                browserComboBox.addItem("chrome");
                browserComboBox.addItem("firefox");
                browserComboBox.addItem("IE");
                browserComboBox.addItem("netscape");
                browserComboBox.addItem("opera");
                browserComboBox.addItem("safari");

                if (dbDataFaker != null) {
                    browserComboBox.setSelectedIndex(dbDataFaker.getUserAgentType());
                } else {
                    browserComboBox.setSelectedIndex(0);
                }

                UIUtil.bag(panel, browserLabel, 0, 1, 0, 0);
                UIUtil.bag(panel, browserComboBox, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });

        fakeProviders.add(new FakerProvider<UniversityDbDataFaker>() {
            @Override
            public String name() {
                return "高校生成器";
            }

            @Override
            public UniversityDbDataFaker faker(Map<String, Object> map) {
                return new UniversityDbDataFaker();
            }

            @Override
            public JComponent component(UniversityDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<CompanyDbDataFaker>() {
            @Override
            public String name() {
                return "公司名称生成器";
            }

            @Override
            public CompanyDbDataFaker faker(Map<String, Object> map) {
                return new CompanyDbDataFaker();
            }

            @Override
            public JComponent component(CompanyDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<EmailDbDataFaker>() {
            @Override
            public String name() {
                return "邮箱号生成器";
            }

            @Override
            public EmailDbDataFaker faker(Map<String, Object> map) {
                return new EmailDbDataFaker();
            }

            @Override
            public JComponent component(EmailDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<JobDbDataFaker>() {
            @Override
            public String name() {
                return "职位生成器";
            }

            @Override
            public JobDbDataFaker faker(Map<String, Object> map) {
                return new JobDbDataFaker();
            }

            @Override
            public JComponent component(JobDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<BullShitDbDataFaker>() {
            @Override
            public String name() {
                return "狗屁不通文章生成器";
            }

            @Override
            public BullShitDbDataFaker faker(Map<String, Object> map) {
                return new BullShitDbDataFaker();
            }

            @Override
            public JComponent component(BullShitDbDataFaker dbDataFaker) {
                return new JPanel();
            }
        });

        fakeProviders.add(new FakerProvider<BloomFilterDbDataFaker>() {
            @Override
            public String name() {
                return "布隆过滤增强";
            }

            @Override
            public BloomFilterDbDataFaker faker(Map<String, Object> map) {
                BloomFilterDbDataFaker bloomFilterDbDataFaker = new BloomFilterDbDataFaker();
                bloomFilterDbDataFaker.setDbDataFaker((IDbDataFaker) map.get("wrapFaker"));
                bloomFilterDbDataFaker.setMaxRetry(Long.parseLong(map.get("maxRetry").toString()));
                return bloomFilterDbDataFaker;
            }

            @Override
            public JComponent component(BloomFilterDbDataFaker dbDataFaker) {
                JPanel panel = new JPanel(new GridBagLayout());
                JLabel maxRetryLabel = new JLabel("最大重试次数：");
                JTextField maxRetryField = new JTextField("1000000");
                maxRetryField.setName("maxRetry");
                JLabel wrapLabel = new JLabel("嵌套生成器：");
                AttachField wrapField = new AttachField();
                wrapField.setName("wrapFaker");

                wrapField.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == 1 && e.getClickCount() == 2) {
                            JTextField source = (JTextField) e.getSource();
                            Component containingWindow = SunToolkit.getContainingWindow(source);
                            showDataFakerBuildDialog(dbDataFaker, containingWindow, wrapField::setAttach);
                        }
                    }
                });

                if (dbDataFaker != null) {
                    wrapField.setAttach(dbDataFaker.getDbDataFaker());
                }

                UIUtil.bag(panel, maxRetryLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, maxRetryField, 0, 1, 1, 0);
                UIUtil.bag(panel, wrapLabel, 1, 1, 0, 0);
                UIUtil.bag(panel, wrapField, 0, 1, 1, 0);
                UIUtil.bag(panel, Box.createGlue(), 0, 1, 1, 1);
                return panel;
            }
        });
    }

    static void showDataFakerBuildDialog(IDbDataFaker dbDataFaker, Component parent, Consumer<IDbDataFaker> consumer) {
        JPanel body = fakeDataRulePanel(dbDataFaker);
        SDialog.builder()
                .parent(parent)
                .title("创建生成器")
                .body(body)
                .foot()
                .footMouseListener(new SDialog.FootMouseListener() {
                    @Override
                    public void mouseClicked(SDialog.FootMouseEvent footMouseEvent) {
                        if (footMouseEvent.getFootIdx() == 2) {
                            try {
                                Map<String, Object> form = UIUtil.form(body);
                                IDbDataFaker dataFaker = genFakerFromInputMap(form);
                                consumer.accept(dataFaker);
                            } catch (Exception e1) {
                                UIUtil.showError("创建生成器错误：" + e1.getMessage());
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

    public static IDbDataFaker genFakerFromInputMap(Map<String, Object> map) {
        String fakerName = map.get("fakeDataBuilder").toString();
        for (FakerProvider fakeProvider : fakeProviders) {
            if (fakeProvider.name().equals(fakerName)) {
                return fakeProvider.faker(map);
            }
        }
        throw new IllegalArgumentException("未找到生成器：" + fakerName);
    }

    private static JPanel fakeDataRulePanel(IDbDataFaker dbDataFaker) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel genLabel = new JLabel("生成器：");
        JComboBox<String> genComboBox = new JComboBox<>();
        genComboBox.setName("fakeDataBuilder");

        CardLayout cardLayout = new CardLayout();
        JPanel comPanel = new JPanel(cardLayout);
        for (FakerProvider fakerProvider : fakeProviders) {
            String name = fakerProvider.name();
            genComboBox.addItem(name);
            if (dbDataFaker != null) {
                if (ClassUtil.getTypeArgument(fakerProvider.getClass()) == dbDataFaker.getClass()) {
                    comPanel.add(name, fakerProvider.component(dbDataFaker));
                } else {
                    comPanel.add(name, fakerProvider.component(null));
                }
            } else {
                comPanel.add(name, fakerProvider.component(null));
            }

        }

        genComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    cardLayout.show(comPanel, e.getItem().toString());
                }
            }
        });

        if (dbDataFaker != null) {
            for (FakerProvider fakeProvider : fakeProviders) {
                try {
                    Class<?> typeArgument = ClassUtil.getTypeArgument(fakeProvider.getClass());
                    if (typeArgument == dbDataFaker.getClass()) {
                        genComboBox.setSelectedItem(fakeProvider.name());
                        break;
                    }
                } catch (Exception ignore) {
                }

            }
        }


        UIUtil.bag(panel, genLabel, 1, 1, 0, 0);
        UIUtil.bag(panel, genComboBox, 0, 1, 1, 0);
        UIUtil.bag(panel, comPanel, 0, 1, 1, 1);

        panel.setPreferredSize(new Dimension(400, 450));
        return panel;
    }


}
