package org.supler

import org.json4s.JsonAST._
import org.scalatest.{FlatSpec, ShouldMatchers}
import org.supler.errors.{ValidationMode, EmptyPath}

class SuplerTest extends FlatSpec with ShouldMatchers {
  "field" should "create a variable field representation" in {
    // given
    class Person {
      var f1: String = ""
      var f2: Option[Int] = Some(0)
    }

    val p1 = new Person { f1 = "s1"; f2 = Some(10) }
    val p2 = new Person { f1 = "s2"; f2 = None }

    // when
    object PersonMeta extends Supler[Person] {
      val f1Field = field(_.f1)
      val f2Field = field(_.f2)
    }

    // then
    import PersonMeta._

    f1Field.name should be ("f1")
    f1Field.read(p1) should be ("s1")
    f1Field.read(p2) should be ("s2")
    f1Field.write(p1, "s11").f1 should be ("s11")
    f1Field.write(p2, "s21").f1 should be ("s21")
    f1Field.required should be (true)

    f2Field.name should be ("f2")
    f2Field.read(p1) should be (Some(10))
    f2Field.read(p2) should be (None)
    f2Field.write(p1, None).f2 should be (None)
    f2Field.write(p2, Some(20)).f2 should be (Some(20))
    f2Field.required should be (false)
  }

  "field" should "create a case class field representation" in {
    // given
    case class Person(f1: String, f2: Option[Int], f3: Boolean, f4: String)

    val p1 = Person("s1", Some(10), f3 = true, "x1")
    val p2 = Person("s2", None, f3 = false, "x2")

    // when
    object PersonMeta extends Supler[Person] {
      val f1Field = field(_.f1)
      val f2Field = field(_.f2)
      val f3Field = field(_.f3)
      val f4Field = field(_.f4)
    }

    // then
    import PersonMeta._

    f1Field.name should be ("f1")
    f1Field.read(p1) should be ("s1")
    f1Field.read(p2) should be ("s2")
    f1Field.write(p1, "s11").f1 should be ("s11")
    f1Field.write(p2, "s21").f1 should be ("s21")
    f1Field.required should be (true)

    f2Field.name should be ("f2")
    f2Field.read(p1) should be (Some(10))
    f2Field.read(p2) should be (None)
    f2Field.write(p1, None).f2 should be (None)
    f2Field.write(p2, Some(20)).f2 should be (Some(20))
    f2Field.required should be (false)

    f3Field.name should be ("f3")
    f3Field.read(p1) should be (true)
    f3Field.read(p2) should be (false)
    f3Field.write(p1, false).f3 should be (false)
    f3Field.write(p2, true).f3 should be (true)
    f3Field.required should be (true)

    f4Field.name should be ("f4")
    f4Field.read(p1) should be ("x1")
    f4Field.read(p2) should be ("x2")
    f4Field.write(p1, "x11").f4 should be ("x11")
    f4Field.write(p2, "x21").f4 should be ("x21")
    f4Field.required should be (true)
  }

  "field" should "validate required fields" in {
    // given
    case class Person(f1: String, f2: Option[String], f3: Int, f4: Option[Int])

    val p1 = Person("s1", Some("x1"), 10, Some(11))
    val p2 = Person("", None, 12, None)
    val p3 = Person(null, null, 12, null)

    // when
    object PersonMeta extends Supler[Person] {
      val f1Field = field(_.f1)
      val f2Field = field(_.f2)
      val f3Field = field(_.f3)
      val f4Field = field(_.f4)
    }

    // then
    import PersonMeta._

    f1Field.doValidate(EmptyPath, p1, ValidationMode.All).size should be (0)
    f1Field.doValidate(EmptyPath, p2, ValidationMode.All).size should be (1)
    f1Field.doValidate(EmptyPath, p3, ValidationMode.All).size should be (1)

    f2Field.doValidate(EmptyPath, p1, ValidationMode.All).size should be (0)
    f2Field.doValidate(EmptyPath, p2, ValidationMode.All).size should be (0)
    f2Field.doValidate(EmptyPath, p3, ValidationMode.All).size should be (0)

    f3Field.doValidate(EmptyPath, p1, ValidationMode.All).size should be (0)
    f3Field.doValidate(EmptyPath, p2, ValidationMode.All).size should be (0)
    f3Field.doValidate(EmptyPath, p3, ValidationMode.All).size should be (0)

    f4Field.doValidate(EmptyPath, p1, ValidationMode.All).size should be (0)
    f4Field.doValidate(EmptyPath, p2, ValidationMode.All).size should be (0)
    f4Field.doValidate(EmptyPath, p3, ValidationMode.All).size should be (0)
  }

  "field" should "not validate empty fields if validating only filled" in {
    // given
    case class Person(f1: String)

    val p1 = Person("aaaa")
    val p2 = Person("aa")
    val p3 = Person("")
    val p4 = Person(null)

    // when
    object PersonMeta extends Supler[Person] {
      val f1Field = field(_.f1).validate(minLength(3))
    }

    // then
    import PersonMeta._

    f1Field.doValidate(EmptyPath, p1, ValidationMode.All).size should be (0)
    f1Field.doValidate(EmptyPath, p2, ValidationMode.All).size should be (1)
    f1Field.doValidate(EmptyPath, p3, ValidationMode.All).size should be (2)
    f1Field.doValidate(EmptyPath, p4, ValidationMode.All).size should be (1)

    f1Field.doValidate(EmptyPath, p1, ValidationMode.OnlyFilled).size should be (0)
    f1Field.doValidate(EmptyPath, p2, ValidationMode.OnlyFilled).size should be (1)
    f1Field.doValidate(EmptyPath, p3, ValidationMode.OnlyFilled).size should be (0)
    f1Field.doValidate(EmptyPath, p4, ValidationMode.OnlyFilled).size should be (0)
  }

  "form" should "apply json values to the entity given" in {
    // given
    case class Person(f1: String, f2: Option[Int], f3: Boolean, f4: Option[String])

    val form = Supler.form[Person](f => List(
      f.field(_.f1),
      f.field(_.f2),
      f.field(_.f3),
      f.field(_.f4)
    ))

    val jsonInOrder = JObject(
      JField("f1", JString("John")),
      JField("f2", JInt(10)),
      JField("f3", JBool(value = true)),
      JField("f4", JString("Something"))
    )

    val jsonOutOfOrder = JObject(
      JField("f3", JBool(value = true)),
      JField("f2", JInt(10)),
      JField("f4", JString("")),
      JField("f1", JString("John"))
    )

    val jsonPartial = JObject(
      JField("f1", JString("John")),
      JField("f2", JInt(10))
    )

    val p = Person("Mary", None, f3 = false, Some("Nothing"))

    // when
    val p1 = form(p).applyJSONValues(jsonInOrder).obj
    val p2 = form(p).applyJSONValues(jsonOutOfOrder).obj
    val p3 = form(p).applyJSONValues(jsonPartial).obj

    // then
    p1 should be (Person("John", Some(10), f3 = true, Some("Something")))
    p2 should be (Person("John", Some(10), f3 = true, None))
    p3 should be (Person("John", Some(10), f3 = false, Some("Nothing")))
  }

  /*"form" should "create a new entity based on the json" in {
    case class Person(f1: String, f2: Option[Int], f3: Boolean, f4: Option[String])

    val form = Supler.form[Person](f => List(
      f.field(_.f1),
      f.field(_.f2),
      f.field(_.f3),
      f.field(_.f4)
    ))

    val jsonInOrder = JObject(
      JField("f1", JString("John")),
      JField("f2", JInt(10)),
      JField("f3", JBool(value = true)),
      JField("f4", JString("Something"))
    )

    val jsonOutOfOrder = JObject(
      JField("f3", JBool(value = true)),
      JField("f2", JInt(10)),
      JField("f4", JString("")),
      JField("f1", JString("John"))
    )

    val jsonPartial1 = JObject(
      JField("f1", JString("John")),
      JField("f2", JInt(10))
    )

    val jsonPartial2 = JObject(
      JField("f1", JString("John")),
      JField("f3", JBool(value = true))
    )

    // when
    val p1 = form.createFromJSONValues(jsonInOrder)
    val p2 = form.createFromJSONValues(jsonOutOfOrder)
    val p3 = form.createFromJSONValues(jsonPartial1)
    val p4 = form.createFromJSONValues(jsonPartial2)

    // then
    p1 should be (Right(Person("John", Some(10), f3 = true, Some("Something"))))
    p2 should be (Right(Person("John", Some(10), f3 = true, Some("Something"))))
    p3.left.map(_.size) should be (Left(1))
    p4 should be (Right(Person("John", None, f3 = true, None)))
  }*/

  "table" should "create a case class field representation" in {
    // given
    case class Car(make: String, age: Int)
    case class Person(name: String, cars: List[Car])

    val p1 = Person("p1", List(Car("m1", 10), Car("m2", 20)))
    val p2 = Person("p2", Nil)

    // when
    import Supler._
    val carForm = form[Car](f => List(
      f.field(_.make),
      f.field(_.age)
    ))
    object PersonMeta extends Supler[Person] {
      val carsField = subform(_.cars, carForm)
    }

    // then
    import PersonMeta.carsField

    carsField.name should be ("cars")
    carsField.read(p1) should be (List(Car("m1", 10), Car("m2", 20)))
    carsField.read(p2) should be (Nil)
    carsField.write(p1, Nil).cars should be (Nil)
    carsField.write(p2, List(Car("m3", 30))).cars should be (List(Car("m3", 30)))
  }

  "form with a table" should "apply json values to the entity given" in {
    // given
    case class Car(make: String, age: Int)
    case class Person(name: String, cars: List[Car])

    import Supler._
    val carForm = form[Car](f => List(
      f.field(_.make),
      f.field(_.age)
    ))
    val personForm = form[Person](f => List(
      f.field(_.name),
      f.subform(_.cars, carForm)
    ))

    val jsonInOrder = JObject(
      JField("cars", JArray(List(
        JObject(
          JField("make", JString("m1")),
          JField("age", JInt(10))
        ),
        JObject(
          JField("age", JInt(20)),
          JField("make", JString("m2"))
        )
      ))),
      JField("name", JString("John"))
    )

    // when
    val result = personForm(Person("", Nil)).applyJSONValues(jsonInOrder)

    // then
    result.errors should be ('empty)
    result.obj should be (Person("John", List(Car("m1", 10), Car("m2", 20))))
  }

  "form" should "generate empty classes" in {
    // given
    case class Car(make: String, age: Int, middleName: Option[String])
    case class Person(name: String, age: Long, height: Double, smokes: Boolean, car: Car,
      colors: List[String], friends: Set[String])

    // when
    import Supler._
    val carForm = form[Car](f => List())
    val personForm = form[Person](f => List())

    // then
    val emptyCar = Car("", 0, None)
    carForm.createEmpty() should be (emptyCar)
    personForm.createEmpty() should be (Person("", 0L, 0.0d, smokes = false, null, Nil, Set()))
  }
}
