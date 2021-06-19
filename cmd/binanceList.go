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
	"fmt"

	"github.com/adshao/go-binance"
	"github.com/spf13/cobra"
)

// binanceListCmd represents the binanceList command
var binanceListCmd = &cobra.Command{
	Use:   "list",
	Short: "List all Binance trading pairs",
	Long: "",
	Run: func(cmd *cobra.Command, args []string) {
		apiKey, _ := cmd.Flags().GetString("key")
		apiSecret, _ := cmd.Flags().GetString("secret")
		debug, _ := cmd.Flags().GetBool("debug")
		listTradingPairs(apiKey, apiSecret, debug)
	},
}

func init() {
	binanceCmd.AddCommand(binanceListCmd)
}

func listTradingPairs(apiKey string, apiSecret string, debug bool) {
	client := binance.NewClient(apiKey, apiSecret)
	client.Debug = debug

	exchangeInfo, err := client.NewExchangeInfoService().Do(context.Background())
	if err != nil {
		fmt.Println(err)
		return
	}
	symbols := exchangeInfo.Symbols
	for _, s := range symbols {
		fmt.Printf("Trading pair: %s\n", s.Symbol)
	}
	fmt.Printf("Found %d trading pairs\n", len(symbols))
}
