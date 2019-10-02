package xyz.tg44.openscad.utils


import scala.sys.process._

/*
Original source:
https://github.com/dzufferey/misc-scala-utils/blob/master/src/main/scala/dzufferey/utils/SysCmd.scala
All rights to the original author!
 */


/** executing command as children process */
object SysCmd {

  type ExecResult = (Int, String, String)

  //TODO add an option for timeout
  def apply(cmds: Array[String], input: Option[String], addToEnv: (String,String)*): ExecResult = {
    val process = Process(cmds, None, addToEnv:_*)
    val withInput = input match {
      case Some(str) => process #< ( new java.io.ByteArrayInputStream(str.getBytes) )
      case None => process
    }

    val bufferOut = new StringBuilder()
    val bufferErr = new StringBuilder()
    val processLogger =
      ProcessLogger(
        line => {bufferOut append line; bufferOut append "\n"},
        line => {bufferErr append line; bufferErr append "\n"}
      )
    //Logger("Utils", Info, "Executing "+ cmds.mkString(""," ",""))
    val exitCode = withInput ! processLogger
    (exitCode, bufferOut.toString, bufferErr.toString)
  }

  def apply(cmds: Array[String], input: String, addToEnv: (String,String)*): ExecResult =
    apply(cmds, Some(input), addToEnv: _*)

  def apply(cmds: Array[String], addToEnv: (String,String)*): ExecResult =
    apply(cmds, None, addToEnv: _*)


  def execWithoutOutput(cmds: Array[String], input: Option[String], addToEnv: (String,String)*): Int = {
    val process = Process(cmds, None, addToEnv:_*)
    val withInput = input match {
      case Some(str) => process #< ( new java.io.ByteArrayInputStream(str.getBytes) )
      case None => process
    }
    //Logger("Utils", Info, "Executing "+ cmds.mkString(""," ",""))
    withInput.!
  }

  def execRedirectToOutput(cmds: Array[String], input: Option[String], addToEnv: (String,String)*): Int = {
    val process = Process(cmds, None, addToEnv:_*)
    val withInput = input match {
      case Some(str) => process #< ( new java.io.ByteArrayInputStream(str.getBytes) )
      case None => process
    }
    val processLogger = ProcessLogger(
      out => Console.println(out),
      err => Console.err.println(err))
    //Logger("Utils", Info, "Executing "+ cmds.mkString(""," ",""))
    withInput ! processLogger
  }

  def execOutputAndLog(cmds: Array[String], input: Option[String], addToEnv: (String,String)*): ExecResult = {
    val process = Process(cmds, None, addToEnv:_*)
    val withInput = input match {
      case Some(str) => process #< ( new java.io.ByteArrayInputStream(str.getBytes) )
      case None => process
    }

    val bufferOut = new StringBuilder()
    val bufferErr = new StringBuilder()
    val processLogger =
      ProcessLogger(
        line => { Console.println(line); bufferOut append line; bufferOut append "\n"},
        line => { Console.err.println(line); bufferErr append line; bufferErr append "\n"}
      )
    //Logger("Utils", Info, "Executing "+ cmds.mkString(""," ",""))
    val exitCode = withInput ! processLogger
    (exitCode, bufferOut.toString, bufferErr.toString)
  }

}
