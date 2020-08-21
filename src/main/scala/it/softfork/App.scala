package it.softfork

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

  verify()
}

object App extends StrictLogging {

  def main(args: Array[String]): Unit = {
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

      case Some(_) =>
        commandConf.printHelp()

      case None =>
        commandConf.printHelp()
    }
    logger.info("Export finished.")
    sys.exit()
  }
}
