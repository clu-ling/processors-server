package processors.api

import org.scalatest.{ FlatSpec, Matchers }
import processors.ProcessorsBridge

class TestProcessorsBridge extends FlatSpec with Matchers {

  val preSplitText = Seq("this is a sentence", "...and this is a sentence", "and so is this")

  "ProcessorsBridge.annotate" should "produce a processors.Document from a pre-split sequence of text" in {
    val doc = ProcessorsBridge.annotate(preSplitText)
    doc.sentences.length == preSplitText.length should be(true)
  }

}
