package com.hankcs.hanlp.api;

import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamCreator;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.utility.Predefine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * ���ȫ�����ã��ȿ����ô����޸ģ�Ҳ����ͨ��hanlp.properties���ã����� ������=ֵ ����ʽ��
 */
public class HanLpGlobalSettings {
    /**
     * ���Ĵʵ�·��
     */
    public static String CoreDictionaryPath = "data/dictionary/CoreNatureDictionary.txt";
    /**
     * ���Ĵʵ����ת�ƾ���·��
     */
    public static String CoreDictionaryTransformMatrixDictionaryPath = "data/dictionary/CoreNatureDictionary.tr.txt";
    /**
     * �û��Զ���ʵ�·��
     */
    public static String CustomDictionaryPath[] = new String[]{"data/dictionary/custom/CustomDictionary.txt"};
    /**
     * 2Ԫ�﷨�ʵ�·��
     */
    public static String BiGramDictionaryPath = "data/dictionary/CoreNatureDictionary.ngram.txt";

    /**
     * ͣ�ôʴʵ�·��
     */
    public static String CoreStopWordDictionaryPath = "data/dictionary/stopwords.txt";
    /**
     * ͬ��ʴʵ�·��
     */
    public static String CoreSynonymDictionaryDictionaryPath = "data/dictionary/synonym/CoreSynonym.txt";
    /**
     * �����ʵ�·��
     */
    public static String PersonDictionaryPath = "data/dictionary/person/nr.txt";
    /**
     * �����ʵ�ת�ƾ���·��
     */
    public static String PersonDictionaryTrPath = "data/dictionary/person/nr.tr.txt";
    /**
     * �����ʵ�·��
     */
    public static String PlaceDictionaryPath = "data/dictionary/place/ns.txt";
    /**
     * �����ʵ�ת�ƾ���·��
     */
    public static String PlaceDictionaryTrPath = "data/dictionary/place/ns.tr.txt";
    /**
     * �����ʵ�·��
     */
    public static String OrganizationDictionaryPath = "data/dictionary/organization/nt.txt";
    /**
     * �����ʵ�ת�ƾ���·��
     */
    public static String OrganizationDictionaryTrPath = "data/dictionary/organization/nt.tr.txt";
    /**
     * ��ת���ʵ��Ŀ¼
     */
    public static String tcDictionaryRoot = "data/dictionary/tc/";
    /**
     * ��ĸ��ĸ����ʵ�
     */
    public static String SYTDictionaryPath = "data/dictionary/pinyin/SYTDictionary.txt";

    /**
     * ƴ���ʵ�·��
     */
    public static String PinyinDictionaryPath = "data/dictionary/pinyin/pinyin.txt";

    /**
     * ���������ʵ�
     */
    public static String TranslatedPersonDictionaryPath = "data/dictionary/person/nrf.txt";

    /**
     * �ձ������ʵ�·��
     */
    public static String JapanesePersonDictionaryPath = "data/dictionary/person/nrj.txt";

    /**
     * �ַ����Ͷ�Ӧ��
     */
//        public static String CharTypePath = "data/dictionary/other/CharType.bin";

    /**
     * �ַ����滯��ȫ��ת��ǣ�����ת���壩
     */
    public static String CharTablePath = "data/dictionary/other/CharTable.txt";

    /**
     * ��-����-�����ϵģ��
     */
    public static String WordNatureModelPath = "data/model/dependency/WordNature.txt";

    /**
     * �����-�����ϵģ��
     */
    public static String MaxEntModelPath = "data/model/dependency/MaxEntModel.txt";
    /**
     * ����������ģ��·��
     */
    public static String NNParserModelPath = "data/model/dependency/NNParserModel.txt";
    /**
     * CRF�ִ�ģ��
     */
    public static String CRFSegmentModelPath = "data/model/segment/CRFSegmentModel.txt";
    /**
     * HMM�ִ�ģ��
     */
//        public static String HMMSegmentModelPath = "data/model/segment/HMMSegmentModel.bin";
    /**
     * CRF����ģ��
     */
    public static String CRFDependencyModelPath = "data/model/dependency/CRFDependencyModelMini.txt";
    /**
     * �ִʽ���Ƿ�չʾ����
     */
    public static boolean ShowTermNature = true;
    /**
     * �Ƿ�ִ���ַ����滯������->���壬ȫ��->��ǣ���д->Сд�����л����ú����ɾCustomDictionary.txt.bin����
     */
    public static boolean Normalization = false;

    static {
        // �Զ���ȡ����
        Properties p = new Properties();
        try {
            IOSafeHelper.openAutoCloseableInputStream(new InputStreamCreator() {
                @Override
                public InputStream create() throws Exception {
                    if (Predefine.HANLP_PROPERTIES_PATH == null) {
                        ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        if (loader == null) {
                            loader = HanLpGlobalSettings.class.getClassLoader();
                        }
                        InputStream inputStream = loader.getResourceAsStream("hanlp.properties");
                        if (inputStream == null) {
                            throw new FileNotFoundException("HanLp setting file[hanlp.properties] not found");
                        }
                        return inputStream;
                    }
                    return new FileInputStream(Predefine.HANLP_PROPERTIES_PATH);
                }
            }, new InputStreamOperator() {
                @Override
                public void process(InputStream input) throws Exception {
                    p.load(new InputStreamReader(input, "UTF-8"));
                }
            });

            String root = p.getProperty("root", "").replaceAll("\\\\", "/");
            if (root.length() > 0 && !root.endsWith("/")) root += "/";
            CoreDictionaryPath = root + p.getProperty("CoreDictionaryPath", CoreDictionaryPath);
            CoreDictionaryTransformMatrixDictionaryPath = root + p.getProperty("CoreDictionaryTransformMatrixDictionaryPath", CoreDictionaryTransformMatrixDictionaryPath);
            BiGramDictionaryPath = root + p.getProperty("BiGramDictionaryPath", BiGramDictionaryPath);
            CoreStopWordDictionaryPath = root + p.getProperty("CoreStopWordDictionaryPath", CoreStopWordDictionaryPath);
            CoreSynonymDictionaryDictionaryPath = root + p.getProperty("CoreSynonymDictionaryDictionaryPath", CoreSynonymDictionaryDictionaryPath);
            PersonDictionaryPath = root + p.getProperty("PersonDictionaryPath", PersonDictionaryPath);
            PersonDictionaryTrPath = root + p.getProperty("PersonDictionaryTrPath", PersonDictionaryTrPath);
            String[] pathArray = p.getProperty("CustomDictionaryPath", "data/dictionary/custom/CustomDictionary.txt").split(";");
            String prePath = root;
            for (int i = 0; i < pathArray.length; ++i) {
                if (pathArray[i].startsWith(" ")) {
                    pathArray[i] = prePath + pathArray[i].trim();
                }
                else {
                    pathArray[i] = root + pathArray[i];
                    int lastSplash = pathArray[i].lastIndexOf('/');
                    if (lastSplash != -1) {
                        prePath = pathArray[i].substring(0, lastSplash + 1);
                    }
                }
            }
            CustomDictionaryPath = pathArray;
            tcDictionaryRoot = root + p.getProperty("tcDictionaryRoot", tcDictionaryRoot);
            if (!tcDictionaryRoot.endsWith("/")) tcDictionaryRoot += '/';
            SYTDictionaryPath = root + p.getProperty("SYTDictionaryPath", SYTDictionaryPath);
            PinyinDictionaryPath = root + p.getProperty("PinyinDictionaryPath", PinyinDictionaryPath);
            TranslatedPersonDictionaryPath = root + p.getProperty("TranslatedPersonDictionaryPath", TranslatedPersonDictionaryPath);
            JapanesePersonDictionaryPath = root + p.getProperty("JapanesePersonDictionaryPath", JapanesePersonDictionaryPath);
            PlaceDictionaryPath = root + p.getProperty("PlaceDictionaryPath", PlaceDictionaryPath);
            PlaceDictionaryTrPath = root + p.getProperty("PlaceDictionaryTrPath", PlaceDictionaryTrPath);
            OrganizationDictionaryPath = root + p.getProperty("OrganizationDictionaryPath", OrganizationDictionaryPath);
            OrganizationDictionaryTrPath = root + p.getProperty("OrganizationDictionaryTrPath", OrganizationDictionaryTrPath);
//                CharTypePath = root + p.getProperty("CharTypePath", CharTypePath);
            CharTablePath = root + p.getProperty("CharTablePath", CharTablePath);
            WordNatureModelPath = root + p.getProperty("WordNatureModelPath", WordNatureModelPath);
            MaxEntModelPath = root + p.getProperty("MaxEntModelPath", MaxEntModelPath);
            NNParserModelPath = root + p.getProperty("NNParserModelPath", NNParserModelPath);
            CRFSegmentModelPath = root + p.getProperty("CRFSegmentModelPath", CRFSegmentModelPath);
            CRFDependencyModelPath = root + p.getProperty("CRFDependencyModelPath", CRFDependencyModelPath);
//                HMMSegmentModelPath = root + p.getProperty("HMMSegmentModelPath", HMMSegmentModelPath);
            ShowTermNature = "true".equals(p.getProperty("ShowTermNature", "true"));
            Normalization = "true".equals(p.getProperty("Normalization", "false"));
        }
        catch (Exception e) {
            HanLpLogger.error(HanLP.class, "Failed to load hanLp settings", e);
        }
    }
}