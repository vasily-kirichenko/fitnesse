// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.responders.run.ExecutionLog;
import static fitnesse.wiki.PageType.*;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiSourcePage;
import fitnesse.wikitext.translator.HtmlTranslator;
import fitnesse.wikitext.translator.Paths;
import fitnesse.wikitext.translator.Translator;
import fitnesse.wikitext.widgets.*;
import util.StringUtil;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public class PageData implements Serializable {

  private static final long serialVersionUID = 1L;

  public static WidgetBuilder classpathWidgetBuilder = new WidgetBuilder(
      IncludeWidget.class, VariableDefinitionWidget.class,
      ClasspathWidget.class);
  public static WidgetBuilder xrefWidgetBuilder = new WidgetBuilder(
      XRefWidget.class);

  public static WidgetBuilder variableDefinitionWidgetBuilder = new WidgetBuilder(
      IncludeWidget.class, PreformattedWidget.class,
      VariableDefinitionWidget.class);

  // TODO: Find a better place for us
  public static final String PropertyLAST_MODIFIED = "LastModified";
  public static final String PropertyHELP = "Help";
  public static final String PropertyPRUNE = "Prune";
  public static final String PropertySEARCH = "Search";
  public static final String PropertyRECENT_CHANGES = "RecentChanges";
  public static final String PropertyFILES = "Files";
  public static final String PropertyWHERE_USED = "WhereUsed";
  public static final String PropertyREFACTOR = "Refactor";
  public static final String PropertyPROPERTIES = "Properties";
  public static final String PropertyVERSIONS = "Versions";
  public static final String PropertyEDIT = "Edit";
  public static final String PropertySUITES = "Suites";

  public static final String PAGE_TYPE_ATTRIBUTE = "PageType";
  public static final String[] PAGE_TYPE_ATTRIBUTES = { NORMAL.toString(),
      TEST.toString(), SUITE.toString() };

  public static final String[] ACTION_ATTRIBUTES = { PropertyEDIT,
      PropertyVERSIONS, PropertyPROPERTIES, PropertyREFACTOR,
      PropertyWHERE_USED };

  public static final String[] NAVIGATION_ATTRIBUTES = {
      PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH, PropertyPRUNE };

  public static final String[] NON_SECURITY_ATTRIBUTES = StringUtil
      .combineArrays(ACTION_ATTRIBUTES, NAVIGATION_ATTRIBUTES);

  public static final String PropertySECURE_READ = "secure-read";
  public static final String PropertySECURE_WRITE = "secure-write";
  public static final String PropertySECURE_TEST = "secure-test";
  public static final String[] SECURITY_ATTRIBUTES = { PropertySECURE_READ,
      PropertySECURE_WRITE, PropertySECURE_TEST };

  public static final String LAST_MODIFYING_USER = "LastModifyingUser";

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";

  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  private transient WikiPage wikiPage;
  private String content;
  private WikiPageProperties properties = new WikiPageProperties();
  private Set<VersionInfo> versions;
  private ParentWidget variableRoot;
  private List<String> literals;

  public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  public static final String TEST_RUNNER = "TEST_RUNNER";
  public static final String PATH_SEPARATOR = "PATH_SEPARATOR";

  private Symbol contentSyntaxTree = null;

    public PageData(WikiPage page) throws Exception {
    wikiPage = page;
    initializeAttributes();
    versions = new HashSet<VersionInfo>();
  }

  public PageData(WikiPage page, String content) throws Exception {
    this(page);
    setContent(content);
  }

  public PageData(PageData data) throws Exception {
    this(data.getWikiPage(), data.content);
    properties = new WikiPageProperties(data.properties);
    versions.addAll(data.versions);
    variableRoot = data.variableRoot;
    contentSyntaxTree = data.contentSyntaxTree;
  }

  public String getStringOfAllAttributes() {
    return properties.toString();
  }

  public void initializeAttributes() throws Exception {
    properties.set(PropertyEDIT, Boolean.toString(true));
    properties.set(PropertyVERSIONS, Boolean.toString(true));
    properties.set(PropertyPROPERTIES, Boolean.toString(true));
    properties.set(PropertyREFACTOR, Boolean.toString(true));
    properties.set(PropertyWHERE_USED, Boolean.toString(true));
    properties.set(PropertyFILES, Boolean.toString(true));
    properties.set(PropertyRECENT_CHANGES, Boolean.toString(true));
    properties.set(PropertySEARCH, Boolean.toString(true));
    properties.setLastModificationTime(new Date());

    initTestOrSuiteProperty();
  }

  private void initTestOrSuiteProperty() throws Exception {
    final String pageName = wikiPage.getName();
    if (pageName == null) {
      handleInvalidPageName(wikiPage);
      return;
    }

    if (isErrorLogsPage())
      return;

    PageType pageType = PageType.getPageTypeForPageName(pageName);

    if (NORMAL.equals(pageType))
      return;

    properties.set(pageType.toString(), Boolean.toString(true));
  }

  private boolean isErrorLogsPage() throws Exception {
    PageCrawler crawler = wikiPage.getPageCrawler();
    String relativePagePath = crawler.getRelativeName(
        crawler.getRoot(wikiPage), wikiPage);
    return relativePagePath.startsWith(ExecutionLog.ErrorLogName);
  }

  // TODO: Should be written to a real logger, but it doesn't like FitNesse's
  // logger is
  // really intended for general logging.
  private void handleInvalidPageName(WikiPage wikiPage) {
    try {
      String msg = "WikiPage " + wikiPage + " does not have a valid name!"
          + wikiPage.getName();
      System.err.println(msg);
      throw new RuntimeException(msg);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public WikiPageProperties getProperties() throws Exception {
    return properties;
  }

  public String getAttribute(String key) throws Exception {
    return properties.get(key);
  }

  public void removeAttribute(String key) throws Exception {
    properties.remove(key);
  }

  public void setAttribute(String key, String value) throws Exception {
    properties.set(key, value);
  }

  public void setAttribute(String key) throws Exception {
    properties.set(key);
  }

  public boolean hasAttribute(String attribute) {
    return properties.has(attribute);
  }

  public void setProperties(WikiPageProperties properties) {
    this.properties = properties;
  }

  public String getContent() throws Exception {
    return StringUtil.stripCarriageReturns(content);
  }

  public void setContent(String content) {
    this.content = content;
    contentSyntaxTree = null;
  }

  /* this is the public entry to page parse and translate */
  public String getHtml() throws Exception {
    return processHTMLWidgets(wikiPage);
  }

  public String getHtml(WikiPage context) throws Exception {
    return processHTMLWidgets(context);
  }

  public String getHeaderPageHtml() throws Exception {
    WikiPage header = wikiPage.getHeaderPage();
    return header == null ? "" : header.getData().getHtml();
  }

  public String getFooterPageHtml() throws Exception {
    WikiPage footer = wikiPage.getFooterPage();
    return footer == null ? "" : footer.getData().getHtml();
  }

  public String getVariable(String name) throws Exception {
    return getInitializedVariableRoot().getVariable(name);
  }

    public Symbol getSyntaxTree() throws Exception {
        if (contentSyntaxTree == null) {
            contentSyntaxTree = Parser.make(new ParsingPage(new WikiSourcePage(wikiPage)), getContent()).parse();
        }
        return contentSyntaxTree;
    }

  public void addVariable(String name, String value) throws Exception {
    getInitializedVariableRoot().addVariable(name, value);
  }

  public void setLiterals(List<String> literals) {
    this.literals = literals;
  }

  private ParentWidget getInitializedVariableRoot() throws Exception {
    if (variableRoot == null) {
      initializeVariableRoot();
    }
    return variableRoot;
  }
  
  private void initializeVariableRoot() throws Exception {
    variableRoot = new TextIgnoringWidgetRoot(getContent(), wikiPage,
        literals, variableDefinitionWidgetBuilder
    );
    variableRoot.render();
  }

    /*private String processHTMLWidgets(String content, WikiPage context)
        throws Exception {
      ParentWidget root = new WidgetRoot(content, context,
          WidgetBuilder.htmlWidgetBuilder);

      return root.render();
    }*/

    private String processHTMLWidgets(WikiPage context) throws Exception {
        return new HtmlTranslator(new WikiSourcePage(context)).translateTree(getSyntaxTree());
    }

  public void setWikiPage(WikiPage page) {
    wikiPage = page;
  }

  public WikiPage getWikiPage() {
    return wikiPage;
  }

  public List<String> getClasspaths() throws Exception {
    return new Paths(new HtmlTranslator(new WikiSourcePage(wikiPage))).getPaths(getSyntaxTree());
    //return getTextOfWidgets(classpathWidgetBuilder);
  }

  public List<String> getXrefPages() throws Exception {
    return getTextOfWidgets(xrefWidgetBuilder);
  }

  private List<String> getTextOfWidgets(WidgetBuilder builder) throws Exception {
    ParentWidget root = new TextIgnoringWidgetRoot(getContent(), wikiPage,
        builder);
    List<WikiWidget> widgets = root.getChildren();
    List<String> values = new ArrayList<String>();
    for (WikiWidget widget : widgets) {
      if (widget instanceof WidgetWithTextArgument)
        values.add(((WidgetWithTextArgument) widget).getText());
      else
        widget.render();
    }
    return values;
  }

  public Set<VersionInfo> getVersions() {
    return versions;
  }

  public void addVersions(Collection<VersionInfo> newVersions) {
    versions.addAll(newVersions);
  }

  public boolean isEmpty() throws Exception {
    return getContent() == null || getContent().length() == 0;
  }
}
