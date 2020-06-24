package com.wavefront.predicates;

import java.util.function.Predicate;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.google.common.annotations.VisibleForTesting;
import com.wavefront.common.TimeProvider;

import condition.parser.EvalExpressionLexer;
import condition.parser.EvalExpressionParser;

/**
 * Utility class for parsing expressions.
 *
 * @author vasily@wavefront.com
 */
public abstract class Predicates {

  private Predicates() {
  }

  /**
   * Parses an expression string into a {@link Predicate<T>}.
   *
   * @param predicateString expression string to parse.
   * @return predicate
   */
  public static <T> Predicate<T> fromEvalExpression(String predicateString) {
    return new ExpressionPredicate<>(parseEvalExpression(predicateString));
  }

  @VisibleForTesting
  static EvalExpression parseEvalExpression(String predicateString) {
    return parseEvalExpression(predicateString, System::currentTimeMillis);
  }

  @VisibleForTesting
  static EvalExpression parseEvalExpression(String predicateString, TimeProvider timeProvider) {
    EvalExpressionLexer lexer = new EvalExpressionLexer(CharStreams.fromString(predicateString));
    lexer.removeErrorListeners();
    ErrorListener errorListener = new ErrorListener();
    lexer.addErrorListener(errorListener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    EvalExpressionParser parser = new EvalExpressionParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    EvalExpressionVisitorImpl visitor = new EvalExpressionVisitorImpl(timeProvider);
    EvalExpressionParser.ProgramContext context = parser.program();
    EvalExpression result = (EvalExpression) context.evalExpression().accept(visitor);
    if (errorListener.getErrors().length() == 0) {
      return result;
    } else {
      throw new IllegalArgumentException(errorListener.getErrors().toString());
    }
  }
}
