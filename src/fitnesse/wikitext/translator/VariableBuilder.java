package fitnesse.wikitext.translator;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class VariableBuilder implements Translation {

    public String toTarget(Translator translator, Symbol symbol) {
        //String name = symbol.childAt(0).getContent();
        //Maybe<String> variable = translator.getVariableSource().findVariable(name, symbol);
        //return variable.isNothing()
        //        ? HtmlUtil.metaText("undefined variable: " + name)
        //        : variable.getValue();
        return translator.translate(symbol.childAt(1));
    }

}
