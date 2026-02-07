/*
 * Decompiled with CFR 0.152.
 */
package com.illtamer.plugin.magicgemtiny.libs.ikexpression.op;

import com.illtamer.plugin.magicgemtiny.libs.ikexpression.IllegalExpressionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.BaseDataMeta;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Constant;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.op.define.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum Operator {
    NOT("!", 80, 1),
    NG("-", 80, 1),
    MUTI("*", 70, 2),
    DIV("/", 70, 2),
    MOD("%", 70, 2),
    PLUS("+", 60, 2),
    MINUS("-", 60, 2),
    LT("<", 50, 2),
    LE("<=", 50, 2),
    GT(">", 50, 2),
    GE(">=", 50, 2),
    EQ("==", 40, 2),
    NEQ("!=", 40, 2),
    AND("&&", 30, 2),
    OR("||", 20, 2),
    APPEND("#", 10, 2),
    QUES("?", 0, 0),
    COLON(":", 0, 0),
    SELECT("?:", 0, 3);

    private static final Set<String> OP_RESERVE_WORD;
    private static final HashMap<Operator, IOperatorExecution> OP_EXEC_MAP;
    private final String token;
    private final int priority;
    private final int opType;

    private Operator(String token, int priority, int opType) {
        this.token = token;
        this.priority = priority;
        this.opType = opType;
    }

    public static boolean isLegalOperatorToken(String tokenText) {
        return OP_RESERVE_WORD.contains(tokenText);
    }

    public String getToken() {
        return this.token;
    }

    public int getPiority() {
        return this.priority;
    }

    public int getOpType() {
        return this.opType;
    }

    public Constant execute(Constant[] args) throws IllegalExpressionException {
        IOperatorExecution opExec = OP_EXEC_MAP.get((Object)this);
        if (opExec == null) {
            throw new IllegalStateException("\u7cfb\u7edf\u5185\u90e8\u9519\u8bef\uff1a\u627e\u4e0d\u5230\u64cd\u4f5c\u7b26\u5bf9\u5e94\u7684\u6267\u884c\u5b9a\u4e49");
        }
        return opExec.execute(args);
    }

    public Constant verify(int opPositin, BaseDataMeta[] args) throws IllegalExpressionException {
        IOperatorExecution opExec = OP_EXEC_MAP.get((Object)this);
        if (opExec == null) {
            throw new IllegalStateException("\u7cfb\u7edf\u5185\u90e8\u9519\u8bef\uff1a\u627e\u4e0d\u5230\u64cd\u4f5c\u7b26\u5bf9\u5e94\u7684\u6267\u884c\u5b9a\u4e49");
        }
        return opExec.verify(opPositin, args);
    }

    static {
        OP_RESERVE_WORD = new HashSet<String>();
        OP_EXEC_MAP = new HashMap();
        OP_RESERVE_WORD.add(NOT.getToken());
        OP_RESERVE_WORD.add(NG.getToken());
        OP_RESERVE_WORD.add(MUTI.getToken());
        OP_RESERVE_WORD.add(DIV.getToken());
        OP_RESERVE_WORD.add(MOD.getToken());
        OP_RESERVE_WORD.add(PLUS.getToken());
        OP_RESERVE_WORD.add(MINUS.getToken());
        OP_RESERVE_WORD.add(LT.getToken());
        OP_RESERVE_WORD.add(LE.getToken());
        OP_RESERVE_WORD.add(GT.getToken());
        OP_RESERVE_WORD.add(GE.getToken());
        OP_RESERVE_WORD.add(EQ.getToken());
        OP_RESERVE_WORD.add(NEQ.getToken());
        OP_RESERVE_WORD.add(AND.getToken());
        OP_RESERVE_WORD.add(OR.getToken());
        OP_RESERVE_WORD.add(APPEND.getToken());
        OP_RESERVE_WORD.add(SELECT.getToken());
        OP_RESERVE_WORD.add(QUES.getToken());
        OP_RESERVE_WORD.add(COLON.getToken());
        OP_EXEC_MAP.put(NOT, new Op_NOT());
        OP_EXEC_MAP.put(NG, new Op_NG());
        OP_EXEC_MAP.put(MUTI, new Op_MUTI());
        OP_EXEC_MAP.put(DIV, new Op_DIV());
        OP_EXEC_MAP.put(MOD, new Op_MOD());
        OP_EXEC_MAP.put(PLUS, new Op_PLUS());
        OP_EXEC_MAP.put(MINUS, new Op_MINUS());
        OP_EXEC_MAP.put(LT, new Op_LT());
        OP_EXEC_MAP.put(LE, new Op_LE());
        OP_EXEC_MAP.put(GT, new Op_GT());
        OP_EXEC_MAP.put(GE, new Op_GE());
        OP_EXEC_MAP.put(EQ, new Op_EQ());
        OP_EXEC_MAP.put(NEQ, new Op_NEQ());
        OP_EXEC_MAP.put(AND, new Op_AND());
        OP_EXEC_MAP.put(OR, new Op_OR());
        OP_EXEC_MAP.put(APPEND, new Op_APPEND());
        OP_EXEC_MAP.put(SELECT, new Op_SELECT());
        OP_EXEC_MAP.put(QUES, new Op_QUES());
        OP_EXEC_MAP.put(COLON, new Op_COLON());
    }
}
