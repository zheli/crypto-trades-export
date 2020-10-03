/*
Copyright © 2020 Zhe Li <linuxcity.jn@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package cmd

import (
	"context"
	"encoding/csv"
	"fmt"
	"log"
	"os"

	"time"

	"github.com/adshao/go-binance"
	"github.com/cheggaaa/pb/v3"
	"github.com/spf13/cobra"
)

// binanceCmd represents the binance command
var binanceCmd = &cobra.Command{
	Use:   "binance",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		apiKey, _ := cmd.Flags().GetString("key")
		apiSecret, _ := cmd.Flags().GetString("secret")
		output, _ := cmd.Flags().GetString("output")
		fmt.Printf("Exporting trading history from binance. Output file: %s\n", output)
		exportTrades(apiKey, apiSecret, output)
	},
}

func init() {
	rootCmd.AddCommand(binanceCmd)
	binanceCmd.Flags().StringP("key", "k", "", "API key")
	binanceCmd.Flags().StringP("secret", "s", "", "API secret")
}

func getTradingType(trade *binance.TradeV3)  string  {
	if (trade.IsBuyer) {
		return "bid"
	} else {
		return "ask"
	}
}

func millionsecondsToDatetimeStringg(t int64) string {
	return time.Unix(0, t * int64(time.Millisecond)).Format("2006-01-02 15:04:05")
}

func exportTrades(apiKey string, apiSecret string, output string) {
	client := binance.NewClient(apiKey, apiSecret)
	listTradesService := client.NewListTradesService()
	exchangeInfo, err := client.NewExchangeInfoService().Do(context.Background())
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Printf("Found %d trading pairs\n", len(exchangeInfo.Symbols))

	f, err := os.Create(output); if err !=nil {
		log.Fatal(err)
		return
	}
	w := csv.NewWriter(f)
	header := []string{"exchange", "pair", "trading_type", "quantity", "price", "timestamp"}
	w.Write(header)

	bar := pb.StartNew(len(exchangeInfo.Symbols))
	for _, s := range exchangeInfo.Symbols {
		trades, err := listTradesService.Symbol(s.Symbol).Do(context.Background())
		if err != nil {
			fmt.Printf("Failed to download, %s\n", err)
			continue
		}
		if len(trades) > 0 {
			fmt.Printf("Found trading history for %s\n", s.Symbol)
			for _, t := range trades {
				row := []string{
					"binance",
					t.Symbol,
					getTradingType(t),
					t.Quantity,
					t.Price,
					millionsecondsToDatetimeStringg(t.Time),
				}
				w.Write(row)
			}
		}
		bar.Increment()
	}

	w.Flush()
	if err := w.Error(); err != nil {
		log.Fatal(err)
	}
}
