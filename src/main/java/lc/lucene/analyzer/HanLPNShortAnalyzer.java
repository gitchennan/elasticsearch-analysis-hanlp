package lc.lucene.analyzer;

import com.hankcs.hanlp.seg.NShort.NShortSegment;
import lc.lucene.tokenizer.HanLPTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.util.Set;

/**
 * N-最短路径分析器
 *
 * @author Kenn
 */
public class HanLPNShortAnalyzer extends Analyzer {

    private boolean enablePorterStemming;
    private Set<String> filter;

    /**
     * @param filter               停用词
     * @param enablePorterStemming 是否分析词干（仅限英文）
     */
    public HanLPNShortAnalyzer(Set<String> filter, boolean enablePorterStemming) {
        this.filter = filter;
        this.enablePorterStemming = enablePorterStemming;
    }

    /**
     * @param enablePorterStemming 是否分析词干.进行单复数,时态的转换
     */
    public HanLPNShortAnalyzer(boolean enablePorterStemming) {
        this.enablePorterStemming = enablePorterStemming;
    }

    public HanLPNShortAnalyzer() {
        super();
    }

    /**
     * 重载Analyzer接口，构造分词组件
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new HanLPTokenizer(new NShortSegment().enableCustomDictionary(false)
                .enablePlaceRecognize(true).enableOrganizationRecognize(true), filter, enablePorterStemming);
        return new TokenStreamComponents(tokenizer);
    }

}
