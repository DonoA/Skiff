package io.dallen.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Acts like a switch statement where .equals is used instead of the normal '=='. Cases from the switch do not cause
 * side effects but rather return a result to be returned by the whole operation.
 *
 * @param <InputType>    The types that will be compared against
 * @param <OutputType>   The type of the switch statement result
 * @param <ContextClass> The class of the context object passed to each case statement when executed
 */
public class AdvancedSwitch<InputType, OutputType, ContextClass> {

    private List<SwitchCase<InputType, OutputType, ContextClass>> cases = new ArrayList<>();

    private CaseHandler<ContextClass, OutputType> defaultCase = null;

    public AdvancedSwitch<InputType, OutputType, ContextClass> addCase(CaseCondition<InputType> cond,
                                                                       CaseHandler<ContextClass, OutputType> action) {
        cases.add(new SwitchCase<>(cond, action));
        return this;
    }

    public AdvancedSwitch<InputType, OutputType, ContextClass> setDefault(CaseHandler<ContextClass, OutputType> action) {
        this.defaultCase = action;
        return this;
    }

    public OutputType execute(InputType input, ContextClass context) {
        for (SwitchCase<InputType, OutputType, ContextClass> c : cases) {
            if (c.cond.apply(input)) {
                return c.action.apply(context);
            }
        }
        if (defaultCase != null) {
            return defaultCase.apply(context);
        }
        return null;
    }

    private static class SwitchCase<InputType, OutputType, ContextClass> {
        private final CaseCondition<InputType> cond;
        private final CaseHandler<ContextClass, OutputType> action;

        SwitchCase(CaseCondition<InputType> cond,
                   CaseHandler<ContextClass, OutputType> action) {
            this.cond = cond;
            this.action = action;
        }
    }

    interface CaseCondition<InputType> {
        boolean apply(InputType input);
    }

    interface CaseHandler<ContextClass, OutputType> {
        OutputType apply(ContextClass input);
    }
}
