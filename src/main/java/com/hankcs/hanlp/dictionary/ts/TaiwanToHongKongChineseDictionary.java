/*
 * <summary></summary>
 * <author>Hankcs</author>
 * <email>me@hankcs.com</email>
 * <create-date>2016-08-30 AM10:29</create-date>
 *
 * <copyright file="SimplifiedToHongKongChineseDictionary.java" company="码农场">
 * Copyright (c) 2008-2016, 码农场. All Right Reserved, http://www.hankcs.com/
 * This source is subject to Hankcs. Please contact Hankcs to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.ts;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.TreeMap;

/**
 * 台湾繁体转香港繁体
 *
 * @author hankcs
 */
public class TaiwanToHongKongChineseDictionary extends BaseChineseDictionary {
    static AhoCorasickDoubleArrayTrie<String> trie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();

    static {
        long start = System.currentTimeMillis();

        TreeMap<String, String> t2hk = Maps.newTreeMap();
        TreeMap<String, String> tw2t = Maps.newTreeMap();
        if (!load(t2hk, false, HanLpGlobalSettings.tcDictionaryRoot + "t2hk.txt") ||
                !load(tw2t, true, HanLpGlobalSettings.tcDictionaryRoot + "t2tw.txt")) {
            throw new IllegalArgumentException("台湾繁体转香港繁体词典加载失败");
        }
        combineReverseChain(t2hk, tw2t, false);
        trie.build(t2hk);

        HanLpLogger.info(TaiwanToHongKongChineseDictionary.class,
                "台湾繁体转香港繁体词典加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToTraditionalHongKongChinese(String traditionalTaiwanChinese) {
        return segLongest(traditionalTaiwanChinese.toCharArray(), trie);
    }

    public static String convertToTraditionalHongKongChinese(char[] traditionalTaiwanChinese) {
        return segLongest(traditionalTaiwanChinese, trie);
    }
}
