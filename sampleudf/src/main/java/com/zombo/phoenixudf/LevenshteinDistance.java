package com.zombo.phoenixudf;

import java.sql.SQLException;
import java.util.List;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode.Argument;
import org.apache.phoenix.parse.FunctionParseNode.BuiltInFunction;
import org.apache.phoenix.schema.SortOrder;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.schema.types.PVarchar;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BuiltInFunction(name=LevenshteinDistance.NAME, args={
    @Argument(allowedTypes={PVarchar.class}),
    @Argument(allowedTypes={PVarchar.class}) } )
public class LevenshteinDistance extends ScalarFunction {
    public static final String NAME = "LEVDISTANCE";
    private static final Logger logger = LoggerFactory.getLogger(LevenshteinDistance.class);

    public LevenshteinDistance() {
    }

    public LevenshteinDistance(List<Expression> children) throws SQLException {
        super(children);
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
        Expression arg1 = getChildren().get(0);
        Expression arg2 = getChildren().get(1);

        // Handle nested functions, etc.
        if (!arg1.evaluate(tuple, ptr)) {
            return false;
        }
        String s0 = (String)PVarchar.INSTANCE.toObject(ptr, getChildren().get(0).getSortOrder());
        if (!arg2.evaluate(tuple, ptr)) {
            return false;
        }
        String s1 = (String)PVarchar.INSTANCE.toObject(ptr, getChildren().get(1).getSortOrder());
        if (logger.isTraceEnabled()) {
            logger.trace("String 0 is " + s0 + " and String 1 is " + s1);
        }

        // Levenshtein algorithm copied from
        // http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }
     
        // the distance is the cost for transforming all letters in both strings
        int distance = cost[len0 - 1];
        ptr.set(PInteger.INSTANCE.toBytes(distance));
        return true;
    }

    @Override
    public SortOrder getSortOrder() {
        return getChildren().get(0).getSortOrder();
    }

    @Override
    public PDataType getDataType() {
        return PInteger.INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
