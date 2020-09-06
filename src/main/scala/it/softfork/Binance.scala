package it.softfork

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.StrictLogging
import it.softfork.Utils._
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.binance.BinanceExchange
import org.knowm.xchange.binance.service.BinanceTradeHistoryParams
import org.knowm.xchange.currency.CurrencyPair

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object Binance extends StrictLogging {

  def exportCSV(writer: CSVWriter, apiKey: String, apiSecert: String)(implicit system: ActorSystem) = {
    val exSpec = new BinanceExchange().getDefaultExchangeSpecification
    exSpec.setApiKey(apiKey)
    exSpec.setSecretKey(apiSecert)
    val exchange = ExchangeFactory.INSTANCE.createExchange(exSpec)
    val tradeService = exchange.getTradeService
    val currencyPairs = exchange.getExchangeSymbols.asScala.toSeq
    logger.info(s"Found ${currencyPairs.length} currency pairs")

    def exportTradeHistory(pair: CurrencyPair) = Future {
      logger.debug(s"Getting trades for $pair")
      val trades = tradeService.getTradeHistory(new BinanceTradeHistoryParams(pair)).getUserTrades.asScala.toSeq
      if (trades.length > 0) {
        logger.info(s"Found ${trades.length} trades for $pair")
      }
      trades.foreach(trade => writer.writeRow(userTradeToRow("binance", trade)))
      trades
    }

    val source = Source(currencyPairs)
    source
      .throttle(240, 1.minute)
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
