package com.developcollect.easycode.core.db;

import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import lombok.Getter;


public class TableInfoWrap {

    private TableInfoWrap(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    @Getter
    private TableInfo tableInfo;

    public static TableInfoWrap of(TableInfo tableInfo) {
        return new TableInfoWrap(tableInfo);
    }

    @Override
    public String toString() {
        return tableInfo.getName();
    }
}
