import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.PreparedExpression;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDecode {

    private static final String AMOUNT_EXPR = "100+2900*$RANDOM()";
    private static final String LORE_EXPR = "60 + $LORE:成功率增幅$";
    private static final String LORE_EXPR_MULTIPLE = """
$WORD:武器品质: 白?100$
+ $WORD:武器品质: 绿?80$
+ $WORD:武器品质: 蓝?70$
+ $WORD:武器品质: 紫?60$
+ $WORD:武器品质: 红?50$
+ $WORD:武器品质: 橙?40$
+ $WORD:武器品质: 黑?30$
+ $WORD:武器品质: 粉?30$
+ $WORD:武器品质: 金?20$""";
    private static final String LORE_REPLACED = "var_1";


    public static void main(String[] args) {
        List<Variable> variables = new ArrayList<>();
        variables.add(Variable.createVariable(LORE_REPLACED, 5.5));
        PreparedExpression prepareded = ExpressionEvaluator.preparedCompile("60 + " + LORE_REPLACED, variables);
        Object execute = prepareded.execute();
        System.out.println(execute instanceof Double);
        System.out.println(execute);
//        onRandom();
    }

    protected static void onRandom() {
        List<Variable> variables = new ArrayList<>();
        PreparedExpression prepareded = ExpressionEvaluator.preparedCompile(AMOUNT_EXPR, variables);
        System.out.println(prepareded.execute());
        System.out.println(prepareded.execute());
    }

}
