package it.softfork

import java.io.File

import akka.actor.ActorSystem
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent._
import scala.concurrent.duration._

object App extends StrictLogging {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("crypto-history-backup")
    val commandConf = new CommandConfig(args)
    val filename = commandConf.output.toOption.getOrElse {
      commandConf.printHelp()
      sys.exit(1)
    }
    val file = new File(filename)

    val writeHeader = !file.exists()
    val writer = CSVWriter.open(file)
    if (writeHeader) {
      writer.writeRow(List("Exchange", "Type", "Pair", "Amount", "Price", "Time"))
    }

    commandConf.subcommand match {
      case Some(commandConf.coinbasepro) =>
        Await.result(
          CoinbasePro.exportCSV(
            writer,
            commandConf.coinbasepro.apiKey(),
            commandConf.coinbasepro.apiSecret(),
            commandConf.coinbasepro.apiPassphrase()
          ),
          10.minutes
        )

      case Some(commandConf.binance) =>
        Await.result(
          Binance.exportCSV(
            writer,
            commandConf.binance.apiKey(),
            commandConf.binance.apiSecret()
          ),
          10.minutes
        )

      case Some(commandConf.kraken) =>
        Kraken.exportCSV(
          writer,
          commandConf.kraken.apiKey(),
          commandConf.kraken.apiSecret()
        )

      case Some(commandConf.bittrex) =>
        Await.result(
          Bittrex.exportCSV(
            writer,
            commandConf.bittrex.apiKey(),
            commandConf.bittrex.apiSecret()
          ),
          30.minutes
        )

      case _ =>
        commandConf.printHelp()
    }
    writer.close()
    logger.info("Export finished.")
    sys.exit()
  }
}
