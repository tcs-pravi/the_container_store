package com.containerstore.common.thirdparty.mvel;

import com.containerstore.common.base.money.Money;
import com.containerstore.offer.domain.OfferOrder;
import com.containerstore.offer.domain.OfferOrderFulfillmentGroup;
import com.containerstore.offer.domain.OfferOrderLine;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.log4j.Logger;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public final class MvelHelper {

    private static final Logger LOGGER = Logger.getLogger(MvelHelper.class);

    private static final ParserConfiguration PARSER_CONFIG;
    private static final String ENV_VAR_MVEL_CACHE_SIZE = "OFFER_MVEL_CACHE_SIZE";
    private static final int DEFAULT_MVEL_CACHE_DURATION_MINUTES = 60;
    private static final long DEFAULT_MVEL_CACHE_MAXIMUM_SIZE = 200L;
    private static LoadingCache<String, Object> compiledExpressionCache;

    static {
        long maximumSize = DEFAULT_MVEL_CACHE_MAXIMUM_SIZE;
        if (System.getenv(ENV_VAR_MVEL_CACHE_SIZE) != null) {
            try {
                maximumSize = Long.parseLong(System.getenv(ENV_VAR_MVEL_CACHE_SIZE));
            } catch (NumberFormatException ex) {
                LOGGER.error(format("Unable to parse cache size to long : %s", System.getenv(ENV_VAR_MVEL_CACHE_SIZE)));
            }
        }

        PARSER_CONFIG = new ParserConfiguration();
        try {
            PARSER_CONFIG.addPackageImport("com.containerstore.offer.domain");
            PARSER_CONFIG.addImport("max",
                    MvelHelper.class.getMethod("max", Integer.class, Integer.class));
            PARSER_CONFIG.addImport("toMoney", MvelHelper.class.getMethod("toMoney", BigDecimal.class));
            PARSER_CONFIG.addImport("isHighestPricedLine",
                    MvelHelper.class.getMethod("isHighestPricedLine", OfferOrder.class, OfferOrderLine.class));
        } catch (NoSuchMethodException ex) {
            LOGGER.error("Unable to import method into the parser context.", ex);
        }

        compiledExpressionCache
                = CacheBuilder.newBuilder()
                        .maximumSize(maximumSize)
                        .expireAfterWrite(DEFAULT_MVEL_CACHE_DURATION_MINUTES, TimeUnit.MINUTES)
                        .build(new CacheLoader<String, Object>() {
                            @Override
                            public Object load(String expression) {
                                try {
                                    return MVEL.compileExpression(expression, new ParserContext(PARSER_CONFIG));
                                } catch (Exception ex) {
                                    LOGGER.error(format(
                                            "evaluateExpression() => Error compiling mvel %s: ",
                                            expression), ex);
                                }
                                return null;
                            }
                        });
    }

    private MvelHelper() {
        throw new UnsupportedOperationException();
    }

    /*
     * Note that this method returns false if a compilation error occurs.  Any unit tests written
     * to validate the provided MVEL need to assert the positive:  assertTrue(evaluateExpression(exp, map))
     */
    public static boolean evaluateExpression(String expression, Map<String, Object> variables) {
        if (Strings.isNullOrEmpty(expression)) {
            return true;
        }

        try {
            Object compiledExpression = getCompiledExpression(expression);
            return MVEL.executeExpression(compiledExpression, variables, Boolean.class);
        } catch (RuntimeException e) {
            LOGGER.error( format("evaluateExpression() => An error occurred evaluating mvel %s:", expression), e);
            return false;
        } catch (ExecutionException ee) {
            LOGGER.error(
                    format("evaluateExpression() => Error getting mvel expression from cache %s: ", expression),
                    ee);
            return false;
        }
    }

    public static <T> Optional<T> evaluateExpressionForValue(
            String expression,
            Map<String, Object> variables,
            Class<T> expected) {
        if (Strings.isNullOrEmpty(expression)) {
            return Optional.empty();
        }

        try {
            Object compiledExpression = getCompiledExpression(expression);
            return Optional.ofNullable(MVEL.executeExpression(compiledExpression, variables, expected));
        } catch (RuntimeException e) {
            LOGGER.error( format("evaluateExpression() => An error occurred evaluating mvel %s:", expression), e);
            return Optional.empty();
        } catch (ExecutionException ee) {
            LOGGER.error(
                    format("evaluateExpression() => Error getting mvel expression from cache %s: ", expression),
                    ee);
            return Optional.empty();
        }
    }

    private static Object getCompiledExpression(String expression) throws ExecutionException {
        return compiledExpressionCache.get(expression);
    }

    public static int max(Integer a, Integer b) {
        return Math.max(a, b);
    }
    public static Money toMoney(BigDecimal value) {
        return new Money(value);
    }

    public static boolean isHighestPricedLine(OfferOrder order, OfferOrderLine line) {
        Optional<OfferOrderLine> highestPricedLine = order.getOfferOrderFulfillmentGroups().stream()
                .map(OfferOrderFulfillmentGroup::getOfferOrderLines)
                .flatMap(Collection::stream)
                .sorted((o1, o2) -> o2.amount().compareTo(o1.amount()))
                .findFirst();

        return highestPricedLine
                .map(hpl -> hpl.getLineId().equals(line.getLineId()))
                .orElse(false);
    }
}
