package it.softfork

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import it.softfork.Utils._
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bittrex.BittrexExchange
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.service.trade.params.DefaultTradeHistoryParamCurrencyPair
import org.rogach.scallop.Subcommand
import si.mazi.rescu.HttpStatusIOException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object Bittrex extends StrictLogging {

  val subcommand = new Subcommand("bittrex") {
    banner("""Download trading history from Bittrex 
             |""".stripMargin)
    val apiKey = opt[String](descr = "API key", required = true, short = 'k')
    val apiSecret = opt[String](descr = "API secret", required = true, short = 's')
  }

  def exportCSV(writer: CSVWriter, apiKey: String, apiSecret: String)(implicit system: ActorSystem) = {
    val exSpec = new BittrexExchange().getDefaultExchangeSpecification
    exSpec.setApiKey(apiKey)
    exSpec.setSecretKey(apiSecret)

    val exchange = ExchangeFactory.INSTANCE.createExchange(exSpec).asInstanceOf[BittrexExchange]
    val currencyPairs = exchange.getExchangeSymbols.asScala.toSeq
    logger.info(s"Found ${currencyPairs.length} currency pairs")
    val tradeService = exchange.getTradeService
    val sleepTime = 1000

    def exportTradeHistory(pair: CurrencyPair) = Future {
      logger.debug(s"Getting trades for $pair")
      val tradingPair = new DefaultTradeHistoryParamCurrencyPair(pair)
      try {
        val trades = tradeService.getTradeHistory(tradingPair).getUserTrades.asScala.toSeq
        if (trades.length > 0) {
          logger.info(s"Found ${trades.length} trades for $pair")
          trades.foreach(trade => writer.writeRow(userTradeToRow("bittex", trade)))
        }
        trades
      } catch {
        case e: HttpStatusIOException if e.getHttpStatusCode == 404 =>
          logger.warn(s"Pair $pair trades are unavailable at the moment.")
          Seq.empty
        case e: HttpStatusIOException if e.getHttpStatusCode == 429 =>
          logger.warn(s"API rate limit hit! Will skip the pair $pair and wait for ${sleepTime / 1000} second.")
          Thread.sleep(sleepTime)
          Seq.empty
      }
    }

    val source = Source(currencyPairs)
    source
      .throttle(30, 1.minute)
      .mapAsyncUnordered(2) { pair =>
        exportTradeHistory(pair)
      }
      .runWith(Sink.seq)
      .map(_.flatten)
      .map { trades =>
        logger.info(s"Exported ${trades.length} trades")
        trades
      }
  }
}
