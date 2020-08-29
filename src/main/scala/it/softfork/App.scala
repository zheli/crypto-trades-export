package it.softfork

import java.io.File
import java.text.SimpleDateFormat
import java.time.ZonedDateTime

import akka.actor.ActorSystem
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import org.rogach.scallop._

import scala.concurrent._
import scala.concurrent.duration._

class CommandConf(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("Crypto-trades-export 0.0.1 (c) 2020 Zhe Li (linuxcity.jn@gmail.com)")
  banner("""Usage: test [OPTION]... 
           |test is an awesome program, which does something funny
           |Options:
           |""".stripMargin)
  footer("\nFor all other tricks, consult the documentation!")

  val exchange = opt[String](descr = "Exchange Id, see README.md")

  val coinbasepro = new Subcommand("coinbasepro") {
    banner("""Download trading history from Coinbase Pro
          |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true)
    val apiSecret = opt[String](descr = "API secret", required = true)
    val apiPassphrase = opt[String](descr = "API passphrase", required = true)
  }
  addSubcommand(coinbasepro)

  val binance = new Subcommand("binance") {
    banner("""Download trading history from Binance
             |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true, short = 'k')
    val apiSecret = opt[String](descr = "API secret", required = true, short = 's')
  }
  addSubcommand(binance)

  val kraken = Kraken.krakenSubcommand
  addSubcommand(kraken)

  verify()
}

object App extends StrictLogging {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("crypto-history-backup")
    val commandConf = new CommandConf(args)

    commandConf.subcommand match {
      case Some(commandConf.coinbasepro) =>
        Await.result(
          CoinbasePro.download(
            commandConf.coinbasepro.apiKey(),
            commandConf.coinbasepro.apiSecret(),
            commandConf.coinbasepro.apiPassphrase()
          ),
          10.minutes
        )

      case Some(commandConf.binance) =>
        val now = ZonedDateTime.now().toLocalDateTime
        val f = new File(s"test-binance-$now.csv")
        val writeHeader = !f.exists()
        val writer = CSVWriter.open(f)
        if (writeHeader) {
          writer.writeRow(List("Exchange", "Type", "Pair", "Amount", "Price", "Time"))
        }
        Await.result(
          Binance.download(
            writer,
            commandConf.binance.apiKey(),
            commandConf.binance.apiSecret()
          ),
          10.minutes
        )
        writer.close()

      case Some(commandConf.kraken) =>
        val now = ZonedDateTime.now().toLocalDateTime
        val f = new File(s"test-kraken-$now.csv")
        val writeHeader = !f.exists()
        val writer = CSVWriter.open(f)
        if (writeHeader) {
          writer.writeRow(List("Exchange", "Type", "Pair", "Amount", "Price", "Time"))
        }
        Kraken.exportCSV(
          writer,
          commandConf.kraken.apiKey(),
          commandConf.kraken.apiSecret()
        )
        writer.close()

      case Some(_) =>
        commandConf.printHelp()

      case None =>
        commandConf.printHelp()
    }
    logger.info("Export finished.")
    sys.exit()
  }
}
