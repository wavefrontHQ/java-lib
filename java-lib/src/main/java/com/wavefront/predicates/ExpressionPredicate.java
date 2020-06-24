package com.wavefront.predicates;

import java.util.function.Predicate;

import static com.wavefront.predicates.EvalExpression.isTrue;

/**
 * {@link EvalExpression} to {@link Predicate<T>} adapter.
 *
 * @author vasily@wavefront.com.
 */
public class ExpressionPredicate<T> implements Predicate<T> {
  private final EvalExpression wrapped;

  public ExpressionPredicate(EvalExpression wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean test(T t) {
    return isTrue(wrapped.getValue(t));
  }
}
