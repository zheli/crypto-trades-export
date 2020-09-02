package it.softfork

import java.io.File

import akka.actor.ActorSystem
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import org.rogach.scallop._

import scala.concurrent._
import scala.concurrent.duration._

class CommandConf(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("Crypto-trades-export 0.0.1 (c) 2020 Zhe Li (linuxcity.jn@gmail.com)")
  banner("""Usage: crypto-trades-export [OPTION]... 
           |crypto-trades-export will download your trade data from any exchange and export them as a single CSV file.
           |For example, to download all trades from Kraken to kraken-trades-2020.csv file, execute:
           |crypto-trades-export --output kraken-trades-2020.csv kraken --api-key [your api key] --api-secret [your api secret]
           |Options:
           |""".stripMargin)
  footer("\nFor all other tricks, consult the documentation!")

  val output = opt[String](descr = "Output CSV filename.", short = 'o')

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
          Binance.download(
            writer,
            commandConf.binance.apiKey(),
            commandConf.binance.apiSecret()
          ),
          10.minutes
        )
        writer.close()

      case Some(commandConf.kraken) =>
        Kraken.exportCSV(
          writer,
          commandConf.kraken.apiKey(),
          commandConf.kraken.apiSecret()
        )

      case Some(_) =>
        commandConf.printHelp()

      case None =>
        commandConf.printHelp()
    }
    writer.close()
    logger.info("Export finished.")
    sys.exit()
  }
}
