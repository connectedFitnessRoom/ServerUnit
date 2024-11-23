package be.serverunit.actors

object MachineActor(machine: Int) {
  def apply(): Behavior[MachineMessage] = Behaviors.setup(context =>
    // Variables to store the current session and set
    var currentSession: Option[Session] = None
    var currentSet: Option[Set] = None
  
    Behaviors.receiveMessage {
      case StartData(user, receivedTime, weight) =>
        // Get the current session, waiting for the result
        getSessionByUserAndDate(user, receivedTime.toLocalDate).onComplete {
          case Success(session) =>
            currentSession = session
            // Get the current set, waiting for the result
            getSetBySession(session.get.id).onComplete {
              case Success(set) =>
                currentSet = set
                // Insert the start data into the database
                insertStartData(session.get, set, machine, user, receivedTime, weight)
              case Failure(exception) =>
                println(s"Error: ${exception.getMessage}")
            }
          case Failure(exception) =>
            println(s"Error: ${exception.getMessage}")
        }
  
  
      case Data(user, distance, timer) =>
        // Check if currentSession and currentSet are defined otherwise log an error
        if (currentSession.isEmpty || currentSet.isEmpty) {
          println("Error: currentSession or currentSet is not defined")
        }
        // Inserting the data into the database
        insertData(user, distance, timer)
  
  
      case EndData(user, reps, time) =>
        // Inserting the data into the database
        insertEndData(user, reps, time)
    }

}
