package com.developcollect.easycode.core.fakedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 暂未实现，留待后续
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/26 10:23
 */
public class TreeEnhanceDbDataFaker implements IDbDataFaker{

    private Map<Object, DbNode> dbNodeMap;

    private Object rootParent;

    @Override
    public Object getFakerData(FakeDataContext context) {
        Object id = null;
        dbNodeMap.computeIfAbsent(id, k -> new DbNode(id, new ArrayList<>(), new ArrayList<>(), 1, 1));
        return null;
    }





    private static class DbNode {
        Object id;
        List<Object> parentIds;
        List<Object> childIds;

        public DbNode(Object id, List<Object> parentIds, List<Object> childIds, int deep, int rank) {
            this.id = id;
            this.parentIds = parentIds;
            this.childIds = childIds;
            this.deep = deep;
            this.rank = rank;
        }

        /**
         * 深度
         */
        int deep;

        /**
         * 父节点的第几个字节点
         */
        int rank;

    }
}
