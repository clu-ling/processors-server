package processors.api

import org.scalatest.{Matchers, FlatSpec}
import processors.{ConverterUtils, ProcessorsBridge}
import processors.api


class TestConverterUtils extends FlatSpec with Matchers {

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
  }

  "ConverterUtils.urlToRules" should "retrieve Odin rules from a url pointing to a yaml file" in {
    val text = "Homer Simpson is a named entity."
    val procDoc = ProcessorsBridge.annotate(text)
    val url = "https://raw.githubusercontent.com/clulab/reach/c33eb9f4f772ff246c4883b7c3230d9154305402/src/main/resources/org/clulab/demo/open/grammars/rules.yml"
    val rules = ConverterUtils.urlToRules(url)
    rules.nonEmpty should be(true)
    val mentions = ProcessorsBridge.getMentions(procDoc, rules)
    mentions.nonEmpty should be(true)
  }

  it should "fail on urls not ending in .yaml or .yml" in {
    val badURL = "https://www.societyofwafflecartographers.com"
    a [api.BadURLException] should be thrownBy ConverterUtils.urlToRules(badURL)
  }
}