package jug.lodz.workshops.starter.functions1.answers

import org.scalatest.{MustMatchers, WordSpec}

import scala.annotation.tailrec

class TailRecursionAnswers extends WordSpec with MustMatchers{

  "EXERCISE1" should {
    "Sum custom list of integers" in {
      @tailrec
      def sumList(list:CustomList,sum:Int=0):Int = list match {
        case ListEnd => sum
        case ListElem(e, tail) => sumList(tail,sum+e)
      }

      val l1=ListElem(1,ListElem(2,ListElem(3,ListEnd)))
      val l2=ListElem(15,ListElem(27,ListEnd))

      sumList(l1) mustBe 6
      sumList(l2) mustBe 42

    }
  }

  "EXERCISE2" should {
    "generate n elements" in {

      val expected2Elem=ListElem(4,ListElem(4,ListEnd))
      val expected3Elem=ListElem(6,ListElem(6,ListElem(6,ListEnd)))

      Exercise2.generate(4,2) mustBe expected2Elem
      Exercise2.generate(6,3) mustBe expected3Elem
    }
  }

  "EXERCISE3" should {
    "implement tail recursive reduceLeft" in {
        import TailRecExercise3._

      val l1=ListElem(2,ListElem(3,ListElem(4,ListEnd)))

      reduceLeft(l1)(0)(_+_) mustBe 9
      reduceLeft(l1)(1)(_*_) mustBe 24
    }
  }

}

sealed trait CustomList
case class ListElem(a:Int,rest:CustomList) extends CustomList
case object ListEnd extends CustomList

object Exercise2{

  def generate(element:Int,howMany:Int) : CustomList = {
      @tailrec
      def innerLoop(current:CustomList,howMany:Int) : CustomList =
        if(howMany<=0) current
        else innerLoop(ListElem(element,current),howMany-1)

    innerLoop(ListEnd,howMany)
  }
}


object TailRecExercise3{
  @tailrec
  def reduceLeft(l:CustomList)(zero:Int)(f:(Int,Int)=>Int) : Int = l match {
    case ListEnd => zero
    case ListElem(a,tail) => reduceLeft(tail)(f(zero,a))(f)
  }
}