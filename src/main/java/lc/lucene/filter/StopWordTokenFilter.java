package lc.lucene.filter;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class StopWordTokenFilter extends TokenFilter {

    private CharTermAttribute termAtt = (CharTermAttribute) this.addAttribute(CharTermAttribute.class);

    public StopWordTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (this.input.incrementToken()) {
            char[] text = this.termAtt.buffer();
            int length = this.termAtt.length();

            if (CoreStopWordDictionary.contains(String.valueOf(text, 0, length))) {
                continue;
            }

            this.termAtt.setEmpty();
            this.termAtt.append(String.valueOf(text, 0, length));
            return true;
        }
        return false;
    }
}
