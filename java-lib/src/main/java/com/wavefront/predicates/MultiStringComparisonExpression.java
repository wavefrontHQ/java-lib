package com.wavefront.predicates;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.wavefront.common.PatternMatchPredicate;

import wavefront.report.Annotation;
import wavefront.report.ReportHistogram;
import wavefront.report.ReportMetric;
import wavefront.report.ReportPoint;
import wavefront.report.Span;

import static com.wavefront.predicates.PredicateEvalExpression.asDouble;

/**
 * An eval expression that compares a collection of strings
 *
 * @author vasily@wavefront.com
 */
public class MultiStringComparisonExpression implements PredicateEvalExpression {

  private final String scope;
  private final StringExpression arg;
  private final PredicateMatchOp matchOp;
  private final BiFunction<String, String, Boolean> cmp;

  private MultiStringComparisonExpression(String scope, StringExpression arg,
                                          PredicateMatchOp matchOp,
                                          BiFunction<String, String, Boolean> cmp) {
    this.scope = scope;
    this.arg = arg;
    this.matchOp = matchOp;
    this.cmp = cmp;
  }

  @Override
  public double getValue(Object entity) {
    if (entity == null) {
      return 0;
    }
    List<String> annotations;
    if (entity instanceof Span) {
      if (scope.equals("spanName")) {
        annotations = ImmutableList.of(((Span) entity).getName());
      } else if (scope.equals("sourceName")){
        annotations = ImmutableList.of(((Span) entity).getSource());
      } else {
        annotations = ((Span) entity).getAnnotations().stream().
            filter(a -> a.getKey().equals(scope)).
            map(Annotation::getValue).collect(Collectors.toList());
      }
    } else if (entity instanceof ReportMetric) {
      if (scope.equals("metricName")) {
        annotations = ImmutableList.of(((ReportMetric) entity).getMetric());
      } else if (scope.equals("sourceName")){
        annotations = ImmutableList.of(((ReportMetric) entity).getHost());
      } else {
        annotations = ((ReportMetric) entity).getAnnotations().stream().
            filter(a -> a.getKey().equals(scope)).
            map(Annotation::getValue).collect(Collectors.toList());
      }
    } else if (entity instanceof ReportHistogram) {
      if (scope.equals("metricName")) {
        annotations = ImmutableList.of(((ReportHistogram) entity).getMetric());
      } else if (scope.equals("sourceName")){
        annotations = ImmutableList.of(((ReportHistogram) entity).getHost());
      } else {
        annotations = ((ReportHistogram) entity).getAnnotations().stream().
            filter(a -> a.getKey().equals(scope)).
            map(Annotation::getValue).collect(Collectors.toList());
      }
    } else if (entity instanceof ReportPoint) {
      if (scope.equals("metricName")) {
        annotations = ImmutableList.of(((ReportPoint) entity).getMetric());
      } else if (scope.equals("sourceName")){
        annotations = ImmutableList.of(((ReportPoint) entity).getHost());
      } else {
        annotations = ImmutableList.of(((ReportPoint) entity).getAnnotations().
            getOrDefault(scope, ""));
      }
    } else {
      throw new IllegalArgumentException(entity.getClass().getCanonicalName() +
          " is not supported!");
    }

    switch (matchOp) {
      case ALL:
        return asDouble(annotations.stream().allMatch(x -> cmp.apply(x, arg.getString(entity))));
      case ANY:
        return asDouble(annotations.stream().anyMatch(x -> cmp.apply(x, arg.getString(entity))));
      case NONE:
        return asDouble(annotations.stream().noneMatch(x -> cmp.apply(x, arg.getString(entity))));
      default :
        throw new IllegalArgumentException("Unknown matchOp type: " + matchOp);
    }
  }

  public static PredicateEvalExpression of(String scope, StringExpression argument,
                                           PredicateMatchOp matchOp, String op) {
    switch (op) {
      case "=":
      case "equals":
        return new MultiStringComparisonExpression(scope, argument, matchOp, String::equals);
      case "startsWith":
        return new MultiStringComparisonExpression(scope, argument, matchOp, String::startsWith);
      case "endsWith":
        return new MultiStringComparisonExpression(scope, argument, matchOp, String::endsWith);
      case "contains":
        return new MultiStringComparisonExpression(scope, argument, matchOp, String::contains);
      case "matches":
        Predicate<String> match = PatternMatchPredicate.buildPredicate(argument.getString(null), false);
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            (s1, s2) -> match.test(s1));
      case "regexMatch":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            new CachingRegexMatcher());
      case "equalsIgnoreCase":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            String::equalsIgnoreCase);
      case "startsWithIgnoreCase":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            StringUtils::startsWithIgnoreCase);
      case "endsWithIgnoreCase":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            StringUtils::endsWithIgnoreCase);
      case "containsIgnoreCase":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            StringUtils::containsIgnoreCase);
      case "matchesIgnoreCase":
        Predicate<String> matchCI = PatternMatchPredicate.buildPredicate(argument.getString(null), true);
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            (s1, s2) -> matchCI.test(s1));
      case "regexMatchIgnoreCase":
        return new MultiStringComparisonExpression(scope, argument, matchOp,
            new CachingRegexMatcher(Pattern.CASE_INSENSITIVE));
      default:
        throw new IllegalArgumentException(op + " is not handled");
    }
  }
}
