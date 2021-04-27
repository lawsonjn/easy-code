package com.developcollect.easycode.ui;

import cn.hutool.core.lang.PatternPool;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.util.regex.Pattern;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/31 9:29
 */
public class StintDocument extends PlainDocument {

    private String stint;
    private boolean useRegex = false;

    public StintDocument(String stint) {
        this.stint = stint;
    }

    public String getStint() {
        return stint;
    }

    public void setStint(String stint) {
        this.stint = stint;
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    @Override
    public void insertString(int var1, String var2, AttributeSet var3) throws BadLocationException {
        if (var3 != null || this.match(var2)) {
            super.insertString(var1, var2, var3);
        }
    }

    private boolean match(String var1) {
        String stint = getStint();
        if (stint == null) {
            return true;
        }
        try {
            if (isUseRegex()) {
                final Pattern pattern = PatternPool.get(stint);
                return pattern.matcher(var1).find();
            } else {
                char[] chars = var1.toCharArray();
                for (char c : chars) {
                    if (stint.indexOf(c) < 0) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception var3) {
            return false;
        }
    }
}
