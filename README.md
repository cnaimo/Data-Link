# About
DataLink is a simple Java interface for the older IEX API. This project is still in developement and currently offers tops, 
last, deep, hist, and book endpoints. All data is provided by IEX.

# Contact
Questions? Feel free to reach out via my LinkedIn on my profile page. I'm also seeking employment in data science/finance!

# Dependencies
* [json_simple V3.1.1](https://github.com/cliftonlabs/json-simple)

# Important Links
* [IEX API Terms of Use](https://iextrading.com/api-terms/)

# Usage
Clone this repo and add DataLink.java to your project directory. Start by defining a new instance of the 
DataLink class in your main function. All methods in the DataLink class return JsonArray types.

``` java
DataLink iex = new DataLink();

// get last trade data for AMD and NFLX
Vector<String> tickers = new Vector<>(2);
tickers.add("AMD");
tickers.add("NFLX");
JsonArray j = iex.last(tickers);
```
For last and tops methods, passing an empty vector will return data for all available tickers on IEX

```java
JsonArray j_last = iex.last(new Vector<String>);
JsonArray j_tops = iex.tops(new Vector<String>);
```

The hist method can optionally download historical data in addition to fetching API data. Passing a blank string will fetch 
data for all available days. Downloads will be saved in the format IEX_DEEP_" + date + ".gz"
```java
// download data for date
iex.hist("20200501", true);

// download all available data
iex.hist("", true);
```

