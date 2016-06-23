package processors.api

import org.scalatest.{Matchers, FlatSpec}
import processors.{ConverterUtils, ProcessorsBridge}
import processors.api


class TestConverterUtils extends FlatSpec with Matchers {

  "proccessors.Document" should "convert to api.Document" in {
    val text = "This is how you write a boring sentence."
    val procDoc = ProcessorsBridge.annotate(text)
    val doc = ConverterUtils.toDocument(procDoc)
    doc.isInstanceOf[api.Document] should be(true)
  }

  "proccessors.Sentence" should "convert to api.Sentence" in {
    val text = "This is how you write a boring sentence."
    val procDoc = ProcessorsBridge.annotate(text)
    val procSent = procDoc.sentences.head
    val s = ConverterUtils.toSentence(procSent)
    s.isInstanceOf[api.Sentence] should be(true)
  }

  "proccessors.Mention" should "convert to api.Mention" in {
    val text = "Homer Simpson is a named entity."
    val rules =
      """
        |- name: "ner-person"
        |  label: [Person, PossiblePerson, Entity]
        |  priority: 1
        |  type: token
        |  pattern: |
        |   [entity="PERSON"]+
        |   |
        |   [tag=/^N/]* [tag=/^N/ & outgoing="cop"] [tag=/^N/]*
      """.stripMargin
    val procDoc = ProcessorsBridge.annotate(text)
    val mentions = ProcessorsBridge.getMentions(procDoc, rules)
    mentions.nonEmpty should be(true)
    mentions.head.isInstanceOf[api.Mention] should be(true)
  }

  "ConverterUtils.urlToRules" should "retrieve Odin rules from a url pointing to a yaml file" in {
    val text = "Homer Simpson is a named entity."
    val procDoc = ProcessorsBridge.annotate(text)
    val url = "https://raw.githubusercontent.com/clulab/reach/master/src/main/resources/edu/arizona/sista/demo/open/grammars/rules.yml"
    val rules = ConverterUtils.urlToRules(url)
    rules.nonEmpty should be(true)
    val mentions = ProcessorsBridge.getMentions(procDoc, rules)
    mentions.nonEmpty should be(true)
    mentions.head.isInstanceOf[api.Mention] should be(true)
  }

  it should "fail on urls not ending in .yaml or .yml" in {
    val badURL = "https://www.societyofwafflecartographers.com"
    a [api.BadURLException] should be thrownBy ConverterUtils.urlToRules(badURL)
  }
}