/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta;

import java.util.ArrayList;

public class Constant
extends BaseDataMeta {
    public Constant(BaseDataMeta.DataType dataType, Object value) {
        super(dataType, value);
        if (dataType == null) {
            throw new IllegalArgumentException("\u975e\u6cd5\u53c2\u6570\uff1a\u6570\u636e\u7c7b\u578b\u4e3a\u7a7a");
        }
        if (BaseDataMeta.DataType.DATATYPE_LIST == dataType && this.dataValue == null) {
            this.dataValue = new ArrayList(0);
        }
    }

    public Constant(Reference ref) {
        super(null, ref);
        this.setReference(true);
    }
}
