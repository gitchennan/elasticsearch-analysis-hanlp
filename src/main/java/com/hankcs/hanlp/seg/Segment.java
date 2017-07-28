/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/29 14:53</create-date>
 *
 * <copyright file="AbstractBaseSegment.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.seg;

import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.InternalWordIds;
import com.hankcs.hanlp.dictionary.WordAttribute;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.dictionary.other.CharType;
import com.hankcs.hanlp.seg.NShort.Path.AtomNode;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.Predefine;
import com.hankcs.hanlp.utility.SentencesUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 分词器（分词服务）<br>
 * 是所有分词器的基类（Abstract）<br>
 * 分词器的分词方法是线程安全的，但配置方法则不保证
 *
 * @author hankcs
 */
public abstract class Segment {

    /**
     * 分词器配置
     */
    protected Config config;

    /**
     * 构造一个分词器
     */
    public Segment() {
        config = new Config();
    }

    /**
     * 原子分词
     *
     * @param start 从start开始（包含）
     * @param end   到end结束（不包含end）
     * @return 一个列表，代表从start到from的所有字构成的原子节点
     */
    protected static List<AtomNode> atomSegment(char[] charArray, int start, int end) {
        List<AtomNode> atomSegment = new ArrayList<AtomNode>();
        int pCur = start, nCurType, nNextType;
        StringBuilder sb = new StringBuilder();
        char c;

        int[] charTypeArray = new int[end - start];

        // 生成对应单个汉字的字符类型数组
        for (int i = 0; i < charTypeArray.length; ++i) {
            c = charArray[i + start];
            charTypeArray[i] = CharType.get(c);

            if (c == '.' && i + start < (charArray.length - 1) && CharType.get(charArray[i + start + 1]) == CharType.CT_NUM)
                charTypeArray[i] = CharType.CT_NUM;
            else if (c == '.' && i + start < (charArray.length - 1) && charArray[i + start + 1] >= '0' && charArray[i + start + 1] <= '9')
                charTypeArray[i] = CharType.CT_SINGLE;
            else if (charTypeArray[i] == CharType.CT_LETTER)
                charTypeArray[i] = CharType.CT_SINGLE;
        }

        // 根据字符类型数组中的内容完成原子切割
        while (pCur < end) {
            nCurType = charTypeArray[pCur - start];

            if (nCurType == CharType.CT_CHINESE || nCurType == CharType.CT_INDEX ||
                    nCurType == CharType.CT_DELIMITER || nCurType == CharType.CT_OTHER) {
                String single = String.valueOf(charArray[pCur]);
                if (single.length() != 0)
                    atomSegment.add(new AtomNode(single, nCurType));
                pCur++;
            }
            //如果是字符、数字或者后面跟随了数字的小数点“.”则一直取下去。
            else if (pCur < end - 1 && ((nCurType == CharType.CT_SINGLE) || nCurType == CharType.CT_NUM)) {
                sb.delete(0, sb.length());
                sb.append(charArray[pCur]);

                boolean reachEnd = true;
                while (pCur < end - 1) {
                    nNextType = charTypeArray[++pCur - start];

                    if (nNextType == nCurType)
                        sb.append(charArray[pCur]);
                    else {
                        reachEnd = false;
                        break;
                    }
                }
                atomSegment.add(new AtomNode(sb.toString(), nCurType));
                if (reachEnd)
                    pCur++;
            }
            // 对于所有其它情况
            else {
                atomSegment.add(new AtomNode(charArray[pCur], nCurType));
                pCur++;
            }
        }

        return atomSegment;
    }

    /**
     * 简易原子分词，将所有字放到一起作为一个词
     */
    protected static List<AtomNode> simpleAtomSegment(char[] charArray, int start, int end) {
        List<AtomNode> atomNodeList = new LinkedList<AtomNode>();
        atomNodeList.add(new AtomNode(new String(charArray, start, end - start), Predefine.CT_LETTER));
        return atomNodeList;
    }

    /**
     * 快速原子分词，希望用这个方法替换掉原来缓慢的方法
     */
    protected static List<AtomNode> quickAtomSegment(char[] charArray, int start, int end) {
        List<AtomNode> atomNodeList = new LinkedList<AtomNode>();
        int offsetAtom = start;
        int preType = CharType.get(charArray[offsetAtom]);
        int curType;
        while (++offsetAtom < end) {
            curType = CharType.get(charArray[offsetAtom]);
            if (curType != preType) {
                // 浮点数识别
                if (charArray[offsetAtom] == '.' && preType == CharType.CT_NUM) {
                    while (++offsetAtom < end) {
                        curType = CharType.get(charArray[offsetAtom]);
                        if (curType != CharType.CT_NUM) break;
                    }
                }
                atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));
                start = offsetAtom;
            }
            preType = curType;
        }
        if (offsetAtom == end)
            atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));

        return atomNodeList;
    }

    /**
     * 合并数字
     */
    protected void mergeNumberQuantifier(List<Vertex> termList, WordNet wordNetAll, Config config) {
        if (termList.size() < 4) return;
        StringBuilder sbQuantifier = new StringBuilder();
        ListIterator<Vertex> iterator = termList.listIterator();
        iterator.next();
        int line = 1;
        while (iterator.hasNext()) {
            Vertex pre = iterator.next();
            if (pre.hasNature(Nature.m)) {
                sbQuantifier.append(pre.realWord);
                Vertex cur = null;
                while (iterator.hasNext() && (cur = iterator.next()).hasNature(Nature.m)) {
                    sbQuantifier.append(cur.realWord);
                    iterator.remove();
                    removeFromWordNet(cur, wordNetAll, line, sbQuantifier.length());
                }
                if (cur != null) {
                    if ((cur.hasNature(Nature.q) || cur.hasNature(Nature.qv) || cur.hasNature(Nature.qt))) {
                        if (config.indexMode) {
                            wordNetAll.add(line, new Vertex(sbQuantifier.toString(), new WordAttribute(Nature.m)));
                        }
                        sbQuantifier.append(cur.realWord);
                        iterator.remove();
                        removeFromWordNet(cur, wordNetAll, line, sbQuantifier.length());
                    }
                    else {
                        line += cur.realWord.length();   // (cur = iterator.next()).hasNatureInSentence(Nature.m) 最后一个next可能不含q词性
                    }
                }
                if (sbQuantifier.length() != pre.realWord.length()) {
                    pre.realWord = sbQuantifier.toString();
                    pre.word = Predefine.TAG_NUMBER;
                    pre.attribute = new WordAttribute(Nature.mq);
                    pre.wordID = InternalWordIds.M_WORD_ID;
                    sbQuantifier.setLength(0);
                }
            }
            sbQuantifier.setLength(0);
            line += pre.realWord.length();
        }
//        System.out.println(wordNetAll);
    }

    /**
     * 将一个词语从词网中彻底抹除
     *
     * @param cur        词语
     * @param wordNetAll 词网
     * @param line       当前扫描的行数
     * @param length     当前缓冲区的长度
     */
    private static void removeFromWordNet(Vertex cur, WordNet wordNetAll, int line, int length) {
        LinkedList<Vertex>[] vertexes = wordNetAll.getVertexes();
        // 将其从wordNet中删除
        for (Vertex vertex : vertexes[line + length]) {
            if (vertex.from == cur)
                vertex.from = null;
        }
        ListIterator<Vertex> iterator = vertexes[line + length - cur.realWord.length()].listIterator();
        while (iterator.hasNext()) {
            Vertex vertex = iterator.next();
            if (vertex == cur) iterator.remove();
        }
    }

    /**
     * 分词<br>
     * 此方法是线程安全的
     *
     * @param text 待分词文本
     * @return 单词列表
     */
    public List<Term> seg(String text) {
        char[] charArray = text.toCharArray();
        if (HanLpGlobalSettings.Normalization) {
            CharTable.normalization(charArray);
        }
        return segSentence(charArray);
    }

    /**
     * 分词
     *
     * @param text 待分词文本
     * @return 单词列表
     */
    public List<Term> seg(char[] text) {
        assert text != null;
        if (HanLpGlobalSettings.Normalization) {
            CharTable.normalization(text);
        }
        return segSentence(text);
    }

    /**
     * 分词断句 输出句子形式
     *
     * @param text 待分词句子
     * @return 句子列表，每个句子由一个单词列表组成
     */
    public List<List<Term>> seg2sentence(String text) {
        List<List<Term>> resultList = new LinkedList<List<Term>>();
        {
            for (String sentence : SentencesUtil.toSentenceList(text)) {
                resultList.add(segSentence(sentence.toCharArray()));
            }
        }

        return resultList;
    }

    /**
     * 给一个句子分词
     *
     * @param sentence 待分词句子
     * @return 单词列表
     */
    protected abstract List<Term> segSentence(char[] sentence);

    /**
     * 设为索引模式
     *
     * @return
     */
    public Segment enableIndexMode(boolean enable) {
        config.indexMode = enable;
        return this;
    }

    /**
     * 开启词性标注
     *
     * @param enable
     * @return
     */
    public Segment enablePartOfSpeechTagging(boolean enable) {
        config.speechTagging = enable;
        return this;
    }

    /**
     * 开启人名识别
     *
     * @param enable
     * @return
     */
    public Segment enableNameRecognize(boolean enable) {
        config.nameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    /**
     * 开启地名识别
     *
     * @param enable
     * @return
     */
    public Segment enablePlaceRecognize(boolean enable) {
        config.placeRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    /**
     * 开启机构名识别
     *
     * @param enable
     * @return
     */
    public Segment enableOrganizationRecognize(boolean enable) {
        config.organizationRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    /**
     * 是否启用用户词典
     *
     * @param enable
     */
    public Segment enableCustomDictionary(boolean enable) {
        config.useCustomDictionary = enable;
        return this;
    }

    /**
     * 是否启用音译人名识别
     *
     * @param enable
     */
    public Segment enableTranslatedNameRecognize(boolean enable) {
        config.translatedNameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    /**
     * 是否启用日本人名识别
     *
     * @param enable
     */
    public Segment enableJapaneseNameRecognize(boolean enable) {
        config.japaneseNameRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    /**
     * 是否启用偏移量计算（开启后Term.offset才会被计算）
     *
     * @param enable
     * @return
     */
    public Segment enableOffset(boolean enable) {
        config.offset = enable;
        return this;
    }

    /**
     * 是否启用数词和数量词识别<br>
     * 即[二, 十, 一] => [二十一]，[十, 九, 元] => [十九元]
     *
     * @param enable
     * @return
     */
    public Segment enableNumberQuantifierRecognize(boolean enable) {
        config.numberQuantifierRecognize = enable;
        return this;
    }

    /**
     * 是否启用所有的命名实体识别
     *
     * @param enable
     * @return
     */
    public Segment enableAllNamedEntityRecognize(boolean enable) {
        config.nameRecognize = enable;
        config.japaneseNameRecognize = enable;
        config.translatedNameRecognize = enable;
        config.placeRecognize = enable;
        config.organizationRecognize = enable;
        config.updateNerConfig();
        return this;
    }
}
