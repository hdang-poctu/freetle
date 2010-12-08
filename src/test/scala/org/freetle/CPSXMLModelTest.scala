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

import meta.CPSMeta
import org.junit._
import Assert._
import java.io.InputStream
import util._

case class TestXMLContext(a : Int = 0)

trait TestXMLHelperMethods[Context] extends CPSXMLModel[Context] {
  val PREFIX : String = "p"
  val NAMESPACE : String = "http://freetle.sf.net/"

  def mloadStreamFromResource(resourceName: String, context : Option[Context]): XMLResultStream = {
    val src: InputStream = this.getClass().getResourceAsStream(resourceName)
    Stream.fromIterator(new XMLEventStream(src) map (x => (Some(x), false)))
  }
  def sloadStreamFromResource(resourceName: String) = mloadStreamFromResource(resourceName, null)
  def loadStreamFromResource = new Memoize1(sloadStreamFromResource)
  // Utility methods used to test XMLResultStream
  /**
   * Asserts that there are only Results in the stream.
   */
  def assertAllResult( r : XMLResultStream, str : String = null) :Unit = r.foreach(x => assertTrue(str, x._2))

  /**
   * Asserts that there are only Tails in the stream.
   */
  def assertAllTail(r : XMLResultStream) :Unit = r.foreach(x => assertTrue(!x._2))

  /**
   * Return the longest substream that begins very a Tail.
   */
  def findFirstTail(r : XMLResultStream) : XMLResultStream = if (r.head._2) findFirstTail(r.tail) else r

  /**
   * Constrains that after the first Tail, there is only Tails.
   */
  def constraintResultsThenTails(x : XMLResultStream) : Unit = assertAllTail(findFirstTail(x))

  /**
   * Number of Results in the Stream.
   */
  def lengthResult(r : XMLResultStream) : Int = r.filter(_._2).length

  /**
   * Number of Tails in the Stream.
   */
  def lengthTail(r : XMLResultStream) : Int = r.filter(_._2).length

  /**
   * Serialize ( not very efficient ).
   */
  def serialize(x : XMLResultStream) : String =
    (new StringBuilder()).appendAll(XMLResultStreamUtils.serializeXMLResultStream(x)).toString

   /**
   * Serialize ( not very efficient ).
   */
  def serializeWithResult(x : XMLResultStream) : String = {
    val charStream : Stream[Char] = (x map
            (y => Stream.cons(if (y._2) 'R' else 'T', y._1.getOrElse(new EvComment("EvEmptyPositive")).toStream))).flatten
    charStream.mkString
  }
  final def createStartElem(s : String) = new EvElemStart(new QName(NAMESPACE, s, PREFIX), null)
  final def createEndElem(s : String) = new EvElemEnd(new QName(NAMESPACE, s, PREFIX))
  final val filterIdentity = new CFilterIdentity()
}
@Test
class CPSXMLModelTest extends CPSXMLModel[TestXMLContext] with TestXMLHelperMethods[TestXMLContext] with CPSMeta[TestXMLContext] {
  @Test
  def testDeepFilter() {
    val s : CPSStream = List(
      createStartElem("body"),
      createStartElem("body"),
      createStartElem("a"),
      createStartElem("a"),
      createEndElem("a"),
      createEndElem("a"),
      createStartElem("a"),
      createEndElem("a"),
      createEndElem("body"),
      createEndElem("body")
      ).toStream map (x => (Some(x), false))

    val t = <("body") ~ <("body") ~ new DeepFilter()
    val r = t(filterIdentity, filterIdentity)(s, new TestXMLContext())
    assertEquals(10, lengthResult(r))
    val t2 = <("body") ~ <("body") ~ <("a") ~ new DeepFilter()
    val r2 = t2(filterIdentity, filterIdentity)(s, new TestXMLContext())
    assertEquals(serializeWithResult(r2), 9, lengthResult(r2))
  }
  
  @Test
  def testDropFilter() = {
    val evStream = loadStreamFromResource("/org/freetle/input.xml")

    val t = <("input") ~
                (( <("message") ~
                   ((<("value") ~
                   TakeText()   ~
                   </("value")) -> new DropFilter()) ~
                  </("message")
                )+) ~ </("input")
    val tmeta = t.metaProcess(new SpaceSkipingMetaProcessor())
    val r = tmeta(filterIdentity, filterIdentity)(evStream, new TestXMLContext())
    assertAllResult(r, "Result : [" + serializeWithResult(r) + "]" )
    assertEquals("Result : [" + serializeWithResult(r) + "]", evStream.length-8, r.length)
    assertTrue(r.filter(x => (x._1 match {
      case Some(EvElemStart(QName(_, "value", _), _)) => true
      case _  => false
    })).isEmpty)
  }  
}