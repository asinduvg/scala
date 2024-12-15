import zio.*

//import compiletime.constValue

// Featherweight dependency injection library, inspired by the use case
// laid out in the ZIO course of RockTheJVM.

@main def Test =
  println()
//  import TupleUtils.*

//  type P = (Int, String, List[Int])
//  val x: P = (11, "hi", List(1, 2, 3))
//  val selectInt = summon[Select[P, Int]]
//  println(selectInt(x))
//  val selectString = summon[Select[P, String]]
//  println(selectString(x))
//  val selectList = summon[Select[P, List[Int]]]
//  println(selectList(x))
//  val selectObject = summon[Select[P, Object]]
//  println(selectObject(x)) // prints "hi"
//  println(s"\nDirect:")
//  Explicit().test()
//  println(s"\nInjected")
//  Injected().test()
//  println(s"\nInjected2")
//  Injected2().test()

object domain:
  case class User(name: String, email: String)

  class UserSubscription(emailService: EmailService, db: UserDatabase):
    def subscribe(user: User) =
      emailService.email(user)
      db.insert(user)

  object UserSubscription:
    val layer = ZLayer.fromFunction(UserSubscription(_, _))

  class EmailService:
    def email(user: User) =
      println(s"You've just been subscribed to RockTheJVM. Welcome, ${user.name}")

  object EmailService:
    val layer = ZLayer.succeed(EmailService())

  class UserDatabase(pool: ConnectionPool):
    def insert(user: User) =
      val conn = pool.get()
      conn.runQuery(s"insert into subscribers(name, email) values ${user.name} ${user.email}")

  object UserDatabase:
    val layer = ZLayer.fromFunction(UserDatabase(_))

  class ConnectionPool(n: Int):
    def get(): Connection =
      println(s"Acquired connection")
      Connection()

  object ConnectionPool:
    val layer = ZLayer.succeed(ConnectionPool(10))

  class Connection():
    def runQuery(query: String): Unit =
      println(s"Executing query: $query")
end domain

/** Demonstrator for explicit dependency construction */
class Explicit:

  import domain.*

  def test() =
    val subscriptionService =
      UserSubscription(
        EmailService(),
        UserDatabase(
          ConnectionPool(10)
        )
      )

    def subscribe(user: User) =
      val sub = subscriptionService
      sub.subscribe(user)

    subscribe(User("Daniel", "daniel@RocktheJVM.com"))
    subscribe(User("Martin", "odersky@gmail.com"))

end Explicit

///** The same application as `Explicit` but using dependency injection */
//class Injected:
//  import Providers.*
//
//  case class User(name: String, email: String)
//
//  class UserSubscription(using Provider[(EmailService, UserDatabase)]):
//    def subscribe(user: User) =
//      provided[EmailService].email(user)
//      provided[UserDatabase].insert(user)
//
//  class EmailService:
//    def email(user: User) =
//      println(s"You've just been subscribed to RockTheJVM. Welcome, ${user.name}")
//
//  class UserDatabase(using Provider[ConnectionPool]):
//    def insert(user: User) =
//      val conn = provided[ConnectionPool].get()
//      conn.runQuery(s"insert into subscribers(name, email) values ${user.name} ${user.email}")
//
//  class ConnectionPool(n: Int):
//    def get(): Connection =
//      println(s"Acquired connection")
//      Connection()
//
//  class Connection():
//    def runQuery(query: String): Unit =
//      println(s"Executing query: $query")
//
//  def test() =
//    given Provider[EmailService]     = provide(EmailService())
//    given Provider[ConnectionPool]   = provide(ConnectionPool(10))
//    given Provider[UserDatabase]     = provide(UserDatabase())
//    given Provider[UserSubscription] = provide(UserSubscription())
//
//    def subscribe(user: User)(using Provider[UserSubscription]) =
//      val sub = provided[UserSubscription]
//      sub.subscribe(user)
//
//    subscribe(User("Daniel", "daniel@RocktheJVM.com"))
//    subscribe(User("Martin", "odersky@gmail.com"))
//  end test
//
//  // explicit version, not used here
//  object explicit:
//    val subscriptionService =
//      UserSubscription(
//        using provide(
//          EmailService(),
//          UserDatabase(
//            using provide(
//              ConnectionPool(10)
//            )
//          )
//        )
//      )
//
//    given Provider[UserSubscription] = provide(subscriptionService)
//  end explicit
//end Injected
//
///** Injected with builders in companion objects */
//class Injected2:
//  import Providers.*
//
//  case class User(name: String, email: String)
//
//  class UserSubscription(emailService: EmailService, db: UserDatabase):
//    def subscribe(user: User) =
//      emailService.email(user)
//      db.insert(user)
//  object UserSubscription:
//    def apply()(using Provider[(EmailService, UserDatabase)]): UserSubscription =
//      new UserSubscription(provided[EmailService], provided[UserDatabase])
//
//  class EmailService:
//    def email(user: User) =
//      println(s"You've just been subscribed to RockTheJVM. Welcome, ${user.name}")
//
//  class UserDatabase(pool: ConnectionPool):
//    def insert(user: User) =
//      pool.get().runQuery(s"insert into subscribers(name, email) values ${user.name} ${user.email}")
//  object UserDatabase:
//    def apply()(using Provider[(ConnectionPool)]): UserDatabase =
//      new UserDatabase(provided[ConnectionPool])
//
//  class ConnectionPool(n: Int):
//    def get(): Connection =
//      println(s"Acquired connection")
//      Connection()
//
//  class Connection():
//    def runQuery(query: String): Unit =
//      println(s"Executing query: $query")
//
//  def test() =
//    given Provider[EmailService]     = provide(EmailService())
//    given Provider[ConnectionPool]   = provide(ConnectionPool(10))
//    given Provider[UserDatabase]     = provide(UserDatabase())
//    given Provider[UserSubscription] = provide(UserSubscription())
//
//    def subscribe(user: User)(using Provider[UserSubscription]) =
//      val sub = UserSubscription()
//      sub.subscribe(user)
//
//    subscribe(User("Daniel", "daniel@RocktheJVM.com"))
//    subscribe(User("Martin", "odersky@gmail.com"))
//  end test
//end Injected2

object ZIODI extends ZIOAppDefault {

  import domain.*

  val program =
    for
      service <- ZIO.service[UserSubscription]
      _ <- ZIO.attempt(service.subscribe(User("Daniel", "daniel@RocktheJVM.com")))
      _ <- ZIO.attempt(service.subscribe(User("Martin", "odersky@gmail.com")))
    yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provide(
      UserSubscription.layer,
      EmailService.layer,
      UserDatabase.layer,
      ConnectionPool.layer
    )
}

object domain2:

  import Providers.*

  case class User(name: String, email: String)

  class EmailService:
    def email(user: User) =
      println(s"You've just been subscribed to RockTheJVM. Welcome, ${user.name}")

  object EmailService:
    //    val layer = ZLayer.succeed(EmailService())
  end EmailService

  class ConnectionPool(n: Int):
    def get(): Connection =
      println(s"Acquired connection")
      Connection()

  object ConnectionPool:
    //    val layer = ZLayer.succeed(ConnectionPool(10))
  end ConnectionPool

  class UserDatabase(using Provider[ConnectionPool]):
    def insert(user: User) =
      val conn = provided[ConnectionPool].get()
      conn.runQuery(s"insert into subscribers(name, email) values ${user.name} ${user.email}")

  object UserDatabase:
    //    val layer = ZLayer.fromFunction(UserDatabase(_))
  end UserDatabase

  class UserSubscription(using Provider[(EmailService, UserDatabase)]):
    def subscribe(user: User) =
      provided[EmailService].email(user)
      provided[UserDatabase].insert(user)

  object UserSubscription:
    //    val layer = ZLayer.fromFunction(UserSubscription(_, _))
  end UserSubscription

  class Connection():
    def runQuery(query: String): Unit =
      println(s"Executing query: $query")
end domain2

/** Some things that are not part of Tuple yet, but that would be nice to have. */
object TupleUtils:

  import compiletime.ops.int.S

  /** The index of the first element type of the tuple `Xs` that is a subtype of `X` */
  // IndexOf[(Int, String, List[String]), String] = 1
  type IndexOf[Xs <: Tuple, X] <: Int = Xs match
    case X *: _ => 0
    case _ *: ys => S[IndexOf[ys, X]]
  //

  /** A trait describing a selection from a tuple `Xs` returning an element of type `X` */
  trait Select[Xs <: Tuple, X]:
    def apply(xs: Xs): X

  /** A given implementing `Select` to return the first element of tuple `Xs`
   * that has a static type matching `X`.
   */
  // (Int, String, List[Int])
  given [Xs <: NonEmptyTuple, X](using index: ValueOf[IndexOf[Xs, X]]): Select[Xs, X] with
    def apply(tuple: Xs): X = tuple.apply(index.value).asInstanceOf[X]

end TupleUtils

/** A featherweight library for dependency injection */
object Providers:

  import TupleUtils.*

  /** A provider is a zero-cost wrapper around a type that is intended
   * to be passed implicitly
   */
  opaque type Provider[T] = T

  def provide[X](x: X): Provider[X] = x

  def provided[X](using p: Provider[X]): X = p

  /** Project a provider to one of its element types */
  given [Xs <: Tuple, X](using ps: Provider[Xs], select: Select[Xs, X] /* can find X in the tuple */): Provider[X] =
  select(ps)

  /** Form a compound provider wrapping a tuple */
  given [X, Xs <: Tuple](using p: Provider[X], ps: Provider[Xs]): Provider[X *: Xs] =
  p *: ps // swappable tuples included

  given Provider[EmptyTuple] = EmptyTuple

end Providers

object AutomaticDependencyInjection:

  import domain2.*
  import Providers.*

  // layers
  given Provider[UserSubscription] = provide(UserSubscription())

  given Provider[EmailService] = provide(EmailService())

  given Provider[UserDatabase] = provide(UserDatabase())

  given Provider[ConnectionPool] = provide(ConnectionPool(10))
  // type system injects instances automatically


  def main(args: Array[String]): Unit =

    val subscriptionService: UserSubscription = provided[UserSubscription]

    def subscribe(user: User) =
      val sub = subscriptionService
      sub.subscribe(user)

    subscribe(User("Daniel", "daniel@RocktheJVM.com"))
    subscribe(User("Martin", "odersky@gmail.com"))
