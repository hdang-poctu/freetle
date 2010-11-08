 /*
  * Copyright 2010 Lucas Bruand
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.freetle
import meta.Meta
import org.junit._
import Assert._

import scala.xml.{Atom, Unparsed, PCData, PrettyPrinter, EntityRef, ProcInstr, Comment, Text, Elem, Node, NodeSeq}
import util._
import scala.util.Random
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.{StreamResult, StreamSource}
import java.io.{StringWriter, StringReader}
import java.lang.String
import testing.Benchmark

@serializable @SerialVersionUID(599494944949L + 20001L)
case class BenchmarkTestContext (
)

@Test
class BenchmarkTest extends TransformTestBase[TransformTestContext] with Meta[TransformTestContext] {
  val catalogHeader = """<?xml version="1.0" encoding="UTF-8"?>
<catalog>""".toStream

  val catalogFooter = """</catalog>
""".toStream

  def buildCD(title : String, artist : String, country : String, company : String, price : String, year : String) : String = {
    var sb = new StringBuilder()
    sb.append("<cd>\n")

    sb.append("<title>")
    sb.append(title)
    sb.append("</title>\n")

    sb.append("<artist>")
    sb.append(artist)
    sb.append("</artist>\n")

    sb.append("<country>")
    sb.append(country)
    sb.append("</country>\n")

    sb.append("<company>")
    sb.append(company)
    sb.append("</company>\n")

    sb.append("<price>")
    sb.append(price)
    sb.append("</price>\n")

    sb.append("<year>")
    sb.append(year)
    sb.append("</year>\n")

    sb.append("</cd>\n")
    sb.toString
  }
  val random = new RandomUtils()
  def buildRandomCD() : String = {
    buildCD("the " + random.nextStringLetterOrDigit(22) + " album",
            random.nextStringLetterOrDigit(15) + " and the band",
            random.nextStringLetterOrDigit(2),
            random.nextStringLetterOrDigit(15) + " inc.",
            "" + (random.nextInt(2000) * 1.0 / 100),
            "" + (1950 + random.nextInt(60))
        )
  }
  def buildRandomCatalog(n : Int) : String = {
    Stream.concat(catalogHeader,
                  Stream.fill(n)(buildRandomCD().toStream).flatten,
                  catalogFooter).mkString
  }

  class XSLTcaseBenchmark(val nCatalog : Int = 3) extends Benchmark {
    val xsltSource = """<?xml version="1.0" encoding="UTF-8"?>
  <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <body>
        <xsl:for-each select="catalog/cd">
          <tr><xsl:value-of select="title"/></tr>
          <tr><xsl:value-of select="artist"/></tr>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
  </xsl:stylesheet>
  """
    val transFactory = TransformerFactory.newInstance
    val transformerSource = new StreamSource(new StringReader(xsltSource))

    val transformer = transFactory.newTransformer(transformerSource)


    val catalogSource = buildRandomCatalog(nCatalog)

    var source : StreamSource = null

    
    var resultStringWriter: StringWriter = null
    var result : StreamResult = null

    def run() = {
      source = new StreamSource(new StringReader(catalogSource))
      resultStringWriter = new StringWriter()
      result = new StreamResult(resultStringWriter)
      transformer.transform(source, result)
    }

    def checkResult() = {
      // Finished Run.
      val resultString: String = resultStringWriter.toString

      assertTrue("<html> not found!! [" + resultString + "]", resultString.indexOf("<html>") >= 0)
      assertTrue("</html> not found!! [" + resultString + "]", resultString.indexOf("</html>") >= 0)
    }
  }
  @Test
  def testXSLT() = {
    val timing = new XSLTcaseBenchmark(nCatalog = 10000).runBenchmark(3)

    println(timing.mkString(","))

  }
  
}