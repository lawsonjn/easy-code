package com.developcollect.easycode.core.db;

import com.baomidou.mybatisplus.generator.config.po.TableField;
import lombok.Getter;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 13:39
 */
public class TableFieldWrap {


    private TableFieldWrap(TableField tableField) {
        this.tableField = tableField;
    }

    @Getter
    private TableField tableField;

    public static TableFieldWrap of(TableField tableField) {
        return new TableFieldWrap(tableField);
    }

    @Override
    public String toString() {
        return tableField.getName();
    }

}
