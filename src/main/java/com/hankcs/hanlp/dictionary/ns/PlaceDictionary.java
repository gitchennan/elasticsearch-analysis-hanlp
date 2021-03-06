/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 14:47</create-date>
 *
 * <copyright file="PersonDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.ns;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.NS;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.TransformMatrixDictionary;
import com.hankcs.hanlp.dictionary.WordAttribute;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.Predefine;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 地名识别用的词典，实际上是对两个词典的包装
 *
 * @author hankcs
 */
public class PlaceDictionary {
    /**
     * 地名词典
     */
    public static NSDictionary dictionary;
    /**
     * 转移矩阵词典
     */
    public static TransformMatrixDictionary<NS> transformMatrixDictionary;
    /**
     * AC算法用到的Trie树
     */
    public static AhoCorasickDoubleArrayTrie<String> trie;

    /**
     * 本词典专注的词的ID
     */
    static final int WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_PLACE);
    /**
     * 本词典专注的词的属性
     */
    static final WordAttribute ATTRIBUTE = CoreDictionary.INSTANCE.get(WORD_ID);

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();
        dictionary = new NSDictionary();
        if (dictionary.load(HanLpGlobalSettings.PlaceDictionaryPath)) {
            HanLpLogger.info(PlaceDictionary.class,
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "PlaceDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PlaceDictionaryPath));
        }
        else {
            HanLpLogger.error(PlaceDictionary.class,
                    String.format("Failed to Load dictionary[PlaceDictionary], takes %sms, path[%s] ",
                            stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PlaceDictionaryPath));
        }

        stopwatch.stop().reset().start();

        transformMatrixDictionary = new TransformMatrixDictionary<NS>(NS.class);
        if (transformMatrixDictionary.load(HanLpGlobalSettings.PlaceDictionaryTrPath)) {
            HanLpLogger.info(PlaceDictionary.class,
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "PlaceDictionary.tr", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PlaceDictionaryTrPath));
        }
        else {
            HanLpLogger.error(PlaceDictionary.class,
                    String.format("Load dictionary[PlaceDictionary.tr], takes %sms, path[%s] ",
                            stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PlaceDictionaryTrPath));
        }

        trie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();
        TreeMap<String, String> patternMap = Maps.newTreeMap();
        patternMap.put("CH", "CH");
        patternMap.put("CDH", "CDH");
        patternMap.put("CDEH", "CDEH");
        patternMap.put("GH", "GH");
        trie.build(patternMap);
    }

    /**
     * 模式匹配
     *
     * @param nsList         确定的标注序列
     * @param vertexList     原始的未加角色标注的序列
     * @param wordNetOptimum 待优化的图
     */
    public static void parsePattern(List<NS> nsList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll) {
        StringBuilder sbPattern = new StringBuilder(nsList.size());
        for (NS ns : nsList) {
            sbPattern.append(ns.toString());
        }
        String pattern = sbPattern.toString();
        final Vertex[] wordArray = vertexList.toArray(new Vertex[vertexList.size()]);
        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<String>() {
            @Override
            public void hit(int begin, int end, String value) {
                StringBuilder sbName = new StringBuilder();
                for (int i = begin; i < end; ++i) {
                    sbName.append(wordArray[i].realWord);
                }
                String name = sbName.toString();
                // 对一些bad case做出调整
                if (isBadCase(name)) {
                    return;
                }

                int offset = 0;
                for (int i = 0; i < begin; ++i) {
                    offset += wordArray[i].realWord.length();
                }
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_PLACE, name, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，
     * 这些bad case会以“盖公章 A 1”的形式加入词典中<BR>
     * 这个方法返回是否是bad case
     */
    static boolean isBadCase(String name) {
        EnumItem<NS> nrEnumItem = dictionary.get(name);
        if (nrEnumItem == null) {
            return false;
        }
        return nrEnumItem.containsLabel(NS.Z);
    }
}
