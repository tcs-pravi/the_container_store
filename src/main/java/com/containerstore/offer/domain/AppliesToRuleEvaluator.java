package com.containerstore.offer.domain;

import com.containerstore.common.thirdparty.immutables.InterfaceBasedBuilderStyle;
import org.immutables.value.Value;

import java.util.Map;

import static com.containerstore.common.thirdparty.mvel.MvelHelper.evaluateExpression;

@Value.Immutable
@InterfaceBasedBuilderStyle
public interface AppliesToRuleEvaluator {

    Map<String, Object> getVariables();

    default boolean applies(String appliesToRule) {
        return evaluateExpression(appliesToRule, getVariables());
    }
}
