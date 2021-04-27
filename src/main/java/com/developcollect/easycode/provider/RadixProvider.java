package com.developcollect.easycode.provider;

import cn.hutool.core.util.StrUtil;
import com.developcollect.core.utils.MathUtil;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.ui.StintDocument;
import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/26 10:18
 */
public class RadixProvider implements Provider {

    @Override
    public int order() {
        return 4000;
    }

    @Override
    public String getTitle() {
        return "进制";
    }

    @Override
    public Component getComponent(JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));


        JPanel leftPanel = new JPanel(new GridBagLayout());

        DocumentListener documentListener = new DocumentListener() {

            private volatile boolean converting = false;


            @Override
            public void insertUpdate(DocumentEvent e) {
                if (converting) {
                    return;
                }
                convertRadix(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (converting) {
                    return;
                }
                convertRadix(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private synchronized void convertRadix(DocumentEvent e) {
                converting = true;
                DocumentProxy documentProxy = (DocumentProxy) e.getDocument();
                JTextField thisField = documentProxy.getField();
                String text = thisField.getText();
                if (StrUtil.isBlank(text)) {
                    Component[] components = thisField.getParent().getComponents();
                    for (Component component : components) {
                        if (component instanceof JTextField) {
                            JTextField otherField = (JTextField) component;
                            try {
                                Integer.parseInt(otherField.getName());
                                otherField.setText("");
                            } catch (Exception ignore) {
                            }
                        }
                    }
                } else {
                    int thisRadix = Integer.parseInt(thisField.getName());

                    Component[] components = thisField.getParent().getComponents();
                    for (Component component : components) {
                        if (component instanceof JTextField) {
                            JTextField otherField = (JTextField) component;
                            try {
                                int otherRadix = Integer.parseInt(otherField.getName());
                                String convertRadix = MathUtil.convertRadix(text, thisRadix, otherRadix);
                                otherField.setText(convertRadix);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
                converting = false;
            }
        };

        int[] radixArr = {2, 8, 10, 16, 26, 32, 36, 52, 58, 62};

        for (int radix : radixArr) {
            String radixStr = radix < 10 ? " " + radix : String.valueOf(radix);
            JLabel label = new JLabel(radixStr + "进制:");
            JTextField field = new JTextField();
            field.setDocument(new StintDocument(MathUtil.RADIX_TABLE.substring(0, radix)));
            field.setName(String.valueOf(radix));
            field.setDocument(new DocumentProxy(field));
            field.getDocument().addDocumentListener(documentListener);

            UIUtil.bag(leftPanel, label, 1, 1, 0, 0);
            UIUtil.bag(leftPanel, field, 0, 1, 1, 0);
        }


        UIUtil.bag(panel, leftPanel, 10, 1, 1, 1);
        return panel;
    }


    private static class DocumentProxy implements Document {

        private JTextField field;
        private Document document;
        private List<DocumentListener> documentListenerList;

        public DocumentProxy(JTextField field) {
            this.field = field;
            this.document = field.getDocument();
            this.documentListenerList = new ArrayList<>();
            this.document.addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    DocumentEventProxy documentEventProxy = new DocumentEventProxy(e, DocumentProxy.this);
                    for (DocumentListener documentListener : documentListenerList) {
                        documentListener.insertUpdate(documentEventProxy);
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    DocumentEventProxy documentEventProxy = new DocumentEventProxy(e, DocumentProxy.this);
                    for (DocumentListener documentListener : documentListenerList) {
                        documentListener.removeUpdate(documentEventProxy);
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    DocumentEventProxy documentEventProxy = new DocumentEventProxy(e, DocumentProxy.this);
                    for (DocumentListener documentListener : documentListenerList) {
                        documentListener.changedUpdate(documentEventProxy);
                    }
                }
            });
        }

        @Override
        public int getLength() {
            return document.getLength();
        }

        @Override
        public void addDocumentListener(DocumentListener listener) {
            this.documentListenerList.add(listener);
        }

        @Override
        public void removeDocumentListener(DocumentListener listener) {
            this.documentListenerList.remove(listener);
        }

        @Override
        public void addUndoableEditListener(UndoableEditListener listener) {
            document.addUndoableEditListener(listener);
        }

        @Override
        public void removeUndoableEditListener(UndoableEditListener listener) {
            document.removeUndoableEditListener(listener);
        }

        @Override
        public Object getProperty(Object key) {
            return document.getProperty(key);
        }

        @Override
        public void putProperty(Object key, Object value) {
            document.putProperty(key, value);
        }

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            document.remove(offs, len);
        }

        @Override
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            document.insertString(offset, str, a);
        }

        @Override
        public String getText(int offset, int length) throws BadLocationException {
            return document.getText(offset, length);
        }

        @Override
        public void getText(int offset, int length, Segment txt) throws BadLocationException {
            document.getText(offset, length, txt);
        }

        @Override
        public Position getStartPosition() {
            return document.getStartPosition();
        }

        @Override
        public Position getEndPosition() {
            return document.getEndPosition();
        }

        @Override
        public Position createPosition(int offs) throws BadLocationException {
            return document.createPosition(offs);
        }

        @Override
        public Element[] getRootElements() {
            return document.getRootElements();
        }

        @Override
        public Element getDefaultRootElement() {
            return document.getDefaultRootElement();
        }

        @Override
        public void render(Runnable r) {
            document.render(r);
        }


        public JTextField getField() {
            return field;
        }
    }

    public static class DocumentEventProxy implements DocumentEvent {

        private Document document;
        private DocumentEvent event;

        public DocumentEventProxy(DocumentEvent event, Document document) {
            this.document = document;
            this.event = event;
        }

        @Override
        public int getOffset() {
            return event.getOffset();
        }

        @Override
        public int getLength() {
            return event.getLength();
        }

        @Override
        public Document getDocument() {
            return this.document;
        }

        @Override
        public EventType getType() {
            return event.getType();
        }

        @Override
        public ElementChange getChange(Element elem) {
            return event.getChange(elem);
        }
    }
}
