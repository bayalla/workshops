package jug.lodz.workshops.modeling.madyfication.answers

import jug.lodz.workshops.modeling.madyfication.answers.EXERCISE2Lib.CustomLens
import monocle.function.Each
import monocle.macros.GenLens
import monocle.{Iso, Lens}
import org.scalatest.{MustMatchers, WordSpecLike}

class LensesAnswers extends WordSpecLike with MustMatchers{

  import LensesAnswerLibrary._

  "EXERCISE1" should {
    "(a) create topic Lens in" in {
      val meetup = fixture()

      //finish lens in LensesAnswerLibrary
      topicLens.get(meetup) mustBe "Scala DDD"
      val changed=topicLens.set("Learning Monocle")(meetup)
      changed.topic mustBe "Learning Monocle"

    }

    "(b) create and compose address lens and tuple iso" in {
      val meetup=fixture()
      val streetToTuple= addressLens composeLens streetLens composeIso streetTupleIso

      streetToTuple.get(meetup) mustBe ("Piotrkowska",1)
      //on the right side comparison of manual work needed without lenses
      streetToTuple.set(("Sienkiewicza",4))(meetup) mustBe meetup.copy(address = meetup.address.copy(street = Street("Sienkiewicza",4)))
    }

    " (c) add meetup topic to each participant history" in {
      val m = fixture()
      val changed=participantsTraversal.modify(m.topic::_)(m)

      //again notice how normal getters doesn't compose
      changed.participants.forall(_.history.contains(m.topic)) mustBe true
    }
  }

  "EXERCISE2" should {
    "(a) implement custom lenses" in {
      lazy val customTopicLens= CustomLens[Meetup,MeetupTopic](_.topic,t=>m => m.copy(topic = t))

      val meetup = fixture()
      customTopicLens.getter(meetup) mustBe "Scala DDD"
      val changed=customTopicLens.setter("Creating Custom Lenses")(meetup)
      changed.topic mustBe "Creating Custom Lenses"
    }
  }


  def fixture(): Meetup={

    val participant1 = Member("Stefan",new Email("stefan@wp.pl"),List("JavaEE for hardcores", "Docker intro"))
    val participant2 = Member("Agnieszka",new Email("aga@onet.pl"),List("Why Ruby", "pro SBT"))
    val participant3 = Member("Zdzichu",new Email("zdzisiek@google.com"),List("Why Ruby"))
    val participant4 = Member("Bozena",new Email("bogna@google.com"),List("Css in action","HTML9"))

    Meetup(
      topic= "Scala DDD",
      date = new MeetupDate("Friday 20"),
      address = Address(city = "Lodz",Street("Piotrkowska",1)),
      participants = List(participant1,participant2,participant3,participant4)
    )
  }

}

object LensesAnswerLibrary{
  type MeetupTopic=String


  class Email(val value:String) extends AnyVal
  class MeetupDate(val value:String) extends AnyVal

  case class Street(name:String,number:Int)
  case class Address(city:String,street: Street)

  case class Member(name:String,email:Email,history:List[MeetupTopic])
  case class Meetup(topic:MeetupTopic,date:MeetupDate,address: Address,participants:List[Member])


  //EXERCISE1 - a
  lazy val topicLens= Lens[Meetup,MeetupTopic](_.topic)(t=>m => m.copy(topic = t))

  //EXERCISE1 - b
  lazy val addressLens = GenLens[Meetup](_.address)
  lazy val streetLens = GenLens[Address](_.street)
  lazy val streetTupleIso = Iso[Street,(String,Int)](s=>(s.name,s.number)){case (name,number)=>Street(name,number)}


  //EXERCISE1 - c
  lazy val participantsLens=GenLens[Meetup](_.participants)
  lazy val historyLens = GenLens[Member](_.history)
  lazy val participantsTraversal = participantsLens.composeTraversal(Each.each).composeLens(historyLens)

}

object EXERCISE2Lib{
  case class CustomLens[Outer,Inner](getter:Outer=>Inner,setter:Inner => Outer => Outer){
    def composeCustomLens[Value](furtherLens:CustomLens[Inner,Value]) : CustomLens[Outer,Value] = {
      val composedGetter : Outer => Value = this.getter andThen furtherLens.getter
      val composedSetter : Value => Outer => Outer = value => outer => {
        val changedInner=furtherLens.setter(value)(this.getter(outer))
        this.setter(changedInner)(outer)
      }
      CustomLens(composedGetter,composedSetter)
    }
  }
}