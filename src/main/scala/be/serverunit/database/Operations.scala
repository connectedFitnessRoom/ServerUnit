package be.serverunit.database

import java.time.LocalDateTime

object Operations {
  def insertStartData(currentSession: Session, currentSet: Option[Set], machineID: Int, userID: Long, time: LocalDateTime, weight: Int): Option[Set] = {
      val newSet = currentSet match {
        case Some(set) =>
          Set(set.id + 1, session.id, machineID, time, None, None, weight)
        case None =>
          Set(0, session.id, machineID, time, None, None, weight)
      }
      Some(insertSet(newSet))
  }
  
  def insertData(user: String, distance: Int, timer: Float, machine: Int): Unit = {
    // Get the current set of the user based on the last session
    val currentSession = getSessionByUser(user).map(_.headOption).flatten
    val currentSet = getSetBySession(currentSession.get.id).map(_.headOption).flatten
    
    val latestRepetition = getRepetitionBySetAndSession(currentSet.get.id, currentSession.get.id).map(_.headOption).flatten
    
    latestRepetition match
      case Some(repetition) =>
        val repetitionNumber = repetition.number + 1
        insertRepetition(Repetition(repetitionNumber, currentSet.get.id, timer, distance))
      case None =>
        insertRepetition(Repetition(0, currentSet.get.id, timer, distance))
    
  }
  
  def insertEndData(user: String, reps: Int, time: Int, machine: Int): Unit = {
    // Get the current set of the user based on the last session
    val currentSession = getSessionByUser(user).map(_.headOption).flatten
    val currentSet = getSetBySession(currentSession.get.id).map(_.headOption).flatten
    
    // Update the end date of the set
    updateSet(currentSet.get.id, time)
    
    // Update the number of repetitions of the set
    updateSetRepetitions(currentSet.get.id, reps)
  }
}
