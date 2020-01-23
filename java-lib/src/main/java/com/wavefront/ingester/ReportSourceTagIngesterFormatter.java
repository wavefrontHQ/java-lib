package com.wavefront.ingester;

import wavefront.report.ReportSourceTag;
import wavefront.report.SourceOperationType;
import wavefront.report.SourceTagAction;

import java.util.List;
import java.util.function.Supplier;

/**
 * This class can be used to parse sourceTags and description.
 *
 * @author Suranjan Pramanik (suranjan@wavefront.com).
 * @author vasily@wavefront.com
 */
public class ReportSourceTagIngesterFormatter extends AbstractIngesterFormatter<ReportSourceTag> {

  private ReportSourceTagIngesterFormatter(List<FormatterElement<ReportSourceTag>> elements) {
    super(elements);
  }

  public static SourceTagIngesterFormatBuilder newBuilder() {
    return new SourceTagIngesterFormatBuilder();
  }

  @Override
  public ReportSourceTag drive(String input, Supplier<String> defaultHostNameSupplier,
                               String customerId, List<String> customerSourceTags) {
    ReportSourceTag sourceTag = new ReportSourceTag();
    StringParser parser = PARSER.get();
    parser.parse(input);
    try {
      for (FormatterElement<ReportSourceTag> element : elements) {
        element.consume(parser, sourceTag);
      }
    } catch (Exception ex) {
      throw new RuntimeException("Could not parse: " + input, ex);
    }

    if (sourceTag.getAnnotations() == null || sourceTag.getAnnotations().isEmpty()) {
      if (!(sourceTag.getOperation() == SourceOperationType.SOURCE_DESCRIPTION &&
          sourceTag.getAction() == SourceTagAction.DELETE)) {
        throw new IllegalArgumentException("No data provided for operation `" +
            sourceTag.getOperation() + "` action: " + sourceTag.getAction());
      }
    } else if (sourceTag.getOperation() == SourceOperationType.SOURCE_DESCRIPTION &&
        sourceTag.getAnnotations().size() > 1) {
      throw new IllegalArgumentException("Only one description expected");
    }
    return ReportSourceTag.newBuilder(sourceTag).build();
  }

  public static class SourceTagIngesterFormatBuilder extends IngesterFormatBuilder<ReportSourceTag> {
    @Override
    public ReportSourceTagIngesterFormatter build() {
      return new ReportSourceTagIngesterFormatter(elements);
    }
  }
}
