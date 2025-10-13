package com.loan.document;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to parse LLM responses for decision outcomes.
 * Detects Pass/Fail markers in multiple common formats.
 */
public final class LlmDecisionParser {

  // Expanded, tolerant patterns for different phrasing and formatting styles
  private static final Pattern[] DECISION_PATTERNS = new Pattern[] {
      // "**Pass/Fail:** Pass" or next-line bullet
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*pass\\s*/\\s*fail\\s*[:\\-\\u2013\\u2014\\uff1a]?\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // "**Pass or Fail:** - Pass"
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*pass\\s*or\\s*fail\\s*[:\\-\\u2013\\u2014\\uff1a]*\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // "**Status:** Pass"
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*status\\s*[:\\-\\u2013\\u2014\\uff1a]\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // "**Decision:** Pass"
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*decision\\s*[:\\-\\u2013\\u2014\\uff1a]\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // "**Result:** Pass"
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*result\\s*[:\\-\\u2013\\u2014\\uff1a]\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // "**Evaluation:** Pass"
      Pattern.compile("(?ims)^\\s*\\*{0,2}\\s*evaluation\\s*[:\\-\\u2013\\u2014\\uff1a]\\s*(?:\\R\\s*[-*]?\\s*)?(pass|fail)\\b"),
      // Fallback: find "Evaluation" followed by Pass/Fail within ~200 characters
      Pattern.compile("(?is)\\bevaluation\\b[\\s\\S]{0,200}?\\b(pass|fail)\\b")
  };

  /**
   * Normalize the raw string: remove markdown and normalize punctuation.
   */
  private static String normalize(String s) {
    if (s == null) return null;
    s = Normalizer.normalize(s, Normalizer.Form.NFKC);
    // strip **bold** markdown
    s = s.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
    // normalize common Unicode dashes to ASCII hyphen
    s = s.replace('\u2013', '-').replace('\u2014', '-');
    return s;
  }

  /**
   * Parse a string and return PASS, FAIL, or UNKNOWN.
   * If multiple matches are found, FAIL takes precedence.
   */
  public static Decision parseDecision(String content) {
    if (content == null) return Decision.UNKNOWN;
    String text = normalize(content);

    boolean sawPass = false;
    boolean sawFail = false;

    for (Pattern p : DECISION_PATTERNS) {
      Matcher m = p.matcher(text);
      while (m.find()) {
        String val = m.group(1).trim().toUpperCase(Locale.ROOT);
        if ("FAIL".equals(val)) {
          sawFail = true;
        } else if ("PASS".equals(val)) {
          sawPass = true;
        }
      }
    }

    if (sawFail) return Decision.FAIL;    // FAIL dominates if present anywhere
    if (sawPass) return Decision.PASS;
    return Decision.UNKNOWN;
  }

  private LlmDecisionParser() {}
}

