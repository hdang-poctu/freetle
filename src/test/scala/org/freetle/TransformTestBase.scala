package org.freetle

import org.junit._
import Assert._
import util.{Memoize1, XMLEventStream}
import java.io.InputStream

class TransformTestBase[Context] extends FreetleModel[Context] {

  def mloadStreamFromResource(resourceName: String, context : Option[Context]): XMLResultStream = {
    val src: InputStream = this.getClass().getResourceAsStream(resourceName)
    Stream.fromIterator(new XMLEventStream(src) map (Tail(_, context)))
  }
  def sloadStreamFromResource(resourceName: String) = mloadStreamFromResource(resourceName, null)
  def loadStreamFromResource = new Memoize1(sloadStreamFromResource)
  // Utility methods used to test XMLResultStream
  /**
   * Asserts that there are only Results in the stream.
   */
  def assertAllResult(r : XMLResultStream) :Unit = r.foreach(x => assertTrue(x.isInstanceOf[Result]))

  /**
   * Asserts that there are only Tails in the stream.
   */
  def assertAllTail(r : XMLResultStream) :Unit = r.foreach(x => assertTrue(x.isInstanceOf[Tail]))

  /**
   * Return the longest substream that begins very a Tail.
   */
  def findFirstTail(r : XMLResultStream) : XMLResultStream = r.head match {
    case result : Result => findFirstTail(r.tail)
    case tail : Tail => r
  }

  /**
   * Constrains that after the first Tail, there is only Tails.
   */
  def constraintResultsThenTails(x : XMLResultStream) : Unit = assertAllTail(findFirstTail(x))

  /**
   * Number of Results in the Stream.
   */
  def lengthResult(r : XMLResultStream) : Int = r.filter(_.isInstanceOf[Result]).length

  /**
   * Number of Tails in the Stream.
   */
  def lengthTail(r : XMLResultStream) : Int = r.filter(_.isInstanceOf[Result]).length

  /**
   * Serialize ( not very efficient ).
   */
  def serialize(x : XMLResultStream) : String = x.foldLeft("")( _ + _.subEvent.toString)
}