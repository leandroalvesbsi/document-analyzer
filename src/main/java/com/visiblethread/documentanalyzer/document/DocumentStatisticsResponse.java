package com.visiblethread.documentanalyzer.document;

import java.util.Map;

public record DocumentStatisticsResponse(int wordCount, String shortestWord, String longestWord, Map<String, Integer> wordFrequency) {}
